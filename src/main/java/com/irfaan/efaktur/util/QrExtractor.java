package com.irfaan.efaktur.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class QrExtractor {

    public static Optional<String> extractQrUrl(MultipartFile file) {
        try (BufferedInputStream bufferedStream = new BufferedInputStream(file.getInputStream())) {
            bufferedStream.mark(0x100000);

            if (!isPdfFile(bufferedStream)) {
                bufferedStream.reset();
                BufferedImage image = ImageIO.read(bufferedStream);
                if (image == null) {
                    log.error("ImageIO.read() returned null, file is not a valid image.");
                    return Optional.empty();
                }
                BufferedImage formattedImage = formatImage(image);
                return tryDecodeQrFromImage(formattedImage);
            }

            bufferedStream.reset();
            try (PDDocument document = PDDocument.load(bufferedStream)) {
                PDFRenderer renderer = new PDFRenderer(document);

                for (int page = 0; page < document.getNumberOfPages(); page++) {
                    BufferedImage image = renderer.renderImageWithDPI(page, 300);
                    image = formatImage(image);

                    Optional<String> result = tryDecodeQrFromImage(image);
                    if (result.isPresent()) {
                        return result;
                    }

                    BufferedImage formattedImage = formatImage(image);

                    result = tryDecodeQrFromImage(formattedImage);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }

        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return Optional.empty();
    }

    private static BufferedImage formatImage(BufferedImage originalImage)  {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int cropX = (int) (width * 0.03);
        int cropY = (int) (height * 0.63);
        int cropWidth = (int) (width * 0.18);
        int cropHeight = (int) (height * 0.17);

        cropWidth = Math.min(cropWidth, width - cropX);
        cropHeight = Math.min(cropHeight, height - cropY);

        BufferedImage croppedImage = originalImage.getSubimage(cropX, cropY, cropWidth, cropHeight);

        BufferedImage padded = padWithWhiteMargin(croppedImage);
        return ImagePreProcessorUtil.resizeImage(padded, 4);
    }

    private static Optional<String> tryDecodeQrFromImage(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));

            Result result = new MultiFormatReader().decode(bitmap, hints);

            return Optional.of(result.getText());
        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.info(e.getMessage());

            return Optional.empty();
        }
    }


    private static boolean isPdfFile(InputStream inputStream) throws IOException {
        inputStream.mark(4);
        byte[] header = new byte[4];
        inputStream.reset();
        return new String(header).startsWith("%PDF");
    }

    private static BufferedImage padWithWhiteMargin(BufferedImage image) {
        int newWidth = image.getWidth() + 2 * 5;
        int newHeight = image.getHeight() + 2 * 5;

        BufferedImage paddedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = paddedImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, newWidth, newHeight);
        g.drawImage(image, 5, 5, null);
        g.dispose();

        return paddedImage;
    }



}
