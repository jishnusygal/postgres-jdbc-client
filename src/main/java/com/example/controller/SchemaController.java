package com.example.controller;

import com.example.service.SchemaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/schema")
public class SchemaController {

    private static final Logger logger = LoggerFactory.getLogger(SchemaController.class);

    private final SchemaService schemaService;

    public SchemaController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSchemaStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("exists", schemaService.schemaExists());
            status.put("valid", schemaService.validateSchema());
            status.put("statistics", schemaService.getSchemaStatistics());
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to get schema status", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get schema status: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSchema(@RequestParam(defaultValue = "false") boolean dropExisting) {
        try {
            logger.info("Schema creation requested via API, dropExisting: {}", dropExisting);
            
            long startTime = System.currentTimeMillis();
            
            if (dropExisting) {
                schemaService.dropSchema();
            }
            
            schemaService.createSchema();
            
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Schema created successfully");
            result.put("duration", duration + "ms");
            result.put("statistics", schemaService.getSchemaStatistics());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Schema creation failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Schema creation failed: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/drop")
    public ResponseEntity<Map<String, Object>> dropSchema() {
        try {
            logger.warn("Schema drop requested via API");
            
            long startTime = System.currentTimeMillis();
            schemaService.dropSchema();
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Schema dropped successfully");
            result.put("duration", duration + "ms");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Schema drop failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Schema drop failed: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/recreate")
    public ResponseEntity<Map<String, Object>> recreateSchema() {
        try {
            logger.info("Schema recreation requested via API");
            
            long startTime = System.currentTimeMillis();
            schemaService.recreateSchema();
            long duration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Schema recreated successfully");
            result.put("duration", duration + "ms");
            result.put("statistics", schemaService.getSchemaStatistics());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Schema recreation failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Schema recreation failed: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSchema() {
        try {
            boolean isValid = schemaService.validateSchema();
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", isValid);
            result.put("statistics", schemaService.getSchemaStatistics());
            result.put("timestamp", System.currentTimeMillis());
            
            if (isValid) {
                result.put("status", "success");
                result.put("message", "Schema validation passed");
            } else {
                result.put("status", "warning");
                result.put("message", "Schema validation failed - some objects are missing");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Schema validation failed", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("valid", false);
            error.put("message", "Schema validation error: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSchemaStatistics() {
        try {
            SchemaService.SchemaStatistics stats = schemaService.getSchemaStatistics();
            
            Map<String, Object> result = new HashMap<>();
            result.put("statistics", stats);
            result.put("summary", stats.toString());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to get schema statistics", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get schema statistics: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSchemaInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            info.put("description", "PostgreSQL JDBC Client Database Schema");
            info.put("version", "1.0.0");
            info.put("tables", 20);
            info.put("expectedSequences", 20);
            info.put("features", new String[]{
                "20 business tables with relationships",
                "Comprehensive indexing for performance",
                "Auto-incrementing sequences",
                "Business views and stored procedures",
                "Audit triggers and logging",
                "Data quality constraints"
            });
            info.put("sqlFiles", new String[]{
                "00_create_schema.sql - Main schema creation script",
                "01_create_sequences.sql - Database sequences",
                "02_create_tables.sql - All 20 tables",
                "03_create_indexes.sql - Performance indexes",
                "04_sample_data.sql - Sample data for testing",
                "05_migration_v1_0_1.sql - Example migration",
                "99_drop_schema.sql - Complete cleanup"
            });
            info.put("exists", schemaService.schemaExists());
            info.put("statistics", schemaService.getSchemaStatistics());
            info.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            logger.error("Failed to get schema info", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get schema info: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/tables")
    public ResponseEntity<Map<String, Object>> getTableList() {
        try {
            // This would require additional implementation in SchemaService
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Table list functionality - to be implemented");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to get table list", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to get table list: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }
}