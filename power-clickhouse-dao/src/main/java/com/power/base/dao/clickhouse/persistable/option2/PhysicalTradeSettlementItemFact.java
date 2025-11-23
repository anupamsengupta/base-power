package com.power.base.dao.clickhouse.persistable.option2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistable entity for Option 2: Settlement items fact table.
 * One row per settlement item with denormalized trade information.
 */
public class PhysicalTradeSettlementItemFact {

    private String tradeId;
    private Integer settlementItemIndex;  // Index in original array
    private String settlementId;
    private LocalDate deliveryDate;
    private Double actualQuantity;
    private String uom;
    private Double settlementPrice;
    private Double tradePrice;
    private String settlementUom;
    private String tradeUom;
    private Double deviationAmount;
    private Double deviationPenalty;
    private Double periodCashflow;
    private String settlementCurrency;
    private String tradeCurrency;
    private String invoiceStatus;
    private List<String> referencedLineItems;  // Array of line item references

    // Denormalized trade info
    private String tenantId;
    private LocalDate tradeDate;
    private String market;
    private String businessUnit;

    private LocalDateTime insertedAt;

    public PhysicalTradeSettlementItemFact() {
        this.insertedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getSettlementItemIndex() {
        return settlementItemIndex;
    }

    public void setSettlementItemIndex(Integer settlementItemIndex) {
        this.settlementItemIndex = settlementItemIndex;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Double getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(Double settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public Double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(Double tradePrice) {
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

    public Double getDeviationAmount() {
        return deviationAmount;
    }

    public void setDeviationAmount(Double deviationAmount) {
        this.deviationAmount = deviationAmount;
    }

    public Double getDeviationPenalty() {
        return deviationPenalty;
    }

    public void setDeviationPenalty(Double deviationPenalty) {
        this.deviationPenalty = deviationPenalty;
    }

    public Double getPeriodCashflow() {
        return periodCashflow;
    }

    public void setPeriodCashflow(Double periodCashflow) {
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

    public List<String> getReferencedLineItems() {
        return referencedLineItems;
    }

    public void setReferencedLineItems(List<String> referencedLineItems) {
        this.referencedLineItems = referencedLineItems;
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

