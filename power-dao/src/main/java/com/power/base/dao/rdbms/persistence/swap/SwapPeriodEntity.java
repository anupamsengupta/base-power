package com.power.base.dao.rdbms.persistence.swap;

import com.power.base.datamodel.dto.financials.SwapPeriodDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "swap_periods")
public class SwapPeriodEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private SwapTradeEntity trade;

    @Column(name = "notional_quantity")
    private double notionalQuantity;

    @Column(name = "notional_uom")
    private String notionalUom;

    @Column(name = "term_start_date")
    private LocalDate termStartDate;

    @Column(name = "term_start_time", columnDefinition = "TIMESTAMP")
    private Instant termStartTime;

    @Column(name = "term_end_date")
    private LocalDate termEndDate;

    @Column(name = "term_end_time", columnDefinition = "TIMESTAMP")
    private Instant termEndTime;

    @Column(name = "period_frequency")
    private String periodFrequency;

    @Column(name = "fixing_index")
    private String fixingIndex;

    @Column(name = "load_shape")
    private String loadShape;

    public static SwapPeriodEntity fromDto(SwapPeriodDto dto, SwapTradeEntity trade) {
        SwapPeriodEntity entity = new SwapPeriodEntity();
        entity.setTrade(trade);
        entity.setNotionalQuantity(dto.getNotionalQuantity());
        entity.setNotionalUom(dto.getNotionalUom());
        entity.setTermStartDate(dto.getTermStartDate());
        entity.setTermStartTime(dto.getTermStartTime());
        entity.setTermEndDate(dto.getTermEndDate());
        entity.setTermEndTime(dto.getTermEndTime());
        entity.setPeriodFrequency(dto.getPeriodFrequency());
        entity.setFixingIndex(dto.getFixingIndex());
        entity.setLoadShape(dto.getLoadShape());
        return entity;
    }

    public SwapPeriodDto toDto() {
        return new SwapPeriodDto(
                notionalQuantity,
                notionalUom,
                termStartDate,
                termStartTime,
                termEndDate,
                termEndTime,
                periodFrequency,
                fixingIndex,
                loadShape
        );
    }

    public Long getId() {
        return id;
    }

    public SwapTradeEntity getTrade() {
        return trade;
    }

    public void setTrade(SwapTradeEntity trade) {
        this.trade = trade;
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

