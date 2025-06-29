package com.irfaan.efaktur.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImagePreProcessorUtil {

    public static BufferedImage optimizeForOCR(BufferedImage input) throws IOException {
        int scaleFactor = 2;
        Image scaled = input.getScaledInstance(
                input.getWidth() * scaleFactor,
                input.getHeight() * scaleFactor,
                Image.SCALE_SMOOTH
        );
        BufferedImage resized = new BufferedImage(
                scaled.getWidth(null),
                scaled.getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        // 2. Grayscale
        BufferedImage grayImage = new BufferedImage(
                resized.getWidth(), resized.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(resized, 0, 0, null);
        g.dispose();

        ImageIO.write(grayImage, "png", new File("output-preprocessed.png"));


        return grayImage;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int scale) {


        int width = originalImage.getWidth() * scale;
        int height = originalImage.getHeight() * scale;

        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_REPLICATE);

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }





}
