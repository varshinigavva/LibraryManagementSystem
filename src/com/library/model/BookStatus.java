package com.library.model;

/**
 * Represents the availability status of a book in the library.
 */
public enum BookStatus {
    AVAILABLE("Available"),
    BORROWED("Borrowed");

    private final String displayName;

    BookStatus(String displayName) {
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

