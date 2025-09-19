package org.scge.c2m2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for database connection settings.
 * Maps to the 'database' section in application.yml.
 */
@ConfigurationProperties(prefix = "database")
@Validated
public record DatabaseProperties(
    @NotEmpty String url,
    @NotEmpty String username,
    @NotEmpty String password,
    @Valid @NotNull Pool pool
) {
    
    /**
     * Connection pool configuration properties.
     */
    public record Pool(
        @Min(1) int maxSize,
        @Min(1000) long connectionTimeout,
        @Min(30000) long idleTimeout,
        @Min(300000) long maxLifetime
    ) {
        public Pool() {
            this(10, 30000L, 600000L, 1800000L);
        }
    }
    
    public DatabaseProperties() {
        this("", "", "", new Pool());
    }
}