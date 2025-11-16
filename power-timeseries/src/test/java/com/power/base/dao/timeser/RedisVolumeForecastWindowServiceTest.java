package com.power.base.dao.timeser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesPointDto;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisVolumeForecastWindowServiceTest {

    private VolumeForecastSeriesDao seriesDao;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private TimeSeriesCacheProperties cacheProperties;
    private ObjectMapper objectMapper;
    private RedisVolumeForecastWindowService service;

    @BeforeEach
    void setUp() {
        seriesDao = Mockito.mock(VolumeForecastSeriesDao.class);
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        cacheProperties = new TimeSeriesCacheProperties();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        service = new RedisVolumeForecastWindowService(seriesDao, redisTemplate, cacheProperties, objectMapper);
    }

    @Test
    void getWindowCachesResultOnMiss() throws Exception {
        Instant start = Instant.parse("2025-11-10T00:00:00Z");
        Instant end = Instant.parse("2025-11-11T00:00:00Z");

        when(valueOperations.get(any(String.class))).thenReturn(null);

        VolumeForecastSeriesPointDto point = new VolumeForecastSeriesPointDto();
        point.setIntervalStartTime(start);
        point.setForecastVolume(10.0);
        when(seriesDao.queryRange("TENANT", "FRC-1", start, end))
                .thenReturn(Collections.singletonList(point));

        List<VolumeForecastSeriesPointDto> result =
                service.getWindow("TENANT", "FRC-1", start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getForecastVolume()).isEqualTo(10.0);

        verify(valueOperations, times(1)).set(any(String.class), any(String.class), any(Long.class), any());
        verify(seriesDao, times(1)).queryRange("TENANT", "FRC-1", start, end);
    }

    @Test
    void getWindowReturnsCachedOnHit() throws Exception {
        Instant start = Instant.parse("2025-11-10T00:00:00Z");
        Instant end = Instant.parse("2025-11-11T00:00:00Z");

        VolumeForecastSeriesPointDto point = new VolumeForecastSeriesPointDto();
        point.setIntervalStartTime(start);
        point.setForecastVolume(15.0);
        String payload = objectMapper.writeValueAsString(Collections.singletonList(point));

        when(valueOperations.get(any(String.class))).thenReturn(payload);

        List<VolumeForecastSeriesPointDto> result =
                service.getWindow("TENANT", "FRC-1", start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getForecastVolume()).isEqualTo(15.0);
        verify(seriesDao, times(0)).queryRange(any(), any(), any(), any());
    }

    @Test
    void evictWindowDeletesKey() {
        Instant start = Instant.parse("2025-11-10T00:00:00Z");
        Instant end = Instant.parse("2025-11-11T00:00:00Z");

        service.evictWindow("TENANT", "FRC-1", start, end);

        verify(redisTemplate, times(1)).delete(any(String.class));
    }
}


