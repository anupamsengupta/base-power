package com.power.base.dao.clickhouse.dao.option1;

import com.power.base.dao.clickhouse.persistable.option1.PhysicalTradeOlap;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ClickHouse DAO implementation for Option 1: Single flat table with arrays.
 */
public class PhysicalTradeOlapDaoImpl implements PhysicalTradeOlapDao {

    private static final String TABLE_NAME = "physical_trades_olap";
    private static final String INSERT_SQL = buildInsertSql();
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE tenant_id = ? AND trade_id = ?";
    private static final String SELECT_BY_DATE_RANGE_SQL = 
        "SELECT * FROM " + TABLE_NAME + " WHERE tenant_id = ? AND trade_date >= ? AND trade_date <= ? ORDER BY trade_date, trade_id";
    private static final String DELETE_BY_ID_SQL = 
        "ALTER TABLE " + TABLE_NAME + " DELETE WHERE tenant_id = ? AND trade_id = ?";

    private final DataSource dataSource;

    public PhysicalTradeOlapDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PhysicalTradeOlap insert(PhysicalTradeOlap trade) {
        return batchInsert(Collections.singletonList(trade)) > 0 ? trade : null;
    }

    @Override
    public int batchInsert(List<PhysicalTradeOlap> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0;
        }

        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_SQL)) {

            for (PhysicalTradeOlap trade : trades) {
                setInsertParameters(stmt, trade);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert trades into ClickHouse", e);
        }
    }

    @Override
    public PhysicalTradeOlap findByTradeId(String tenantId, String tradeId) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setString(2, tradeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find trade by ID", e);
        }
    }

    @Override
    public List<PhysicalTradeOlap> findByDateRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_BY_DATE_RANGE_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setObject(2, startDate);
            stmt.setObject(3, endDate);

            List<PhysicalTradeOlap> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find trades by date range", e);
        }
    }

    @Override
    public int deleteByTradeId(String tenantId, String tradeId) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(DELETE_BY_ID_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setString(2, tradeId);

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete trade by ID", e);
        }
    }

    private static String buildInsertSql() {
        return "INSERT INTO " + TABLE_NAME + " (" +
            "trade_id, tenant_id, trade_date, trade_time, " +
            "document_type, document_version, buyer_party_id, buyer_party_name, buyer_party_role, " +
            "seller_party_id, seller_party_name, seller_party_role, business_unit, book_strategy, " +
            "trader_name, agreement_id, market, commodity, transaction_type, delivery_point, " +
            "load_type, buy_sell_indicator, amendment_indicator, " +
            "total_volume, total_volume_uom, pricing_mechanism, settlement_price, trade_price, " +
            "settlement_currency, trade_currency, settlement_uom, trade_uom, " +
            "start_applicability_date, start_applicability_time, end_applicability_date, end_applicability_time, " +
            "payment_event, payment_offset, total_contract_value, rounding, " +
            "effective_date, termination_date, governing_law, " +
            "line_item_period_start_dates, line_item_period_start_times, line_item_period_end_dates, " +
            "line_item_period_end_times, line_item_day_hours, line_item_quantities, line_item_uoms, " +
            "line_item_capacities, line_item_profiles, " +
            "settlement_item_ids, settlement_item_delivery_dates, settlement_item_actual_quantities, " +
            "settlement_item_uoms, settlement_item_settlement_prices, settlement_item_trade_prices, " +
            "settlement_item_settlement_uoms, settlement_item_trade_uoms, settlement_item_deviation_amounts, " +
            "settlement_item_deviation_penalties, settlement_item_period_cashflows, " +
            "settlement_item_settlement_currencies, settlement_item_trade_currencies, " +
            "settlement_item_invoice_statuses, settlement_item_referenced_line_items, " +
            "total_line_items, total_settlement_items, total_quantity, total_cashflow, " +
            "inserted_at, updated_at, source_system, version" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?)";
    }

    private void setInsertParameters(PreparedStatement stmt, PhysicalTradeOlap trade) throws SQLException {
        int paramIndex = 1;

        // Primary Key & Partitioning
        stmt.setString(paramIndex++, trade.getTradeId());
        stmt.setString(paramIndex++, trade.getTenantId());
        stmt.setObject(paramIndex++, trade.getTradeDate());
        stmt.setObject(paramIndex++, trade.getTradeTime());

        // Header Information
        stmt.setString(paramIndex++, trade.getDocumentType());
        stmt.setString(paramIndex++, trade.getDocumentVersion());
        stmt.setString(paramIndex++, trade.getBuyerPartyId());
        stmt.setString(paramIndex++, trade.getBuyerPartyName());
        stmt.setString(paramIndex++, trade.getBuyerPartyRole());
        stmt.setString(paramIndex++, trade.getSellerPartyId());
        stmt.setString(paramIndex++, trade.getSellerPartyName());
        stmt.setString(paramIndex++, trade.getSellerPartyRole());
        stmt.setString(paramIndex++, trade.getBusinessUnit());
        stmt.setString(paramIndex++, trade.getBookStrategy());
        stmt.setString(paramIndex++, trade.getTraderName());
        stmt.setString(paramIndex++, trade.getAgreementId());
        stmt.setString(paramIndex++, trade.getMarket());
        stmt.setString(paramIndex++, trade.getCommodity());
        stmt.setString(paramIndex++, trade.getTransactionType());
        stmt.setString(paramIndex++, trade.getDeliveryPoint());
        stmt.setString(paramIndex++, trade.getLoadType());
        stmt.setString(paramIndex++, trade.getBuySellIndicator());
        stmt.setBoolean(paramIndex++, trade.isAmendmentIndicator());

        // Settlement Info
        stmt.setObject(paramIndex++, trade.getTotalVolume());
        stmt.setString(paramIndex++, trade.getTotalVolumeUom());
        stmt.setString(paramIndex++, trade.getPricingMechanism());
        stmt.setObject(paramIndex++, trade.getSettlementPrice());
        stmt.setObject(paramIndex++, trade.getTradePrice());
        stmt.setString(paramIndex++, trade.getSettlementCurrency());
        stmt.setString(paramIndex++, trade.getTradeCurrency());
        stmt.setString(paramIndex++, trade.getSettlementUom());
        stmt.setString(paramIndex++, trade.getTradeUom());
        stmt.setObject(paramIndex++, trade.getStartApplicabilityDate());
        stmt.setObject(paramIndex++, trade.getStartApplicabilityTime());
        stmt.setObject(paramIndex++, trade.getEndApplicabilityDate());
        stmt.setObject(paramIndex++, trade.getEndApplicabilityTime());
        stmt.setString(paramIndex++, trade.getPaymentEvent());
        stmt.setObject(paramIndex++, trade.getPaymentOffset());
        stmt.setObject(paramIndex++, trade.getTotalContractValue());
        stmt.setObject(paramIndex++, trade.getRounding());

        // Metadata
        stmt.setObject(paramIndex++, trade.getEffectiveDate());
        stmt.setObject(paramIndex++, trade.getTerminationDate());
        stmt.setString(paramIndex++, trade.getGoverningLaw());

        // Line Items Arrays
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Date", trade.getLineItemPeriodStartDates()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "DateTime64(3)", trade.getLineItemPeriodStartTimes()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Date", trade.getLineItemPeriodEndDates()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "DateTime64(3)", trade.getLineItemPeriodEndTimes()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getLineItemDayHours()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getLineItemQuantities()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getLineItemUoms()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getLineItemCapacities()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getLineItemProfiles()));

        // Settlement Items Arrays
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemIds()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Date", trade.getSettlementItemDeliveryDates()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getSettlementItemActualQuantities()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemUoms()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getSettlementItemSettlementPrices()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getSettlementItemTradePrices()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemSettlementUoms()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemTradeUoms()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getSettlementItemDeviationAmounts()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getSettlementItemDeviationPenalties()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "Float64", trade.getSettlementItemPeriodCashflows()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemSettlementCurrencies()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemTradeCurrencies()));
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", trade.getSettlementItemInvoiceStatuses()));
        // Nested array for referenced line items
        stmt.setArray(paramIndex++, createNestedArray(stmt.getConnection(), trade.getSettlementItemReferencedLineItems()));

        // Computed columns
        stmt.setInt(paramIndex++, trade.getTotalLineItems() != null ? trade.getTotalLineItems() : 0);
        stmt.setInt(paramIndex++, trade.getTotalSettlementItems() != null ? trade.getTotalSettlementItems() : 0);
        stmt.setObject(paramIndex++, trade.getTotalQuantity());
        stmt.setObject(paramIndex++, trade.getTotalCashflow());

        // Audit metadata
        stmt.setObject(paramIndex++, trade.getInsertedAt());
        stmt.setObject(paramIndex++, trade.getUpdatedAt());
        stmt.setString(paramIndex++, trade.getSourceSystem());
        stmt.setInt(paramIndex++, trade.getVersion() != null ? trade.getVersion() : 1);
    }

    private java.sql.Array createArray(java.sql.Connection conn, String type, List<?> values) throws SQLException {
        return com.power.base.dao.clickhouse.util.ClickHouseArrayUtil.createArray(conn, type, values);
    }

    private java.sql.Array createNestedArray(java.sql.Connection conn, List<List<String>> nestedList) throws SQLException {
        return com.power.base.dao.clickhouse.util.ClickHouseArrayUtil.createNestedArray(conn, nestedList);
    }

    private PhysicalTradeOlap mapResultSetToEntity(ResultSet rs) throws SQLException {
        PhysicalTradeOlap trade = new PhysicalTradeOlap();

        // Primary Key & Partitioning
        trade.setTradeId(rs.getString("trade_id"));
        trade.setTenantId(rs.getString("tenant_id"));
        trade.setTradeDate(rs.getObject("trade_date", LocalDate.class));
        trade.setTradeTime(rs.getObject("trade_time", Instant.class));

        // Header Information
        trade.setDocumentType(rs.getString("document_type"));
        trade.setDocumentVersion(rs.getString("document_version"));
        trade.setBuyerPartyId(rs.getString("buyer_party_id"));
        trade.setBuyerPartyName(rs.getString("buyer_party_name"));
        trade.setBuyerPartyRole(rs.getString("buyer_party_role"));
        trade.setSellerPartyId(rs.getString("seller_party_id"));
        trade.setSellerPartyName(rs.getString("seller_party_name"));
        trade.setSellerPartyRole(rs.getString("seller_party_role"));
        trade.setBusinessUnit(rs.getString("business_unit"));
        trade.setBookStrategy(rs.getString("book_strategy"));
        trade.setTraderName(rs.getString("trader_name"));
        trade.setAgreementId(rs.getString("agreement_id"));
        trade.setMarket(rs.getString("market"));
        trade.setCommodity(rs.getString("commodity"));
        trade.setTransactionType(rs.getString("transaction_type"));
        trade.setDeliveryPoint(rs.getString("delivery_point"));
        trade.setLoadType(rs.getString("load_type"));
        trade.setBuySellIndicator(rs.getString("buy_sell_indicator"));
        trade.setAmendmentIndicator(rs.getBoolean("amendment_indicator"));

        // Settlement Info
        trade.setTotalVolume(rs.getObject("total_volume", Double.class));
        trade.setTotalVolumeUom(rs.getString("total_volume_uom"));
        trade.setPricingMechanism(rs.getString("pricing_mechanism"));
        trade.setSettlementPrice(rs.getObject("settlement_price", Double.class));
        trade.setTradePrice(rs.getObject("trade_price", Double.class));
        trade.setSettlementCurrency(rs.getString("settlement_currency"));
        trade.setTradeCurrency(rs.getString("trade_currency"));
        trade.setSettlementUom(rs.getString("settlement_uom"));
        trade.setTradeUom(rs.getString("trade_uom"));
        trade.setStartApplicabilityDate(rs.getObject("start_applicability_date", LocalDate.class));
        trade.setStartApplicabilityTime(rs.getObject("start_applicability_time", Instant.class));
        trade.setEndApplicabilityDate(rs.getObject("end_applicability_date", LocalDate.class));
        trade.setEndApplicabilityTime(rs.getObject("end_applicability_time", Instant.class));
        trade.setPaymentEvent(rs.getString("payment_event"));
        trade.setPaymentOffset(rs.getObject("payment_offset", Double.class));
        trade.setTotalContractValue(rs.getObject("total_contract_value", Double.class));
        trade.setRounding(rs.getObject("rounding", Double.class));

        // Metadata
        trade.setEffectiveDate(rs.getObject("effective_date", LocalDate.class));
        trade.setTerminationDate(rs.getObject("termination_date", LocalDate.class));
        trade.setGoverningLaw(rs.getString("governing_law"));

        // Arrays - Note: ClickHouse JDBC array handling may vary
        // This is a simplified version; actual implementation may need ClickHouse-specific array handling
        trade.setLineItemPeriodStartDates(extractArray(rs, "line_item_period_start_dates", LocalDate.class));
        trade.setLineItemPeriodStartTimes(extractArray(rs, "line_item_period_start_times", Instant.class));
        trade.setLineItemPeriodEndDates(extractArray(rs, "line_item_period_end_dates", LocalDate.class));
        trade.setLineItemPeriodEndTimes(extractArray(rs, "line_item_period_end_times", Instant.class));
        trade.setLineItemDayHours(extractArray(rs, "line_item_day_hours", String.class));
        trade.setLineItemQuantities(extractArray(rs, "line_item_quantities", Double.class));
        trade.setLineItemUoms(extractArray(rs, "line_item_uoms", String.class));
        trade.setLineItemCapacities(extractArray(rs, "line_item_capacities", Double.class));
        trade.setLineItemProfiles(extractArray(rs, "line_item_profiles", String.class));

        trade.setSettlementItemIds(extractArray(rs, "settlement_item_ids", String.class));
        trade.setSettlementItemDeliveryDates(extractArray(rs, "settlement_item_delivery_dates", LocalDate.class));
        trade.setSettlementItemActualQuantities(extractArray(rs, "settlement_item_actual_quantities", Double.class));
        trade.setSettlementItemUoms(extractArray(rs, "settlement_item_uoms", String.class));
        trade.setSettlementItemSettlementPrices(extractArray(rs, "settlement_item_settlement_prices", Double.class));
        trade.setSettlementItemTradePrices(extractArray(rs, "settlement_item_trade_prices", Double.class));
        trade.setSettlementItemSettlementUoms(extractArray(rs, "settlement_item_settlement_uoms", String.class));
        trade.setSettlementItemTradeUoms(extractArray(rs, "settlement_item_trade_uoms", String.class));
        trade.setSettlementItemDeviationAmounts(extractArray(rs, "settlement_item_deviation_amounts", Double.class));
        trade.setSettlementItemDeviationPenalties(extractArray(rs, "settlement_item_deviation_penalties", Double.class));
        trade.setSettlementItemPeriodCashflows(extractArray(rs, "settlement_item_period_cashflows", Double.class));
        trade.setSettlementItemSettlementCurrencies(extractArray(rs, "settlement_item_settlement_currencies", String.class));
        trade.setSettlementItemTradeCurrencies(extractArray(rs, "settlement_item_trade_currencies", String.class));
        trade.setSettlementItemInvoiceStatuses(extractArray(rs, "settlement_item_invoice_statuses", String.class));
        trade.setSettlementItemReferencedLineItems(extractNestedArray(rs, "settlement_item_referenced_line_items"));

        // Computed columns
        trade.setTotalLineItems(rs.getObject("total_line_items", Integer.class));
        trade.setTotalSettlementItems(rs.getObject("total_settlement_items", Integer.class));
        trade.setTotalQuantity(rs.getObject("total_quantity", Double.class));
        trade.setTotalCashflow(rs.getObject("total_cashflow", Double.class));

        // Audit metadata
        trade.setInsertedAt(rs.getObject("inserted_at", LocalDateTime.class));
        trade.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        trade.setSourceSystem(rs.getString("source_system"));
        trade.setVersion(rs.getObject("version", Integer.class));

        return trade;
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> extractArray(ResultSet rs, String columnName, Class<T> type) throws SQLException {
        return com.power.base.dao.clickhouse.util.ClickHouseArrayUtil.extractArray(rs, columnName, type);
    }

    private List<List<String>> extractNestedArray(ResultSet rs, String columnName) throws SQLException {
        return com.power.base.dao.clickhouse.util.ClickHouseArrayUtil.extractNestedArray(rs, columnName);
    }
}

