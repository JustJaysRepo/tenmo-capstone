package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;
//        * 1. Get all Accounts -> findALl()
//        * 2. Get Account id by user id -> getAccountIdByUserId()
//        * 3. Get Account balance by user id -> getAccountBalanceByUserId()
//        * 4. Get Account by user id -> getAccountByUserId()
//        * 5. Update a Account balance by account ,user id  -> updateAccountByUserId()
//        */

public interface AccountDao {
    public List<Account> findAll(int user_id);

    public int getAccountIdByUserId(int user_id);

    public BigDecimal getAccountBalanceByUserId(int user_id);

    public Account getAccountByUserId(int user_id);

    public void updateAccountByUserId(Account account, int user_id);

    public Account getAccountById(int id);
}
