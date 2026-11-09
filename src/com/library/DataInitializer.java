package com.library;

import com.library.model.UserViolationInfo;
import com.library.service.LibraryService;
import com.library.storage.FileStorageService;

import java.io.File;

/**
 * Utility to verify and initialize sample data on disk.
 */
public class DataInitializer {
    public static void main(String[] args) {
        try {
            FileStorageService storage = new FileStorageService("data/library_data.ser");
            LibraryService service = new LibraryService(storage);

            System.out.println("Persistent Data File: " + new File("data/library_data.ser").getAbsolutePath());
            System.out.println("Total Books: " + service.getTotalBooksCount());
            System.out.println("Available Books: " + service.getAvailableBooksCount());
            System.out.println("Borrowed Books: " + service.getBorrowedBooksCount());
            System.out.println("Total Users: " + service.getTotalUsersCount());
            System.out.println("Total Borrowing Transactions: " + service.getAllBorrowings().size());
            System.out.println("Users Exceeding Limit (>3): " + service.getUsersExceedingLimitCount());

            for (UserViolationInfo v : service.getUsersExceedingLimit()) {
                System.out.println(" -> " + v.getWarningMessage());
            }

            System.out.println("Initialization check passed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

