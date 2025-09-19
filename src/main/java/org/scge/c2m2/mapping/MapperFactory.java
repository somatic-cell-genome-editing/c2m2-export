package org.scge.c2m2.mapping;

import org.scge.c2m2.mapping.mappers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Factory for creating and managing entity mappers.
 * Provides access to all available mappers and supports mapper lookup by type.
 */
@Component
public class MapperFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(MapperFactory.class);
    
    private final Map<Class<?>, EntityMapper<?, ?>> mappersBySourceType = new HashMap<>();
    private final List<EntityMapper<?, ?>> allMappers = new ArrayList<>();
    
    @Autowired
    public MapperFactory(
            StudyToProjectMapper studyToProjectMapper,
            PersonToSubjectMapper personToSubjectMapper,
            ModelToSubjectMapper modelToSubjectMapper,
            ExperimentRecordToBiosampleMapper experimentRecordToBiosampleMapper,
            ProtocolToFileMapper protocolToFileMapper,
            ImageToFileMapper imageToFileMapper) {
        
        registerMapper(studyToProjectMapper);
        registerMapper(personToSubjectMapper);
        registerMapper(modelToSubjectMapper);
        registerMapper(experimentRecordToBiosampleMapper);
        registerMapper(protocolToFileMapper);
        registerMapper(imageToFileMapper);
        
        logger.info("MapperFactory initialized with {} mappers", allMappers.size());
    }
    
    /**
     * Registers a mapper with the factory.
     */
    private void registerMapper(EntityMapper<?, ?> mapper) {
        Class<?> sourceType = mapper.getSourceType();
        mappersBySourceType.put(sourceType, mapper);
        allMappers.add(mapper);
        logger.debug("Registered mapper: {} -> {}", 
                    sourceType.getSimpleName(), 
                    mapper.getTargetType().getSimpleName());
    }
    
    /**
     * Gets a mapper for the specified source type.
     * 
     * @param sourceType The source entity type
     * @param <T> The source type
     * @param <U> The target type
     * @return The mapper for the source type, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T, U> EntityMapper<T, U> getMapper(Class<T> sourceType) {
        EntityMapper<?, ?> mapper = mappersBySourceType.get(sourceType);
        if (mapper != null) {
            return (EntityMapper<T, U>) mapper;
        }
        return null;
    }
    
    /**
     * Gets a mapper that can handle the specified source object.
     * 
     * @param source The source object
     * @param <T> The source type
     * @param <U> The target type
     * @return The mapper that can handle the source object, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T, U> EntityMapper<T, U> getMapperFor(T source) {
        if (source == null) {
            return null;
        }
        
        Class<?> sourceClass = source.getClass();
        EntityMapper<?, ?> mapper = mappersBySourceType.get(sourceClass);
        
        if (mapper != null && canMapUnsafe(mapper, source)) {
            return (EntityMapper<T, U>) mapper;
        }
        
        // If no exact match, try to find a compatible mapper
        for (EntityMapper<?, ?> candidateMapper : allMappers) {
            if (candidateMapper.getSourceType().isAssignableFrom(sourceClass) && 
                canMapUnsafe(candidateMapper, source)) {
                return (EntityMapper<T, U>) candidateMapper;
            }
        }
        
        return null;
    }
    
    /**
     * Helper method to safely call canMap with type erasure.
     */
    @SuppressWarnings("unchecked")
    private boolean canMapUnsafe(EntityMapper<?, ?> mapper, Object source) {
        try {
            return ((EntityMapper<Object, ?>) mapper).canMap(source);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    /**
     * Gets all available mappers.
     * 
     * @return List of all registered mappers
     */
    public List<EntityMapper<?, ?>> getAllMappers() {
        return new ArrayList<>(allMappers);
    }
    
    /**
     * Gets all mappers that produce the specified target type.
     * 
     * @param targetType The target entity type
     * @param <U> The target type
     * @return List of mappers that produce the target type
     */
    @SuppressWarnings("unchecked")
    public <U> List<EntityMapper<?, U>> getMappersForTargetType(Class<U> targetType) {
        List<EntityMapper<?, U>> result = new ArrayList<>();
        
        for (EntityMapper<?, ?> mapper : allMappers) {
            if (targetType.isAssignableFrom(mapper.getTargetType())) {
                result.add((EntityMapper<?, U>) mapper);
            }
        }
        
        return result;
    }
    
    /**
     * Checks if a mapper exists for the specified source type.
     * 
     * @param sourceType The source entity type
     * @return true if a mapper exists, false otherwise
     */
    public boolean hasMapper(Class<?> sourceType) {
        return mappersBySourceType.containsKey(sourceType);
    }
    
    /**
     * Checks if any mapper can handle the specified source object.
     * 
     * @param source The source object
     * @return true if a mapper can handle the object, false otherwise
     */
    public boolean canMap(Object source) {
        return getMapperFor(source) != null;
    }
    
    /**
     * Gets the supported source types.
     * 
     * @return Set of supported source types
     */
    public Set<Class<?>> getSupportedSourceTypes() {
        return new HashSet<>(mappersBySourceType.keySet());
    }
    
    /**
     * Gets mapping statistics.
     * 
     * @return Map of mapper names to their source/target type information
     */
    public Map<String, String> getMappingInfo() {
        Map<String, String> info = new HashMap<>();
        
        for (EntityMapper<?, ?> mapper : allMappers) {
            String mapperName = mapper.getClass().getSimpleName();
            String mapping = mapper.getSourceType().getSimpleName() + 
                           " -> " + 
                           mapper.getTargetType().getSimpleName();
            info.put(mapperName, mapping);
        }
        
        return info;
    }
}