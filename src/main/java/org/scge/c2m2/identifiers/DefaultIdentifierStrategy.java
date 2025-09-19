package org.scge.c2m2.identifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Default identifier strategy for C2M2 entities.
 * Generates predictable, readable identifiers based on entity type and source ID.
 */
@Component
public class DefaultIdentifierStrategy implements IdentifierStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultIdentifierStrategy.class);
    
    private static final String STRATEGY_NAME = "default";
    
    // Supported entity types
    private static final Set<String> SUPPORTED_ENTITY_TYPES = Set.of(
        "project", "subject", "biosample", "file", "collection",
        "protocol", "experiment", "analysis", "image", "model"
    );
    
    // Pattern for valid local IDs (alphanumeric, hyphens, underscores)
    private static final Pattern LOCAL_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    
    // Pattern for valid persistent IDs (namespace:type:id format)
    private static final Pattern PERSISTENT_ID_PATTERN = Pattern.compile("^[A-Z0-9._-]+:[A-Z]+:[0-9]+$");
    
    @Override
    public String generateLocalId(String entityType, Object sourceId, Object... additionalContext) {
        if (entityType == null || sourceId == null) {
            throw new IllegalArgumentException("Entity type and source ID cannot be null");
        }
        
        if (!supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        String sanitizedEntityType = sanitizeForId(entityType);
        String sanitizedSourceId = sanitizeForId(sourceId.toString());
        
        String localId = sanitizedEntityType + "-" + sanitizedSourceId;
        
        logger.debug("Generated local ID: {} for entity type: {}, source ID: {}", 
                    localId, entityType, sourceId);
        
        return localId;
    }
    
    @Override
    public String generatePersistentId(String namespace, String entityType, Object sourceId, Object... additionalContext) {
        if (namespace == null || entityType == null || sourceId == null) {
            throw new IllegalArgumentException("Namespace, entity type, and source ID cannot be null");
        }
        
        if (!supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        String sanitizedNamespace = sanitizeForPersistentId(namespace);
        String sanitizedEntityType = entityType.toUpperCase();
        String sanitizedSourceId = sourceId.toString();
        
        // Ensure source ID is numeric for persistent IDs
        if (!sanitizedSourceId.matches("\\d+")) {
            // If not numeric, generate a hash-based numeric ID
            sanitizedSourceId = String.valueOf(Math.abs(sanitizedSourceId.hashCode()));
        }
        
        String persistentId = sanitizedNamespace + ":" + sanitizedEntityType + ":" + sanitizedSourceId;
        
        logger.debug("Generated persistent ID: {} for namespace: {}, entity type: {}, source ID: {}", 
                    persistentId, namespace, entityType, sourceId);
        
        return persistentId;
    }
    
    @Override
    public boolean isValidIdentifier(String identifier, String entityType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        // Check if it looks like a local ID
        if (LOCAL_ID_PATTERN.matcher(identifier).matches()) {
            return identifier.startsWith(entityType + "-");
        }
        
        // Check if it looks like a persistent ID
        if (PERSISTENT_ID_PATTERN.matcher(identifier).matches()) {
            String[] parts = identifier.split(":");
            return parts.length == 3 && parts[1].equalsIgnoreCase(entityType);
        }
        
        return false;
    }
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public boolean supportsEntityType(String entityType) {
        return entityType != null && SUPPORTED_ENTITY_TYPES.contains(entityType.toLowerCase());
    }
    
    /**
     * Sanitizes a string for use in local IDs.
     */
    private String sanitizeForId(String input) {
        if (input == null) {
            return "";
        }
        
        return input.trim()
                   .toLowerCase()
                   .replaceAll("[^a-zA-Z0-9_-]", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
    
    /**
     * Sanitizes a string for use in persistent ID namespaces.
     */
    private String sanitizeForPersistentId(String input) {
        if (input == null) {
            return "";
        }
        
        return input.trim()
                   .toUpperCase()
                   .replaceAll("[^A-Z0-9._-]", "")
                   .replaceAll("^[.-]|[.-]$", "");
    }
}