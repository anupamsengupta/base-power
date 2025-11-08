package com.power.base.datamodel.dto.financials;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class SwapPeriodDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private double notionalQuantity;
    private String notionalUom;
    private LocalDate termStartDate;
    private Instant termStartTime;
    private LocalDate termEndDate;
    private Instant termEndTime;
    private String periodFrequency;
    private String fixingIndex;
    private String loadShape;

    public SwapPeriodDto() {
    }

    public SwapPeriodDto(double notionalQuantity,
                         String notionalUom,
                         LocalDate termStartDate,
                         Instant termStartTime,
                         LocalDate termEndDate,
                         Instant termEndTime,
                         String periodFrequency,
                         String fixingIndex,
                         String loadShape) {
        this.notionalQuantity = notionalQuantity;
        this.notionalUom = notionalUom;
        this.termStartDate = termStartDate;
        this.termStartTime = termStartTime;
        this.termEndDate = termEndDate;
        this.termEndTime = termEndTime;
        this.periodFrequency = periodFrequency;
        this.fixingIndex = fixingIndex;
        this.loadShape = loadShape;
    }

    public double getNotionalQuantity() {
        return notionalQuantity;
    }

    public void setNotionalQuantity(double notionalQuantity) {
        this.notionalQuantity = notionalQuantity;
    }

    public String getNotionalUom() {
        return notionalUom;
    }

    public void setNotionalUom(String notionalUom) {
        this.notionalUom = notionalUom;
    }

    public LocalDate getTermStartDate() {
        return termStartDate;
    }

    public void setTermStartDate(LocalDate termStartDate) {
        this.termStartDate = termStartDate;
    }

    public Instant getTermStartTime() {
        return termStartTime;
    }

    public void setTermStartTime(Instant termStartTime) {
        this.termStartTime = termStartTime;
    }

    public LocalDate getTermEndDate() {
        return termEndDate;
    }

    public void setTermEndDate(LocalDate termEndDate) {
        this.termEndDate = termEndDate;
    }

    public Instant getTermEndTime() {
        return termEndTime;
    }

    public void setTermEndTime(Instant termEndTime) {
        this.termEndTime = termEndTime;
    }

    public String getPeriodFrequency() {
        return periodFrequency;
    }

    public void setPeriodFrequency(String periodFrequency) {
        this.periodFrequency = periodFrequency;
    }

    public String getFixingIndex() {
        return fixingIndex;
    }

    public void setFixingIndex(String fixingIndex) {
        this.fixingIndex = fixingIndex;
    }

    public String getLoadShape() {
        return loadShape;
    }

    public void setLoadShape(String loadShape) {
        this.loadShape = loadShape;
    }
}


