package com.irfaan.efaktur.util;

import com.irfaan.efaktur.enums.KeyElectronicFaktur;
import org.apache.commons.lang3.StringUtils;

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

        result.put(KeyElectronicFaktur.namaPenjual,
                match(text, "Pengusaha Kena Pajak\\s+Nama\\s*[:：]\\s*(.*?)(?=\\s+Alamat)"));

        result.put(KeyElectronicFaktur.npwpPenjual,
                match(text, "NPWP\\s*[:：]\\s*(\\d{2}\\.\\d{3}\\.\\d{3}\\.\\d-\\d{3}\\.\\d{3})"));

        result.put(KeyElectronicFaktur.namaPembeli,
                match(text, "Pembeli.*?Nama\\s*[:：]\\s*(.*?)(?=\\s+Alamat)"));

        result.put(KeyElectronicFaktur.npwpPembeli,
                match(text, "Pembeli.*?NPWP\\s*[:：]\\s*(\\d{2}\\.\\d{3}\\.\\d{3}\\.\\d-\\d{3}\\.\\d{3})"));

        result.put(KeyElectronicFaktur.tanggalFaktur,
                match(text, "[A-Za-z ]+,\\s*(\\d{1,2}\\s+[A-Z][a-z]+\\s+\\d{4})"));


        result.put(KeyElectronicFaktur.jumlahDpp,
                cleanCurrency(match(text, "Dasar Pengenaan Pajak\\s*(Rp\\s*)?([\\d\\.]+,[\\d]{2})")));

        result.put(KeyElectronicFaktur.jumlahPpn,
                cleanCurrency(match(text, "Total\\s+PPN\\s*(Rp\\s*)?([\\d\\.]+,[\\d]{2})")));

        result.computeIfPresent(KeyElectronicFaktur.tanggalFaktur,
                (key, value) -> parseTanggalFaktur(value));

        result.computeIfPresent(KeyElectronicFaktur.npwpPembeli,
                (key, value) -> onlyDigit(value));

        result.computeIfPresent(KeyElectronicFaktur.npwpPenjual,
                (key, value) -> onlyDigit(value));

        result.computeIfPresent(KeyElectronicFaktur.nomorFaktur,
                (key, value) -> onlyDigit(value));

        result.computeIfPresent(KeyElectronicFaktur.jumlahPpn, (key, value) -> cleanCurrency(value));

        result.computeIfPresent(KeyElectronicFaktur.jumlahDpp, (key, value) -> cleanCurrency(value));

        return result;
    }

    private static String cleanCurrency(String value) {
        if (value == null) return null;
        return value.replace(".", "").replace(",00", "");
    }

    private static String onlyDigit(String value) {
        return StringUtils.getDigits(value);
    }

    private static String match(String text, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(m.groupCount()).trim(); // ambil group terakhir
        }
        return null;
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
