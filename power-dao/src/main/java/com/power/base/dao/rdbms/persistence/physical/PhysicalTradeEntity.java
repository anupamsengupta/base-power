package com.power.base.dao.rdbms.persistence.physical;

import com.power.base.datamodel.dto.physicals.PhysicalMetadataDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementInfoDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeDetailsDto;
import com.power.base.datamodel.dto.physicals.PhysicalTradeHeaderDto;
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
@Table(name = "physical_trades")
public class PhysicalTradeEntity {

    @Id
    @Column(name = "trade_id", nullable = false, updatable = false, length = 64)
    private String tradeId;

    @Embedded
    private PhysicalTradeHeaderEmbeddable header;

    @Embedded
    private PhysicalMetadataEmbeddable metadata;

    @Column(name = "settlement_currency")
    private String settlementCurrency;

    @Column(name = "trade_currency")
    private String tradeCurrency;

    @Column(name = "settlement_uom")
    private String settlementUom;

    @Column(name = "trade_uom")
    private String tradeUom;

    @Column(name = "settlement_price")
    private Double settlementPrice;

    @Column(name = "trade_price")
    private Double tradePrice;

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhysicalLineItemEntity> lineItems = new ArrayList<>();

    @OneToMany(mappedBy = "trade", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PhysicalSettlementItemEntity> settlementItems = new ArrayList<>();

    public static PhysicalTradeEntity fromDto(PhysicalPowerTradeDto dto) {
        PhysicalTradeEntity entity = new PhysicalTradeEntity();
        entity.tradeId = dto.getTradeHeader().getTradeId();

        entity.header = toHeaderEmbeddable(dto.getTradeHeader());
        entity.metadata = toMetadataEmbeddable(dto.getMetadata());
        if (dto.getSettlementInfo() != null) {
            var settlementInfo = dto.getSettlementInfo();
            entity.settlementCurrency = settlementInfo.getSettlementCurrency();
            entity.tradeCurrency = settlementInfo.getTradeCurrency();
            entity.settlementUom = settlementInfo.getSettlementUom();
            entity.tradeUom = settlementInfo.getTradeUom();
            entity.settlementPrice = settlementInfo.getSettlementPrice();
            entity.tradePrice = settlementInfo.getTradePrice();
        } else {
            entity.settlementCurrency = null;
            entity.tradeCurrency = null;
            entity.settlementUom = null;
            entity.tradeUom = null;
            entity.settlementPrice = null;
            entity.tradePrice = null;
        }

        entity.lineItems.clear();
        if (dto.getTradeDetails() != null && dto.getTradeDetails().getLineItems() != null) {
            dto.getTradeDetails().getLineItems().forEach(lineItemDto -> {
                PhysicalLineItemEntity line = PhysicalLineItemEntity.fromDto(lineItemDto, entity);
                entity.lineItems.add(line);
            });
        }

        entity.settlementItems.clear();
        if (dto.getSettlementInfo() != null && dto.getSettlementInfo().getSettlementItems() != null) {
            dto.getSettlementInfo().getSettlementItems().forEach(itemDto -> {
                PhysicalSettlementItemEntity settlementItem = PhysicalSettlementItemEntity.fromDto(itemDto, entity);
                entity.settlementItems.add(settlementItem);
            });
        }

        return entity;
    }

    public PhysicalPowerTradeDto toDto() {
        PhysicalTradeHeaderDto headerDto = toHeaderDto(header, tradeId);
        PhysicalTradeDetailsDto detailsDto = new PhysicalTradeDetailsDto(
                lineItems.stream().map(PhysicalLineItemEntity::toDto).collect(Collectors.toList())
        );

        List<PhysicalSettlementItemDto> settlementItemDtos = settlementItems.stream()
                .map(PhysicalSettlementItemEntity::toDto)
                .collect(Collectors.toList());

        PhysicalSettlementInfoDto settlementInfoDto = new PhysicalSettlementInfoDto(
                settlementInfoDtosTotalVolume(settlementItemDtos),
                null,
                null,
                settlementPrice == null ? 0d : settlementPrice,
                tradePrice == null ? 0d : tradePrice,
                settlementCurrency,
                tradeCurrency,
                settlementUom,
                tradeUom,
                null,
                null,
                null,
                null,
                null,
                0d,
                0d,
                0d,
                settlementItemDtos
        );
        PhysicalMetadataDto metadataDto = metadata == null ? new PhysicalMetadataDto() : new PhysicalMetadataDto(
                metadata.getEffectiveDate(),
                metadata.getTerminationDate(),
                metadata.getGoverningLaw()
        );

        return new PhysicalPowerTradeDto(
                headerDto,
                detailsDto,
                settlementInfoDto,
                metadataDto
        );
    }

    private static PhysicalTradeHeaderEmbeddable toHeaderEmbeddable(PhysicalTradeHeaderDto headerDto) {
        PhysicalTradeHeaderEmbeddable headerEmbeddable = new PhysicalTradeHeaderEmbeddable();
        headerEmbeddable.setTradeDate(headerDto.getTradeDate());
        headerEmbeddable.setTradeTime(headerDto.getTradeTime());
        headerEmbeddable.setDocumentType(headerDto.getDocumentType());
        headerEmbeddable.setDocumentVersion(headerDto.getDocumentVersion());
        if (headerDto.getBuyerParty() != null) {
            headerEmbeddable.setBuyerParty(new PhysicalPartyEmbeddable(
                    headerDto.getBuyerParty().getId(),
                    headerDto.getBuyerParty().getName(),
                    headerDto.getBuyerParty().getRole()
            ));
        }
        if (headerDto.getSellerParty() != null) {
            headerEmbeddable.setSellerParty(new PhysicalPartyEmbeddable(
                    headerDto.getSellerParty().getId(),
                    headerDto.getSellerParty().getName(),
                    headerDto.getSellerParty().getRole()
            ));
        }
        headerEmbeddable.setBusinessUnit(headerDto.getBusinessUnit());
        headerEmbeddable.setBookStrategy(headerDto.getBookStrategy());
        headerEmbeddable.setTraderName(headerDto.getTraderName());
        headerEmbeddable.setAgreementId(headerDto.getAgreementId());
        headerEmbeddable.setMarket(headerDto.getMarket());
        headerEmbeddable.setCommodity(headerDto.getCommodity());
        headerEmbeddable.setTransactionType(headerDto.getTransactionType());
        headerEmbeddable.setDeliveryPoint(headerDto.getDeliveryPoint());
        headerEmbeddable.setLoadType(headerDto.getLoadType());
        headerEmbeddable.setBuySellIndicator(headerDto.getBuySellIndicator());
        headerEmbeddable.setAmendmentIndicator(headerDto.isAmendmentIndicator());
        return headerEmbeddable;
    }

    private static PhysicalMetadataEmbeddable toMetadataEmbeddable(PhysicalMetadataDto dto) {
        if (dto == null) {
            return null;
        }
        PhysicalMetadataEmbeddable metadata = new PhysicalMetadataEmbeddable();
        metadata.setEffectiveDate(dto.getEffectiveDate());
        metadata.setTerminationDate(dto.getTerminationDate());
        metadata.setGoverningLaw(dto.getGoverningLaw());
        return metadata;
    }

    private static PhysicalTradeHeaderDto toHeaderDto(PhysicalTradeHeaderEmbeddable header, String tradeId) {
        PhysicalTradeHeaderDto dto = new PhysicalTradeHeaderDto();
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
        dto.setDeliveryPoint(header.getDeliveryPoint());
        dto.setLoadType(header.getLoadType());
        dto.setBuySellIndicator(header.getBuySellIndicator());
        dto.setAmendmentIndicator(header.isAmendmentIndicator());
        return dto;
    }

    private static double settlementInfoDtosTotalVolume(List<PhysicalSettlementItemDto> items) {
        return items.stream().mapToDouble(PhysicalSettlementItemDto::getActualQuantity).sum();
    }

    public String getTradeId() {
        return tradeId;
    }

    public PhysicalTradeHeaderEmbeddable getHeader() {
        return header;
    }

    public void setHeader(PhysicalTradeHeaderEmbeddable header) {
        this.header = header;
    }

    public PhysicalMetadataEmbeddable getMetadata() {
        return metadata;
    }

    public void setMetadata(PhysicalMetadataEmbeddable metadata) {
        this.metadata = metadata;
    }

    public List<PhysicalLineItemEntity> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<PhysicalLineItemEntity> lineItems) {
        this.lineItems = lineItems;
    }

    public List<PhysicalSettlementItemEntity> getSettlementItems() {
        return settlementItems;
    }

    public void setSettlementItems(List<PhysicalSettlementItemEntity> settlementItems) {
        this.settlementItems = settlementItems;
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
}

