package org.scge.c2m2.database;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for database connection using TestContainers.
 */
@SpringBootTest
@Testcontainers
class DatabaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15.3")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("database.url", postgres::getJdbcUrl);
        registry.add("database.username", postgres::getUsername);
        registry.add("database.password", postgres::getPassword);
    }
    
    @Autowired
    private DatabaseConnectionManager connectionManager;
    
    @Autowired
    private DatabaseService databaseService;
    
    @Test
    void testDatabaseConnection() throws Exception {
        // Test basic connection
        try (Connection connection = connectionManager.getConnection()) {
            assertNotNull(connection);
            assertTrue(connection.isValid(5));
            assertFalse(connection.isClosed());
        }
    }
    
    @Test
    void testHealthCheck() {
        assertTrue(connectionManager.isHealthy());
        assertTrue(databaseService.isHealthy());
    }
    
    @Test
    void testWithConnectionFunction() throws Exception {
        String result = connectionManager.withConnection(connection -> {
            assertNotNull(connection);
            assertTrue(connection.isValid(5));
            return "success";
        });
        
        assertEquals("success", result);
    }
    
    @Test
    void testDatabaseServiceQuery() throws Exception {
        // Test simple query
        Integer result = databaseService.executeQuery(
            "SELECT 42 as test_value",
            resultSet -> {
                assertTrue(resultSet.next());
                return resultSet.getInt("test_value");
            }
        );
        
        assertEquals(42, result);
    }
    
    @Test
    void testDatabaseServiceQueryForList() throws Exception {
        // Create a test table and insert some data
        databaseService.executeUpdate(
            "CREATE TEMPORARY TABLE test_table (id SERIAL PRIMARY KEY, name VARCHAR(50))",
            null
        );
        
        databaseService.executeUpdate(
            "INSERT INTO test_table (name) VALUES (?), (?), (?)",
            new Object[]{"Alice", "Bob", "Charlie"}
        );
        
        // Query the data
        List<String> names = databaseService.executeQueryForList(
            "SELECT name FROM test_table ORDER BY name",
            resultSet -> resultSet.getString("name")
        );
        
        assertEquals(3, names.size());
        assertEquals("Alice", names.get(0));
        assertEquals("Bob", names.get(1));
        assertEquals("Charlie", names.get(2));
    }
    
    @Test
    void testDatabaseServiceUpdate() throws Exception {
        // Create a test table
        databaseService.executeUpdate(
            "CREATE TEMPORARY TABLE test_update (id SERIAL PRIMARY KEY, value INTEGER)",
            null
        );
        
        // Insert data
        int insertCount = databaseService.executeUpdate(
            "INSERT INTO test_update (value) VALUES (?), (?)",
            new Object[]{100, 200}
        );
        
        assertEquals(2, insertCount);
        
        // Update data
        int updateCount = databaseService.executeUpdate(
            "UPDATE test_update SET value = value + 10 WHERE value > ?",
            new Object[]{150}
        );
        
        assertEquals(1, updateCount);
        
        // Verify the update
        Integer updatedValue = databaseService.executeQuery(
            "SELECT value FROM test_update WHERE value > 200",
            resultSet -> {
                if (resultSet.next()) {
                    return resultSet.getInt("value");
                }
                return null;
            }
        );
        
        assertEquals(210, updatedValue);
    }
    
    @Test
    void testParameterizedQuery() throws Exception {
        // Test query with parameters
        Integer result = databaseService.executeQuery(
            "SELECT ? + ? as sum",
            new Object[]{10, 20},
            resultSet -> {
                assertTrue(resultSet.next());
                return resultSet.getInt("sum");
            }
        );
        
        assertEquals(30, result);
    }
}