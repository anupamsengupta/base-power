package com.power.base.dao.rdbms.jpa.repository.swap;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public class SwapTradeSearchCriteria {

    private String tenantId;
    private String businessUnit;
    private String market;
    private String traderName;
    private String agreementId;
    private String commodity;
    private String transactionType;
    private String referenceZone;
    private LocalDate tradeDate;
    private Instant tradeTimeFrom;
    private Instant tradeTimeTo;

    public Optional<String> getBusinessUnit() {
        return Optional.ofNullable(businessUnit);
    }

    public Optional<String> getTenantId() {
        return Optional.ofNullable(tenantId);
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    public Optional<String> getMarket() {
        return Optional.ofNullable(market);
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Optional<String> getTraderName() {
        return Optional.ofNullable(traderName);
    }

    public void setTraderName(String traderName) {
        this.traderName = traderName;
    }

    public Optional<String> getAgreementId() {
        return Optional.ofNullable(agreementId);
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public Optional<String> getCommodity() {
        return Optional.ofNullable(commodity);
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public Optional<String> getTransactionType() {
        return Optional.ofNullable(transactionType);
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Optional<String> getReferenceZone() {
        return Optional.ofNullable(referenceZone);
    }

    public void setReferenceZone(String referenceZone) {
        this.referenceZone = referenceZone;
    }

    public Optional<LocalDate> getTradeDate() {
        return Optional.ofNullable(tradeDate);
    }

    public void setTradeDate(LocalDate tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Optional<Instant> getTradeTimeFrom() {
        return Optional.ofNullable(tradeTimeFrom);
    }

    public void setTradeTimeFrom(Instant tradeTimeFrom) {
        this.tradeTimeFrom = tradeTimeFrom;
    }

    public Optional<Instant> getTradeTimeTo() {
        return Optional.ofNullable(tradeTimeTo);
    }

    public void setTradeTimeTo(Instant tradeTimeTo) {
        this.tradeTimeTo = tradeTimeTo;
    }
}

