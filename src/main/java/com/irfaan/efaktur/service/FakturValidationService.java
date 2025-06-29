package com.irfaan.efaktur.service;

import com.irfaan.efaktur.model.ResponsePayload;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class FakturValidationService {


    public ResponsePayload processingEfaktur(MultipartFile file) {
        try {
            // Step 1: Parse PDF
            Map<String, String> pdfData = PdfParser.extractFields(file.getInputStream());

            // Step 2: QR Code extraction
            String qrUrl = QrExtractor.extractQrUrl(file.getInputStream());

            // Step 3: Fetch DJP XML
            String xmlContent = new RestTemplate().getForObject(qrUrl, String.class);

            // Step 4: Parse XML
            Map<String, String> djpData = XmlParser.parse(xmlContent);

            // Step 5: Compare & prepare report
            return ValidationUtils.compareAndBuildResponse(pdfData, djpData);

        } catch (Exception e) {
            return ResponsePayload.error("Failed to validate: " + e.getMessage());
        }
    }
}
