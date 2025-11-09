package com.power.base.dao.rdbms.jpa.persistence.swap;

import com.power.base.datamodel.dto.financials.SwapMetadataDto;
import com.power.base.datamodel.dto.financials.SwapPowerTradeDto;
import com.power.base.datamodel.dto.financials.SwapSettlementInfoDto;
import com.power.base.datamodel.dto.financials.SwapTradeDetailsDto;
import com.power.base.datamodel.dto.financials.SwapTradeHeaderDto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "swap_trades")
public class SwapTradeEntity {

    @Id
    @Column(name = "trade_id", nullable = false, updatable = false, length = 64)
    private String tradeId;

    @Embedded
    private SwapTradeHeaderEmbeddable header;

    @Embedded
    private SwapSettlementInfoEmbeddable settlementInfo;

    @Embedded
    private SwapMetadataEmbeddable metadata;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SwapPeriodEntity> periods = new ArrayList<>();

    public static SwapTradeEntity fromDto(SwapPowerTradeDto dto) {
        SwapTradeEntity entity = new SwapTradeEntity();
        entity.tradeId = dto.getTradeHeader().getTradeId();
        entity.header = toHeaderEmbeddable(dto.getTradeHeader());
        entity.settlementInfo = toSettlementEmbeddable(dto.getSettlementInfo());
        entity.metadata = toMetadataEmbeddable(dto.getMetadata());

        entity.periods.clear();
        if (dto.getFinancialDetails() != null && dto.getFinancialDetails().getPeriods() != null) {
            dto.getFinancialDetails().getPeriods().forEach(periodDto -> {
                SwapPeriodEntity periodEntity = SwapPeriodEntity.fromDto(periodDto, entity);
                entity.periods.add(periodEntity);
            });
        }
        return entity;
    }

    public SwapPowerTradeDto toDto() {
        SwapTradeHeaderDto headerDto = toHeaderDto(header, tradeId);
        SwapTradeDetailsDto detailsDto = new SwapTradeDetailsDto(
                periods.stream().map(SwapPeriodEntity::toDto).collect(Collectors.toList())
        );
        SwapSettlementInfoDto settlementInfoDto = toSettlementDto(settlementInfo);
        SwapMetadataDto metadataDto = toMetadataDto(metadata);

        return new SwapPowerTradeDto(
                headerDto,
                detailsDto,
                settlementInfoDto,
                metadataDto
        );
    }

    private static SwapTradeHeaderEmbeddable toHeaderEmbeddable(SwapTradeHeaderDto dto) {
        SwapTradeHeaderEmbeddable header = new SwapTradeHeaderEmbeddable();
        header.setTradeDate(dto.getTradeDate());
        header.setTradeTime(dto.getTradeTime());
        header.setDocumentType(dto.getDocumentType());
        header.setDocumentVersion(dto.getDocumentVersion());
        if (dto.getBuyerParty() != null) {
            SwapPartyEmbeddable buyer = new SwapPartyEmbeddable();
            buyer.setId(dto.getBuyerParty().getId());
            buyer.setName(dto.getBuyerParty().getName());
            buyer.setRole(dto.getBuyerParty().getRole());
            header.setBuyerParty(buyer);
        }
        if (dto.getSellerParty() != null) {
            SwapPartyEmbeddable seller = new SwapPartyEmbeddable();
            seller.setId(dto.getSellerParty().getId());
            seller.setName(dto.getSellerParty().getName());
            seller.setRole(dto.getSellerParty().getRole());
            header.setSellerParty(seller);
        }
        header.setBusinessUnit(dto.getBusinessUnit());
        header.setBookStrategy(dto.getBookStrategy());
        header.setTraderName(dto.getTraderName());
        header.setAgreementId(dto.getAgreementId());
        header.setMarket(dto.getMarket());
        header.setCommodity(dto.getCommodity());
        header.setTransactionType(dto.getTransactionType());
        header.setReferenceZone(dto.getReferenceZone());
        header.setBuySellIndicator(dto.getBuySellIndicator());
        header.setAmendmentIndicator(dto.isAmendmentIndicator());
        return header;
    }

    private static SwapSettlementInfoEmbeddable toSettlementEmbeddable(SwapSettlementInfoDto dto) {
        if (dto == null) {
            return null;
        }
        SwapSettlementInfoEmbeddable embeddable = new SwapSettlementInfoEmbeddable();
        embeddable.setTotalNotional(dto.getTotalNotional());
        embeddable.setTotalNotionalUom(dto.getTotalNotionalUom());
        embeddable.setPricingMechanism(dto.getPricingMechanism());
        embeddable.setFixedPrice(dto.getFixedPrice());
        embeddable.setSpread(dto.getSpread());
        embeddable.setSettlementCurrency(dto.getSettlementCurrency());
        embeddable.setTradeCurrency(dto.getTradeCurrency());
        embeddable.setSettlementUom(dto.getSettlementUom());
        embeddable.setTradeUom(dto.getTradeUom());
        embeddable.setSettlementType(dto.getSettlementType());
        embeddable.setSettlementDate(dto.getSettlementDate());
        embeddable.setStartApplicabilityDate(dto.getStartApplicabilityDate());
        embeddable.setStartApplicabilityTime(dto.getStartApplicabilityTime());
        embeddable.setEndApplicabilityDate(dto.getEndApplicabilityDate());
        embeddable.setEndApplicabilityTime(dto.getEndApplicabilityTime());
        embeddable.setPaymentOffset(dto.getPaymentOffset());
        embeddable.setTotalExpectedValue(dto.getTotalExpectedValue());
        embeddable.setRounding(dto.getRounding());
        return embeddable;
    }

    private static SwapMetadataEmbeddable toMetadataEmbeddable(SwapMetadataDto dto) {
        if (dto == null) {
            return null;
        }
        SwapMetadataEmbeddable embeddable = new SwapMetadataEmbeddable();
        embeddable.setEffectiveDate(dto.getEffectiveDate());
        embeddable.setTerminationDate(dto.getTerminationDate());
        embeddable.setGoverningLaw(dto.getGoverningLaw());
        embeddable.setClearable(dto.isClearable());
        embeddable.setUti(dto.getUti());
        return embeddable;
    }

    private static SwapTradeHeaderDto toHeaderDto(SwapTradeHeaderEmbeddable header, String tradeId) {
        SwapTradeHeaderDto dto = new SwapTradeHeaderDto();
        dto.setTradeId(tradeId);
        dto.setTradeDate(header.getTradeDate());
        dto.setTradeTime(header.getTradeTime());
        dto.setDocumentType(header.getDocumentType());
        dto.setDocumentVersion(header.getDocumentVersion());
        if (header.getBuyerParty() != null) {
            dto.setBuyerParty(new com.power.base.datamodel.dto.common.PartyDto(
                    header.getBuyerParty().getId(),
                    header.getBuyerParty().getName(),
                    header.getBuyerParty().getRole()
            ));
        }
        if (header.getSellerParty() != null) {
            dto.setSellerParty(new com.power.base.datamodel.dto.common.PartyDto(
                    header.getSellerParty().getId(),
                    header.getSellerParty().getName(),
                    header.getSellerParty().getRole()
            ));
        }
        dto.setBusinessUnit(header.getBusinessUnit());
        dto.setBookStrategy(header.getBookStrategy());
        dto.setTraderName(header.getTraderName());
        dto.setAgreementId(header.getAgreementId());
        dto.setMarket(header.getMarket());
        dto.setCommodity(header.getCommodity());
        dto.setTransactionType(header.getTransactionType());
        dto.setReferenceZone(header.getReferenceZone());
        dto.setBuySellIndicator(header.getBuySellIndicator());
        dto.setAmendmentIndicator(header.isAmendmentIndicator());
        return dto;
    }

    private static SwapSettlementInfoDto toSettlementDto(SwapSettlementInfoEmbeddable embeddable) {
        if (embeddable == null) {
            return new SwapSettlementInfoDto();
        }
        SwapSettlementInfoDto dto = new SwapSettlementInfoDto();
        dto.setTotalNotional(embeddable.getTotalNotional());
        dto.setTotalNotionalUom(embeddable.getTotalNotionalUom());
        dto.setPricingMechanism(embeddable.getPricingMechanism());
        dto.setFixedPrice(embeddable.getFixedPrice());
        dto.setSpread(embeddable.getSpread());
        dto.setSettlementCurrency(embeddable.getSettlementCurrency());
        dto.setTradeCurrency(embeddable.getTradeCurrency());
        dto.setSettlementUom(embeddable.getSettlementUom());
        dto.setTradeUom(embeddable.getTradeUom());
        dto.setSettlementType(embeddable.getSettlementType());
        dto.setSettlementDate(embeddable.getSettlementDate());
        dto.setStartApplicabilityDate(embeddable.getStartApplicabilityDate());
        dto.setStartApplicabilityTime(embeddable.getStartApplicabilityTime());
        dto.setEndApplicabilityDate(embeddable.getEndApplicabilityDate());
        dto.setEndApplicabilityTime(embeddable.getEndApplicabilityTime());
        dto.setPaymentOffset(embeddable.getPaymentOffset());
        dto.setTotalExpectedValue(embeddable.getTotalExpectedValue());
        dto.setRounding(embeddable.getRounding());
        return dto;
    }

    private static SwapMetadataDto toMetadataDto(SwapMetadataEmbeddable embeddable) {
        if (embeddable == null) {
            return new SwapMetadataDto();
        }
        SwapMetadataDto dto = new SwapMetadataDto();
        dto.setEffectiveDate(embeddable.getEffectiveDate());
        dto.setTerminationDate(embeddable.getTerminationDate());
        dto.setGoverningLaw(embeddable.getGoverningLaw());
        dto.setClearable(embeddable.isClearable());
        dto.setUti(embeddable.getUti());
        return dto;
    }

    public String getTradeId() {
        return tradeId;
    }

    public SwapTradeHeaderEmbeddable getHeader() {
        return header;
    }

    public void setHeader(SwapTradeHeaderEmbeddable header) {
        this.header = header;
    }

    public SwapSettlementInfoEmbeddable getSettlementInfo() {
        return settlementInfo;
    }

    public void setSettlementInfo(SwapSettlementInfoEmbeddable settlementInfo) {
        this.settlementInfo = settlementInfo;
    }

    public SwapMetadataEmbeddable getMetadata() {
        return metadata;
    }

    public void setMetadata(SwapMetadataEmbeddable metadata) {
        this.metadata = metadata;
    }

    public List<SwapPeriodEntity> getPeriods() {
        return periods;
    }

    public void setPeriods(List<SwapPeriodEntity> periods) {
        this.periods = periods;
    }
}

