package org.scge.c2m2.database;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatabaseConnectionManager.
 */
@ExtendWith(MockitoExtension.class)
class DatabaseConnectionManagerTest {
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
    private DatabaseConnectionManager connectionManager;
    
    @BeforeEach
    void setUp() {
        connectionManager = new DatabaseConnectionManager(dataSource);
    }
    
    @Test
    void testGetConnection_Success() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        
        Connection result = connectionManager.getConnection();
        
        assertSame(connection, result);
        verify(dataSource).getConnection();
    }
    
    @Test
    void testGetConnection_ThrowsSQLException() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        assertThrows(DatabaseConnectionException.class, () -> {
            connectionManager.getConnection();
        });
    }
    
    @Test
    void testCloseConnection_Success() throws Exception {
        connectionManager.closeConnection(connection);
        
        verify(connection).close();
    }
    
    @Test
    void testCloseConnection_WithException() throws Exception {
        doThrow(new SQLException("Close failed")).when(connection).close();
        
        // Should not throw exception, just log warning
        assertDoesNotThrow(() -> connectionManager.closeConnection(connection));
        verify(connection).close();
    }
    
    @Test
    void testCloseConnection_WithNull() {
        // Should not throw exception with null connection
        assertDoesNotThrow(() -> connectionManager.closeConnection(null));
    }
    
    @Test
    void testIsHealthy_Success() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT 1")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        
        boolean result = connectionManager.isHealthy();
        
        assertTrue(result);
        verify(connection).createStatement();
        verify(statement).executeQuery("SELECT 1");
        verify(resultSet).next();
        verify(resultSet).getInt(1);
    }
    
    @Test
    void testIsHealthy_Failure() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        boolean result = connectionManager.isHealthy();
        
        assertFalse(result);
    }
    
    @Test
    void testWithConnection_Success() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        
        String expectedResult = "test result";
        DatabaseConnectionManager.DatabaseFunction<String> function = conn -> {
            assertSame(connection, conn);
            return expectedResult;
        };
        
        String result = connectionManager.withConnection(function);
        
        assertEquals(expectedResult, result);
        verify(connection).close();
    }
    
    @Test
    void testWithConnection_FunctionThrowsException() throws Exception {
        when(dataSource.getConnection()).thenReturn(connection);
        
        DatabaseConnectionManager.DatabaseFunction<String> function = conn -> {
            throw new RuntimeException("Function failed");
        };
        
        assertThrows(DatabaseConnectionException.class, () -> {
            connectionManager.withConnection(function);
        });
        
        verify(connection).close();
    }
    
    @Test
    void testWithConnection_ConnectionFailure() throws Exception {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));
        
        DatabaseConnectionManager.DatabaseFunction<String> function = conn -> "test";
        
        assertThrows(DatabaseConnectionException.class, () -> {
            connectionManager.withConnection(function);
        });
    }
}