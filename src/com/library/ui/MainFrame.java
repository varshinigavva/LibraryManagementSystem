package com.library.ui;

import com.library.model.LibraryUser;
import com.library.model.UserRole;
import com.library.service.LibraryService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window featuring role-based sidebar navigation,
 * top branding banner, and synchronized CardLayout content pane.
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public static final String NAV_DASHBOARD = "Dashboard";
    public static final String NAV_BOOKS = "Books";
    public static final String NAV_USERS = "Users";
    public static final String NAV_BORROWINGS = "Borrowings";
    public static final String NAV_RETURNS = "Returns";
    public static final String NAV_REPORTS = "Reports";

    private final LibraryService libraryService;
    private final LibraryUser currentUser;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);

    private final Map<String, JButton> navButtons = new HashMap<>();
    private String currentNav = NAV_DASHBOARD;

    // Panels
    private DashboardPanel dashboardPanel;
    private StudentDashboardPanel studentDashboardPanel;
    private BooksPanel booksPanel;
    private UsersPanel usersPanel;
    private BorrowingsPanel borrowingsPanel;
    private ReturnsPanel returnsPanel;
    private ReportsPanel reportsPanel;

    public MainFrame(LibraryService libraryService) {
        this(libraryService, createDefaultLibrarianUser());
    }

    public MainFrame(LibraryService libraryService, LibraryUser currentUser) {
        super("College Library Management System — " + 
                (currentUser != null && currentUser.isLibrarian() ? "Librarian Portal" : "Student Portal"));
        this.libraryService = libraryService;
        this.currentUser = currentUser != null ? currentUser : createDefaultLibrarianUser();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 800);
        setMinimumSize(new Dimension(1050, 680));
        setLocationRelativeTo(null);

        initUI();
    }

    private static LibraryUser createDefaultLibrarianUser() {
        return new LibraryUser("LIB-1001", "Chief Librarian", "STAFF-001", 
                "Library Administration", "librarian@campus.edu", true, 
                UserRole.LIBRARIAN, "admin", "admin123");
    }

    public LibraryUser getCurrentUser() {
        return currentUser;
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_MAIN);

        // 1. Sidebar Navigation
        JPanel sidebar = createSidebar();
        rootPanel.add(sidebar, BorderLayout.WEST);

        // 2. Right Container (Top Bar + Card Content)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setBackground(UITheme.BG_MAIN);

        JPanel topHeader = createTopHeader();
        rightContainer.add(topHeader, BorderLayout.NORTH);

        // Initialize Card Panels according to role
        booksPanel = new BooksPanel(libraryService, this);
        borrowingsPanel = new BorrowingsPanel(libraryService, this);

        contentCards.setBackground(UITheme.BG_MAIN);

        if (currentUser.isLibrarian()) {
            dashboardPanel = new DashboardPanel(libraryService, this);
            usersPanel = new UsersPanel(libraryService, this);
            returnsPanel = new ReturnsPanel(libraryService, this);
            reportsPanel = new ReportsPanel(libraryService, this);

            contentCards.add(dashboardPanel, NAV_DASHBOARD);
            contentCards.add(booksPanel, NAV_BOOKS);
            contentCards.add(usersPanel, NAV_USERS);
            contentCards.add(borrowingsPanel, NAV_BORROWINGS);
            contentCards.add(returnsPanel, NAV_RETURNS);
            contentCards.add(reportsPanel, NAV_REPORTS);
        } else {
            // Student role: restricted navigation
            studentDashboardPanel = new StudentDashboardPanel(libraryService, currentUser, this);
            contentCards.add(studentDashboardPanel, NAV_DASHBOARD);
            contentCards.add(booksPanel, NAV_BOOKS);
            contentCards.add(borrowingsPanel, NAV_BORROWINGS);
        }

        rightContainer.add(contentCards, BorderLayout.CENTER);
        rootPanel.add(rightContainer, BorderLayout.CENTER);

        setContentPane(rootPanel);
        highlightNav(NAV_DASHBOARD);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 800));

        // Brand / Logo Top
        JPanel brandPanel = new JPanel(new BorderLayout());
        brandPanel.setOpaque(false);
        brandPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, new Color(51, 65, 85)),
                new EmptyBorder(24, 20, 24, 20)
        ));

        JLabel logoLabel = new JLabel("CAMPUS LIB");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);

        JLabel tagLabel = new JLabel(currentUser.isLibrarian() ? "Staff Administration" : "Student Member Portal");
        tagLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tagLabel.setForeground(new Color(148, 163, 184));

        brandPanel.add(logoLabel, BorderLayout.NORTH);
        brandPanel.add(tagLabel, BorderLayout.SOUTH);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Nav Button List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(new EmptyBorder(16, 12, 16, 12));

        String[] navItems;
        if (currentUser.isLibrarian()) {
            navItems = new String[]{
                    NAV_DASHBOARD,
                    NAV_BOOKS,
                    NAV_USERS,
                    NAV_BORROWINGS,
                    NAV_RETURNS,
                    NAV_REPORTS
            };
        } else {
            // Student sees only their relevant options
            navItems = new String[]{
                    NAV_DASHBOARD,
                    NAV_BOOKS,
                    NAV_BORROWINGS
            };
        }

        for (String nav : navItems) {
            String label = nav;
            if (currentUser.isStudent()) {
                if (NAV_DASHBOARD.equals(nav)) label = "My Dashboard";
                else if (NAV_BOOKS.equals(nav)) label = "Browse Catalog";
                else if (NAV_BORROWINGS.equals(nav)) label = "My Loans History";
            }

            JButton btn = createNavButton(nav, label);
            navButtons.put(nav, btn);
            navList.add(btn);
            navList.add(Box.createVerticalStrut(6));
        }

        sidebar.add(navList, BorderLayout.CENTER);

        // Sidebar Footer / Logout
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        footer.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(51, 65, 85)),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JButton logoutBtn = UITheme.createSecondaryButton("⏻ Sign Out / Switch Role");
        logoutBtn.addActionListener(e -> onLogout());
        footer.add(logoutBtn, BorderLayout.CENTER);

        JLabel roleFoot = new JLabel("Logged as: " + currentUser.getRole().getDisplayName());
        roleFoot.setFont(UITheme.FONT_SMALL);
        roleFoot.setForeground(new Color(148, 163, 184));
        roleFoot.setHorizontalAlignment(SwingConstants.CENTER);
        footer.add(roleFoot, BorderLayout.SOUTH);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private JButton createNavButton(String navKey, String displayText) {
        JButton btn = new JButton(displayText);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UITheme.SIDEBAR_BG);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 42));
        btn.setPreferredSize(new Dimension(216, 42));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> navigateTo(navKey));
        return btn;
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(12, 24, 12, 24)
        ));

        JLabel collegeTitle = new JLabel("College Library Management System");
        collegeTitle.setFont(UITheme.FONT_HEADER);
        collegeTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel rightInfo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightInfo.setOpaque(false);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy"));
        JLabel dateBadge = new JLabel("📅 " + today);
        dateBadge.setFont(UITheme.FONT_REGULAR);
        dateBadge.setForeground(UITheme.TEXT_MUTED);

        JLabel userBadge = new JLabel((currentUser.isLibrarian() ? "👤 " : "🎓 ") + 
                currentUser.getFullName() + " (" + currentUser.getRole().getDisplayName() + ")");
        userBadge.setFont(UITheme.FONT_BOLD);
        userBadge.setForeground(currentUser.isLibrarian() ? UITheme.PRIMARY : new Color(5, 150, 105));
        userBadge.setBorder(new CompoundBorder(
                new LineBorder(currentUser.isLibrarian() ? UITheme.PRIMARY : new Color(5, 150, 105), 1, true),
                new EmptyBorder(3, 8, 3, 8)
        ));

        rightInfo.add(dateBadge);
        rightInfo.add(userBadge);

        header.add(collegeTitle, BorderLayout.WEST);
        header.add(rightInfo, BorderLayout.EAST);
        return header;
    }

    public void navigateTo(String navName) {
        this.currentNav = navName;
        cardLayout.show(contentCards, navName);
        highlightNav(navName);

        // Refresh panel when navigated to
        if (NAV_DASHBOARD.equals(navName)) {
            if (currentUser.isLibrarian() && dashboardPanel != null) dashboardPanel.refreshData();
            else if (studentDashboardPanel != null) studentDashboardPanel.refreshData();
        }
        else if (NAV_BOOKS.equals(navName) && booksPanel != null) booksPanel.refreshTable();
        else if (NAV_USERS.equals(navName) && usersPanel != null) usersPanel.refreshTable();
        else if (NAV_BORROWINGS.equals(navName) && borrowingsPanel != null) borrowingsPanel.refreshAll();
        else if (NAV_RETURNS.equals(navName) && returnsPanel != null) returnsPanel.refreshTable();
        else if (NAV_REPORTS.equals(navName) && reportsPanel != null) reportsPanel.refreshReports();
    }

    private void highlightNav(String selectedNav) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton btn = entry.getValue();
            if (entry.getKey().equals(selectedNav)) {
                btn.setBackground(UITheme.SIDEBAR_ACTIVE);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UITheme.SIDEBAR_BG);
                btn.setForeground(new Color(203, 213, 225));
            }
        }
    }

    private void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to sign out?",
                "Confirm Sign Out", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame(libraryService).setVisible(true));
        }
    }

    /**
     * Broadcasts a data change notification to refresh all panel contents
     * immediately in accordance with Requirement 6, 9, and 11.
     */
    public void notifyDataChanged() {
        SwingUtilities.invokeLater(() -> {
            if (dashboardPanel != null) dashboardPanel.refreshData();
            if (studentDashboardPanel != null) studentDashboardPanel.refreshData();
            if (booksPanel != null) booksPanel.refreshTable();
            if (usersPanel != null) usersPanel.refreshTable();
            if (borrowingsPanel != null) borrowingsPanel.refreshAll();
            if (returnsPanel != null) returnsPanel.refreshTable();
            if (reportsPanel != null) reportsPanel.refreshReports();
        });
    }
}
