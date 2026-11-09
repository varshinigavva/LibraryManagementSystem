package com.library.ui;

import com.library.model.Book;
import com.library.model.BorrowingRecord;
import com.library.model.LibraryUser;
import com.library.service.LibraryService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Personalized dashboard for students displaying their current active loans,
 * borrowing limit status, held books table, and return alerts.
 */
@SuppressWarnings("serial")
public class StudentDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final LibraryService libraryService;
    private final LibraryUser studentUser;
    private final MainFrame mainFrame;

    private JLabel activeLoansLabel;
    private JLabel limitLabel;
    private JLabel totalAvailLabel;
    private JLabel myReturnsLabel;
    private JPanel warningContainer;

    private JTable heldBooksTable;
    private DefaultTableModel heldBooksModel;

    public StudentDashboardPanel(LibraryService libraryService, LibraryUser studentUser, MainFrame mainFrame) {
        this.libraryService = libraryService;
        this.studentUser = studentUser;
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

        JLabel welcomeLabel = new JLabel("Welcome back, " + studentUser.getFullName());
        welcomeLabel.setFont(UITheme.FONT_TITLE);
        welcomeLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Student Portal — " + studentUser.getRegistrationNumber() + " (" + studentUser.getDepartment() + ")");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);
        titleBlock.add(welcomeLabel);
        titleBlock.add(subLabel);

        JButton browseBtn = UITheme.createPrimaryButton("Browse Library Catalog");
        browseBtn.addActionListener(e -> mainFrame.navigateTo(MainFrame.NAV_BOOKS));

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(browseBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerContent = new JPanel();
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
        centerContent.setOpaque(false);

        // 1. KPI Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 14, 14));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(3000, 110));

        activeLoansLabel = new JLabel("0");
        limitLabel = new JLabel("3 Books");
        totalAvailLabel = new JLabel("0");
        myReturnsLabel = new JLabel("0");

        statsGrid.add(createStatCard("My Active Borrowings", activeLoansLabel, UITheme.PRIMARY, "Currently checked out"));
        statsGrid.add(createStatCard("Borrowing Limit", limitLabel, new Color(99, 102, 241), "College standard policy"));
        statsGrid.add(createStatCard("Catalog Available", totalAvailLabel, UITheme.SUCCESS, "Ready to borrow"));
        statsGrid.add(createStatCard("My Past Returns", myReturnsLabel, new Color(20, 184, 166), "Returned history"));

        centerContent.add(statsGrid);
        centerContent.add(Box.createVerticalStrut(16));

        // 2. Warning Container (Requirement 8)
        warningContainer = new JPanel();
        warningContainer.setLayout(new BoxLayout(warningContainer, BoxLayout.Y_AXIS));
        warningContainer.setOpaque(false);
        centerContent.add(warningContainer);
        centerContent.add(Box.createVerticalStrut(16));

        // 3. Held Books Table
        JPanel heldPanel = UITheme.createCardPanel();
        heldPanel.setLayout(new BorderLayout(0, 12));

        JLabel tableTitle = new JLabel("Books Currently in Your Possession");
        tableTitle.setFont(UITheme.FONT_SECTION);
        tableTitle.setForeground(UITheme.TEXT_PRIMARY);
        heldPanel.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Txn ID", "Book ID", "Title", "Author", "Category", "Borrow Date", "Days Held", "Status"};
        heldBooksModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        heldBooksTable = new JTable(heldBooksModel);
        UITheme.styleTable(heldBooksTable);
        heldBooksTable.getColumnModel().getColumn(7).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(heldBooksTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        heldPanel.add(scrollPane, BorderLayout.CENTER);

        centerContent.add(heldPanel);

        JScrollPane mainScrollPane = new JScrollPane(centerContent);
        mainScrollPane.setBorder(null);
        mainScrollPane.setOpaque(false);
        mainScrollPane.getViewport().setOpaque(false);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor, String subtitle) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout(0, 6));

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

    public void refreshData() {
        int activeCount = libraryService.getActiveBorrowingCount(studentUser.getUserId());
        activeLoansLabel.setText(String.valueOf(activeCount));
        totalAvailLabel.setText(String.valueOf(libraryService.getAvailableBooksCount()));

        int returnsCount = 0;
        for (BorrowingRecord r : libraryService.getAllBorrowings()) {
            if (!r.isActive() && r.getUserId().equalsIgnoreCase(studentUser.getUserId())) {
                returnsCount++;
            }
        }
        myReturnsLabel.setText(String.valueOf(returnsCount));

        // Update Warning
        warningContainer.removeAll();
        if (activeCount > 3) {
            JPanel banner = new JPanel(new BorderLayout(10, 8));
            banner.setBackground(UITheme.WARNING_LIGHT);
            banner.setBorder(new CompoundBorder(
                    new LineBorder(UITheme.WARNING_BORDER, 2, true),
                    new EmptyBorder(12, 16, 12, 16)
            ));

            JLabel title = new JLabel("⚠️ POLICY NOTICE: You have " + activeCount + " active borrowings, exceeding the 3-book limit");
            title.setFont(UITheme.FONT_HEADER);
            title.setForeground(new Color(146, 64, 14));

            JLabel text = new JLabel("<html>You are currently holding " + activeCount + 
                    " books. According to college policy, you cannot borrow new books until you return outstanding books " +
                    "to the circulation counter. Please visit the library desk to process returns.</html>");
            text.setFont(UITheme.FONT_REGULAR);
            text.setForeground(UITheme.TEXT_PRIMARY);

            banner.add(title, BorderLayout.NORTH);
            banner.add(text, BorderLayout.CENTER);
            warningContainer.add(banner);
        } else if (activeCount == 3) {
            JPanel banner = new JPanel(new BorderLayout(8, 0));
            banner.setBackground(UITheme.WARNING_LIGHT);
            banner.setBorder(new CompoundBorder(
                    new LineBorder(UITheme.WARNING_BORDER, 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            JLabel lbl = new JLabel("ℹ You have reached your borrowing limit (3/3 active books). Please return a book before borrowing another.");
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.WARNING);
            banner.add(lbl, BorderLayout.CENTER);
            warningContainer.add(banner);
        } else {
            JPanel banner = new JPanel(new BorderLayout(8, 0));
            banner.setBackground(UITheme.SUCCESS_LIGHT);
            banner.setBorder(new CompoundBorder(
                    new LineBorder(UITheme.SUCCESS, 1, true),
                    new EmptyBorder(10, 14, 10, 14)
            ));
            JLabel lbl = new JLabel("✓ Account in Good Standing: You can borrow " + (3 - activeCount) + " more book" + (3 - activeCount == 1 ? "" : "s") + ".");
            lbl.setFont(UITheme.FONT_BOLD);
            lbl.setForeground(UITheme.SUCCESS);
            banner.add(lbl, BorderLayout.CENTER);
            warningContainer.add(banner);
        }

        // Update held books table
        heldBooksModel.setRowCount(0);
        List<BorrowingRecord> activeRecords = libraryService.getActiveBorrowingsForUser(studentUser.getUserId());
        for (BorrowingRecord r : activeRecords) {
            Book b = libraryService.getBookById(r.getBookId());
            long daysHeld = ChronoUnit.DAYS.between(r.getBorrowDate(), LocalDate.now());

            heldBooksModel.addRow(new Object[]{
                    r.getTransactionId(),
                    r.getBookId(),
                    b != null ? b.getTitle() : "N/A",
                    b != null ? b.getAuthor() : "N/A",
                    b != null ? b.getCategory() : "N/A",
                    r.getBorrowDate(),
                    daysHeld + " day(s)",
                    "Active Borrowing"
            });
        }

        warningContainer.revalidate();
        warningContainer.repaint();
    }
}

