package com.power.base.dao.timeser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.power.base.datamodel.dto.timeser.VolumeForecastSeriesPointDto;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed implementation of {@link VolumeForecastWindowService} that
 * materializes time windows from {@link VolumeForecastSeriesDao} and caches them.
 */
@Service
@ConditionalOnBean({VolumeForecastSeriesDao.class, StringRedisTemplate.class})
@ConditionalOnProperty(prefix = "power.timeser.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TimeSeriesCacheProperties.class)
public class RedisVolumeForecastWindowService implements VolumeForecastWindowService {

    private static final TypeReference<List<VolumeForecastSeriesPointDto>> POINT_LIST_TYPE =
            new TypeReference<List<VolumeForecastSeriesPointDto>>() {};

    private final VolumeForecastSeriesDao seriesDao;
    private final StringRedisTemplate redisTemplate;
    private final TimeSeriesCacheProperties cacheProperties;
    private final ObjectMapper objectMapper;

    public RedisVolumeForecastWindowService(VolumeForecastSeriesDao seriesDao,
                                            StringRedisTemplate redisTemplate,
                                            TimeSeriesCacheProperties cacheProperties,
                                            ObjectMapper objectMapper) {
        this.seriesDao = seriesDao;
        this.redisTemplate = redisTemplate;
        this.cacheProperties = cacheProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<VolumeForecastSeriesPointDto> getWindow(String tenantId,
                                                        String forecastId,
                                                        Instant startInclusive,
                                                        Instant endExclusive) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(forecastId)) {
            throw new InfluxTimeSeriesDaoException("tenantId and forecastId must be provided");
        }
        if (startInclusive == null || endExclusive == null) {
            throw new InfluxTimeSeriesDaoException("Both startInclusive and endExclusive must be provided");
        }

        String key = buildKey(tenantId, forecastId, startInclusive, endExclusive);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        try {
            String cached = ops.get(key);
            if (cached != null) {
                return objectMapper.readValue(cached, POINT_LIST_TYPE);
            }
        } catch (IOException ex) {
            // Corrupt cache entry; evict and fall back to source of truth
            redisTemplate.delete(key);
        }

        List<VolumeForecastSeriesPointDto> points =
                seriesDao.queryRange(tenantId, forecastId, startInclusive, endExclusive);
        if (points == null) {
            points = Collections.emptyList();
        }

        try {
            String payload = objectMapper.writeValueAsString(points);
            ops.set(key, payload, cacheProperties.getTtl().toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            // Cache failures must not break reads; log at caller side if needed
        }

        return points;
    }

    @Override
    public void evictWindow(String tenantId,
                            String forecastId,
                            Instant startInclusive,
                            Instant endExclusive) {
        if (!StringUtils.hasText(tenantId) || !StringUtils.hasText(forecastId)
                || startInclusive == null || endExclusive == null) {
            return;
        }
        String key = buildKey(tenantId, forecastId, startInclusive, endExclusive);
        redisTemplate.delete(key);
    }

    private String buildKey(String tenantId,
                            String forecastId,
                            Instant startInclusive,
                            Instant endExclusive) {
        String prefix = cacheProperties.getKeyPrefix();
        if (prefix == null) {
            prefix = "";
        }
        return prefix
                + "vol:"
                + tenantId + ':'
                + forecastId + ':'
                + startInclusive.toEpochMilli() + ':'
                + endExclusive.toEpochMilli();
    }
}


