# ClickHouse DAO Tests

This directory contains integration tests for the ClickHouse DAO implementation.

## Prerequisites

### 1. Start ClickHouse Docker Container

```bash
docker run -d \
  --name clickhouse-test \
  -p 8123:8123 \
  -p 9000:9000 \
  -e CLICKHOUSE_USER=admin \
  -e CLICKHOUSE_PASSWORD=supersecret \
  clickhouse/clickhouse-server
```

### 2. Create Database and Table

Connect to ClickHouse:

```bash
docker exec -it clickhouse-test clickhouse-client --user admin --password supersecret
```

Then run:

```sql
CREATE DATABASE IF NOT EXISTS mydb;
USE mydb;
```

Then execute the schema SQL file:

```bash
# From the project root
cat power-clickhouse-dao/src/main/resources/schema/clickhouse-physical-trade-schema.sql | \
  docker exec -i clickhouse-test clickhouse-client --user admin --password supersecret --database mydb
```

Or manually copy and paste the SQL from:
`power-clickhouse-dao/src/main/resources/schema/clickhouse-physical-trade-schema.sql`

Make sure to execute the **Option 1** table creation (the `physical_trades_olap` table).

### 3. Verify Connection

Test the connection:

```bash
docker exec -it clickhouse-test clickhouse-client --user admin --password supersecret --database mydb --query "SELECT 1"
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=PhysicalTradeOlapServiceTest
```

### Run from IDE

1. Ensure ClickHouse is running (see Prerequisites)
2. Run `PhysicalTradeOlapServiceTest` as a JUnit test

## Test Configuration

The tests use the configuration from:
- `src/test/resources/application.yml`

This file contains:
```yaml
clickhouse:
  url: jdbc:clickhouse://localhost:8123/mydb
  database: mydb
  username: admin
  password: "supersecret"
```

## Test Coverage

The `PhysicalTradeOlapServiceTest` covers:

1. **save()** - Save single trade
   - Normal save
   - Save with null DTO (should throw exception)
   - Save with empty line items

2. **batchSave()** - Batch save multiple trades
   - Save multiple trades
   - Empty list handling
   - Null list handling
   - Large batch (10+ trades)

3. **findByTradeId()** - Find trade by ID
   - Find existing trade
   - Find non-existent trade

4. **findByDateRange()** - Query by date range
   - Find trades in date range
   - Empty date range

5. **deleteByTradeId()** - Delete trade
   - Delete existing trade
   - Delete non-existent trade
   - Tenant isolation (delete with different tenant)

6. **Complete Workflow** - End-to-end test
   - Save → Find → Delete workflow

## Notes

- Tests are ordered using `@Order` annotations to ensure proper execution sequence
- Test data is cleaned up in `@AfterAll` method
- Tests use a dedicated tenant ID: `TEST_TENANT`
- Currently, `findByTradeId()` and `findByDateRange()` return null/empty until reverse mapper is implemented

## Troubleshooting

### Connection Refused

- Ensure ClickHouse container is running: `docker ps | grep clickhouse`
- Check port 8123 is accessible: `telnet localhost 8123`

### Authentication Failed

- Verify username/password in `application.yml` matches Docker container settings
- Check ClickHouse user configuration

### Table Not Found

- Ensure schema SQL has been executed
- Verify database name is `mydb`
- Check table exists: `SHOW TABLES FROM mydb;`

### Test Failures

- Check ClickHouse logs: `docker logs clickhouse-test`
- Verify test data cleanup completed successfully
- Ensure no other tests are running concurrently

