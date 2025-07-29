package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class SchemaService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaService.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.schema.auto-create:true}")
    private boolean autoCreateSchema;

    @Value("${app.schema.drop-existing:false}")
    private boolean dropExisting;

    @Value("${app.schema.script-location:classpath:db/}")
    private String scriptLocation;

    @Value("${app.schema.create-sample-data:false}")
    private boolean createSampleData;

    public SchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initializeSchema() {
        if (autoCreateSchema) {
            logger.info("Auto-creating database schema from SQL files");
            createSchema();
        }
    }

    public void createSchema() {
        try {
            if (dropExisting) {
                logger.info("Dropping existing schema");
                dropSchema();
            }

            logger.info("Creating database schema with 20 tables, indexes, and sequences from SQL files");
            
            // Execute SQL files in order
            executeSchemaScript("00_create_schema.sql");
            
            if (createSampleData) {
                logger.info("Creating sample data");
                executeSchemaScript("04_sample_data.sql");
            }
            
            logger.info("Database schema created successfully from SQL files");
            
        } catch (Exception e) {
            logger.error("Failed to create database schema", e);
            throw new RuntimeException("Schema creation failed", e);
        }
    }

    /**
     * Execute a SQL script file
     */
    private void executeSchemaScript(String scriptName) {
        try {
            logger.info("Executing SQL script: {}", scriptName);
            
            ClassPathResource resource = new ClassPathResource("db/" + scriptName);
            if (!resource.exists()) {
                logger.warn("SQL script not found: {}", scriptName);
                return;
            }
            
            String sql = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            );
            
            // Split SQL into individual statements (basic implementation)
            String[] statements = sql.split(";\\s*\\n");
            
            int executedCount = 0;
            for (String statement : statements) {
                statement = statement.trim();
                if (!statement.isEmpty() && !statement.startsWith("--") && !statement.startsWith("\\")) {
                    try {
                        jdbcTemplate.execute(statement);
                        executedCount++;
                    } catch (Exception e) {
                        logger.warn("Failed to execute statement: {}", statement.substring(0, Math.min(statement.length(), 100)), e);
                    }
                }
            }
            
            logger.info("Successfully executed {} statements from {}", executedCount, scriptName);
            
        } catch (Exception e) {
            logger.error("Failed to execute SQL script: {}", scriptName, e);
            throw new RuntimeException("SQL script execution failed: " + scriptName, e);
        }
    }

    /**
     * Execute individual SQL script files in order
     */
    private void executeSchemaScriptsIndividually() {
        String[] scripts = {
            "01_create_sequences.sql",
            "02_create_tables.sql", 
            "03_create_indexes.sql"
        };
        
        for (String script : scripts) {
            executeSchemaScript(script);
        }
    }

    /**
     * Drop the existing schema
     */
    public void dropSchema() {
        try {
            logger.info("Dropping existing schema");
            executeSchemaScript("99_drop_schema.sql");
        } catch (Exception e) {
            logger.warn("Failed to execute drop schema script, attempting manual cleanup");
            manualDropSchema();
        }
    }
    
    /**
     * Manual schema cleanup if script fails
     */
    private void manualDropSchema() {
        logger.info("Performing manual schema cleanup");
        
        String[] tables = {
            "logs", "documents", "contacts", "addresses", "promotions", "reviews",
            "shipments", "inventory", "payments", "invoices", "tasks", "projects",
            "employees", "departments", "suppliers", "orders", "customers", 
            "products", "categories", "users", "schema_info"
        };

        // Drop tables in reverse order to handle foreign key constraints
        for (String table : tables) {
            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS " + table + " CASCADE");
                logger.debug("Dropped table: {}", table);
            } catch (Exception e) {
                logger.warn("Failed to drop table: {}", table, e);
            }
        }

        // Drop sequences
        String[] sequences = {
            "users_seq", "orders_seq", "products_seq", "categories_seq", "customers_seq",
            "suppliers_seq", "employees_seq", "departments_seq", "projects_seq", "tasks_seq",
            "invoices_seq", "payments_seq", "inventory_seq", "shipments_seq", "reviews_seq",
            "promotions_seq", "addresses_seq", "contacts_seq", "documents_seq", "logs_seq"
        };

        for (String seq : sequences) {
            try {
                jdbcTemplate.execute("DROP SEQUENCE IF EXISTS " + seq + " CASCADE");
                logger.debug("Dropped sequence: {}", seq);
            } catch (Exception e) {
                logger.warn("Failed to drop sequence: {}", seq, e);
            }
        }
        
        // Drop views
        try {
            jdbcTemplate.execute("DROP VIEW IF EXISTS v_customer_summary CASCADE");
            jdbcTemplate.execute("DROP VIEW IF EXISTS v_product_inventory CASCADE");
            jdbcTemplate.execute("DROP VIEW IF EXISTS v_order_details CASCADE");
            jdbcTemplate.execute("DROP VIEW IF EXISTS v_project_summary CASCADE");
        } catch (Exception e) {
            logger.warn("Failed to drop views", e);
        }
        
        // Drop types
        try {
            jdbcTemplate.execute("DROP TYPE IF EXISTS order_status_type CASCADE");
            jdbcTemplate.execute("DROP TYPE IF EXISTS payment_status_type CASCADE");
            jdbcTemplate.execute("DROP TYPE IF EXISTS task_status_type CASCADE");
            jdbcTemplate.execute("DROP TYPE IF EXISTS priority_type CASCADE");
        } catch (Exception e) {
            logger.warn("Failed to drop types", e);
        }
    }

    /**
     * Validate schema structure
     */
    public boolean validateSchema() {
        try {
            logger.info("Validating database schema");
            
            // Check if all expected tables exist
            String[] expectedTables = {
                "users", "categories", "products", "customers", "orders",
                "suppliers", "employees", "departments", "projects", "tasks",
                "invoices", "payments", "inventory", "shipments", "reviews",
                "promotions", "addresses", "contacts", "documents", "logs"
            };
            
            for (String table : expectedTables) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = 'public'",
                    Integer.class, table
                );
                
                if (count == null || count == 0) {
                    logger.error("Required table '{}' not found", table);
                    return false;
                }
            }
            
            // Check if sequences exist
            String[] expectedSequences = {
                "users_seq", "categories_seq", "products_seq", "customers_seq", "orders_seq"
            };
            
            for (String seq : expectedSequences) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.sequences WHERE sequence_name = ? AND sequence_schema = 'public'",
                    Integer.class, seq
                );
                
                if (count == null || count == 0) {
                    logger.error("Required sequence '{}' not found", seq);
                    return false;
                }
            }
            
            logger.info("Schema validation completed successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Schema validation failed", e);
            return false;
        }
    }

    /**
     * Get schema statistics
     */
    public SchemaStatistics getSchemaStatistics() {
        try {
            SchemaStatistics stats = new SchemaStatistics();
            
            // Count tables
            stats.tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'",
                Integer.class
            );
            
            // Count sequences
            stats.sequenceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.sequences WHERE sequence_schema = 'public'",
                Integer.class
            );
            
            // Count indexes
            stats.indexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public'",
                Integer.class
            );
            
            // Count views
            stats.viewCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.views WHERE table_schema = 'public'",
                Integer.class
            );
            
            // Count functions
            stats.functionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = 'public'",
                Integer.class
            );
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Failed to get schema statistics", e);
            return new SchemaStatistics();
        }
    }

    /**
     * Schema statistics data class
     */
    public static class SchemaStatistics {
        public int tableCount = 0;
        public int sequenceCount = 0;
        public int indexCount = 0;
        public int viewCount = 0;
        public int functionCount = 0;
        
        @Override
        public String toString() {
            return String.format("Tables: %d, Sequences: %d, Indexes: %d, Views: %d, Functions: %d",
                tableCount, sequenceCount, indexCount, viewCount, functionCount);
        }
    }

    /**
     * Recreate schema (drop and create)
     */
    public void recreateSchema() {
        logger.info("Recreating database schema");
        dropSchema();
        createSchema();
    }

    /**
     * Check if schema exists and is valid
     */
    public boolean schemaExists() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'users' AND table_schema = 'public'",
                Integer.class
            );
            return count != null && count > 0;
        } catch (Exception e) {
            logger.debug("Error checking schema existence", e);
            return false;
        }
    }
}