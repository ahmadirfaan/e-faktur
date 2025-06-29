package com.irfaan.efaktur.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.irfaan.efaktur.enums.DeviationType;
import lombok.Data;

@Data
public class DeviationData {

    private String field;

    @JsonProperty(value = "pdf_value")
    private String pdfValue;

    @JsonProperty(value = "djp_api_value")
    private String djpApiValue;

    private DeviationType deviationType;
}
