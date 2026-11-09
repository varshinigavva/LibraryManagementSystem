package com.library.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Visual styling theme, modern color palette, typography, and reusable UI factory methods.
 */
public final class UITheme {

    private UITheme() {}

    // Colors
    public static final Color BG_MAIN = new Color(248, 250, 252);        // #F8FAFC
    public static final Color CARD_BG = Color.WHITE;
    public static final Color SIDEBAR_BG = new Color(15, 23, 42);         // #0F172A (Deep Slate)
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);     // #1E293B
    public static final Color SIDEBAR_ACTIVE = new Color(37, 99, 235);    // #2563EB (Primary Blue)

    public static final Color PRIMARY = new Color(37, 99, 235);           // #2563EB
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);     // #1D4ED8
    public static final Color SECONDARY = new Color(100, 116, 139);       // #64748B
    public static final Color SUCCESS = new Color(16, 185, 129);          // #10B981
    public static final Color SUCCESS_LIGHT = new Color(236, 253, 245);   // #ECFDF5
    public static final Color WARNING = new Color(217, 119, 6);           // #D97706
    public static final Color WARNING_LIGHT = new Color(254, 243, 199);   // #FEF3C7
    public static final Color WARNING_BORDER = new Color(245, 158, 11);   // #F59E0B
    public static final Color DANGER = new Color(220, 38, 38);            // #DC2626
    public static final Color DANGER_LIGHT = new Color(254, 226, 226);    // #FEE2E2

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);       // #0F172A
    public static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748B
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);          // #E2E8F0
    public static final Color ROW_ALT = new Color(248, 250, 252);         // #F8FAFC

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_STAT_NUM = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /**
     * Creates a styled card panel with a border and subtle margin.
     */
    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    /**
     * Creates a primary styled button.
     */
    public static JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY);
            }
        });
        return button;
    }

    /**
     * Creates a secondary/outline styled button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(7, 14, 7, 14)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ROW_ALT);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        return button;
    }

    /**
     * Creates a danger/action button (e.g. Delete).
     */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(DANGER);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(185, 28, 28));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(DANGER);
            }
        });
        return button;
    }

    /**
     * Styles standard JTable with modern look, striping, and custom header.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header style
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        headerRenderer.setBorder(new EmptyBorder(0, 10, 0, 10));

        // Default cell renderer with padding and alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int col) {
                super.getTableCellRendererComponent(tbl, val, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : ROW_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                return this;
            }
        });
    }

    /**
     * Creates a styled text field with rounded-style padding and border.
     */
    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return tf;
    }

    /**
     * Creates a styled JComboBox.
     */
    public static <E> JComboBox<E> createComboBox(E[] items) {
        JComboBox<E> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setBackground(Color.WHITE);
        cb.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(2, 4, 2, 4)
        ));
        return cb;
    }

    /**
     * Custom renderer for status pills (Available, Borrowed, Active, Returned).
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(FONT_BOLD);

            String text = value != null ? value.toString() : "";
            label.setText(text);

            if (isSelected) {
                return label;
            }

            if ("Available".equalsIgnoreCase(text) || "Returned".equalsIgnoreCase(text) || "Active".equalsIgnoreCase(text)) {
                label.setBackground(SUCCESS_LIGHT);
                label.setForeground(SUCCESS);
                label.setOpaque(true);
            } else if ("Borrowed".equalsIgnoreCase(text) || "Active Borrowing".equalsIgnoreCase(text)) {
                label.setBackground(WARNING_LIGHT);
                label.setForeground(WARNING);
                label.setOpaque(true);
            } else if ("Deactivated".equalsIgnoreCase(text) || "Limit Exceeded".equalsIgnoreCase(text)) {
                label.setBackground(DANGER_LIGHT);
                label.setForeground(DANGER);
                label.setOpaque(true);
            } else {
                label.setBackground(Color.WHITE);
                label.setForeground(TEXT_PRIMARY);
            }

            label.setBorder(new EmptyBorder(2, 8, 2, 8));
            return label;
        }
    }
}
