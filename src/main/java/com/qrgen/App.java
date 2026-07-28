package com.qrgen;

import com.qrgen.gui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the QR Code Generator desktop application.
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default look and feel if the system one is unavailable.
            }
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
