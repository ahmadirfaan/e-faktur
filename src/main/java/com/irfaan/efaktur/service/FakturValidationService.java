package com.irfaan.efaktur.service;

import com.irfaan.efaktur.enums.DeviationType;
import com.irfaan.efaktur.enums.EFakturStatus;
import com.irfaan.efaktur.enums.KeyElectronicFaktur;
import com.irfaan.efaktur.model.DeviationData;
import com.irfaan.efaktur.model.ResponsePayload;
import com.irfaan.efaktur.model.ValidatedData;
import com.irfaan.efaktur.model.ValidationResult;
import com.irfaan.efaktur.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FakturValidationService {

    final FileUtil fileUtil;

    @Autowired
    private ThreadPoolTaskExecutor imageThreadPoolTaskExecutor;

    @Autowired
    public FakturValidationService(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }


    public ResponseEntity<ResponsePayload> processingEfaktur(MultipartFile file) {
        try {
            // Step 1: Parse PDF

            BufferedImage image = fileUtil.convertToImage(file);
            if (image == null) {
                return ResponseEntity.badRequest().body(ResponsePayload.error("pdf file is empty"));
            }

            Map<KeyElectronicFaktur, String> pdfData = new ConcurrentHashMap<>();
            List<BufferedImage> bufferedImages = fileUtil.convertInto3PartImage(image);
            List<Future<String>> futureTexts = new ArrayList<>();
            bufferedImages.forEach(
                    bufferedImage -> {
                        Future<String> futureText = imageThreadPoolTaskExecutor.submit(
                                () -> extractValueFromImage(file, bufferedImage, pdfData)
                        );
                        futureTexts.add(futureText);
                    }
            );

            List<String> texts = futureTexts.stream().map(future -> {
                try {
                    return future.get(1000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    log.error("Error processing image: " + e.getMessage());
                    return null;
                }
            }).filter(StringUtils::isNotBlank).toList();

            texts.forEach(text -> pdfData.putAll(FakturPdfParser.extractFields(text)));

            if (CollectionUtils.isEmpty(pdfData) ||
                    pdfData.values().stream().allMatch(StringUtils::isBlank)
            ) {
                return ResponseEntity.badRequest().body(ResponsePayload.error("pdf text is empty"));
            }


            Map<KeyElectronicFaktur, String> resultFromApi = generateDataFromAPI(file);
            return ResponseEntity.ok(validateElectronicFaktur(pdfData, resultFromApi));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponsePayload.error("Failed to validate faktur: " + e.getMessage()));
        }
    }

    private String extractValueFromImage(MultipartFile file, BufferedImage bufferedImage, Map<KeyElectronicFaktur, String> pdfData) {
        BufferedImage bufferedImageResize = ImagePreProcessorUtil.resizeImage(bufferedImage, 20);
        return extractImageIntoText(file.getOriginalFilename(), bufferedImageResize);

    }

    private String extractImageIntoText(String originalFileName, BufferedImage bufferedImage) {
        try {
            ImageIO.write(bufferedImage, "jpg", new File(System.currentTimeMillis() + originalFileName));
        } catch (IOException e) {
            return null;
        }
        return fileUtil.doOcr(bufferedImage);
    }

    private ResponsePayload validateElectronicFaktur(Map<KeyElectronicFaktur, String> pdfData, Map<KeyElectronicFaktur, String> resultFromApi) {

        ResponsePayload responsePayload = new ResponsePayload();
        ValidationResult validationResult = new ValidationResult();
        var validatedData = new ValidatedData();
        List<DeviationData> deviations = new ArrayList<>();

        checkForDeviation(pdfData, resultFromApi, validatedData, deviations);
        validationResult.setValidatedData(validatedData);
        validationResult.setDeviations(deviations);


        responsePayload.setValidationResults(validationResult);

        if (CollectionUtils.isEmpty(deviations)) {
            responsePayload.setStatus(EFakturStatus.VALIDATED_SUCCESSFULLY);
            responsePayload.setMessage("validated success");
        } else {
            responsePayload.setStatus(EFakturStatus.VALIDATED_WITH_DEVIATIONS);
            responsePayload.setMessage("there is deviations");
        }
        return responsePayload;
    }

    private void checkForDeviation(Map<KeyElectronicFaktur, String> pdfData, Map<KeyElectronicFaktur, String> resultFromApi, ValidatedData validatedData, List<DeviationData> deviations) {
        Arrays.stream(KeyElectronicFaktur.values()).forEach(keyElectronicFaktur -> {

            String textPdf = pdfData.get(keyElectronicFaktur);
            String resultDjp = resultFromApi.get(keyElectronicFaktur);

            if (StringUtils.isNotBlank(resultDjp)) {
                setValidatedData(validatedData, keyElectronicFaktur, resultDjp);
            }


            if (textPdf == null) {
                DeviationData deviationData = DeviationData.generateNullPdf(keyElectronicFaktur, resultDjp);
                deviations.add(deviationData);
                return;
            }

            if (resultDjp == null) {
                DeviationData deviationData = DeviationData.generateNullDjp(keyElectronicFaktur, textPdf);
                deviations.add(deviationData);
                return;
            }

            if (!textPdf.equals(resultDjp)) {
                DeviationData deviationData = new DeviationData();
                deviationData.setPdfValue(textPdf);
                deviationData.setDjpApiValue(resultDjp);
                deviationData.setDeviationType(DeviationType.MISMATCH);
                deviationData.setField(keyElectronicFaktur.name());
                deviations.add(deviationData);
            }
        });
    }

    private void setValidatedData(ValidatedData validatedData, KeyElectronicFaktur keyElectronicFaktur, String resultDjp) {

        if (KeyElectronicFaktur.tanggalFaktur == keyElectronicFaktur) {
            validatedData.setTanggalFaktur(resultDjp);
        } else if (KeyElectronicFaktur.nomorFaktur == keyElectronicFaktur) {
            validatedData.setNomorFaktur(resultDjp);
        } else if (KeyElectronicFaktur.jumlahPpn == keyElectronicFaktur) {
            validatedData.setJumlahPpn(resultDjp);
        } else if (KeyElectronicFaktur.jumlahDpp == keyElectronicFaktur) {
            validatedData.setJumlahDpp(resultDjp);
        } else if (KeyElectronicFaktur.namaPembeli == keyElectronicFaktur) {
            validatedData.setNamaLawanTransaksi(resultDjp);
        } else if (KeyElectronicFaktur.npwpPembeli == keyElectronicFaktur) {
            validatedData.setNpwpLawanTransaksi(resultDjp);
        } else if (KeyElectronicFaktur.namaPenjual == keyElectronicFaktur) {
            validatedData.setNamaPenjual(resultDjp);
        } else if (KeyElectronicFaktur.npwpPenjual == keyElectronicFaktur) {
            validatedData.setNpwpPenjual(resultDjp);
        }

    }

    private Map<KeyElectronicFaktur, String> generateDataFromAPI(MultipartFile file) {

        try {
            // QR Code extraction
            var optionalQrUrl = QrExtractor.extractQrUrl(file);
            if (optionalQrUrl.isEmpty()) {
                return Collections.emptyMap();
            }

            String url = optionalQrUrl.get();
            log.info("url text: " + url);

            //Fetch DJP XML
            String xmlContent = new RestTemplate().getForObject(url, String.class);
            //Parse XML
            return DjpXmlParser.parse(xmlContent);
        } catch (Exception e) {
            return Collections.emptyMap();
        }

    }

}
