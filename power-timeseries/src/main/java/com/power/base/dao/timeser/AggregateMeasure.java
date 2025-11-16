package com.power.base.dao.timeser;

public enum AggregateMeasure {
    FORECAST("forecastVolume"),
    ACTUAL("actualVolume");

    private final String fieldName;

    AggregateMeasure(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}


