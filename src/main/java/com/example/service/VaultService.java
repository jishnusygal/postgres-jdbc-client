package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class VaultService {

    private static final Logger logger = LoggerFactory.getLogger(VaultService.class);

    private final VaultTemplate vaultTemplate;

    @Value("${app.vault.path:secret/postgres-db}")
    private String vaultPath;

    @Value("${app.vault.enabled:true}")
    private boolean vaultEnabled;

    public VaultService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    /**
     * Retrieves database credentials from Vault
     * @return Map containing database connection details
     */
    public Map<String, String> getCredentials() {
        if (!vaultEnabled) {
            logger.warn("Vault is disabled, returning empty credentials map");
            return new HashMap<>();
        }

        try {
            logger.info("Retrieving credentials from Vault path: {}", vaultPath);
            
            VaultResponse response = vaultTemplate.read(vaultPath);
            
            if (response == null || response.getData() == null) {
                logger.error("No data found at Vault path: {}", vaultPath);
                throw new RuntimeException("No credentials found in Vault at path: " + vaultPath);
            }

            Map<String, Object> data = response.getData();
            Map<String, String> credentials = new HashMap<>();

            // Extract credentials from Vault response
            credentials.put("url", getStringValue(data, "url"));
            credentials.put("username", getStringValue(data, "username"));
            credentials.put("password", getStringValue(data, "password"));
            credentials.put("driver", getStringValue(data, "driver"));

            logger.info("Successfully retrieved credentials from Vault for URL: {}", 
                       maskUrl(credentials.get("url")));

            return credentials;

        } catch (Exception e) {
            logger.error("Failed to retrieve credentials from Vault at path: {}", vaultPath, e);
            throw new RuntimeException("Failed to retrieve database credentials from Vault", e);
        }
    }

    /**
     * Stores database credentials in Vault (for testing/setup purposes)
     */
    public void storeCredentials(String url, String username, String password, String driver) {
        if (!vaultEnabled) {
            logger.warn("Vault is disabled, cannot store credentials");
            return;
        }

        try {
            Map<String, Object> credentials = new HashMap<>();
            credentials.put("url", url);
            credentials.put("username", username);
            credentials.put("password", password);
            credentials.put("driver", driver);

            vaultTemplate.write(vaultPath, credentials);
            
            logger.info("Successfully stored credentials in Vault at path: {}", vaultPath);

        } catch (Exception e) {
            logger.error("Failed to store credentials in Vault at path: {}", vaultPath, e);
            throw new RuntimeException("Failed to store database credentials in Vault", e);
        }
    }

    /**
     * Checks if Vault is accessible and the path exists
     */
    public boolean isVaultAccessible() {
        if (!vaultEnabled) {
            return false;
        }

        try {
            VaultResponse response = vaultTemplate.read(vaultPath);
            return response != null;
        } catch (Exception e) {
            logger.warn("Vault is not accessible: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the current Vault path being used
     */
    public String getVaultPath() {
        return vaultPath;
    }

    /**
     * Updates the Vault path (useful for dynamic configuration)
     */
    public void setVaultPath(String newPath) {
        logger.info("Updating Vault path from {} to {}", this.vaultPath, newPath);
        this.vaultPath = newPath;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            logger.warn("Missing key '{}' in Vault data", key);
            return "";
        }
        return value.toString();
    }

    private String maskUrl(String url) {
        if (url == null) return "null";
        // Mask sensitive parts of URL for logging
        return url.replaceAll("://[^@]+@", "://***:***@");
    }
}