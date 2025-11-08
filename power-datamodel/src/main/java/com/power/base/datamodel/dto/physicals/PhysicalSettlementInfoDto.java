package com.power.base.datamodel.dto.physicals;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhysicalSettlementInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private double totalVolume;
    private String totalVolumeUom;
    private String pricingMechanism;
    private double settlementPrice;
    private double tradePrice;
    private String settlementCurrency;
    private String tradeCurrency;
    private String settlementUom;
    private String tradeUom;
    private LocalDate startApplicabilityDate;
    private Instant startApplicabilityTime;
    private LocalDate endApplicabilityDate;
    private Instant endApplicabilityTime;
    private String paymentEvent;
    private double paymentOffset;
    private double totalContractValue;
    private double rounding;
    private List<PhysicalSettlementItemDto> settlementItems = new ArrayList<>();

    public PhysicalSettlementInfoDto() {
    }

    public PhysicalSettlementInfoDto(double totalVolume,
                                     String totalVolumeUom,
                                     String pricingMechanism,
                                     double settlementPrice,
                                     double tradePrice,
                                     String settlementCurrency,
                                     String tradeCurrency,
                                     String settlementUom,
                                     String tradeUom,
                                     LocalDate startApplicabilityDate,
                                     Instant startApplicabilityTime,
                                     LocalDate endApplicabilityDate,
                                     Instant endApplicabilityTime,
                                     String paymentEvent,
                                     double paymentOffset,
                                     double totalContractValue,
                                     double rounding,
                                     List<PhysicalSettlementItemDto> settlementItems) {
        this.totalVolume = totalVolume;
        this.totalVolumeUom = totalVolumeUom;
        this.pricingMechanism = pricingMechanism;
        this.settlementPrice = settlementPrice;
        this.tradePrice = tradePrice;
        this.settlementCurrency = settlementCurrency;
        this.tradeCurrency = tradeCurrency;
        this.settlementUom = settlementUom;
        this.tradeUom = tradeUom;
        this.startApplicabilityDate = startApplicabilityDate;
        this.startApplicabilityTime = startApplicabilityTime;
        this.endApplicabilityDate = endApplicabilityDate;
        this.endApplicabilityTime = endApplicabilityTime;
        this.paymentEvent = paymentEvent;
        this.paymentOffset = paymentOffset;
        this.totalContractValue = totalContractValue;
        this.rounding = rounding;
        if (settlementItems != null) {
            this.settlementItems = new ArrayList<>(settlementItems);
        }
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public String getTotalVolumeUom() {
        return totalVolumeUom;
    }

    public void setTotalVolumeUom(String totalVolumeUom) {
        this.totalVolumeUom = totalVolumeUom;
    }

    public String getPricingMechanism() {
        return pricingMechanism;
    }

    public void setPricingMechanism(String pricingMechanism) {
        this.pricingMechanism = pricingMechanism;
    }

    public double getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(double settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(double tradePrice) {
        this.tradePrice = tradePrice;
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

    public String getPaymentEvent() {
        return paymentEvent;
    }

    public void setPaymentEvent(String paymentEvent) {
        this.paymentEvent = paymentEvent;
    }

    public double getPaymentOffset() {
        return paymentOffset;
    }

    public void setPaymentOffset(double paymentOffset) {
        this.paymentOffset = paymentOffset;
    }

    public double getTotalContractValue() {
        return totalContractValue;
    }

    public void setTotalContractValue(double totalContractValue) {
        this.totalContractValue = totalContractValue;
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

    public List<PhysicalSettlementItemDto> getSettlementItems() {
        return Collections.unmodifiableList(settlementItems);
    }

    public void setSettlementItems(List<PhysicalSettlementItemDto> settlementItems) {
        this.settlementItems = settlementItems == null ? new ArrayList<>() : new ArrayList<>(settlementItems);
    }

    public void addSettlementItem(PhysicalSettlementItemDto settlementItem) {
        if (settlementItem != null) {
            this.settlementItems.add(settlementItem);
        }
    }

    public void clearSettlementItems() {
        this.settlementItems.clear();
    }
}


