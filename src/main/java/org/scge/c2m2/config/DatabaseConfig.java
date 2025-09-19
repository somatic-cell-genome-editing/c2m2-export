package org.scge.c2m2.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuration class for database connection setup.
 * Creates and configures the HikariCP connection pool.
 */
@Configuration
@EnableConfigurationProperties(DatabaseProperties.class)
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    /**
     * Creates and configures the HikariCP DataSource bean.
     * 
     * @param properties Database configuration properties
     * @return Configured HikariDataSource
     */
    @Bean
    public DataSource dataSource(DatabaseProperties properties) {
        logger.info("Configuring database connection pool for URL: {}", 
                   maskUrl(properties.url()));
        
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(properties.url());
        config.setUsername(properties.username());
        config.setPassword(properties.password());
        config.setDriverClassName("org.postgresql.Driver");
        
        // Pool configuration
        config.setMaximumPoolSize(properties.pool().maxSize());
        config.setConnectionTimeout(properties.pool().connectionTimeout());
        config.setIdleTimeout(properties.pool().idleTimeout());
        config.setMaxLifetime(properties.pool().maxLifetime());
        config.setMinimumIdle(Math.min(2, properties.pool().maxSize() / 2));
        
        // Additional pool settings for reliability
        config.setLeakDetectionThreshold(60000); // 1 minute
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5000);
        
        // Pool name for monitoring
        config.setPoolName("C2M2-HikariCP");
        
        logger.info("Database pool configured - Max size: {}, Connection timeout: {}ms, Idle timeout: {}ms",
                   properties.pool().maxSize(),
                   properties.pool().connectionTimeout(),
                   properties.pool().idleTimeout());
        
        return new HikariDataSource(config);
    }
    
    /**
     * Masks sensitive information in the database URL for logging.
     * 
     * @param url Database URL
     * @return Masked URL
     */
    private String maskUrl(String url) {
        if (url == null || !url.contains("://")) {
            return url;
        }
        
        try {
            int protocolEnd = url.indexOf("://") + 3;
            int hostStart = url.indexOf("@", protocolEnd);
            if (hostStart == -1) {
                return url; // No credentials in URL
            }
            
            String protocol = url.substring(0, protocolEnd);
            String hostAndPath = url.substring(hostStart);
            return protocol + "***:***" + hostAndPath;
        } catch (Exception e) {
            return "***masked***";
        }
    }
}