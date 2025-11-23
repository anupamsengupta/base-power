/**
 * ClickHouse DAO layer for Power Base project.
 * <p>
 * This package provides data access objects for storing PhysicalPowerTradeDto
 * in ClickHouse OLAP database using two different design options:
 * <ul>
 *   <li>Option 1: Single flat table with arrays (recommended for most use cases)</li>
 *   <li>Option 2: Separate fact tables (for more flexible line item queries)</li>
 * </ul>
 * <p>
 * Package structure:
 * <ul>
 *   <li>{@link com.power.base.dao.clickhouse.persistable} - Persistable entities</li>
 *   <li>{@link com.power.base.dao.clickhouse.dao} - DAO interfaces and implementations</li>
 *   <li>{@link com.power.base.dao.clickhouse.service} - Service layer</li>
 *   <li>{@link com.power.base.dao.clickhouse.config} - Configuration utilities</li>
 *   <li>{@link com.power.base.dao.clickhouse.util} - Utility classes</li>
 * </ul>
 */
package com.power.base.dao.clickhouse;

