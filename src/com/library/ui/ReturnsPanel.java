package com.library.ui;

import com.library.model.Book;
import com.library.model.BorrowingRecord;
import com.library.model.LibraryUser;
import com.library.service.LibraryService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * Returns Management Panel designed for fast return processing,
 * overdue/duration tracking, and real-time inventory restoration.
 */
@SuppressWarnings("serial")
public class ReturnsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryService libraryService;
    private final MainFrame mainFrame;

    private JTable returnsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JCheckBox showActiveOnlyCheckBox;
    private JLabel countLabel;

    public ReturnsPanel(LibraryService libraryService, MainFrame mainFrame) {
        this.libraryService = libraryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        refreshTable();
    }

    private void initComponents() {
        // Top Card Toolbar
        JPanel topCard = UITheme.createCardPanel();
        topCard.setLayout(new BorderLayout(0, 12));

        // Header Title row
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("Book Return Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Select an active transaction to record return, restore book availability, and reduce user count");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        // Action Buttons
        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnRow.setOpaque(false);

        JButton processReturnBtn = UITheme.createPrimaryButton("✓ Process Book Return");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        processReturnBtn.addActionListener(e -> onProcessReturn());
        refreshBtn.addActionListener(e -> refreshTable());

        actionBtnRow.add(processReturnBtn);
        actionBtnRow.add(refreshBtn);

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(actionBtnRow, BorderLayout.EAST);
        topCard.add(headerRow, BorderLayout.NORTH);

        // Filter / Search Toolbar Row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        searchField = UITheme.createTextField(22);
        searchField.setToolTipText("Search by Transaction ID, User, or Book Title");

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> applyFilter());

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            showActiveOnlyCheckBox.setSelected(true);
            applyFilter();
        });

        showActiveOnlyCheckBox = new JCheckBox("Show Active Borrowings Only", true);
        showActiveOnlyCheckBox.setFont(UITheme.FONT_BOLD);
        showActiveOnlyCheckBox.setBackground(Color.WHITE);
        showActiveOnlyCheckBox.addActionListener(e -> applyFilter());

        filterRow.add(new JLabel("Quick Search:"));
        filterRow.add(searchField);
        filterRow.add(searchBtn);
        filterRow.add(clearBtn);
        filterRow.add(Box.createHorizontalStrut(15));
        filterRow.add(showActiveOnlyCheckBox);

        topCard.add(filterRow, BorderLayout.SOUTH);
        add(topCard, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        String[] cols = {"Transaction ID", "User Name", "Registration No.", "Book ID", "Book Title", "Borrow Date", "Days Held", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        returnsTable = new JTable(tableModel);
        UITheme.styleTable(returnsTable);
        rowSorter = new TableRowSorter<>(tableModel);
        returnsTable.setRowSorter(rowSorter);

        returnsTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        returnsTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        returnsTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        returnsTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        returnsTable.getColumnModel().getColumn(4).setPreferredWidth(260);
        returnsTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        returnsTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        returnsTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        returnsTable.getColumnModel().getColumn(7).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(returnsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Table Status Row
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        countLabel = new JLabel("Showing 0 transactions");
        countLabel.setFont(UITheme.FONT_SUBTITLE);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel instructionLabel = new JLabel("Double-click an active row or select and click 'Process Book Return' above.");
        instructionLabel.setFont(UITheme.FONT_SMALL);
        instructionLabel.setForeground(UITheme.TEXT_MUTED);

        bottomRow.add(countLabel, BorderLayout.WEST);
        bottomRow.add(instructionLabel, BorderLayout.EAST);
        tableCard.add(bottomRow, BorderLayout.SOUTH);

        // Double click to process return
        returnsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && returnsTable.getSelectedRow() != -1) {
                    onProcessReturn();
                }
            }
        });

        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshTable() {
        applyFilter();
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        boolean activeOnly = showActiveOnlyCheckBox.isSelected();

        List<BorrowingRecord> records = libraryService.getAllBorrowings();
        int count = 0;

        for (BorrowingRecord r : records) {
            if (activeOnly && !r.isActive()) {
                continue;
            }

            LibraryUser user = libraryService.getUserById(r.getUserId());
            Book book = libraryService.getBookById(r.getBookId());

            String userName = user != null ? user.getFullName() : r.getUserId();
            String regNo = user != null ? user.getRegistrationNumber() : "N/A";
            String bookTitle = book != null ? book.getTitle() : r.getBookId();

            if (!query.isEmpty()) {
                boolean match = r.getTransactionId().toLowerCase(Locale.ROOT).contains(query) ||
                        userName.toLowerCase(Locale.ROOT).contains(query) ||
                        regNo.toLowerCase(Locale.ROOT).contains(query) ||
                        bookTitle.toLowerCase(Locale.ROOT).contains(query) ||
                        r.getBookId().toLowerCase(Locale.ROOT).contains(query);
                if (!match) continue;
            }

            count++;
            long daysHeld = ChronoUnit.DAYS.between(r.getBorrowDate(), 
                    r.getReturnDate() != null ? r.getReturnDate() : LocalDate.now());

            tableModel.addRow(new Object[]{
                    r.getTransactionId(),
                    userName,
                    regNo,
                    r.getBookId(),
                    bookTitle,
                    r.getBorrowDate(),
                    daysHeld + " day(s)",
                    r.getStatus().getDisplayName()
            });
        }

        countLabel.setText("Showing " + count + " transaction" + (count == 1 ? "" : "s") + 
                (activeOnly ? " (Active Only)" : " (All Transactions)"));
    }

    private void onProcessReturn() {
        int selectedRow = returnsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an active borrowing transaction from the table.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = returnsTable.convertRowIndexToModel(selectedRow);
        String txnId = (String) tableModel.getValueAt(modelRow, 0);
        String userName = (String) tableModel.getValueAt(modelRow, 1);
        String bookTitle = (String) tableModel.getValueAt(modelRow, 4);
        String status = (String) tableModel.getValueAt(modelRow, 7);

        if ("Returned".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this,
                    "Transaction " + txnId + " is already marked as RETURNED.\nYou cannot return an already returned book.",
                    "Already Returned", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BorrowingRecord record = libraryService.getBorrowingById(txnId);
        if (record == null) {
            JOptionPane.showMessageDialog(this, "Transaction not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm Return Processing:\n\n" +
                "• Transaction ID: " + txnId + "\n" +
                "• Borrower: " + userName + "\n" +
                "• Book: " + bookTitle + "\n" +
                "• Return Date: Today (" + LocalDate.now() + ")\n\n" +
                "The book will immediately become available in the catalog and the user's active count will decrease.",
                "Confirm Return", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                libraryService.returnBook(txnId, LocalDate.now());

                JOptionPane.showMessageDialog(this,
                        "Book successfully returned!\n" +
                        "Book '" + bookTitle + "' is now AVAILABLE in the library catalog.\n" +
                        "Borrower's active count has been updated.",
                        "Return Successful", JOptionPane.INFORMATION_MESSAGE);

                refreshTable();
                mainFrame.notifyDataChanged();

            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Return Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unexpected error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
