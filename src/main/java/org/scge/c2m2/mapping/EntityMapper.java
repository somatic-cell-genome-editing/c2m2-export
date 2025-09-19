package org.scge.c2m2.mapping;

/**
 * Generic interface for mapping entities from SCGE database format to C2M2 format.
 * 
 * @param <T> The source entity type (SCGE format)
 * @param <U> The target entity type (C2M2 format)
 */
public interface EntityMapper<T, U> {
    
    /**
     * Maps a source entity to a target entity.
     * 
     * @param source The source entity to map
     * @return The mapped target entity, or null if mapping is not possible
     * @throws MappingException if an error occurs during mapping
     */
    U map(T source) throws MappingException;
    
    /**
     * Checks if the mapper can handle the given source entity.
     * 
     * @param source The source entity to check
     * @return true if the mapper can handle this entity, false otherwise
     */
    default boolean canMap(T source) {
        return source != null;
    }
    
    /**
     * Gets the source entity type this mapper handles.
     * 
     * @return The source entity class
     */
    Class<T> getSourceType();
    
    /**
     * Gets the target entity type this mapper produces.
     * 
     * @return The target entity class
     */
    Class<U> getTargetType();
}