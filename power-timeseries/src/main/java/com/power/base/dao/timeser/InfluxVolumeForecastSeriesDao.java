package com.power.base.dao.timeser;

import com.influxdb.client.BucketsApi;
import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.OrganizationsApi;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesDto;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesPointDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@ConditionalOnBean(InfluxDBClient.class)
public class InfluxVolumeForecastSeriesDao implements VolumeForecastSeriesDao {

    private static final String MEASUREMENT = "volume_forecast";
    private static final String FIELD_FORECAST_VOLUME = "forecastVolume";
    private static final String FIELD_ACTUAL_VOLUME = "actualVolume";
    private static final String FIELD_CONFIDENCE_LOW = "confidenceLowerBound";
    private static final String FIELD_CONFIDENCE_HIGH = "confidenceUpperBound";
    private static final String FIELD_TEMPERATURE = "temperature";
    private static final String FIELD_INTERVAL_END = "intervalEndTimeMs";
    private static final String FIELD_CREATED = "createdTimeMs";
    private static final String FIELD_UPDATED = "lastUpdatedTimeMs";

    private final InfluxDBClient influxDBClient;
    private final QueryApi queryApi;
    private final DeleteApi deleteApi;
    private final BucketsApi bucketsApi;
    private final OrganizationsApi organizationsApi;
    private final String bucket;
    private final String org;

    public InfluxVolumeForecastSeriesDao(InfluxDBClient influxDBClient,
                                         InfluxTimeSeriesProperties properties) {
        this.influxDBClient = influxDBClient;
        this.queryApi = influxDBClient.getQueryApi();
        this.deleteApi = influxDBClient.getDeleteApi();
        this.bucketsApi = influxDBClient.getBucketsApi();
        this.organizationsApi = influxDBClient.getOrganizationsApi();
        this.bucket = properties.getBucket();
        this.org = properties.getOrg();
    }

    @Override
    public void save(VolumeForecastSeriesDto series) {
        if (series == null) {
            throw new InfluxTimeSeriesDaoException("Series payload must not be null");
        }
        if (CollectionUtils.isEmpty(series.getDataPoints())) {
            throw new InfluxTimeSeriesDaoException("At least one data point is required to persist a series");
        }
        try {
            Instant created = Optional.ofNullable(series.getCreatedTime()).orElseGet(Instant::now);
            Instant updated = Optional.ofNullable(series.getLastUpdatedTime()).orElseGet(Instant::now);
            List<Point> points = series.getDataPoints().stream()
                    .filter(Objects::nonNull)
                    .map(point -> buildPoint(series, point, created, updated))
                    .collect(Collectors.toList());

            WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();
            writeApi.writePoints(bucket, org, points);
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to persist volume forecast series " + series.getForecastId(), ex);
        }
    }

    @Override
    public void saveAll(List<VolumeForecastSeriesDto> seriesList) {
        if (CollectionUtils.isEmpty(seriesList)) {
            return;
        }
        try {
            List<Point> batchPoints = new ArrayList<>();
            for (VolumeForecastSeriesDto series : seriesList) {
                if (series == null || CollectionUtils.isEmpty(series.getDataPoints())) {
                    continue;
                }
                Instant created = Optional.ofNullable(series.getCreatedTime()).orElseGet(Instant::now);
                Instant updated = Optional.ofNullable(series.getLastUpdatedTime()).orElseGet(Instant::now);
                batchPoints.addAll(series.getDataPoints().stream()
                        .filter(Objects::nonNull)
                        .map(point -> buildPoint(series, point, created, updated))
                        .collect(Collectors.toList()));
            }
            if (!batchPoints.isEmpty()) {
                influxDBClient.getWriteApiBlocking().writePoints(bucket, org, batchPoints);
            }
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to persist batch volume forecast series", ex);
        }
    }

    @Override
    public Optional<VolumeForecastSeriesDto> findSeries(String tenantId,
                                                        String forecastId,
                                                        Instant startInclusive,
                                                        Instant endExclusive) {
        validateKey(tenantId, "tenantId");
        validateKey(forecastId, "forecastId");
        String flux = buildQuery(tenantId, forecastId, startInclusive, endExclusive);
        try {
            List<FluxTable> tables = queryApi.query(flux, org);
            List<FluxRecord> records = tables.stream()
                    .filter(Objects::nonNull)
                    .flatMap(table -> table.getRecords().stream())
                    .sorted(Comparator.comparing(FluxRecord::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());

            if (records.isEmpty()) {
                return Optional.empty();
            }
            VolumeForecastSeriesDto dto = mapSeries(records);
            return Optional.of(dto);
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to query series " + forecastId, ex);
        }
    }

    @Override
    public List<VolumeForecastSeriesPointDto> queryRange(String tenantId,
                                                         String forecastId,
                                                         Instant startInclusive,
                                                         Instant endExclusive) {
        validateKey(tenantId, "tenantId");
        validateKey(forecastId, "forecastId");
        String flux = buildQuery(tenantId, forecastId, startInclusive, endExclusive);
        return executeQuery(flux).stream()
                .map(this::mapPoint)
                .collect(Collectors.toList());
    }

    @Override
    public List<VolumeForecastSeriesPointDto> queryAggregated(String tenantId,
                                                              String forecastId,
                                                              Instant startInclusive,
                                                              Instant endExclusive,
                                                              AggregateWindow window,
                                                              AggregateFunction function,
                                                              AggregateMeasure measure) {
        validateKey(tenantId, "tenantId");
        validateKey(forecastId, "forecastId");
        if (window == null || function == null || measure == null) {
            throw new InfluxTimeSeriesDaoException("Aggregation window, function and measure must be provided");
        }
        Instant start = Optional.ofNullable(startInclusive).orElse(Instant.EPOCH);
        Instant end = Optional.ofNullable(endExclusive).orElse(Instant.now());
        String flux = String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s) "
                        + "|> filter(fn: (r) => r._measurement == \"%s\" and r.tenantId == \"%s\" and r.forecastId == \"%s\") "
                        + "|> filter(fn: (r) => r._field == \"%s\") "
                        + "|> aggregateWindow(every: %s, fn: %s, createEmpty: false) "
                        + "|> rename(columns: {_value: \"%s\"})",
                bucket,
                fluxTime(start),
                fluxTime(end),
                MEASUREMENT,
                escapePredicateValue(tenantId),
                escapePredicateValue(forecastId),
                measure.getFieldName(),
                window.getFluxInterval(),
                function.getFluxFunction(),
                measure.getFieldName());

        return executeQuery(flux).stream()
                .map(record -> mapAggregatedPoint(record, measure))
                .collect(Collectors.toList());
    }

    @Override
    public List<VolumeForecastSeriesPointDto> queryByTags(String tenantId,
                                                          Map<String, String> tagFilters,
                                                          Instant startInclusive,
                                                          Instant endExclusive) {
        validateKey(tenantId, "tenantId");
        Instant start = Optional.ofNullable(startInclusive).orElse(Instant.EPOCH);
        Instant end = Optional.ofNullable(endExclusive).orElse(Instant.now());
        StringBuilder flux = new StringBuilder(String.format(
                "from(bucket:\"%s\") |> range(start: %s, stop: %s) "
                        + "|> filter(fn: (r) => r._measurement == \"%s\" and r.tenantId == \"%s\")",
                bucket,
                fluxTime(start),
                fluxTime(end),
                MEASUREMENT,
                escapePredicateValue(tenantId)));
        if (tagFilters != null) {
            tagFilters.forEach((key, value) -> {
                if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
                    flux.append(" |> filter(fn: (r) => r[\"")
                            .append(key)
                            .append("\"] == \"")
                            .append(escapePredicateValue(value))
                            .append("\")");
                }
            });
        }
        flux.append(" |> pivot(rowKey:[\"_time\"], columnKey:[\"_field\"], valueColumn:\"_value\") |> sort(columns:[\"_time\"])");
        return executeQuery(flux.toString()).stream()
                .map(this::mapPoint)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSeries(String tenantId,
                             String forecastId,
                             Instant startInclusive,
                             Instant endExclusive) {
        validateKey(tenantId, "tenantId");
        validateKey(forecastId, "forecastId");
        Instant start = Optional.ofNullable(startInclusive).orElse(Instant.EPOCH);
        Instant end = Optional.ofNullable(endExclusive).orElse(Instant.now().plusSeconds(315360000L)); // +10y
        String predicate = String.format("_measurement=\"%s\" AND tenantId=\"%s\" AND forecastId=\"%s\"",
                MEASUREMENT,
                escapePredicateValue(tenantId),
                escapePredicateValue(forecastId));
        try {
            deleteApi.delete(toOffset(start), toOffset(end), predicate, bucket, org);
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to delete series " + forecastId, ex);
        }
    }

    @Override
    public boolean health() {
        try {
            return influxDBClient.ping();
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void ensureBucket() {
        try {
            Bucket existing = bucketsApi.findBucketByName(bucket);
            if (existing != null) {
                return;
            }
            Organization organization = resolveOrganization();
            if (organization == null || !StringUtils.hasText(organization.getId())) {
                throw new InfluxTimeSeriesDaoException("Unable to resolve InfluxDB organization " + org);
            }
            bucketsApi.createBucket(bucket, organization.getId());
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to ensure bucket " + bucket, ex);
        }
    }

    private Point buildPoint(VolumeForecastSeriesDto series,
                             VolumeForecastSeriesPointDto pointDto,
                             Instant created,
                             Instant updated) {
        if (pointDto.getIntervalStartTime() == null) {
            throw new InfluxTimeSeriesDaoException("Interval start time is required for every data point");
        }
        Point point = Point.measurement(MEASUREMENT)
                .time(pointDto.getIntervalStartTime(), WritePrecision.NS);

        addTag(point, "tenantId", series.getTenantId());
        addTag(point, "forecastId", series.getForecastId());
        addTag(point, "externalReference", series.getExternalReference());
        addTag(point, "assetId", series.getAssetId());
        addTag(point, "assetName", series.getAssetName());
        addTag(point, "assetType", series.getAssetType());
        addTag(point, "isoRegion", series.getIsoRegion());
        addTag(point, "marketZone", series.getMarketZone());
        addTag(point, "balancingAuthority", series.getBalancingAuthority());
        addTag(point, "meterId", series.getMeterId());
        addTag(point, "meterName", series.getMeterName());
        addTag(point, "location", series.getLocation());
        addTag(point, "timeZone", series.getTimeZone());
        addTag(point, "volumeUom", series.getVolumeUom());
        addTag(point, "forecaster", series.getForecaster());
        addTag(point, "forecastDate", formatDate(series.getForecastDate()));

        addTag(point, "intervalDate", formatDate(pointDto.getIntervalDate()));
        addTag(point, "measurementUom", pointDto.getMeasurementUom());
        addTag(point, "meterReadingStatus", pointDto.getMeterReadingStatus());
        addTag(point, "weatherCondition", pointDto.getWeatherCondition());

        point.addField(FIELD_FORECAST_VOLUME, pointDto.getForecastVolume());
        point.addField(FIELD_ACTUAL_VOLUME, pointDto.getActualVolume());
        addField(point, FIELD_CONFIDENCE_LOW, pointDto.getConfidenceLowerBound());
        addField(point, FIELD_CONFIDENCE_HIGH, pointDto.getConfidenceUpperBound());
        addField(point, FIELD_TEMPERATURE, pointDto.getTemperature());
        if (pointDto.getIntervalEndTime() != null) {
            point.addField(FIELD_INTERVAL_END, toEpochMillis(pointDto.getIntervalEndTime()));
        }
        point.addField(FIELD_CREATED, toEpochMillis(created));
        point.addField(FIELD_UPDATED, toEpochMillis(updated));

        return point;
    }

    private VolumeForecastSeriesDto mapSeries(List<FluxRecord> records) {
        FluxRecord head = records.get(0);
        VolumeForecastSeriesDto dto = new VolumeForecastSeriesDto();
        dto.setTenantId(getString(head, "tenantId"));
        dto.setForecastId(getString(head, "forecastId"));
        dto.setExternalReference(getString(head, "externalReference"));
        dto.setAssetId(getString(head, "assetId"));
        dto.setAssetName(getString(head, "assetName"));
        dto.setAssetType(getString(head, "assetType"));
        dto.setIsoRegion(getString(head, "isoRegion"));
        dto.setMarketZone(getString(head, "marketZone"));
        dto.setBalancingAuthority(getString(head, "balancingAuthority"));
        dto.setMeterId(getString(head, "meterId"));
        dto.setMeterName(getString(head, "meterName"));
        dto.setLocation(getString(head, "location"));
        dto.setTimeZone(getString(head, "timeZone"));
        dto.setVolumeUom(getString(head, "volumeUom"));
        dto.setForecaster(getString(head, "forecaster"));
        dto.setForecastDate(parseDate(getString(head, "forecastDate")));
        dto.setCreatedTime(instantFromField(head, FIELD_CREATED));
        dto.setLastUpdatedTime(instantFromField(head, FIELD_UPDATED));

        List<VolumeForecastSeriesPointDto> points = records.stream()
                .map(this::mapPoint)
                .collect(Collectors.toList());
        dto.setDataPoints(points);
        return dto;
    }

    private VolumeForecastSeriesPointDto mapPoint(FluxRecord record) {
        VolumeForecastSeriesPointDto point = new VolumeForecastSeriesPointDto();
        point.setIntervalDate(parseDate(getString(record, "intervalDate")));
        point.setIntervalStartTime(record.getTime());
        point.setIntervalEndTime(instantFromField(record, FIELD_INTERVAL_END));
        point.setMeasurementUom(getString(record, "measurementUom"));
        point.setMeterReadingStatus(getString(record, "meterReadingStatus"));
        point.setWeatherCondition(getString(record, "weatherCondition"));
        point.setForecastVolume(doubleValue(record, FIELD_FORECAST_VOLUME, 0d));
        point.setActualVolume(doubleValue(record, FIELD_ACTUAL_VOLUME, 0d));
        point.setConfidenceLowerBound(doubleObject(record, FIELD_CONFIDENCE_LOW));
        point.setConfidenceUpperBound(doubleObject(record, FIELD_CONFIDENCE_HIGH));
        point.setTemperature(doubleObject(record, FIELD_TEMPERATURE));
        return point;
    }

    private VolumeForecastSeriesPointDto mapAggregatedPoint(FluxRecord record, AggregateMeasure measure) {
        VolumeForecastSeriesPointDto point = new VolumeForecastSeriesPointDto();
        Instant time = record.getTime();
        point.setIntervalStartTime(time);
        if (time != null) {
            point.setIntervalDate(time.atZone(ZoneOffset.UTC).toLocalDate());
        }
        Double value = doubleObject(record, measure.getFieldName());
        if (measure == AggregateMeasure.ACTUAL) {
            point.setActualVolume(value == null ? 0d : value);
        } else {
            point.setForecastVolume(value == null ? 0d : value);
        }
        return point;
    }

    private List<FluxRecord> executeQuery(String flux) {
        try {
            List<FluxTable> tables = queryApi.query(flux, org);
            if (tables == null) {
                return Collections.emptyList();
            }
            return tables.stream()
                    .filter(Objects::nonNull)
                    .flatMap(table -> table.getRecords().stream())
                    .sorted(Comparator.comparing(FluxRecord::getTime, Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to execute Flux query", ex);
        }
    }

    private String buildQuery(String tenantId, String forecastId, Instant start, Instant end) {
        Instant startTime = Optional.ofNullable(start).orElse(Instant.EPOCH);
        Instant stopTime = Optional.ofNullable(end).orElse(Instant.now().plusSeconds(315360000L));
        return String.format("from(bucket:\"%s\") |> range(start: %s, stop: %s) |> filter(fn: (r) => r._measurement == \"%s\" and r.tenantId == \"%s\" and r.forecastId == \"%s\") |> pivot(rowKey:[\"_time\"], columnKey:[\"_field\"], valueColumn:\"_value\") |> sort(columns:[\"_time\"])",
                bucket,
                fluxTime(startTime),
                fluxTime(stopTime),
                MEASUREMENT,
                escapePredicateValue(tenantId),
                escapePredicateValue(forecastId));
    }

    private static void validateKey(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new InfluxTimeSeriesDaoException(name + " must not be blank");
        }
    }

    private static void addTag(Point point, String name, String value) {
        if (StringUtils.hasText(value)) {
            point.addTag(name, value);
        }
    }

    private static void addField(Point point, String name, Double value) {
        if (value != null) {
            point.addField(name, value);
        }
    }

    private static void addField(Point point, String name, long value) {
        point.addField(name, value);
    }

    private static String formatDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static long toEpochMillis(Instant instant) {
        return instant == null ? 0L : instant.toEpochMilli();
    }

    private static String fluxTime(Instant instant) {
        return "\"" + instant.toString() + "\"";
    }

    private static OffsetDateTime toOffset(Instant instant) {
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    private static String escapePredicateValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String getString(FluxRecord record, String key) {
        Object value = record.getValueByKey(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Double doubleObject(FluxRecord record, String key) {
        Object value = record.getValueByKey(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(value.toString());
    }

    private static double doubleValue(FluxRecord record, String key, double defaultValue) {
        Double value = doubleObject(record, key);
        return value == null ? defaultValue : value;
    }

    private static Instant instantFromField(FluxRecord record, String key) {
        Object value = record.getValueByKey(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        return Instant.parse(value.toString());
    }

    private static LocalDate parseDate(String isoDate) {
        return StringUtils.hasText(isoDate) ? LocalDate.parse(isoDate) : null;
    }

    private Organization resolveOrganization() {
        try {
            List<Organization> organizations = organizationsApi.findOrganizations();
            if (organizations == null) {
                return null;
            }
            return organizations.stream()
                    .filter(o -> org.equals(o.getName()) || org.equals(o.getId()))
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            throw new InfluxTimeSeriesDaoException("Failed to query organizations", ex);
        }
    }
}


