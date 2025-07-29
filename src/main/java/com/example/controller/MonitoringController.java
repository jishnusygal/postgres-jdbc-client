package com.example.controller;

import com.example.service.ConnectionMonitoringService;
import com.example.service.VaultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);

    private final ConnectionMonitoringService monitoringService;
    private final VaultService vaultService;

    public MonitoringController(ConnectionMonitoringService monitoringService, 
                               VaultService vaultService) {
        this.monitoringService = monitoringService;
        this.vaultService = vaultService;
    }

    @GetMapping("/connections")
    public ResponseEntity<Map<String, Object>> getConnectionMetrics() {
        try {
            Map<String, Object> metrics = monitoringService.getConnectionMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to retrieve connection metrics", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve connection metrics");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Get connection metrics
            Map<String, Object> connectionMetrics = monitoringService.getConnectionMetrics();
            health.put("connectionPool", connectionMetrics);
            
            // Check Vault accessibility
            boolean vaultAccessible = vaultService.isVaultAccessible();
            Map<String, Object> vaultHealth = new HashMap<>();
            vaultHealth.put("accessible", vaultAccessible);
            vaultHealth.put("path", vaultService.getVaultPath());
            health.put("vault", vaultHealth);
            
            // Overall status
            health.put("status", vaultAccessible ? "UP" : "DEGRADED");
            health.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            logger.error("Failed to retrieve health status", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(health);
        }
    }

    @PostMapping("/connections/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        try {
            monitoringService.performConnectionTest();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Connection test completed successfully");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("message", "Connection test failed: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/connections/suspend")
    public ResponseEntity<Map<String, Object>> suspendConnectionPool() {
        try {
            monitoringService.suspendPool();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Connection pool suspended");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to suspend connection pool", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("message", "Failed to suspend pool: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/connections/resume")
    public ResponseEntity<Map<String, Object>> resumeConnectionPool() {
        try {
            monitoringService.resumePool();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Connection pool resumed");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to resume connection pool", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("message", "Failed to resume pool: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @PostMapping("/connections/evict")
    public ResponseEntity<Map<String, Object>> evictIdleConnections() {
        try {
            monitoringService.softEvictConnections();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Idle connections evicted");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to evict idle connections", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "failed");
            result.put("message", "Failed to evict connections: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    @GetMapping("/monitoring/status")
    public ResponseEntity<Map<String, Object>> getMonitoringStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", monitoringService.isMonitoringEnabled());
        status.put("interval", monitoringService.getMonitoringInterval());
        return ResponseEntity.ok(status);
    }

    @PostMapping("/monitoring/enable")
    public ResponseEntity<Map<String, Object>> enableMonitoring() {
        monitoringService.setMonitoringEnabled(true);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Monitoring enabled");
        result.put("enabled", true);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/monitoring/disable")
    public ResponseEntity<Map<String, Object>> disableMonitoring() {
        monitoringService.setMonitoringEnabled(false);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Monitoring disabled");
        result.put("enabled", false);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/monitoring/interval/{seconds}")
    public ResponseEntity<Map<String, Object>> updateMonitoringInterval(@PathVariable int seconds) {
        if (seconds < 5) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Monitoring interval must be at least 5 seconds");
            return ResponseEntity.badRequest().body(error);
        }
        
        monitoringService.setMonitoringInterval(seconds);
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "Monitoring interval updated");
        result.put("interval", seconds);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/vault/path")
    public ResponseEntity<Map<String, Object>> updateVaultPath(@RequestBody Map<String, String> request) {
        String newPath = request.get("path");
        
        if (newPath == null || newPath.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Vault path cannot be empty");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            String oldPath = vaultService.getVaultPath();
            vaultService.setVaultPath(newPath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Vault path updated");
            result.put("oldPath", oldPath);
            result.put("newPath", newPath);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to update Vault path", e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "Failed to update Vault path: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/vault/path")
    public ResponseEntity<Map<String, Object>> getVaultPath() {
        Map<String, Object> result = new HashMap<>();
        result.put("path", vaultService.getVaultPath());
        result.put("accessible", vaultService.isVaultAccessible());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/schema/status")
    public ResponseEntity<Map<String, Object>> getSchemaStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // This would require injecting SchemaService
            status.put("message", "Schema status - refer to /api/schema/status endpoint");
            status.put("available", true);
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Failed to get schema status", e);
            
            status.put("status", "error");
            status.put("message", "Schema status check failed: " + e.getMessage());
            status.put("available", false);
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(status);
        }
    }
}