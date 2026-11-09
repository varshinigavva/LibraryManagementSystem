package com.library.ui;

import com.library.model.LibraryUser;
import com.library.service.LibraryService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern login window providing role-based authentication for Librarians and Students,
 * with quick-login buttons for smooth hackathon demonstrations.
 */
@SuppressWarnings("serial")
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final LibraryService libraryService;
    private final JTextField usernameField = UITheme.createTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JRadioButton librarianRadio = new JRadioButton("Librarian (Staff)", true);
    private final JRadioButton studentRadio = new JRadioButton("Student Member", false);

    public LoginFrame(LibraryService libraryService) {
        super("Sign In — College Library Management System");
        this.libraryService = libraryService;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(520, 620);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG_MAIN);
        root.setBorder(new EmptyBorder(24, 32, 24, 32));

        // 1. Header & Branding
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandBadge = new JLabel("CAMPUS LIBRARY SYSTEM");
        brandBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        brandBadge.setForeground(UITheme.PRIMARY);
        brandBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in with your role credentials to access your portal");
        subtitleLabel.setFont(UITheme.FONT_SUBTITLE);
        subtitleLabel.setForeground(UITheme.TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(brandBadge);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(4));
        headerPanel.add(subtitleLabel);
        headerPanel.add(Box.createVerticalStrut(20));

        root.add(headerPanel, BorderLayout.NORTH);

        // 2. Form Card
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        // Role selector
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        rolePanel.setOpaque(false);
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(librarianRadio);
        roleGroup.add(studentRadio);

        librarianRadio.setFont(UITheme.FONT_BOLD);
        librarianRadio.setBackground(Color.WHITE);
        studentRadio.setFont(UITheme.FONT_BOLD);
        studentRadio.setBackground(Color.WHITE);

        librarianRadio.addActionListener(e -> updatePlaceholder(true));
        studentRadio.addActionListener(e -> updatePlaceholder(false));

        rolePanel.add(librarianRadio);
        rolePanel.add(studentRadio);

        formCard.add(rolePanel);
        formCard.add(Box.createVerticalStrut(16));

        // Username / Reg No
        JLabel userLbl = new JLabel("Username / Student Registration No.:");
        userLbl.setFont(UITheme.FONT_BOLD);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        usernameField.setText("admin");

        formCard.add(userLbl);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(usernameField);
        formCard.add(Box.createVerticalStrut(14));

        // Password
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(UITheme.FONT_BOLD);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField.setFont(UITheme.FONT_REGULAR);
        passwordField.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passwordField.setText("admin123");

        formCard.add(passLbl);
        formCard.add(Box.createVerticalStrut(6));
        formCard.add(passwordField);
        formCard.add(Box.createVerticalStrut(20));

        // Sign In Button
        JButton signInBtn = UITheme.createPrimaryButton("Sign In to Library System");
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signInBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        signInBtn.setPreferredSize(new Dimension(400, 40));
        signInBtn.addActionListener(e -> onLogin());

        // Press Enter to submit
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        formCard.add(signInBtn);
        formCard.add(Box.createVerticalStrut(22));

        // 3. Quick Demo 1-Click Login Card (Judge Convenience)
        JPanel quickPanel = new JPanel();
        quickPanel.setLayout(new BoxLayout(quickPanel, BoxLayout.Y_AXIS));
        quickPanel.setOpaque(false);
        quickPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));
        quickPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel quickTitle = new JLabel("Quick Demo Sign-In (For Hackathon Judges):");
        quickTitle.setFont(UITheme.FONT_SMALL);
        quickTitle.setForeground(UITheme.TEXT_MUTED);
        quickTitle.setBorder(new EmptyBorder(12, 0, 8, 0));

        JButton quickAdminBtn = UITheme.createSecondaryButton("👑 Quick Login: Chief Librarian (Staff)");
        quickAdminBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        quickAdminBtn.addActionListener(e -> {
            librarianRadio.setSelected(true);
            usernameField.setText("admin");
            passwordField.setText("admin123");
            onLogin();
        });

        JButton quickAlexBtn = UITheme.createSecondaryButton("🎓 Quick Login: Alex Chen (Student — 4 Books Held)");
        quickAlexBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        quickAlexBtn.addActionListener(e -> {
            studentRadio.setSelected(true);
            usernameField.setText("REG-2023-CS-001");
            passwordField.setText("student123");
            onLogin();
        });

        JButton quickMarcusBtn = UITheme.createSecondaryButton("🎓 Quick Login: Marcus Johnson (Student — 2 Books Held)");
        quickMarcusBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        quickMarcusBtn.addActionListener(e -> {
            studentRadio.setSelected(true);
            usernameField.setText("REG-2023-EE-012");
            passwordField.setText("student123");
            onLogin();
        });

        quickPanel.add(quickTitle);
        quickPanel.add(quickAdminBtn);
        quickPanel.add(Box.createVerticalStrut(6));
        quickPanel.add(quickAlexBtn);
        quickPanel.add(Box.createVerticalStrut(6));
        quickPanel.add(quickMarcusBtn);

        formCard.add(quickPanel);
        root.add(formCard, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void updatePlaceholder(boolean isLibrarian) {
        if (isLibrarian) {
            usernameField.setText("admin");
            passwordField.setText("admin123");
        } else {
            usernameField.setText("REG-2023-CS-001");
            passwordField.setText("student123");
        }
    }

    private void onLogin() {
        String identifier = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        try {
            LibraryUser user = libraryService.authenticate(identifier, password);

            // Open main frame with user session
            MainFrame mainFrame = new MainFrame(libraryService, user);
            mainFrame.setVisible(true);

            dispose(); // Close login window

        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, ve.getMessage(), "Authentication Failed", JOptionPane.WARNING_MESSAGE);
            passwordField.requestFocus();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "System error during login: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

