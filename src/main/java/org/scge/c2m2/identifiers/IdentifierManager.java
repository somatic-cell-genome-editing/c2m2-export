package org.scge.c2m2.identifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for C2M2 entity identifiers.
 * Coordinates identifier generation, validation, and tracking across the system.
 */
@Service
public class IdentifierManager {
    
    private static final Logger logger = LoggerFactory.getLogger(IdentifierManager.class);
    
    @Value("${c2m2.identifiers.default-namespace:scge.org}")
    private String defaultNamespace;
    
    @Value("${c2m2.identifiers.default-strategy:default}")
    private String defaultStrategyName;
    
    private final Map<String, IdentifierStrategy> strategies;
    private final Map<String, Set<String>> generatedIds = new ConcurrentHashMap<>();
    private final Map<String, String> entityTypeStrategies = new ConcurrentHashMap<>();
    
    @Autowired
    public IdentifierManager(List<IdentifierStrategy> availableStrategies) {
        this.strategies = new HashMap<>();
        
        // Register all available strategies
        for (IdentifierStrategy strategy : availableStrategies) {
            strategies.put(strategy.getStrategyName(), strategy);
            logger.info("Registered identifier strategy: {}", strategy.getStrategyName());
        }
        
        logger.info("IdentifierManager initialized with {} strategies", strategies.size());
    }
    
    /**
     * Generates a local identifier for an entity using the default strategy.
     */
    public String generateLocalId(String entityType, Object sourceId, Object... additionalContext) {
        return generateLocalId(entityType, sourceId, defaultStrategyName, additionalContext);
    }
    
    /**
     * Generates a local identifier for an entity using a specific strategy.
     */
    public String generateLocalId(String entityType, Object sourceId, String strategyName, Object... additionalContext) {
        IdentifierStrategy strategy = getStrategy(entityType, strategyName);
        String localId = strategy.generateLocalId(entityType, sourceId, additionalContext);
        
        // Track generated ID for uniqueness checking
        trackGeneratedId(entityType + "_local", localId);
        
        return localId;
    }
    
    /**
     * Generates a persistent identifier for an entity using the default namespace and strategy.
     */
    public String generatePersistentId(String entityType, Object sourceId, Object... additionalContext) {
        return generatePersistentId(defaultNamespace, entityType, sourceId, defaultStrategyName, additionalContext);
    }
    
    /**
     * Generates a persistent identifier for an entity using a specific namespace and strategy.
     */
    public String generatePersistentId(String namespace, String entityType, Object sourceId, String strategyName, Object... additionalContext) {
        IdentifierStrategy strategy = getStrategy(entityType, strategyName);
        String persistentId = strategy.generatePersistentId(namespace, entityType, sourceId, additionalContext);
        
        // Track generated ID for uniqueness checking
        trackGeneratedId(entityType + "_persistent", persistentId);
        
        return persistentId;
    }
    
    /**
     * Validates an identifier for a specific entity type.
     */
    public boolean isValidIdentifier(String identifier, String entityType) {
        return isValidIdentifier(identifier, entityType, defaultStrategyName);
    }
    
    /**
     * Validates an identifier for a specific entity type using a specific strategy.
     */
    public boolean isValidIdentifier(String identifier, String entityType, String strategyName) {
        IdentifierStrategy strategy = getStrategy(entityType, strategyName);
        return strategy.isValidIdentifier(identifier, entityType);
    }
    
    /**
     * Checks if an identifier has already been generated.
     */
    public boolean isIdentifierUsed(String entityType, String identifier, boolean isPersistent) {
        String key = entityType + (isPersistent ? "_persistent" : "_local");
        Set<String> usedIds = generatedIds.get(key);
        return usedIds != null && usedIds.contains(identifier);
    }
    
    /**
     * Reserves an identifier to prevent duplication.
     */
    public void reserveIdentifier(String entityType, String identifier, boolean isPersistent) {
        String key = entityType + (isPersistent ? "_persistent" : "_local");
        trackGeneratedId(key, identifier);
        logger.debug("Reserved identifier: {} for entity type: {}", identifier, entityType);
    }
    
    /**
     * Gets all identifiers generated for a specific entity type.
     */
    public Set<String> getGeneratedIdentifiers(String entityType, boolean isPersistent) {
        String key = entityType + (isPersistent ? "_persistent" : "_local");
        return new HashSet<>(generatedIds.getOrDefault(key, Collections.emptySet()));
    }
    
    /**
     * Sets the preferred strategy for a specific entity type.
     */
    public void setEntityTypeStrategy(String entityType, String strategyName) {
        if (!strategies.containsKey(strategyName)) {
            throw new IllegalArgumentException("Unknown strategy: " + strategyName);
        }
        
        IdentifierStrategy strategy = strategies.get(strategyName);
        if (!strategy.supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Strategy " + strategyName + " does not support entity type: " + entityType);
        }
        
        entityTypeStrategies.put(entityType, strategyName);
        logger.info("Set strategy {} for entity type {}", strategyName, entityType);
    }
    
    /**
     * Gets the strategy for a specific entity type.
     */
    private IdentifierStrategy getStrategy(String entityType, String strategyName) {
        // Use entity-specific strategy if configured
        String effectiveStrategyName = entityTypeStrategies.getOrDefault(entityType, strategyName);
        
        IdentifierStrategy strategy = strategies.get(effectiveStrategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown identifier strategy: " + effectiveStrategyName);
        }
        
        if (!strategy.supportsEntityType(entityType)) {
            throw new IllegalArgumentException("Strategy " + effectiveStrategyName + " does not support entity type: " + entityType);
        }
        
        return strategy;
    }
    
    /**
     * Tracks a generated identifier to prevent duplicates.
     */
    private void trackGeneratedId(String key, String identifier) {
        generatedIds.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet()).add(identifier);
    }
    
    /**
     * Gets all available identifier strategies.
     */
    public Set<String> getAvailableStrategies() {
        return new HashSet<>(strategies.keySet());
    }
    
    /**
     * Gets statistics about identifier generation.
     */
    public IdentifierStatistics getStatistics() {
        Map<String, Integer> countsByType = new HashMap<>();
        int totalGenerated = 0;
        
        for (Map.Entry<String, Set<String>> entry : generatedIds.entrySet()) {
            int count = entry.getValue().size();
            countsByType.put(entry.getKey(), count);
            totalGenerated += count;
        }
        
        return new IdentifierStatistics(
            totalGenerated,
            countsByType,
            new HashMap<>(entityTypeStrategies),
            getAvailableStrategies()
        );
    }
    
    /**
     * Clears all tracked identifiers (useful for testing).
     */
    public void clearTrackedIdentifiers() {
        generatedIds.clear();
        logger.info("Cleared all tracked identifiers");
    }
    
    /**
     * Gets the default namespace.
     */
    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    /**
     * Gets the default strategy name.
     */
    public String getDefaultStrategyName() {
        return defaultStrategyName;
    }
    
    /**
     * Statistics about identifier generation.
     */
    public record IdentifierStatistics(
        int totalGenerated,
        Map<String, Integer> countsByType,
        Map<String, String> entityTypeStrategies,
        Set<String> availableStrategies
    ) {
        public double getAveragePerType() {
            return countsByType.isEmpty() ? 0.0 : (double) totalGenerated / countsByType.size();
        }
        
        public String getMostUsedType() {
            return countsByType.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("none");
        }
    }
}