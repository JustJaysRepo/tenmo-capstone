package com.techelevator.tenmo.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import java.math.BigDecimal;

public class Transfer {
    @NotNull
    private int transfer_id;

    @NotNull
    private TransferType transfer_type;


    @JsonProperty(access = Access.READ_ONLY)
    private TransferStatus transfer_status;

    @NotNull
    private int account_from;

    @NotNull
    private int account_to;

    @NotNull
    @Positive
    private BigDecimal amount;

    public Transfer() {
    }
    public Transfer(int transfer_id, TransferType transfer_type, TransferStatus transfer_status, int account_from, int account_to, BigDecimal amount) {
        this.transfer_id = transfer_id;
        this.transfer_type = transfer_type;
        this.transfer_status = transfer_status;
        this.account_from = account_from;
        this.account_to = account_to;
        this.amount = amount;
    }

    public int getTransfer_id() {
        return transfer_id;
    }

    public void setTransfer_id(int transfer_id) {
        this.transfer_id = transfer_id;
    }

    public TransferType getTransfer_type() {
        return transfer_type;
    }

    public void setTransfer_type(TransferType transfer_type) {
        this.transfer_type = transfer_type;
    }

    public TransferStatus getTransfer_status() {
        return transfer_status;
    }

    public void setTransfer_status(TransferStatus transfer_status) {
        this.transfer_status = transfer_status;
    }

    public int getAccount_from() {
        return account_from;
    }

    public void setAccount_from(int account_from) {
        this.account_from = account_from;
    }

    public int getAccount_to() {
        return account_to;
    }

    public void setAccount_to(int account_to) {
        this.account_to = account_to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal bigDecimal) {
        this.amount = bigDecimal;
    }

    @Override
    public String toString() {
        return "Transfer {" +
                "ID = " + transfer_id +
                ", Type = " + transfer_type +
                ", Status = " + transfer_status +
                ", From Account = " + account_from +
                ", To Account = " + account_to +
                ", Amount = " + amount +
                '}';
    }

}
