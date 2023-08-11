package com.techelevator.tenmo.model;

public enum TransferType {
    REQUEST("Request"),
    SEND("Send");

    public final String desc;
    TransferType(String desc) {
        this.desc = desc;
    }

}
