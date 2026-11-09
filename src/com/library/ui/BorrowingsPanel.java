package com.library.ui;

import com.library.model.Book;
import com.library.model.BorrowingRecord;
import com.library.model.BorrowingStatus;
import com.library.model.LibraryUser;
import com.library.service.LibraryService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Borrowing Management Panel providing interactive borrowing creation,
 * real-time three-book limit validation, status badges, and transaction history.
 */
@SuppressWarnings("serial")
public class BorrowingsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryService libraryService;
    private final MainFrame mainFrame;

    // Form inputs
    private JPanel formCard;
    private JComboBox<UserComboItem> userComboBox;
    private JComboBox<BookComboItem> bookComboBox;
    private JTextField borrowDateField;
    private JLabel limitStatusBadge;
    private JButton confirmBorrowBtn;

    // History table
    private JTable borrowingTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JComboBox<String> userFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JLabel countLabel;

    public BorrowingsPanel(LibraryService libraryService, MainFrame mainFrame) {
        this.libraryService = libraryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        refreshAll();
    }

    private void initComponents() {
        // TOP CARD: New Borrowing Transaction Form
        formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 14));

        JPanel formTitleRow = new JPanel(new BorderLayout());
        formTitleRow.setOpaque(false);

        JLabel formTitle = new JLabel("Issue / Borrow Book Transaction");
        formTitle.setFont(UITheme.FONT_TITLE);
        formTitle.setForeground(UITheme.TEXT_PRIMARY);

        JLabel formSub = new JLabel("Select an active member and an available book. Maximum 3 active borrowings enforced per user.");
        formSub.setFont(UITheme.FONT_SUBTITLE);
        formSub.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);
        titleBlock.add(formTitle);
        titleBlock.add(formSub);
        formTitleRow.add(titleBlock, BorderLayout.WEST);

        formCard.add(formTitleRow, BorderLayout.NORTH);

        // Form Fields Grid
        JPanel fieldsGrid = new JPanel(new GridBagLayout());
        fieldsGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 1. User Selection
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLbl = new JLabel("Select Registered User *:");
        userLbl.setFont(UITheme.FONT_BOLD);
        fieldsGrid.add(userLbl, gbc);

        gbc.gridx = 1;
        userComboBox = new JComboBox<>();
        userComboBox.setFont(UITheme.FONT_REGULAR);
        userComboBox.setPreferredSize(new Dimension(320, 34));
        userComboBox.addActionListener(e -> updateLimitBadge());
        fieldsGrid.add(userComboBox, gbc);

        // Limit badge next to user combo
        gbc.gridx = 2;
        limitStatusBadge = new JLabel("Active: 0 / 3");
        limitStatusBadge.setFont(UITheme.FONT_BOLD);
        limitStatusBadge.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        fieldsGrid.add(limitStatusBadge, gbc);

        // 2. Book Selection
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel bookLbl = new JLabel("Select Available Book *:");
        bookLbl.setFont(UITheme.FONT_BOLD);
        fieldsGrid.add(bookLbl, gbc);

        gbc.gridx = 1;
        bookComboBox = new JComboBox<>();
        bookComboBox.setFont(UITheme.FONT_REGULAR);
        bookComboBox.setPreferredSize(new Dimension(320, 34));
        fieldsGrid.add(bookComboBox, gbc);

        // 3. Borrow Date
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel dateLbl = new JLabel("Borrow Date (YYYY-MM-DD):");
        dateLbl.setFont(UITheme.FONT_BOLD);
        fieldsGrid.add(dateLbl, gbc);

        gbc.gridx = 1;
        borrowDateField = UITheme.createTextField(15);
        borrowDateField.setText(LocalDate.now().toString());
        fieldsGrid.add(borrowDateField, gbc);

        // 4. Confirm Button
        gbc.gridx = 2;
        confirmBorrowBtn = UITheme.createPrimaryButton("Confirm Borrowing");
        confirmBorrowBtn.addActionListener(e -> onConfirmBorrowing());
        fieldsGrid.add(confirmBorrowBtn, gbc);

        formCard.add(fieldsGrid, BorderLayout.CENTER);
        add(formCard, BorderLayout.NORTH);

        // CENTER CARD: All Borrowings Table
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        // Table toolbar row
        JPanel tableToolbar = new JPanel(new BorderLayout());
        tableToolbar.setOpaque(false);

        JLabel historyTitle = new JLabel("Circulation Transactions Log");
        historyTitle.setFont(UITheme.FONT_SECTION);
        historyTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterControls.setOpaque(false);

        userFilterCombo = UITheme.createComboBox(new String[]{"All Users"});
        userFilterCombo.addActionListener(e -> applyFilters());

        statusFilterCombo = UITheme.createComboBox(new String[]{"All Transactions", "ACTIVE Only", "RETURNED Only"});
        statusFilterCombo.addActionListener(e -> applyFilters());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshAll());

        filterControls.add(new JLabel("Filter by User:"));
        filterControls.add(userFilterCombo);
        filterControls.add(new JLabel("Status:"));
        filterControls.add(statusFilterCombo);
        filterControls.add(refreshBtn);

        tableToolbar.add(historyTitle, BorderLayout.WEST);
        tableToolbar.add(filterControls, BorderLayout.EAST);
        tableCard.add(tableToolbar, BorderLayout.NORTH);

        String[] cols = {"Transaction ID", "User Name", "Registration No.", "Book Title", "Borrow Date", "Return Date", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        borrowingTable = new JTable(tableModel);
        UITheme.styleTable(borrowingTable);
        rowSorter = new TableRowSorter<>(tableModel);
        borrowingTable.setRowSorter(rowSorter);

        borrowingTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        borrowingTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        borrowingTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        borrowingTable.getColumnModel().getColumn(3).setPreferredWidth(260);
        borrowingTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        borrowingTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        borrowingTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        borrowingTable.getColumnModel().getColumn(6).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(borrowingTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom status count
        JPanel bottomStatus = new JPanel(new BorderLayout());
        bottomStatus.setOpaque(false);
        countLabel = new JLabel("Showing 0 transactions");
        countLabel.setFont(UITheme.FONT_SUBTITLE);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        bottomStatus.add(countLabel, BorderLayout.WEST);
        tableCard.add(bottomStatus, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshAll() {
        boolean isLibrarian = mainFrame.getCurrentUser() == null || mainFrame.getCurrentUser().isLibrarian();
        formCard.setVisible(isLibrarian);
        userFilterCombo.setVisible(isLibrarian);

        if (isLibrarian) {
            populateUserDropdown();
            populateBookDropdown();
            populateUserFilterDropdown();
        }
        applyFilters();
    }

    private void populateUserDropdown() {
        UserComboItem selected = (UserComboItem) userComboBox.getSelectedItem();
        userComboBox.removeAllItems();

        List<LibraryUser> activeUsers = libraryService.getActiveUsers();
        for (LibraryUser u : activeUsers) {
            int activeBorrowings = libraryService.getActiveBorrowingCount(u.getUserId());
            userComboBox.addItem(new UserComboItem(u, activeBorrowings));
        }

        if (selected != null) {
            for (int i = 0; i < userComboBox.getItemCount(); i++) {
                if (userComboBox.getItemAt(i).user.getUserId().equals(selected.user.getUserId())) {
                    userComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        updateLimitBadge();
    }

    private void populateBookDropdown() {
        BookComboItem selected = (BookComboItem) bookComboBox.getSelectedItem();
        bookComboBox.removeAllItems();

        List<Book> availableBooks = libraryService.getAvailableBooks();
        if (availableBooks.isEmpty()) {
            bookComboBox.addItem(new BookComboItem(null));
        } else {
            for (Book b : availableBooks) {
                bookComboBox.addItem(new BookComboItem(b));
            }
        }

        if (selected != null && selected.book != null) {
            for (int i = 0; i < bookComboBox.getItemCount(); i++) {
                BookComboItem item = bookComboBox.getItemAt(i);
                if (item.book != null && item.book.getBookId().equals(selected.book.getBookId())) {
                    bookComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void populateUserFilterDropdown() {
        String currentFilter = (String) userFilterCombo.getSelectedItem();
        userFilterCombo.removeAllItems();
        userFilterCombo.addItem("All Users");

        for (LibraryUser u : libraryService.getAllUsers()) {
            userFilterCombo.addItem(u.getFullName() + " (" + u.getUserId() + ")");
        }

        if (currentFilter != null) {
            userFilterCombo.setSelectedItem(currentFilter);
        }
    }

    private void updateLimitBadge() {
        UserComboItem selected = (UserComboItem) userComboBox.getSelectedItem();
        if (selected == null || selected.user == null) {
            limitStatusBadge.setText("No User Selected");
            limitStatusBadge.setBackground(UITheme.ROW_ALT);
            limitStatusBadge.setForeground(UITheme.TEXT_MUTED);
            confirmBorrowBtn.setEnabled(false);
            return;
        }

        int count = libraryService.getActiveBorrowingCount(selected.user.getUserId());
        if (count >= 3) {
            limitStatusBadge.setText("⚠️ Borrowing Blocked (" + count + " / 3 active)");
            limitStatusBadge.setBackground(UITheme.DANGER_LIGHT);
            limitStatusBadge.setForeground(UITheme.DANGER);
            limitStatusBadge.setOpaque(true);
            confirmBorrowBtn.setEnabled(false);
            confirmBorrowBtn.setToolTipText("User has reached or exceeded the 3-book limit. Must return a book first.");
        } else {
            limitStatusBadge.setText("✓ Eligible (" + count + " / 3 active)");
            limitStatusBadge.setBackground(UITheme.SUCCESS_LIGHT);
            limitStatusBadge.setForeground(UITheme.SUCCESS);
            limitStatusBadge.setOpaque(true);
            confirmBorrowBtn.setEnabled(true);
            confirmBorrowBtn.setToolTipText("Ready to borrow");
        }
    }

    private void applyFilters() {
        tableModel.setRowCount(0);
        String selectedUserFilter = (String) userFilterCombo.getSelectedItem();
        String selectedStatusFilter = (String) statusFilterCombo.getSelectedItem();
        boolean isLibrarian = mainFrame.getCurrentUser() == null || mainFrame.getCurrentUser().isLibrarian();

        String targetUserId = null;
        if (!isLibrarian && mainFrame.getCurrentUser() != null) {
            targetUserId = mainFrame.getCurrentUser().getUserId();
        } else if (selectedUserFilter != null && !"All Users".equals(selectedUserFilter)) {
            int openParen = selectedUserFilter.lastIndexOf('(');
            int closeParen = selectedUserFilter.lastIndexOf(')');
            if (openParen != -1 && closeParen != -1) {
                targetUserId = selectedUserFilter.substring(openParen + 1, closeParen).trim();
            }
        }

        List<BorrowingRecord> allRecords = libraryService.getAllBorrowings();
        int matchingCount = 0;

        for (BorrowingRecord r : allRecords) {
            if (targetUserId != null && !r.getUserId().equalsIgnoreCase(targetUserId)) {
                continue;
            }

            if ("ACTIVE Only".equals(selectedStatusFilter) && !r.isActive()) {
                continue;
            }
            if ("RETURNED Only".equals(selectedStatusFilter) && r.isActive()) {
                continue;
            }

            matchingCount++;
            LibraryUser user = libraryService.getUserById(r.getUserId());
            Book book = libraryService.getBookById(r.getBookId());

            tableModel.addRow(new Object[]{
                    r.getTransactionId(),
                    user != null ? user.getFullName() : r.getUserId(),
                    user != null ? user.getRegistrationNumber() : "N/A",
                    book != null ? book.getTitle() : r.getBookId(),
                    r.getBorrowDate(),
                    r.getReturnDate() != null ? r.getReturnDate() : "—",
                    r.getStatus().getDisplayName()
            });
        }

        countLabel.setText("Showing " + matchingCount + " of " + allRecords.size() + " total transactions");
    }

    private void onConfirmBorrowing() {
        UserComboItem selectedUser = (UserComboItem) userComboBox.getSelectedItem();
        if (selectedUser == null || selectedUser.user == null) {
            JOptionPane.showMessageDialog(this, "Please select a registered user.",
                    "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BookComboItem selectedBook = (BookComboItem) bookComboBox.getSelectedItem();
        if (selectedBook == null || selectedBook.book == null) {
            JOptionPane.showMessageDialog(this, "No available book selected.",
                    "Missing Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate borrowDate;
        try {
            borrowDate = LocalDate.parse(borrowDateField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.",
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BorrowingRecord record = libraryService.borrowBook(
                    selectedUser.user.getUserId(),
                    selectedBook.book.getBookId(),
                    borrowDate
            );

            JOptionPane.showMessageDialog(this,
                    "Borrowing transaction confirmed successfully!\n" +
                    "Transaction ID: " + record.getTransactionId() + "\n" +
                    "Borrower: " + selectedUser.user.getFullName() + "\n" +
                    "Book: " + selectedBook.book.getTitle() + "\n" +
                    "Borrow Date: " + record.getBorrowDate(),
                    "Transaction Confirmed", JOptionPane.INFORMATION_MESSAGE);

            refreshAll();
            mainFrame.notifyDataChanged();

        } catch (ValidationException ve) {
            // Strict 3-book rejection popup
            JOptionPane.showMessageDialog(this, ve.getMessage(),
                    "Borrowing Rejected", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System error processing transaction: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Helper items for ComboBoxes
    private static class UserComboItem {
        final LibraryUser user;
        final int activeCount;

        UserComboItem(LibraryUser user, int activeCount) {
            this.user = user;
            this.activeCount = activeCount;
        }

        @Override
        public String toString() {
            if (user == null) return "No Users Available";
            return user.getFullName() + " (" + user.getRegistrationNumber() + ") — [" + activeCount + " Active]";
        }
    }

    private static class BookComboItem {
        final Book book;

        BookComboItem(Book book) {
            this.book = book;
        }

        @Override
        public String toString() {
            if (book == null) return "No Available Books In Stock";
            return book.getTitle() + " (" + book.getBookId() + ") - " + book.getCategory();
        }
    }
}
