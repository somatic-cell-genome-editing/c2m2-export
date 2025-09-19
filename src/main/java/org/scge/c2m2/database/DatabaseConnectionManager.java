package org.scge.c2m2.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service for managing database connections.
 * Provides methods for obtaining and managing database connections
 * from the configured HikariCP connection pool.
 */
@Service
public class DatabaseConnectionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    
    private final DataSource dataSource;
    
    @Autowired
    public DatabaseConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        logger.info("DatabaseConnectionManager initialized with DataSource: {}", 
                   dataSource.getClass().getSimpleName());
    }
    
    /**
     * Obtains a database connection from the connection pool.
     * 
     * @return Database connection
     * @throws DatabaseConnectionException if connection cannot be obtained
     */
    public Connection getConnection() throws DatabaseConnectionException {
        try {
            logger.debug("Obtaining database connection from pool");
            Connection connection = dataSource.getConnection();
            logger.debug("Successfully obtained database connection");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to obtain database connection", e);
            throw new DatabaseConnectionException("Failed to obtain database connection", e);
        }
    }
    
    /**
     * Safely closes a database connection.
     * This method logs any errors but does not throw exceptions.
     * 
     * @param connection The connection to close (can be null)
     */
    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                logger.debug("Closing database connection");
                connection.close();
                logger.debug("Database connection closed successfully");
            } catch (SQLException e) {
                logger.warn("Error closing database connection", e);
            }
        }
    }
    
    /**
     * Checks if the database connection is healthy by executing a test query.
     * 
     * @return true if the database is accessible, false otherwise
     */
    public boolean isHealthy() {
        try (Connection connection = getConnection()) {
            // Execute a simple test query
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1");
            boolean healthy = resultSet.next() && resultSet.getInt(1) == 1;
            
            logger.debug("Database health check: {}", healthy ? "PASSED" : "FAILED");
            return healthy;
            
        } catch (Exception e) {
            logger.warn("Database health check failed", e);
            return false;
        }
    }
    
    /**
     * Executes a function with a managed database connection.
     * The connection is automatically closed after the function completes.
     * 
     * @param function The function to execute with the connection
     * @param <T> The return type of the function
     * @return The result of the function
     * @throws DatabaseConnectionException if connection operations fail
     */
    public <T> T withConnection(DatabaseFunction<T> function) throws DatabaseConnectionException {
        Connection connection = null;
        try {
            connection = getConnection();
            logger.debug("Executing function with managed connection");
            T result = function.apply(connection);
            logger.debug("Function executed successfully");
            return result;
        } catch (DatabaseConnectionException e) {
            logger.error("Database connection error executing function", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error executing function with database connection", e);
            throw new DatabaseConnectionException("Error executing database operation", e);
        } finally {
            closeConnection(connection);
        }
    }
    
    /**
     * Functional interface for operations that use a database connection.
     * 
     * @param <T> The return type of the operation
     */
    @FunctionalInterface
    public interface DatabaseFunction<T> {
        T apply(Connection connection) throws Exception;
    }
}