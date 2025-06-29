package com.irfaan.efaktur.model;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class TestingUtil {

    public static MultipartFile generateFileMock() {
        ClassPathResource resource = new ClassPathResource("mock-faktur-pajak.jpg");
        InputStream inputStream;
        try {
            inputStream = resource.getInputStream();
            return new MockMultipartFile(
                    "file",                               // name of parameter
                    resource.getFilename(),               // original filename
                    "application/jpeg",                    // content type
                    inputStream                           // content
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
