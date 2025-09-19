package org.scge.c2m2.validation;

import org.scge.c2m2.model.c2m2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central service for coordinating C2M2 data validation.
 * Manages multiple validators and provides comprehensive validation capabilities.
 */
@Service
public class ValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    
    private final Map<Class<?>, Validator<?>> validators;
    private final RelationshipValidator relationshipValidator;
    private final Map<String, ValidationResult> validationCache = new ConcurrentHashMap<>();
    
    @Autowired
    public ValidationService(List<Validator<?>> availableValidators, 
                           RelationshipValidator relationshipValidator) {
        this.relationshipValidator = relationshipValidator;
        this.validators = new HashMap<>();
        
        // Register all available validators
        for (Validator<?> validator : availableValidators) {
            validators.put(validator.getValidatedType(), validator);
            logger.info("Registered validator: {} for type {}", 
                       validator.getValidatorName(), validator.getValidatedType().getSimpleName());
        }
        
        logger.info("ValidationService initialized with {} validators", validators.size());
    }
    
    /**
     * Validates a single C2M2 entity.
     */
    @SuppressWarnings("unchecked")
    public <T> ValidationResult validateEntity(T entity) {
        if (entity == null) {
            ValidationResult result = new ValidationResult("Entity");
            result.addError("VAL_000", "Entity is null", "unknown", null, null);
            return result;
        }
        
        Class<?> entityClass = entity.getClass();
        Validator<T> validator = (Validator<T>) validators.get(entityClass);
        
        if (validator == null) {
            ValidationResult result = new ValidationResult("Entity");
            result.addWarning("VAL_W001", 
                "No validator found for entity type: " + entityClass.getSimpleName(), 
                entityClass.getSimpleName(), null, null);
            return result;
        }
        
        return validator.validate(entity);
    }
    
    /**
     * Validates a collection of entities of the same type.
     */
    public <T> ValidationResult validateEntities(Collection<T> entities, Class<T> entityType) {
        ValidationResult aggregatedResult = new ValidationResult("EntityCollection");
        
        if (entities == null || entities.isEmpty()) {
            aggregatedResult.addInfo("VAL_I001", "No entities to validate", 
                                   entityType.getSimpleName(), null, null);
            return aggregatedResult;
        }
        
        logger.info("Validating {} entities of type {}", entities.size(), entityType.getSimpleName());
        
        int validCount = 0;
        int invalidCount = 0;
        
        for (T entity : entities) {
            ValidationResult entityResult = validateEntity(entity);
            aggregatedResult.merge(entityResult);
            
            if (entityResult.isValid()) {
                validCount++;
            } else {
                invalidCount++;
            }
        }
        
        aggregatedResult.addMetadata("total_entities", entities.size());
        aggregatedResult.addMetadata("valid_entities", validCount);
        aggregatedResult.addMetadata("invalid_entities", invalidCount);
        aggregatedResult.addMetadata("entity_type", entityType.getSimpleName());
        
        logger.info("Entity validation completed: {}/{} valid", validCount, entities.size());
        
        return aggregatedResult;
    }
    
    /**
     * Validates a mixed collection of entities.
     */
    public ValidationResult validateMixedEntities(Collection<Object> entities) {
        ValidationResult aggregatedResult = new ValidationResult("MixedEntityCollection");
        
        if (entities == null || entities.isEmpty()) {
            aggregatedResult.addInfo("VAL_I002", "No entities to validate", "mixed", null, null);
            return aggregatedResult;
        }
        
        // Group entities by type
        Map<Class<?>, List<Object>> entitiesByType = new HashMap<>();
        for (Object entity : entities) {
            if (entity != null) {
                entitiesByType.computeIfAbsent(entity.getClass(), k -> new ArrayList<>()).add(entity);
            }
        }
        
        logger.info("Validating {} entities across {} types", entities.size(), entitiesByType.size());
        
        // Validate each type group
        for (Map.Entry<Class<?>, List<Object>> entry : entitiesByType.entrySet()) {
            Class<?> entityType = entry.getKey();
            List<Object> entitiesOfType = entry.getValue();
            
            for (Object entity : entitiesOfType) {
                ValidationResult entityResult = validateEntity(entity);
                aggregatedResult.merge(entityResult);
            }
        }
        
        aggregatedResult.addMetadata("total_entities", entities.size());
        aggregatedResult.addMetadata("entity_types", entitiesByType.keySet().stream()
                                   .map(Class::getSimpleName).toList());
        
        return aggregatedResult;
    }
    
    /**
     * Validates an entire C2M2 dataset including relationships.
     */
    public ValidationResult validateDataset(RelationshipValidator.C2M2Dataset dataset) {
        ValidationResult aggregatedResult = new ValidationResult("C2M2Dataset");
        
        if (dataset == null) {
            aggregatedResult.addError("VAL_001", "Dataset is null", "dataset", null, null);
            return aggregatedResult;
        }
        
        logger.info("Starting comprehensive dataset validation");
        
        // Validate individual entity collections
        ValidationResult projectsResult = validateEntities(dataset.getProjects(), C2M2Project.class);
        ValidationResult subjectsResult = validateEntities(dataset.getSubjects(), C2M2Subject.class);
        ValidationResult biosamplesResult = validateEntities(dataset.getBiosamples(), C2M2Biosample.class);
        ValidationResult filesResult = validateEntities(dataset.getFiles(), C2M2File.class);
        
        // Merge entity validation results
        aggregatedResult.merge(projectsResult);
        aggregatedResult.merge(subjectsResult);
        aggregatedResult.merge(biosamplesResult);
        aggregatedResult.merge(filesResult);
        
        // Validate relationships
        ValidationResult relationshipResult = relationshipValidator.validateDatasetRelationships(dataset);
        aggregatedResult.merge(relationshipResult);
        
        // Add dataset-level metadata
        aggregatedResult.addMetadata("dataset_total_entities", dataset.getTotalEntityCount());
        aggregatedResult.addMetadata("validation_scope", "comprehensive");
        
        logger.info("Dataset validation completed: {} total issues found", 
                   aggregatedResult.getAllIssues().size());
        
        return aggregatedResult;
    }
    
    /**
     * Validates with caching for repeated validations.
     */
    public ValidationResult validateWithCache(Object entity, String cacheKey) {
        if (cacheKey != null && validationCache.containsKey(cacheKey)) {
            logger.debug("Returning cached validation result for key: {}", cacheKey);
            return validationCache.get(cacheKey);
        }
        
        ValidationResult result = validateEntity(entity);
        
        if (cacheKey != null) {
            validationCache.put(cacheKey, result);
            logger.debug("Cached validation result for key: {}", cacheKey);
        }
        
        return result;
    }
    
    /**
     * Clears the validation cache.
     */
    public void clearCache() {
        validationCache.clear();
        logger.info("Validation cache cleared");
    }
    
    /**
     * Gets validation statistics.
     */
    public ValidationServiceStatistics getStatistics() {
        return new ValidationServiceStatistics(
            validators.size(),
            validationCache.size(),
            validators.keySet().stream().map(Class::getSimpleName).toList(),
            new ArrayList<>(validationCache.keySet())
        );
    }
    
    /**
     * Gets all available validators.
     */
    public Map<Class<?>, Validator<?>> getAvailableValidators() {
        return new HashMap<>(validators);
    }
    
    /**
     * Checks if a validator exists for the given entity type.
     */
    public boolean hasValidator(Class<?> entityType) {
        return validators.containsKey(entityType);
    }
    
    /**
     * Gets the validator for a specific entity type.
     */
    public Validator<?> getValidator(Class<?> entityType) {
        return validators.get(entityType);
    }
    
    /**
     * Statistics about the validation service.
     */
    public record ValidationServiceStatistics(
        int registeredValidators,
        int cachedResults,
        List<String> supportedEntityTypes,
        List<String> cachedKeys
    ) {
        public boolean hasCachedResults() {
            return cachedResults > 0;
        }
        
        public boolean supportsEntityType(String entityType) {
            return supportedEntityTypes.contains(entityType);
        }
    }
}