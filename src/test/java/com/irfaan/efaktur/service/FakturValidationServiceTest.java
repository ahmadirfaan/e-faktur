package com.irfaan.efaktur.service;

import com.irfaan.efaktur.enums.EFakturStatus;
import com.irfaan.efaktur.model.ResponsePayload;
import com.irfaan.efaktur.model.TestingUtil;
import com.irfaan.efaktur.util.FileUtil;
import com.irfaan.efaktur.util.QrExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@SpringBootTest
class FakturValidationServiceTest {


    @Autowired
    private FakturValidationService fakturValidationService;

    @MockBean
    private FileUtil fileUtil;

    @Test
    void testProcessingFakturFromMock() throws Exception {
        MultipartFile file = TestingUtil.generateFileMock("mock-faktur-pajak.jpg");
        String text = """
                Kode dan Nomor Seri Faktur Pajak : 070.000-22.12345678
                Pengusaha Kena Pajak
                Nama : PT ABC
                Alamat: Jalan Gatot Subroto No. 40A, Senayan, Kebayoran Baru, Jakarta Selatan 12910
                NPWP : 01.234.567.8-012.000

                
                Pembeli Barang Kena Pajak / Penerima Jasa Kena Pajak
                Nama : PT XYZ
                Alamat: Jalan Kuda Laut Nomer 1, Sungai Jodoh, Batu Ampar, Batam, 29444
                NPWP : 02.345.678.9-217.000
                No. Nama Barang Kena Pajak / Jasa Kena Pajak Harga Jual Penggantian / Uang Muka/Uang Termin
                
                1 KOMPUTER MERK ABC, HS Code 84714110 15.000.000,00
                Harga Jual/Penggantian / Uang Termin 15.000.000,00
                Dikurangi Potongan Harga 0,00
                Dikurangi Uang Muka yang Telah Diterima 0,00
                Dasar Pengenaan Pajak Rp 15.000.000,00
                Total PPN 1.650.000,00
                Jakarta Selatan, 1 April 2022
                
                Slamet Aman Sentosa
                """;

        Mockito.when(fileUtil.readFile(Mockito.any()))
                .thenReturn(text);
        try (MockedStatic<QrExtractor> qrExtractorMockedStatic = Mockito.mockStatic(QrExtractor.class)) {
            qrExtractorMockedStatic.when(() -> QrExtractor.extractQrUrl(Mockito.any(MultipartFile.class)))
                    .thenReturn(Optional.of("http://localhost:3000/mock-online-pajak"));

            ResponseEntity<ResponsePayload> responsePayloadResponseEntity = fakturValidationService.processingEfaktur(file);

            Assertions.assertEquals(HttpStatus.OK, responsePayloadResponseEntity.getStatusCode());
            ResponsePayload body = responsePayloadResponseEntity.getBody();
            Assertions.assertEquals(EFakturStatus.VALIDATED_SUCCESSFULLY, body.getStatus());
        }


    }

}