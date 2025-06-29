package com.irfaan.efaktur.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class QrExtractor {

    public static Optional<String> extractQrUrl(MultipartFile file) {

        try (BufferedInputStream bufferedStream = new BufferedInputStream(file.getInputStream())) {

            if (!isPdfFile(bufferedStream)) {
                BufferedImage image = ImageIO.read(bufferedStream);
                ImageIO.write(image, "png", new File("debug-ocr-not-pdf.png"));
                return tryDecodeQrFromImage(image);
            }

            try (PDDocument document = PDDocument.load(bufferedStream)) {
                PDFRenderer renderer = new PDFRenderer(document);

                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    BufferedImage image = renderer.renderImageWithDPI(page, 300);

                    ImageIO.write(image, "png", new File("debug-ocr-full.png"));

                    Optional<String> result = tryDecodeQrFromImage(image);
                    if (result.isPresent()) {
                        return result;
                    }

                    int width = image.getWidth();
                    int height = image.getHeight();
                    BufferedImage cropped = image.getSubimage(width / 10, height - height / 3, width / 3, height / 3);
                    ImageIO.write(cropped, "png", new File("debug-ocr-cropped.png"));
                    result = tryDecodeQrFromImage(cropped);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
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


    private static boolean isPdfFile(InputStream inputStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        bis.mark(4);
        byte[] header = new byte[4];
        bis.read(header);
        bis.reset();
        return new String(header).startsWith("%PDF");
    }
}
