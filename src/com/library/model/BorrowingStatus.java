package com.library.model;

/**
 * Represents the status of a borrowing transaction.
 */
public enum BorrowingStatus {
    ACTIVE("Active"),
    RETURNED("Returned");

    private final String displayName;

    BorrowingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

