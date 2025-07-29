package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

@Configuration
public class VaultConfiguration {

    @Value("${app.vault.host:localhost}")
    private String vaultHost;

    @Value("${app.vault.port:8200}")
    private int vaultPort;

    @Value("${app.vault.scheme:http}")
    private String vaultScheme;

    @Value("${app.vault.token}")
    private String vaultToken;

    @Bean
    public VaultTemplate vaultTemplate() {
        VaultEndpoint vaultEndpoint = VaultEndpoint.from(URI.create(
            String.format("%s://%s:%d", vaultScheme, vaultHost, vaultPort)
        ));

        TokenAuthentication authentication = new TokenAuthentication(vaultToken);
        
        return new VaultTemplate(vaultEndpoint, authentication);
    }

    @Bean
    public VaultProperties vaultProperties() {
        VaultProperties properties = new VaultProperties();
        properties.setHost(vaultHost);
        properties.setPort(vaultPort);
        properties.setScheme(vaultScheme);
        properties.setToken(vaultToken);
        return properties;
    }

    public static class VaultProperties {
        private String host;
        private int port;
        private String scheme;
        private String token;
        private String path;

        // Getters and setters
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public String getScheme() { return scheme; }
        public void setScheme(String scheme) { this.scheme = scheme; }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}