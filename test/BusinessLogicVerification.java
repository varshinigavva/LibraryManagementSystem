package test;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.model.BorrowingRecord;
import com.library.model.BorrowingStatus;
import com.library.model.LibraryUser;
import com.library.model.UserViolationInfo;
import com.library.service.LibraryService;
import com.library.service.ValidationException;
import com.library.storage.FileStorageService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Automated test suite verifying the 12 core functional testing requirements.
 */
public class BusinessLogicVerification {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("LIBRARY MANAGEMENT SYSTEM - VERIFICATION SUITE");
        System.out.println("==================================================");

        String testDataPath = "data/test_library_data.ser";
        File testFile = new File(testDataPath);
        if (testFile.exists()) {
            testFile.delete();
        }

        try {
            FileStorageService storageService = new FileStorageService(testDataPath);
            LibraryService service = new LibraryService(storageService);

            // 1. Add, edit, search, sort, and delete a book
            testScenario1(service);

            // 2. Register, edit, search, and deactivate a user
            testScenario2(service);

            // 3. Successfully borrow an available book
            testScenario3(service);

            // 4. Allow exactly three active borrowings for a user
            testScenario4(service);

            // 5. Reject the fourth active borrowing
            testScenario5(service);

            // 6. Return a book and verify that the user can borrow another
            testScenario6(service);

            // 7. Load an existing user with four active borrowings and display a warning without deleting any records
            testScenario7(service);

            // 8. Reject borrowing for that user until their active count falls below three
            testScenario8(service);

            // 9. Verify that returned transactions remain in the history
            testScenario9(service);

            // 10. Restart the application and verify that all data persists
            testScenario10(testDataPath);

            // 11. Verify that unavailable books cannot be borrowed twice
            testScenario11(service);

            // 12. Verify that all dashboard statistics update correctly
            testScenario12(service);

            // 13. Verify Role-Based Authentication & Access Control
            testScenario13(service);

        } catch (Exception e) {
            e.printStackTrace();
            testsFailed++;
        } finally {
            // Clean up test file
            if (testFile.exists()) {
                testFile.delete();
            }
        }

        System.out.println("==================================================");
        System.out.println("TEST RESULTS: " + testsPassed + " PASSED, " + testsFailed + " FAILED.");
        System.out.println("==================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String errorDetail) {
        if (condition) {
            System.out.println("[PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("[FAIL] " + testName + " - " + errorDetail);
            testsFailed++;
        }
    }

    // 1. Add, edit, search, sort, and delete a book
    private static void testScenario1(LibraryService service) throws Exception {
        Book b = service.addBook("Test-Driven Development", "Kent Beck", "Software Engineering");
        assertTrue("Add book generates valid ID and available status", 
                b != null && b.getBookId().startsWith("BK-") && b.isAvailable(), "Invalid book created");

        service.updateBook(b.getBookId(), "TDD By Example", "Kent Beck", "Software Engineering");
        Book updated = service.getBookById(b.getBookId());
        assertTrue("Edit book updates title", updated.getTitle().equals("TDD By Example"), "Title not updated");

        List<Book> search = service.searchAndFilterBooks("Kent", "Software Engineering");
        assertTrue("Search books by author/query", search.size() >= 1, "Book not found by search");

        service.deleteBook(b.getBookId());
        assertTrue("Delete unborrowed book succeeds", service.getBookById(b.getBookId()) == null, "Book not deleted");
    }

    // 2. Register, edit, search, and deactivate a user
    private static void testScenario2(LibraryService service) throws Exception {
        LibraryUser u = service.registerUser("Alice Smith", "REG-TEST-001", "Computer Science", "alice@test.com");
        assertTrue("Register user succeeds", u != null && u.getUserId().startsWith("USR-"), "User registration failed");

        service.updateUser(u.getUserId(), "Alice Smith Updated", "REG-TEST-001", "Computer Science", "alice.new@test.com", true);
        LibraryUser updated = service.getUserById(u.getUserId());
        assertTrue("Update user succeeds", updated.getFullName().equals("Alice Smith Updated"), "User update failed");

        List<LibraryUser> search = service.searchUsers("REG-TEST-001");
        assertTrue("Search user by registration number", search.size() == 1, "User search failed");

        // Safe delete of user with no history -> deleted
        String msg = service.deleteOrDeactivateUser(u.getUserId());
        assertTrue("Delete user with no history permanently removes user", 
                service.getUserById(u.getUserId()) == null, "User was not removed: " + msg);
    }

    // 3. Successfully borrow an available book
    private static void testScenario3(LibraryService service) throws Exception {
        LibraryUser user = service.registerUser("Bob Builder", "REG-TEST-002", "Civil Engineering", "bob@test.com");
        Book book = service.addBook("Structural Mechanics", "R. C. Hibbeler", "Civil Engineering");

        BorrowingRecord record = service.borrowBook(user.getUserId(), book.getBookId(), LocalDate.now());
        assertTrue("Borrow available book succeeds", 
                record != null && record.isActive() && !book.isAvailable() && book.getStatus() == BookStatus.BORROWED,
                "Borrowing failed or book status not BORROWED");
    }

    // 4 & 5. Allow exactly three active borrowings for a user and reject the fourth
    private static void testScenario4(LibraryService service) throws Exception {
        LibraryUser user = service.registerUser("Charlie Brown", "REG-TEST-003", "Arts", "charlie@test.com");
        Book b1 = service.addBook("Book One", "Author 1", "Arts");
        Book b2 = service.addBook("Book Two", "Author 2", "Arts");
        Book b3 = service.addBook("Book Three", "Author 3", "Arts");

        service.borrowBook(user.getUserId(), b1.getBookId(), LocalDate.now());
        service.borrowBook(user.getUserId(), b2.getBookId(), LocalDate.now());
        service.borrowBook(user.getUserId(), b3.getBookId(), LocalDate.now());

        int count = service.getActiveBorrowingCount(user.getUserId());
        assertTrue("Allow exactly three active borrowings", count == 3, "Expected 3 active borrowings, got " + count);
    }

    private static void testScenario5(LibraryService service) throws Exception {
        LibraryUser user = service.getUserByRegistrationNumber("REG-TEST-003");
        Book b4 = service.addBook("Book Four", "Author 4", "Arts");

        boolean caught = false;
        try {
            service.borrowBook(user.getUserId(), b4.getBookId(), LocalDate.now());
        } catch (ValidationException e) {
            caught = true;
            assertTrue("Reject fourth borrowing with specific message", 
                    e.getMessage().contains("maximum of 3 books"), "Unexpected message: " + e.getMessage());
        }
        assertTrue("Fourth borrowing strictly rejected", caught, "ValidationException was not thrown for 4th borrowing");
        assertTrue("Book four remains available after rejection", b4.isAvailable(), "Book status changed despite rejection");
    }

    // 6. Return a book and verify that the user can borrow another
    private static void testScenario6(LibraryService service) throws Exception {
        LibraryUser user = service.getUserByRegistrationNumber("REG-TEST-003");
        List<BorrowingRecord> active = service.getActiveBorrowingsForUser(user.getUserId());
        BorrowingRecord toReturn = active.get(0);

        service.returnBook(toReturn.getTransactionId(), LocalDate.now());
        int countAfterReturn = service.getActiveBorrowingCount(user.getUserId());
        assertTrue("Active count decrements after return", countAfterReturn == 2, "Expected 2 active, got " + countAfterReturn);

        // Now user can borrow b4
        List<Book> search = service.searchAndFilterBooks("Book Four", "Arts");
        Book b4 = search.get(0);
        BorrowingRecord newBorrow = service.borrowBook(user.getUserId(), b4.getBookId(), LocalDate.now());
        assertTrue("User can borrow after returning a book", newBorrow != null, "Borrowing failed after return");
    }

    // 7. Load an existing user with four active borrowings and display a warning without deleting any records
    private static void testScenario7(LibraryService service) {
        // Alex Chen in sample data has 4 active borrowings
        List<UserViolationInfo> violations = service.getUsersExceedingLimit();
        boolean foundAlex = false;
        for (UserViolationInfo v : violations) {
            if (v.getUser().getFullName().contains("Alex Chen")) {
                foundAlex = true;
                assertTrue("User Alex Chen has 4 active borrowings", v.getActiveBorrowingsCount() == 4, 
                        "Expected 4 active, got " + v.getActiveBorrowingsCount());
                assertTrue("Borrowed books list populated in violation info", v.getBorrowedBooks().size() == 4, 
                        "Books list mismatch");
                assertTrue("Warning message formatted properly", 
                        v.getWarningMessage().contains("exceeding the allowed limit of 3"), "Warning text incorrect");
            }
        }
        assertTrue("Exceeding limit detection found affected user", foundAlex, "Alex Chen not detected in violations");
    }

    // 8. Reject borrowing for that user until their active count falls below three
    private static void testScenario8(LibraryService service) throws Exception {
        LibraryUser alex = service.getUserByRegistrationNumber("REG-2023-CS-001");
        Book newBook = service.addBook("Sample Test Book", "Author", "Computer Science");

        // Alex has 4 active borrowings -> borrowing rejected
        boolean rejectedAt4 = false;
        try {
            service.borrowBook(alex.getUserId(), newBook.getBookId(), LocalDate.now());
        } catch (ValidationException e) {
            rejectedAt4 = true;
        }
        assertTrue("Reject borrowing when user has 4 active borrowings", rejectedAt4, "Allowed borrowing at 4");

        // Return 1 book -> active count becomes 3 -> borrowing still rejected
        List<BorrowingRecord> alexActive = service.getActiveBorrowingsForUser(alex.getUserId());
        service.returnBook(alexActive.get(0).getTransactionId(), LocalDate.now());
        assertTrue("Alex count is now 3", service.getActiveBorrowingCount(alex.getUserId()) == 3, "Count not 3");

        boolean rejectedAt3 = false;
        try {
            service.borrowBook(alex.getUserId(), newBook.getBookId(), LocalDate.now());
        } catch (ValidationException e) {
            rejectedAt3 = true;
        }
        assertTrue("Reject borrowing when user has 3 active borrowings", rejectedAt3, "Allowed borrowing at 3");

        // Return another book -> active count becomes 2 -> borrowing allowed
        alexActive = service.getActiveBorrowingsForUser(alex.getUserId());
        service.returnBook(alexActive.get(0).getTransactionId(), LocalDate.now());
        assertTrue("Alex count is now 2", service.getActiveBorrowingCount(alex.getUserId()) == 2, "Count not 2");

        BorrowingRecord allowedRec = service.borrowBook(alex.getUserId(), newBook.getBookId(), LocalDate.now());
        assertTrue("Borrowing allowed once active count falls below 3", allowedRec != null, "Failed to borrow at count 2");
    }

    // 9. Verify that returned transactions remain in the history
    private static void testScenario9(LibraryService service) {
        List<BorrowingRecord> returnedList = service.getReturnedBorrowings();
        assertTrue("Returned transactions are preserved in history", returnedList.size() >= 4, 
                "Expected at least 4 returned transactions in history, found " + returnedList.size());
    }

    // 10. Restart the application and verify that all data persists
    private static void testScenario10(String testDataPath) throws Exception {
        FileStorageService newStorage = new FileStorageService(testDataPath);
        LibraryService restartedService = new LibraryService(newStorage);

        assertTrue("Restarted service loaded books", restartedService.getTotalBooksCount() > 0, "No books loaded");
        assertTrue("Restarted service loaded users", restartedService.getTotalUsersCount() > 0, "No users loaded");
        assertTrue("Restarted service loaded borrowings", restartedService.getAllBorrowings().size() > 0, "No borrowings loaded");
    }

    // 11. Verify that unavailable books cannot be borrowed twice
    private static void testScenario11(LibraryService service) throws Exception {
        LibraryUser u1 = service.registerUser("User 11A", "REG-11A", "Dept", "11a@test.com");
        LibraryUser u2 = service.registerUser("User 11B", "REG-11B", "Dept", "11b@test.com");
        Book book = service.addBook("Unique Single Copy", "Author", "Science");

        service.borrowBook(u1.getUserId(), book.getBookId(), LocalDate.now());
        boolean caught = false;
        try {
            service.borrowBook(u2.getUserId(), book.getBookId(), LocalDate.now());
        } catch (ValidationException e) {
            caught = true;
            assertTrue("Rejects borrowing unavailable book", e.getMessage().contains("unavailable"), e.getMessage());
        }
        assertTrue("Unavailable book cannot be borrowed twice", caught, "Double borrowing succeeded unexpectedly");
    }

    // 12. Verify that all dashboard statistics update correctly
    private static void testScenario12(LibraryService service) {
        int totalBooks = service.getTotalBooksCount();
        int avail = service.getAvailableBooksCount();
        int borrowed = service.getBorrowedBooksCount();
        assertTrue("Total books equals available + borrowed", totalBooks == (avail + borrowed), 
                "total: " + totalBooks + ", avail: " + avail + ", borrowed: " + borrowed);

        int totalUsers = service.getTotalUsersCount();
        assertTrue("Total users count > 0", totalUsers > 0, "Users count zero");

        int returnedCount = service.getTotalReturnedTransactionsCount();
        assertTrue("Returned count matches records", returnedCount == service.getReturnedBorrowings().size(), "Mismatch");
    }

    // 13. Verify Role-Based Authentication & Access Control
    private static void testScenario13(LibraryService service) throws Exception {
        // Librarian login
        LibraryUser admin = service.authenticate("admin", "admin123");
        assertTrue("Librarian authentication succeeds", admin != null && admin.isLibrarian(), "Librarian login failed");

        // Student login by registration number
        LibraryUser student = service.authenticate("REG-2023-CS-001", "student123");
        assertTrue("Student authentication by reg number succeeds", 
                student != null && student.isStudent() && student.getFullName().contains("Alex Chen"), 
                "Student login failed");

        // Student login by username
        LibraryUser studentByUsername = service.authenticate("alex", "student123");
        assertTrue("Student authentication by username succeeds", 
                studentByUsername != null && studentByUsername.getUserId().equals("USR-1001"), 
                "Student username login failed");

        // Bad password
        boolean badPassCaught = false;
        try {
            service.authenticate("admin", "wrong_password");
        } catch (ValidationException e) {
            badPassCaught = true;
        }
        assertTrue("Invalid password rejected", badPassCaught, "Bad password was accepted");

        // Non-existent user
        boolean missingUserCaught = false;
        try {
            service.authenticate("nonexistent_user", "password");
        } catch (ValidationException e) {
            missingUserCaught = true;
        }
        assertTrue("Non-existent user rejected", missingUserCaught, "Non-existent user was accepted");
    }
}

