package com.techelevator.tenmo.controller;

import java.security.Principal;
import java.util.List;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferStatus;

import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@RestController
@RequestMapping("/transfers")
@PreAuthorize("isAuthenticated()")
public class TransferController {

    private final TransferDao transferDao;
    private final UserDao userDao;
    private final AccountDao accountDao;

    @Autowired
    public TransferController(TransferDao transferDao, UserDao userDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @GetMapping
    public List<Transfer> getTransfers(Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return transferDao.getTransfersByUserId(user.getId());
    }

    @GetMapping(params = "status")
    public List<Transfer> getTransfersByStatus(@RequestParam("status") String status, Principal principal) {
        User user = userDao.findByUsername(principal.getName());
        return transferDao.getTransfersByUserIdAndStatus(user.getId(), status);
    }

    @GetMapping("/{id}")
    public Transfer getTransferById(@PathVariable int id) {
        Transfer transfer = transferDao.getTransferById(id);
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
        }
        return transfer;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer createTransfer(@RequestBody @Valid Transfer transfer) {
        Transfer createdTransfer = transferDao.create(transfer);
        if (createdTransfer == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create transfer");
        }
        return createdTransfer;
    }

    @PutMapping("/{transferId}/status/{newStatus}")
    public ResponseEntity<Void> updateTransfer(@PathVariable int transferId, @PathVariable String newStatus, Principal principal) {
        Transfer transfer = transferDao.getTransferById(transferId);
        User user = userDao.findByUsername(principal.getName());
        if (transfer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer not found");
        }

        // Ensure the status is valid
        TransferStatus status;
        try {
            status = TransferStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid transfer status");
        }

        // Ensure the status is changing
        if (transfer.getTransfer_status() == status) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New status is same as current status");
        }

        // Ensure the transfer is pending
        if (transfer.getTransfer_status() != TransferStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer is not pending");
        }

//        // Ensure the user is not approving/rejecting his own request
//        if (transfer.getAccount_to() == accountDao.getAccountIdByUserId(user.getId())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not authorized to approve or reject his own transfer request");
//        }

        // Ensure the user has enough money to approve the request
        if (status == TransferStatus.APPROVED && transfer.getAmount().compareTo(accountDao.getAccountBalanceByUserId(user.getId())) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not have enough money to approve the transfer");
        }

        transfer.setTransfer_status(status);
        boolean isUpdated = transferDao.update(transferId, transfer);

        if (!isUpdated) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update transfer");
        }

        return ResponseEntity.noContent().build();
    }


}
