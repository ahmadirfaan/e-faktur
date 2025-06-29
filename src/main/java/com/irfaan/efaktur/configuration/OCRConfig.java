package com.irfaan.efaktur.configuration;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Configuration
@Slf4j
public class OCRConfig {

    @Bean(value = "tesseract")
    public Tesseract tesseract(@Qualifier(value = "tessDataDir") File tessDataDir) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataDir.getAbsolutePath());
        tesseract.setLanguage("ind");
        tesseract.setOcrEngineMode(1);
        tesseract.setTessVariable("tessedit_pageseg_mode", "6");
        return tesseract;
    }

    @Bean(value = "tessDataDir")
    public File initTessdata() throws IOException {
        Path tempDir = Files.createTempDirectory("tessdata-");
        Path tessdataDir = tempDir.resolve("tessdata");
        Files.createDirectories(tessdataDir);

        copyResourceToFile(tessdataDir.resolve("ind.traineddata"));

        return tessdataDir.toFile();
    }

    private static void copyResourceToFile(Path outputPath) throws IOException {
        try (InputStream in = OCRConfig.class.getClassLoader().getResourceAsStream("tessdata/ind.traineddata")) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + "tessdata/ind.traineddata");
            }
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied " + "tessdata/ind.traineddata" + " to " + outputPath);
        }
    }
}
