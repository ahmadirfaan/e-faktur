package com.irfaan.efaktur.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

class QrExtractorTest {


    @Test
    void testQRCodeFromImage() throws IOException {

        ClassPathResource resource = new ClassPathResource("mock-faktur-pajak.jpg");
        InputStream inputStream = resource.getInputStream();
        MultipartFile multipartFile = new MockMultipartFile(
                "file",                               // name of parameter
                resource.getFilename(),               // original filename
                "application/jpeg",                    // content type
                inputStream                           // content
        );

        Optional<String> optionalUrl = QrExtractor.extractQrUrl(multipartFile);

        Assertions.assertTrue(optionalUrl.isPresent());
        Assertions.assertTrue(StringUtils.isNotBlank(optionalUrl.get()));
        Assertions.assertEquals("http://svc.efaktur.pajak.go.id/validasi/faktur/approvalCode/527d5baf11452b2a424b8b899e549f99426cc89fe072d84cac822e58bdf8bb56", optionalUrl.get());
    }
}