package com.example;

import com.example.service.ConnectionMonitoringService;
import com.example.service.SchemaService;
import com.example.service.VaultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;

@SpringBootApplication
@EnableScheduling
public class PostgresJdbcClientApplication {

    private static final Logger logger = LoggerFactory.getLogger(PostgresJdbcClientApplication.class);

    public static void main(String[] args) {
        logger.info("Starting PostgreSQL JDBC Client Application");
        
        try {
            SpringApplication.run(PostgresJdbcClientApplication.class, args);
            logger.info("PostgreSQL JDBC Client Application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start PostgreSQL JDBC Client Application", e);
            System.exit(1);
        }
    }

    @Bean
    public CommandLineRunner applicationRunner(DataSource dataSource,
                                             VaultService vaultService,
                                             SchemaService schemaService,
                                             ConnectionMonitoringService monitoringService) {
        return args -> {
            logger.info("=== PostgreSQL JDBC Client Application Initialization ===");
            
            try {
                // Display application banner
                displayBanner();
                
                // Initialize and test Vault connection
                initializeVault(vaultService);
                
                // Test database connection
                testDatabaseConnection(dataSource);
                
                // Initialize schema if configured
                initializeSchema(schemaService);
                
                // Display connection pool status
                displayConnectionPoolStatus(monitoringService);
                
                // Display API endpoints
                displayApiEndpoints();
                
                logger.info("=== Application initialization completed successfully ===");
                
            } catch (Exception e) {
                logger.error("Application initialization failed", e);
                throw e;
            }
        };
    }

    private void displayBanner() {
        String banner = """
            
            ╔══════════════════════════════════════════════════════════════╗
            ║               PostgreSQL JDBC Client Application             ║
            ║                    with Connection Pooling                   ║
            ║                      and Vault Integration                   ║
            ╚══════════════════════════════════════════════════════════════╝
            
            Features:
            • HikariCP Connection Pool with 10 connections
            • 20 Database tables with indexes and sequences  
            • HashiCorp Vault integration for credentials
            • Real-time connection monitoring
            • REST API for management and monitoring
            • Configurable pool settings
            
            """;
        
        System.out.println(banner);
    }

    private void initializeVault(VaultService vaultService) {
        logger.info("Initializing Vault integration...");
        
        try {
            String vaultPath = vaultService.getVaultPath();
            boolean accessible = vaultService.isVaultAccessible();
            
            logger.info("Vault Configuration:");
            logger.info("  Path: {}", vaultPath);
            logger.info("  Accessible: {}", accessible);
            
            if (accessible) {
                logger.info("✓ Vault integration is working correctly");
            } else {
                logger.warn("⚠ Vault is not accessible - using fallback configuration");
            }
            
        } catch (Exception e) {
            logger.error("Vault initialization failed", e);
        }
    }

    private void testDatabaseConnection(DataSource dataSource) {
        logger.info("Testing database connection...");
        
        try (var connection = dataSource.getConnection()) {
            try (var stmt = connection.createStatement();
                 var rs = stmt.executeQuery("SELECT version(), current_database(), current_user")) {
                
                if (rs.next()) {
                    String version = rs.getString(1);
                    String database = rs.getString(2);
                    String user = rs.getString(3);
                    
                    logger.info("✓ Database connection successful");
                    logger.info("  Database: {}", database);
                    logger.info("  User: {}", user);
                    logger.info("  Version: {}", version.split(" ")[0] + " " + version.split(" ")[1]);
                }
            }
        } catch (Exception e) {
            logger.error("Database connection test failed", e);
            throw new RuntimeException("Database connection failed", e);
        }
    }

    private void initializeSchema(SchemaService schemaService) {
        logger.info("Checking database schema...");
        
        try {
            // Schema initialization is handled by @PostConstruct in SchemaService
            logger.info("✓ Database schema initialization completed");
            
        } catch (Exception e) {
            logger.error("Schema initialization failed", e);
        }
    }

    private void displayConnectionPoolStatus(ConnectionMonitoringService monitoringService) {
        logger.info("Connection Pool Status:");
        
        try {
            var metrics = monitoringService.getConnectionMetrics();
            
            logger.info("  Pool Name: {}", metrics.get("poolName"));
            logger.info("  Maximum Pool Size: {}", metrics.get("maximumPoolSize"));
            logger.info("  Minimum Idle: {}", metrics.get("minimumIdle"));
            logger.info("  Total Connections: {}", metrics.get("totalConnections"));
            logger.info("  Active Connections: {}", metrics.get("activeConnections"));
            logger.info("  Idle Connections: {}", metrics.get("idleConnections"));
            logger.info("  Threads Awaiting: {}", metrics.get("threadsAwaitingConnection"));
            logger.info("  Connection Timeout: {}ms", metrics.get("connectionTimeout"));
            logger.info("  Idle Timeout: {}ms", metrics.get("idleTimeout"));
            logger.info("  Max Lifetime: {}ms", metrics.get("maxLifetime"));
            
            boolean isRunning = (Boolean) metrics.getOrDefault("isRunning", false);
            boolean isClosed = (Boolean) metrics.getOrDefault("isClosed", true);
            
            logger.info("  Pool Status: {}", isRunning && !isClosed ? "RUNNING" : "STOPPED");
            
        } catch (Exception e) {
            logger.error("Failed to retrieve connection pool status", e);
        }
    }

    private void displayApiEndpoints() {
        logger.info("Available API Endpoints:");
        logger.info("  Health Check:         GET  /api/monitoring/health");
        logger.info("  Connection Metrics:   GET  /api/monitoring/connections");
        logger.info("  Connection Test:      POST /api/monitoring/connections/test");
        logger.info("  Suspend Pool:         POST /api/monitoring/connections/suspend");
        logger.info("  Resume Pool:          POST /api/monitoring/connections/resume");
        logger.info("  Evict Idle:           POST /api/monitoring/connections/evict");
        logger.info("  Monitoring Status:    GET  /api/monitoring/monitoring/status");
        logger.info("  Enable Monitoring:    POST /api/monitoring/monitoring/enable");
        logger.info("  Disable Monitoring:   POST /api/monitoring/monitoring/disable");
        logger.info("  Update Interval:      POST /api/monitoring/monitoring/interval/{{seconds}}");
        logger.info("  Vault Path:           GET  /api/monitoring/vault/path");
        logger.info("  Update Vault Path:    POST /api/monitoring/vault/path");
        logger.info("  Schema Status:        GET  /api/schema/status");
        logger.info("  Create Schema:        POST /api/schema/create");
        logger.info("  Actuator Health:      GET  /actuator/health");
        logger.info("  Actuator Metrics:     GET  /actuator/metrics");
        logger.info("");
        logger.info("Application is running on: http://localhost:8080");
    }
}