package com.irfaan.efaktur.util;


import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class ImagePreProcessorUtil {

    public static BufferedImage optimizeForOCR(BufferedImage input) throws IOException {
        int scaleFactor = 5;
        Image scaled = input.getScaledInstance(
                input.getWidth() * scaleFactor,
                input.getHeight() * scaleFactor,
                Image.SCALE_REPLICATE
        );
        BufferedImage resized = new BufferedImage(
                scaled.getWidth(null),
                scaled.getHeight(null),
                BufferedImage.TYPE_INT_RGB
        );
        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(scaled, 0, 0, null);
        g2d.dispose();

        BufferedImage grayImage = new BufferedImage(
                resized.getWidth(), resized.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(resized, 0, 0, null);
        g.dispose();

        ImageIO.write(grayImage, "png", new File("output-preprocessed.png"));

        BufferedImage afterOpenCV = doProcessorUsingOpenCV(grayImage);

        ImageIO.write(afterOpenCV, "png", new File("output-after-openCV.png"));


        return afterOpenCV;
    }

    private static BufferedImage doProcessorUsingOpenCV(BufferedImage sourceImage) {

        Mat srcMat = bufferedImageToMat(sourceImage);

        Mat binary = new Mat();
        adaptiveThreshold(srcMat, binary, 255,
                ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 15, 10);

        Mat horizontal = binary.clone();
        int horizontalSize = horizontal.cols() / 30;
        Mat horizontalStructure = getStructuringElement(MORPH_RECT, new Size(horizontalSize, 1));
        erode(horizontal, horizontal, horizontalStructure);
        dilate(horizontal, horizontal, horizontalStructure);

        Mat vertical = binary.clone();
        int verticalsize = vertical.rows() / 30;
        Mat verticalStructure = getStructuringElement(MORPH_RECT, new Size(1, verticalsize));
        erode(vertical, vertical, verticalStructure);
        dilate(vertical, vertical, verticalStructure);

        Mat lines = new Mat();
        add(horizontal, vertical, lines);

        Mat mask = new Mat();
        bitwise_not(lines, mask);

        Mat cleaned = new Mat();
        bitwise_and(binary, mask, cleaned);

        // Convert back to BufferedImage
        return matToBufferedImage(cleaned);


    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
            baos.flush();
            byte[] imageBytes = baos.toByteArray();
            baos.close();

            return imdecode(new Mat(new BytePointer(imageBytes)), IMREAD_GRAYSCALE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert BufferedImage to Mat", e);
        }
    }

    public static BufferedImage matToBufferedImage(Mat mat) {
        try (OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
             Java2DFrameConverter java2dConverter = new Java2DFrameConverter()) {
            return java2dConverter.convert(converter.convert(mat));
        } catch (Exception e) {
            return null;
        }
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
