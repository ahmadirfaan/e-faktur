package com.irfaan.efaktur.controller;

import com.irfaan.efaktur.model.ResponsePayload;
import com.irfaan.efaktur.service.FakturValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/validate-efaktur")
@RestController
public class FakturController {

    @Autowired
    private FakturValidationService fakturValidationService;

    @PostMapping
    public ResponseEntity<ResponsePayload> validate(@RequestParam("file") MultipartFile file) {
        return fakturValidationService.processingEfaktur(file);
    }
}
