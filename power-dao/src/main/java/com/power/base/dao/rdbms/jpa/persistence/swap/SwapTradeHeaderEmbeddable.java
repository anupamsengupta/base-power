package com.power.base.dao.rdbms.jpa.persistence.swap;

import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Embeddable
public class SwapTradeHeaderEmbeddable implements Serializable {

    @Column(name = "trade_date")
    private LocalDate tradeDate;

    @Column(name = "trade_time", columnDefinition = "TIMESTAMP")
    private Instant tradeTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private DocumentType documentType;

    @Column(name = "document_version")
    private String documentVersion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "buyer_party_id")),
            @AttributeOverride(name = "name", column = @Column(name = "buyer_party_name")),
            @AttributeOverride(name = "role", column = @Column(name = "buyer_party_role"))
    })
    private SwapPartyEmbeddable buyerParty;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "seller_party_id")),
            @AttributeOverride(name = "name", column = @Column(name = "seller_party_name")),
            @AttributeOverride(name = "role", column = @Column(name = "seller_party_role"))
    })
    private SwapPartyEmbeddable sellerParty;

    @Column(name = "business_unit")
    private String businessUnit;

    @Column(name = "book_strategy")
    private String bookStrategy;

    @Column(name = "trader_name")
    private String traderName;

    @Column(name = "agreement_id")
    private String agreementId;

    @Column(name = "market")
    private String market;

    @Column(name = "commodity")
    private String commodity;

    @Column(name = "transaction_type")
    private String transactionType;

    @Column(name = "reference_zone")
    private String referenceZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "buy_sell_indicator")
    private BuySellIndicator buySellIndicator;

    @Column(name = "amendment_indicator")
    private boolean amendmentIndicator;

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

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public void setDocumentVersion(String documentVersion) {
        this.documentVersion = documentVersion;
    }

    public SwapPartyEmbeddable getBuyerParty() {
        return buyerParty;
    }

    public void setBuyerParty(SwapPartyEmbeddable buyerParty) {
        this.buyerParty = buyerParty;
    }

    public SwapPartyEmbeddable getSellerParty() {
        return sellerParty;
    }

    public void setSellerParty(SwapPartyEmbeddable sellerParty) {
        this.sellerParty = sellerParty;
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

    public String getReferenceZone() {
        return referenceZone;
    }

    public void setReferenceZone(String referenceZone) {
        this.referenceZone = referenceZone;
    }

    public BuySellIndicator getBuySellIndicator() {
        return buySellIndicator;
    }

    public void setBuySellIndicator(BuySellIndicator buySellIndicator) {
        this.buySellIndicator = buySellIndicator;
    }

    public boolean isAmendmentIndicator() {
        return amendmentIndicator;
    }

    public void setAmendmentIndicator(boolean amendmentIndicator) {
        this.amendmentIndicator = amendmentIndicator;
    }
}

