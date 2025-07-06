package com.irfaan.efaktur.util;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
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
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class FileUtil {

    @Autowired
    private Tesseract tesseract;


    public BufferedImage convertToImage(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }
        ;

        String extension = getExtension(filename).toLowerCase();

        return switch (extension) {
            case "pdf" -> extractTextFromPdf(file);
            case "jpg", "jpeg", "png" -> extractTextFromImage(file);
            default -> throw new IllegalArgumentException("Unsupported file type: " + extension);
        };
    }

    private BufferedImage extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer renderer = new PDFRenderer(document);
            return renderer.renderImageWithDPI(0, 500);
        } catch (Exception e) {
            return null;
        }
    }


    private BufferedImage extractTextFromImage(MultipartFile file) throws Exception {
        return ImageIO.read(file.getInputStream());
    }


    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }


    public List<BufferedImage> convertInto3PartImage(BufferedImage image) {
        List<BufferedImage> images = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();
        int partHeight = height / 3;

        for (int i = 0; i < 3; i++) {
            int y = i * partHeight;
            int h = (i == 2) ? height - y : partHeight; // last part catch remainder
            BufferedImage partImage = image.getSubimage(0, y, width, h);
            images.add(partImage);
        }
        return images;
    }

    public String doOcr(BufferedImage bufferedImage) {
        try {
            return tesseract.doOCR(bufferedImage);
        } catch (TesseractException e) {
            return null;
        }
    }
}
