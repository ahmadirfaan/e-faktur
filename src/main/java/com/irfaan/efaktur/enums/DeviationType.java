package com.irfaan.efaktur.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import com.irfaan.efaktur.util.EnumUtil;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum DeviationType {

    MISMATCH, MISSING_IN_PDF, MISSING_IN_API;

    @JsonValue
    public String toJson() {
        // Convert enum name to snake_case
        return EnumUtil.toSnakeCase(this.name());
    }

}
