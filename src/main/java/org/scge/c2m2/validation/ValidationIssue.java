package org.scge.c2m2.validation;

import java.util.Map;

/**
 * Represents a single validation issue (error, warning, or info).
 * Contains detailed information about what was validated and what the issue is.
 */
public record ValidationIssue(
    ValidationLevel level,
    String code,
    String message,
    String entityType,
    String entityId,
    String field,
    Map<String, Object> context
) {
    
    /**
     * Creates an error validation issue.
     */
    public static ValidationIssue error(String code, String message, String entityType, String entityId, String field) {
        return new ValidationIssue(ValidationLevel.ERROR, code, message, entityType, entityId, field, null);
    }
    
    /**
     * Creates a warning validation issue.
     */
    public static ValidationIssue warning(String code, String message, String entityType, String entityId, String field) {
        return new ValidationIssue(ValidationLevel.WARNING, code, message, entityType, entityId, field, null);
    }
    
    /**
     * Creates an info validation issue.
     */
    public static ValidationIssue info(String code, String message, String entityType, String entityId, String field) {
        return new ValidationIssue(ValidationLevel.INFO, code, message, entityType, entityId, field, null);
    }
    
    /**
     * Creates an error validation issue with context.
     */
    public static ValidationIssue error(String code, String message, String entityType, String entityId, 
                                       String field, Map<String, Object> context) {
        return new ValidationIssue(ValidationLevel.ERROR, code, message, entityType, entityId, field, context);
    }
    
    /**
     * Gets a formatted display string for the issue.
     */
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(level).append("]");
        
        if (code != null) {
            sb.append(" ").append(code);
        }
        
        if (entityType != null || entityId != null) {
            sb.append(" (");
            if (entityType != null) {
                sb.append(entityType);
                if (entityId != null) {
                    sb.append(":").append(entityId);
                }
            } else if (entityId != null) {
                sb.append(entityId);
            }
            sb.append(")");
        }
        
        if (field != null) {
            sb.append(" [").append(field).append("]");
        }
        
        sb.append(": ").append(message);
        
        return sb.toString();
    }
    
    /**
     * Checks if this is an error-level issue.
     */
    public boolean isError() {
        return level == ValidationLevel.ERROR;
    }
    
    /**
     * Checks if this is a warning-level issue.
     */
    public boolean isWarning() {
        return level == ValidationLevel.WARNING;
    }
    
    /**
     * Checks if this is an info-level issue.
     */
    public boolean isInfo() {
        return level == ValidationLevel.INFO;
    }
    
    /**
     * Gets context value by key.
     */
    public Object getContext(String key) {
        return context != null ? context.get(key) : null;
    }
    
    /**
     * Checks if context contains a key.
     */
    public boolean hasContext(String key) {
        return context != null && context.containsKey(key);
    }
    
    @Override
    public String toString() {
        return getDisplayString();
    }
}