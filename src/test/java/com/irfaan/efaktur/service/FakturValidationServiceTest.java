package com.irfaan.efaktur.service;

import com.irfaan.efaktur.model.ResponsePayload;
import com.irfaan.efaktur.model.TestingUtil;
import com.irfaan.efaktur.tessdata.TessDataLoader;
import com.irfaan.efaktur.util.FileUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

class FakturValidationServiceTest {


    private FakturValidationService fakturValidationService = new FakturValidationService();

    @BeforeEach
    void setUp() {
         FileUtil fileUtil = new FileUtil();
         fileUtil.initTessdata();
    }

    @Test
    void testProcessingFakturIsValid() {
        MultipartFile file = TestingUtil.generateFileMock();
        ResponseEntity<ResponsePayload> responsePayloadResponseEntity = fakturValidationService.processingEfaktur(file);

        Assertions.assertEquals(HttpStatus.OK, responsePayloadResponseEntity.getStatusCode());

    }

}