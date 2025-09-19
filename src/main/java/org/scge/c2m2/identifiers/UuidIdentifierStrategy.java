package org.scge.c2m2.identifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * UUID-based identifier strategy for C2M2 entities.
 * Generates globally unique identifiers using UUIDs, suitable for distributed systems.
 */
@Component
public class UuidIdentifierStrategy implements IdentifierStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(UuidIdentifierStrategy.class);
    
    private static final String STRATEGY_NAME = "uuid";
    
    // All entity types are supported with UUID strategy
    private static final Set<String> SUPPORTED_ENTITY_TYPES = Set.of(
        "project", "subject", "biosample", "file", "collection",
        "protocol", "experiment", "analysis", "image", "model"
    );
    
    // Pattern for UUID format
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );
    
    // Pattern for persistent ID with UUID
    private static final Pattern PERSISTENT_UUID_PATTERN = Pattern.compile(
        "^[A-Z0-9._-]+:[A-Z]+:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
    );
    
    @Override
    public String generateLocalId(String entityType, Object sourceId, Object... additionalContext) {
        if (entityType == null) {
            throw new IllegalArgumentException("Entity type cannot be null");
        }
        
        if (!supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        // Generate a deterministic UUID based on entity type and source ID
        String seedString = entityType + ":" + (sourceId != null ? sourceId.toString() : "");
        UUID uuid = UUID.nameUUIDFromBytes(seedString.getBytes());
        
        String localId = uuid.toString();
        
        logger.debug("Generated UUID local ID: {} for entity type: {}, source ID: {}", 
                    localId, entityType, sourceId);
        
        return localId;
    }
    
    @Override
    public String generatePersistentId(String namespace, String entityType, Object sourceId, Object... additionalContext) {
        if (namespace == null || entityType == null) {
            throw new IllegalArgumentException("Namespace and entity type cannot be null");
        }
        
        if (!supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        String sanitizedNamespace = sanitizeNamespace(namespace);
        String sanitizedEntityType = entityType.toUpperCase();
        
        // Generate a deterministic UUID
        String seedString = sanitizedNamespace + ":" + sanitizedEntityType + ":" + 
                           (sourceId != null ? sourceId.toString() : "");
        UUID uuid = UUID.nameUUIDFromBytes(seedString.getBytes());
        
        String persistentId = sanitizedNamespace + ":" + sanitizedEntityType + ":" + uuid.toString();
        
        logger.debug("Generated UUID persistent ID: {} for namespace: {}, entity type: {}, source ID: {}", 
                    persistentId, namespace, entityType, sourceId);
        
        return persistentId;
    }
    
    @Override
    public boolean isValidIdentifier(String identifier, String entityType) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return false;
        }
        
        // Check if it's a plain UUID (local ID)
        if (UUID_PATTERN.matcher(identifier.toLowerCase()).matches()) {
            return true;
        }
        
        // Check if it's a persistent ID with UUID
        if (PERSISTENT_UUID_PATTERN.matcher(identifier).matches()) {
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
     * Sanitizes namespace for persistent IDs.
     */
    private String sanitizeNamespace(String namespace) {
        if (namespace == null) {
            return "DEFAULT";
        }
        
        return namespace.trim()
                       .toUpperCase()
                       .replaceAll("[^A-Z0-9._-]", "")
                       .replaceAll("^[.-]|[.-]$", "");
    }
    
    /**
     * Generates a random UUID (for cases where deterministic UUIDs are not needed).
     */
    public String generateRandomLocalId(String entityType) {
        if (!supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        UUID uuid = UUID.randomUUID();
        String localId = uuid.toString();
        
        logger.debug("Generated random UUID local ID: {} for entity type: {}", localId, entityType);
        
        return localId;
    }
    
    /**
     * Generates a random persistent ID.
     */
    public String generateRandomPersistentId(String namespace, String entityType) {
        if (namespace == null || entityType == null) {
            throw new IllegalArgumentException("Namespace and entity type cannot be null");
        }
        
        if (!supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        String sanitizedNamespace = sanitizeNamespace(namespace);
        String sanitizedEntityType = entityType.toUpperCase();
        UUID uuid = UUID.randomUUID();
        
        String persistentId = sanitizedNamespace + ":" + sanitizedEntityType + ":" + uuid.toString();
        
        logger.debug("Generated random UUID persistent ID: {} for namespace: {}, entity type: {}", 
                    persistentId, namespace, entityType);
        
        return persistentId;
    }
}