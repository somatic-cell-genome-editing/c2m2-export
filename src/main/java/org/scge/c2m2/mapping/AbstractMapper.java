package org.scge.c2m2.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Abstract base class for entity mappers providing common mapping functionality.
 * 
 * @param <T> The source entity type (SCGE format)
 * @param <U> The target entity type (C2M2 format)
 */
public abstract class AbstractMapper<T, U> implements EntityMapper<T, U> {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Class<T> sourceType;
    private final Class<U> targetType;
    
    @SuppressWarnings("unchecked")
    protected AbstractMapper() {
        Type superClass = getClass().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) superClass;
        this.sourceType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        this.targetType = (Class<U>) parameterizedType.getActualTypeArguments()[1];
    }
    
    @Override
    public Class<T> getSourceType() {
        return sourceType;
    }
    
    @Override
    public Class<U> getTargetType() {
        return targetType;
    }
    
    @Override
    public boolean canMap(T source) {
        if (source == null) {
            return false;
        }
        return sourceType.isAssignableFrom(source.getClass());
    }
    
    /**
     * Generates a unique identifier for an entity.
     * 
     * @param prefix Optional prefix for the ID
     * @return A unique identifier string
     */
    protected String generateUniqueId(String prefix) {
        String uuid = UUID.randomUUID().toString();
        return prefix != null ? prefix + "-" + uuid : uuid;
    }
    
    /**
     * Generates a unique identifier for an entity based on its type and source ID.
     * 
     * @param entityType The type of entity (e.g., "project", "subject")
     * @param sourceId The source entity's ID
     * @return A unique identifier string
     */
    protected String generateMappedId(String entityType, Object sourceId) {
        if (sourceId == null) {
            return generateUniqueId(entityType);
        }
        return entityType + "-" + sourceId.toString();
    }
    
    /**
     * Safely converts a string to a non-null string, handling null values.
     * 
     * @param value The string value to convert
     * @param defaultValue The default value to use if the input is null or empty
     * @return The converted string
     */
    protected String safeString(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }
    
    /**
     * Safely converts a string to a non-null string.
     * 
     * @param value The string value to convert
     * @return The converted string or empty string if null
     */
    protected String safeString(String value) {
        return safeString(value, "");
    }
    
    /**
     * Formats a timestamp to ISO 8601 format for C2M2 compliance.
     * 
     * @param timestamp The timestamp to format
     * @return ISO 8601 formatted string or null if input is null
     */
    protected String formatTimestamp(Instant timestamp) {
        if (timestamp == null) {
            return null;
        }
        return DateTimeFormatter.ISO_INSTANT.format(timestamp);
    }
    
    /**
     * Gets the current timestamp in ISO 8601 format.
     * 
     * @return Current timestamp as ISO 8601 string
     */
    protected String getCurrentTimestamp() {
        return formatTimestamp(Instant.now());
    }
    
    /**
     * Logs a mapping operation start.
     * 
     * @param source The source entity being mapped
     */
    protected void logMappingStart(T source) {
        logger.debug("Starting mapping of {} to {}", 
                    sourceType.getSimpleName(), 
                    targetType.getSimpleName());
    }
    
    /**
     * Logs a successful mapping operation.
     * 
     * @param source The source entity that was mapped
     * @param target The resulting target entity
     */
    protected void logMappingSuccess(T source, U target) {
        logger.debug("Successfully mapped {} to {}", 
                    sourceType.getSimpleName(), 
                    targetType.getSimpleName());
    }
    
    /**
     * Logs a failed mapping operation.
     * 
     * @param source The source entity that failed to map
     * @param error The error that occurred
     */
    protected void logMappingError(T source, Exception error) {
        logger.error("Failed to map {} to {}: {}", 
                    sourceType.getSimpleName(), 
                    targetType.getSimpleName(), 
                    error.getMessage(), error);
    }
}