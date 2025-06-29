package com.irfaan.efaktur.service;

import com.irfaan.efaktur.model.ResponsePayload;
import com.irfaan.efaktur.model.TestingUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
class FakturValidationServiceTest {


    @Autowired
    private FakturValidationService fakturValidationService;

    @Test
    void testProcessingFakturIsValid() {
        MultipartFile file = TestingUtil.generateFileMock();
        ResponseEntity<ResponsePayload> responsePayloadResponseEntity = fakturValidationService.processingEfaktur(file);

        Assertions.assertEquals(HttpStatus.OK, responsePayloadResponseEntity.getStatusCode());

    }

}