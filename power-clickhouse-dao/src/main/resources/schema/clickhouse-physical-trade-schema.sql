-- ClickHouse Schema Design for PhysicalPowerTradeDto
-- OLAP-optimized denormalized flat table design

-- ============================================
-- OPTION 1: Fully Flattened with Arrays (RECOMMENDED)
-- ============================================
-- Single table with arrays for nested collections
-- Best for: Aggregations, time-series analysis, reporting

CREATE TABLE IF NOT EXISTS physical_trades_olap
(
    -- Primary Key & Partitioning
    trade_id String,
    tenant_id String,
    trade_date Date,
    trade_time DateTime64(3),
    
    -- Header Information (flattened)
    document_type String,
    document_version String,
    buyer_party_id String,
    buyer_party_name String,
    buyer_party_role String,
    seller_party_id String,
    seller_party_name String,
    seller_party_role String,
    business_unit String,
    book_strategy String,
    trader_name String,
    agreement_id String,
    market String,
    commodity String,
    transaction_type String,
    delivery_point String,
    load_type String,
    buy_sell_indicator String,  -- 'BUY' or 'SELL'
    amendment_indicator UInt8,  -- 0 or 1
    
    -- Settlement Info (flattened from PhysicalSettlementInfoDto)
    total_volume Float64,
    total_volume_uom String,
    pricing_mechanism String,
    settlement_price Float64,
    trade_price Float64,
    settlement_currency String,
    trade_currency String,
    settlement_uom String,
    trade_uom String,
    start_applicability_date Nullable(Date),
    start_applicability_time Nullable(DateTime64(3)),
    end_applicability_date Nullable(Date),
    end_applicability_time Nullable(DateTime64(3)),
    payment_event Nullable(String),
    payment_offset Float64 DEFAULT 0,
    total_contract_value Float64 DEFAULT 0,
    rounding Float64 DEFAULT 0,
    
    -- Metadata (flattened)
    effective_date Nullable(Date),
    termination_date Nullable(Date),
    governing_law Nullable(String),
    
    -- Line Items as Arrays (nested structure)
    -- Each array index corresponds to one line item
    line_item_period_start_dates Array(Date),
    line_item_period_start_times Array(DateTime64(3)),
    line_item_period_end_dates Array(Date),
    line_item_period_end_times Array(DateTime64(3)),
    line_item_day_hours Array(String),
    line_item_quantities Array(Float64),
    line_item_uoms Array(String),
    line_item_capacities Array(Float64),
    line_item_profiles Array(String),
    
    -- Settlement Items as Arrays
    settlement_item_ids Array(String),
    settlement_item_delivery_dates Array(Date),
    settlement_item_actual_quantities Array(Float64),
    settlement_item_uoms Array(String),
    settlement_item_settlement_prices Array(Float64),
    settlement_item_trade_prices Array(Float64),
    settlement_item_settlement_uoms Array(String),
    settlement_item_trade_uoms Array(String),
    settlement_item_deviation_amounts Array(Float64),
    settlement_item_deviation_penalties Array(Float64),
    settlement_item_period_cashflows Array(Float64),
    settlement_item_settlement_currencies Array(String),
    settlement_item_trade_currencies Array(String),
    settlement_item_invoice_statuses Array(String),
    -- Referenced line items as nested array (Array of Arrays of Strings)
    settlement_item_referenced_line_items Array(Array(String)),
    
    -- Computed/Derived columns for analytics
    total_line_items UInt32,  -- length(line_item_quantities)
    total_settlement_items UInt32,  -- length(settlement_item_ids)
    total_quantity Float64,  -- sum(line_item_quantities)
    total_cashflow Float64,  -- sum(settlement_item_period_cashflows)
    
    -- Audit/ETL metadata
    inserted_at DateTime DEFAULT now(),
    updated_at DateTime DEFAULT now(),
    source_system String DEFAULT 'OLTP',
    version UInt32 DEFAULT 1
)
ENGINE = MergeTree()
-- RECOMMENDED: Partition by tenant_id + date for multi-tenant isolation and efficient pruning
-- Creates partitions like: (TENANT_A, 202501), (TENANT_B, 202501), etc.
PARTITION BY (tenant_id, toYYYYMM(trade_date))
ORDER BY (tenant_id, trade_date, market, business_unit, trade_id)  -- Optimized for common query patterns
SETTINGS index_granularity = 8192;

-- ============================================
-- ALTERNATIVE PARTITIONING STRATEGIES
-- ============================================
-- Choose based on your tenant count:

-- Option A: Tenant + Date (RECOMMENDED for < 1000 tenants)
-- PARTITION BY (tenant_id, toYYYYMM(trade_date))
-- Benefits: Tenant isolation, efficient pruning, easy tenant data deletion
-- Use when: You have reasonable number of tenants (< 1000)

-- Option B: Date only (for many tenants > 1000)
-- PARTITION BY toYYYYMM(trade_date)
-- Benefits: Fewer partitions, simpler management
-- Use when: You have many tenants and tenant_id in ORDER BY is sufficient

-- Option C: Tenant hash + Date (for very high tenant count)
-- PARTITION BY (cityHash64(tenant_id) % 100, toYYYYMM(trade_date))
-- Benefits: Limits partition count while maintaining some tenant separation
-- Use when: You have thousands of tenants and want to limit partition count

-- ============================================
-- OPTION 2: Separate Fact Tables (Alternative)
-- ============================================
-- If you need more flexibility for line items and settlement items
-- Use this if you frequently query line items independently

-- Main trade fact table
CREATE TABLE IF NOT EXISTS physical_trades_fact
(
    trade_id String,
    tenant_id String,
    trade_date Date,
    trade_time DateTime64(3),
    
    -- All header, settlement, and metadata fields (same as Option 1)
    document_type String,
    document_version String,
    buyer_party_id String,
    buyer_party_name String,
    buyer_party_role String,
    seller_party_id String,
    seller_party_name String,
    seller_party_role String,
    business_unit String,
    book_strategy String,
    trader_name String,
    agreement_id String,
    market String,
    commodity String,
    transaction_type String,
    delivery_point String,
    load_type String,
    buy_sell_indicator String,
    amendment_indicator UInt8,
    
    total_volume Float64,
    total_volume_uom String,
    pricing_mechanism String,
    settlement_price Float64,
    trade_price Float64,
    settlement_currency String,
    trade_currency String,
    settlement_uom String,
    trade_uom String,
    start_applicability_date Nullable(Date),
    start_applicability_time Nullable(DateTime64(3)),
    end_applicability_date Nullable(Date),
    end_applicability_time Nullable(DateTime64(3)),
    payment_event Nullable(String),
    payment_offset Float64 DEFAULT 0,
    total_contract_value Float64 DEFAULT 0,
    rounding Float64 DEFAULT 0,
    
    effective_date Nullable(Date),
    termination_date Nullable(Date),
    governing_law Nullable(String),
    
    inserted_at DateTime DEFAULT now(),
    updated_at DateTime DEFAULT now(),
    source_system String DEFAULT 'OLTP',
    version UInt32 DEFAULT 1
)
ENGINE = MergeTree()
PARTITION BY (tenant_id, toYYYYMM(trade_date))  -- Multi-tenant partitioning
ORDER BY (tenant_id, trade_date, market, business_unit, trade_id)
SETTINGS index_granularity = 8192;

-- Line items fact table (one row per line item)
CREATE TABLE IF NOT EXISTS physical_trade_line_items_fact
(
    trade_id String,
    line_item_index UInt32,  -- Index in original array
    period_start_date Date,
    period_start_time DateTime64(3),
    period_end_date Date,
    period_end_time DateTime64(3),
    day_hour String,
    quantity Float64,
    uom String,
    capacity Float64,
    profile String,
    
    -- Denormalized trade info for easier queries
    tenant_id String,
    trade_date Date,
    market String,
    business_unit String,
    
    inserted_at DateTime DEFAULT now()
)
ENGINE = MergeTree()
PARTITION BY (tenant_id, toYYYYMM(period_start_date))  -- Multi-tenant partitioning
ORDER BY (tenant_id, trade_id, period_start_date, line_item_index)
SETTINGS index_granularity = 8192;

-- Settlement items fact table (one row per settlement item)
CREATE TABLE IF NOT EXISTS physical_trade_settlement_items_fact
(
    trade_id String,
    settlement_item_index UInt32,  -- Index in original array
    settlement_id String,
    delivery_date Date,
    actual_quantity Float64,
    uom String,
    settlement_price Float64,
    trade_price Float64,
    settlement_uom String,
    trade_uom String,
    deviation_amount Float64,
    deviation_penalty Float64,
    period_cashflow Float64,
    settlement_currency String,
    trade_currency String,
    invoice_status String,
    referenced_line_items Array(String),  -- Array of line item references
    
    -- Denormalized trade info
    tenant_id String,
    trade_date Date,
    market String,
    business_unit String,
    
    inserted_at DateTime DEFAULT now()
)
ENGINE = MergeTree()
PARTITION BY (tenant_id, toYYYYMM(delivery_date))  -- Multi-tenant partitioning
ORDER BY (tenant_id, trade_id, delivery_date, settlement_item_index)
SETTINGS index_granularity = 8192;

-- ============================================
-- MATERIALIZED VIEWS for Common Aggregations
-- ============================================

-- Daily trade summary by market
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_daily_trades_by_market
ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(trade_date))  -- Multi-tenant partitioning
ORDER BY (trade_date, tenant_id, market, business_unit)
AS SELECT
    trade_date,
    tenant_id,
    market,
    business_unit,
    count() as trade_count,
    sum(total_volume) as total_volume_sum,
    sum(total_contract_value) as total_contract_value_sum,
    sum(total_cashflow) as total_cashflow_sum,
    avg(settlement_price) as avg_settlement_price,
    avg(trade_price) as avg_trade_price
FROM physical_trades_olap
GROUP BY trade_date, tenant_id, market, business_unit;

-- Hourly trade volume by delivery point
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_hourly_volume_by_delivery_point
ENGINE = SummingMergeTree()
PARTITION BY (tenant_id, toYYYYMM(trade_date))  -- Multi-tenant partitioning
ORDER BY (trade_date, hour(trade_time), tenant_id, delivery_point)
AS SELECT
    trade_date,
    toHour(trade_time) as trade_hour,
    tenant_id,
    delivery_point,
    count() as trade_count,
    sum(total_volume) as total_volume_sum,
    sum(total_contract_value) as total_contract_value_sum
FROM physical_trades_olap
GROUP BY trade_date, trade_hour, tenant_id, delivery_point;

-- ============================================
-- INDEXES for Better Query Performance
-- ============================================

-- Secondary indexes (if using ClickHouse 22.8+)
-- ALTER TABLE physical_trades_olap ADD INDEX idx_trader_name trader_name TYPE bloom_filter GRANULARITY 1;
-- ALTER TABLE physical_trades_olap ADD INDEX idx_agreement_id agreement_id TYPE bloom_filter GRANULARITY 1;

-- ============================================
-- SAMPLE QUERIES
-- ============================================

-- Query 1: Total volume by market for last 30 days
-- SELECT 
--     market,
--     sum(total_volume) as total_volume,
--     count() as trade_count
-- FROM physical_trades_olap
-- WHERE tenant_id = 'TENANT_A' AND trade_date >= today() - 30
-- GROUP BY market
-- ORDER BY total_volume DESC;

-- Query 2: Find trades with specific line item characteristics
-- SELECT 
--     trade_id,
--     tenant_id,
--     market,
--     arraySum(line_item_quantities) as total_quantity,
--     length(line_item_quantities) as line_item_count
-- FROM physical_trades_olap
-- WHERE tenant_id = 'TENANT_A' AND has(line_item_profiles, 'ONE_HOUR')
--   AND trade_date >= today() - 7;

-- Query 3: Settlement analysis by business unit
-- SELECT 
--     business_unit,
--     sum(arraySum(settlement_item_period_cashflows)) as total_cashflow,
--     avg(arrayAvg(settlement_item_settlement_prices)) as avg_settlement_price,
--     count() as trade_count
-- FROM physical_trades_olap
-- WHERE tenant_id = 'TENANT_A' AND trade_date >= today() - 90
-- GROUP BY business_unit
-- ORDER BY total_cashflow DESC;

