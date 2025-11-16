package com.power.base.dao.timeser;

import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesPointDto;

import java.time.Instant;
import java.util.List;

/**
 * Service contract for retrieving "materialized window" views of volume forecast series,
 * backed by a cache with InfluxDB as the source of truth.
 */
public interface VolumeForecastWindowService {

    /**
     * Returns a window of 15-minute volume points for the given forecast and time range.
     * Implementations may serve data from a cache and fall back to InfluxDB on a cache miss.
     */
    List<VolumeForecastSeriesPointDto> getWindow(String tenantId,
                                                 String forecastId,
                                                 Instant startInclusive,
                                                 Instant endExclusive);

    /**
     * Evicts any cached window for the given forecast and time range, forcing a reload
     * from the backing store on the next access.
     */
    void evictWindow(String tenantId,
                     String forecastId,
                     Instant startInclusive,
                     Instant endExclusive);
}


