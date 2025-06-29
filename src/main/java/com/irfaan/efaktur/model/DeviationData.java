package com.irfaan.efaktur.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.irfaan.efaktur.enums.DeviationType;
import com.irfaan.efaktur.enums.KeyElectronicFaktur;
import lombok.Data;

@Data
public class DeviationData {

    private String field;

    @JsonProperty(value = "pdf_value")
    private String pdfValue;

    @JsonProperty(value = "djp_api_value")
    private String djpApiValue;

    private DeviationType deviationType;

    public static DeviationData generateNullPdf(KeyElectronicFaktur keyElectronicFaktur, String resultDjp) {
        DeviationData deviationData = new DeviationData();
        deviationData.setDeviationType(DeviationType.MISSING_IN_PDF);
        deviationData.setDjpApiValue(resultDjp);
        deviationData.setField(keyElectronicFaktur.name());
        return deviationData;
    }

    public static DeviationData generateNullDjp(KeyElectronicFaktur keyElectronicFaktur, String textPdf) {
        DeviationData deviationData = new DeviationData();
        deviationData.setDeviationType(DeviationType.MISSING_IN_API);
        deviationData.setPdfValue(textPdf);
        deviationData.setField(keyElectronicFaktur.name());
        return deviationData;
    }
}
