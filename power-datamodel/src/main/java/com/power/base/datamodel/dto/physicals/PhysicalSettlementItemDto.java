package com.power.base.datamodel.dto.physicals;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhysicalSettlementItemDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String settlementId;
    private List<String> referencedLineItems = new ArrayList<>();
    private LocalDate deliveryDate;
    private double actualQuantity;
    private String uom;
    private double settlementPrice;
    private double tradePrice;
    private String settlementUom;
    private String tradeUom;
    private double deviationAmount;
    private double deviationPenalty;
    private double periodCashflow;
    private String settlementCurrency;
    private String tradeCurrency;
    private String invoiceStatus;

    public PhysicalSettlementItemDto() {
    }

    public PhysicalSettlementItemDto(String settlementId,
                                     List<String> referencedLineItems,
                                     LocalDate deliveryDate,
                                     double actualQuantity,
                                     String uom,
                                     double settlementPrice,
                                     double tradePrice,
                                     String settlementUom,
                                     String tradeUom,
                                     double deviationAmount,
                                     double deviationPenalty,
                             double periodCashflow,
                             String settlementCurrency,
                             String tradeCurrency,
                                     String invoiceStatus) {
        this.settlementId = settlementId;
        if (referencedLineItems != null) {
            this.referencedLineItems = new ArrayList<>(referencedLineItems);
        }
        this.deliveryDate = deliveryDate;
        this.actualQuantity = actualQuantity;
        this.uom = uom;
        this.settlementPrice = settlementPrice;
        this.tradePrice = tradePrice;
        this.settlementUom = settlementUom;
        this.tradeUom = tradeUom;
        this.deviationAmount = deviationAmount;
        this.deviationPenalty = deviationPenalty;
        this.periodCashflow = periodCashflow;
        this.settlementCurrency = settlementCurrency;
        this.tradeCurrency = tradeCurrency;
        this.invoiceStatus = invoiceStatus;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
    }

    public List<String> getReferencedLineItems() {
        return Collections.unmodifiableList(referencedLineItems);
    }

    public void setReferencedLineItems(List<String> referencedLineItems) {
        this.referencedLineItems = referencedLineItems == null ? new ArrayList<>() : new ArrayList<>(referencedLineItems);
    }

    public void addReferencedLineItem(String lineItemId) {
        if (lineItemId != null && !lineItemId.isEmpty()) {
            this.referencedLineItems.add(lineItemId);
        }
    }

    public void clearReferencedLineItems() {
        this.referencedLineItems.clear();
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
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

    public double getDeviationAmount() {
        return deviationAmount;
    }

    public void setDeviationAmount(double deviationAmount) {
        this.deviationAmount = deviationAmount;
    }

    public double getDeviationPenalty() {
        return deviationPenalty;
    }

    public void setDeviationPenalty(double deviationPenalty) {
        this.deviationPenalty = deviationPenalty;
    }

    public double getPeriodCashflow() {
        return periodCashflow;
    }

    public void setPeriodCashflow(double periodCashflow) {
        this.periodCashflow = periodCashflow;
    }

    public String getSettlementCurrency() {
        return settlementCurrency;
    }

    public void setSettlementCurrency(String settlementCurrency) {
        this.settlementCurrency = settlementCurrency;
    }

    public String getTradeCurrency() {
        return tradeCurrency;
    }

    public void setTradeCurrency(String tradeCurrency) {
        this.tradeCurrency = tradeCurrency;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }
}

