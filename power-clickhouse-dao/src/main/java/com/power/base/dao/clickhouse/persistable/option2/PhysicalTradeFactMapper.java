package com.power.base.dao.clickhouse.persistable.option2;

import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper to convert PhysicalPowerTradeDto to Option 2 persistable entities.
 */
public class PhysicalTradeFactMapper {

    public static PhysicalTradeFact toTradeFact(PhysicalPowerTradeDto dto) {
        if (dto == null || dto.getTradeHeader() == null) {
            throw new IllegalArgumentException("PhysicalPowerTradeDto and tradeHeader cannot be null");
        }

        PhysicalTradeFact fact = new PhysicalTradeFact();
        var header = dto.getTradeHeader();
        var settlement = dto.getSettlementInfo();
        var metadata = dto.getMetadata();

        // Primary Key & Partitioning
        fact.setTradeId(header.getTradeId());
        fact.setTenantId(header.getTenantId());
        fact.setTradeDate(header.getTradeDate());
        fact.setTradeTime(header.getTradeTime());

        // Header Information
        fact.setDocumentType(header.getDocumentType() != null ? header.getDocumentType().name() : null);
        fact.setDocumentVersion(header.getDocumentVersion());
        if (header.getBuyerParty() != null) {
            fact.setBuyerPartyId(header.getBuyerParty().getId());
            fact.setBuyerPartyName(header.getBuyerParty().getName());
            fact.setBuyerPartyRole(header.getBuyerParty().getRole());
        }
        if (header.getSellerParty() != null) {
            fact.setSellerPartyId(header.getSellerParty().getId());
            fact.setSellerPartyName(header.getSellerParty().getName());
            fact.setSellerPartyRole(header.getSellerParty().getRole());
        }
        fact.setBusinessUnit(header.getBusinessUnit());
        fact.setBookStrategy(header.getBookStrategy());
        fact.setTraderName(header.getTraderName());
        fact.setAgreementId(header.getAgreementId());
        fact.setMarket(header.getMarket());
        fact.setCommodity(header.getCommodity());
        fact.setTransactionType(header.getTransactionType());
        fact.setDeliveryPoint(header.getDeliveryPoint());
        fact.setLoadType(header.getLoadType());
        fact.setBuySellIndicator(header.getBuySellIndicator() != null ? header.getBuySellIndicator().name() : null);
        fact.setAmendmentIndicator(header.isAmendmentIndicator());

        // Settlement Info
        if (settlement != null) {
            fact.setTotalVolume(settlement.getTotalVolume());
            fact.setTotalVolumeUom(settlement.getTotalVolumeUom());
            fact.setPricingMechanism(settlement.getPricingMechanism());
            fact.setSettlementPrice(settlement.getSettlementPrice());
            fact.setTradePrice(settlement.getTradePrice());
            fact.setSettlementCurrency(settlement.getSettlementCurrency());
            fact.setTradeCurrency(settlement.getTradeCurrency());
            fact.setSettlementUom(settlement.getSettlementUom());
            fact.setTradeUom(settlement.getTradeUom());
            fact.setStartApplicabilityDate(settlement.getStartApplicabilityDate());
            fact.setStartApplicabilityTime(settlement.getStartApplicabilityTime());
            fact.setEndApplicabilityDate(settlement.getEndApplicabilityDate());
            fact.setEndApplicabilityTime(settlement.getEndApplicabilityTime());
            fact.setPaymentEvent(settlement.getPaymentEvent());
            fact.setPaymentOffset(settlement.getPaymentOffset());
            fact.setTotalContractValue(settlement.getTotalContractValue());
            fact.setRounding(settlement.getRounding());
        }

        // Metadata
        if (metadata != null) {
            fact.setEffectiveDate(metadata.getEffectiveDate());
            fact.setTerminationDate(metadata.getTerminationDate());
            fact.setGoverningLaw(metadata.getGoverningLaw());
        }

        return fact;
    }

    public static List<PhysicalTradeLineItemFact> toLineItemFacts(PhysicalPowerTradeDto dto) {
        if (dto == null || dto.getTradeDetails() == null || 
            dto.getTradeDetails().getLineItems() == null || 
            dto.getTradeDetails().getLineItems().isEmpty()) {
            return new ArrayList<>();
        }

        var header = dto.getTradeHeader();
        List<PhysicalTradeLineItemFact> facts = new ArrayList<>();
        List<PhysicalLineItemDto> lineItems = dto.getTradeDetails().getLineItems();

        for (int i = 0; i < lineItems.size(); i++) {
            PhysicalLineItemDto item = lineItems.get(i);
            PhysicalTradeLineItemFact fact = new PhysicalTradeLineItemFact();
            
            fact.setTradeId(header.getTradeId());
            fact.setLineItemIndex(i);
            fact.setPeriodStartDate(item.getPeriodStartDate());
            fact.setPeriodStartTime(item.getPeriodStartTime());
            fact.setPeriodEndDate(item.getPeriodEndDate());
            fact.setPeriodEndTime(item.getPeriodEndTime());
            fact.setDayHour(item.getDayHour());
            fact.setQuantity(item.getQuantity());
            fact.setUom(item.getUom());
            fact.setCapacity(item.getCapacity());
            fact.setProfile(item.getProfile() != null ? item.getProfile().name() : null);

            // Denormalized trade info
            fact.setTenantId(header.getTenantId());
            fact.setTradeDate(header.getTradeDate());
            fact.setMarket(header.getMarket());
            fact.setBusinessUnit(header.getBusinessUnit());

            facts.add(fact);
        }

        return facts;
    }

    public static List<PhysicalTradeSettlementItemFact> toSettlementItemFacts(PhysicalPowerTradeDto dto) {
        if (dto == null || dto.getSettlementInfo() == null ||
            dto.getSettlementInfo().getSettlementItems() == null ||
            dto.getSettlementInfo().getSettlementItems().isEmpty()) {
            return new ArrayList<>();
        }

        var header = dto.getTradeHeader();
        List<PhysicalTradeSettlementItemFact> facts = new ArrayList<>();
        List<PhysicalSettlementItemDto> settlementItems = dto.getSettlementInfo().getSettlementItems();

        for (int i = 0; i < settlementItems.size(); i++) {
            PhysicalSettlementItemDto item = settlementItems.get(i);
            PhysicalTradeSettlementItemFact fact = new PhysicalTradeSettlementItemFact();

            fact.setTradeId(header.getTradeId());
            fact.setSettlementItemIndex(i);
            fact.setSettlementId(item.getSettlementId());
            fact.setDeliveryDate(item.getDeliveryDate());
            fact.setActualQuantity(item.getActualQuantity());
            fact.setUom(item.getUom());
            fact.setSettlementPrice(item.getSettlementPrice());
            fact.setTradePrice(item.getTradePrice());
            fact.setSettlementUom(item.getSettlementUom());
            fact.setTradeUom(item.getTradeUom());
            fact.setDeviationAmount(item.getDeviationAmount());
            fact.setDeviationPenalty(item.getDeviationPenalty());
            fact.setPeriodCashflow(item.getPeriodCashflow());
            fact.setSettlementCurrency(item.getSettlementCurrency());
            fact.setTradeCurrency(item.getTradeCurrency());
            fact.setInvoiceStatus(item.getInvoiceStatus());
            fact.setReferencedLineItems(new ArrayList<>(item.getReferencedLineItems()));

            // Denormalized trade info
            fact.setTenantId(header.getTenantId());
            fact.setTradeDate(header.getTradeDate());
            fact.setMarket(header.getMarket());
            fact.setBusinessUnit(header.getBusinessUnit());

            facts.add(fact);
        }

        return facts;
    }
}

