package com.library.ui;

import com.library.model.Book;
import com.library.model.BorrowingRecord;
import com.library.model.LibraryUser;
import com.library.model.UserViolationInfo;
import com.library.service.LibraryService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Main dashboard displaying real-time library KPIs, limit-exceeded alerts,
 * and recent transaction activity.
 */
@SuppressWarnings("serial")
public class DashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryService libraryService;
    private final MainFrame mainFrame;

    // Stat card labels
    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel borrowedBooksLabel;
    private JLabel totalUsersLabel;
    private JLabel returnedTxnsLabel;
    private JLabel limitExceededLabel;

    // Warning container panel
    private JPanel warningContainer;
    private JTable recentBorrowingsTable;
    private DefaultTableModel recentBorrowingsModel;

    public DashboardPanel(LibraryService libraryService, MainFrame mainFrame) {
        this.libraryService = libraryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        refreshData();
    }

    private void initComponents() {
        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Library Overview Dashboard");
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Real-time circulation metrics, inventory status, and policy alerts");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(welcomeLabel);
        titleBlock.add(subLabel);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnGroup.setOpaque(false);

        JButton resetBtn = UITheme.createSecondaryButton("↺ Reset Demo Data");
        resetBtn.setToolTipText("Resets data back to the clean Hackathon demo state with Alex Chen having 4 active borrowings");
        resetBtn.addActionListener(e -> onResetDemoData());

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh Dashboard");
        refreshBtn.addActionListener(e -> refreshData());

        btnGroup.add(resetBtn);
        btnGroup.add(refreshBtn);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(btnGroup, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center scrollable content
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // 1. Stat cards grid (2 rows x 3 columns)
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(3000, 210));

        totalBooksLabel = new JLabel("0");
        availableBooksLabel = new JLabel("0");
        borrowedBooksLabel = new JLabel("0");
        totalUsersLabel = new JLabel("0");
        returnedTxnsLabel = new JLabel("0");
        limitExceededLabel = new JLabel("0");

        statsGrid.add(createStatCard("Total Books Cataloged", totalBooksLabel, UITheme.PRIMARY, "All registered books"));
        statsGrid.add(createStatCard("Available Books", availableBooksLabel, UITheme.SUCCESS, "Ready to borrow"));
        statsGrid.add(createStatCard("Currently Borrowed", borrowedBooksLabel, UITheme.WARNING, "In circulation"));
        statsGrid.add(createStatCard("Total Registered Users", totalUsersLabel, new Color(99, 102, 241), "Active & alumni users"));
        statsGrid.add(createStatCard("Total Completed Returns", returnedTxnsLabel, new Color(20, 184, 166), "Archived transactions"));
        statsGrid.add(createStatCard("Users Exceeding Limit (>3)", limitExceededLabel, UITheme.DANGER, "Requires attention"));

        centerContent.add(statsGrid);
        centerContent.add(Box.createVerticalStrut(16));

        // 2. Warning alert container for Requirement 8
        warningContainer = new JPanel();
        warningContainer.setLayout(new BoxLayout(warningContainer, BoxLayout.Y_AXIS));
        warningContainer.setOpaque(false);
        centerContent.add(warningContainer);
        centerContent.add(Box.createVerticalStrut(16));

        // 3. Quick activity panel: Recent active borrowings
        JPanel recentPanel = UITheme.createCardPanel();
        recentPanel.setLayout(new BorderLayout(0, 12));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);

        JLabel recentTitle = new JLabel("Currently Active Borrowings");
        recentTitle.setFont(UITheme.FONT_SECTION);
        recentTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton viewAllBtn = UITheme.createSecondaryButton("View All Borrowings");
        viewAllBtn.addActionListener(e -> mainFrame.navigateTo(MainFrame.NAV_BORROWINGS));

        recentHeader.add(recentTitle, BorderLayout.WEST);
        recentHeader.add(viewAllBtn, BorderLayout.EAST);
        recentPanel.add(recentHeader, BorderLayout.NORTH);

        String[] cols = {"Transaction ID", "User Name", "Registration No.", "Book Title", "Borrow Date", "Status"};
        recentBorrowingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        recentBorrowingsTable = new JTable(recentBorrowingsModel);
        UITheme.styleTable(recentBorrowingsTable);
        recentBorrowingsTable.getColumnModel().getColumn(5).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(recentBorrowingsTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        scrollPane.setPreferredSize(new Dimension(800, 220));
        recentPanel.add(scrollPane, BorderLayout.CENTER);

        centerContent.add(recentPanel);

        JScrollPane mainScrollPane = new JScrollPane(centerContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.setOpaque(false);
        mainScrollPane.getViewport().setOpaque(false);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String subtitle) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

        // Accent top bar
        JPanel topBar = new JPanel();
        topBar.setBackground(accentColor);
        topBar.setPreferredSize(new Dimension(card.getWidth(), 3));
        card.add(topBar, BorderLayout.NORTH);

        JPanel inner = new JPanel(new GridLayout(3, 1, 0, 2));
        inner.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UITheme.FONT_STAT_LBL);
        titleLbl.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_STAT_NUM);
        valueLabel.setForeground(accentColor);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UITheme.FONT_SMALL);
        subLbl.setForeground(UITheme.TEXT_MUTED);

        inner.add(titleLbl);
        inner.add(valueLabel);
        inner.add(subLbl);

        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes all metrics, warnings, and table rows on the dashboard.
     */
    public void refreshData() {
        totalBooksLabel.setText(String.valueOf(libraryService.getTotalBooksCount()));
        availableBooksLabel.setText(String.valueOf(libraryService.getAvailableBooksCount()));
        borrowedBooksLabel.setText(String.valueOf(libraryService.getBorrowedBooksCount()));
        totalUsersLabel.setText(String.valueOf(libraryService.getTotalUsersCount()));
        returnedTxnsLabel.setText(String.valueOf(libraryService.getTotalReturnedTransactionsCount()));

        List<UserViolationInfo> violations = libraryService.getUsersExceedingLimit();
        limitExceededLabel.setText(String.valueOf(violations.size()));

        updateWarningBanner(violations);
        updateRecentTable();
    }

    private void updateWarningBanner(List<UserViolationInfo> violations) {
        warningContainer.removeAll();

        if (!violations.isEmpty()) {
            JPanel banner = new JPanel(new BorderLayout(12, 10));
            banner.setBackground(UITheme.WARNING_LIGHT);
            banner.setBorder(new CompoundBorder(
                    new LineBorder(UITheme.WARNING_BORDER, 2, true),
                    new EmptyBorder(14, 16, 14, 16)
            ));

            // Banner Title
            JLabel alertTitle = new JLabel("⚠️ POLICY ALERT: " + violations.size() + 
                    " User" + (violations.size() == 1 ? " has" : "s have") + 
                    " exceeded the 3-book borrowing limit");
            alertTitle.setFont(UITheme.FONT_HEADER);
            alertTitle.setForeground(new Color(146, 64, 14)); // Deep amber
            banner.add(alertTitle, BorderLayout.NORTH);

            // Violation details list
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setOpaque(false);

            for (UserViolationInfo v : violations) {
                LibraryUser u = v.getUser();
                String name = u != null ? u.getFullName() : "Unknown User";
                String reg = u != null ? u.getRegistrationNumber() : "N/A";
                int count = v.getActiveBorrowingsCount();

                StringBuilder booksText = new StringBuilder();
                for (int i = 0; i < v.getBorrowedBooks().size(); i++) {
                    Book b = v.getBorrowedBooks().get(i);
                    booksText.append(b.getTitle()).append(" [").append(b.getBookId()).append("]");
                    if (i < v.getBorrowedBooks().size() - 1) booksText.append(", ");
                }

                JLabel warningLabel = new JLabel("• Warning: User '" + name + "' (" + reg + 
                        ") has " + count + " active borrowings, exceeding the allowed limit of 3.");
                warningLabel.setFont(UITheme.FONT_BOLD);
                warningLabel.setForeground(new Color(180, 83, 9));

                JLabel booksLabel = new JLabel("   Currently holding (" + count + " books): " + booksText);
                booksLabel.setFont(UITheme.FONT_REGULAR);
                booksLabel.setForeground(UITheme.TEXT_PRIMARY);

                detailsPanel.add(warningLabel);
                detailsPanel.add(booksLabel);
                detailsPanel.add(Box.createVerticalStrut(6));
            }

            JLabel footerNote = new JLabel("ℹ Note: Historical records are strictly preserved without automatic deletion. " +
                    "Additional borrowing is locked for these users until active borrowings fall below 3. Return operations are permitted.");
            footerNote.setFont(UITheme.FONT_SMALL);
            footerNote.setForeground(new Color(120, 53, 15));
            detailsPanel.add(footerNote);

            banner.add(detailsPanel, BorderLayout.CENTER);

            // Quick button to go to returns
            JButton goToReturnsBtn = UITheme.createPrimaryButton("Process Returns for Exceeding Users");
            goToReturnsBtn.addActionListener(e -> mainFrame.navigateTo(MainFrame.NAV_RETURNS));
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actionPanel.setOpaque(false);
            actionPanel.add(goToReturnsBtn);
            banner.add(actionPanel, BorderLayout.SOUTH);

            warningContainer.add(banner);
        } else {
            // Calm compliant banner
            JPanel goodBanner = new JPanel(new BorderLayout(8, 0));
            goodBanner.setBackground(UITheme.SUCCESS_LIGHT);
            goodBanner.setBorder(new CompoundBorder(
                    new LineBorder(UITheme.SUCCESS, 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            JLabel goodLabel = new JLabel("✓ Policy Compliance: All active users are currently within the 3-book borrowing limit.");
            goodLabel.setFont(UITheme.FONT_BOLD);
            goodLabel.setForeground(UITheme.SUCCESS);
            goodBanner.add(goodLabel, BorderLayout.CENTER);
            warningContainer.add(goodBanner);
        }

        warningContainer.revalidate();
        warningContainer.repaint();
    }

    private void updateRecentTable() {
        recentBorrowingsModel.setRowCount(0);
        List<BorrowingRecord> active = libraryService.getActiveBorrowings();

        for (BorrowingRecord r : active) {
            LibraryUser user = libraryService.getUserById(r.getUserId());
            Book book = libraryService.getBookById(r.getBookId());

            recentBorrowingsModel.addRow(new Object[]{
                    r.getTransactionId(),
                    user != null ? user.getFullName() : r.getUserId(),
                    user != null ? user.getRegistrationNumber() : "N/A",
                    book != null ? book.getTitle() : r.getBookId(),
                    r.getBorrowDate(),
                    "Active Borrowing"
            });
        }
    }

    private void onResetDemoData() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Reset all data back to original Hackathon demonstration state?\n\n" +
                "• All catalog books, users, and borrowing transactions will be re-initialized.\n" +
                "• Student Alex Chen will be reset to 4 active borrowings for Requirement 8 testing.\n\n" +
                "Do you want to proceed?",
                "Reset Demonstration Data", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                libraryService.resetToSampleData();
                JOptionPane.showMessageDialog(this,
                        "Sample demonstration data has been re-initialized successfully!",
                        "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.notifyDataChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error resetting data: " + ex.getMessage(),
                        "Reset Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
