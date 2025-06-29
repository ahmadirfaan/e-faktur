package com.irfaan.efaktur.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.irfaan.efaktur.enums.EFakturStatus;
import lombok.Data;

import java.util.List;

@Data
public class ResponsePayload {

    private EFakturStatus status;

    private String message;

    @JsonProperty("validation_results")
    private List<ValidationResult> validationResults;


}
