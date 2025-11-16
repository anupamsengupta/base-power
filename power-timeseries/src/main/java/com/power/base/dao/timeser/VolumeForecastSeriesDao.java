package com.power.base.dao.timeser;

import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesDto;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesPointDto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VolumeForecastSeriesDao {

    void save(VolumeForecastSeriesDto series);

    void saveAll(List<VolumeForecastSeriesDto> seriesList);

    Optional<VolumeForecastSeriesDto> findSeries(String tenantId,
                                                 String forecastId,
                                                 Instant startInclusive,
                                                 Instant endExclusive);

    List<VolumeForecastSeriesPointDto> queryRange(String tenantId,
                                                  String forecastId,
                                                  Instant startInclusive,
                                                  Instant endExclusive);

    List<VolumeForecastSeriesPointDto> queryAggregated(String tenantId,
                                                       String forecastId,
                                                       Instant startInclusive,
                                                       Instant endExclusive,
                                                       AggregateWindow window,
                                                       AggregateFunction function,
                                                       AggregateMeasure measure);

    List<VolumeForecastSeriesPointDto> queryByTags(String tenantId,
                                                   Map<String, String> tagFilters,
                                                   Instant startInclusive,
                                                   Instant endExclusive);

    void deleteSeries(String tenantId,
                      String forecastId,
                      Instant startInclusive,
                      Instant endExclusive);

    boolean health();

    void ensureBucket();
}


