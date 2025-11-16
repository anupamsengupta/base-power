package com.power.base.datamodel.dto.timeser;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents a single interval forecast (and optional actual) reading for a renewable asset.
 */
public class VolumeForecastSeriesPointDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate intervalDate;
    private Instant intervalStartTime;
    private Instant intervalEndTime;
    private double forecastVolume;
    private double actualVolume;
    private String measurementUom;
    private String meterReadingStatus;
    private Double confidenceLowerBound;
    private Double confidenceUpperBound;
    private Double temperature;
    private String weatherCondition;

    public VolumeForecastSeriesPointDto() {
    }

    public LocalDate getIntervalDate() {
        return intervalDate;
    }

    public void setIntervalDate(LocalDate intervalDate) {
        this.intervalDate = intervalDate;
    }

    public Instant getIntervalStartTime() {
        return intervalStartTime;
    }

    public void setIntervalStartTime(Instant intervalStartTime) {
        this.intervalStartTime = intervalStartTime;
    }

    public Instant getIntervalEndTime() {
        return intervalEndTime;
    }

    public void setIntervalEndTime(Instant intervalEndTime) {
        this.intervalEndTime = intervalEndTime;
    }

    public double getForecastVolume() {
        return forecastVolume;
    }

    public void setForecastVolume(double forecastVolume) {
        this.forecastVolume = forecastVolume;
    }

    public double getActualVolume() {
        return actualVolume;
    }

    public void setActualVolume(double actualVolume) {
        this.actualVolume = actualVolume;
    }

    public String getMeasurementUom() {
        return measurementUom;
    }

    public void setMeasurementUom(String measurementUom) {
        this.measurementUom = measurementUom;
    }

    public String getMeterReadingStatus() {
        return meterReadingStatus;
    }

    public void setMeterReadingStatus(String meterReadingStatus) {
        this.meterReadingStatus = meterReadingStatus;
    }

    public Double getConfidenceLowerBound() {
        return confidenceLowerBound;
    }

    public void setConfidenceLowerBound(Double confidenceLowerBound) {
        this.confidenceLowerBound = confidenceLowerBound;
    }

    public Double getConfidenceUpperBound() {
        return confidenceUpperBound;
    }

    public void setConfidenceUpperBound(Double confidenceUpperBound) {
        this.confidenceUpperBound = confidenceUpperBound;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public void setWeatherCondition(String weatherCondition) {
        this.weatherCondition = weatherCondition;
    }
}

