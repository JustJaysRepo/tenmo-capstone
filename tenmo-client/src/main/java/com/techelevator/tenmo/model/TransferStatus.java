package com.techelevator.tenmo.model;

public enum TransferStatus {
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected");
    public final String desc;

    TransferStatus(String desc) {
        this.desc = desc;
    }

    public static TransferStatus valueOfDesc(String desc) {
        for (TransferStatus e : values()) {
            if (e.desc.equals(desc)) {
                return e;
            }
        }
        return REJECTED;
    }
}
