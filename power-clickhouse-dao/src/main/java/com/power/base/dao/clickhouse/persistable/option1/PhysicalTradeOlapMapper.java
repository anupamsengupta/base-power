package com.power.base.dao.clickhouse.persistable.option1;

import com.power.base.datamodel.dto.physicals.PhysicalLineItemDto;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper to convert PhysicalPowerTradeDto to PhysicalTradeOlap (Option 1).
 */
public class PhysicalTradeOlapMapper {

    public static PhysicalTradeOlap fromDto(PhysicalPowerTradeDto dto) {
        if (dto == null || dto.getTradeHeader() == null) {
            throw new IllegalArgumentException("PhysicalPowerTradeDto and tradeHeader cannot be null");
        }

        PhysicalTradeOlap olap = new PhysicalTradeOlap();
        var header = dto.getTradeHeader();
        var details = dto.getTradeDetails();
        var settlement = dto.getSettlementInfo();
        var metadata = dto.getMetadata();

        // Primary Key & Partitioning
        olap.setTradeId(header.getTradeId());
        olap.setTenantId(header.getTenantId());
        olap.setTradeDate(header.getTradeDate());
        olap.setTradeTime(header.getTradeTime());

        // Header Information
        olap.setDocumentType(header.getDocumentType() != null ? header.getDocumentType().name() : null);
        olap.setDocumentVersion(header.getDocumentVersion());
        if (header.getBuyerParty() != null) {
            olap.setBuyerPartyId(header.getBuyerParty().getId());
            olap.setBuyerPartyName(header.getBuyerParty().getName());
            olap.setBuyerPartyRole(header.getBuyerParty().getRole());
        }
        if (header.getSellerParty() != null) {
            olap.setSellerPartyId(header.getSellerParty().getId());
            olap.setSellerPartyName(header.getSellerParty().getName());
            olap.setSellerPartyRole(header.getSellerParty().getRole());
        }
        olap.setBusinessUnit(header.getBusinessUnit());
        olap.setBookStrategy(header.getBookStrategy());
        olap.setTraderName(header.getTraderName());
        olap.setAgreementId(header.getAgreementId());
        olap.setMarket(header.getMarket());
        olap.setCommodity(header.getCommodity());
        olap.setTransactionType(header.getTransactionType());
        olap.setDeliveryPoint(header.getDeliveryPoint());
        olap.setLoadType(header.getLoadType());
        olap.setBuySellIndicator(header.getBuySellIndicator() != null ? header.getBuySellIndicator().name() : null);
        olap.setAmendmentIndicator(header.isAmendmentIndicator());

        // Settlement Info
        if (settlement != null) {
            olap.setTotalVolume(settlement.getTotalVolume());
            olap.setTotalVolumeUom(settlement.getTotalVolumeUom());
            olap.setPricingMechanism(settlement.getPricingMechanism());
            olap.setSettlementPrice(settlement.getSettlementPrice());
            olap.setTradePrice(settlement.getTradePrice());
            olap.setSettlementCurrency(settlement.getSettlementCurrency());
            olap.setTradeCurrency(settlement.getTradeCurrency());
            olap.setSettlementUom(settlement.getSettlementUom());
            olap.setTradeUom(settlement.getTradeUom());
            olap.setStartApplicabilityDate(settlement.getStartApplicabilityDate());
            olap.setStartApplicabilityTime(settlement.getStartApplicabilityTime());
            olap.setEndApplicabilityDate(settlement.getEndApplicabilityDate());
            olap.setEndApplicabilityTime(settlement.getEndApplicabilityTime());
            olap.setPaymentEvent(settlement.getPaymentEvent());
            olap.setPaymentOffset(settlement.getPaymentOffset());
            olap.setTotalContractValue(settlement.getTotalContractValue());
            olap.setRounding(settlement.getRounding());
        }

        // Metadata
        if (metadata != null) {
            olap.setEffectiveDate(metadata.getEffectiveDate());
            olap.setTerminationDate(metadata.getTerminationDate());
            olap.setGoverningLaw(metadata.getGoverningLaw());
        }

        // Line Items as Arrays
        if (details != null && details.getLineItems() != null && !details.getLineItems().isEmpty()) {
            List<PhysicalLineItemDto> lineItems = details.getLineItems();
            olap.setLineItemPeriodStartDates(lineItems.stream()
                    .map(PhysicalLineItemDto::getPeriodStartDate)
                    .collect(Collectors.toList()));
            olap.setLineItemPeriodStartTimes(lineItems.stream()
                    .map(PhysicalLineItemDto::getPeriodStartTime)
                    .collect(Collectors.toList()));
            olap.setLineItemPeriodEndDates(lineItems.stream()
                    .map(PhysicalLineItemDto::getPeriodEndDate)
                    .collect(Collectors.toList()));
            olap.setLineItemPeriodEndTimes(lineItems.stream()
                    .map(PhysicalLineItemDto::getPeriodEndTime)
                    .collect(Collectors.toList()));
            olap.setLineItemDayHours(lineItems.stream()
                    .map(PhysicalLineItemDto::getDayHour)
                    .collect(Collectors.toList()));
            olap.setLineItemQuantities(lineItems.stream()
                    .map(PhysicalLineItemDto::getQuantity)
                    .collect(Collectors.toList()));
            olap.setLineItemUoms(lineItems.stream()
                    .map(PhysicalLineItemDto::getUom)
                    .collect(Collectors.toList()));
            olap.setLineItemCapacities(lineItems.stream()
                    .map(PhysicalLineItemDto::getCapacity)
                    .collect(Collectors.toList()));
            olap.setLineItemProfiles(lineItems.stream()
                    .map(item -> item.getProfile() != null ? item.getProfile().name() : null)
                    .collect(Collectors.toList()));

            // Compute total quantity
            olap.setTotalQuantity(lineItems.stream()
                    .mapToDouble(PhysicalLineItemDto::getQuantity)
                    .sum());
            olap.setTotalLineItems(lineItems.size());
        } else {
            olap.setLineItemPeriodStartDates(Collections.emptyList());
            olap.setLineItemPeriodStartTimes(Collections.emptyList());
            olap.setLineItemPeriodEndDates(Collections.emptyList());
            olap.setLineItemPeriodEndTimes(Collections.emptyList());
            olap.setLineItemDayHours(Collections.emptyList());
            olap.setLineItemQuantities(Collections.emptyList());
            olap.setLineItemUoms(Collections.emptyList());
            olap.setLineItemCapacities(Collections.emptyList());
            olap.setLineItemProfiles(Collections.emptyList());
            olap.setTotalQuantity(0.0);
            olap.setTotalLineItems(0);
        }

        // Settlement Items as Arrays
        if (settlement != null && settlement.getSettlementItems() != null && !settlement.getSettlementItems().isEmpty()) {
            List<PhysicalSettlementItemDto> settlementItems = settlement.getSettlementItems();
            olap.setSettlementItemIds(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getSettlementId)
                    .collect(Collectors.toList()));
            olap.setSettlementItemDeliveryDates(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getDeliveryDate)
                    .collect(Collectors.toList()));
            olap.setSettlementItemActualQuantities(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getActualQuantity)
                    .collect(Collectors.toList()));
            olap.setSettlementItemUoms(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getUom)
                    .collect(Collectors.toList()));
            olap.setSettlementItemSettlementPrices(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getSettlementPrice)
                    .collect(Collectors.toList()));
            olap.setSettlementItemTradePrices(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getTradePrice)
                    .collect(Collectors.toList()));
            olap.setSettlementItemSettlementUoms(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getSettlementUom)
                    .collect(Collectors.toList()));
            olap.setSettlementItemTradeUoms(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getTradeUom)
                    .collect(Collectors.toList()));
            olap.setSettlementItemDeviationAmounts(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getDeviationAmount)
                    .collect(Collectors.toList()));
            olap.setSettlementItemDeviationPenalties(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getDeviationPenalty)
                    .collect(Collectors.toList()));
            olap.setSettlementItemPeriodCashflows(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getPeriodCashflow)
                    .collect(Collectors.toList()));
            olap.setSettlementItemSettlementCurrencies(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getSettlementCurrency)
                    .collect(Collectors.toList()));
            olap.setSettlementItemTradeCurrencies(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getTradeCurrency)
                    .collect(Collectors.toList()));
            olap.setSettlementItemInvoiceStatuses(settlementItems.stream()
                    .map(PhysicalSettlementItemDto::getInvoiceStatus)
                    .collect(Collectors.toList()));
            olap.setSettlementItemReferencedLineItems(settlementItems.stream()
                    .map(item -> new ArrayList<>(item.getReferencedLineItems()))
                    .collect(Collectors.toList()));

            // Compute total cashflow
            olap.setTotalCashflow(settlementItems.stream()
                    .mapToDouble(PhysicalSettlementItemDto::getPeriodCashflow)
                    .sum());
            olap.setTotalSettlementItems(settlementItems.size());
        } else {
            olap.setSettlementItemIds(Collections.emptyList());
            olap.setSettlementItemDeliveryDates(Collections.emptyList());
            olap.setSettlementItemActualQuantities(Collections.emptyList());
            olap.setSettlementItemUoms(Collections.emptyList());
            olap.setSettlementItemSettlementPrices(Collections.emptyList());
            olap.setSettlementItemTradePrices(Collections.emptyList());
            olap.setSettlementItemSettlementUoms(Collections.emptyList());
            olap.setSettlementItemTradeUoms(Collections.emptyList());
            olap.setSettlementItemDeviationAmounts(Collections.emptyList());
            olap.setSettlementItemDeviationPenalties(Collections.emptyList());
            olap.setSettlementItemPeriodCashflows(Collections.emptyList());
            olap.setSettlementItemSettlementCurrencies(Collections.emptyList());
            olap.setSettlementItemTradeCurrencies(Collections.emptyList());
            olap.setSettlementItemInvoiceStatuses(Collections.emptyList());
            olap.setSettlementItemReferencedLineItems(Collections.emptyList());
            olap.setTotalCashflow(0.0);
            olap.setTotalSettlementItems(0);
        }

        // Audit metadata
        olap.setInsertedAt(LocalDateTime.now());
        olap.setUpdatedAt(LocalDateTime.now());

        return olap;
    }
}

