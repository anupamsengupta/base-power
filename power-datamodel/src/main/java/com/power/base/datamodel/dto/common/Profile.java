package com.power.base.datamodel.dto.common;

import java.io.Serializable;
import java.time.Duration;

public enum Profile implements Serializable {
    ONE_HOUR("1Hr", Duration.ofHours(1)),
    FIFTEEN_MIN("15min", Duration.ofMinutes(15)),
    FIVE_MIN("5min", Duration.ofMinutes(5)),
    ONE_MIN("1min", Duration.ofMinutes(1));

    private final String value;
    private final Duration duration;

    Profile(String value, Duration duration) {
        this.value = value;
        this.duration = duration;
    }

    public String getValue() {
        return value;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Profile fromValue(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        for (Profile profile : values()) {
            if (profile.value.equalsIgnoreCase(normalized)) {
                return profile;
            }
        }
        throw new IllegalArgumentException("Unknown profile value: " + value);
    }
}

