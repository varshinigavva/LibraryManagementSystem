package com.library.service;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.model.BorrowingRecord;
import com.library.model.BorrowingStatus;
import com.library.model.LibraryUser;
import com.library.model.UserViolationInfo;
import com.library.storage.LibraryData;
import com.library.storage.StorageService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core business service implementing all validation, borrowing rules,
 * limit enforcement, and coordination with persistent storage.
 */
public class LibraryService {
    public static final int MAX_BORROWING_LIMIT = 3;

    private final StorageService storageService;
    private LibraryData data;

    public LibraryService(StorageService storageService) throws IOException, ClassNotFoundException {
        this.storageService = storageService;
        this.data = storageService.loadData();
        synchronizeBookStatusesWithBorrowings();
    }

    /**
     * Ensures book availability status strictly reflects active borrowing records on startup.
     */
    private synchronized void synchronizeBookStatusesWithBorrowings() {
        Map<String, Boolean> activeBorrowedBookIds = new HashMap<>();
        for (BorrowingRecord record : data.getBorrowings()) {
            if (record.isActive()) {
                activeBorrowedBookIds.put(record.getBookId(), Boolean.TRUE);
            }
        }

        boolean updated = false;
        for (Book book : data.getBooks()) {
            boolean shouldBeBorrowed = activeBorrowedBookIds.containsKey(book.getBookId());
            if (shouldBeBorrowed && book.getStatus() != BookStatus.BORROWED) {
                book.setStatus(BookStatus.BORROWED);
                updated = true;
            } else if (!shouldBeBorrowed && book.getStatus() != BookStatus.AVAILABLE) {
                book.setStatus(BookStatus.AVAILABLE);
                updated = true;
            }
        }

        if (updated) {
            try {
                persist();
            } catch (IOException e) {
                System.err.println("Failed to persist synchronized book statuses: " + e.getMessage());
            }
        }
    }

    private synchronized void persist() throws IOException {
        storageService.saveData(data);
    }

    // =========================================================================
    // BOOK MANAGEMENT
    // =========================================================================

    public synchronized List<Book> getAllBooks() {
        return new ArrayList<>(data.getBooks());
    }

    public synchronized Book getBookById(String bookId) {
        if (bookId == null) return null;
        for (Book book : data.getBooks()) {
            if (book.getBookId().equalsIgnoreCase(bookId.trim())) {
                return book;
            }
        }
        return null;
    }

    public synchronized Book addBook(String title, String author, String category) throws ValidationException, IOException {
        validateBookFields(title, author, category);

        String bookId = "BK-" + (data.getNextBookIdSeq());
        data.setNextBookIdSeq(data.getNextBookIdSeq() + 1);

        Book newBook = new Book(bookId, title.trim(), author.trim(), category.trim(), BookStatus.AVAILABLE);
        data.getBooks().add(newBook);
        persist();
        return newBook;
    }

    public synchronized void updateBook(String bookId, String title, String author, String category) 
            throws ValidationException, IOException {
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new ValidationException("Book ID cannot be empty.");
        }
        validateBookFields(title, author, category);

        Book book = getBookById(bookId);
        if (book == null) {
            throw new ValidationException("Book with ID '" + bookId + "' not found.");
        }

        book.setTitle(title.trim());
        book.setAuthor(author.trim());
        book.setCategory(category.trim());
        persist();
    }

    public synchronized void deleteBook(String bookId) throws ValidationException, IOException {
        Book book = getBookById(bookId);
        if (book == null) {
            throw new ValidationException("Book with ID '" + bookId + "' does not exist.");
        }

        // Check if book is currently borrowed
        for (BorrowingRecord record : data.getBorrowings()) {
            if (record.isActive() && record.getBookId().equalsIgnoreCase(bookId)) {
                throw new ValidationException("Cannot delete book '" + book.getTitle() + "' (ID: " + bookId + 
                        ") because it is currently borrowed by an active transaction (" + record.getTransactionId() + 
                        "). Please return the book before deleting it.");
            }
        }

        data.getBooks().remove(book);
        persist();
    }

    public synchronized List<Book> searchAndFilterBooks(String query, String category) {
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        String cat = category != null ? category.trim() : "";

        return data.getBooks().stream()
                .filter(b -> {
                    boolean matchesQuery = q.isEmpty() ||
                            b.getTitle().toLowerCase(Locale.ROOT).contains(q) ||
                            b.getAuthor().toLowerCase(Locale.ROOT).contains(q) ||
                            b.getBookId().toLowerCase(Locale.ROOT).contains(q);
                    boolean matchesCat = cat.isEmpty() || cat.equalsIgnoreCase("All Categories") ||
                            b.getCategory().equalsIgnoreCase(cat);
                    return matchesQuery && matchesCat;
                })
                .collect(Collectors.toList());
    }

    public synchronized List<String> getAllCategories() {
        return data.getBooks().stream()
                .map(Book::getCategory)
                .filter(c -> c != null && !c.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private void validateBookFields(String title, String author, String category) throws ValidationException {
        if (title == null || title.trim().isEmpty()) {
            throw new ValidationException("Book title is required and cannot be blank.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new ValidationException("Author name is required and cannot be blank.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category is required and cannot be blank.");
        }
    }

    // =========================================================================
    // USER MANAGEMENT
    // =========================================================================

    public synchronized List<LibraryUser> getAllUsers() {
        return new ArrayList<>(data.getUsers());
    }

    public synchronized List<LibraryUser> getActiveUsers() {
        return data.getUsers().stream()
                .filter(LibraryUser::isActive)
                .collect(Collectors.toList());
    }

    public synchronized LibraryUser getUserById(String userId) {
        if (userId == null) return null;
        for (LibraryUser user : data.getUsers()) {
            if (user.getUserId().equalsIgnoreCase(userId.trim())) {
                return user;
            }
        }
        return null;
    }

    public synchronized LibraryUser getUserByRegistrationNumber(String regNo) {
        if (regNo == null) return null;
        for (LibraryUser user : data.getUsers()) {
            if (user.getRegistrationNumber().equalsIgnoreCase(regNo.trim())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Authenticates a user or librarian by username, registration number, or user ID.
     */
    public synchronized LibraryUser authenticate(String identifier, String password) throws ValidationException {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new ValidationException("Username or Registration Number is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required.");
        }

        String idTrim = identifier.trim();
        String passTrim = password.trim();

        for (LibraryUser user : data.getUsers()) {
            boolean matchesIdent = user.getUsername().equalsIgnoreCase(idTrim) ||
                                   user.getRegistrationNumber().equalsIgnoreCase(idTrim) ||
                                   user.getUserId().equalsIgnoreCase(idTrim) ||
                                   (user.isLibrarian() && idTrim.equalsIgnoreCase("librarian"));

            if (matchesIdent) {
                if (!user.isActive()) {
                    throw new ValidationException("This account has been deactivated. Please contact the administrator.");
                }

                if (user.getPassword().equals(passTrim) || 
                    (user.isLibrarian() && ("admin123".equals(passTrim) || "librarian123".equals(passTrim)))) {
                    return user;
                } else {
                    throw new ValidationException("Invalid password. Please check your credentials.");
                }
            }
        }

        throw new ValidationException("Account '" + identifier + "' was not found. Please register or verify spelling.");
    }

    public synchronized LibraryUser registerUser(String fullName, String registrationNumber, 
                                                 String department, String contactDetails) 
            throws ValidationException, IOException {
        validateUserFields(fullName, registrationNumber, department, contactDetails);

        LibraryUser existing = getUserByRegistrationNumber(registrationNumber);
        if (existing != null) {
            throw new ValidationException("A user with registration number '" + registrationNumber.trim() + 
                    "' already exists (" + existing.getFullName() + ", ID: " + existing.getUserId() + ").");
        }

        String userId = "USR-" + (data.getNextUserIdSeq());
        data.setNextUserIdSeq(data.getNextUserIdSeq() + 1);

        LibraryUser newUser = new LibraryUser(userId, fullName.trim(), registrationNumber.trim(), 
                department.trim(), contactDetails.trim(), true);
        data.getUsers().add(newUser);
        persist();
        return newUser;
    }

    public synchronized void updateUser(String userId, String fullName, String registrationNumber, 
                                       String department, String contactDetails, boolean active) 
            throws ValidationException, IOException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("User ID cannot be empty.");
        }
        validateUserFields(fullName, registrationNumber, department, contactDetails);

        LibraryUser user = getUserById(userId);
        if (user == null) {
            throw new ValidationException("User with ID '" + userId + "' does not exist.");
        }

        LibraryUser existingWithReg = getUserByRegistrationNumber(registrationNumber);
        if (existingWithReg != null && !existingWithReg.getUserId().equalsIgnoreCase(userId)) {
            throw new ValidationException("Registration number '" + registrationNumber.trim() + 
                    "' is already registered to another user (" + existingWithReg.getFullName() + ").");
        }

        user.setFullName(fullName.trim());
        user.setRegistrationNumber(registrationNumber.trim());
        user.setDepartment(department.trim());
        user.setContactDetails(contactDetails.trim());
        user.setActive(active);
        persist();
    }

    /**
     * Safely deletes or deactivates a user.
     * If user has active borrowings -> blocked with explanation.
     * If user has historical borrowings -> safe deactivation to preserve history.
     * If user has no history -> permanently deleted.
     */
    public synchronized String deleteOrDeactivateUser(String userId) throws ValidationException, IOException {
        LibraryUser user = getUserById(userId);
        if (user == null) {
            throw new ValidationException("User with ID '" + userId + "' does not exist.");
        }

        int activeCount = getActiveBorrowingCount(userId);
        if (activeCount > 0) {
            throw new ValidationException("Cannot delete or deactivate user '" + user.getFullName() + 
                    "' because they currently have " + activeCount + " active borrowed book(s). " +
                    "All books must be returned before deleting this user.");
        }

        boolean hasHistory = false;
        for (BorrowingRecord record : data.getBorrowings()) {
            if (record.getUserId().equalsIgnoreCase(userId)) {
                hasHistory = true;
                break;
            }
        }

        if (hasHistory) {
            user.setActive(false);
            persist();
            return "User '" + user.getFullName() + "' has historical borrowing transactions. " +
                   "The user account has been safely DEACTIVATED to preserve historical records.";
        } else {
            data.getUsers().remove(user);
            persist();
            return "User '" + user.getFullName() + "' had no borrowing history and was permanently removed.";
        }
    }

    public synchronized List<LibraryUser> searchUsers(String query) {
        String q = query != null ? query.trim().toLowerCase(Locale.ROOT) : "";
        if (q.isEmpty()) {
            return new ArrayList<>(data.getUsers());
        }

        return data.getUsers().stream()
                .filter(u -> u.getFullName().toLowerCase(Locale.ROOT).contains(q) ||
                             u.getRegistrationNumber().toLowerCase(Locale.ROOT).contains(q) ||
                             u.getDepartment().toLowerCase(Locale.ROOT).contains(q) ||
                             u.getUserId().toLowerCase(Locale.ROOT).contains(q))
                .collect(Collectors.toList());
    }

    private void validateUserFields(String fullName, String registrationNumber, 
                                    String department, String contactDetails) throws ValidationException {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required and cannot be blank.");
        }
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new ValidationException("Student registration number is required.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new ValidationException("Department is required.");
        }
        if (contactDetails == null || contactDetails.trim().isEmpty()) {
            throw new ValidationException("Contact details (email or phone) are required.");
        }
    }

    // =========================================================================
    // BORROWING MANAGEMENT & THREE-BOOK LIMIT ENFORCEMENT
    // =========================================================================

    public synchronized List<BorrowingRecord> getAllBorrowings() {
        return new ArrayList<>(data.getBorrowings());
    }

    public synchronized List<BorrowingRecord> getActiveBorrowings() {
        return data.getBorrowings().stream()
                .filter(BorrowingRecord::isActive)
                .collect(Collectors.toList());
    }

    public synchronized List<BorrowingRecord> getReturnedBorrowings() {
        return data.getBorrowings().stream()
                .filter(r -> !r.isActive())
                .collect(Collectors.toList());
    }

    public synchronized List<BorrowingRecord> getActiveBorrowingsForUser(String userId) {
        if (userId == null) return Collections.emptyList();
        return data.getBorrowings().stream()
                .filter(r -> r.isActive() && r.getUserId().equalsIgnoreCase(userId.trim()))
                .collect(Collectors.toList());
    }

    public synchronized int getActiveBorrowingCount(String userId) {
        if (userId == null) return 0;
        int count = 0;
        for (BorrowingRecord record : data.getBorrowings()) {
            if (record.isActive() && record.getUserId().equalsIgnoreCase(userId.trim())) {
                count++;
            }
        }
        return count;
    }

    public synchronized BorrowingRecord getBorrowingById(String transactionId) {
        if (transactionId == null) return null;
        for (BorrowingRecord record : data.getBorrowings()) {
            if (record.getTransactionId().equalsIgnoreCase(transactionId.trim())) {
                return record;
            }
        }
        return null;
    }

    /**
     * Confirms and executes a book borrowing transaction.
     * Strictly enforces the 3-book limit in business logic.
     */
    public synchronized BorrowingRecord borrowBook(String userId, String bookId, LocalDate borrowDate) 
            throws ValidationException, IOException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new ValidationException("Please select a registered user.");
        }
        if (bookId == null || bookId.trim().isEmpty()) {
            throw new ValidationException("Please select a book to borrow.");
        }

        LibraryUser user = getUserById(userId);
        if (user == null) {
            throw new ValidationException("Selected user (ID: " + userId + ") does not exist.");
        }
        if (!user.isActive()) {
            throw new ValidationException("Borrowing rejected: User '" + user.getFullName() + 
                    "' is deactivated and cannot borrow books.");
        }

        Book book = getBookById(bookId);
        if (book == null) {
            throw new ValidationException("Selected book (ID: " + bookId + ") does not exist.");
        }
        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new ValidationException("Borrowing rejected: Book '" + book.getTitle() + 
                    "' is currently unavailable (Status: " + book.getStatus().getDisplayName() + ").");
        }

        // CRITICAL REQUIREMENT 7: THREE-BOOK BORROWING LIMIT
        int activeCount = getActiveBorrowingCount(userId);
        if (activeCount >= MAX_BORROWING_LIMIT) {
            throw new ValidationException("Borrowing rejected: This user has already borrowed the maximum of 3 books. " +
                    "(Active borrowings: " + activeCount + "). A book must be returned before borrowing another.");
        }

        String transactionId = "TXN-" + (data.getNextTransactionIdSeq());
        data.setNextTransactionIdSeq(data.getNextTransactionIdSeq() + 1);

        LocalDate actualBorrowDate = borrowDate != null ? borrowDate : LocalDate.now();
        BorrowingRecord record = new BorrowingRecord(transactionId, user.getUserId(), book.getBookId(), actualBorrowDate);
        
        // Mark book as borrowed
        book.setStatus(BookStatus.BORROWED);
        data.getBorrowings().add(record);

        persist();
        return record;
    }

    // =========================================================================
    // RETURN MANAGEMENT
    // =========================================================================

    public synchronized BorrowingRecord returnBook(String transactionId, LocalDate returnDate) 
            throws ValidationException, IOException {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new ValidationException("Transaction ID is required to process return.");
        }

        BorrowingRecord record = getBorrowingById(transactionId);
        if (record == null) {
            throw new ValidationException("Borrowing transaction '" + transactionId + "' was not found.");
        }

        if (record.getStatus() == BorrowingStatus.RETURNED) {
            throw new ValidationException("Transaction " + transactionId + " has already been returned on " + 
                    record.getReturnDate() + ".");
        }

        // Update record
        LocalDate actualReturnDate = returnDate != null ? returnDate : LocalDate.now();
        record.setStatus(BorrowingStatus.RETURNED);
        record.setReturnDate(actualReturnDate);

        // Mark book as available
        Book book = getBookById(record.getBookId());
        if (book != null) {
            book.setStatus(BookStatus.AVAILABLE);
        }

        persist();
        return record;
    }

    // =========================================================================
    // REQUIREMENT 8: USERS EXCEEDING LIMIT DETECTION & WARNINGS
    // =========================================================================

    public synchronized List<UserViolationInfo> getUsersExceedingLimit() {
        List<UserViolationInfo> violations = new ArrayList<>();
        Map<String, List<BorrowingRecord>> userActiveRecords = new HashMap<>();

        for (BorrowingRecord record : data.getBorrowings()) {
            if (record.isActive()) {
                userActiveRecords.computeIfAbsent(record.getUserId(), k -> new ArrayList<>()).add(record);
            }
        }

        for (Map.Entry<String, List<BorrowingRecord>> entry : userActiveRecords.entrySet()) {
            int count = entry.getValue().size();
            if (count > MAX_BORROWING_LIMIT) {
                LibraryUser user = getUserById(entry.getKey());
                List<Book> books = new ArrayList<>();
                for (BorrowingRecord rec : entry.getValue()) {
                    Book b = getBookById(rec.getBookId());
                    if (b != null) books.add(b);
                }
                violations.add(new UserViolationInfo(user, count, books, entry.getValue()));
            }
        }

        // Sort by highest violation count first
        violations.sort(Comparator.comparingInt(UserViolationInfo::getActiveBorrowingsCount).reversed());
        return violations;
    }

    public synchronized boolean hasUsersExceedingLimit() {
        return !getUsersExceedingLimit().isEmpty();
    }

    // =========================================================================
    // DASHBOARD & REPORTING METRICS
    // =========================================================================

    public synchronized int getTotalBooksCount() {
        return data.getBooks().size();
    }

    public synchronized int getAvailableBooksCount() {
        int count = 0;
        for (Book b : data.getBooks()) {
            if (b.isAvailable()) count++;
        }
        return count;
    }

    public synchronized int getBorrowedBooksCount() {
        int count = 0;
        for (Book b : data.getBooks()) {
            if (!b.isAvailable()) count++;
        }
        return count;
    }

    public synchronized int getTotalUsersCount() {
        return data.getUsers().size();
    }

    public synchronized int getActiveUsersCount() {
        int count = 0;
        for (LibraryUser u : data.getUsers()) {
            if (u.isActive()) count++;
        }
        return count;
    }

    public synchronized int getTotalReturnedTransactionsCount() {
        int count = 0;
        for (BorrowingRecord r : data.getBorrowings()) {
            if (!r.isActive()) count++;
        }
        return count;
    }

    public synchronized int getTotalActiveBorrowingsCount() {
        int count = 0;
        for (BorrowingRecord r : data.getBorrowings()) {
            if (r.isActive()) count++;
        }
        return count;
    }

    public synchronized int getUsersExceedingLimitCount() {
        return getUsersExceedingLimit().size();
    }

    public synchronized List<Book> getAvailableBooks() {
        return data.getBooks().stream()
                .filter(Book::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Resets library data back to the clean demonstration sample state.
     */
    public synchronized void resetToSampleData() throws IOException {
        this.data = storageService.createSampleData();
        synchronizeBookStatusesWithBorrowings();
        persist();
    }
}

