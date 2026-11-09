package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a borrowing transaction in the library.
 */
public class BorrowingRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String userId;
    private String bookId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private BorrowingStatus status;

    public BorrowingRecord() {
        this.status = BorrowingStatus.ACTIVE;
        this.borrowDate = LocalDate.now();
    }

    public BorrowingRecord(String transactionId, String userId, String bookId, LocalDate borrowDate) {
        this(transactionId, userId, bookId, borrowDate, null, BorrowingStatus.ACTIVE);
    }

    public BorrowingRecord(String transactionId, String userId, String bookId, LocalDate borrowDate, LocalDate returnDate, BorrowingStatus status) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate != null ? borrowDate : LocalDate.now();
        this.returnDate = returnDate;
        this.status = status != null ? status : BorrowingStatus.ACTIVE;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public BorrowingStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowingStatus status) {
        this.status = status != null ? status : BorrowingStatus.ACTIVE;
    }

    public boolean isActive() {
        return status == BorrowingStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowingRecord that = (BorrowingRecord) o;
        return Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    @Override
    public String toString() {
        return "Transaction " + transactionId + " [User: " + userId + ", Book: " + bookId + ", Status: " + status + "]";
    }
}

