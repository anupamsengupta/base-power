package com.power.base.dao.clickhouse.benchmark;

import com.power.base.dao.clickhouse.config.ClickHouseDataSourceFactory;
import com.power.base.dao.clickhouse.config.ClickHouseProperties;
import com.power.base.dao.clickhouse.service.option1.PhysicalTradeOlapService;
import com.power.base.datamodel.dto.common.BuySellIndicator;
import com.power.base.datamodel.dto.common.DocumentType;
import com.power.base.datamodel.dto.common.PartyDto;
import com.power.base.datamodel.dto.common.Profile;
import com.power.base.datamodel.dto.physicals.*;
import org.openjdk.jmh.annotations.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH microbenchmarks for PhysicalTradeOlapService (ClickHouse Option 1).
 * 
 * Prerequisites:
 * - ClickHouse must be running at localhost:8123
 * - Database 'mydb' and table 'physical_trades_olap' must exist
 * 
 * To run:
 * 1. Ensure ClickHouse is running: docker run -d -p 8123:8123 -p 9000:9000 clickhouse/clickhouse-server
 * 2. Create database and table (see setupDatabase method)
 * 3. Build: mvn clean package
 * 4. Run: java -jar target/clickhouse-benchmarks.jar
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
public class PhysicalTradeOlapServiceBenchmark {

    private static final String TENANT_ID = "BENCH_TENANT";
    private PhysicalTradeOlapService service;
    private List<PhysicalPowerTradeDto> batchTrades;
    private int tradeCounter = 0;
    private LocalDate startDate;
    private LocalDate endDate;

    @Setup(Level.Trial)
    public void setupTrial() {
        // Configure ClickHouse connection
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUrl("jdbc:clickhouse://localhost:8123/mydb");
        properties.setDatabase("mydb");
        properties.setUsername("admin");
        properties.setPassword("supersecret");
        properties.setConnectionTimeout(30000);
        properties.setSocketTimeout(30000);
        properties.setCompress(false);

        // Reset singleton to ensure fresh connection
        ClickHouseDataSourceFactory.reset();

        // Setup database and table if needed
        setupDatabase(properties);

        // Create service
        service = new PhysicalTradeOlapService(properties);

        // Setup date range for queries
        startDate = LocalDate.of(2025, 1, 1);
        endDate = LocalDate.of(2025, 12, 31);
    }

    @Setup(Level.Iteration)
    public void setupIteration() {
        // Clean up test data from previous iteration
        try {
            // Delete test trades from previous iterations
            for (int i = 0; i < 20; i++) {
                service.deleteByTradeId(TENANT_ID, "BENCH-TRADE-" + i);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }

        // Create test data for benchmarks
        try {
            // Insert some test data for findByDateRange benchmarks
            for (int i = 0; i < 10; i++) {
                PhysicalPowerTradeDto trade = buildTrade("BENCH-TRADE-" + i, 3, 2);
                service.save(trade);
            }
        } catch (Exception e) {
            // Ignore setup errors
        }

        // Create batch trades
        batchTrades = new ArrayList<>();
        for (int i = 100; i < 110; i++) {
            batchTrades.add(buildTrade("BENCH-BATCH-" + i, 2, 1));
        }
    }

    @TearDown(Level.Iteration)
    public void tearDownIteration() {
        // Clean up test data
        try {
            service.deleteByTradeId(TENANT_ID, "BENCH-SAVE");
            for (int i = 100; i < 110; i++) {
                service.deleteByTradeId(TENANT_ID, "BENCH-BATCH-" + i);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @TearDown(Level.Trial)
    public void tearDownTrial() {
        // Clean up all test data
        try {
            for (int i = 0; i < 20; i++) {
                service.deleteByTradeId(TENANT_ID, "BENCH-TRADE-" + i);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
        ClickHouseDataSourceFactory.reset();
    }

    @Benchmark
    public void benchmarkSave() {
        PhysicalPowerTradeDto trade = buildTrade("BENCH-SAVE-" + (tradeCounter++), 3, 2);
        service.save(trade);
    }

    @Benchmark
    public void benchmarkBatchSave() {
        List<PhysicalPowerTradeDto> trades = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            trades.add(buildTrade("BENCH-BATCH-" + (tradeCounter++) + "-" + i, 2, 1));
        }
        service.batchSave(trades);
    }

    @Benchmark
    public void benchmarkFindByTradeId() {
        // Use a trade that should exist from setup
        service.findByTradeId(TENANT_ID, "BENCH-TRADE-0");
    }

    @Benchmark
    public void benchmarkFindByDateRange() {
        service.findByDateRange(TENANT_ID, startDate, endDate);
    }

    @Benchmark
    public void benchmarkDeleteByTradeId() {
        // Create and delete a trade
        PhysicalPowerTradeDto trade = buildTrade("BENCH-DELETE-" + (tradeCounter++), 2, 1);
        service.save(trade);
        service.deleteByTradeId(TENANT_ID, trade.getTradeHeader().getTradeId());
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

