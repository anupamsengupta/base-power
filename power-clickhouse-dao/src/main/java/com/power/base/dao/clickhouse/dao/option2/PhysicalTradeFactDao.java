package com.power.base.dao.clickhouse.dao.option2;

import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeFact;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeLineItemFact;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeSettlementItemFact;

import java.util.List;

/**
 * DAO interface for Option 2: Separate fact tables.
 */
public interface PhysicalTradeFactDao {

    /**
     * Insert a single physical trade fact record.
     *
     * @param trade the trade fact to insert
     * @return the inserted trade fact
     */
    PhysicalTradeFact insertTradeFact(PhysicalTradeFact trade);

    /**
     * Batch insert multiple physical trade fact records.
     *
     * @param trades the trade facts to insert
     * @return number of inserted records
     */
    int batchInsertTradeFacts(List<PhysicalTradeFact> trades);

    /**
     * Insert line items for a trade.
     *
     * @param lineItems the line items to insert
     * @return number of inserted records
     */
    int insertLineItems(List<PhysicalTradeLineItemFact> lineItems);

    /**
     * Insert settlement items for a trade.
     *
     * @param settlementItems the settlement items to insert
     * @return number of inserted records
     */
    int insertSettlementItems(List<PhysicalTradeSettlementItemFact> settlementItems);

    /**
     * Find trade fact by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return the trade fact if found, null otherwise
     */
    PhysicalTradeFact findByTradeId(String tenantId, String tradeId);

    /**
     * Find line items for a trade.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return list of line items
     */
    List<PhysicalTradeLineItemFact> findLineItemsByTradeId(String tenantId, String tradeId);

    /**
     * Find settlement items for a trade.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return list of settlement items
     */
    List<PhysicalTradeSettlementItemFact> findSettlementItemsByTradeId(String tenantId, String tradeId);

    /**
     * Query trades by tenant and date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of trade facts
     */
    List<PhysicalTradeFact> findByDateRange(String tenantId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Delete trade and all related data by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return number of deleted records
     */
    int deleteByTradeId(String tenantId, String tradeId);
}

