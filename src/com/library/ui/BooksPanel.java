package com.library.ui;

import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.service.LibraryService;
import com.library.service.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Complete Book Management panel supporting CRUD, searching, category filtering,
 * multi-attribute sorting, and status-aware validation.
 */
@SuppressWarnings("serial")
public class BooksPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final LibraryService libraryService;
    private final MainFrame mainFrame;

    private JTable bookTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField searchField;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> sortByCombo;
    private JLabel countLabel;

    public BooksPanel(LibraryService libraryService, MainFrame mainFrame) {
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
        JLabel titleLabel = new JLabel("Book Catalog Management");
        titleLabel.setFont(UITheme.FONT_TITLE);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subLabel = new JLabel("Add, edit, search, filter, and manage college library books");
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);

        titleBlock.add(titleLabel);
        titleBlock.add(subLabel);

        // Action Buttons
        JPanel actionBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBtnRow.setOpaque(false);

        boolean isLibrarian = mainFrame.getCurrentUser() == null || mainFrame.getCurrentUser().isLibrarian();

        JButton addBtn = UITheme.createPrimaryButton("+ Add New Book");
        JButton editBtn = UITheme.createSecondaryButton("Edit Book");
        JButton deleteBtn = UITheme.createDangerButton("Delete Book");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        addBtn.addActionListener(e -> onAddBook());
        editBtn.addActionListener(e -> onEditBook());
        deleteBtn.addActionListener(e -> onDeleteBook());
        refreshBtn.addActionListener(e -> refreshTable());

        if (isLibrarian) {
            actionBtnRow.add(addBtn);
            actionBtnRow.add(editBtn);
            actionBtnRow.add(deleteBtn);
        }
        actionBtnRow.add(refreshBtn);

        headerRow.add(titleBlock, BorderLayout.WEST);
        headerRow.add(actionBtnRow, BorderLayout.EAST);
        topCard.add(headerRow, BorderLayout.NORTH);

        // Filter / Search Toolbar Row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        searchField = UITheme.createTextField(18);
        searchField.setToolTipText("Search title, author, or ID");

        JButton searchBtn = UITheme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> applyFilters());

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            categoryFilterCombo.setSelectedIndex(0);
            applyFilters();
        });

        categoryFilterCombo = UITheme.createComboBox(new String[]{"All Categories"});
        categoryFilterCombo.addActionListener(e -> applyFilters());

        String[] sortOptions = {"Sort by Book ID", "Sort by Title", "Sort by Author", "Sort by Category"};
        sortByCombo = UITheme.createComboBox(sortOptions);
        sortByCombo.addActionListener(e -> applySorting());

        filterRow.add(new JLabel("Search:"));
        filterRow.add(searchField);
        filterRow.add(searchBtn);
        filterRow.add(clearBtn);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(new JLabel("Category:"));
        filterRow.add(categoryFilterCombo);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(new JLabel("Sort:"));
        filterRow.add(sortByCombo);

        topCard.add(filterRow, BorderLayout.SOUTH);
        add(topCard, BorderLayout.NORTH);

        // Center Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        String[] columnNames = {"Book ID", "Title", "Author(s)", "Category", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        bookTable = new JTable(tableModel);
        UITheme.styleTable(bookTable);
        rowSorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(rowSorter);

        bookTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        bookTable.getColumnModel().getColumn(4).setCellRenderer(new UITheme.StatusBadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Table Status Row
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        countLabel = new JLabel("Showing 0 books");
        countLabel.setFont(UITheme.FONT_SUBTITLE);
        countLabel.setForeground(UITheme.TEXT_MUTED);

        boolean isLibrarianRole = mainFrame.getCurrentUser() == null || mainFrame.getCurrentUser().isLibrarian();
        JLabel hintLabel = new JLabel(isLibrarianRole ? 
                "Tip: Double-click a book to edit. Deletion is blocked if book is currently borrowed." :
                "Showing college library collection. Visit circulation counter to borrow books.");
        hintLabel.setFont(UITheme.FONT_SMALL);
        hintLabel.setForeground(UITheme.TEXT_MUTED);

        bottomRow.add(countLabel, BorderLayout.WEST);
        bottomRow.add(hintLabel, BorderLayout.EAST);
        tableCard.add(bottomRow, BorderLayout.SOUTH);

        // Double click to edit (librarians only)
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bookTable.getSelectedRow() != -1) {
                    if (mainFrame.getCurrentUser() == null || mainFrame.getCurrentUser().isLibrarian()) {
                        onEditBook();
                    }
                }
            }
        });

        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshTable() {
        // Refresh category combo
        String currentCat = (String) categoryFilterCombo.getSelectedItem();
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("All Categories");
        for (String c : libraryService.getAllCategories()) {
            categoryFilterCombo.addItem(c);
        }
        if (currentCat != null) {
            categoryFilterCombo.setSelectedItem(currentCat);
        }

        applyFilters();
    }

    private void applyFilters() {
        tableModel.setRowCount(0);
        String query = searchField.getText().trim();
        String selectedCat = (String) categoryFilterCombo.getSelectedItem();

        List<Book> books = libraryService.searchAndFilterBooks(query, selectedCat);

        for (Book b : books) {
            tableModel.addRow(new Object[]{
                    b.getBookId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getStatus().getDisplayName()
            });
        }

        applySorting();
        countLabel.setText("Showing " + books.size() + " of " + libraryService.getTotalBooksCount() + " cataloged books");
    }

    private void applySorting() {
        int sortIndex = sortByCombo.getSelectedIndex();
        switch (sortIndex) {
            case 1: // Title
                rowSorter.setComparator(1, String.CASE_INSENSITIVE_ORDER);
                rowSorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
                break;
            case 2: // Author
                rowSorter.setComparator(2, String.CASE_INSENSITIVE_ORDER);
                rowSorter.setSortKeys(List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
                break;
            case 3: // Category
                rowSorter.setComparator(3, String.CASE_INSENSITIVE_ORDER);
                rowSorter.setSortKeys(List.of(new RowSorter.SortKey(3, SortOrder.ASCENDING)));
                break;
            default: // Book ID
                rowSorter.setComparator(0, String.CASE_INSENSITIVE_ORDER);
                rowSorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
                break;
        }
    }

    private void onAddBook() {
        BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Book", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                Book newBook = libraryService.addBook(dialog.getBookTitle(), dialog.getAuthor(), dialog.getCategory());
                JOptionPane.showMessageDialog(this,
                        "Book successfully cataloged!\nGenerated Book ID: " + newBook.getBookId(),
                        "Book Added", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.notifyDataChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding book: " + ex.getMessage(),
                        "Add Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onEditBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book from the table to edit.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        String bookId = (String) tableModel.getValueAt(modelRow, 0);
        Book book = libraryService.getBookById(bookId);

        if (book == null) {
            JOptionPane.showMessageDialog(this, "Selected book could not be found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Book Details", book);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            try {
                libraryService.updateBook(bookId, dialog.getBookTitle(), dialog.getAuthor(), dialog.getCategory());
                JOptionPane.showMessageDialog(this, "Book details successfully updated.",
                        "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.notifyDataChanged();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage(),
                        "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onDeleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete.",
                    "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        String bookId = (String) tableModel.getValueAt(modelRow, 0);
        String bookTitle = (String) tableModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete book '" + bookTitle + "' (" + bookId + ")?\n" +
                "Note: A book with an active borrowing cannot be deleted.",
                "Confirm Book Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                libraryService.deleteBook(bookId);
                JOptionPane.showMessageDialog(this, "Book '" + bookTitle + "' deleted successfully.",
                        "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                mainFrame.notifyDataChanged();
            } catch (ValidationException ve) {
                // Clear explanation dialog as required
                JOptionPane.showMessageDialog(this, ve.getMessage(),
                        "Cannot Delete Book", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
