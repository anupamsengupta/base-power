package com.power.base.dao.rdbms.persistence.physical;

import com.power.base.datamodel.dto.physicals.PhysicalSettlementItemDto;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "physical_settlement_items")
public class PhysicalSettlementItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id")
    private PhysicalTradeEntity trade;

    @Column(name = "settlement_id", nullable = false)
    private String settlementId;

    @ElementCollection
    @CollectionTable(name = "physical_settlement_line_refs", joinColumns = @JoinColumn(name = "settlement_item_id"))
    @Column(name = "line_item_ref")
    private List<String> referencedLineItems = new ArrayList<>();

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "actual_quantity")
    private double actualQuantity;

    @Column(name = "uom")
    private String uom;

    @Column(name = "settlement_price")
    private double settlementPrice;

    @Column(name = "trade_price")
    private double tradePrice;

    @Column(name = "settlement_uom")
    private String settlementUom;

    @Column(name = "trade_uom")
    private String tradeUom;

    @Column(name = "deviation_amount")
    private double deviationAmount;

    @Column(name = "deviation_penalty")
    private double deviationPenalty;

    @Column(name = "period_cashflow")
    private double periodCashflow;

    @Column(name = "settlement_currency")
    private String settlementCurrency;

    @Column(name = "trade_currency")
    private String tradeCurrency;

    @Column(name = "invoice_status")
    private String invoiceStatus;

    public static PhysicalSettlementItemEntity fromDto(PhysicalSettlementItemDto dto, PhysicalTradeEntity trade) {
        PhysicalSettlementItemEntity entity = new PhysicalSettlementItemEntity();
        entity.setTrade(trade);
        entity.setSettlementId(dto.getSettlementId());
        entity.setReferencedLineItems(dto.getReferencedLineItems() == null
                ? new ArrayList<>()
                : new ArrayList<>(dto.getReferencedLineItems()));
        entity.setDeliveryDate(dto.getDeliveryDate());
        entity.setActualQuantity(dto.getActualQuantity());
        entity.setUom(dto.getUom());
        entity.setSettlementPrice(dto.getSettlementPrice());
        entity.setTradePrice(dto.getTradePrice());
        entity.setSettlementUom(dto.getSettlementUom());
        entity.setDeviationAmount(dto.getDeviationAmount());
        entity.setDeviationPenalty(dto.getDeviationPenalty());
        entity.setPeriodCashflow(dto.getPeriodCashflow());
        entity.setSettlementCurrency(dto.getSettlementCurrency());
        entity.setTradeCurrency(dto.getTradeCurrency());
        entity.setTradeUom(dto.getTradeUom());
        entity.setInvoiceStatus(dto.getInvoiceStatus());
        return entity;
    }

    public PhysicalSettlementItemDto toDto() {
        return new PhysicalSettlementItemDto(
                settlementId,
                new ArrayList<>(referencedLineItems),
                deliveryDate,
                actualQuantity,
                uom,
                settlementPrice,
                tradePrice,
                settlementUom,
                tradeUom,
                deviationAmount,
                deviationPenalty,
                periodCashflow,
                settlementCurrency,
                tradeCurrency,
                invoiceStatus
        );
    }

    public Long getId() {
        return id;
    }

    public PhysicalTradeEntity getTrade() {
        return trade;
    }

    public void setTrade(PhysicalTradeEntity trade) {
        this.trade = trade;
    }

    public String getSettlementId() {
        return settlementId;
    }

    public void setSettlementId(String settlementId) {
        this.settlementId = settlementId;
    }

    public List<String> getReferencedLineItems() {
        return referencedLineItems;
    }

    public void setReferencedLineItems(List<String> referencedLineItems) {
        this.referencedLineItems = referencedLineItems == null ? new ArrayList<>() : referencedLineItems;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDate deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public double getSettlementPrice() {
        return settlementPrice;
    }

    public void setSettlementPrice(double settlementPrice) {
        this.settlementPrice = settlementPrice;
    }

    public double getTradePrice() {
        return tradePrice;
    }

    public void setTradePrice(double tradePrice) {
        this.tradePrice = tradePrice;
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

    public double getDeviationAmount() {
        return deviationAmount;
    }

    public void setDeviationAmount(double deviationAmount) {
        this.deviationAmount = deviationAmount;
    }

    public double getDeviationPenalty() {
        return deviationPenalty;
    }

    public void setDeviationPenalty(double deviationPenalty) {
        this.deviationPenalty = deviationPenalty;
    }

    public double getPeriodCashflow() {
        return periodCashflow;
    }

    public void setPeriodCashflow(double periodCashflow) {
        this.periodCashflow = periodCashflow;
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

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }
}

