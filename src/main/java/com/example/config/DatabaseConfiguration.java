package com.example.config;

import com.example.service.VaultService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DatabaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Value("${app.vault.enabled:true}")
    private boolean vaultEnabled;

    @Value("${app.database.pool.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${app.database.pool.minimum-idle:5}")
    private int minimumIdle;

    @Value("${app.database.pool.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${app.database.pool.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${app.database.pool.max-lifetime:1800000}")
    private long maxLifetime;

    @Value("${app.database.pool.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    // Fallback properties
    @Value("${app.database.fallback.url}")
    private String fallbackUrl;

    @Value("${app.database.fallback.username}")
    private String fallbackUsername;

    @Value("${app.database.fallback.password}")
    private String fallbackPassword;

    @Value("${app.database.fallback.driver-class-name}")
    private String fallbackDriverClassName;

    private final VaultService vaultService;

    public DatabaseConfiguration(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        try {
            if (vaultEnabled) {
                logger.info("Attempting to retrieve database credentials from Vault");
                Map<String, String> dbCredentials = vaultService.getCredentials();
                
                config.setJdbcUrl(dbCredentials.get("url"));
                config.setUsername(dbCredentials.get("username"));
                config.setPassword(dbCredentials.get("password"));
                config.setDriverClassName(dbCredentials.getOrDefault("driver", "org.postgresql.Driver"));
                
                logger.info("Successfully configured database connection using Vault credentials");
            } else {
                logger.warn("Vault is disabled, using fallback configuration");
                configureWithFallback(config);
            }
        } catch (Exception e) {
            logger.error("Failed to retrieve credentials from Vault, falling back to default configuration", e);
            configureWithFallback(config);
        }

        // Connection pool configuration
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);

        // Pool name and additional settings
        config.setPoolName("PostgreSQLPool");
        config.setConnectionTestQuery("SELECT 1");
        config.setAutoCommit(true);

        // Additional PostgreSQL specific settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("reWriteBatchedInserts", "true");

        logger.info("Creating HikariCP DataSource with pool size: {}, minimum idle: {}", 
                   maximumPoolSize, minimumIdle);

        return new HikariDataSource(config);
    }

    private void configureWithFallback(HikariConfig config) {
        config.setJdbcUrl(fallbackUrl);
        config.setUsername(fallbackUsername);
        config.setPassword(fallbackPassword);
        config.setDriverClassName(fallbackDriverClassName);
    }

    // Configuration properties for monitoring
    @Bean
    @ConfigurationProperties(prefix = "app.database.pool")
    public DatabasePoolProperties databasePoolProperties() {
        return new DatabasePoolProperties();
    }

    public static class DatabasePoolProperties {
        private int maximumPoolSize = 10;
        private int minimumIdle = 5;
        private long connectionTimeout = 30000;
        private long idleTimeout = 600000;
        private long maxLifetime = 1800000;
        private long leakDetectionThreshold = 60000;

        // Getters and setters
        public int getMaximumPoolSize() { return maximumPoolSize; }
        public void setMaximumPoolSize(int maximumPoolSize) { this.maximumPoolSize = maximumPoolSize; }

        public int getMinimumIdle() { return minimumIdle; }
        public void setMinimumIdle(int minimumIdle) { this.minimumIdle = minimumIdle; }

        public long getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }

        public long getMaxLifetime() { return maxLifetime; }
        public void setMaxLifetime(long maxLifetime) { this.maxLifetime = maxLifetime; }

        public long getLeakDetectionThreshold() { return leakDetectionThreshold; }
        public void setLeakDetectionThreshold(long leakDetectionThreshold) { this.leakDetectionThreshold = leakDetectionThreshold; }
    }
}