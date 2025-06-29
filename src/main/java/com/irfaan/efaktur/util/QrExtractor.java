package com.irfaan.efaktur.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class QrExtractor {

    public static Optional<String> extractQrUrl(MultipartFile file) {

        try (InputStream in = file.getInputStream()) {
            if (!isPdfFile(in)) {
                BufferedImage image = ImageIO.read(file.getInputStream());
                ImageIO.write(image, "png", new File("debug-ocr-not-pdf-file.png"));
                return tryDecodeQrFromImage(image);
            }

            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);

                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    BufferedImage image = renderer.renderImageWithDPI(page, 300);

                    Optional<String> result = tryDecodeQrFromImage(image);
                    if (result.isPresent()) return result;

                    int w = image.getWidth(), h = image.getHeight();
                    BufferedImage cropped = image.getSubimage(w / 10, h - h / 3, w / 3, h / 3);
                    result = tryDecodeQrFromImage(cropped);
                    if (result.isPresent()) {
                        return result;
                    }

                    ImageIO.write(image, "png", new File("debug-ocr-full.png"));
                    ImageIO.write(cropped, "png", new File("debug-ocr-cropped.png"));

                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // log saat dev
        }

        return Optional.empty();
    }

    private static Optional<String> tryDecodeQrFromImage(BufferedImage image) {
        try {
            if (image == null) return Optional.empty();
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);

            return Optional.of(result.getText());
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    private static boolean isPdfFile(InputStream stream) throws IOException {
        stream.mark(4);
        byte[] header = new byte[4];
        stream.read(header);
        stream.reset();
        return new String(header).startsWith("%PDF");
    }
}
