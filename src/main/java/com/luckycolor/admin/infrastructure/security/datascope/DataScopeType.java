package com.luckycolor.admin.infrastructure.security.datascope;

import java.util.Locale;

public enum DataScopeType {
    ALL,
    TENANT,
    DEPARTMENT,
    DEPARTMENT_AND_CHILDREN,
    CUSTOM;

    public static DataScopeType from(String value) {
        if (value == null || value.isBlank()) {
            return TENANT;
        }
        try {
            return DataScopeType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return TENANT;
        }
    }
}
