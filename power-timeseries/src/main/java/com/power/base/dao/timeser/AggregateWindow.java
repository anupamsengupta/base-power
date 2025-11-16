package com.power.base.dao.timeser;

public enum AggregateWindow {
    FIFTEEN_MINUTES("15m"),
    ONE_HOUR("1h"),
    ONE_DAY("24h");

    private final String fluxInterval;

    AggregateWindow(String fluxInterval) {
        this.fluxInterval = fluxInterval;
    }

    public String getFluxInterval() {
        return fluxInterval;
    }
}


