package com.library.storage;

import com.library.model.Book;
import com.library.model.BorrowingRecord;
import com.library.model.LibraryUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Container object encapsulating all persistent library state.
 */
@SuppressWarnings("serial")
public class LibraryData implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Book> books;
    private List<LibraryUser> users;
    private List<BorrowingRecord> borrowings;

    private int nextBookIdSeq;
    private int nextUserIdSeq;
    private int nextTransactionIdSeq;

    public LibraryData() {
        this.books = new ArrayList<>();
        this.users = new ArrayList<>();
        this.borrowings = new ArrayList<>();
        this.nextBookIdSeq = 1001;
        this.nextUserIdSeq = 1001;
        this.nextTransactionIdSeq = 10001;
    }

    public List<Book> getBooks() {
        if (books == null) books = new ArrayList<>();
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    public List<LibraryUser> getUsers() {
        if (users == null) users = new ArrayList<>();
        return users;
    }

    public void setUsers(List<LibraryUser> users) {
        this.users = users;
    }

    public List<BorrowingRecord> getBorrowings() {
        if (borrowings == null) borrowings = new ArrayList<>();
        return borrowings;
    }

    public void setBorrowings(List<BorrowingRecord> borrowings) {
        this.borrowings = borrowings;
    }

    public int getNextBookIdSeq() {
        return nextBookIdSeq;
    }

    public void setNextBookIdSeq(int nextBookIdSeq) {
        this.nextBookIdSeq = nextBookIdSeq;
    }

    public int getNextUserIdSeq() {
        return nextUserIdSeq;
    }

    public void setNextUserIdSeq(int nextUserIdSeq) {
        this.nextUserIdSeq = nextUserIdSeq;
    }

    public int getNextTransactionIdSeq() {
        return nextTransactionIdSeq;
    }

    public void setNextTransactionIdSeq(int nextTransactionIdSeq) {
        this.nextTransactionIdSeq = nextTransactionIdSeq;
    }
}
