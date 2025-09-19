package org.scge.c2m2.validation;

/**
 * Interface for validating C2M2 entities and data structures.
 * Implementations provide specific validation logic for different aspects of C2M2 compliance.
 * 
 * @param <T> The type of object this validator can validate
 */
public interface Validator<T> {
    
    /**
     * Validates a single entity and returns validation results.
     * 
     * @param entity The entity to validate
     * @return ValidationResult containing any issues found
     */
    ValidationResult validate(T entity);
    
    /**
     * Gets the type of entity this validator handles.
     * 
     * @return Class of the entity type
     */
    Class<T> getValidatedType();
    
    /**
     * Gets a human-readable name for this validator.
     * 
     * @return Validator name
     */
    String getValidatorName();
    
    /**
     * Checks if this validator can handle the given entity type.
     * 
     * @param entityClass The entity class to check
     * @return true if this validator can validate the entity type
     */
    default boolean canValidate(Class<?> entityClass) {
        return getValidatedType().isAssignableFrom(entityClass);
    }
    
    /**
     * Gets the validation rules or schema this validator enforces.
     * Used for documentation and debugging purposes.
     * 
     * @return Description of validation rules
     */
    default String getValidationRules() {
        return "Standard validation rules for " + getValidatedType().getSimpleName();
    }
}