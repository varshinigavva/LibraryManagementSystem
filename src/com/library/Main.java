package com.library;

import com.library.service.LibraryService;
import com.library.storage.FileStorageService;
import com.library.storage.StorageService;
import com.library.ui.LoginFrame;

import javax.swing.*;

/**
 * Application entry point for the College Library Management System.
 */
public class Main {
    public static void main(String[] args) {
        // Set system look and feel for native UI fidelity
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default cross-platform look and feel
        }

        // Initialize business services and storage
        SwingUtilities.invokeLater(() -> {
            try {
                StorageService storageService = new FileStorageService("data/library_data.ser");
                LibraryService libraryService = new LibraryService(storageService);

                LoginFrame loginFrame = new LoginFrame(libraryService);
                loginFrame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to initialize Library Management System:\n" + e.getMessage() +
                        "\n\nPlease verify that you have write permissions to the 'data' directory.",
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

