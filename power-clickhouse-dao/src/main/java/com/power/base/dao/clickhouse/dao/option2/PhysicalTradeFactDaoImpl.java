package com.power.base.dao.clickhouse.dao.option2;

import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeFact;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeLineItemFact;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeSettlementItemFact;

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
 * ClickHouse DAO implementation for Option 2: Separate fact tables.
 */
public class PhysicalTradeFactDaoImpl implements PhysicalTradeFactDao {

    private static final String TRADE_FACT_TABLE = "physical_trades_fact";
    private static final String LINE_ITEM_TABLE = "physical_trade_line_items_fact";
    private static final String SETTLEMENT_ITEM_TABLE = "physical_trade_settlement_items_fact";

    private static final String INSERT_TRADE_FACT_SQL = buildInsertTradeFactSql();
    private static final String INSERT_LINE_ITEM_SQL = buildInsertLineItemSql();
    private static final String INSERT_SETTLEMENT_ITEM_SQL = buildInsertSettlementItemSql();
    private static final String SELECT_TRADE_FACT_SQL = 
        "SELECT * FROM " + TRADE_FACT_TABLE + " WHERE tenant_id = ? AND trade_id = ?";
    private static final String SELECT_LINE_ITEMS_SQL = 
        "SELECT * FROM " + LINE_ITEM_TABLE + " WHERE tenant_id = ? AND trade_id = ? ORDER BY line_item_index";
    private static final String SELECT_SETTLEMENT_ITEMS_SQL = 
        "SELECT * FROM " + SETTLEMENT_ITEM_TABLE + " WHERE tenant_id = ? AND trade_id = ? ORDER BY settlement_item_index";
    private static final String SELECT_TRADE_FACTS_BY_DATE_SQL = 
        "SELECT * FROM " + TRADE_FACT_TABLE + " WHERE tenant_id = ? AND trade_date >= ? AND trade_date <= ? ORDER BY trade_date, trade_id";
    private static final String DELETE_TRADE_FACT_SQL = 
        "ALTER TABLE " + TRADE_FACT_TABLE + " DELETE WHERE tenant_id = ? AND trade_id = ?";
    private static final String DELETE_LINE_ITEMS_SQL = 
        "ALTER TABLE " + LINE_ITEM_TABLE + " DELETE WHERE tenant_id = ? AND trade_id = ?";
    private static final String DELETE_SETTLEMENT_ITEMS_SQL = 
        "ALTER TABLE " + SETTLEMENT_ITEM_TABLE + " DELETE WHERE tenant_id = ? AND trade_id = ?";

    private final DataSource dataSource;

    public PhysicalTradeFactDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public PhysicalTradeFact insertTradeFact(PhysicalTradeFact trade) {
        return batchInsertTradeFacts(Collections.singletonList(trade)) > 0 ? trade : null;
    }

    @Override
    public int batchInsertTradeFacts(List<PhysicalTradeFact> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0;
        }

        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_TRADE_FACT_SQL)) {

            for (PhysicalTradeFact trade : trades) {
                setTradeFactParameters(stmt, trade);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert trade facts into ClickHouse", e);
        }
    }

    @Override
    public int insertLineItems(List<PhysicalTradeLineItemFact> lineItems) {
        if (lineItems == null || lineItems.isEmpty()) {
            return 0;
        }

        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_LINE_ITEM_SQL)) {

            for (PhysicalTradeLineItemFact item : lineItems) {
                setLineItemParameters(stmt, item);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert line items into ClickHouse", e);
        }
    }

    @Override
    public int insertSettlementItems(List<PhysicalTradeSettlementItemFact> settlementItems) {
        if (settlementItems == null || settlementItems.isEmpty()) {
            return 0;
        }

        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_SETTLEMENT_ITEM_SQL)) {

            for (PhysicalTradeSettlementItemFact item : settlementItems) {
                setSettlementItemParameters(stmt, item);
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert settlement items into ClickHouse", e);
        }
    }

    @Override
    public PhysicalTradeFact findByTradeId(String tenantId, String tradeId) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_TRADE_FACT_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setString(2, tradeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapTradeFactFromResultSet(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find trade fact by ID", e);
        }
    }

    @Override
    public List<PhysicalTradeLineItemFact> findLineItemsByTradeId(String tenantId, String tradeId) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_LINE_ITEMS_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setString(2, tradeId);

            List<PhysicalTradeLineItemFact> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapLineItemFromResultSet(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find line items by trade ID", e);
        }
    }

    @Override
    public List<PhysicalTradeSettlementItemFact> findSettlementItemsByTradeId(String tenantId, String tradeId) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_SETTLEMENT_ITEMS_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setString(2, tradeId);

            List<PhysicalTradeSettlementItemFact> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapSettlementItemFromResultSet(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find settlement items by trade ID", e);
        }
    }

    @Override
    public List<PhysicalTradeFact> findByDateRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        try (var connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SELECT_TRADE_FACTS_BY_DATE_SQL)) {

            stmt.setString(1, tenantId);
            stmt.setObject(2, startDate);
            stmt.setObject(3, endDate);

            List<PhysicalTradeFact> results = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapTradeFactFromResultSet(rs));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find trade facts by date range", e);
        }
    }

    @Override
    public int deleteByTradeId(String tenantId, String tradeId) {
        try (var connection = dataSource.getConnection()) {
            int deleted = 0;

            // Delete settlement items first
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_SETTLEMENT_ITEMS_SQL)) {
                stmt.setString(1, tenantId);
                stmt.setString(2, tradeId);
                deleted += stmt.executeUpdate();
            }

            // Delete line items
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_LINE_ITEMS_SQL)) {
                stmt.setString(1, tenantId);
                stmt.setString(2, tradeId);
                deleted += stmt.executeUpdate();
            }

            // Delete trade fact
            try (PreparedStatement stmt = connection.prepareStatement(DELETE_TRADE_FACT_SQL)) {
                stmt.setString(1, tenantId);
                stmt.setString(2, tradeId);
                deleted += stmt.executeUpdate();
            }

            return deleted;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete trade by ID", e);
        }
    }

    private static String buildInsertTradeFactSql() {
        return "INSERT INTO " + TRADE_FACT_TABLE + " (" +
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
            "inserted_at, updated_at, source_system, version" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
            "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static String buildInsertLineItemSql() {
        return "INSERT INTO " + LINE_ITEM_TABLE + " (" +
            "trade_id, line_item_index, period_start_date, period_start_time, " +
            "period_end_date, period_end_time, day_hour, quantity, uom, capacity, profile, " +
            "tenant_id, trade_date, market, business_unit, inserted_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static String buildInsertSettlementItemSql() {
        return "INSERT INTO " + SETTLEMENT_ITEM_TABLE + " (" +
            "trade_id, settlement_item_index, settlement_id, delivery_date, " +
            "actual_quantity, uom, settlement_price, trade_price, settlement_uom, trade_uom, " +
            "deviation_amount, deviation_penalty, period_cashflow, settlement_currency, " +
            "trade_currency, invoice_status, referenced_line_items, " +
            "tenant_id, trade_date, market, business_unit, inserted_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private void setTradeFactParameters(PreparedStatement stmt, PhysicalTradeFact trade) throws SQLException {
        int paramIndex = 1;

        stmt.setString(paramIndex++, trade.getTradeId());
        stmt.setString(paramIndex++, trade.getTenantId());
        stmt.setObject(paramIndex++, trade.getTradeDate());
        stmt.setObject(paramIndex++, trade.getTradeTime());

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

        stmt.setObject(paramIndex++, trade.getEffectiveDate());
        stmt.setObject(paramIndex++, trade.getTerminationDate());
        stmt.setString(paramIndex++, trade.getGoverningLaw());

        stmt.setObject(paramIndex++, trade.getInsertedAt());
        stmt.setObject(paramIndex++, trade.getUpdatedAt());
        stmt.setString(paramIndex++, trade.getSourceSystem());
        stmt.setInt(paramIndex++, trade.getVersion() != null ? trade.getVersion() : 1);
    }

    private void setLineItemParameters(PreparedStatement stmt, PhysicalTradeLineItemFact item) throws SQLException {
        int paramIndex = 1;

        stmt.setString(paramIndex++, item.getTradeId());
        stmt.setInt(paramIndex++, item.getLineItemIndex());
        stmt.setObject(paramIndex++, item.getPeriodStartDate());
        stmt.setObject(paramIndex++, item.getPeriodStartTime());
        stmt.setObject(paramIndex++, item.getPeriodEndDate());
        stmt.setObject(paramIndex++, item.getPeriodEndTime());
        stmt.setString(paramIndex++, item.getDayHour());
        stmt.setObject(paramIndex++, item.getQuantity());
        stmt.setString(paramIndex++, item.getUom());
        stmt.setObject(paramIndex++, item.getCapacity());
        stmt.setString(paramIndex++, item.getProfile());

        stmt.setString(paramIndex++, item.getTenantId());
        stmt.setObject(paramIndex++, item.getTradeDate());
        stmt.setString(paramIndex++, item.getMarket());
        stmt.setString(paramIndex++, item.getBusinessUnit());
        stmt.setObject(paramIndex++, item.getInsertedAt());
    }

    private void setSettlementItemParameters(PreparedStatement stmt, PhysicalTradeSettlementItemFact item) throws SQLException {
        int paramIndex = 1;

        stmt.setString(paramIndex++, item.getTradeId());
        stmt.setInt(paramIndex++, item.getSettlementItemIndex());
        stmt.setString(paramIndex++, item.getSettlementId());
        stmt.setObject(paramIndex++, item.getDeliveryDate());
        stmt.setObject(paramIndex++, item.getActualQuantity());
        stmt.setString(paramIndex++, item.getUom());
        stmt.setObject(paramIndex++, item.getSettlementPrice());
        stmt.setObject(paramIndex++, item.getTradePrice());
        stmt.setString(paramIndex++, item.getSettlementUom());
        stmt.setString(paramIndex++, item.getTradeUom());
        stmt.setObject(paramIndex++, item.getDeviationAmount());
        stmt.setObject(paramIndex++, item.getDeviationPenalty());
        stmt.setObject(paramIndex++, item.getPeriodCashflow());
        stmt.setString(paramIndex++, item.getSettlementCurrency());
        stmt.setString(paramIndex++, item.getTradeCurrency());
        stmt.setString(paramIndex++, item.getInvoiceStatus());
        stmt.setArray(paramIndex++, createArray(stmt.getConnection(), "String", item.getReferencedLineItems()));

        stmt.setString(paramIndex++, item.getTenantId());
        stmt.setObject(paramIndex++, item.getTradeDate());
        stmt.setString(paramIndex++, item.getMarket());
        stmt.setString(paramIndex++, item.getBusinessUnit());
        stmt.setObject(paramIndex++, item.getInsertedAt());
    }

    private java.sql.Array createArray(java.sql.Connection conn, String type, List<?> values) throws SQLException {
        return com.power.base.dao.clickhouse.util.ClickHouseArrayUtil.createArray(conn, type, values);
    }

    private PhysicalTradeFact mapTradeFactFromResultSet(ResultSet rs) throws SQLException {
        PhysicalTradeFact fact = new PhysicalTradeFact();

        fact.setTradeId(rs.getString("trade_id"));
        fact.setTenantId(rs.getString("tenant_id"));
        fact.setTradeDate(rs.getObject("trade_date", LocalDate.class));
        fact.setTradeTime(rs.getObject("trade_time", Instant.class));

        fact.setDocumentType(rs.getString("document_type"));
        fact.setDocumentVersion(rs.getString("document_version"));
        fact.setBuyerPartyId(rs.getString("buyer_party_id"));
        fact.setBuyerPartyName(rs.getString("buyer_party_name"));
        fact.setBuyerPartyRole(rs.getString("buyer_party_role"));
        fact.setSellerPartyId(rs.getString("seller_party_id"));
        fact.setSellerPartyName(rs.getString("seller_party_name"));
        fact.setSellerPartyRole(rs.getString("seller_party_role"));
        fact.setBusinessUnit(rs.getString("business_unit"));
        fact.setBookStrategy(rs.getString("book_strategy"));
        fact.setTraderName(rs.getString("trader_name"));
        fact.setAgreementId(rs.getString("agreement_id"));
        fact.setMarket(rs.getString("market"));
        fact.setCommodity(rs.getString("commodity"));
        fact.setTransactionType(rs.getString("transaction_type"));
        fact.setDeliveryPoint(rs.getString("delivery_point"));
        fact.setLoadType(rs.getString("load_type"));
        fact.setBuySellIndicator(rs.getString("buy_sell_indicator"));
        fact.setAmendmentIndicator(rs.getBoolean("amendment_indicator"));

        fact.setTotalVolume(rs.getObject("total_volume", Double.class));
        fact.setTotalVolumeUom(rs.getString("total_volume_uom"));
        fact.setPricingMechanism(rs.getString("pricing_mechanism"));
        fact.setSettlementPrice(rs.getObject("settlement_price", Double.class));
        fact.setTradePrice(rs.getObject("trade_price", Double.class));
        fact.setSettlementCurrency(rs.getString("settlement_currency"));
        fact.setTradeCurrency(rs.getString("trade_currency"));
        fact.setSettlementUom(rs.getString("settlement_uom"));
        fact.setTradeUom(rs.getString("trade_uom"));
        fact.setStartApplicabilityDate(rs.getObject("start_applicability_date", LocalDate.class));
        fact.setStartApplicabilityTime(rs.getObject("start_applicability_time", Instant.class));
        fact.setEndApplicabilityDate(rs.getObject("end_applicability_date", LocalDate.class));
        fact.setEndApplicabilityTime(rs.getObject("end_applicability_time", Instant.class));
        fact.setPaymentEvent(rs.getString("payment_event"));
        fact.setPaymentOffset(rs.getObject("payment_offset", Double.class));
        fact.setTotalContractValue(rs.getObject("total_contract_value", Double.class));
        fact.setRounding(rs.getObject("rounding", Double.class));

        fact.setEffectiveDate(rs.getObject("effective_date", LocalDate.class));
        fact.setTerminationDate(rs.getObject("termination_date", LocalDate.class));
        fact.setGoverningLaw(rs.getString("governing_law"));

        fact.setInsertedAt(rs.getObject("inserted_at", LocalDateTime.class));
        fact.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        fact.setSourceSystem(rs.getString("source_system"));
        fact.setVersion(rs.getObject("version", Integer.class));

        return fact;
    }

    private PhysicalTradeLineItemFact mapLineItemFromResultSet(ResultSet rs) throws SQLException {
        PhysicalTradeLineItemFact fact = new PhysicalTradeLineItemFact();

        fact.setTradeId(rs.getString("trade_id"));
        fact.setLineItemIndex(rs.getInt("line_item_index"));
        fact.setPeriodStartDate(rs.getObject("period_start_date", LocalDate.class));
        fact.setPeriodStartTime(rs.getObject("period_start_time", Instant.class));
        fact.setPeriodEndDate(rs.getObject("period_end_date", LocalDate.class));
        fact.setPeriodEndTime(rs.getObject("period_end_time", Instant.class));
        fact.setDayHour(rs.getString("day_hour"));
        fact.setQuantity(rs.getObject("quantity", Double.class));
        fact.setUom(rs.getString("uom"));
        fact.setCapacity(rs.getObject("capacity", Double.class));
        fact.setProfile(rs.getString("profile"));

        fact.setTenantId(rs.getString("tenant_id"));
        fact.setTradeDate(rs.getObject("trade_date", LocalDate.class));
        fact.setMarket(rs.getString("market"));
        fact.setBusinessUnit(rs.getString("business_unit"));
        fact.setInsertedAt(rs.getObject("inserted_at", LocalDateTime.class));

        return fact;
    }

    private PhysicalTradeSettlementItemFact mapSettlementItemFromResultSet(ResultSet rs) throws SQLException {
        PhysicalTradeSettlementItemFact fact = new PhysicalTradeSettlementItemFact();

        fact.setTradeId(rs.getString("trade_id"));
        fact.setSettlementItemIndex(rs.getInt("settlement_item_index"));
        fact.setSettlementId(rs.getString("settlement_id"));
        fact.setDeliveryDate(rs.getObject("delivery_date", LocalDate.class));
        fact.setActualQuantity(rs.getObject("actual_quantity", Double.class));
        fact.setUom(rs.getString("uom"));
        fact.setSettlementPrice(rs.getObject("settlement_price", Double.class));
        fact.setTradePrice(rs.getObject("trade_price", Double.class));
        fact.setSettlementUom(rs.getString("settlement_uom"));
        fact.setTradeUom(rs.getString("trade_uom"));
        fact.setDeviationAmount(rs.getObject("deviation_amount", Double.class));
        fact.setDeviationPenalty(rs.getObject("deviation_penalty", Double.class));
        fact.setPeriodCashflow(rs.getObject("period_cashflow", Double.class));
        fact.setSettlementCurrency(rs.getString("settlement_currency"));
        fact.setTradeCurrency(rs.getString("trade_currency"));
        fact.setInvoiceStatus(rs.getString("invoice_status"));

        java.sql.Array array = rs.getArray("referenced_line_items");
        if (array != null) {
            Object[] objects = (Object[]) array.getArray();
            List<String> referencedItems = new ArrayList<>();
            for (Object obj : objects) {
                if (obj != null) {
                    referencedItems.add(obj.toString());
                }
            }
            fact.setReferencedLineItems(referencedItems);
        }

        fact.setTenantId(rs.getString("tenant_id"));
        fact.setTradeDate(rs.getObject("trade_date", LocalDate.class));
        fact.setMarket(rs.getString("market"));
        fact.setBusinessUnit(rs.getString("business_unit"));
        fact.setInsertedAt(rs.getObject("inserted_at", LocalDateTime.class));

        return fact;
    }
}

