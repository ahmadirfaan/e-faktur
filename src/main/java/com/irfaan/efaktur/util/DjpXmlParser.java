package com.irfaan.efaktur.util;

import com.irfaan.efaktur.enums.KeyElectronicFaktur;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class DjpXmlParser {

    public static Map<KeyElectronicFaktur, String> parse(String xmlContent) throws Exception {
        Map<KeyElectronicFaktur, String> map = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xmlContent)));

        map.put(KeyElectronicFaktur.npwpPenjual, getText(doc, KeyElectronicFaktur.npwpPenjual.name()));
        map.put(KeyElectronicFaktur.namaPenjual, getText(doc, KeyElectronicFaktur.namaPenjual.name()));
        map.put(KeyElectronicFaktur.npwpPembeli, getText(doc, "npwpLawanTransaksi"));
        map.put(KeyElectronicFaktur.namaPembeli, getText(doc, "namaLawanTransaksi"));
        map.put(KeyElectronicFaktur.nomorFaktur, getText(doc, KeyElectronicFaktur.nomorFaktur.name()));
        map.put(KeyElectronicFaktur.tanggalFaktur, getText(doc, KeyElectronicFaktur.tanggalFaktur.name()));
        map.put(KeyElectronicFaktur.jumlahDpp, getText(doc, KeyElectronicFaktur.jumlahDpp.name()));
        map.put(KeyElectronicFaktur.jumlahPpn, getText(doc, KeyElectronicFaktur.jumlahPpn.name()));

        return map;
    }

    private static String getText(Document doc, String tag) {
        return doc.getElementsByTagName(tag).item(0).getTextContent();
    }
}
