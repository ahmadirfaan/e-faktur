package com.irfaan.efaktur.util;

import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class FileUtil {

    public static String extractTextByType(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) throw new IllegalArgumentException("Filename cannot be null");

        String extension = getExtension(filename).toLowerCase();

        return switch (extension) {
            case "pdf" -> extractTextFromPdf(file);
            case "jpg", "jpeg", "png" -> extractTextFromImage(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + extension);
        };
    }

    private static String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            return doOcrFromPdfImage(file);
        }
    }

    private static String doOcrFromPdfImage(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 300);
            return doOcr(image);
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractTextFromImage(MultipartFile file) throws Exception {
        BufferedImage image = ImageIO.read(file.getInputStream());
        return doOcr(image);
    }

    private static String doOcr(BufferedImage image) throws Exception {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("tessdata");
        tesseract.setLanguage("ind");
        return tesseract.doOCR(image);
    }

    private static String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }
}
