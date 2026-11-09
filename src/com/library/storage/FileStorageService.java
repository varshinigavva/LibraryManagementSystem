package com.library.storage;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.model.BorrowingRecord;
import com.library.model.BorrowingStatus;
import com.library.model.LibraryUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

/**
 * Concrete implementation of StorageService using Java Object Serialization and atomic file operations.
 */
public class FileStorageService implements StorageService {
    private final File dataFile;
    private final File dataDir;

    public FileStorageService() {
        this("data/library_data.ser");
    }

    public FileStorageService(String filePath) {
        this.dataFile = new File(filePath);
        this.dataDir = dataFile.getParentFile() != null ? dataFile.getParentFile() : new File("data");
    }

    @Override
    public boolean hasExistingData() {
        return dataFile.exists() && dataFile.length() > 0;
    }

    @Override
    public synchronized LibraryData loadData() throws IOException, ClassNotFoundException {
        if (!hasExistingData()) {
            // Fresh run: create initial demo data
            LibraryData initialData = createSampleData();
            saveData(initialData);
            return initialData;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Object obj = ois.readObject();
            if (obj instanceof LibraryData) {
                return (LibraryData) obj;
            } else {
                throw new IOException("Corrupted data format: expected LibraryData instance");
            }
        } catch (IOException | ClassNotFoundException ex) {
            // Backup corrupted file
            File backup = new File(dataDir, "library_data_corrupt_" + System.currentTimeMillis() + ".bak");
            try {
                Files.copy(dataFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.err.println("Warning: Corrupted data file backed up to " + backup.getAbsolutePath());
            } catch (Exception ignored) {
            }
            throw ex;
        }
    }

    @Override
    public synchronized void saveData(LibraryData data) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException("Cannot save null LibraryData");
        }

        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                throw new IOException("Failed to create storage directory: " + dataDir.getAbsolutePath());
            }
        }

        // Write to a temporary file first, then atomically move to avoid corruption on crash
        File tempFile = new File(dataDir, dataFile.getName() + ".tmp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
            oos.writeObject(data);
            oos.flush();
        }

        Files.move(tempFile.toPath(), dataFile.toPath(), 
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /**
     * Creates rich sample demonstration data, strictly fulfilling requirement 8
     * (including an existing user with > 3 active borrowings to verify the warning logic).
     */
    public LibraryData createSampleData() {
        LibraryData data = new LibraryData();

        // 1. Books
        data.getBooks().add(new Book("BK-1001", "Introduction to Algorithms", "Thomas H. Cormen et al.", "Computer Science", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1002", "Clean Code: A Handbook of Agile Craftsmanship", "Robert C. Martin", "Computer Science", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1003", "Artificial Intelligence: A Modern Approach", "Stuart Russell & Peter Norvig", "Computer Science", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1004", "Design Patterns: Elements of Reusable Object-Oriented Software", "Erich Gamma et al.", "Computer Science", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1005", "Operating System Concepts", "Abraham Silberschatz et al.", "Computer Science", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1006", "Database System Concepts", "Abraham Silberschatz et al.", "Computer Science", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1007", "Computer Networking: A Top-Down Approach", "James Kurose & Keith Ross", "Computer Science", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1008", "Microelectronic Circuits", "Adel S. Sedra & Kenneth C. Smith", "Electronics", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1009", "Signals and Systems", "Alan V. Oppenheim & Alan S. Willsky", "Electronics", BookStatus.BORROWED));
        data.getBooks().add(new Book("BK-1010", "Discrete Mathematics and Its Applications", "Kenneth H. Rosen", "Mathematics", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1011", "Linear Algebra and Its Applications", "Gilbert Strang", "Mathematics", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1012", "University Physics with Modern Physics", "Hugh D. Young & Roger A. Freedman", "Physics", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1013", "The Pragmatic Programmer", "David Thomas & Andrew Hunt", "Computer Science", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1014", "Principles of Management", "Harold Koontz & Heinz Weihrich", "Management", BookStatus.AVAILABLE));
        data.getBooks().add(new Book("BK-1015", "Modern Control Systems", "Richard C. Dorf & Robert H. Bishop", "Electronics", BookStatus.AVAILABLE));
        data.setNextBookIdSeq(1016);

        // 2. Users (Librarian Staff & Students)
        LibraryUser admin = new LibraryUser("LIB-1001", "Chief Librarian (Admin)", "STAFF-001", 
                "Library Administration", "librarian@campus.edu | +1-555-0100", 
                true, com.library.model.UserRole.LIBRARIAN, "admin", "admin123");

        LibraryUser alex = new LibraryUser("USR-1001", "Alex Chen", "REG-2023-CS-001", 
                "Computer Science", "alex.chen@campus.edu | +1-555-0101",
                true, com.library.model.UserRole.STUDENT, "alex", "student123");

        LibraryUser priya = new LibraryUser("USR-1002", "Priya Sharma", "REG-2023-CS-045", 
                "Computer Science", "priya.s@campus.edu | +1-555-0145",
                true, com.library.model.UserRole.STUDENT, "priya", "student123");

        LibraryUser marcus = new LibraryUser("USR-1003", "Marcus Johnson", "REG-2023-EE-012", 
                "Electrical Engineering", "marcus.j@campus.edu | +1-555-0212",
                true, com.library.model.UserRole.STUDENT, "marcus", "student123");

        LibraryUser sophia = new LibraryUser("USR-1004", "Sophia Rodriguez", "REG-2024-ME-088", 
                "Mechanical Engineering", "sophia.r@campus.edu | +1-555-0388",
                true, com.library.model.UserRole.STUDENT, "sophia", "student123");

        LibraryUser david = new LibraryUser("USR-1005", "David Kim", "REG-2024-MATH-007", 
                "Mathematics", "david.kim@campus.edu | +1-555-0407",
                true, com.library.model.UserRole.STUDENT, "david", "student123");

        LibraryUser emily = new LibraryUser("USR-1006", "Emily Watson", "REG-2024-LIT-023", 
                "Humanities & Literature", "emily.w@campus.edu | +1-555-0523",
                true, com.library.model.UserRole.STUDENT, "emily", "student123");

        data.getUsers().add(admin);
        data.getUsers().add(alex);
        data.getUsers().add(priya);
        data.getUsers().add(marcus);
        data.getUsers().add(sophia);
        data.getUsers().add(david);
        data.getUsers().add(emily);
        data.setNextUserIdSeq(1007);

        // 3. Borrowing transactions
        // Demonstrating Requirement 8: Alex Chen has 4 ACTIVE borrowings (exceeding limit of 3)
        data.getBorrowings().add(new BorrowingRecord("TXN-10001", "USR-1001", "BK-1001", LocalDate.now().minusDays(20), null, BorrowingStatus.ACTIVE));
        data.getBorrowings().add(new BorrowingRecord("TXN-10002", "USR-1001", "BK-1002", LocalDate.now().minusDays(15), null, BorrowingStatus.ACTIVE));
        data.getBorrowings().add(new BorrowingRecord("TXN-10003", "USR-1001", "BK-1003", LocalDate.now().minusDays(10), null, BorrowingStatus.ACTIVE));
        data.getBorrowings().add(new BorrowingRecord("TXN-10004", "USR-1001", "BK-1004", LocalDate.now().minusDays(5), null, BorrowingStatus.ACTIVE));

        // Priya Sharma: 1 active, 2 returned
        data.getBorrowings().add(new BorrowingRecord("TXN-10005", "USR-1002", "BK-1005", LocalDate.now().minusDays(8), null, BorrowingStatus.ACTIVE));
        data.getBorrowings().add(new BorrowingRecord("TXN-10006", "USR-1002", "BK-1006", LocalDate.now().minusDays(40), LocalDate.now().minusDays(20), BorrowingStatus.RETURNED));
        data.getBorrowings().add(new BorrowingRecord("TXN-10007", "USR-1002", "BK-1007", LocalDate.now().minusDays(60), LocalDate.now().minusDays(45), BorrowingStatus.RETURNED));

        // Marcus Johnson: 2 active borrowings (can borrow 1 more)
        data.getBorrowings().add(new BorrowingRecord("TXN-10008", "USR-1003", "BK-1008", LocalDate.now().minusDays(12), null, BorrowingStatus.ACTIVE));
        data.getBorrowings().add(new BorrowingRecord("TXN-10009", "USR-1003", "BK-1009", LocalDate.now().minusDays(7), null, BorrowingStatus.ACTIVE));

        // Sophia Rodriguez: 1 returned
        data.getBorrowings().add(new BorrowingRecord("TXN-10010", "USR-1004", "BK-1010", LocalDate.now().minusDays(50), LocalDate.now().minusDays(30), BorrowingStatus.RETURNED));

        data.setNextTransactionIdSeq(10011);

        return data;
    }
}

