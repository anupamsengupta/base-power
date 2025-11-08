package com.power.base.datamodel.dto.physicals;

import com.power.base.datamodel.dto.common.Profile;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PhysicalLineItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate periodStartDate;
    private Instant periodStartTime;
    private LocalDate periodEndDate;
    private Instant periodEndTime;
    private String dayHour;
    private double quantity;
    private String uom;
    private double capacity;
    private Profile profile;

    public PhysicalLineItemDto() {
    }

    public PhysicalLineItemDto(LocalDate periodStartDate,
                               Instant periodStartTime,
                               LocalDate periodEndDate,
                               Instant periodEndTime,
                               String dayHour,
                               double quantity,
                               String uom,
                               double capacity,
                               Profile profile) {
        this.periodStartDate = periodStartDate;
        this.periodStartTime = periodStartTime;
        this.periodEndDate = periodEndDate;
        this.periodEndTime = periodEndTime;
        this.dayHour = dayHour;
        this.quantity = quantity;
        this.uom = uom;
        this.capacity = capacity;
        this.profile = profile;
    }

    public LocalDate getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public Instant getPeriodStartTime() {
        return periodStartTime;
    }

    public void setPeriodStartTime(Instant periodStartTime) {
        this.periodStartTime = periodStartTime;
    }

    public LocalDate getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(LocalDate periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    public Instant getPeriodEndTime() {
        return periodEndTime;
    }

    public void setPeriodEndTime(Instant periodEndTime) {
        this.periodEndTime = periodEndTime;
    }

    public String getDayHour() {
        return dayHour;
    }

    public void setDayHour(String dayHour) {
        this.dayHour = dayHour;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * Generates contiguous {@link PhysicalLineItemDto} segments between the provided bounds using the given profile granularity.
     *
     * @param startInclusive inclusive lower bound instant
     * @param endExclusive   exclusive upper bound instant
     * @param profile        granularity profile to apply
     * @param zoneId         zone used to derive local dates for each segment (UTC if null)
     * @param quantity       quantity value assigned to each generated line item
     * @param uom            unit of measure assigned to each generated line item
     * @param capacity       capacity value assigned to each generated line item
     * @return ordered list of {@link PhysicalLineItemDto} instances covering the requested range without gaps
     */
    public static List<PhysicalLineItemDto> generateSchedule(Instant startInclusive,
                                                             Instant endExclusive,
                                                             Profile profile,
                                                             ZoneId zoneId,
                                                             double quantity,
                                                             String uom,
                                                             double capacity) {
        Objects.requireNonNull(startInclusive, "startInclusive must not be null");
        Objects.requireNonNull(endExclusive, "endExclusive must not be null");
        Objects.requireNonNull(profile, "profile must not be null");

        if (!startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("startInclusive must be before endExclusive");
        }

        var step = profile.getDuration();
        if (step.isZero() || step.isNegative()) {
            throw new IllegalArgumentException("Profile duration must be positive");
        }

        long totalSeconds = endExclusive.getEpochSecond() - startInclusive.getEpochSecond();
        long stepSeconds = step.getSeconds();
        if (totalSeconds % stepSeconds != 0) {
            throw new IllegalArgumentException("Range must be an exact multiple of the profile duration");
        }

        ZoneId effectiveZone = zoneId == null ? ZoneOffset.UTC : zoneId;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        List<PhysicalLineItemDto> lineItems = new ArrayList<>();
        Instant currentStart = startInclusive;

        while (currentStart.isBefore(endExclusive)) {
            Instant currentEnd = currentStart.plus(step);

            ZonedDateTime startZdt = currentStart.atZone(effectiveZone);
            ZonedDateTime endZdt = currentEnd.atZone(effectiveZone);

            String dayHour = String.format("%s %s-%s",
                    startZdt.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    startZdt.toLocalTime().format(timeFormatter),
                    endZdt.toLocalTime().format(timeFormatter));

            PhysicalLineItemDto item = new PhysicalLineItemDto(
                    startZdt.toLocalDate(),
                    currentStart,
                    endZdt.toLocalDate(),
                    currentEnd,
                    dayHour,
                    quantity,
                    uom,
                    capacity,
                    profile
            );
            lineItems.add(item);
            currentStart = currentEnd;
        }

        return lineItems;
    }
}


