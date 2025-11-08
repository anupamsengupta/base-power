package com.power.base.datamodel.dto.financials;

import java.io.Serializable;

/**
 * Serializable DTO representing a financial power swap trade confirmation.
 */
public class SwapPowerTradeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private SwapTradeHeaderDto tradeHeader;
    private SwapTradeDetailsDto financialDetails;
    private SwapSettlementInfoDto settlementInfo;
    private SwapMetadataDto metadata;

    public SwapPowerTradeDto() {
        // Default constructor for serialization frameworks
    }

    public SwapPowerTradeDto(SwapTradeHeaderDto tradeHeader,
                             SwapTradeDetailsDto financialDetails,
                             SwapSettlementInfoDto settlementInfo,
                             SwapMetadataDto metadata) {
        this.tradeHeader = tradeHeader;
        this.financialDetails = financialDetails;
        this.settlementInfo = settlementInfo;
        this.metadata = metadata;
    }

    public SwapTradeHeaderDto getTradeHeader() {
        return tradeHeader;
    }

    public void setTradeHeader(SwapTradeHeaderDto tradeHeader) {
        this.tradeHeader = tradeHeader;
    }

    public SwapTradeDetailsDto getFinancialDetails() {
        return financialDetails;
    }

    public void setFinancialDetails(SwapTradeDetailsDto financialDetails) {
        this.financialDetails = financialDetails;
    }

    public SwapSettlementInfoDto getSettlementInfo() {
        return settlementInfo;
    }

    public void setSettlementInfo(SwapSettlementInfoDto settlementInfo) {
        this.settlementInfo = settlementInfo;
    }

    public SwapMetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(SwapMetadataDto metadata) {
        this.metadata = metadata;
    }
}

