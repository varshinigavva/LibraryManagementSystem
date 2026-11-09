package com.library.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a book item in the college library catalog.
 */
public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookId;
    private String title;
    private String author;
    private String category;
    private BookStatus status;

    public Book() {
        this.status = BookStatus.AVAILABLE;
    }

    public Book(String bookId, String title, String author, String category) {
        this(bookId, title, author, category, BookStatus.AVAILABLE);
    }

    public Book(String bookId, String title, String author, String category, BookStatus status) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.status = status != null ? status : BookStatus.AVAILABLE;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status != null ? status : BookStatus.AVAILABLE;
    }

    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return bookId + " - " + title + " (" + author + ") [" + status.getDisplayName() + "]";
    }
}

