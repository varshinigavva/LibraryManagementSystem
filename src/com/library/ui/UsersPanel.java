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
import java.util.List;

/**
 * Complete User Management panel providing registration, editing,
 * search, safe deactivation/deletion, and current borrowed books inspection.
 */
@SuppressWarnings("serial")
public class UsersPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryService libraryService;
    private final MainFrame mainFrame;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JLabel countLabel;

    public UsersPanel(LibraryService libraryService, MainFrame mainFrame) {
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
        JLabel titleLabel = new JLabel("Library User Directory");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Register students/faculty, view active borrowings, and manage memberships");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        // Action Buttons
        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnRow.setOpaque(false);

        JButton addBtn = UITheme.createPrimaryButton("+ Register User");
        JButton editBtn = UITheme.createSecondaryButton("Edit User");
        JButton viewBooksBtn = UITheme.createSecondaryButton("View Borrowed Books");
        JButton deleteBtn = UITheme.createDangerButton("Delete / Deactivate");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        addBtn.addActionListener(e -> onAddUser());
        editBtn.addActionListener(e -> onEditUser());
        viewBooksBtn.addActionListener(e -> onViewBorrowedBooks());
        deleteBtn.addActionListener(e -> onDeleteUser());
        refreshBtn.addActionListener(e -> refreshTable());

        actionBtnRow.add(addBtn);
        actionBtnRow.add(editBtn);
        actionBtnRow.add(viewBooksBtn);
        actionBtnRow.add(deleteBtn);
        actionBtnRow.add(refreshBtn);

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(actionBtnRow, BorderLayout.EAST);
        topCard.add(headerRow, BorderLayout.NORTH);

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchRow.setOpaque(false);

        searchField = UITheme.createTextField(22);
        searchField.setToolTipText("Search user name, registration number, or department");

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> applySearch());

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            applySearch();
        });

        searchRow.add(new JLabel("Search Users:"));
        searchRow.add(searchField);
        searchRow.add(searchBtn);
        searchRow.add(clearBtn);

        topCard.add(searchRow, BorderLayout.SOUTH);
        add(topCard, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        String[] columnNames = {"User ID", "Full Name", "Registration No.", "Department", "Contact Details", "Active Books", "Account Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        userTable = new JTable(tableModel);
        UITheme.styleTable(userTable);
        rowSorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(rowSorter);

        userTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(160);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(210);
        userTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        userTable.getColumnModel().getColumn(6).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Table Status Row
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        countLabel = new JLabel("Showing 0 users");
        countLabel.setFont(UITheme.FONT_SUBTITLE);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel safetyNotice = new JLabel("Notice: Users with borrowing history are safely deactivated rather than erased to preserve transaction integrity.");
        safetyNotice.setFont(UITheme.FONT_SMALL);
        safetyNotice.setForeground(UITheme.TEXT_MUTED);

        bottomRow.add(countLabel, BorderLayout.WEST);
        bottomRow.add(safetyNotice, BorderLayout.EAST);
        tableCard.add(bottomRow, BorderLayout.SOUTH);

        // Double click to edit or view
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && userTable.getSelectedRow() != -1) {
                    onEditUser();
                }
            }
        });

        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshTable() {
        applySearch();
    }

    private void applySearch() {
        tableModel.setRowCount(0);
        String query = searchField.getText().trim();
        List<LibraryUser> users = libraryService.searchUsers(query);

        for (LibraryUser u : users) {
            int activeBooks = libraryService.getActiveBorrowingCount(u.getUserId());
            String statusStr = u.isActive() ? "Active" : "Deactivated";
            tableModel.addRow(new Object[]{
                    u.getUserId(),
                    u.getFullName(),
                    u.getRegistrationNumber(),
                    u.getDepartment(),
                    u.getContactDetails(),
                    activeBooks + (activeBooks > 3 ? " (⚠️ >3)" : ""),
                    statusStr
            });
        }

        countLabel.setText("Showing " + users.size() + " of " + libraryService.getTotalUsersCount() + " registered users");
    }

    private void onAddUser() {
        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New User", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                LibraryUser newUser = libraryService.registerUser(
                        dialog.getFullName(),
                        dialog.getRegistrationNumber(),
                        dialog.getDepartment(),
                        dialog.getContactDetails()
                );
                JOptionPane.showMessageDialog(this,
                        "User successfully registered!\nGenerated User ID: " + newUser.getUserId(),
                        "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.notifyDataChanged();
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Registration Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error registering user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table to edit.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String userId = (String) tableModel.getValueAt(modelRow, 0);
        LibraryUser user = libraryService.getUserById(userId);

        if (user == null) {
            JOptionPane.showMessageDialog(this, "Selected user could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserDialog dialog = new UserDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User Details", user);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                libraryService.updateUser(
                        userId,
                        dialog.getFullName(),
                        dialog.getRegistrationNumber(),
                        dialog.getDepartment(),
                        dialog.getContactDetails(),
                        dialog.isUserActive()
                );
                JOptionPane.showMessageDialog(this, "User details updated successfully.",
                        "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.notifyDataChanged();
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete or deactivate.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String userId = (String) tableModel.getValueAt(modelRow, 0);
        String userName = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to remove user '" + userName + "' (" + userId + ")?\n\n" +
                "• If user has active borrowed books, deletion will be blocked.\n" +
                "• If user has historical return transactions, account will be safely deactivated to preserve records.",
                "Confirm User Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String outcomeMessage = libraryService.deleteOrDeactivateUser(userId);
                JOptionPane.showMessageDialog(this, outcomeMessage, "User Action Completed", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.notifyDataChanged();
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, ve.getMessage(), "Action Blocked", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error processing user: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onViewBorrowedBooks() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to view their borrowed books.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String userId = (String) tableModel.getValueAt(modelRow, 0);
        LibraryUser user = libraryService.getUserById(userId);

        if (user == null) return;

        List<BorrowingRecord> activeRecords = libraryService.getActiveBorrowingsForUser(userId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Active Borrowings - " + user.getFullName(), true);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.setSize(600, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));
        panel.setBackground(Color.WHITE);

        JLabel info = new JLabel("Currently Borrowed by: " + user.getFullName() + 
                " (" + user.getRegistrationNumber() + ") — Total: " + activeRecords.size() + " book(s)");
        info.setFont(UITheme.FONT_HEADER);
        panel.add(info, BorderLayout.NORTH);

        String[] cols = {"Txn ID", "Book ID", "Book Title", "Author", "Borrow Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (BorrowingRecord r : activeRecords) {
            Book b = libraryService.getBookById(r.getBookId());
            model.addRow(new Object[]{
                    r.getTransactionId(),
                    r.getBookId(),
                    b != null ? b.getTitle() : "N/A",
                    b != null ? b.getAuthor() : "N/A",
                    r.getBorrowDate()
            });
        }

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bp.setBackground(Color.WHITE);
        bp.add(closeBtn);
        panel.add(bp, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }
}
