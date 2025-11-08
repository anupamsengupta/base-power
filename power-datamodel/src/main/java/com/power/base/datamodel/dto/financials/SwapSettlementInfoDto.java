package com.power.base.datamodel.dto.financials;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class SwapSettlementInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private double totalNotional;
    private String totalNotionalUom;
    private String pricingMechanism;
    private double fixedPrice;
    private double spread;
    private String settlementCurrency;
    private String tradeCurrency;
    private String settlementUom;
    private String tradeUom;
    private String settlementType;
    private LocalDate settlementDate;
    private LocalDate startApplicabilityDate;
    private Instant startApplicabilityTime;
    private LocalDate endApplicabilityDate;
    private Instant endApplicabilityTime;
    private double paymentOffset;
    private double totalExpectedValue;
    private double rounding;

    public SwapSettlementInfoDto() {
    }

    public SwapSettlementInfoDto(double totalNotional,
                                 String totalNotionalUom,
                                 String pricingMechanism,
                                 double fixedPrice,
                                 double spread,
                                 String settlementCurrency,
                                 String tradeCurrency,
                                 String settlementUom,
                                 String tradeUom,
                                 String settlementType,
                                 LocalDate settlementDate,
                                 LocalDate startApplicabilityDate,
                                 Instant startApplicabilityTime,
                                 LocalDate endApplicabilityDate,
                                 Instant endApplicabilityTime,
                                 double paymentOffset,
                                 double totalExpectedValue,
                                 double rounding) {
        this.totalNotional = totalNotional;
        this.totalNotionalUom = totalNotionalUom;
        this.pricingMechanism = pricingMechanism;
        this.fixedPrice = fixedPrice;
        this.spread = spread;
        this.settlementCurrency = settlementCurrency;
        this.tradeCurrency = tradeCurrency;
        this.settlementUom = settlementUom;
        this.tradeUom = tradeUom;
        this.settlementType = settlementType;
        this.settlementDate = settlementDate;
        this.startApplicabilityDate = startApplicabilityDate;
        this.startApplicabilityTime = startApplicabilityTime;
        this.endApplicabilityDate = endApplicabilityDate;
        this.endApplicabilityTime = endApplicabilityTime;
        this.paymentOffset = paymentOffset;
        this.totalExpectedValue = totalExpectedValue;
        this.rounding = rounding;
    }

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


