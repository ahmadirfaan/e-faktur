package com.irfaan.efaktur.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@Component
@Slf4j
public class FileUtil {

    @Autowired
    private Tesseract tesseract;


    public String readFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        };

        String extension = getExtension(filename).toLowerCase();

        return switch (extension) {
            case "pdf" -> extractTextFromPdf(file);
            case "jpg", "jpeg", "png" -> extractTextFromImage(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + extension);
        };
    }

    private String extractTextFromPdf(MultipartFile file) {
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

    private String doOcrFromPdfImage(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage rawImage = renderer.renderImageWithDPI(0, 300);
            BufferedImage optimizeForOCR = ImagePreProcessorUtil.optimizeForOCR(rawImage);
            return tesseract.doOCR(optimizeForOCR);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTextFromImage(MultipartFile file) throws Exception {
        BufferedImage rawImage = ImageIO.read(file.getInputStream());
        BufferedImage optimizeForOCR = ImagePreProcessorUtil.optimizeForOCR(rawImage);
        return tesseract.doOCR(optimizeForOCR);
    }


    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }


}
