package com.power.base.datamodel.dto.financials;

import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class SwapTradeHeaderDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tradeId;
    private LocalDate tradeDate;
    private Instant tradeTime;
    private DocumentType documentType;
    private String documentVersion;
    private PartyDto buyerParty;
    private PartyDto sellerParty;
    private String businessUnit;
    private String bookStrategy;
    private String traderName;
    private String agreementId;
    private String market;
    private String commodity;
    private String transactionType;
    private String referenceZone;
    private BuySellIndicator buySellIndicator;
    private boolean amendmentIndicator;

    public SwapTradeHeaderDto() {
    }

    public SwapTradeHeaderDto(String tradeId,
                              LocalDate tradeDate,
                              Instant tradeTime,
                              DocumentType documentType,
                              String documentVersion,
                              PartyDto buyerParty,
                              PartyDto sellerParty,
                              String businessUnit,
                              String bookStrategy,
                              String traderName,
                              String agreementId,
                              String market,
                              String commodity,
                              String transactionType,
                              String referenceZone,
                              BuySellIndicator buySellIndicator,
                              boolean amendmentIndicator) {
        this.tradeId = tradeId;
        this.tradeDate = tradeDate;
        this.tradeTime = tradeTime;
        this.documentType = documentType;
        this.documentVersion = documentVersion;
        this.buyerParty = buyerParty;
        this.sellerParty = sellerParty;
        this.businessUnit = businessUnit;
        this.bookStrategy = bookStrategy;
        this.traderName = traderName;
        this.agreementId = agreementId;
        this.market = market;
        this.commodity = commodity;
        this.transactionType = transactionType;
        this.referenceZone = referenceZone;
        this.buySellIndicator = buySellIndicator;
        this.amendmentIndicator = amendmentIndicator;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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

    public PartyDto getBuyerParty() {
        return buyerParty;
    }

    public void setBuyerParty(PartyDto buyerParty) {
        this.buyerParty = buyerParty;
    }

    public PartyDto getSellerParty() {
        return sellerParty;
    }

    public void setSellerParty(PartyDto sellerParty) {
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

    public boolean isAmendmentIndicator() {
        return amendmentIndicator;
    }

    public void setAmendmentIndicator(boolean amendmentIndicator) {
        this.amendmentIndicator = amendmentIndicator;
    }

    public BuySellIndicator getBuySellIndicator() {
        return buySellIndicator;
    }

    public void setBuySellIndicator(BuySellIndicator buySellIndicator) {
        this.buySellIndicator = buySellIndicator;
    }
}

