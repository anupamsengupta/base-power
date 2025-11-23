# ClickHouse Design for PhysicalPowerTradeDto

## Design Philosophy

**YES, store it as FLAT (denormalized)** - This is the recommended approach for ClickHouse OLAP databases.

## Multi-Tenant Partitioning: Why tenant_id in Partition Key?

**✅ YES, adding tenant_id to partition key is highly recommended for multi-tenant systems!**

### Benefits of `PARTITION BY (tenant_id, toYYYYMM(trade_date))`:

1. **Partition Pruning**: Queries with `tenant_id` filter only scan relevant partitions
   - Query: `WHERE tenant_id = 'TENANT_A' AND trade_date >= '2025-01-01'`
   - Only scans: `('TENANT_A', '202501')`, `('TENANT_A', '202502')`, etc.
   - Skips all other tenant partitions = **much faster queries**

2. **Tenant Data Isolation**: Each tenant's data is physically separated
   - Better for compliance/regulations
   - Easier to manage tenant-specific data retention policies

3. **Easy Tenant Data Management**:
   ```sql
   -- Delete all data for a tenant
   ALTER TABLE DROP PARTITION ('TENANT_A', '202501');
   
   -- Archive tenant data
   -- Move partitions to archive table
   ```

4. **Better Query Performance**: 
   - Tenant queries are faster (fewer partitions to scan)
   - Cross-tenant queries still work but are explicit about scanning all tenants

5. **Scalability**: 
   - Each tenant's partitions can be managed independently
   - Can move tenant data to different storage tiers

### When NOT to use tenant_id in partition key:

- **Very high tenant count (> 1000)**: Too many partitions can hurt performance
  - Solution: Use `PARTITION BY toYYYYMM(trade_date)` and rely on ORDER BY
  - Or use hash partitioning: `PARTITION BY (cityHash64(tenant_id) % 100, toYYYYMM(trade_date))`

### Why Denormalized/Flat Design?

1. **Columnar Storage Benefits**: ClickHouse stores data column-by-column. Wide, flat tables with many columns are more efficient than joins across multiple tables.

2. **Query Performance**: OLAP queries typically aggregate across many rows. Denormalized data means:
   - No expensive JOINs
   - Better compression (similar values in columns)
   - Faster aggregations

3. **Write-Optimized**: ClickHouse is optimized for bulk inserts. Flat structure simplifies ETL pipelines.

4. **Time-Series Optimization**: Partitioning by date and ordering by common query patterns makes time-range queries extremely fast.

## Recommended Design: Option 1 (Fully Flattened with Arrays)

### Structure Overview

```
physical_trades_olap
├── Trade Header Fields (flattened)
├── Settlement Info Fields (flattened)
├── Metadata Fields (flattened)
├── Line Items (as Arrays - one array per attribute)
└── Settlement Items (as Arrays - one array per attribute)
```

### Key Design Decisions

#### 1. **Arrays for Nested Collections**
- **Line Items**: Stored as parallel arrays (one array per attribute)
  - `line_item_quantities Array(Float64)`
  - `line_item_period_start_times Array(DateTime64(3))`
  - etc.
  
- **Settlement Items**: Same approach
  - `settlement_item_ids Array(String)`
  - `settlement_item_period_cashflows Array(Float64)`
  - etc.

**Why Arrays?**
- Maintains relationship between attributes (index-based)
- Efficient for aggregations: `arraySum(line_item_quantities)`
- No need for separate tables or complex joins
- ClickHouse handles arrays efficiently

#### 2. **Partitioning Strategy**
```sql
PARTITION BY (tenant_id, toYYYYMM(trade_date))
```
- **Multi-tenant + Monthly partitions** for optimal balance:
  - **Tenant isolation**: Each tenant's data is in separate partitions
  - **Efficient pruning**: Queries with tenant_id filter only scan relevant partitions
  - **Easy tenant data management**: Drop tenant data: `ALTER TABLE DROP PARTITION ('TENANT_A', '202501')`
  - **Better query performance**: Partition pruning happens at both tenant and date level
  - **Monthly granularity**: Not too small (avoids too many partitions), not too large (maintains performance)

**Partition Examples:**
- `('TENANT_A', '202501')` - Tenant A, January 2025
- `('TENANT_B', '202501')` - Tenant B, January 2025
- `('TENANT_A', '202502')` - Tenant A, February 2025

**When to use different strategies:**
- **< 1000 tenants**: Use `PARTITION BY (tenant_id, toYYYYMM(trade_date))` ✅ RECOMMENDED
- **> 1000 tenants**: Use `PARTITION BY toYYYYMM(trade_date)` and rely on ORDER BY for tenant filtering
- **Very high tenant count**: Use `PARTITION BY (cityHash64(tenant_id) % 100, toYYYYMM(trade_date))` to limit partitions

#### 3. **Ordering Key (Primary Key)**
```sql
ORDER BY (tenant_id, trade_date, market, business_unit, trade_id)
```
- Optimized for common query patterns:
  - Filter by tenant (multi-tenancy)
  - Filter by date range
  - Filter by market/business unit
  - Unique trade lookup

#### 4. **Computed Columns**
```sql
total_line_items UInt32,  -- Pre-computed for faster queries
total_quantity Float64,   -- Pre-aggregated sum
total_cashflow Float64    -- Pre-aggregated sum
```
- Reduces computation during queries
- Can be maintained via triggers or ETL

## Alternative Design: Option 2 (Separate Fact Tables)

Use this if:
- You frequently query line items independently
- You need to join line items across trades
- Line items have very high cardinality (thousands per trade)

### Trade-offs

**Option 1 (Arrays) - Recommended**
- ✅ Simpler queries (single table)
- ✅ Better for trade-level aggregations
- ✅ Faster inserts (single table)
- ❌ Less flexible for line-item-only queries
- ❌ Array size limits (though ClickHouse handles large arrays well)

**Option 2 (Separate Tables)**
- ✅ More flexible for line-item queries
- ✅ Better for cross-trade line item analysis
- ✅ No array size concerns
- ❌ Requires JOINs for complete trade view
- ❌ More complex ETL
- ❌ Slightly slower for trade-level queries

## Data Type Choices

### Strings vs LowCardinality
- Use `String` for high-cardinality fields (trade_id, party_ids)
- Consider `LowCardinality(String)` for low-cardinality fields (market, commodity, transaction_type)
  - Better compression
  - Faster queries
  - Example: `market LowCardinality(String)`

### Nullable Fields
- Use `Nullable()` only when truly optional
- ClickHouse handles NULLs efficiently in columnar storage
- Example: `governing_law Nullable(String)`

### Date/Time Types
- `Date` for date-only fields
- `DateTime64(3)` for precise timestamps (millisecond precision)
- Consider timezone handling if needed

## Materialized Views

Pre-aggregate common queries:

```sql
-- Daily summaries
mv_daily_trades_by_market

-- Hourly aggregations
mv_hourly_volume_by_delivery_point
```

**Benefits:**
- Query materialized views instead of base table
- Automatic updates on insert
- Much faster for dashboard/reporting queries

## ETL Considerations

### Insert Strategy

1. **Batch Inserts**: Insert in batches (1000-10000 rows)
   ```sql
   INSERT INTO physical_trades_olap VALUES (...), (...), ...
   ```

2. **Transform DTO to Flat Structure**:
   ```java
   // Flatten nested objects
   String buyerPartyId = dto.getTradeHeader().getBuyerParty().getId();
   
   // Convert collections to arrays
   Array<Float64> quantities = dto.getTradeDetails().getLineItems()
       .stream()
       .map(PhysicalLineItemDto::getQuantity)
       .collect(toArray());
   ```

3. **Handle Arrays**:
   - Ensure all arrays have same length (per trade)
   - Use empty arrays `[]` if no items
   - Consider max array size limits

### Update Strategy

ClickHouse is **append-only**. For updates:

1. **Versioning**: Use `version` column, insert new row with incremented version
2. **Deduplication**: Use `ReplacingMergeTree` engine
3. **Final Queries**: Use `FINAL` keyword or materialized views

## Query Patterns

### Pattern 1: Trade-Level Aggregations (Multi-Tenant)
```sql
-- Always include tenant_id in WHERE for partition pruning
SELECT 
    market,
    business_unit,
    sum(total_volume) as total_volume,
    count() as trade_count
FROM physical_trades_olap
WHERE tenant_id = 'TENANT_A'  -- Partition pruning
  AND trade_date >= today() - 30
GROUP BY market, business_unit
```

### Pattern 2: Line Item Analysis (Multi-Tenant)
```sql
-- Include tenant_id for partition pruning
SELECT 
    trade_id,
    arraySum(line_item_quantities) as total_quantity,
    length(line_item_quantities) as item_count
FROM physical_trades_olap
WHERE tenant_id = 'TENANT_A'  -- Partition pruning
  AND has(line_item_profiles, 'ONE_HOUR')
  AND trade_date >= today() - 7
```

### Pattern 3: Settlement Analysis (Multi-Tenant)
```sql
-- Include tenant_id for partition pruning
SELECT 
    business_unit,
    sum(arraySum(settlement_item_period_cashflows)) as total_cashflow
FROM physical_trades_olap
WHERE tenant_id = 'TENANT_A'  -- Partition pruning
  AND trade_date >= today() - 90
GROUP BY business_unit
```

### Pattern 4: Time-Series Analysis (Multi-Tenant)
```sql
-- Include tenant_id for partition pruning
SELECT 
    toStartOfHour(trade_time) as hour,
    sum(total_volume) as hourly_volume
FROM physical_trades_olap
WHERE tenant_id = 'TENANT_A'  -- Partition pruning
  AND trade_date = today()
GROUP BY hour
ORDER BY hour
```

### Pattern 5: Cross-Tenant Analysis (Admin Queries)
```sql
-- For admin queries across all tenants
-- Note: This scans all partitions, use sparingly
SELECT 
    tenant_id,
    market,
    sum(total_volume) as total_volume,
    count() as trade_count
FROM physical_trades_olap
WHERE trade_date >= today() - 30
GROUP BY tenant_id, market
ORDER BY tenant_id, total_volume DESC
```

## Performance Optimization Tips

1. **Use Materialized Views** for common aggregations
2. **Partition Pruning**: Always include BOTH partition keys in WHERE clause:
   ```sql
   WHERE tenant_id = 'TENANT_A' AND trade_date >= '2025-01-01'
   ```
   This ensures ClickHouse only scans relevant partitions.
3. **Ordering Key**: Filter by leftmost columns in ORDER BY clause
4. **Pre-aggregate**: Store computed values (total_quantity, total_cashflow)
5. **LowCardinality**: Use for enum-like fields
6. **Sparse Columns**: NULLs are cheap in columnar storage
7. **Tenant-Aware Queries**: Always filter by tenant_id first for best performance

## Migration Strategy

1. **Phase 1**: Create schema and start dual-write (OLTP + ClickHouse)
2. **Phase 2**: Backfill historical data in batches (per tenant for better performance)
3. **Phase 3**: Switch read queries to ClickHouse (ensure tenant_id is always in WHERE clause)
4. **Phase 4**: Optimize based on query patterns

## Tenant Data Management

### Delete Tenant Data
```sql
-- Drop all partitions for a specific tenant
ALTER TABLE physical_trades_olap DROP PARTITION ('TENANT_A', '202501');
ALTER TABLE physical_trades_olap DROP PARTITION ('TENANT_A', '202502');
-- Or drop all data for a tenant:
ALTER TABLE physical_trades_olap DELETE WHERE tenant_id = 'TENANT_A';
```

### Archive Tenant Data
```sql
-- Move old tenant data to archive table
INSERT INTO physical_trades_olap_archive 
SELECT * FROM physical_trades_olap 
WHERE tenant_id = 'TENANT_A' AND trade_date < '2024-01-01';
```

### Query Performance with Tenant Partitioning

**✅ Good Query (uses partition pruning):**
```sql
WHERE tenant_id = 'TENANT_A' AND trade_date >= '2025-01-01'
-- Only scans partitions: ('TENANT_A', '202501'), ('TENANT_A', '202502'), etc.
```

**❌ Less Efficient (scans all tenant partitions):**
```sql
WHERE trade_date >= '2025-01-01'  -- Missing tenant_id filter
-- Scans ALL tenant partitions for those months
```

**Best Practice**: Always include `tenant_id` in WHERE clause when querying.

## Monitoring

Track:
- Insert performance (rows/second)
- Query latency
- Partition sizes
- Disk usage
- Materialized view lag

## Conclusion

**Use Option 1 (Fully Flattened with Arrays)** unless you have specific requirements for independent line item queries. The flat, denormalized design aligns perfectly with ClickHouse's strengths and will provide excellent query performance for OLAP workloads.

