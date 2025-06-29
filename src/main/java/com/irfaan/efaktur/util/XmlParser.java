package com.irfaan.efaktur.util;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlParser {

    public static Map<String, String> parse(String xmlContent) throws Exception {
        Map<String, String> map = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xmlContent)));

        map.put("npwpPenjual", getText(doc, "npwpPenjual"));
        map.put("namaPenjual", getText(doc, "namaPenjual"));
        // ... dan seterusnya

        return map;
    }

    private static String getText(Document doc, String tag) {
        return doc.getElementsByTagName(tag).item(0).getTextContent();
    }
}
