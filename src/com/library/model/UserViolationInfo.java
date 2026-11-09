package com.library.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data transfer object encapsulating a user who exceeds the 3-book borrowing limit.
 */
@SuppressWarnings("serial")
public class UserViolationInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LibraryUser user;
    private final int activeBorrowingsCount;
    private final List<Book> borrowedBooks;
    private final List<BorrowingRecord> activeRecords;

    public UserViolationInfo(LibraryUser user, int activeBorrowingsCount, List<Book> borrowedBooks, List<BorrowingRecord> activeRecords) {
        this.user = user;
        this.activeBorrowingsCount = activeBorrowingsCount;
        this.borrowedBooks = borrowedBooks != null ? new ArrayList<>(borrowedBooks) : new ArrayList<>();
        this.activeRecords = activeRecords != null ? new ArrayList<>(activeRecords) : new ArrayList<>();
    }

    public LibraryUser getUser() {
        return user;
    }

    public int getActiveBorrowingsCount() {
        return activeBorrowingsCount;
    }

    public List<Book> getBorrowedBooks() {
        return Collections.unmodifiableList(borrowedBooks);
    }

    public List<BorrowingRecord> getActiveRecords() {
        return Collections.unmodifiableList(activeRecords);
    }

    public String getWarningMessage() {
        return "Warning: User '" + (user != null ? user.getFullName() : "Unknown") + 
               "' (" + (user != null ? user.getRegistrationNumber() : "") + 
               ") has " + activeBorrowingsCount + " active borrowings, exceeding the allowed limit of 3.";
    }

    @Override
    public String toString() {
        return getWarningMessage();
    }
}
