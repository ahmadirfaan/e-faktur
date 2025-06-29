package com.irfaan.efaktur.util;

import org.apache.commons.lang3.StringUtils;

public class EnumUtil {

    public static String toSnakeCase(String input) {
        return StringUtils.join(
                StringUtils.splitByCharacterTypeCamelCase(input),
                "_"
        ).toLowerCase();
    }
}
