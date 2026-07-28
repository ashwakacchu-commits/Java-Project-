package com.qrgen.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Service responsible for turning raw text content into a rendered QR code
 * image. This class isolates all ZXing-specific logic so the rest of the
 * application (GUI, history, file I/O) never has to deal with the encoding
 * library directly.
 */
public class QRCodeService {

    /**
     * Generates a QR code image from the given text.
     *
     * @param content        the text/data to encode (URL, plain text, vCard, etc.)
     * @param sizePx         width and height of the output image, in pixels (square)
     * @param errorCorrection desired error correction level
     * @param foreground     color used for the QR "on" modules (the dark squares)
     * @param background     color used for the background ("off" modules)
     * @param marginModules  quiet-zone margin, in modules (ZXing default is 4)
     * @return a BufferedImage containing the rendered QR code
     * @throws WriterException if the content cannot be encoded (e.g. too long
     *                          for the chosen error-correction level)
     */
    public BufferedImage generate(String content,
                                   int sizePx,
                                   ErrorCorrectionLevel errorCorrection,
                                   Color foreground,
                                   Color background,
                                   int marginModules) throws WriterException {

        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Content to encode must not be empty.");
        }

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, errorCorrection);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, marginModules);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints);

        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int onColor = foreground.getRGB();
        int offColor = background.getRGB();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, matrix.get(x, y) ? onColor : offColor);
            }
        }

        return image;
    }

    /** Rough estimate of encoded byte size, useful for showing stats in the UI. */
    public int estimateByteSize(String content) {
        return content == null ? 0 : content.getBytes().length;
    }
}
