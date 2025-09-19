package org.scge.c2m2.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseProperties configuration binding.
 */
class DatabasePropertiesTest {
    
    @Test
    void testDefaultPoolConfiguration() {
        var pool = new DatabaseProperties.Pool();
        
        assertEquals(10, pool.maxSize());
        assertEquals(30000L, pool.connectionTimeout());
        assertEquals(600000L, pool.idleTimeout());
        assertEquals(1800000L, pool.maxLifetime());
    }
    
    @Test
    void testCustomPoolConfiguration() {
        var pool = new DatabaseProperties.Pool(5, 15000L, 300000L, 900000L);
        
        assertEquals(5, pool.maxSize());
        assertEquals(15000L, pool.connectionTimeout());
        assertEquals(300000L, pool.idleTimeout());
        assertEquals(900000L, pool.maxLifetime());
    }
    
    @Test
    void testDatabasePropertiesBinding() {
        Map<String, Object> properties = Map.of(
            "database.url", "jdbc:postgresql://localhost:5432/test_db",
            "database.username", "test_user",
            "database.password", "test_password",
            "database.pool.max-size", "15",
            "database.pool.connection-timeout", "20000",
            "database.pool.idle-timeout", "500000",
            "database.pool.max-lifetime", "2000000"
        );
        
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source);
        
        DatabaseProperties dbProps = binder.bind("database", DatabaseProperties.class).orElse(new DatabaseProperties());
        
        assertEquals("jdbc:postgresql://localhost:5432/test_db", dbProps.url());
        assertEquals("test_user", dbProps.username());
        assertEquals("test_password", dbProps.password());
        assertEquals(15, dbProps.pool().maxSize());
        assertEquals(20000L, dbProps.pool().connectionTimeout());
        assertEquals(500000L, dbProps.pool().idleTimeout());
        assertEquals(2000000L, dbProps.pool().maxLifetime());
    }
    
    @Test
    void testDefaultDatabaseProperties() {
        var dbProps = new DatabaseProperties();
        
        assertEquals("", dbProps.url());
        assertEquals("", dbProps.username());
        assertEquals("", dbProps.password());
        assertNotNull(dbProps.pool());
    }
}