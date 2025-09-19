package org.scge.c2m2.database;

/**
 * Custom exception for database connection-related errors.
 * This exception is thrown when database connection operations fail.
 */
public class DatabaseConnectionException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new DatabaseConnectionException with the specified detail message.
     * 
     * @param message the detail message
     */
    public DatabaseConnectionException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new DatabaseConnectionException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DatabaseConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new DatabaseConnectionException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public DatabaseConnectionException(Throwable cause) {
        super(cause);
    }
}