package com.power.base.datamodel.dto.timeser;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a tenant-aware renewable power production or load forecast series,
 * including asset metadata and granular interval level projections.
 */
public class VolumeForecastSeriesDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tenantId;
    private String forecastId;
    private String externalReference;
    private String assetId;
    private String assetName;
    private String assetType;
    private String isoRegion;
    private String marketZone;
    private String balancingAuthority;
    private String meterId;
    private String meterName;
    private String location;
    private String timeZone;
    private String volumeUom;
    private LocalDate forecastDate;
    private Instant createdTime;
    private Instant lastUpdatedTime;
    private String forecaster;
    private List<VolumeForecastSeriesPointDto> dataPoints = new ArrayList<>();

    public VolumeForecastSeriesDto() {
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getForecastId() {
        return forecastId;
    }

    public void setForecastId(String forecastId) {
        this.forecastId = forecastId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getIsoRegion() {
        return isoRegion;
    }

    public void setIsoRegion(String isoRegion) {
        this.isoRegion = isoRegion;
    }

    public String getMarketZone() {
        return marketZone;
    }

    public void setMarketZone(String marketZone) {
        this.marketZone = marketZone;
    }

    public String getBalancingAuthority() {
        return balancingAuthority;
    }

    public void setBalancingAuthority(String balancingAuthority) {
        this.balancingAuthority = balancingAuthority;
    }

    public String getMeterId() {
        return meterId;
    }

    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }

    public String getMeterName() {
        return meterName;
    }

    public void setMeterName(String meterName) {
        this.meterName = meterName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getVolumeUom() {
        return volumeUom;
    }

    public void setVolumeUom(String volumeUom) {
        this.volumeUom = volumeUom;
    }

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Instant lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getForecaster() {
        return forecaster;
    }

    public void setForecaster(String forecaster) {
        this.forecaster = forecaster;
    }

    public List<VolumeForecastSeriesPointDto> getDataPoints() {
        return Collections.unmodifiableList(dataPoints);
    }

    public void setDataPoints(List<VolumeForecastSeriesPointDto> dataPoints) {
        this.dataPoints = dataPoints == null ? new ArrayList<>() : new ArrayList<>(dataPoints);
    }

    public void addDataPoint(VolumeForecastSeriesPointDto point) {
        if (point != null) {
            this.dataPoints.add(point);
        }
    }

    public void clearDataPoints() {
        this.dataPoints.clear();
    }
}

