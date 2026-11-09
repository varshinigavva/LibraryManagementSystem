package com.library.ui;

import com.library.model.Book;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modal dialog for adding or editing a book record.
 */
public class BookDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JTextField titleField = UITheme.createTextField(25);
    private final JTextField authorField = UITheme.createTextField(25);
    private final JComboBox<String> categoryCombo;
    private final JTextField customCategoryField = UITheme.createTextField(15);
    private boolean confirmed = false;

    private static final String[] DEFAULT_CATEGORIES = {
            "Computer Science", "Electronics", "Mathematics", "Physics",
            "Mechanical Engineering", "Civil Engineering", "Management",
            "Humanities & Literature", "Other (Custom)"
    };

    public BookDialog(Frame owner, String title, Book existingBook) {
        super(owner, title, true);
        setLayout(new BorderLayout());
        setResizable(false);

        categoryCombo = UITheme.createComboBox(DEFAULT_CATEGORIES);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(24, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Header notice
        JLabel headerLabel = new JLabel(existingBook == null ? "Enter Book Details" : "Edit Book (" + existingBook.getBookId() + ")");
        headerLabel.setFont(UITheme.FONT_HEADER);
        headerLabel.setForeground(UITheme.PRIMARY);
        gbc.gridwidth = 2;
        contentPanel.add(headerLabel, gbc);

        gbc.gridwidth = 1;

        // Book Title
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l1 = new JLabel("Book Title *:");
        l1.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l1, gbc);

        gbc.gridx = 1;
        contentPanel.add(titleField, gbc);

        // Author
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l2 = new JLabel("Author(s) *:");
        l2.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l2, gbc);

        gbc.gridx = 1;
        contentPanel.add(authorField, gbc);

        // Category
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel l3 = new JLabel("Category *:");
        l3.setFont(UITheme.FONT_BOLD);
        contentPanel.add(l3, gbc);

        gbc.gridx = 1;
        JPanel catPanel = new JPanel(new BorderLayout(5, 0));
        catPanel.setBackground(Color.WHITE);
        catPanel.add(categoryCombo, BorderLayout.CENTER);
        catPanel.add(customCategoryField, BorderLayout.EAST);
        customCategoryField.setVisible(false);
        contentPanel.add(catPanel, gbc);

        categoryCombo.addActionListener(e -> {
            boolean isOther = "Other (Custom)".equals(categoryCombo.getSelectedItem());
            customCategoryField.setVisible(isOther);
            pack();
        });

        // Prepopulate if editing
        if (existingBook != null) {
            titleField.setText(existingBook.getTitle());
            authorField.setText(existingBook.getAuthor());
            String cat = existingBook.getCategory();
            boolean found = false;
            for (String c : DEFAULT_CATEGORIES) {
                if (c.equalsIgnoreCase(cat)) {
                    categoryCombo.setSelectedItem(c);
                    found = true;
                    break;
                }
            }
            if (!found) {
                categoryCombo.setSelectedItem("Other (Custom)");
                customCategoryField.setText(cat);
                customCategoryField.setVisible(true);
            }
        }

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(UITheme.ROW_ALT);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER));

        JButton cancelButton = UITheme.createSecondaryButton("Cancel");
        JButton saveButton = UITheme.createPrimaryButton(existingBook == null ? "Add Book" : "Save Changes");

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
        if (getBookTitle().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Book title is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return false;
        }
        if (getAuthor().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Author name is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            authorField.requestFocus();
            return false;
        }
        if (getCategory().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Category is required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            if (customCategoryField.isVisible()) customCategoryField.requestFocus();
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getBookTitle() {
        return titleField.getText().trim();
    }

    public String getAuthor() {
        return authorField.getText().trim();
    }

    public String getCategory() {
        String selected = (String) categoryCombo.getSelectedItem();
        if ("Other (Custom)".equals(selected)) {
            return customCategoryField.getText().trim();
        }
        return selected != null ? selected : "";
    }
}
