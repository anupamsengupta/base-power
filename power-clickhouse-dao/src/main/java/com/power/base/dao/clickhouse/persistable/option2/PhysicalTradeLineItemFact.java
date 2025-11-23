package com.power.base.dao.clickhouse.persistable.option2;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Persistable entity for Option 2: Line items fact table.
 * One row per line item with denormalized trade information.
 */
public class PhysicalTradeLineItemFact {

    private String tradeId;
    private Integer lineItemIndex;  // Index in original array
    private LocalDate periodStartDate;
    private Instant periodStartTime;
    private LocalDate periodEndDate;
    private Instant periodEndTime;
    private String dayHour;
    private Double quantity;
    private String uom;
    private Double capacity;
    private String profile;

    // Denormalized trade info for easier queries
    private String tenantId;
    private LocalDate tradeDate;
    private String market;
    private String businessUnit;

    private LocalDateTime insertedAt;

    public PhysicalTradeLineItemFact() {
        this.insertedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getLineItemIndex() {
        return lineItemIndex;
    }

    public void setLineItemIndex(Integer lineItemIndex) {
        this.lineItemIndex = lineItemIndex;
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

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Double getCapacity() {
        return capacity;
    }

    public void setCapacity(Double capacity) {
        this.capacity = capacity;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDate getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public LocalDateTime getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(LocalDateTime insertedAt) {
        this.insertedAt = insertedAt;
    }
}

