# College Library Management System

A desktop application designed for college librarians to manage book catalogs, student memberships, borrowing transactions, and return workflows. Built exclusively with standard **Java SE (Swing & AWT)** with persistent storage, strict three-book borrowing limits, and compliance monitoring.

---

## 1. Key Features

### 🏛️ Main Dashboard
- **Real-Time KPIs**: Total Books, Available Books, Currently Borrowed, Total Users, Completed Returns, and Users Exceeding Limit.
- **Policy Alert Banner**: Detects and highlights any existing users who exceed the maximum 3-book borrowing limit with details of their held books.
- **Active Borrowings Log**: Live table showing currently checked-out materials.
- **Quick Navigation**: Instant switching between Dashboard, Books, Users, Borrowings, Returns, and Reports.

### 📚 Book Catalog Management
- **Full CRUD**: Add, view, edit, and delete books.
- **Automatic ID Generation**: Unique IDs (`BK-1001`, `BK-1002`, etc.) generated sequentially.
- **Safety Lock on Deletion**: Prevents deletion of books currently borrowed in an active transaction, providing a clear explanation.
- **Live Search & Category Filter**: Filter by category (Computer Science, Electronics, Mathematics, etc.) and search by title, author, or book ID.
- **Multi-Attribute Sorting**: Sort by Book ID, Title, Author, or Category.

### 👥 Member / User Management
- **Student & Faculty Records**: Full Name, Student Registration Number, Department, and Contact Info.
- **Uniqueness & Validation**: Rejects duplicate student registration numbers and enforces non-empty fields.
- **Historical Record Preservation**: Users with transaction history are safely deactivated rather than erased, preserving borrowing logs.
- **Borrowed Books Inspector**: One-click modal to inspect all books currently held by any selected user.

### 🔄 Borrowing Transactions & Three-Book Limit Enforcement
- **Selection Dropdowns**: Dropdown of active registered members and currently available books.
- **Three-Book Limit Enforcement**:
  - Automatically checks borrower's active loan count.
  - If active count is 0, 1, or 2: Borrowing is approved.
  - If active count is 3 or more: Borrowing is strictly rejected with a clear message:  
    `"Borrowing rejected: This user has already borrowed the maximum of 3 books."`
  - Rejection is enforced at both the business logic service layer and UI level.
- **Immediate State Synchronization**: Marks transaction `ACTIVE`, marks the book `BORROWED`, and updates dashboard counters in real time.

### ⚠️ Legacy / Existing Records Exceeding Limit Support
- Safely loads and preserves pre-existing records where a user has more than 3 active borrowings without deleting or corrupting them.
- Displays a visible warning on the dashboard:  
  `"Warning: User '[Name]' ([RegNo]) has [N] active borrowings, exceeding the allowed limit of 3."`
- Blocks additional loans for affected users until their active count drops below 3.
- Fully allows return operations to resolve the violation.

### 📥 Return Management
- Quick search by Transaction ID, Borrower Name, or Book Title.
- Default view shows active loans with days held.
- One-click "Process Book Return" with confirmation dialog.
- Marks transaction `RETURNED`, records return date, restores book status to `AVAILABLE`, and updates the user's active count.
- Prevents double-returning transactions.

### 📊 Reports & Audit Export
- Catalog, membership, and circulation breakdown metrics.
- Category distribution and circulation percentages.
- Policy compliance audit table.
- "Generate Audit Report" tool with one-click clipboard copy for official documentation.

### 🔐 Role-Based Authentication & Access Control
- **Librarian (Staff) Role**: Full administrative access.
  - Manage catalog (Add, edit, delete books).
  - Manage users (Register, edit, safe deactivation).
  - Issue loans and process book returns.
  - View system analytics, policy alerts, and audit reports.
- **Student Member Role**: Restricted personalized self-service access.
  - **My Dashboard**: View active loans count, 3-book limit meter, personal violation notices, and held books list.
  - **Browse Catalog**: Search and filter available books in the library. Cannot add, edit, or delete books.
  - **My Loan History**: View personal active borrowings and past returns.
  - Cannot access other students' records, issue arbitrary loans, or process desk returns.
- **Quick Demo 1-Click Sign-In**: Dedicated 1-click buttons on the login screen for instant hackathon demonstrations.

---

## 2. Default Login Credentials

| Role | Username / ID | Password | Access Privileges |
|------|---------------|----------|-------------------|
| **Chief Librarian** | `admin` *(or `librarian`)* | `admin123` | Full access (Books, Users, Borrow, Return, Reports) |
| **Student (Alex Chen)** | `REG-2023-CS-001` *(or `alex`)* | `student123` | Student portal (Holds 4 books, Exceeds 3-book limit) |
| **Student (Marcus Johnson)** | `REG-2023-EE-012` *(or `marcus`)* | `student123` | Student portal (Holds 2 books, Eligible for 1 more) |
| **Student (Priya Sharma)** | `REG-2023-CS-045` *(or `priya`)* | `student123` | Student portal (Holds 1 active book, 2 returned books) |

---

## 3. Technology Stack & Constraints

- **Language**: Java 17+ / Java 21+ / Java 26 (Standard Java SE only).
- **GUI**: Java Swing (`JFrame`, `JPanel`, `JTable`, `JScrollPane`, `JTextField`, `JComboBox`, `JButton`, `JLabel`, `JDialog`, `CardLayout`).
- **Styling & Layout**: Java AWT (`Color`, `Font`, `BorderLayout`, `GridBagLayout`, `FlowLayout`, `Dimension`).
- **Storage**: Java Object Serialization (`ObjectInputStream`, `ObjectOutputStream`) with atomic file swapping (`StandardCopyOption.ATOMIC_MOVE`).
- **External Dependencies**: **None** (Standard JDK standard library only).

---

## 3. Project Directory Structure

```
javaproject/
├── src/
│   └── com/
│       └── library/
│           ├── Main.java                        # Application entry point
│           ├── DataInitializer.java             # Standalone data diagnostic utility
│           ├── model/
│           │   ├── Book.java                    # Book entity model
│           │   ├── BookStatus.java              # AVAILABLE, BORROWED enum
│           │   ├── LibraryUser.java             # User entity model
│           │   ├── BorrowingRecord.java         # Borrowing transaction model
│           │   ├── BorrowingStatus.java         # ACTIVE, RETURNED enum
│           │   └── UserViolationInfo.java       # Exceeding-limit policy DTO
│           ├── storage/
│           │   ├── LibraryData.java             # Serializable container for persistence
│           │   ├── StorageService.java          # Storage interface
│           │   └── FileStorageService.java      # Serialization implementation
│           ├── service/
│           │   ├── LibraryService.java          # Business logic, limits & validations
│           │   └── ValidationException.java     # Business rule exception
│           └── ui/
│               ├── UITheme.java                 # Modern color palette & component factory
│               ├── MainFrame.java               # Main window & sidebar navigation
│               ├── DashboardPanel.java          # KPI cards & policy alerts
│               ├── BooksPanel.java              # Book CRUD & filters
│               ├── UsersPanel.java              # User management & loan inspection
│               ├── BorrowingsPanel.java         # Borrow form & 3-book limit validation
│               ├── ReturnsPanel.java            # Return processing & table
│               ├── ReportsPanel.java            # Analytics & audit report preview
│               ├── BookDialog.java              # Book create/edit modal
│               └── UserDialog.java              # User registration modal
├── test/
│   └── BusinessLogicVerification.java          # 33 automated requirement tests
├── data/
│   └── library_data.ser                         # Persistent binary data storage
├── bin/                                         # Compiled bytecode output
├── build.bat                                    # One-click Windows build script
├── run.bat                                      # One-click Windows launch script
├── test.bat                                     # One-click verification runner
├── package.bat                                  # Packages into standalone executable JAR
├── LibraryManagementSystem.jar                  # Standalone executable desktop JAR
└── README.md                                    # Documentation
```

---

## 4. Simple Commands

You can run these simple 1-line scripts or commands directly in your terminal:

### 1. Build / Compile
```cmd
build.bat
```
*(Or manually: `javac -d bin src\com\library\*.java src\com\library\model\*.java src\com\library\storage\*.java src\com\library\service\*.java src\com\library\ui\*.java test\*.java`)*

### 2. Run Application (Launch Desktop GUI)
```cmd
run.bat
```
*(Or directly double-click `LibraryManagementSystem.jar` or run: `java -jar LibraryManagementSystem.jar`)*

### 3. Run Automated Tests (38 Assertions)
```cmd
test.bat
```
*(Or manually: `java -cp bin test.BusinessLogicVerification`)*

### 4. Package Standalone Executable JAR
```cmd
package.bat
```

---

## 5. Sample Demonstration Data

On initial launch, if `data/library_data.ser` is not present, rich demonstration data is generated automatically:
- **15 Books**: Across Computer Science, Electronics, Mathematics, Physics, and Management.
- **6 Users**: Students and faculty across various departments.
- **Active & Historical Borrowings**:
  - **Alex Chen (`USR-1001`)**: Has **4 active borrowings** (demonstrating Requirement 8: warning banner appears on Dashboard, 5th borrowing is rejected, returns are permitted).
  - **Marcus Johnson (`USR-1003`)**: Has **2 active borrowings** (eligible to borrow 1 more book).
  - **Priya Sharma (`USR-1002`)**: Has **1 active borrowing** and 2 returned transactions in history.
  - **Sophia Rodriguez (`USR-1004`)**: Has returned history.

---

## 6. Functional Verification Checklist (12 Hackathon Scenarios)

| # | Test Scenario | Expected Result | Status |
|---|---------------|-----------------|:------:|
| 1 | Add, edit, search, sort, and delete a book | ID auto-generated (`BK-XXXX`), fields update, searches filter correctly, unborrowed book deletes safely | **PASSED** |
| 2 | Register, edit, search, and deactivate a user | Registration number uniqueness enforced, user updates, safe deactivation preserves history | **PASSED** |
| 3 | Borrow an available book | Transaction created (`TXN-XXXXX`), status set to `ACTIVE`, book marked `BORROWED` | **PASSED** |
| 4 | Allow up to 3 active borrowings for a user | User can borrow book 1, book 2, and book 3 without error | **PASSED** |
| 5 | Reject fourth active borrowing | Attempting a 4th borrow is blocked with clear message; book stays `AVAILABLE` | **PASSED** |
| 6 | Return a book and borrow another | Returning decreases active count to 2, allowing a subsequent borrowing | **PASSED** |
| 7 | Load user with 4 active borrowings (Requirement 8) | Preserves all 4 records without deletion; displays warning banner on Dashboard | **PASSED** |
| 8 | Reject borrowing for exceeding user until count < 3 | Borrowing blocked at 4; returning 1 drops count to 3 (still blocked); returning 2nd drops count to 2 (borrowing permitted) | **PASSED** |
| 9 | Preserved history for returned transactions | Completed returns remain intact in circulation logs and reports | **PASSED** |
| 10 | Application persistence across restarts | Changes save to `data/library_data.ser` atomically; data reloads on restart | **PASSED** |
| 11 | Unavailable books cannot be borrowed twice | Book already borrowed throws validation error; prevents double loaning | **PASSED** |
| 12 | Dashboard statistics update in real time | Counters accurately reflect additions, deletions, loans, returns, and violations | **PASSED** |

