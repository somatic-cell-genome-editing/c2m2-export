package org.scge.c2m2.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates the mapping process for converting SCGE entities to C2M2 entities.
 * Manages batch processing, error handling, and mapping statistics.
 */
@Service
public class MappingOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(MappingOrchestrator.class);
    
    private final MapperFactory mapperFactory;
    private final Map<String, MappingStatistics> statistics = new ConcurrentHashMap<>();
    
    @Autowired
    public MappingOrchestrator(MapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
        logger.info("MappingOrchestrator initialized");
    }
    
    /**
     * Maps a single entity from SCGE format to C2M2 format.
     * 
     * @param source The source entity
     * @param <T> The source type
     * @param <U> The target type
     * @return The mapped entity, or null if mapping failed
     */
    public <T, U> U mapEntity(T source) {
        if (source == null) {
            return null;
        }
        
        EntityMapper<T, U> mapper = mapperFactory.getMapperFor(source);
        if (mapper == null) {
            logger.warn("No mapper found for entity type: {}", source.getClass().getSimpleName());
            recordMappingAttempt(source.getClass().getSimpleName(), false, "No mapper available");
            return null;
        }
        
        try {
            U result = mapper.map(source);
            recordMappingAttempt(source.getClass().getSimpleName(), true, null);
            return result;
        } catch (Exception e) {
            logger.error("Failed to map entity of type {}: {}", 
                        source.getClass().getSimpleName(), e.getMessage(), e);
            recordMappingAttempt(source.getClass().getSimpleName(), false, e.getMessage());
            return null;
        }
    }
    
    /**
     * Maps a collection of entities from SCGE format to C2M2 format.
     * 
     * @param sources The source entities
     * @param <T> The source type
     * @param <U> The target type
     * @return List of successfully mapped entities
     */
    public <T, U> List<U> mapEntities(Collection<T> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.info("Starting batch mapping of {} entities", sources.size());
        
        List<U> results = sources.stream()
                .map(this::<T, U>mapEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        logger.info("Batch mapping completed. Mapped {}/{} entities successfully", 
                   results.size(), sources.size());
        
        return results;
    }
    
    /**
     * Maps entities and groups them by type.
     * 
     * @param sources The source entities
     * @return Map of target type names to lists of mapped entities
     */
    public Map<String, List<Object>> mapAndGroupByType(Collection<Object> sources) {
        if (sources == null || sources.isEmpty()) {
            return new HashMap<>();
        }
        
        logger.info("Starting grouped mapping of {} entities", sources.size());
        
        Map<String, List<Object>> groupedResults = new HashMap<>();
        
        for (Object source : sources) {
            Object mapped = mapEntity(source);
            if (mapped != null) {
                String targetType = mapped.getClass().getSimpleName();
                groupedResults.computeIfAbsent(targetType, k -> new ArrayList<>()).add(mapped);
            }
        }
        
        logger.info("Grouped mapping completed. Generated {} target types", groupedResults.size());
        
        return groupedResults;
    }
    
    /**
     * Maps entities with a custom result collector.
     * 
     * @param sources The source entities
     * @param collector Function to collect and transform results
     * @param <T> The source type
     * @param <U> The target type
     * @param <R> The result type
     * @return The collected result
     */
    public <T, U, R> R mapEntities(Collection<T> sources, Function<List<U>, R> collector) {
        List<U> mappedEntities = mapEntities(sources);
        return collector.apply(mappedEntities);
    }
    
    /**
     * Validates that all entities in the collection can be mapped.
     * 
     * @param sources The source entities to validate
     * @return ValidationResult containing validation details
     */
    public ValidationResult validateMappability(Collection<Object> sources) {
        if (sources == null || sources.isEmpty()) {
            return new ValidationResult(true, 0, 0, new ArrayList<>());
        }
        
        int totalEntities = sources.size();
        int mappableEntities = 0;
        List<String> unmappableTypes = new ArrayList<>();
        
        Map<String, Integer> typeCounts = new HashMap<>();
        
        for (Object source : sources) {
            String typeName = source.getClass().getSimpleName();
            typeCounts.merge(typeName, 1, Integer::sum);
            
            if (mapperFactory.canMap(source)) {
                mappableEntities++;
            } else {
                if (!unmappableTypes.contains(typeName)) {
                    unmappableTypes.add(typeName);
                }
            }
        }
        
        boolean allMappable = unmappableTypes.isEmpty();
        
        logger.info("Validation completed: {}/{} entities mappable, {} unmappable types", 
                   mappableEntities, totalEntities, unmappableTypes.size());
        
        return new ValidationResult(allMappable, totalEntities, mappableEntities, unmappableTypes);
    }
    
    /**
     * Records a mapping attempt for statistics.
     */
    private void recordMappingAttempt(String entityType, boolean success, String errorMessage) {
        statistics.computeIfAbsent(entityType, k -> new MappingStatistics())
                 .recordAttempt(success, errorMessage);
    }
    
    /**
     * Gets mapping statistics for all entity types.
     * 
     * @return Map of entity types to their mapping statistics
     */
    public Map<String, MappingStatistics> getStatistics() {
        return new HashMap<>(statistics);
    }
    
    /**
     * Clears all mapping statistics.
     */
    public void clearStatistics() {
        statistics.clear();
        logger.info("Mapping statistics cleared");
    }
    
    /**
     * Gets a summary of mapping capabilities.
     * 
     * @return Map of information about available mappers
     */
    public Map<String, Object> getMappingCapabilities() {
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("supportedSourceTypes", mapperFactory.getSupportedSourceTypes()
                .stream().map(Class::getSimpleName).collect(Collectors.toList()));
        capabilities.put("availableMappers", mapperFactory.getMappingInfo());
        capabilities.put("totalMappers", mapperFactory.getAllMappers().size());
        return capabilities;
    }
    
    /**
     * Represents the result of mapping validation.
     */
    public record ValidationResult(
        boolean allMappable,
        int totalEntities,
        int mappableEntities,
        List<String> unmappableTypes
    ) {
        public double getMappabilityPercentage() {
            return totalEntities > 0 ? (mappableEntities * 100.0) / totalEntities : 0.0;
        }
    }
    
    /**
     * Tracks mapping statistics for a specific entity type.
     */
    public static class MappingStatistics {
        private int successCount = 0;
        private int failureCount = 0;
        private final List<String> recentErrors = new ArrayList<>();
        private static final int MAX_RECENT_ERRORS = 10;
        
        public synchronized void recordAttempt(boolean success, String errorMessage) {
            if (success) {
                successCount++;
            } else {
                failureCount++;
                if (errorMessage != null) {
                    recentErrors.add(errorMessage);
                    if (recentErrors.size() > MAX_RECENT_ERRORS) {
                        recentErrors.remove(0);
                    }
                }
            }
        }
        
        public int getSuccessCount() { return successCount; }
        public int getFailureCount() { return failureCount; }
        public int getTotalAttempts() { return successCount + failureCount; }
        public double getSuccessRate() { 
            int total = getTotalAttempts();
            return total > 0 ? (successCount * 100.0) / total : 0.0; 
        }
        public List<String> getRecentErrors() { return new ArrayList<>(recentErrors); }
    }
}