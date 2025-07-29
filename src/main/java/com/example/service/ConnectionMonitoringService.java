package com.example.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConnectionMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionMonitoringService.class);

    private final DataSource dataSource;

    @Value("${app.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    @Value("${app.monitoring.interval:30}")
    private int monitoringInterval;

    private HikariPoolMXBean poolMXBean;

    public ConnectionMonitoringService(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeMonitoring();
    }

    private void initializeMonitoring() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDS = (HikariDataSource) dataSource;
            this.poolMXBean = hikariDS.getHikariPoolMXBean();
            logger.info("Connection monitoring initialized for HikariCP pool: {}", 
                       hikariDS.getPoolName());
        } else {
            logger.warn("DataSource is not HikariDataSource, limited monitoring available");
        }
    }

    @Scheduled(fixedDelayString = "${app.monitoring.interval:30}000")
    public void logConnectionStatus() {
        if (!monitoringEnabled) {
            return;
        }

        try {
            Map<String, Object> metrics = getConnectionMetrics();
            
            logger.info("Connection Pool Status - Total: {}, Active: {}, Idle: {}, Waiting: {}",
                       metrics.get("totalConnections"),
                       metrics.get("activeConnections"),
                       metrics.get("idleConnections"),
                       metrics.get("threadsAwaitingConnection"));

            // Log additional details at debug level
            if (logger.isDebugEnabled()) {
                logger.debug("Detailed Pool Metrics: {}", metrics);
            }

            // Log warnings for potential issues
            checkPoolHealth(metrics);

        } catch (Exception e) {
            logger.error("Failed to retrieve connection pool metrics", e);
        }
    }

    public Map<String, Object> getConnectionMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        if (poolMXBean != null) {
            metrics.put("totalConnections", poolMXBean.getTotalConnections());
            metrics.put("activeConnections", poolMXBean.getActiveConnections());
            metrics.put("idleConnections", poolMXBean.getIdleConnections());
            metrics.put("threadsAwaitingConnection", poolMXBean.getThreadsAwaitingConnection());
            
            // Additional HikariCP metrics
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                metrics.put("maximumPoolSize", hikariDS.getMaximumPoolSize());
                metrics.put("minimumIdle", hikariDS.getMinimumIdle());
                metrics.put("connectionTimeout", hikariDS.getConnectionTimeout());
                metrics.put("idleTimeout", hikariDS.getIdleTimeout());
                metrics.put("maxLifetime", hikariDS.getMaxLifetime());
                metrics.put("poolName", hikariDS.getPoolName());
                metrics.put("isClosed", hikariDS.isClosed());
                metrics.put("isRunning", hikariDS.isRunning());
            }
        } else {
            logger.warn("Pool MXBean not available, cannot retrieve detailed metrics");
            metrics.put("totalConnections", "N/A");
            metrics.put("activeConnections", "N/A");
            metrics.put("idleConnections", "N/A");
            metrics.put("threadsAwaitingConnection", "N/A");
        }

        return metrics;
    }

    private void checkPoolHealth(Map<String, Object> metrics) {
        try {
            if (poolMXBean != null) {
                int totalConnections = poolMXBean.getTotalConnections();
                int activeConnections = poolMXBean.getActiveConnections();
                int threadsWaiting = poolMXBean.getThreadsAwaitingConnection();
                
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                int maxPoolSize = hikariDS.getMaximumPoolSize();

                // Check for pool exhaustion
                if (totalConnections >= maxPoolSize) {
                    logger.warn("Connection pool is at maximum capacity! " +
                               "Total: {}, Max: {}", totalConnections, maxPoolSize);
                }

                // Check for high utilization
                double utilizationPercent = (double) activeConnections / maxPoolSize * 100;
                if (utilizationPercent > 80) {
                    logger.warn("High connection pool utilization: {:.1f}% " +
                               "(Active: {}, Max: {})", utilizationPercent, activeConnections, maxPoolSize);
                }

                // Check for waiting threads
                if (threadsWaiting > 0) {
                    logger.warn("Threads waiting for connections: {}", threadsWaiting);
                }

                // Check if pool is closed
                if (hikariDS.isClosed()) {
                    logger.error("Connection pool is closed!");
                }
            }
        } catch (Exception e) {
            logger.error("Error during pool health check", e);
        }
    }

    public void performConnectionTest() {
        logger.info("Performing connection test");
        
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;
                
                // Test getting a connection
                long startTime = System.currentTimeMillis();
                try (var connection = hikariDS.getConnection()) {
                    long connectionTime = System.currentTimeMillis() - startTime;
                    
                    // Test executing a simple query
                    try (var stmt = connection.createStatement();
                         var rs = stmt.executeQuery("SELECT 1")) {
                        
                        if (rs.next()) {
                            logger.info("Connection test successful - Connection time: {}ms", connectionTime);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Connection test failed", e);
        }
    }

    public boolean isMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void setMonitoringEnabled(boolean enabled) {
        this.monitoringEnabled = enabled;
        logger.info("Connection monitoring {}", enabled ? "enabled" : "disabled");
    }

    public int getMonitoringInterval() {
        return monitoringInterval;
    }

    public void setMonitoringInterval(int intervalSeconds) {
        this.monitoringInterval = intervalSeconds;
        logger.info("Monitoring interval updated to {} seconds", intervalSeconds);
    }

    /**
     * Suspends the connection pool
     */
    public void suspendPool() {
        if (poolMXBean != null) {
            try {
                poolMXBean.suspendPool();
                logger.info("Connection pool suspended");
            } catch (Exception e) {
                logger.error("Failed to suspend connection pool", e);
            }
        }
    }

    /**
     * Resumes the connection pool
     */
    public void resumePool() {
        if (poolMXBean != null) {
            try {
                poolMXBean.resumePool();
                logger.info("Connection pool resumed");
            } catch (Exception e) {
                logger.error("Failed to resume connection pool", e);
            }
        }
    }

    /**
     * Soft evicts currently idle connections
     */
    public void softEvictConnections() {
        if (poolMXBean != null) {
            try {
                poolMXBean.softEvictConnections();
                logger.info("Soft evicted idle connections");
            } catch (Exception e) {
                logger.error("Failed to soft evict connections", e);
            }
        }
    }
}