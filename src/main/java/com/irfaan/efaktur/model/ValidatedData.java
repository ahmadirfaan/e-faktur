package com.irfaan.efaktur.model;

import lombok.Data;

@Data
public class ValidatedData {

    private String npwpPenjual;

    private String namaPenjual;

    private String npwpLawanTransaksi;

    private String namaLawanTransaksi;

    private String nomorFaktur;

    private String tanggalFaktur;

    private String jumlahDpp;

    private String jumlahPpn;
}
