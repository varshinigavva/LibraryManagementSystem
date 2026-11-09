package com.library.ui;

import com.library.model.LibraryUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for registering a new library user or updating an existing user record.
 */
public class UserDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JTextField nameField = UITheme.createTextField(25);
    private final JTextField regNoField = UITheme.createTextField(25);
    private final JComboBox<String> deptCombo;
    private final JTextField customDeptField = UITheme.createTextField(15);
    private final JTextField contactField = UITheme.createTextField(25);
    private final JCheckBox activeCheckBox = new JCheckBox("Account is Active (permitted to borrow)");
    private boolean confirmed = false;

    private static final String[] DEFAULT_DEPTS = {
            "Computer Science", "Information Technology", "Electrical Engineering",
            "Electronics & Communication", "Mechanical Engineering", "Civil Engineering",
            "Mathematics", "Physics", "Humanities & Literature", "Business Administration",
            "Other (Custom)"
    };

    public UserDialog(Frame owner, String title, LibraryUser existingUser) {
        super(owner, title, true);
        setLayout(new BorderLayout());
        setResizable(false);

        deptCombo = UITheme.createComboBox(DEFAULT_DEPTS);
        activeCheckBox.setFont(UITheme.FONT_REGULAR);
        activeCheckBox.setSelected(true);
        activeCheckBox.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(24, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Header
        JLabel headerLabel = new JLabel(existingUser == null ? "Register New User" : "Edit User (" + existingUser.getUserId() + ")");
        headerLabel.setFont(UITheme.FONT_HEADER);
        headerLabel.setForeground(UITheme.PRIMARY);
        gbc.gridwidth = 2;
        contentPanel.add(headerLabel, gbc);

        gbc.gridwidth = 1;

        // Full Name
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l1 = new JLabel("Full Name *:");
        l1.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l1, gbc);

        gbc.gridx = 1;
        contentPanel.add(nameField, gbc);

        // Registration Number
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l2 = new JLabel("Registration No. *:");
        l2.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l2, gbc);

        gbc.gridx = 1;
        contentPanel.add(regNoField, gbc);

        // Department
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l3 = new JLabel("Department *:");
        l3.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l3, gbc);

        gbc.gridx = 1;
        JPanel deptPanel = new JPanel(new BorderLayout(5, 0));
        deptPanel.setBackground(Color.WHITE);
        deptPanel.add(deptCombo, BorderLayout.CENTER);
        deptPanel.add(customDeptField, BorderLayout.EAST);
        customDeptField.setVisible(false);
        contentPanel.add(deptPanel, gbc);

        deptCombo.addActionListener(e -> {
            boolean isOther = "Other (Custom)".equals(deptCombo.getSelectedItem());
            customDeptField.setVisible(isOther);
            pack();
        });

        // Contact Details
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l4 = new JLabel("Contact Details *:");
        l4.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l4, gbc);

        gbc.gridx = 1;
        contactField.setToolTipText("e.g. email@univ.edu or +1-555-0100");
        contentPanel.add(contactField, gbc);

        // Active Checkbox (for editing)
        if (existingUser != null) {
            gbc.gridy++;
            gbc.gridx = 1;
            contentPanel.add(activeCheckBox, gbc);
        }

        // Prepopulate if editing
        if (existingUser != null) {
            nameField.setText(existingUser.getFullName());
            regNoField.setText(existingUser.getRegistrationNumber());
            contactField.setText(existingUser.getContactDetails());
            activeCheckBox.setSelected(existingUser.isActive());

            String dept = existingUser.getDepartment();
            boolean found = false;
            for (String d : DEFAULT_DEPTS) {
                if (d.equalsIgnoreCase(dept)) {
                    deptCombo.setSelectedItem(d);
                    found = true;
                    break;
                }
            }
            if (!found) {
                deptCombo.setSelectedItem("Other (Custom)");
                customDeptField.setText(dept);
                customDeptField.setVisible(true);
            }
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(UITheme.ROW_ALT);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelButton = UITheme.createSecondaryButton("Cancel");
        JButton saveButton = UITheme.createPrimaryButton(existingUser == null ? "Register User" : "Save Changes");

        cancelButton.addActionListener(e -> dispose());

        saveButton.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private boolean validateInput() {
        if (getFullName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return false;
        }
        if (getRegistrationNumber().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Registration number is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            regNoField.requestFocus();
            return false;
        }
        if (getDepartment().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Department is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            if (customDeptField.isVisible()) customDeptField.requestFocus();
            return false;
        }
        if (getContactDetails().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Contact details (email or phone) are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            contactField.requestFocus();
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getFullName() {
        return nameField.getText().trim();
    }

    public String getRegistrationNumber() {
        return regNoField.getText().trim();
    }

    public String getDepartment() {
        String selected = (String) deptCombo.getSelectedItem();
        if ("Other (Custom)".equals(selected)) {
            return customDeptField.getText().trim();
        }
        return selected != null ? selected : "";
    }

    public String getContactDetails() {
        return contactField.getText().trim();
    }

    public boolean isUserActive() {
        return activeCheckBox.isSelected();
    }
}
