# Power ClickHouse DAO

This module provides ClickHouse OLAP data access layer for Power Base project.

## Overview

The `power-clickhouse-dao` module contains:
- ClickHouse schema definitions for PhysicalPowerTradeDto
- Complete Java DAO layer with two design options
- Design documentation for OLAP data storage
- Service layer for easy integration

## Package Structure

```
com.power.base.dao.clickhouse
├── persistable/
│   ├── option1/          # Single flat table with arrays
│   │   ├── PhysicalTradeOlap
│   │   └── PhysicalTradeOlapMapper
│   └── option2/          # Separate fact tables
│       ├── PhysicalTradeFact
│       ├── PhysicalTradeLineItemFact
│       ├── PhysicalTradeSettlementItemFact
│       └── PhysicalTradeFactMapper
├── dao/
│   ├── option1/          # DAO for Option 1
│   │   ├── PhysicalTradeOlapDao
│   │   └── PhysicalTradeOlapDaoImpl
│   └── option2/          # DAO for Option 2
│       ├── PhysicalTradeFactDao
│       └── PhysicalTradeFactDaoImpl
├── service/
│   ├── option1/          # Service for Option 1
│   │   └── PhysicalTradeOlapService
│   └── option2/          # Service for Option 2
│       └── PhysicalTradeFactService
├── config/               # DataSource configuration
│   ├── ClickHouseProperties
│   ├── ClickHouseDataSourceFactory
│   └── ClickHouseAutoConfiguration
└── util/                # Utility classes
    └── ClickHouseArrayUtil
```

## Design Options

### Option 1: Single Flat Table with Arrays (Recommended)
- **Persistable**: `PhysicalTradeOlap`
- **DAO**: `PhysicalTradeOlapDao` / `PhysicalTradeOlapDaoImpl`
- **Service**: `PhysicalTradeOlapService`
- **Best for**: Trade-level aggregations, simpler queries, faster inserts

### Option 2: Separate Fact Tables
- **Persistable**: `PhysicalTradeFact`, `PhysicalTradeLineItemFact`, `PhysicalTradeSettlementItemFact`
- **DAO**: `PhysicalTradeFactDao` / `PhysicalTradeFactDaoImpl`
- **Service**: `PhysicalTradeFactService`
- **Best for**: Line item analysis, cross-trade queries, very high cardinality

## Quick Start

### 1. Configure ClickHouse Properties

Add ClickHouse configuration to your `application.yml` or `application.properties`:

**application.yml:**
```yaml
clickhouse:
  enabled: true
  url: jdbc:clickhouse://localhost:8123/default
  database: default
  username: default
  password: ""
  connection-timeout: 30000
  socket-timeout: 30000
  max-connections: 10
  min-idle-connections: 2
```

**application.properties:**
```properties
clickhouse.enabled=true
clickhouse.url=jdbc:clickhouse://localhost:8123/default
clickhouse.database=default
clickhouse.username=default
clickhouse.password=
clickhouse.connection-timeout=30000
clickhouse.socket-timeout=30000
clickhouse.max-connections=10
clickhouse.min-idle-connections=2
```

See `src/main/resources/application-clickhouse.yml.example` for a complete example.

### 2. Use Spring Boot Auto-Configuration (Recommended)

When using Spring Boot, services are automatically configured and can be injected:

**Option 1 (Recommended):**
```java
import com.power.base.dao.clickhouse.service.option1.PhysicalTradeOlapService;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private PhysicalTradeOlapService service;
```

**Option 2:**
```java
import com.power.base.dao.clickhouse.service.option2.PhysicalTradeFactService;
import org.springframework.beans.factory.annotation.Autowired;

@Autowired
private PhysicalTradeFactService service;
```

### 3. Manual Configuration (Non-Spring)

If not using Spring Boot, you can manually create services:

**Option 1:**
```java
import com.power.base.dao.clickhouse.config.ClickHouseProperties;
import com.power.base.dao.clickhouse.service.option1.PhysicalTradeOlapService;

ClickHouseProperties properties = new ClickHouseProperties();
properties.setUrl("jdbc:clickhouse://localhost:8123/default");
properties.setDatabase("default");
properties.setUsername("default");
properties.setPassword("");

PhysicalTradeOlapService service = new PhysicalTradeOlapService(properties);
```

**Option 2:**
```java
import com.power.base.dao.clickhouse.config.ClickHouseProperties;
import com.power.base.dao.clickhouse.service.option2.PhysicalTradeFactService;

ClickHouseProperties properties = new ClickHouseProperties();
// ... configure properties ...

PhysicalTradeFactService service = new PhysicalTradeFactService(properties);
```

### 3. Use the Service

```java
// Save a trade
PhysicalPowerTradeDto tradeDto = ...;
PhysicalPowerTradeDto saved = service.save(tradeDto);

// Batch save
List<PhysicalPowerTradeDto> trades = ...;
int savedCount = service.batchSave(trades);

// Find by ID
PhysicalPowerTradeDto found = service.findByTradeId("TENANT_A", "PWR-001");

// Query by date range
List<PhysicalPowerTradeDto> results = service.findByDateRange(
    "TENANT_A",
    LocalDate.of(2025, 1, 1),
    LocalDate.of(2025, 12, 31)
);

// Delete
service.deleteByTradeId("TENANT_A", "PWR-001");
```

## Schema Files

- **Schema**: `src/main/resources/schema/clickhouse-physical-trade-schema.sql`
- **Design Documentation**: `CLICKHOUSE_DESIGN.md`

## Key Features

- **Multi-Tenant Partitioning**: `PARTITION BY (tenant_id, toYYYYMM(trade_date))`
- **Array-Based Storage**: Nested collections stored as parallel arrays (Option 1)
- **Separate Fact Tables**: Flexible line item queries (Option 2)
- **Batch Operations**: Efficient bulk inserts
- **Tenant-Aware**: All operations require tenant_id for optimal performance

## Dependencies

- ClickHouse JDBC Driver (0.6.0)
- Power Datamodel (for DTOs)
- Spring Framework (for Spring Boot integration)
- Spring Boot Auto-Configure (for auto-configuration)
- Jackson (for JSON serialization)

## Notes

- **Spring Boot Integration**: Services automatically create and manage a singleton DataSource based on Spring Boot configuration properties. No manual DataSource creation required.
- **Configuration Properties**: All ClickHouse connection settings are externalized to Spring Boot properties with prefix `clickhouse.*`.
- **Reverse Mapping**: Currently, services convert DTOs to persistable entities but reverse mapping (persistable → DTO) is marked as TODO. This can be implemented when read operations are needed.
- **Array Handling**: ClickHouse array handling is abstracted in `ClickHouseArrayUtil` for easier maintenance.
- **Connection Management**: Services use a singleton DataSource created from configuration properties. The DataSource is thread-safe and shared across all service instances.

## Documentation

See `CLICKHOUSE_DESIGN.md` for detailed design documentation and query patterns.

