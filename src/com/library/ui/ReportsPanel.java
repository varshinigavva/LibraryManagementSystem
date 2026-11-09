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
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Comprehensive Reports Panel providing aggregated metrics,
 * category distribution analysis, active loan rosters, and audit report generation.
 */
@SuppressWarnings("serial")
public class ReportsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryService libraryService;
    private final MainFrame mainFrame;

    // Report Summary Labels
    private JLabel totalBooksVal;
    private JLabel availableBooksVal;
    private JLabel borrowedBooksVal;
    private JLabel totalUsersVal;
    private JLabel activeUsersVal;
    private JLabel completedReturnsVal;
    private JLabel activeLoansVal;
    private JLabel limitViolationsVal;

    // Tables
    private JTable categoryTable;
    private DefaultTableModel categoryTableModel;
    private JTable violationsTable;
    private DefaultTableModel violationsTableModel;

    public ReportsPanel(LibraryService libraryService, MainFrame mainFrame) {
        this.libraryService = libraryService;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        initComponents();
        refreshReports();
    }

    private void initComponents() {
        // Top Header Row
        JPanel headerCard = UITheme.createCardPanel();
        headerCard.setLayout(new BorderLayout());

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        JLabel titleLabel = new JLabel("College Library Analytics & Circulation Reports");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Official statistics, category breakdown, and policy compliance verification");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);

        JButton exportBtn = UITheme.createPrimaryButton("Generate Audit Report");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        exportBtn.addActionListener(e -> onGenerateAuditReport());
        refreshBtn.addActionListener(e -> refreshReports());

        btnRow.add(exportBtn);
        btnRow.add(refreshBtn);

        headerCard.add(titleBlock, BorderLayout.WEST);
        headerCard.add(btnRow, BorderLayout.EAST);
        add(headerCard, BorderLayout.NORTH);

        // Center Content with ScrollPane
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        // 1. KPI Metric Grid
        JPanel kpiCard = UITheme.createCardPanel();
        kpiCard.setLayout(new BorderLayout(0, 10));

        JLabel kpiTitle = new JLabel("Key Operational Metrics");
        kpiTitle.setFont(UITheme.FONT_SECTION);
        kpiCard.add(kpiTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 4, 12, 12));
        grid.setOpaque(false);

        totalBooksVal = new JLabel("0");
        availableBooksVal = new JLabel("0");
        borrowedBooksVal = new JLabel("0");
        totalUsersVal = new JLabel("0");
        activeUsersVal = new JLabel("0");
        completedReturnsVal = new JLabel("0");
        activeLoansVal = new JLabel("0");
        limitViolationsVal = new JLabel("0");

        grid.add(createMiniStat("Total Cataloged Books", totalBooksVal, UITheme.PRIMARY));
        grid.add(createMiniStat("Currently Available", availableBooksVal, UITheme.SUCCESS));
        grid.add(createMiniStat("Currently Borrowed", borrowedBooksVal, UITheme.WARNING));
        grid.add(createMiniStat("Total Registered Users", totalUsersVal, new Color(99, 102, 241)));
        grid.add(createMiniStat("Active Members", activeUsersVal, new Color(14, 165, 233)));
        grid.add(createMiniStat("Total Completed Returns", completedReturnsVal, new Color(20, 184, 166)));
        grid.add(createMiniStat("Active Circulation Loans", activeLoansVal, new Color(234, 88, 12)));
        grid.add(createMiniStat("Users Exceeding Limit (>3)", limitViolationsVal, UITheme.DANGER));

        kpiCard.add(grid, BorderLayout.CENTER);
        scrollContent.add(kpiCard);
        scrollContent.add(Box.createVerticalStrut(16));

        // 2. Policy Compliance Report (Requirement 8)
        JPanel violationCard = UITheme.createCardPanel();
        violationCard.setLayout(new BorderLayout(0, 10));

        JLabel violTitle = new JLabel("Borrowing Policy Compliance (>3 Active Loans)");
        violTitle.setFont(UITheme.FONT_SECTION);
        violTitle.setForeground(new Color(180, 83, 9));
        violationCard.add(violTitle, BorderLayout.NORTH);

        String[] vCols = {"User ID", "User Name", "Registration No.", "Department", "Active Loans", "Books Currently Held", "Policy Status"};
        violationsTableModel = new DefaultTableModel(vCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        violationsTable = new JTable(violationsTableModel);
        UITheme.styleTable(violationsTable);
        violationsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        violationsTable.getColumnModel().getColumn(5).setPreferredWidth(320);
        violationsTable.getColumnModel().getColumn(6).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane vScroll = new JScrollPane(violationsTable);
        vScroll.setBorder(new LineBorder(UITheme.BORDER, 1));
        vScroll.setPreferredSize(new Dimension(800, 130));
        violationCard.add(vScroll, BorderLayout.CENTER);

        scrollContent.add(violationCard);
        scrollContent.add(Box.createVerticalStrut(16));

        // 3. Category Breakdown Table
        JPanel catCard = UITheme.createCardPanel();
        catCard.setLayout(new BorderLayout(0, 10));

        JLabel catTitle = new JLabel("Category Distribution & Availability Breakdown");
        catTitle.setFont(UITheme.FONT_SECTION);
        catCard.add(catTitle, BorderLayout.NORTH);

        String[] cCols = {"Category Name", "Total Books", "Available", "Borrowed", "Circulation %"};
        categoryTableModel = new DefaultTableModel(cCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        categoryTable = new JTable(categoryTableModel);
        UITheme.styleTable(categoryTable);

        JScrollPane catScroll = new JScrollPane(categoryTable);
        catScroll.setBorder(new LineBorder(UITheme.BORDER, 1));
        catScroll.setPreferredSize(new Dimension(800, 180));
        catCard.add(catScroll, BorderLayout.CENTER);

        scrollContent.add(catCard);

        JScrollPane mainScrollPane = new JScrollPane(scrollContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.setOpaque(false);
        mainScrollPane.getViewport().setOpaque(false);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createMiniStat(String label, JLabel valueLbl, Color color) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setBackground(UITheme.ROW_ALT);
        p.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel title = new JLabel(label);
        title.setFont(UITheme.FONT_SMALL);
        title.setForeground(UITheme.TEXT_MUTED);

        valueLbl.setFont(UITheme.FONT_HEADER);
        valueLbl.setForeground(color);

        p.add(title);
        p.add(valueLbl);
        return p;
    }

    public void refreshReports() {
        totalBooksVal.setText(String.valueOf(libraryService.getTotalBooksCount()));
        availableBooksVal.setText(String.valueOf(libraryService.getAvailableBooksCount()));
        borrowedBooksVal.setText(String.valueOf(libraryService.getBorrowedBooksCount()));
        totalUsersVal.setText(String.valueOf(libraryService.getTotalUsersCount()));
        activeUsersVal.setText(String.valueOf(libraryService.getActiveUsersCount()));
        completedReturnsVal.setText(String.valueOf(libraryService.getTotalReturnedTransactionsCount()));
        activeLoansVal.setText(String.valueOf(libraryService.getTotalActiveBorrowingsCount()));

        List<UserViolationInfo> violations = libraryService.getUsersExceedingLimit();
        limitViolationsVal.setText(String.valueOf(violations.size()));

        // Populate violations table
        violationsTableModel.setRowCount(0);
        if (violations.isEmpty()) {
            violationsTableModel.addRow(new Object[]{
                    "—", "All active members are compliant", "—", "—", "—", "No limit violations detected", "Active"
            });
        } else {
            for (UserViolationInfo v : violations) {
                LibraryUser u = v.getUser();
                StringBuilder books = new StringBuilder();
                for (int i = 0; i < v.getBorrowedBooks().size(); i++) {
                    books.append(v.getBorrowedBooks().get(i).getTitle());
                    if (i < v.getBorrowedBooks().size() - 1) books.append("; ");
                }

                violationsTableModel.addRow(new Object[]{
                        u != null ? u.getUserId() : "N/A",
                        u != null ? u.getFullName() : "N/A",
                        u != null ? u.getRegistrationNumber() : "N/A",
                        u != null ? u.getDepartment() : "N/A",
                        v.getActiveBorrowingsCount(),
                        books.toString(),
                        "Limit Exceeded"
                });
            }
        }

        // Populate Category breakdown
        categoryTableModel.setRowCount(0);
        Map<String, int[]> catStats = new HashMap<>(); // [total, available, borrowed]
        for (Book b : libraryService.getAllBooks()) {
            String cat = b.getCategory();
            int[] stats = catStats.computeIfAbsent(cat, k -> new int[3]);
            stats[0]++;
            if (b.isAvailable()) stats[1]++;
            else stats[2]++;
        }

        for (Map.Entry<String, int[]> entry : catStats.entrySet()) {
            int total = entry.getValue()[0];
            int avail = entry.getValue()[1];
            int borrowed = entry.getValue()[2];
            int pct = total > 0 ? (borrowed * 100) / total : 0;

            categoryTableModel.addRow(new Object[]{
                    entry.getKey(),
                    total,
                    avail,
                    borrowed,
                    pct + "%"
            });
        }
    }

    private void onGenerateAuditReport() {
        StringBuilder sb = new StringBuilder();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));

        sb.append("========================================================================================\n");
        sb.append("                   COLLEGE LIBRARY MANAGEMENT SYSTEM - AUDIT REPORT                     \n");
        sb.append("========================================================================================\n");
        sb.append("Generated On: ").append(dateStr).append("\n\n");

        sb.append("1. EXECUTIVE INVENTORY SUMMARY\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-32s : %d\n", "Total Cataloged Books", libraryService.getTotalBooksCount()));
        sb.append(String.format("%-32s : %d\n", "Available Copies", libraryService.getAvailableBooksCount()));
        sb.append(String.format("%-32s : %d\n", "Currently Borrowed", libraryService.getBorrowedBooksCount()));
        sb.append(String.format("%-32s : %d\n", "Total Registered Users", libraryService.getTotalUsersCount()));
        sb.append(String.format("%-32s : %d\n", "Active Users", libraryService.getActiveUsersCount()));
        sb.append(String.format("%-32s : %d\n", "Total Completed Returns", libraryService.getTotalReturnedTransactionsCount()));
        sb.append(String.format("%-32s : %d\n", "Active Circulation Loans", libraryService.getTotalActiveBorrowingsCount()));
        sb.append(String.format("%-32s : %d\n", "Users Exceeding 3-Book Limit", libraryService.getUsersExceedingLimitCount()));
        sb.append("\n");

        sb.append("2. BORROWING LIMIT POLICY AUDIT (>3 Active Borrowings)\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        List<UserViolationInfo> violations = libraryService.getUsersExceedingLimit();
        if (violations.isEmpty()) {
            sb.append("No active policy violations. All users have 3 or fewer active loans.\n");
        } else {
            for (UserViolationInfo v : violations) {
                LibraryUser u = v.getUser();
                sb.append("• USER: ").append(u.getFullName()).append(" (ID: ").append(u.getUserId())
                        .append(", Reg: ").append(u.getRegistrationNumber()).append(", Dept: ").append(u.getDepartment()).append(")\n");
                sb.append("  Active Loans: ").append(v.getActiveBorrowingsCount()).append(" [EXCEEDS ALLOWED LIMIT OF 3]\n");
                sb.append("  Books Held:\n");
                for (Book b : v.getBorrowedBooks()) {
                    sb.append("    - ").append(b.getTitle()).append(" (ID: ").append(b.getBookId()).append(", Category: ").append(b.getCategory()).append(")\n");
                }
                sb.append("\n");
            }
        }

        sb.append("3. ACTIVE CIRCULATION ROSTER\n");
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-12s %-20s %-16s %-30s %-12s\n", "Txn ID", "User Name", "Reg No.", "Book Title", "Borrow Date"));
        sb.append("----------------------------------------------------------------------------------------\n");
        for (BorrowingRecord r : libraryService.getActiveBorrowings()) {
            LibraryUser u = libraryService.getUserById(r.getUserId());
            Book b = libraryService.getBookById(r.getBookId());
            String uName = u != null ? u.getFullName() : r.getUserId();
            String reg = u != null ? u.getRegistrationNumber() : "N/A";
            String title = b != null ? b.getTitle() : r.getBookId();
            if (title.length() > 28) title = title.substring(0, 25) + "...";
            if (uName.length() > 18) uName = uName.substring(0, 16) + "..";

            sb.append(String.format("%-12s %-20s %-16s %-30s %-12s\n",
                    r.getTransactionId(), uName, reg, title, r.getBorrowDate()));
        }
        sb.append("========================================================================================\n");
        sb.append("                                 END OF AUDIT REPORT                                    \n");
        sb.append("========================================================================================\n");

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Library Audit Report Preview", true);
        dialog.setSize(750, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(textArea);
        dialog.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(UITheme.ROW_ALT);

        JButton copyBtn = UITheme.createSecondaryButton("Copy Report Text");
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
            JOptionPane.showMessageDialog(dialog, "Audit report copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton exportFileBtn = UITheme.createPrimaryButton("Export to File (.txt)");
        exportFileBtn.addActionListener(e -> {
            try {
                java.io.File reportsDir = new java.io.File("reports");
                if (!reportsDir.exists()) reportsDir.mkdirs();
                java.io.File outFile = new java.io.File(reportsDir, "library_audit_report_" + LocalDate.now() + ".txt");
                try (java.io.FileWriter writer = new java.io.FileWriter(outFile)) {
                    writer.write(sb.toString());
                }
                JOptionPane.showMessageDialog(dialog, 
                        "Audit report successfully exported to:\n" + outFile.getAbsolutePath(), 
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Failed to export report: " + ex.getMessage(), 
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = UITheme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(exportFileBtn);
        btnPanel.add(copyBtn);
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
