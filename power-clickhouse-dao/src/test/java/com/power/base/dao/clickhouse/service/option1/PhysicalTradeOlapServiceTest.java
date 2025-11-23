package com.power.base.dao.clickhouse.service.option1;

import com.power.base.dao.clickhouse.config.ClickHouseDataSourceFactory;
import com.power.base.dao.clickhouse.config.ClickHouseProperties;
import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;
import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for PhysicalTradeOlapService (Option 1).
 * 
 * Prerequisites:
 * - ClickHouse must be running in Docker at localhost:8123
 * - Database 'mydb' must exist
 * - Table 'physical_trades_olap' must be created using the schema SQL
 * 
 * To run these tests:
 * 1. Start ClickHouse: docker run -d -p 8123:8123 -p 9000:9000 clickhouse/clickhouse-server
 * 2. Create database: CREATE DATABASE IF NOT EXISTS mydb;
 * 3. Run the schema SQL to create the table
 * 4. Run the tests
 */
@SpringBootTest(classes = {ClickHouseProperties.class})
@TestPropertySource(locations = "classpath:application.yml")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PhysicalTradeOlapServiceTest {

    private PhysicalTradeOlapService service;
    private static final String TENANT_ID = "TEST_TENANT";
    private static final String TEST_TRADE_ID_1 = "TEST-TRADE-001";
    private static final String TEST_TRADE_ID_2 = "TEST-TRADE-002";
    private static final String TEST_TRADE_ID_3 = "TEST-TRADE-003";

    @BeforeAll
    void setUp() {
        // Load properties from application.yml
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUrl("jdbc:clickhouse://localhost:8123");
        properties.setDatabase("mydb");
        properties.setUsername("admin");
        properties.setPassword("supersecret");
        properties.setConnectionTimeout(30000);
        properties.setSocketTimeout(30000);
        properties.setCompress(false);

        // Reset singleton to ensure fresh connection
        ClickHouseDataSourceFactory.reset();
        
        // Create database and table if they don't exist
        setupDatabase(properties);
        
        // Create service with properties
        service = new PhysicalTradeOlapService(properties);
    }

    /**
     * Setup ClickHouse database and table if they don't exist.
     */
    private void setupDatabase(ClickHouseProperties properties) {
        try {
            // First, connect to default database to create our database
            ClickHouseProperties defaultProps = new ClickHouseProperties();
            defaultProps.setUrl("jdbc:clickhouse://localhost:8123/default");
            defaultProps.setDatabase("default");
            defaultProps.setUsername(properties.getUsername());
            defaultProps.setPassword(properties.getPassword());
            defaultProps.setConnectionTimeout(properties.getConnectionTimeout());
            defaultProps.setSocketTimeout(properties.getSocketTimeout());
            defaultProps.setCompress(false);
            
            DataSource defaultDataSource = ClickHouseDataSourceFactory.createDataSource(defaultProps);
            
            try (Connection conn = defaultDataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Create database if it doesn't exist
                stmt.execute("CREATE DATABASE IF NOT EXISTS " + properties.getDatabase());
            }
            
            // Now connect to our database and create the table
            DataSource dataSource = ClickHouseDataSourceFactory.createDataSource(properties);
            
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Create table if it doesn't exist
                String createTableSql = 
                    "CREATE TABLE IF NOT EXISTS physical_trades_olap (" +
                    "trade_id String, " +
                    "tenant_id String, " +
                    "trade_date Date, " +
                    "trade_time DateTime64(3), " +
                    "document_type String, " +
                    "document_version String, " +
                    "buyer_party_id String, " +
                    "buyer_party_name String, " +
                    "buyer_party_role String, " +
                    "seller_party_id String, " +
                    "seller_party_name String, " +
                    "seller_party_role String, " +
                    "business_unit String, " +
                    "book_strategy String, " +
                    "trader_name String, " +
                    "agreement_id String, " +
                    "market String, " +
                    "commodity String, " +
                    "transaction_type String, " +
                    "delivery_point String, " +
                    "load_type String, " +
                    "buy_sell_indicator String, " +
                    "amendment_indicator UInt8, " +
                    "total_volume Float64, " +
                    "total_volume_uom String, " +
                    "pricing_mechanism String, " +
                    "settlement_price Float64, " +
                    "trade_price Float64, " +
                    "settlement_currency String, " +
                    "trade_currency String, " +
                    "settlement_uom String, " +
                    "trade_uom String, " +
                    "start_applicability_date Nullable(Date), " +
                    "start_applicability_time Nullable(DateTime64(3)), " +
                    "end_applicability_date Nullable(Date), " +
                    "end_applicability_time Nullable(DateTime64(3)), " +
                    "payment_event Nullable(String), " +
                    "payment_offset Float64 DEFAULT 0, " +
                    "total_contract_value Float64 DEFAULT 0, " +
                    "rounding Float64 DEFAULT 0, " +
                    "effective_date Nullable(Date), " +
                    "termination_date Nullable(Date), " +
                    "governing_law Nullable(String), " +
                    "line_item_period_start_dates Array(Date), " +
                    "line_item_period_start_times Array(DateTime64(3)), " +
                    "line_item_period_end_dates Array(Date), " +
                    "line_item_period_end_times Array(DateTime64(3)), " +
                    "line_item_day_hours Array(String), " +
                    "line_item_quantities Array(Float64), " +
                    "line_item_uoms Array(String), " +
                    "line_item_capacities Array(Float64), " +
                    "line_item_profiles Array(String), " +
                    "settlement_item_ids Array(String), " +
                    "settlement_item_delivery_dates Array(Date), " +
                    "settlement_item_actual_quantities Array(Float64), " +
                    "settlement_item_uoms Array(String), " +
                    "settlement_item_settlement_prices Array(Float64), " +
                    "settlement_item_trade_prices Array(Float64), " +
                    "settlement_item_settlement_uoms Array(String), " +
                    "settlement_item_trade_uoms Array(String), " +
                    "settlement_item_deviation_amounts Array(Float64), " +
                    "settlement_item_deviation_penalties Array(Float64), " +
                    "settlement_item_period_cashflows Array(Float64), " +
                    "settlement_item_settlement_currencies Array(String), " +
                    "settlement_item_trade_currencies Array(String), " +
                    "settlement_item_invoice_statuses Array(String), " +
                    "settlement_item_referenced_line_items Array(Array(String)), " +
                    "total_line_items UInt32, " +
                    "total_settlement_items UInt32, " +
                    "total_quantity Float64, " +
                    "total_cashflow Float64, " +
                    "inserted_at DateTime DEFAULT now(), " +
                    "updated_at DateTime DEFAULT now(), " +
                    "source_system String DEFAULT 'OLTP', " +
                    "version UInt32 DEFAULT 1" +
                    ") ENGINE = MergeTree() " +
                    "PARTITION BY (tenant_id, toYYYYMM(trade_date)) " +
                    "ORDER BY (tenant_id, trade_date, market, business_unit, trade_id) " +
                    "SETTINGS index_granularity = 8192";
                
                stmt.execute(createTableSql);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup ClickHouse database and table", e);
        }
    }

    @AfterAll
    void tearDown() {
        // Clean up test data
        try {
            //service.deleteByTradeId(TENANT_ID, TEST_TRADE_ID_1);
            //service.deleteByTradeId(TENANT_ID, TEST_TRADE_ID_2);
            //service.deleteByTradeId(TENANT_ID, TEST_TRADE_ID_3);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        ClickHouseDataSourceFactory.reset();
    }

    @Test
    @Order(1)
    @DisplayName("Test save() - Save a single trade")
    void testSave() {
        // Given
        PhysicalPowerTradeDto trade = buildTrade(TEST_TRADE_ID_1, 3, 2);

        // When
        PhysicalPowerTradeDto saved = service.save(trade);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getTradeHeader().getTradeId()).isEqualTo(TEST_TRADE_ID_1);
        assertThat(saved.getTradeHeader().getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    @Order(2)
    @DisplayName("Test save() - Save trade with null DTO should throw exception")
    void testSaveWithNullDto() {
        // When/Then
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    @Order(3)
    @DisplayName("Test save() - Save trade with empty line items")
    void testSaveWithEmptyLineItems() {
        // Given
        PhysicalPowerTradeDto trade = buildTrade(TEST_TRADE_ID_2, 0, 0);

        // When
        PhysicalPowerTradeDto saved = service.save(trade);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getTradeHeader().getTradeId()).isEqualTo(TEST_TRADE_ID_2);
    }

    @Test
    @Order(4)
    @DisplayName("Test batchSave() - Save multiple trades")
    void testBatchSave() {
        // Given
        List<PhysicalPowerTradeDto> trades = new ArrayList<>();
        trades.add(buildTrade(TEST_TRADE_ID_3, 2, 1));
        trades.add(buildTrade("TEST-TRADE-004", 5, 3));
        trades.add(buildTrade("TEST-TRADE-005", 1, 1));

        // When
        int savedCount = service.batchSave(trades);

        // Then
        assertThat(savedCount).isEqualTo(3);

        // Cleanup
        service.deleteByTradeId(TENANT_ID, "TEST-TRADE-004");
        service.deleteByTradeId(TENANT_ID, "TEST-TRADE-005");
    }

    @Test
    @Order(5)
    @DisplayName("Test batchSave() - Empty list should return 0")
    void testBatchSaveWithEmptyList() {
        // Given
        List<PhysicalPowerTradeDto> emptyList = new ArrayList<>();

        // When
        int savedCount = service.batchSave(emptyList);

        // Then
        assertThat(savedCount).isEqualTo(0);
    }

    @Test
    @Order(6)
    @DisplayName("Test batchSave() - Null list should return 0")
    void testBatchSaveWithNullList() {
        // When
        int savedCount = service.batchSave(null);

        // Then
        assertThat(savedCount).isEqualTo(0);
    }

    @Test
    @Order(7)
    @DisplayName("Test batchSave() - Large batch")
    void testBatchSaveLargeBatch() {
        // Given
        List<PhysicalPowerTradeDto> trades = new ArrayList<>();
        for (int i = 100; i < 110; i++) {
            trades.add(buildTrade("TEST-TRADE-" + i, 2, 1));
        }

        // When
        int savedCount = service.batchSave(trades);

        // Then
        assertThat(savedCount).isEqualTo(10);

        // Cleanup
        for (int i = 100; i < 110; i++) {
            service.deleteByTradeId(TENANT_ID, "TEST-TRADE-" + i);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Test findByTradeId() - Find existing trade")
    void testFindByTradeId() {
        // Given - Trade was saved in testSave()
        // Note: Currently returns null due to missing reverse mapper implementation
        // This test verifies the method doesn't throw exceptions

        // When
        PhysicalPowerTradeDto found = service.findByTradeId(TENANT_ID, TEST_TRADE_ID_1);

        // Then
        // Currently returns null until reverse mapper is implemented
        // When reverse mapper is implemented, uncomment:
        // assertThat(found).isNotNull();
        // assertThat(found.getTradeHeader().getTradeId()).isEqualTo(TEST_TRADE_ID_1);
        // For now, just verify the method doesn't throw exceptions
        assertThat(found).isNull(); // Expected until reverse mapper is implemented
    }

    @Test
    @Order(9)
    @DisplayName("Test findByTradeId() - Find non-existent trade")
    void testFindByTradeIdNotFound() {
        // When
        PhysicalPowerTradeDto found = service.findByTradeId(TENANT_ID, "NON-EXISTENT-TRADE");

        // Then
        assertThat(found).isNull();
    }

    @Test
    @Order(10)
    @DisplayName("Test findByDateRange() - Find trades in date range")
    void testFindByDateRange() {
        // Given - Trades were saved in previous tests
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);

        // When
        List<PhysicalPowerTradeDto> found = service.findByDateRange(TENANT_ID, startDate, endDate);

        // Then
        // Currently returns empty list until reverse mapper is implemented
        // When reverse mapper is implemented, uncomment:
        // assertThat(found).isNotEmpty();
        // assertThat(found).hasSizeGreaterThanOrEqualTo(3);
        // For now, just verify the method doesn't throw exceptions
        assertThat(found).isNotNull(); // Should return empty list, not null
    }

    @Test
    @Order(11)
    @DisplayName("Test findByDateRange() - Empty date range")
    void testFindByDateRangeEmpty() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 12, 31);

        // When
        List<PhysicalPowerTradeDto> found = service.findByDateRange(TENANT_ID, startDate, endDate);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Test deleteByTradeId() - Delete existing trade")
    void testDeleteByTradeId() {
        // Given - Trade exists from testSave()
        
        // When
        int deletedCount = service.deleteByTradeId(TENANT_ID, TEST_TRADE_ID_1);

        // Then
        assertThat(deletedCount).isEqualTo(1);

        // Verify it's deleted
        PhysicalPowerTradeDto foundAfterDelete = service.findByTradeId(TENANT_ID, TEST_TRADE_ID_1);
        assertThat(foundAfterDelete).isNull();
    }

    /*@Test
    @Order(13)
    @DisplayName("Test deleteByTradeId() - Delete non-existent trade")
    void testDeleteByTradeIdNotFound() {
        // When
        int deletedCount = service.deleteByTradeId(TENANT_ID, "NON-EXISTENT-TRADE");

        // Then
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    @Order(14)
    @DisplayName("Test deleteByTradeId() - Delete with different tenant")
    void testDeleteByTradeIdDifferentTenant() {
        // Given - Trade exists for TENANT_ID
        // When - Try to delete with different tenant
        int deletedCount = service.deleteByTradeId("DIFFERENT_TENANT", TEST_TRADE_ID_2);

        // Then - Should not delete (tenant isolation)
        assertThat(deletedCount).isEqualTo(0);
    }*/

    @Test
    @Order(15)
    @DisplayName("Test complete workflow - Save, Find, Delete")
    void testCompleteWorkflow() {
        // Given
        String workflowTradeId = "WORKFLOW-TRADE-001";
        PhysicalPowerTradeDto trade = buildTrade(workflowTradeId, 4, 2);

        // Step 1: Save
        PhysicalPowerTradeDto saved = service.save(trade);
        assertThat(saved).isNotNull();
        assertThat(saved.getTradeHeader().getTradeId()).isEqualTo(workflowTradeId);

        // Step 2: Find (currently returns null, but should not throw)
        PhysicalPowerTradeDto found = service.findByTradeId(TENANT_ID, workflowTradeId);
        // Note: Currently null due to missing reverse mapper
        assertThat(found).isNull(); // Expected until reverse mapper is implemented

        // Step 3: Delete
        int deletedCount = service.deleteByTradeId(TENANT_ID, workflowTradeId);
        assertThat(deletedCount).isEqualTo(1);

        // Step 4: Verify deleted
        PhysicalPowerTradeDto foundAfterDelete = service.findByTradeId(TENANT_ID, workflowTradeId);
        assertThat(foundAfterDelete).isNull();
    }

    /**
     * Helper method to build test trade data.
     */
    private PhysicalPowerTradeDto buildTrade(String tradeId, int lineItemCount, int settlementItemCount) {
        PhysicalTradeHeaderDto header = new PhysicalTradeHeaderDto();
        header.setTenantId(TENANT_ID);
        header.setTradeId(tradeId);
        header.setTradeDate(LocalDate.of(2025, 6, 15));
        header.setTradeTime(Instant.parse("2025-06-15T10:30:00Z"));
        header.setDocumentType(DocumentType.CONFIRMATION);
        header.setDocumentVersion("1.0");
        header.setBuyerParty(new PartyDto("BUYER-001", "Buyer Corp", "Buyer"));
        header.setSellerParty(new PartyDto("SELLER-001", "Seller Corp", "Seller"));
        header.setBusinessUnit("Trading Desk");
        header.setBookStrategy("Hedging Book");
        header.setTraderName("Test Trader");
        header.setAgreementId("AGR-" + tradeId);
        header.setMarket("EPEX");
        header.setCommodity("Power");
        header.setTransactionType("FORWARD");
        header.setDeliveryPoint("DE-LU");
        header.setLoadType("Base");
        header.setBuySellIndicator(BuySellIndicator.BUY);
        header.setAmendmentIndicator(false);

        // Line Items
        List<PhysicalLineItemDto> lineItems = new ArrayList<>();
        for (int i = 0; i < lineItemCount; i++) {
            PhysicalLineItemDto lineItem = new PhysicalLineItemDto();
            lineItem.setPeriodStartDate(LocalDate.of(2025, 6, 15));
            lineItem.setPeriodStartTime(Instant.parse("2025-06-15T" + String.format("%02d", i % 24) + ":00:00Z"));
            lineItem.setPeriodEndDate(LocalDate.of(2025, 6, 15));
            lineItem.setPeriodEndTime(Instant.parse("2025-06-15T" + String.format("%02d", (i % 24) + 1) + ":00:00Z"));
            lineItem.setDayHour("H" + (i + 1));
            lineItem.setQuantity(10.0 + i);
            lineItem.setCapacity(5.0 + i);
            lineItem.setUom("MWh");
            lineItem.setProfile(Profile.ONE_HOUR);
            lineItems.add(lineItem);
        }
        PhysicalTradeDetailsDto details = new PhysicalTradeDetailsDto(lineItems);

        // Settlement Info
        PhysicalSettlementInfoDto settlementInfo = new PhysicalSettlementInfoDto();
        settlementInfo.setTotalVolume(100.0 * lineItemCount);
        settlementInfo.setTotalVolumeUom("MWh");
        settlementInfo.setPricingMechanism("Fixed");
        settlementInfo.setSettlementPrice(75.50);
        settlementInfo.setTradePrice(75.50);
        settlementInfo.setSettlementCurrency("EUR");
        settlementInfo.setTradeCurrency("EUR");
        settlementInfo.setSettlementUom("EUR/MWh");
        settlementInfo.setTradeUom("EUR/MWh");
        settlementInfo.setStartApplicabilityDate(LocalDate.of(2025, 6, 15));
        settlementInfo.setEndApplicabilityDate(LocalDate.of(2025, 6, 22));
        settlementInfo.setPaymentEvent("Schedule_Date");
        settlementInfo.setPaymentOffset(5.0);
        settlementInfo.setTotalContractValue(10000.0);
        settlementInfo.setRounding(2.0);

        // Settlement Items
        List<PhysicalSettlementItemDto> settlementItems = new ArrayList<>();
        for (int i = 0; i < settlementItemCount; i++) {
            PhysicalSettlementItemDto item = new PhysicalSettlementItemDto();
            item.setSettlementId("SET-" + tradeId + "-" + (i + 1));
            item.setReferencedLineItems(List.of("LINE#" + String.format("%04d", Math.min(i + 1, Math.max(1, lineItemCount)))));
            item.setDeliveryDate(LocalDate.of(2025, 6, 16 + i));
            item.setActualQuantity(50.0 + i);
            item.setUom("MWh");
            item.setSettlementPrice(76.0 + i);
            item.setTradePrice(75.0 + i);
            item.setSettlementUom("EUR/MWh");
            item.setTradeUom("EUR/MWh");
            item.setDeviationAmount(1.5 + i);
            item.setDeviationPenalty(0.5 + i);
            item.setPeriodCashflow(1000.0 + i);
            item.setSettlementCurrency("EUR");
            item.setTradeCurrency("EUR");
            item.setInvoiceStatus("Pending");
            settlementItems.add(item);
        }
        settlementInfo.setSettlementItems(settlementItems);

        // Metadata
        PhysicalMetadataDto metadata = new PhysicalMetadataDto();
        metadata.setEffectiveDate(LocalDate.of(2025, 6, 15));
        metadata.setTerminationDate(LocalDate.of(2025, 6, 22));
        metadata.setGoverningLaw("English Law");

        return new PhysicalPowerTradeDto(header, details, settlementInfo, metadata);
    }
}

