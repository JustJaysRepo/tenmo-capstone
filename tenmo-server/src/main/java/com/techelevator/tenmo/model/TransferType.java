package com.techelevator.tenmo.model;

public enum TransferType {
    REQUEST("Request"),
    SEND("Send");

    private final String displayValue;

    TransferType(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return this.displayValue;
    }
}
