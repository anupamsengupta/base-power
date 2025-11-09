package com.power.base.dao.rdbms.mybatis.repository.swap;

/**
 * MyBatis search criteria that delegates to the JPA criteria implementation.
 */
public class SwapTradeSearchCriteria
        extends com.power.base.dao.rdbms.jpa.repository.swap.SwapTradeSearchCriteria {

    public String getBusinessUnitValue() {
        return super.getBusinessUnit().orElse(null);
    }

    public String getMarketValue() {
        return super.getMarket().orElse(null);
    }

    public String getTraderNameValue() {
        return super.getTraderName().orElse(null);
    }

    public String getAgreementIdValue() {
        return super.getAgreementId().orElse(null);
    }

    public String getCommodityValue() {
        return super.getCommodity().orElse(null);
    }

    public String getTransactionTypeValue() {
        return super.getTransactionType().orElse(null);
    }

    public String getReferenceZoneValue() {
        return super.getReferenceZone().orElse(null);
    }

    public java.time.LocalDate getTradeDateValue() {
        return super.getTradeDate().orElse(null);
    }

    public java.time.Instant getTradeTimeFromValue() {
        return super.getTradeTimeFrom().orElse(null);
    }

    public java.time.Instant getTradeTimeToValue() {
        return super.getTradeTimeTo().orElse(null);
    }
}


