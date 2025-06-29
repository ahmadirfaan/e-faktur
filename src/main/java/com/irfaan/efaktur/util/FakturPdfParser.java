package com.irfaan.efaktur.util;

import com.irfaan.efaktur.enums.KeyElectronicFaktur;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FakturPdfParser {

    public static Map<KeyElectronicFaktur, String> extractFields(String text) {
        Map<KeyElectronicFaktur, String> result = new HashMap<>();

        result.put(KeyElectronicFaktur.nomorFaktur,
                match(text, "Kode dan Nomor Seri Faktur Pajak\\s*[:：]\\s*(\\d{3}\\.\\d{3}-\\d{2}\\.\\d{8})"));

        result.put(KeyElectronicFaktur.npwpPenjual,
                match(text, "NPWP\\s*[:：]\\s*(\\d{2}\\.\\d{3}\\.\\d{3}\\.\\d-\\d{3}\\.\\d{3})"));

        result.put(KeyElectronicFaktur.namaPenjual,
                match(text, "Nama\\s*[:：]\\s*(PT\\s+.*?)(?=\\s*Alamat)"));

        result.put(KeyElectronicFaktur.npwpPembeli,
                match(text, "Pembeli.*?NPWP\\s*[:：]\\s*(\\d{2}\\.\\d{3}\\.\\d{3}\\.\\d-\\d{3}\\.\\d{3})"));

        result.put(KeyElectronicFaktur.namaPembeli,
                match(text, "Pembeli.*?Nama\\s*[:：]\\s*(PT\\s+.*?)(?=\\s+NIK|\\s+Alamat|\\n)"));

        result.put(KeyElectronicFaktur.tanggalFaktur,
                match(text, "[A-Z ]+,\\s*(\\d{1,2}\\s+[A-Z]+\\s+\\d{4})"));

        result.put(KeyElectronicFaktur.jumlahDpp,
                match(text, "\\b(Rp|RP)?\\s?([\\d\\.]+,[\\d]{2})\\b.*?(?=\\s*Dikurangi|\\s*PPN|\\s*Total)"));

        result.put(KeyElectronicFaktur.jumlahPPn,
                match(text, "Total\\s*PPN\\s*:?\\s*([\\d\\.]+,[\\d]{2})"));

        if (result.containsKey(KeyElectronicFaktur.tanggalFaktur)) {
            result.put(KeyElectronicFaktur.tanggalFaktur,
                    parseTanggalFaktur(result.get(KeyElectronicFaktur.tanggalFaktur)));
        }

        return result;
    }

    private static String match(String text, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : null;
    }

    private static String parseTanggalFaktur(String rawTanggal) {
        try {
            DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID"));
            DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate date = LocalDate.parse(rawTanggal, inputFormat);
            return date.format(outputFormat);
        } catch (Exception e) {
            return null;
        }
    }

}
