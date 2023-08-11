package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcAccountDAO;
import com.techelevator.tenmo.model.Account;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/account/")
public class AccountController {
    private JdbcAccountDAO accountJdbcDAO;

    public AccountController(JdbcAccountDAO accountJdbcDAO){
        this.accountJdbcDAO=accountJdbcDAO;
    }
    @GetMapping("user/{id}")
    public Account getAccountByUserId(@PathVariable int id) {
        return accountJdbcDAO.getAccountByUserId(id);
    }
    @GetMapping("")
    public int getAccountIdByUserId(@PathVariable int id){
        return accountJdbcDAO.getAccountIdByUserId(id);
    }
    @GetMapping("{id}/balance")
    public BigDecimal getBalanceByUserId(@PathVariable int id) {
        return accountJdbcDAO.getAccountBalanceByUserId(id);
    }

    @PutMapping("{id}")
    public void updateAccountByUserId(@RequestBody Account account, @PathVariable int id) {
        accountJdbcDAO.updateAccountByUserId(account, id);
    }

    @GetMapping("{id}")
    public Account getAccountById(@PathVariable int id) {
        return accountJdbcDAO.getAccountById(id);
    }

//======== should not be needed since registration and login is already handled======//
//    @PostMapping
//    public void createAccount(@RequestBody Account account) {
//        accountJdbcDAO.createAccount(account);
//    }

}
