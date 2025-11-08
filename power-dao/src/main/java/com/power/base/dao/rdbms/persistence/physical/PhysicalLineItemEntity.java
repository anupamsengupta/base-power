package com.power.base.dao.rdbms.persistence.physical;

import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "physical_trade_line_items")
public class PhysicalLineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private PhysicalTradeEntity trade;

    @Column(name = "period_start_date")
    private LocalDate periodStartDate;

    @Column(name = "period_start_time", columnDefinition = "TIMESTAMP")
    private Instant periodStartTime;

    @Column(name = "period_end_date")
    private LocalDate periodEndDate;

    @Column(name = "period_end_time", columnDefinition = "TIMESTAMP")
    private Instant periodEndTime;

    @Column(name = "day_hour_label")
    private String dayHour;

    @Column(name = "quantity")
    private double quantity;

    @Column(name = "uom")
    private String uom;

    @Column(name = "capacity")
    private double capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile")
    private Profile profile;

    public static PhysicalLineItemEntity fromDto(PhysicalLineItemDto dto, PhysicalTradeEntity trade) {
        PhysicalLineItemEntity entity = new PhysicalLineItemEntity();
        entity.setTrade(trade);
        entity.setPeriodStartDate(dto.getPeriodStartDate());
        entity.setPeriodStartTime(dto.getPeriodStartTime());
        entity.setPeriodEndDate(dto.getPeriodEndDate());
        entity.setPeriodEndTime(dto.getPeriodEndTime());
        entity.setDayHour(dto.getDayHour());
        entity.setQuantity(dto.getQuantity());
        entity.setUom(dto.getUom());
        entity.setCapacity(dto.getCapacity());
        entity.setProfile(dto.getProfile());
        return entity;
    }

    public PhysicalLineItemDto toDto() {
        return new PhysicalLineItemDto(
                periodStartDate,
                periodStartTime,
                periodEndDate,
                periodEndTime,
                dayHour,
                quantity,
                uom,
                capacity,
                profile
        );
    }

    public Long getId() {
        return id;
    }

    public PhysicalTradeEntity getTrade() {
        return trade;
    }

    public void setTrade(PhysicalTradeEntity trade) {
        this.trade = trade;
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
}

