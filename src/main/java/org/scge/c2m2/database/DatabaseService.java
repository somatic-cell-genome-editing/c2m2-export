package org.scge.c2m2.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * High-level database service for executing queries and managing database operations.
 * Provides common database operations with proper connection management.
 */
@Service
public class DatabaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    
    private final DatabaseConnectionManager connectionManager;
    
    @Autowired
    public DatabaseService(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        logger.info("DatabaseService initialized");
    }
    
    /**
     * Executes a query and processes the results using the provided handler.
     * 
     * @param sql The SQL query to execute
     * @param parameters Query parameters
     * @param resultHandler Function to process the ResultSet
     * @param <T> The return type
     * @return The result from the result handler
     * @throws DatabaseConnectionException if database operations fail
     */
    public <T> T executeQuery(String sql, Object[] parameters, 
                             ResultSetHandler<T> resultHandler) throws DatabaseConnectionException {
        
        logger.debug("Executing query: {}", sql);
        
        return connectionManager.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Set parameters
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        statement.setObject(i + 1, parameters[i]);
                    }
                }
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    T result = resultHandler.handle(resultSet);
                    logger.debug("Query executed successfully");
                    return result;
                }
                
            } catch (SQLException e) {
                logger.error("Error executing query: {}", sql, e);
                throw new RuntimeException("Query execution failed", e);
            }
        });
    }
    
    /**
     * Executes a query without parameters.
     * 
     * @param sql The SQL query to execute
     * @param resultHandler Function to process the ResultSet
     * @param <T> The return type
     * @return The result from the result handler
     * @throws DatabaseConnectionException if database operations fail
     */
    public <T> T executeQuery(String sql, ResultSetHandler<T> resultHandler) 
            throws DatabaseConnectionException {
        return executeQuery(sql, null, resultHandler);
    }
    
    /**
     * Executes a query and returns a list of results.
     * 
     * @param sql The SQL query to execute
     * @param parameters Query parameters
     * @param rowMapper Function to map each row to an object
     * @param <T> The type of objects in the result list
     * @return List of mapped objects
     * @throws DatabaseConnectionException if database operations fail
     */
    public <T> List<T> executeQueryForList(String sql, Object[] parameters, 
                                          RowMapper<T> rowMapper) throws DatabaseConnectionException {
        
        return executeQuery(sql, parameters, resultSet -> {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        });
    }
    
    /**
     * Executes a query and returns a list of results without parameters.
     * 
     * @param sql The SQL query to execute
     * @param rowMapper Function to map each row to an object
     * @param <T> The type of objects in the result list
     * @return List of mapped objects
     * @throws DatabaseConnectionException if database operations fail
     */
    public <T> List<T> executeQueryForList(String sql, RowMapper<T> rowMapper) 
            throws DatabaseConnectionException {
        return executeQueryForList(sql, null, rowMapper);
    }
    
    /**
     * Executes an update query (INSERT, UPDATE, DELETE).
     * 
     * @param sql The SQL statement to execute
     * @param parameters Statement parameters
     * @return The number of affected rows
     * @throws DatabaseConnectionException if database operations fail
     */
    public int executeUpdate(String sql, Object[] parameters) throws DatabaseConnectionException {
        
        logger.debug("Executing update: {}", sql);
        
        return connectionManager.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Set parameters
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        statement.setObject(i + 1, parameters[i]);
                    }
                }
                
                int affectedRows = statement.executeUpdate();
                logger.debug("Update executed successfully, affected rows: {}", affectedRows);
                return affectedRows;
                
            } catch (SQLException e) {
                logger.error("Error executing update: {}", sql, e);
                throw new RuntimeException("Update execution failed", e);
            }
        });
    }
    
    /**
     * Checks database connectivity and health.
     * 
     * @return true if database is accessible and healthy
     */
    public boolean isHealthy() {
        return connectionManager.isHealthy();
    }
    
    /**
     * Functional interface for handling ResultSet processing.
     * 
     * @param <T> The return type
     */
    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet resultSet) throws SQLException;
    }
    
    /**
     * Functional interface for mapping a single row to an object.
     * 
     * @param <T> The type of the mapped object
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T mapRow(ResultSet resultSet) throws SQLException;
    }
}