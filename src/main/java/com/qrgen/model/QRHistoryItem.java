package com.qrgen.model;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single previously generated QR code, kept in-memory so the
 * user can revisit and re-export earlier results within the same session.
 */
public class QRHistoryItem {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String label;
    private final String content;
    private final BufferedImage image;
    private final LocalDateTime createdAt;

    public QRHistoryItem(String content, BufferedImage image) {
        this.content = content;
        this.image = image;
        this.createdAt = LocalDateTime.now();
        this.label = buildLabel(content);
    }

    private String buildLabel(String content) {
        String singleLine = content.replace("\n", " ").trim();
        String preview = singleLine.length() > 40 ? singleLine.substring(0, 40) + "..." : singleLine;
        return "[" + createdAtFormatted() + "] " + preview;
    }

    public String getContent() {
        return content;
    }

    public BufferedImage getImage() {
        return image;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String createdAtFormatted() {
        return TIME_FORMAT.format(createdAt);
    }

    @Override
    public String toString() {
        return label;
    }
}
