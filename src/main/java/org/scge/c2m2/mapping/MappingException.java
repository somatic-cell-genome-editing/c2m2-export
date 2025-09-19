package org.scge.c2m2.mapping;

/**
 * Exception thrown when entity mapping operations fail.
 */
public class MappingException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new MappingException with the specified detail message.
     * 
     * @param message the detail message
     */
    public MappingException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new MappingException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructs a new MappingException with the specified cause.
     * 
     * @param cause the cause of the exception
     */
    public MappingException(Throwable cause) {
        super(cause);
    }
}