package com.power.base.dao.clickhouse.service.option1;

import com.power.base.dao.clickhouse.config.ClickHouseDataSourceFactory;
import com.power.base.dao.clickhouse.config.ClickHouseProperties;
import com.power.base.dao.clickhouse.dao.option1.PhysicalTradeOlapDao;
import com.power.base.dao.clickhouse.dao.option1.PhysicalTradeOlapDaoImpl;
import com.power.base.dao.clickhouse.persistable.option1.PhysicalTradeOlap;
import com.power.base.dao.clickhouse.persistable.option1.PhysicalTradeOlapMapper;
import com.power.base.datamodel.dto.physicals.PhysicalPowerTradeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Option 1: Single flat table with arrays.
 * Handles conversion between DTOs and persistable entities.
 * Creates and manages its own DataSource singleton based on Spring Boot properties.
 */
@Service
public class PhysicalTradeOlapService {

    private final PhysicalTradeOlapDao dao;
    private final DataSource dataSource;

    /**
     * Constructor that creates DataSource from Spring Boot properties.
     *
     * @param properties ClickHouse configuration properties
     */
    @Autowired
    public PhysicalTradeOlapService(ClickHouseProperties properties) {
        this.dataSource = ClickHouseDataSourceFactory.getOrCreateDataSource(properties);
        this.dao = new PhysicalTradeOlapDaoImpl(this.dataSource);
    }

    /**
     * Constructor for testing or when DataSource is provided externally.
     *
     * @param dataSource the DataSource to use
     */
    public PhysicalTradeOlapService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.dao = new PhysicalTradeOlapDaoImpl(dataSource);
    }

    /**
     * Save a physical trade to ClickHouse.
     *
     * @param tradeDto the trade DTO to save
     * @return the saved trade DTO
     */
    public PhysicalPowerTradeDto save(PhysicalPowerTradeDto tradeDto) {
        if (tradeDto == null) {
            throw new IllegalArgumentException("PhysicalPowerTradeDto cannot be null");
        }

        PhysicalTradeOlap olap = PhysicalTradeOlapMapper.fromDto(tradeDto);
        PhysicalTradeOlap saved = dao.insert(olap);
        
        // Note: Converting back from OLAP to DTO would require a reverse mapper
        // For now, return the original DTO
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

        List<PhysicalTradeOlap> olapList = tradeDtos.stream()
                .map(PhysicalTradeOlapMapper::fromDto)
                .collect(Collectors.toList());

        return dao.batchInsert(olapList);
    }

    /**
     * Find trade by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return the trade DTO if found, null otherwise
     */
    public PhysicalPowerTradeDto findByTradeId(String tenantId, String tradeId) {
        PhysicalTradeOlap olap = dao.findByTradeId(tenantId, tradeId);
        if (olap == null) {
            return null;
        }
        // Note: Would need reverse mapper to convert OLAP back to DTO
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
        List<PhysicalTradeOlap> olapList = dao.findByDateRange(tenantId, startDate, endDate);
        // Note: Would need reverse mapper to convert OLAP back to DTO
        return List.of(); // TODO: Implement reverse mapping
    }

    /**
     * Delete trade by trade ID and tenant ID.
     *
     * @param tenantId the tenant ID
     * @param tradeId  the trade ID
     * @return number of deleted records
     */
    public int deleteByTradeId(String tenantId, String tradeId) {
        return dao.deleteByTradeId(tenantId, tradeId);
    }
}

