package com.techelevator.tenmo.dao;

import java.util.List;

import com.techelevator.tenmo.model.Transfer;

/*
 * 1. Get all transfers -> findALl()
 * 2. Get transfer by id -> getTransferById()
 * 3. Get all transfers by user id -> getTransfersByUserId()
 * 4. Create a transfer -> create()
 * 5. Update a transfer -> update()
 */
public interface TransferDao {

    List<Transfer> findAll();

    Transfer getTransferById(int id);

    List<Transfer> getTransfersByUserId(int user_id);

    // Is it okay to used string here ??? most of the methods seem to use ids instead.
    List<Transfer> getTransfersByUserIdAndStatus(int user_id, String status);

    Transfer create(Transfer transfer);

    boolean update(int transfer_id, Transfer transfer);

}
