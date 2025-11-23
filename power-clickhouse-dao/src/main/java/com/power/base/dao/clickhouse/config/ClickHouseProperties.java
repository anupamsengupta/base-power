package com.power.base.dao.clickhouse.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring Boot configuration properties for ClickHouse DataSource.
 */
@ConfigurationProperties(prefix = "clickhouse")
public class ClickHouseProperties {

    /**
     * ClickHouse JDBC URL (e.g., "jdbc:clickhouse://localhost:8123/default")
     */
    private String url = "jdbc:clickhouse://localhost:8123/default";

    /**
     * Database name
     */
    private String database = "default";

    /**
     * Username for ClickHouse connection
     */
    private String username = "default";

    /**
     * Password for ClickHouse connection
     */
    private String password = "";

    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeout = 30000;

    /**
     * Socket timeout in milliseconds
     */
    private int socketTimeout = 30000;

    /**
     * Maximum number of connections in the pool
     */
    private int maxConnections = 10;

    /**
     * Minimum number of idle connections in the pool
     */
    private int minIdleConnections = 2;

    /**
     * Compression algorithm (e.g., "lz4", "gzip", "zstd", "none")
     * If not set, defaults to LZ4 if available
     */
    private String compressAlgorithm;

    /**
     * Enable/disable compression (true = enabled, false = disabled)
     * If not set, compression is enabled by default
     */
    private Boolean compress;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMinIdleConnections() {
        return minIdleConnections;
    }

    public void setMinIdleConnections(int minIdleConnections) {
        this.minIdleConnections = minIdleConnections;
    }

    public String getCompressAlgorithm() {
        return compressAlgorithm;
    }

    public void setCompressAlgorithm(String compressAlgorithm) {
        this.compressAlgorithm = compressAlgorithm;
    }

    public Boolean getCompress() {
        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }
}

