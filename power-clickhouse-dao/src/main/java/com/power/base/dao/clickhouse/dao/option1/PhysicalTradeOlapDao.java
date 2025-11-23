package com.power.base.dao.clickhouse.dao.option1;

import com.power.base.dao.clickhouse.persistable.option1.PhysicalTradeOlap;

import java.util.List;

/**
 * DAO interface for Option 1: Single flat table with arrays.
 */
public interface PhysicalTradeOlapDao {

    /**
     * Insert a single physical trade record.
     *
     * @param trade the trade to insert
     * @return the inserted trade
     */
    PhysicalTradeOlap insert(PhysicalTradeOlap trade);

    /**
     * Batch insert multiple physical trade records.
     *
     * @param trades the trades to insert
     * @return number of inserted records
     */
    int batchInsert(List<PhysicalTradeOlap> trades);

    /**
     * Find trade by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return the trade if found, null otherwise
     */
    PhysicalTradeOlap findByTradeId(String tenantId, String tradeId);

    /**
     * Query trades by tenant and date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of trades
     */
    List<PhysicalTradeOlap> findByDateRange(String tenantId, java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * Delete trade by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return number of deleted records
     */
    int deleteByTradeId(String tenantId, String tradeId);
}

