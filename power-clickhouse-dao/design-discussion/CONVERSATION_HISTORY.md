# ClickHouse DAO Module - Conversation History

This document preserves the conversation history and implementation decisions for the `power-clickhouse-dao` module.

## Overview

The `power-clickhouse-dao` module was created to provide an OLAP (Online Analytical Processing) data access layer for storing `PhysicalPowerTradeDto` in ClickHouse. This module supports two design options for data storage and provides a complete Java DAO layer with Spring Boot integration.

## Initial Request

The user requested:
> "if I need to store the PhysicalPowerTradeDTO in OLAP DB clickhouse what should be the data store design, should it be stored as flat? Please help me with the design."

## Design Considerations

### OLAP Database Characteristics
- **Columnar Storage**: ClickHouse is optimized for columnar storage, making it ideal for analytical queries
- **Denormalization**: OLAP databases favor denormalized structures over normalized ones
- **Partitioning**: Critical for performance, especially in multi-tenant environments
- **Ordering Keys**: Important for query performance and data locality

### Multi-Tenant Requirements
The user requested:
> "would it be better to add tenant_id to the partition key? If yes, please redo the solution"

This led to the partition key design: `PARTITION BY (tenant_id, toYYYYMM(trade_date))`

## Module Creation

The user requested:
> "please add a separate maven sub-module power-clickhouse-dao and add these changes there"

This resulted in:
- New Maven sub-module `power-clickhouse-dao`
- Updated root `pom.xml` to include the new module
- All ClickHouse-related files moved from `power-microbenchmark` to the new module

## Design Options

Two design options were provided to address different use cases:

### Option 1: Single Flat Table with Arrays (Recommended)
- **Structure**: One table with array columns for nested collections
- **Advantages**:
  - Simpler queries
  - Faster inserts
  - Better for trade-level aggregations
  - Less complex data model
- **Use Case**: When most queries are at the trade level

### Option 2: Separate Fact Tables
- **Structure**: Three separate tables (trade fact, line items fact, settlement items fact)
- **Advantages**:
  - Better for line item analysis
  - Supports cross-trade queries
  - Handles very high cardinality scenarios
  - More flexible for complex analytical queries
- **Use Case**: When detailed line item analysis is required

## Implementation Request

The user requested:
> "add java dao layer for clickhouse persistence in power-clickhouse-dao module. This should split the persistable object in com.power.base.dao.clickhouse.persistable package. The Dao should be written to com.power.base.dao.clickhouse.dao package. and service to call dao should go to the com.power.base.dao.clickhouse.service package. Also generate the implementation for both option#1 "Single flat table with arrays" and option#2 "Separate Fact Tables""

### Package Structure Created

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
│   │   ├── PhysicalTradeOlapDao (interface)
│   │   └── PhysicalTradeOlapDaoImpl (JDBC implementation)
│   └── option2/          # DAO for Option 2
│       ├── PhysicalTradeFactDao (interface)
│       └── PhysicalTradeFactDaoImpl (JDBC implementation)
├── service/
│   ├── option1/          # Service for Option 1
│   │   └── PhysicalTradeOlapService
│   └── option2/          # Service for Option 2
│       └── PhysicalTradeFactService
├── config/               # Configuration
│   ├── ClickHouseProperties
│   ├── ClickHouseDataSourceFactory
│   └── ClickHouseAutoConfiguration
└── util/                # Utility classes
    └── ClickHouseArrayUtil
```

## Configuration Externalization

The user requested:
> "externalize the datasource creation properties to spring boot config properties and inject them in the service classes. Use these to instantiate the singleton datasource in the service class itself rather than depending on external users providing the data source."

### Changes Made

1. **Created `ClickHouseProperties.java`**
   - Spring Boot `@ConfigurationProperties` class
   - Prefix: `clickhouse.*`
   - Properties:
     - `url`: ClickHouse JDBC URL
     - `database`: Database name
     - `username`: Username
     - `password`: Password
     - `connection-timeout`: Connection timeout in milliseconds
     - `socket-timeout`: Socket timeout in milliseconds
     - `max-connections`: Maximum pool size
     - `min-idle-connections`: Minimum idle connections

2. **Created `ClickHouseDataSourceFactory.java`**
   - Factory class with singleton pattern (double-checked locking)
   - Creates DataSource from `ClickHouseProperties`
   - Thread-safe singleton management

3. **Updated Service Classes**
   - Both `PhysicalTradeOlapService` and `PhysicalTradeFactService` now:
     - Accept `ClickHouseProperties` via constructor injection
     - Create singleton DataSource internally using the factory
     - Still support DataSource constructor for testing/external use
     - Annotated with `@Service` for Spring Boot auto-wiring

4. **Spring Boot Auto-Configuration**
   - Created `ClickHouseAutoConfiguration.java`
   - Added `META-INF/spring.factories` for auto-configuration discovery
   - Updated POM to make Spring dependencies required
   - Added `spring-boot-configuration-processor` for IDE support

5. **Configuration Example**
   - Created `application-clickhouse.yml.example` with all properties
   - Updated README with Spring Boot configuration examples

## Key Implementation Details

### Data Mapping
- **DTO to Persistable**: Mappers convert `PhysicalPowerTradeDto` to ClickHouse persistable entities
- **Array Handling**: `ClickHouseArrayUtil` provides utilities for converting Java Lists to ClickHouse array strings
- **Reverse Mapping**: Currently marked as TODO (persistable → DTO) for read operations

### DAO Layer
- **JDBC-Based**: Uses standard JDBC with ClickHouse JDBC driver
- **Batch Operations**: Supports efficient bulk inserts
- **Tenant-Aware**: All operations require `tenant_id` parameter
- **Error Handling**: Proper exception handling and resource management

### Service Layer
- **Abstraction**: Provides high-level API over DAO layer
- **DTO Conversion**: Handles conversion between DTOs and persistable entities
- **Batch Support**: Provides batch save operations
- **Query Support**: Date range queries and find by ID operations

## Schema Design

### Partitioning Strategy
```sql
PARTITION BY (tenant_id, toYYYYMM(trade_date))
```
- **tenant_id**: Enables multi-tenant data isolation
- **toYYYYMM(trade_date)**: Monthly partitions for efficient time-based queries

### Ordering Keys
- Optimized for common query patterns
- Includes `tenant_id`, `trade_date`, and `trade_id` for efficient lookups

### Materialized Views (Option 2)
- Pre-aggregated views for common analytical queries
- Automatically maintained by ClickHouse

## Files Created

### Configuration Files
- `pom.xml`: Maven configuration with ClickHouse JDBC and Spring dependencies
- `META-INF/spring.factories`: Spring Boot auto-configuration
- `src/main/resources/application-clickhouse.yml.example`: Configuration example

### Schema Files
- `src/main/resources/schema/clickhouse-physical-trade-schema.sql`: Complete schema for both options

### Java Classes

#### Option 1 (Single Flat Table)
- `persistable/option1/PhysicalTradeOlap.java`: Persistable entity
- `persistable/option1/PhysicalTradeOlapMapper.java`: DTO mapper
- `dao/option1/PhysicalTradeOlapDao.java`: DAO interface
- `dao/option1/PhysicalTradeOlapDaoImpl.java`: JDBC implementation
- `service/option1/PhysicalTradeOlapService.java`: Service layer

#### Option 2 (Separate Fact Tables)
- `persistable/option2/PhysicalTradeFact.java`: Main trade fact entity
- `persistable/option2/PhysicalTradeLineItemFact.java`: Line items fact entity
- `persistable/option2/PhysicalTradeSettlementItemFact.java`: Settlement items fact entity
- `persistable/option2/PhysicalTradeFactMapper.java`: DTO mapper
- `dao/option2/PhysicalTradeFactDao.java`: DAO interface
- `dao/option2/PhysicalTradeFactDaoImpl.java`: JDBC implementation
- `service/option2/PhysicalTradeFactService.java`: Service layer

#### Configuration
- `config/ClickHouseProperties.java`: Spring Boot properties
- `config/ClickHouseDataSourceFactory.java`: DataSource factory
- `config/ClickHouseAutoConfiguration.java`: Auto-configuration class

#### Utilities
- `util/ClickHouseArrayUtil.java`: Array conversion utilities

#### Documentation
- `README.md`: Module overview and usage
- `CLICKHOUSE_DESIGN.md`: Detailed design documentation
- `CONVERSATION_HISTORY.md`: This file

## Dependencies

- **ClickHouse JDBC Driver**: 0.6.0
- **Power Datamodel**: For DTOs
- **Spring Framework**: For Spring Boot integration
- **Spring Boot Auto-Configure**: For auto-configuration
- **Jackson**: For JSON serialization

## Usage Pattern

### Spring Boot (Recommended)
```yaml
# application.yml
clickhouse:
  url: jdbc:clickhouse://localhost:8123/default
  database: default
  username: default
  password: ""
```

```java
@Autowired
private PhysicalTradeOlapService service;
```

### Manual Configuration
```java
ClickHouseProperties properties = new ClickHouseProperties();
properties.setUrl("jdbc:clickhouse://localhost:8123/default");
// ... configure other properties

PhysicalTradeOlapService service = new PhysicalTradeOlapService(properties);
```

## Key Design Decisions

1. **Two Design Options**: Provided flexibility for different use cases
2. **Multi-Tenant Partitioning**: `tenant_id` in partition key for optimal performance
3. **Singleton DataSource**: Services manage their own DataSource singleton
4. **Spring Boot Integration**: Full auto-configuration support
5. **Package Separation**: Clear separation between options and layers
6. **JDBC-Based**: Standard JDBC for maximum compatibility

## Future Enhancements

- **Reverse Mapping**: Implement persistable → DTO conversion for read operations
- **Connection Pooling**: Consider HikariCP or similar for production
- **Query Builder**: Add fluent query builder for complex queries
- **Metrics**: Add metrics/monitoring support
- **Caching**: Consider caching layer for frequently accessed data

## Notes

- All operations are tenant-aware for security and performance
- Batch operations are optimized for high-throughput scenarios
- The singleton DataSource pattern ensures efficient connection management
- Both design options are fully implemented and can be used independently

