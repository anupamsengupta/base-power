package com.power.base.datamodel.dto.common;

import java.io.Serializable;

public enum DocumentType implements Serializable {
    TRADE("Trade"),
    CONFIRMATION("Confirmation"),
    INVOICE("Invoice");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static DocumentType fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        for (DocumentType type : values()) {
            if (type.value.equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown document type value: " + value);
    }
}

