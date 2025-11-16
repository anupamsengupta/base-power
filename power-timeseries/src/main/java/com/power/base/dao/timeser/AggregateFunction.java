package com.power.base.dao.timeser;

public enum AggregateFunction {
    SUM("sum"),
    MEAN("mean"),
    MIN("min"),
    MAX("max");

    private final String fluxFunction;

    AggregateFunction(String fluxFunction) {
        this.fluxFunction = fluxFunction;
    }

    public String getFluxFunction() {
        return fluxFunction;
    }
}


