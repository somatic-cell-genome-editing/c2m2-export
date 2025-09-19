package org.scge.c2m2.identifiers;

/**
 * Strategy interface for generating identifiers for C2M2 entities.
 * Different strategies can be used for different entity types or requirements.
 */
public interface IdentifierStrategy {
    
    /**
     * Generates a local identifier for an entity.
     * 
     * @param entityType The type of entity (e.g., "project", "subject", "biosample")
     * @param sourceId The source system identifier
     * @param additionalContext Additional context for ID generation
     * @return Generated local identifier
     */
    String generateLocalId(String entityType, Object sourceId, Object... additionalContext);
    
    /**
     * Generates a persistent identifier for an entity.
     * 
     * @param namespace The ID namespace
     * @param entityType The type of entity
     * @param sourceId The source system identifier
     * @param additionalContext Additional context for ID generation
     * @return Generated persistent identifier
     */
    String generatePersistentId(String namespace, String entityType, Object sourceId, Object... additionalContext);
    
    /**
     * Validates that an identifier follows the strategy's rules.
     * 
     * @param identifier The identifier to validate
     * @param entityType The expected entity type
     * @return true if the identifier is valid
     */
    boolean isValidIdentifier(String identifier, String entityType);
    
    /**
     * Gets the strategy name for logging and configuration.
     * 
     * @return Strategy name
     */
    String getStrategyName();
    
    /**
     * Checks if this strategy supports the given entity type.
     * 
     * @param entityType The entity type to check
     * @return true if supported
     */
    boolean supportsEntityType(String entityType);
}