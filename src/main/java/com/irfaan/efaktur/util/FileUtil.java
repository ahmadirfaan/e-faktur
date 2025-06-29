package com.irfaan.efaktur.util;

import com.irfaan.efaktur.tessdata.TessDataLoader;
import jakarta.annotation.PostConstruct;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class FileUtil {

    public static File tessdataDir;


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
            String text = stripper.getText(document);

            if (StringUtils.isBlank(text)) {
                return doOcrFromPdfImage(file);
            }
            return text;
        } catch (Exception e) {
            return null;
        }
    }

    private static String doOcrFromPdfImage(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage rawImage = renderer.renderImageWithDPI(0, 300);
            BufferedImage optimizeForOCR = ImagePreProcessorUtil.optimizeForOCR(rawImage);
            return doOcr(optimizeForOCR);
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractTextFromImage(MultipartFile file) throws Exception {
        BufferedImage rawImage = ImageIO.read(file.getInputStream());
        BufferedImage optimizeForOCR = ImagePreProcessorUtil.optimizeForOCR(rawImage);
        return doOcr(optimizeForOCR);
    }

    private static String doOcr(BufferedImage image) throws Exception {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataDir.getAbsolutePath());
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

    @PostConstruct
    public void initTessdata() {
        try {
            tessdataDir = TessDataLoader.prepareTessdata();
            System.out.println("Tessdata prepared at: " + tessdataDir.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to extract tessdata", e);
        }
    }
}
