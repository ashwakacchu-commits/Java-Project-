package com.qrgen.gui;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.qrgen.model.QRHistoryItem;
import com.qrgen.qr.QRCodeService;
import com.qrgen.util.ContentBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The main application window. Wires together the content-type tabs, the
 * generation options, the live preview, and the session history panel.
 */
public class MainFrame extends JFrame {

    private final QRCodeService qrService = new QRCodeService();
    private final DefaultListModel<QRHistoryItem> historyModel = new DefaultListModel<>();

    // --- Content type tabs -------------------------------------------------
    private JTabbedPane contentTabs;
    private JTextArea textArea;
    private JTextField urlField;
    private JTextField emailAddressField;
    private JTextField emailSubjectField;
    private JTextArea emailBodyArea;
    private JTextField phoneField;
    private JTextField smsNumberField;
    private JTextArea smsMessageArea;
    private JTextField wifiSsidField;
    private JPasswordField wifiPasswordField;
    private JComboBox<String> wifiSecurityCombo;
    private JCheckBox wifiHiddenCheck;
    private JTextField contactNameField;
    private JTextField contactPhoneField;
    private JTextField contactEmailField;
    private JTextField contactOrgField;

    // --- Options -------------------------------------------------------
    private JSpinner sizeSpinner;
    private JSpinner marginSpinner;
    private JComboBox<ErrorCorrectionLevel> errorCorrectionCombo;
    private JButton foregroundColorButton;
    private JButton backgroundColorButton;
    private Color foregroundColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;

    // --- Preview / output -------------------------------------------------
    private JLabel previewLabel;
    private JButton saveButton;
    private JButton copyButton;
    private JButton generateButton;
    private JButton clearButton;

    // --- History -------------------------------------------------------
    private JList<QRHistoryItem> historyList;

    // --- Status bar -------------------------------------------------------
    private JLabel statusLabel;
    private JLabel statsLabel;

    private BufferedImage currentImage;

    public MainFrame() {
        super("QR Code Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 640));
        setLayout(new BorderLayout());

        setJMenuBar(buildMenuBar());
        add(buildContentTabs(), BorderLayout.NORTH);
        add(buildCenterSplit(), BorderLayout.CENTER);
        add(buildHistoryPanel(), BorderLayout.EAST);
        add(buildStatusBar(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        updateStatus("Ready. Enter content and click Generate.");
    }

    // ------------------------------------------------------------------
    // Menu bar
    // ------------------------------------------------------------------
    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem saveItem = new JMenuItem("Save QR Image As...");
        saveItem.addActionListener(e -> saveImage());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem copyItem = new JMenuItem("Copy Image to Clipboard");
        copyItem.addActionListener(e -> copyImageToClipboard());
        JMenuItem clearItem = new JMenuItem("Clear Input");
        clearItem.addActionListener(e -> clearCurrentTabInput());
        editMenu.add(copyItem);
        editMenu.add(clearItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "QR Code Generator\nVersion 1.0.0\n\nBuilt with Java Swing and the ZXing library.\n" +
                        "Supports plain text, URLs, email, phone, SMS, WiFi, and contact card QR codes.",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Content type tabs
    // ------------------------------------------------------------------
    private JComponent buildContentTabs() {
        contentTabs = new JTabbedPane();
        contentTabs.addTab("Text / URL", buildTextUrlTab());
        contentTabs.addTab("Email", buildEmailTab());
        contentTabs.addTab("Phone", buildPhoneTab());
        contentTabs.addTab("SMS", buildSmsTab());
        contentTabs.addTab("WiFi", buildWifiTab());
        contentTabs.addTab("Contact", buildContactTab());
        contentTabs.setBorder(new EmptyBorder(8, 8, 0, 8));
        return contentTabs;
    }

    private JComponent buildTextUrlTab() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel urlRow = new JPanel(new BorderLayout(6, 0));
        urlRow.add(new JLabel("URL (optional quick-fill):"), BorderLayout.WEST);
        urlField = new JTextField();
        urlRow.add(urlField, BorderLayout.CENTER);
        JButton useUrlButton = new JButton("Use as Text");
        useUrlButton.addActionListener(e -> {
            String u = urlField.getText().trim();
            if (!u.isEmpty()) {
                textArea.setText(ContentBuilder.url(u));
            }
        });
        urlRow.add(useUrlButton, BorderLayout.EAST);

        textArea = new JTextArea(4, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Text content to encode"));

        panel.add(urlRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildEmailTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = labelFieldConstraints();

        emailAddressField = new JTextField(25);
        emailSubjectField = new JTextField(25);
        emailBodyArea = new JTextArea(3, 25);
        emailBodyArea.setLineWrap(true);

        addRow(panel, gbc, 0, "To address:", emailAddressField);
        addRow(panel, gbc, 1, "Subject:", emailSubjectField);
        addRow(panel, gbc, 2, "Body:", new JScrollPane(emailBodyArea));
        return panel;
    }

    private JComponent buildPhoneTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = labelFieldConstraints();
        phoneField = new JTextField(25);
        addRow(panel, gbc, 0, "Phone number:", phoneField);
        return panel;
    }

    private JComponent buildSmsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = labelFieldConstraints();
        smsNumberField = new JTextField(25);
        smsMessageArea = new JTextArea(3, 25);
        smsMessageArea.setLineWrap(true);
        addRow(panel, gbc, 0, "Phone number:", smsNumberField);
        addRow(panel, gbc, 1, "Message:", new JScrollPane(smsMessageArea));
        return panel;
    }

    private JComponent buildWifiTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = labelFieldConstraints();

        wifiSsidField = new JTextField(25);
        wifiPasswordField = new JPasswordField(25);
        wifiSecurityCombo = new JComboBox<>(new String[]{"WPA", "WEP", "nopass"});
        wifiHiddenCheck = new JCheckBox("Hidden network");

        addRow(panel, gbc, 0, "Network name (SSID):", wifiSsidField);
        addRow(panel, gbc, 1, "Password:", wifiPasswordField);
        addRow(panel, gbc, 2, "Security type:", wifiSecurityCombo);
        addRow(panel, gbc, 3, "", wifiHiddenCheck);
        return panel;
    }

    private JComponent buildContactTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));
        GridBagConstraints gbc = labelFieldConstraints();

        contactNameField = new JTextField(25);
        contactPhoneField = new JTextField(25);
        contactEmailField = new JTextField(25);
        contactOrgField = new JTextField(25);

        addRow(panel, gbc, 0, "Full name:", contactNameField);
        addRow(panel, gbc, 1, "Phone number:", contactPhoneField);
        addRow(panel, gbc, 2, "Email:", contactEmailField);
        addRow(panel, gbc, 3, "Organization:", contactOrgField);
        return panel;
    }

    private GridBagConstraints labelFieldConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);
    }

    // ------------------------------------------------------------------
    // Center: options (left) + preview (right)
    // ------------------------------------------------------------------
    private JComponent buildCenterSplit() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildOptionsPanel(), buildPreviewPanel());
        split.setResizeWeight(0.32);
        split.setDividerLocation(320);
        split.setBorder(new EmptyBorder(4, 8, 8, 8));
        return split;
    }

    private JComponent buildOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Options"));
        GridBagConstraints gbc = labelFieldConstraints();

        sizeSpinner = new JSpinner(new SpinnerNumberModel(400, 100, 2000, 50));
        marginSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 20, 1));
        errorCorrectionCombo = new JComboBox<>(ErrorCorrectionLevel.values());
        errorCorrectionCombo.setSelectedItem(ErrorCorrectionLevel.M);
        errorCorrectionCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                            boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ErrorCorrectionLevel level) {
                    setText(switch (level) {
                        case L -> "L - Low (~7% recovery)";
                        case M -> "M - Medium (~15% recovery)";
                        case Q -> "Q - Quartile (~25% recovery)";
                        case H -> "H - High (~30% recovery)";
                    });
                }
                return this;
            }
        });

        foregroundColorButton = new JButton("Choose...");
        styleColorButton(foregroundColorButton, foregroundColor);
        foregroundColorButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose foreground color", foregroundColor);
            if (chosen != null) {
                foregroundColor = chosen;
                styleColorButton(foregroundColorButton, foregroundColor);
            }
        });

        backgroundColorButton = new JButton("Choose...");
        styleColorButton(backgroundColorButton, backgroundColor);
        backgroundColorButton.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Choose background color", backgroundColor);
            if (chosen != null) {
                backgroundColor = chosen;
                styleColorButton(backgroundColorButton, backgroundColor);
            }
        });

        int row = 0;
        addRow(panel, gbc, row++, "Image size (px):", sizeSpinner);
        addRow(panel, gbc, row++, "Quiet zone margin:", marginSpinner);
        addRow(panel, gbc, row++, "Error correction:", errorCorrectionCombo);
        addRow(panel, gbc, row++, "Foreground color:", foregroundColorButton);
        addRow(panel, gbc, row++, "Background color:", backgroundColorButton);

        generateButton = new JButton("Generate QR Code");
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD));
        generateButton.addActionListener(e -> generateQrCode());

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearCurrentTabInput());

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonRow.add(generateButton);
        buttonRow.add(clearButton);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 4, 4, 4);
        panel.add(buttonRow, gbc);

        // Filler to push everything to the top
        gbc.gridy = row + 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private void styleColorButton(JButton button, Color color) {
        button.setIcon(new ColorSwatch(color));
    }

    private JComponent buildPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new TitledBorder("Preview"));

        previewLabel = new JLabel("QR code will appear here", SwingConstants.CENTER);
        previewLabel.setVerticalAlignment(SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(420, 420));
        previewLabel.setBorder(BorderFactory.createDashedBorder(Color.GRAY));

        JScrollPane scrollPane = new JScrollPane(previewLabel);

        saveButton = new JButton("Save as PNG...");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveImage());

        copyButton = new JButton("Copy to Clipboard");
        copyButton.setEnabled(false);
        copyButton.addActionListener(e -> copyImageToClipboard());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        buttonRow.add(saveButton);
        buttonRow.add(copyButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonRow, BorderLayout.SOUTH);
        return panel;
    }

    // ------------------------------------------------------------------
    // History panel
    // ------------------------------------------------------------------
    private JComponent buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(new EmptyBorder(8, 0, 8, 8));
        panel.setPreferredSize(new Dimension(230, 0));

        historyList = new JList<>(historyModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadFromHistory();
            }
        });
        JScrollPane scrollPane = new JScrollPane(historyList);
        scrollPane.setBorder(new TitledBorder("History (this session)"));

        JButton clearHistoryButton = new JButton("Clear History");
        clearHistoryButton.addActionListener(e -> {
            historyModel.clear();
            updateStatus("History cleared.");
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(clearHistoryButton, BorderLayout.SOUTH);
        return panel;
    }

    private void loadFromHistory() {
        QRHistoryItem selected = historyList.getSelectedValue();
        if (selected == null) return;
        currentImage = selected.getImage();
        previewLabel.setIcon(new ImageIcon(currentImage));
        previewLabel.setText(null);
        saveButton.setEnabled(true);
        copyButton.setEnabled(true);
        updateStats(selected.getContent(), currentImage);
        updateStatus("Loaded QR code generated at " + selected.createdAtFormatted() + " from history.");
    }

    // ------------------------------------------------------------------
    // Status bar
    // ------------------------------------------------------------------
    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(4, 10, 4, 10));
        statusLabel = new JLabel("Ready.");
        statsLabel = new JLabel(" ");
        bar.add(statusLabel, BorderLayout.WEST);
        bar.add(statsLabel, BorderLayout.EAST);
        return bar;
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    private void updateStats(String content, BufferedImage image) {
        int bytes = qrService.estimateByteSize(content);
        statsLabel.setText(String.format("%d chars | %d bytes | %dx%d px",
                content.length(), bytes, image.getWidth(), image.getHeight()));
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------
    private String resolveContentFromActiveTab() {
        int index = contentTabs.getSelectedIndex();
        return switch (index) {
            case 0 -> textArea.getText().trim();
            case 1 -> ContentBuilder.email(emailAddressField.getText().trim(),
                    emailSubjectField.getText().trim(), emailBodyArea.getText().trim());
            case 2 -> ContentBuilder.phone(phoneField.getText().trim());
            case 3 -> ContentBuilder.sms(smsNumberField.getText().trim(), smsMessageArea.getText().trim());
            case 4 -> ContentBuilder.wifi(wifiSsidField.getText().trim(),
                    new String(wifiPasswordField.getPassword()),
                    (String) wifiSecurityCombo.getSelectedItem(), wifiHiddenCheck.isSelected());
            case 5 -> ContentBuilder.contact(contactNameField.getText().trim(), contactPhoneField.getText().trim(),
                    contactEmailField.getText().trim(), contactOrgField.getText().trim());
            default -> "";
        };
    }

    private void clearCurrentTabInput() {
        int index = contentTabs.getSelectedIndex();
        switch (index) {
            case 0 -> {
                textArea.setText("");
                urlField.setText("");
            }
            case 1 -> {
                emailAddressField.setText("");
                emailSubjectField.setText("");
                emailBodyArea.setText("");
            }
            case 2 -> phoneField.setText("");
            case 3 -> {
                smsNumberField.setText("");
                smsMessageArea.setText("");
            }
            case 4 -> {
                wifiSsidField.setText("");
                wifiPasswordField.setText("");
                wifiHiddenCheck.setSelected(false);
            }
            case 5 -> {
                contactNameField.setText("");
                contactPhoneField.setText("");
                contactEmailField.setText("");
                contactOrgField.setText("");
            }
        }
        updateStatus("Input cleared.");
    }

    private void generateQrCode() {
        String content = resolveContentFromActiveTab();
        if (content == null || content.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter some content before generating a QR code.",
                    "Nothing to encode", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int size = (Integer) sizeSpinner.getValue();
        int margin = (Integer) marginSpinner.getValue();
        ErrorCorrectionLevel level = (ErrorCorrectionLevel) errorCorrectionCombo.getSelectedItem();

        try {
            BufferedImage image = qrService.generate(content, size, level, foregroundColor, backgroundColor, margin);
            currentImage = image;
            previewLabel.setIcon(new ImageIcon(image));
            previewLabel.setText(null);
            saveButton.setEnabled(true);
            copyButton.setEnabled(true);

            QRHistoryItem item = new QRHistoryItem(content, image);
            historyModel.add(0, item);
            historyList.clearSelection();

            updateStats(content, image);
            updateStatus("QR code generated successfully.");
        } catch (WriterException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not generate QR code: " + ex.getMessage() +
                            "\nTry a lower error-correction level or shorter content.",
                    "Generation failed", JOptionPane.ERROR_MESSAGE);
            updateStatus("Generation failed: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid input", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveImage() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Generate a QR code first.", "Nothing to save",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save QR Code Image");
        chooser.setSelectedFile(new File("qrcode.png"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png")) {
            file = new File(file.getParentFile(), file.getName() + ".png");
        }
        try {
            ImageIO.write(currentImage, "png", file);
            updateStatus("Saved to " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save image: " + ex.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyImageToClipboard() {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "Generate a QR code first.", "Nothing to copy",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        Transferable transferable = new ImageTransferable(currentImage);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(transferable, null);
        updateStatus("QR code image copied to clipboard.");
    }

    /** Small square icon used to preview a chosen color on the color buttons. */
    private static class ColorSwatch implements Icon {
        private final Color color;

        ColorSwatch(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, getIconWidth() - 1, getIconHeight() - 1);
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }
}
