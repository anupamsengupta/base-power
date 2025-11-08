package com.power.base.datamodel.dto.physicals;

import java.io.Serializable;

/**
 * Serializable DTO representing a physical power trade confirmation.
 */
public class PhysicalPowerTradeDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private PhysicalTradeHeaderDto tradeHeader;
    private PhysicalTradeDetailsDto tradeDetails;
    private PhysicalSettlementInfoDto settlementInfo;
    private PhysicalMetadataDto metadata;

    public PhysicalPowerTradeDto() {
        // No-args constructor required for serialization frameworks
    }

    public PhysicalPowerTradeDto(PhysicalTradeHeaderDto tradeHeader,
                                 PhysicalTradeDetailsDto tradeDetails,
                                 PhysicalSettlementInfoDto settlementInfo,
                                 PhysicalMetadataDto metadata) {
        this.tradeHeader = tradeHeader;
        this.tradeDetails = tradeDetails;
        this.settlementInfo = settlementInfo;
        this.metadata = metadata;
    }

    public PhysicalTradeHeaderDto getTradeHeader() {
        return tradeHeader;
    }

    public void setTradeHeader(PhysicalTradeHeaderDto tradeHeader) {
        this.tradeHeader = tradeHeader;
    }

    public PhysicalTradeDetailsDto getTradeDetails() {
        return tradeDetails;
    }

    public void setTradeDetails(PhysicalTradeDetailsDto tradeDetails) {
        this.tradeDetails = tradeDetails;
    }

    public PhysicalSettlementInfoDto getSettlementInfo() {
        return settlementInfo;
    }

    public void setSettlementInfo(PhysicalSettlementInfoDto settlementInfo) {
        this.settlementInfo = settlementInfo;
    }

    public PhysicalMetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(PhysicalMetadataDto metadata) {
        this.metadata = metadata;
    }
}

