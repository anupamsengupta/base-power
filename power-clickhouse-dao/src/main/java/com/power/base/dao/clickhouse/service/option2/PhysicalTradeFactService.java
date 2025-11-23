package com.power.base.dao.clickhouse.service.option2;

import com.power.base.dao.clickhouse.config.ClickHouseDataSourceFactory;
import com.power.base.dao.clickhouse.config.ClickHouseProperties;
import com.power.base.dao.clickhouse.dao.option2.PhysicalTradeFactDao;
import com.power.base.dao.clickhouse.dao.option2.PhysicalTradeFactDaoImpl;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeFact;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeFactMapper;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeLineItemFact;
import com.power.base.dao.clickhouse.persistable.option2.PhysicalTradeSettlementItemFact;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for Option 2: Separate fact tables.
 * Handles conversion between DTOs and persistable entities.
 * Creates and manages its own DataSource singleton based on Spring Boot properties.
 */
@Service
public class PhysicalTradeFactService {

    private final PhysicalTradeFactDao dao;
    private final DataSource dataSource;

    /**
     * Constructor that creates DataSource from Spring Boot properties.
     *
     * @param properties ClickHouse configuration properties
     */
    @Autowired
    public PhysicalTradeFactService(ClickHouseProperties properties) {
        this.dataSource = ClickHouseDataSourceFactory.getOrCreateDataSource(properties);
        this.dao = new PhysicalTradeFactDaoImpl(this.dataSource);
    }

    /**
     * Constructor for testing or when DataSource is provided externally.
     *
     * @param dataSource the DataSource to use
     */
    public PhysicalTradeFactService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.dao = new PhysicalTradeFactDaoImpl(dataSource);
    }

    /**
     * Save a physical trade to ClickHouse (all related tables).
     *
     * @param tradeDto the trade DTO to save
     * @return the saved trade DTO
     */
    public PhysicalPowerTradeDto save(PhysicalPowerTradeDto tradeDto) {
        if (tradeDto == null) {
            throw new IllegalArgumentException("PhysicalPowerTradeDto cannot be null");
        }

        // Convert to persistable entities
        PhysicalTradeFact tradeFact = PhysicalTradeFactMapper.toTradeFact(tradeDto);
        List<PhysicalTradeLineItemFact> lineItems = PhysicalTradeFactMapper.toLineItemFacts(tradeDto);
        List<PhysicalTradeSettlementItemFact> settlementItems = PhysicalTradeFactMapper.toSettlementItemFacts(tradeDto);

        // Insert in order: trade fact, then line items, then settlement items
        dao.insertTradeFact(tradeFact);
        if (!lineItems.isEmpty()) {
            dao.insertLineItems(lineItems);
        }
        if (!settlementItems.isEmpty()) {
            dao.insertSettlementItems(settlementItems);
        }

        return tradeDto;
    }

    /**
     * Batch save multiple physical trades to ClickHouse.
     *
     * @param tradeDtos the trade DTOs to save
     * @return number of saved trades
     */
    public int batchSave(List<PhysicalPowerTradeDto> tradeDtos) {
        if (tradeDtos == null || tradeDtos.isEmpty()) {
            return 0;
        }

        int saved = 0;
        for (PhysicalPowerTradeDto dto : tradeDtos) {
            save(dto);
            saved++;
        }
        return saved;
    }

    /**
     * Find trade by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return the trade DTO if found, null otherwise
     */
    public PhysicalPowerTradeDto findByTradeId(String tenantId, String tradeId) {
        PhysicalTradeFact tradeFact = dao.findByTradeId(tenantId, tradeId);
        if (tradeFact == null) {
            return null;
        }

        List<PhysicalTradeLineItemFact> lineItems = dao.findLineItemsByTradeId(tenantId, tradeId);
        List<PhysicalTradeSettlementItemFact> settlementItems = dao.findSettlementItemsByTradeId(tenantId, tradeId);

        // Note: Would need reverse mapper to convert back to DTO
        // For now, return null or implement reverse mapping
        return null; // TODO: Implement reverse mapping
    }

    /**
     * Query trades by tenant and date range.
     *
     * @param tenantId  the tenant ID
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @return list of trade DTOs
     */
    public List<PhysicalPowerTradeDto> findByDateRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        List<PhysicalTradeFact> tradeFacts = dao.findByDateRange(tenantId, startDate, endDate);
        // Note: Would need reverse mapper to convert back to DTO
        // Would also need to fetch line items and settlement items for each trade
        return List.of(); // TODO: Implement reverse mapping
    }

    /**
     * Delete trade and all related data by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return number of deleted records
     */
    public int deleteByTradeId(String tenantId, String tradeId) {
        return dao.deleteByTradeId(tenantId, tradeId);
    }
}

