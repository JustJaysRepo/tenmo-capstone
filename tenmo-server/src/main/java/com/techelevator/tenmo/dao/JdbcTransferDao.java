package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.AccountNotFoundException;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferStatus;
import com.techelevator.tenmo.model.TransferType;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class JdbcTransferDao implements TransferDao {
    private static final String TRANSFER_BASE_SQL = "SELECT transfer_id, tt.transfer_type_desc, ts.transfer_status_desc, account_from, account_to, amount " + "FROM transfer " + "JOIN transfer_type AS tt ON transfer.transfer_type_id = tt.transfer_type_id " + "JOIN transfer_status AS ts ON transfer.transfer_status_id = ts.transfer_status_id";

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransferDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Transfer> findAll() {
        return getTransfers(TRANSFER_BASE_SQL);
    }

    @Override
    public Transfer getTransferById(int id) {
        String sql = TRANSFER_BASE_SQL + " WHERE transfer_id = ?";
        return getSingleTransfer(sql, id);
    }

    @Override
    public List<Transfer> getTransfersByUserId(int userId) { // remove or to have it form only from account
        String sql = TRANSFER_BASE_SQL + " JOIN account a ON (transfer.account_from = a.account_id OR transfer.account_to = a.account_id) AND a.user_id = ?";
        return getTransfers(sql, userId);
    }

    @Override
    public Transfer create(Transfer transfer) {

        // Check the transfer type and set status accordingly
        if (TransferType.SEND.equals(transfer.getTransfer_type())) {
            transfer.setTransfer_status(TransferStatus.APPROVED);
        } else if (TransferType.REQUEST.equals(transfer.getTransfer_type())) {
            transfer.setTransfer_status(TransferStatus.PENDING);
        }
        // validate the transfer
        validateTransfer(transfer);



        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " + "VALUES (?, ?, ?, ?, ?) RETURNING transfer_id";

        Optional<Integer> transferTypeIdOptional = getTransferTypeId(transfer.getTransfer_type().getDisplayValue());
        Optional<Integer> transferStatusIdOptional = getTransferStatusId(transfer.getTransfer_status().getDisplayValue());

        // Check if transfer type ID is present
        if (transferTypeIdOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid transfer type");
        }

        // Check if transfer status ID is present
        if (transferStatusIdOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid transfer status");
        }

        // If both are present, you can safely get the values
        Integer transferTypeId = transferTypeIdOptional.get();
        Integer transferStatusId = transferStatusIdOptional.get();

        Integer newId = jdbcTemplate.queryForObject(sql, Integer.class, transferTypeId, transferStatusId, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount());
        if(newId == null) {
            throw new RuntimeException("Failed to insert transfer, no ID obtained.");
        }
        transfer.setTransfer_id(newId);

        return transfer;
    }

    @Override
    public boolean update(int transferId, Transfer transfer) {
        validateTransfer(transfer);

        String sql = "UPDATE transfer SET transfer_type_id = ?, transfer_status_id = ?, account_from = ?, account_to = ?, amount = ? " + "WHERE transfer_id = ?";

        Optional<Integer> transferTypeIdOptional = getTransferTypeId(transfer.getTransfer_type().getDisplayValue());
        Optional<Integer> transferStatusIdOptional = getTransferStatusId(transfer.getTransfer_status().getDisplayValue());

        // Check if transfer type ID is present
        if (transferTypeIdOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid transfer type");
        }

        // Check if transfer status ID is present
        if (transferStatusIdOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid transfer status");
        }

        // If both are present, you can safely get the values
        Integer transferTypeId = transferTypeIdOptional.get();
        Integer transferStatusId = transferStatusIdOptional.get();

        jdbcTemplate.update(sql, transferTypeId, transferStatusId, transfer.getAccount_from(), transfer.getAccount_to(), transfer.getAmount(), transferId);

        return true;
    }


    @Override
    public List<Transfer> getTransfersByUserIdAndStatus(int userId, String status) {
        String sql = TRANSFER_BASE_SQL + " JOIN account a ON (transfer.account_from = a.account_id OR transfer.account_to = a.account_id) AND a.user_id = ? " + "WHERE ts.transfer_status_desc = ?";
        return getTransfers(sql, userId, status);
    }

    private List<Transfer> getTransfers(String sql, Object... params) {
        List<Transfer> transfers = new ArrayList<>();
        SqlRowSet results = jdbcTemplate.queryForRowSet(sql, params);
        while (results.next()) {
            transfers.add(mapRowTransfer(results));
        }
        return transfers;
    }

    private Transfer getSingleTransfer(String sql, Object... params) {
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, params);
        if (result.next()) {
            return mapRowTransfer(result);
        }
        throw new EmptyResultDataAccessException("Transfer not found", 1);
    }

    private Transfer mapRowTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransfer_id(rs.getInt("transfer_id"));
        transfer.setTransfer_type(TransferType.valueOf(rs.getString("transfer_type_desc").toUpperCase()));
        transfer.setTransfer_status(TransferStatus.valueOf(rs.getString("transfer_status_desc").toUpperCase()));
        transfer.setAccount_from(rs.getInt("account_from"));
        transfer.setAccount_to(rs.getInt("account_to"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        return transfer;
    }

    /**
     * Gets the Transfer Type Id from the transfer_type table.
     *
     * @param transfer_type_desc The transfer type description
     * @return The id of the transfer type
     */
    private Optional<Integer> getTransferTypeId(String transfer_type_desc) {
        String getTransferTypeIdSql = "SELECT transfer_type_id FROM transfer_type WHERE transfer_type_desc = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(getTransferTypeIdSql, transfer_type_desc);
        if (result.next()) {
            return Optional.of(result.getInt("transfer_type_id"));
        } else {
            return Optional.empty();
        }
    }


    /**
     * Gets the Transfer Status Id from the transfer_status table.
     *
     * @param transfer_status_desc The transfer status description
     * @return The id of the transfer status
     */
    private Optional<Integer> getTransferStatusId(String transfer_status_desc) {
        String getTransferStatusIdSql = "SELECT transfer_status_id FROM transfer_status WHERE transfer_status_desc = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(getTransferStatusIdSql, transfer_status_desc);
        if (result.next()) {
            return Optional.of(result.getInt("transfer_status_id"));
        } else {
            return Optional.empty();
        }
    }


    private void validateTransfer(Transfer transfer) {
        System.out.println("submitted transfer");
        // Validate required account.
        if (!checkAccountExists(transfer.getAccount_from()) || !checkAccountExists(transfer.getAccount_to())) {
            throw new IllegalArgumentException("Accounts for the transfer do not exist");
        }
        // Validate different account ownership.
        if (!checkAccountOwners(transfer.getAccount_from(), transfer.getAccount_to())) {
            throw new IllegalArgumentException("Accounts for the transfer are owned by the same user");
        }

        // Validate transfer type and status.
        if(transfer.getTransfer_type() == null || transfer.getTransfer_status() == null) {
            System.out.println("Running the test for transfer type and status");
            throw new IllegalArgumentException("Transfer type and status are required");
        }

    }

    private boolean checkAccountExists(int accountId) {
        String sql = "SELECT account_id FROM account WHERE account_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, accountId);
        if (result.next()) {
            return true;
        } else {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }
    }

    /**
     * Validates if the account owners are different for the transfer.
     *
     * @param fromAccountId The id of the account from which the transfer is to be made
     * @param toAccountId   The id of the account to which the transfer is to be made
     * @throws AccountNotFoundException if the account with the provided id is not found
     */
    private boolean checkAccountOwners(final int fromAccountId, final int toAccountId) {
        final String sql = "SELECT user_id FROM account WHERE account_id = ?";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, fromAccountId);

        if (!result.next()) {
            throw new AccountNotFoundException("No account found with id: " + fromAccountId);
        }

        final int fromUserId = result.getInt("user_id");
        result = jdbcTemplate.queryForRowSet(sql, toAccountId);

        if (!result.next()) {
            throw new AccountNotFoundException("No account found with id: " + toAccountId);
        }

        final int toUserId = result.getInt("user_id");
        return fromUserId != toUserId;
    }

}
