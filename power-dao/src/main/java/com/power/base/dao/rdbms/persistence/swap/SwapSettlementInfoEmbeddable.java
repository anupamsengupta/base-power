package com.power.base.dao.rdbms.persistence.swap;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Embeddable
public class SwapSettlementInfoEmbeddable implements Serializable {

    @Column(name = "total_notional")
    private double totalNotional;

    @Column(name = "total_notional_uom")
    private String totalNotionalUom;

    @Column(name = "pricing_mechanism")
    private String pricingMechanism;

    @Column(name = "fixed_price")
    private double fixedPrice;

    @Column(name = "spread")
    private double spread;

    @Column(name = "settlement_currency")
    private String settlementCurrency;

    @Column(name = "trade_currency")
    private String tradeCurrency;

    @Column(name = "settlement_uom")
    private String settlementUom;

    @Column(name = "trade_uom")
    private String tradeUom;

    @Column(name = "settlement_type")
    private String settlementType;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "start_applicability_date")
    private LocalDate startApplicabilityDate;

    @Column(name = "start_applicability_time", columnDefinition = "TIMESTAMP")
    private Instant startApplicabilityTime;

    @Column(name = "end_applicability_date")
    private LocalDate endApplicabilityDate;

    @Column(name = "end_applicability_time", columnDefinition = "TIMESTAMP")
    private Instant endApplicabilityTime;

    @Column(name = "payment_offset")
    private double paymentOffset;

    @Column(name = "total_expected_value")
    private double totalExpectedValue;

    @Column(name = "rounding")
    private double rounding;

    public double getTotalNotional() {
        return totalNotional;
    }

    public void setTotalNotional(double totalNotional) {
        this.totalNotional = totalNotional;
    }

    public String getTotalNotionalUom() {
        return totalNotionalUom;
    }

    public void setTotalNotionalUom(String totalNotionalUom) {
        this.totalNotionalUom = totalNotionalUom;
    }

    public String getPricingMechanism() {
        return pricingMechanism;
    }

    public void setPricingMechanism(String pricingMechanism) {
        this.pricingMechanism = pricingMechanism;
    }

    public double getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(double fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public double getSpread() {
        return spread;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public String getSettlementCurrency() {
        return settlementCurrency;
    }

    public void setSettlementCurrency(String settlementCurrency) {
        this.settlementCurrency = settlementCurrency;
    }

    public String getSettlementUom() {
        return settlementUom;
    }

    public void setSettlementUom(String settlementUom) {
        this.settlementUom = settlementUom;
    }

    public String getTradeUom() {
        return tradeUom;
    }

    public void setTradeUom(String tradeUom) {
        this.tradeUom = tradeUom;
    }

    public String getSettlementType() {
        return settlementType;
    }

    public void setSettlementType(String settlementType) {
        this.settlementType = settlementType;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public void setSettlementDate(LocalDate settlementDate) {
        this.settlementDate = settlementDate;
    }

    public LocalDate getStartApplicabilityDate() {
        return startApplicabilityDate;
    }

    public void setStartApplicabilityDate(LocalDate startApplicabilityDate) {
        this.startApplicabilityDate = startApplicabilityDate;
    }

    public Instant getStartApplicabilityTime() {
        return startApplicabilityTime;
    }

    public void setStartApplicabilityTime(Instant startApplicabilityTime) {
        this.startApplicabilityTime = startApplicabilityTime;
    }

    public LocalDate getEndApplicabilityDate() {
        return endApplicabilityDate;
    }

    public void setEndApplicabilityDate(LocalDate endApplicabilityDate) {
        this.endApplicabilityDate = endApplicabilityDate;
    }

    public Instant getEndApplicabilityTime() {
        return endApplicabilityTime;
    }

    public void setEndApplicabilityTime(Instant endApplicabilityTime) {
        this.endApplicabilityTime = endApplicabilityTime;
    }

    public double getPaymentOffset() {
        return paymentOffset;
    }

    public void setPaymentOffset(double paymentOffset) {
        this.paymentOffset = paymentOffset;
    }

    public double getTotalExpectedValue() {
        return totalExpectedValue;
    }

    public void setTotalExpectedValue(double totalExpectedValue) {
        this.totalExpectedValue = totalExpectedValue;
    }

    public double getRounding() {
        return rounding;
    }

    public void setRounding(double rounding) {
        this.rounding = rounding;
    }

    public String getTradeCurrency() {
        return tradeCurrency;
    }

    public void setTradeCurrency(String tradeCurrency) {
        this.tradeCurrency = tradeCurrency;
    }
}

