package com.power.base.dao.timeser;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration for time series window caching.
 */
@ConfigurationProperties(prefix = "power.timeser.cache")
public class TimeSeriesCacheProperties {

    /**
     * Enable or disable the cache layer.
     */
    private boolean enabled = true;

    /**
     * Key prefix for all cached windows.
     */
    private String keyPrefix = "timeser:window:";

    /**
     * Default TTL for window entries.
     */
    private Duration ttl = Duration.ofHours(1);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}


