package com.irfaan.efaktur.tessdata;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Slf4j
public class TessDataLoader {

    public static File prepareTessdata() throws IOException {
        Path tempDir = Files.createTempDirectory("tessdata-");
        Path tessdataDir = tempDir.resolve("tessdata");
        Files.createDirectories(tessdataDir);

        copyResourceToFile("tessdata/ind.traineddata", tessdataDir.resolve("ind.traineddata"));

        return tessdataDir.toFile();
    }

    private static void copyResourceToFile(String resourcePath, Path outputPath) throws IOException {
        try (InputStream in = TessDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found: " + resourcePath);
            }
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Copied " + resourcePath + " to " + outputPath);
        }
    }


}
