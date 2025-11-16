package com.power.base.dao.timeser;

import com.influxdb.client.BucketsApi;
import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.OrganizationsApi;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.Organization;
import com.influxdb.client.write.Point;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesDto;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesPointDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InfluxVolumeForecastSeriesDaoTest {

    private InfluxDBClient influxDBClient;
    private WriteApiBlocking writeApi;
    private QueryApi queryApi;
    private DeleteApi deleteApi;
    private BucketsApi bucketsApi;
    private OrganizationsApi organizationsApi;
    private InfluxVolumeForecastSeriesDao dao;

    @BeforeEach
    void setUp() {
        influxDBClient = Mockito.mock(InfluxDBClient.class);
        writeApi = Mockito.mock(WriteApiBlocking.class);
        queryApi = Mockito.mock(QueryApi.class);
        deleteApi = Mockito.mock(DeleteApi.class);
        bucketsApi = Mockito.mock(BucketsApi.class);
        organizationsApi = Mockito.mock(OrganizationsApi.class);

        when(influxDBClient.getWriteApiBlocking()).thenReturn(writeApi);
        when(influxDBClient.getQueryApi()).thenReturn(queryApi);
        when(influxDBClient.getDeleteApi()).thenReturn(deleteApi);
        when(influxDBClient.getBucketsApi()).thenReturn(bucketsApi);
        when(influxDBClient.getOrganizationsApi()).thenReturn(organizationsApi);

        InfluxTimeSeriesProperties properties = new InfluxTimeSeriesProperties();
        properties.setBucket("bucket");
        properties.setOrg("org");
        dao = new InfluxVolumeForecastSeriesDao(influxDBClient, properties);
    }

    @Test
    void saveWritesAllPoints() {
        VolumeForecastSeriesDto series = buildSeries();
        dao.save(series);

        ArgumentCaptor<List<Point>> captor = ArgumentCaptor.forClass(List.class);
        verify(writeApi).writePoints(eq("bucket"), eq("org"), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        String lineProtocol = captor.getValue().get(0).toLineProtocol();
        assertThat(lineProtocol).contains("volume_forecast");
        assertThat(lineProtocol).contains("tenantId=TENANT_X");
    }

    @Test
    void saveAllWritesBatch() {
        VolumeForecastSeriesDto series1 = buildSeries();
        VolumeForecastSeriesDto series2 = buildSeries();
        series2.getDataPoints().get(0).setIntervalStartTime(Instant.parse("2025-11-10T02:00:00Z"));

        dao.saveAll(List.of(series1, series2));

        ArgumentCaptor<List<Point>> captor = ArgumentCaptor.forClass(List.class);
        verify(writeApi).writePoints(eq("bucket"), eq("org"), captor.capture());
        assertThat(captor.getValue()).hasSize(2);
    }

    @Test
    void findSeriesReturnsEmptyWhenNoRecordsReturned() {
        when(queryApi.query(any(String.class), eq("org"))).thenReturn(Collections.emptyList());
        Optional<VolumeForecastSeriesDto> result = dao.findSeries("TENANT_X", "FRC-1",
                Instant.parse("2025-11-10T00:00:00Z"),
                Instant.parse("2025-11-11T00:00:00Z"));
        assertThat(result).isEmpty();
        verify(queryApi).query(any(String.class), eq("org"));
    }

    @Test
    void deleteSeriesInvokesDeleteApi() {
        Instant start = Instant.parse("2025-11-10T00:00:00Z");
        Instant end = Instant.parse("2025-11-11T00:00:00Z");
        dao.deleteSeries("TENANT_X", "FRC-1", start, end);
        ArgumentCaptor<OffsetDateTime> startCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(deleteApi).delete(startCaptor.capture(), endCaptor.capture(), any(String.class), eq("bucket"), eq("org"));
        assertThat(startCaptor.getValue().toInstant()).isEqualTo(start);
        assertThat(endCaptor.getValue().toInstant()).isEqualTo(end);
    }

    @Test
    void healthReturnsPingResult() {
        when(influxDBClient.ping()).thenReturn(true);
        assertThat(dao.health()).isTrue();
    }

    @Test
    void ensureBucketCreatesWhenMissing() {
        when(bucketsApi.findBucketByName("bucket")).thenReturn(null);
        Organization organization = Mockito.mock(Organization.class);
        when(organization.getId()).thenReturn("org-id");
        when(organization.getName()).thenReturn("org");
        when(organizationsApi.findOrganizations()).thenReturn(List.of(organization));

        dao.ensureBucket();

        verify(bucketsApi).createBucket(eq("bucket"), eq("org-id"));
    }

    private VolumeForecastSeriesDto buildSeries() {
        VolumeForecastSeriesDto series = new VolumeForecastSeriesDto();
        series.setTenantId("TENANT_X");
        series.setForecastId("FRC-1");
        series.setAssetId("ASSET-1");
        series.setVolumeUom("MWh");

        VolumeForecastSeriesPointDto point = new VolumeForecastSeriesPointDto();
        point.setIntervalStartTime(Instant.parse("2025-11-10T00:00:00Z"));
        point.setIntervalEndTime(Instant.parse("2025-11-10T01:00:00Z"));
        point.setForecastVolume(10.5);
        point.setActualVolume(10.0);

        series.setDataPoints(Collections.singletonList(point));
        return series;
    }
}


