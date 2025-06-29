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
        result.put(KeyElectronicFaktur.npwpPenjual, match(text, "Pengusaha Kena Pajak\\s*NPWP\\s*[:：]\\s*(\\d{15})"));
        result.put(KeyElectronicFaktur.namaPenjual, match(text, "Pengusaha Kena Pajak.*?Nama\\s*[:：]\\s*(.+?)\\n"));
        result.put(KeyElectronicFaktur.npwpPembeli, match(text, "Pembeli\\s*NPWP\\s*[:：]\\s*(\\d{15})"));
        result.put(KeyElectronicFaktur.namaPembeli, match(text, "Pembeli.*?Nama\\s*[:：]\\s*(.+?)\\n"));
        result.put(KeyElectronicFaktur.nomorFaktur, match(text, "Nomor Seri Faktur Pajak\\s*[:：]\\s*(\\d{16})"));
        result.put(KeyElectronicFaktur.tanggalFaktur, match(text, "\\b\\p{L}+,?\\s+(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4})"));
        result.put(KeyElectronicFaktur.jumlahDpp, match(text, "Dasar Pengenaan Pajak\\s*[:：]\\s*([\\d.,]+)"));
        result.put(KeyElectronicFaktur.jumlahPPn, match(text, "PPN\\s*[:：]\\s*([\\d.,]+)"));

        if (result.containsKey(KeyElectronicFaktur.tanggalFaktur)) {
            result.put(KeyElectronicFaktur.tanggalFaktur, parseTanggalFaktur(result.get(KeyElectronicFaktur.tanggalFaktur)));
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
