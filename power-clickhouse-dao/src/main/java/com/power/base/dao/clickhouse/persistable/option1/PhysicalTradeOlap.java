package com.power.base.dao.clickhouse.persistable.option1;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Persistable entity for Option 1: Single flat table with arrays.
 * Represents a physical trade with all nested collections stored as arrays.
 */
public class PhysicalTradeOlap {

    // Primary Key & Partitioning
    private String tradeId;
    private String tenantId;
    private LocalDate tradeDate;
    private Instant tradeTime;

    // Header Information (flattened)
    private String documentType;
    private String documentVersion;
    private String buyerPartyId;
    private String buyerPartyName;
    private String buyerPartyRole;
    private String sellerPartyId;
    private String sellerPartyName;
    private String sellerPartyRole;
    private String businessUnit;
    private String bookStrategy;
    private String traderName;
    private String agreementId;
    private String market;
    private String commodity;
    private String transactionType;
    private String deliveryPoint;
    private String loadType;
    private String buySellIndicator;  // 'BUY' or 'SELL'
    private boolean amendmentIndicator;

    // Settlement Info (flattened)
    private Double totalVolume;
    private String totalVolumeUom;
    private String pricingMechanism;
    private Double settlementPrice;
    private Double tradePrice;
    private String settlementCurrency;
    private String tradeCurrency;
    private String settlementUom;
    private String tradeUom;
    private LocalDate startApplicabilityDate;
    private Instant startApplicabilityTime;
    private LocalDate endApplicabilityDate;
    private Instant endApplicabilityTime;
    private String paymentEvent;
    private Double paymentOffset;
    private Double totalContractValue;
    private Double rounding;

    // Metadata (flattened)
    private LocalDate effectiveDate;
    private LocalDate terminationDate;
    private String governingLaw;

    // Line Items as Arrays
    private List<LocalDate> lineItemPeriodStartDates;
    private List<Instant> lineItemPeriodStartTimes;
    private List<LocalDate> lineItemPeriodEndDates;
    private List<Instant> lineItemPeriodEndTimes;
    private List<String> lineItemDayHours;
    private List<Double> lineItemQuantities;
    private List<String> lineItemUoms;
    private List<Double> lineItemCapacities;
    private List<String> lineItemProfiles;

    // Settlement Items as Arrays
    private List<String> settlementItemIds;
    private List<LocalDate> settlementItemDeliveryDates;
    private List<Double> settlementItemActualQuantities;
    private List<String> settlementItemUoms;
    private List<Double> settlementItemSettlementPrices;
    private List<Double> settlementItemTradePrices;
    private List<String> settlementItemSettlementUoms;
    private List<String> settlementItemTradeUoms;
    private List<Double> settlementItemDeviationAmounts;
    private List<Double> settlementItemDeviationPenalties;
    private List<Double> settlementItemPeriodCashflows;
    private List<String> settlementItemSettlementCurrencies;
    private List<String> settlementItemTradeCurrencies;
    private List<String> settlementItemInvoiceStatuses;
    // Referenced line items as nested array (List of Lists of Strings)
    private List<List<String>> settlementItemReferencedLineItems;

    // Computed/Derived columns
    private Integer totalLineItems;
    private Integer totalSettlementItems;
    private Double totalQuantity;
    private Double totalCashflow;

    // Audit/ETL metadata
    private LocalDateTime insertedAt;
    private LocalDateTime updatedAt;
    private String sourceSystem;
    private Integer version;

    // Constructors
    public PhysicalTradeOlap() {
        this.insertedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.sourceSystem = "OLTP";
        this.version = 1;
    }

    // Getters and Setters
    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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

    public Instant getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Instant tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }

    public String getBuyerPartyId() {
        return buyerPartyId;
    }

    public void setBuyerPartyId(String buyerPartyId) {
        this.buyerPartyId = buyerPartyId;
    }

    public String getBuyerPartyName() {
        return buyerPartyName;
    }

    public void setBuyerPartyName(String buyerPartyName) {
        this.buyerPartyName = buyerPartyName;
    }

    public String getBuyerPartyRole() {
        return buyerPartyRole;
    }

    public void setBuyerPartyRole(String buyerPartyRole) {
        this.buyerPartyRole = buyerPartyRole;
    }

    public String getSellerPartyId() {
        return sellerPartyId;
    }

    public void setSellerPartyId(String sellerPartyId) {
        this.sellerPartyId = sellerPartyId;
    }

    public String getSellerPartyName() {
        return sellerPartyName;
    }

    public void setSellerPartyName(String sellerPartyName) {
        this.sellerPartyName = sellerPartyName;
    }

    public String getSellerPartyRole() {
        return sellerPartyRole;
    }

    public void setSellerPartyRole(String sellerPartyRole) {
        this.sellerPartyRole = sellerPartyRole;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public String getBookStrategy() {
        return bookStrategy;
    }

    public void setBookStrategy(String bookStrategy) {
        this.bookStrategy = bookStrategy;
    }

    public String getTraderName() {
        return traderName;
    }

    public void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDeliveryPoint() {
        return deliveryPoint;
    }

    public void setDeliveryPoint(String deliveryPoint) {
        this.deliveryPoint = deliveryPoint;
    }

    public String getLoadType() {
        return loadType;
    }

    public void setLoadType(String loadType) {
        this.loadType = loadType;
    }

    public String getBuySellIndicator() {
        return buySellIndicator;
    }

    public void setBuySellIndicator(String buySellIndicator) {
        this.buySellIndicator = buySellIndicator;
    }

    public boolean isAmendmentIndicator() {
        return amendmentIndicator;
    }

    public void setAmendmentIndicator(boolean amendmentIndicator) {
        this.amendmentIndicator = amendmentIndicator;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Double totalVolume) {
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

    public Double getPaymentOffset() {
        return paymentOffset;
    }

    public void setPaymentOffset(Double paymentOffset) {
        this.paymentOffset = paymentOffset;
    }

    public Double getTotalContractValue() {
        return totalContractValue;
    }

    public void setTotalContractValue(Double totalContractValue) {
        this.totalContractValue = totalContractValue;
    }

    public Double getRounding() {
        return rounding;
    }

    public void setRounding(Double rounding) {
        this.rounding = rounding;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getGoverningLaw() {
        return governingLaw;
    }

    public void setGoverningLaw(String governingLaw) {
        this.governingLaw = governingLaw;
    }

    public List<LocalDate> getLineItemPeriodStartDates() {
        return lineItemPeriodStartDates;
    }

    public void setLineItemPeriodStartDates(List<LocalDate> lineItemPeriodStartDates) {
        this.lineItemPeriodStartDates = lineItemPeriodStartDates;
    }

    public List<Instant> getLineItemPeriodStartTimes() {
        return lineItemPeriodStartTimes;
    }

    public void setLineItemPeriodStartTimes(List<Instant> lineItemPeriodStartTimes) {
        this.lineItemPeriodStartTimes = lineItemPeriodStartTimes;
    }

    public List<LocalDate> getLineItemPeriodEndDates() {
        return lineItemPeriodEndDates;
    }

    public void setLineItemPeriodEndDates(List<LocalDate> lineItemPeriodEndDates) {
        this.lineItemPeriodEndDates = lineItemPeriodEndDates;
    }

    public List<Instant> getLineItemPeriodEndTimes() {
        return lineItemPeriodEndTimes;
    }

    public void setLineItemPeriodEndTimes(List<Instant> lineItemPeriodEndTimes) {
        this.lineItemPeriodEndTimes = lineItemPeriodEndTimes;
    }

    public List<String> getLineItemDayHours() {
        return lineItemDayHours;
    }

    public void setLineItemDayHours(List<String> lineItemDayHours) {
        this.lineItemDayHours = lineItemDayHours;
    }

    public List<Double> getLineItemQuantities() {
        return lineItemQuantities;
    }

    public void setLineItemQuantities(List<Double> lineItemQuantities) {
        this.lineItemQuantities = lineItemQuantities;
    }

    public List<String> getLineItemUoms() {
        return lineItemUoms;
    }

    public void setLineItemUoms(List<String> lineItemUoms) {
        this.lineItemUoms = lineItemUoms;
    }

    public List<Double> getLineItemCapacities() {
        return lineItemCapacities;
    }

    public void setLineItemCapacities(List<Double> lineItemCapacities) {
        this.lineItemCapacities = lineItemCapacities;
    }

    public List<String> getLineItemProfiles() {
        return lineItemProfiles;
    }

    public void setLineItemProfiles(List<String> lineItemProfiles) {
        this.lineItemProfiles = lineItemProfiles;
    }

    public List<String> getSettlementItemIds() {
        return settlementItemIds;
    }

    public void setSettlementItemIds(List<String> settlementItemIds) {
        this.settlementItemIds = settlementItemIds;
    }

    public List<LocalDate> getSettlementItemDeliveryDates() {
        return settlementItemDeliveryDates;
    }

    public void setSettlementItemDeliveryDates(List<LocalDate> settlementItemDeliveryDates) {
        this.settlementItemDeliveryDates = settlementItemDeliveryDates;
    }

    public List<Double> getSettlementItemActualQuantities() {
        return settlementItemActualQuantities;
    }

    public void setSettlementItemActualQuantities(List<Double> settlementItemActualQuantities) {
        this.settlementItemActualQuantities = settlementItemActualQuantities;
    }

    public List<String> getSettlementItemUoms() {
        return settlementItemUoms;
    }

    public void setSettlementItemUoms(List<String> settlementItemUoms) {
        this.settlementItemUoms = settlementItemUoms;
    }

    public List<Double> getSettlementItemSettlementPrices() {
        return settlementItemSettlementPrices;
    }

    public void setSettlementItemSettlementPrices(List<Double> settlementItemSettlementPrices) {
        this.settlementItemSettlementPrices = settlementItemSettlementPrices;
    }

    public List<Double> getSettlementItemTradePrices() {
        return settlementItemTradePrices;
    }

    public void setSettlementItemTradePrices(List<Double> settlementItemTradePrices) {
        this.settlementItemTradePrices = settlementItemTradePrices;
    }

    public List<String> getSettlementItemSettlementUoms() {
        return settlementItemSettlementUoms;
    }

    public void setSettlementItemSettlementUoms(List<String> settlementItemSettlementUoms) {
        this.settlementItemSettlementUoms = settlementItemSettlementUoms;
    }

    public List<String> getSettlementItemTradeUoms() {
        return settlementItemTradeUoms;
    }

    public void setSettlementItemTradeUoms(List<String> settlementItemTradeUoms) {
        this.settlementItemTradeUoms = settlementItemTradeUoms;
    }

    public List<Double> getSettlementItemDeviationAmounts() {
        return settlementItemDeviationAmounts;
    }

    public void setSettlementItemDeviationAmounts(List<Double> settlementItemDeviationAmounts) {
        this.settlementItemDeviationAmounts = settlementItemDeviationAmounts;
    }

    public List<Double> getSettlementItemDeviationPenalties() {
        return settlementItemDeviationPenalties;
    }

    public void setSettlementItemDeviationPenalties(List<Double> settlementItemDeviationPenalties) {
        this.settlementItemDeviationPenalties = settlementItemDeviationPenalties;
    }

    public List<Double> getSettlementItemPeriodCashflows() {
        return settlementItemPeriodCashflows;
    }

    public void setSettlementItemPeriodCashflows(List<Double> settlementItemPeriodCashflows) {
        this.settlementItemPeriodCashflows = settlementItemPeriodCashflows;
    }

    public List<String> getSettlementItemSettlementCurrencies() {
        return settlementItemSettlementCurrencies;
    }

    public void setSettlementItemSettlementCurrencies(List<String> settlementItemSettlementCurrencies) {
        this.settlementItemSettlementCurrencies = settlementItemSettlementCurrencies;
    }

    public List<String> getSettlementItemTradeCurrencies() {
        return settlementItemTradeCurrencies;
    }

    public void setSettlementItemTradeCurrencies(List<String> settlementItemTradeCurrencies) {
        this.settlementItemTradeCurrencies = settlementItemTradeCurrencies;
    }

    public List<String> getSettlementItemInvoiceStatuses() {
        return settlementItemInvoiceStatuses;
    }

    public void setSettlementItemInvoiceStatuses(List<String> settlementItemInvoiceStatuses) {
        this.settlementItemInvoiceStatuses = settlementItemInvoiceStatuses;
    }

    public List<List<String>> getSettlementItemReferencedLineItems() {
        return settlementItemReferencedLineItems;
    }

    public void setSettlementItemReferencedLineItems(List<List<String>> settlementItemReferencedLineItems) {
        this.settlementItemReferencedLineItems = settlementItemReferencedLineItems;
    }

    public Integer getTotalLineItems() {
        return totalLineItems;
    }

    public void setTotalLineItems(Integer totalLineItems) {
        this.totalLineItems = totalLineItems;
    }

    public Integer getTotalSettlementItems() {
        return totalSettlementItems;
    }

    public void setTotalSettlementItems(Integer totalSettlementItems) {
        this.totalSettlementItems = totalSettlementItems;
    }

    public Double getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Double totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Double getTotalCashflow() {
        return totalCashflow;
    }

    public void setTotalCashflow(Double totalCashflow) {
        this.totalCashflow = totalCashflow;
    }

    public LocalDateTime getInsertedAt() {
        return insertedAt;
    }

    public void setInsertedAt(LocalDateTime insertedAt) {
        this.insertedAt = insertedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}

