package com.library.model;

/**
 * Defines system access roles for role-based authorization.
 */
public enum UserRole {
    LIBRARIAN("Librarian (Staff)"),
    STUDENT("Student Member");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isLibrarian() {
        return this == LIBRARIAN;
    }

    public boolean isStudent() {
        return this == STUDENT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

