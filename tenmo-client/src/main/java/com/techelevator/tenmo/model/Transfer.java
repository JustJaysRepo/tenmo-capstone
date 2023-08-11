package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private int transfer_id;
    private TransferType transfer_type; // Changed from String to TransferType
    private TransferStatus transfer_status; // Changed from String to TransferStatus
    private int account_from;
    private int account_to;
    private BigDecimal amount;

    public int getTransfer_id() {
        return transfer_id;
    }
    public void setTransfer_id(int transfer_id) {
        this.transfer_id = transfer_id;
    }
    public TransferType getTransfer_type() { // Changed return type to TransferType
        return transfer_type;
    }
    public void setTransfer_type(TransferType transfer_type) { // Changed parameter type to TransferType
        this.transfer_type = transfer_type;
    }
    public TransferStatus getTransfer_status() { // Changed return type to TransferStatus
        return transfer_status;
    }
    public void setTransfer_status(TransferStatus transfer_status) { // Changed parameter type to TransferStatus
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
        return "Transfer{" +
                "transfer_id=" + transfer_id +
                ", transfer_type=" + transfer_type + // Adjusted to show enum type instead of string
                ", transfer_status=" + transfer_status + // Adjusted to show enum type instead of string
                ", account_from=" + account_from +
                ", account_to=" + account_to +
                ", amount=" + amount +
                '}';
    }
}
