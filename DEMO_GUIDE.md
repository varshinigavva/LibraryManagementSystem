# Hackathon Live Demonstration Walkthrough Guide
**College Library Management System**

Follow this 5-minute step-by-step walkthrough during your live hackathon judging to demonstrate every required feature smoothly.

---

### Step 0: Login Screen & Role Demonstration (45 sec)
1. Run `run.bat` (or `java -cp bin com.library.Main`).
2. **Show Judges the Login Screen**:
   - Notice the role selector (`Librarian (Staff)` vs `Student Member`).
   - Notice the convenient **1-Click Quick Demo Sign-In** buttons at the bottom:
     - `👑 Quick Login: Chief Librarian (Staff)`
     - `🎓 Quick Login: Alex Chen (Student — 4 Books Held)`
     - `🎓 Quick Login: Marcus Johnson (Student — 2 Books Held)`
3. Click **"👑 Quick Login: Chief Librarian (Staff)"** to enter the Administrative Portal.

---

### Step 1: Librarian Dashboard & Policy Alert (1 min)
   - Point out the 6 Key Metric Cards: Total Books (15), Available Books (8), Currently Borrowed (7), Total Users (6), Completed Returns (3), and Users Exceeding Limit (1).
   - **Show Requirement 8**: Point out the prominent yellow Policy Alert Banner:
     > *"⚠️ POLICY ALERT: 1 User has exceeded the 3-book borrowing limit"*
     > *"• Warning: User 'Alex Chen' (REG-2023-CS-001) has 4 active borrowings, exceeding the allowed limit of 3."*
   - Note that historical data was preserved safely without automatic deletion.

---

### Step 2: Book Catalog Operations (1 min)
1. Click **"Books"** in the sidebar.
2. **Search & Filter**:
   - Type `"Algorithms"` in the Search field and click **Search**. The table instantly filters down to the matching book.
   - Click **Clear**, then select `"Electronics"` in the **Category** dropdown.
3. **Add a New Book**:
   - Click **"+ Add New Book"**.
   - Enter Title: `Computer Architecture`, Author: `David Patterson`, Category: `Computer Science`.
   - Click **Add Book**. Observe the auto-generated Book ID (`BK-1016`) and status `Available`.
4. **Safety Lock on Deletion (Requirement 4)**:
   - Select `Introduction to Algorithms` (Status: `Borrowed`) and click **Delete Book**.
   - Click **Yes** on confirmation dialog.
   - **Show Judges**: The system blocks deletion and displays the explanation:
     > *"Cannot delete book 'Introduction to Algorithms' (ID: BK-1001) because it is currently borrowed by an active transaction (TXN-10001)."*

---

### Step 3: Member Management & Safe Deactivation (45 sec)
1. Click **"Users"** in the sidebar.
2. Select **"Alex Chen"** and click **"View Borrowed Books"**.
   - A modal opens displaying all 4 books currently in Alex Chen's possession.
3. **Safe Deactivation / History Preservation (Requirement 5)**:
   - Select **"Sophia Rodriguez"** (has completed return history, 0 active loans).
   - Click **"Delete / Deactivate"** and confirm.
   - **Show Judges**: The system deactivates the user (`[Deactivated]`) instead of deleting them, permanently preserving her historical borrowing transaction!

---

### Step 4: Strict Three-Book Limit Enforcement (1 min)
1. Click **"Borrowings"** in the sidebar.
2. **Test 1 — User Exceeding Limit (Alex Chen has 4 active borrowings)**:
   - In the "Select Registered User" dropdown, select **Alex Chen**.
   - **Show Judges**: The live status badge immediately turns red:  
     `⚠️ Borrowing Blocked (4 / 3 active)`. The "Confirm Borrowing" button is disabled.
3. **Test 2 — User at Limit (Test 3-book limit enforcement)**:
   - Select **Marcus Johnson** (currently has 2 active borrowings).
   - The badge shows green: `✓ Eligible (2 / 3 active)`.
   - Select any available book and click **Confirm Borrowing**.
   - Marcus now has 3 active borrowings (`3 / 3 active`).
   - Try to borrow a 4th book for Marcus:
   - **Show Judges**: The system blocks the 4th borrowing with:
     > *"Borrowing rejected: This user has already borrowed the maximum of 3 books."*

---

### Step 5: Return Processing & Limit Restoration (45 sec)
1. Click **"Returns"** in the sidebar.
2. Locate transaction `TXN-10001` (Alex Chen returning *Introduction to Algorithms*).
3. Click **"Process Book Return"** and confirm.
4. **Show Judges**:
   - The book is immediately marked `RETURNED`.
   - In the "Books" catalog, *Introduction to Algorithms* is now restored to `Available`.
   - Alex Chen's active count decreases from 4 to 3.
5. Return a second book for Alex Chen (`TXN-10002`).
   - Alex Chen's active count drops to 2.
6. Return to **"Borrowings"**: Alex Chen is now eligible to borrow again!

---

### Step 6: Reports & Audit Generation (30 sec)
1. Click **"Reports"** in the sidebar.
2. Review the aggregated metrics, the policy compliance table, and the category breakdown.
3. Click **"Generate Audit Report"**:
   - Shows the clean, formatted institutional audit report.
   - Click **"Copy Report Text"** to demonstrate clipboard integration.

---

### Step 7: Switch to Student Portal (Role-Based Access) (45 sec)
1. In the sidebar footer, click **"⏻ Sign Out / Switch Role"**.
2. Click **"🎓 Quick Login: Alex Chen (Student — 4 Books Held)"**.
3. **Show Judges the Student Experience**:
   - **Personalized Dashboard**: Displays Alex's active loans (4), borrowing limit (3), and a personalized policy warning:
     > *"⚠️ POLICY NOTICE: You have 4 active borrowings, exceeding the 3-book limit. Please return outstanding books..."*
   - Table of books currently in Alex's possession.
   - **Browse Catalog**: Students can search and browse available books, but cannot add, edit, or delete catalog records.
   - **My Loans History**: Shows only Alex's personal transactions. Administrative tabs (Users, Issue Loan, Returns, Admin Reports) are hidden.

---

### Quick Reset for Next Demo
- Return to **Chief Librarian** session, go to **Dashboard**, and click **"↺ Reset Demo Data"** to restore the initial sample dataset (with Alex Chen holding 4 books) in 1 click for the next judging round!
