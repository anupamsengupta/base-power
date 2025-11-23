package com.power.base.dao.clickhouse.config;

import com.clickhouse.jdbc.ClickHouseDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory for creating ClickHouse DataSource instances.
 * Creates a singleton DataSource based on configuration properties.
 */
public class ClickHouseDataSourceFactory {

    private static volatile DataSource singletonDataSource;
    private static final Object lock = new Object();

    /**
     * Create or get singleton ClickHouse DataSource from properties.
     *
     * @param properties the ClickHouse configuration properties
     * @return singleton DataSource instance
     */
    public static DataSource getOrCreateDataSource(ClickHouseProperties properties) {
        if (singletonDataSource == null) {
            synchronized (lock) {
                if (singletonDataSource == null) {
                    singletonDataSource = createDataSource(properties);
                }
            }
        }
        return singletonDataSource;
    }

    /**
     * Create a new ClickHouse DataSource from properties.
     *
     * @param properties the ClickHouse configuration properties
     * @return new DataSource instance
     */
    public static DataSource createDataSource(ClickHouseProperties properties) {
        try {
            Properties props = new Properties();
            props.setProperty("database", properties.getDatabase());
            props.setProperty("user", properties.getUsername());
            props.setProperty("password", properties.getPassword());
            props.setProperty("connect_timeout", String.valueOf(properties.getConnectionTimeout()));
            props.setProperty("socket_timeout", String.valueOf(properties.getSocketTimeout()));
            
            // Configure compression (LZ4 is default, but we can override if needed)
            // If compress_algorithm is not set, it will use LZ4 if available
            if (properties.getCompressAlgorithm() != null && !properties.getCompressAlgorithm().isEmpty()) {
                props.setProperty("compress_algorithm", properties.getCompressAlgorithm());
            }
            if (properties.getCompress() != null) {
                props.setProperty("compress", String.valueOf(properties.getCompress() ? 1 : 0));
            }

            return new ClickHouseDataSource(properties.getUrl(), props);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create ClickHouse DataSource", e);
        }
    }

    /**
     * Reset the singleton DataSource (useful for testing).
     */
    public static void reset() {
        synchronized (lock) {
            singletonDataSource = null;
        }
    }
}

