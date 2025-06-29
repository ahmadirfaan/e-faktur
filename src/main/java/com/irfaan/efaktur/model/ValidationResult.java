package com.irfaan.efaktur.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ValidationResult {

    private List<DeviationData> deviations;

    @JsonProperty("validated_data")
    private ValidatedData validatedData;
}
