package org.scge.c2m2.output;

import org.scge.c2m2.model.c2m2.*;
import org.scge.c2m2.output.generators.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating C2M2 table files from mapped entities.
 * Coordinates all table generators and manages the output process.
 */
@Service
public class TableGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TableGenerationService.class);
    
    private final ProjectTableGenerator projectTableGenerator;
    private final SubjectTableGenerator subjectTableGenerator;
    private final BiosampleTableGenerator biosampleTableGenerator;
    private final FileTableGenerator fileTableGenerator;
    
    private final Map<Class<?>, TableGenerator<?>> generatorsByType;
    
    @Autowired
    public TableGenerationService(
            ProjectTableGenerator projectTableGenerator,
            SubjectTableGenerator subjectTableGenerator,
            BiosampleTableGenerator biosampleTableGenerator,
            FileTableGenerator fileTableGenerator) {
        
        this.projectTableGenerator = projectTableGenerator;
        this.subjectTableGenerator = subjectTableGenerator;
        this.biosampleTableGenerator = biosampleTableGenerator;
        this.fileTableGenerator = fileTableGenerator;
        
        // Build type-to-generator mapping
        this.generatorsByType = Map.of(
            C2M2Project.class, projectTableGenerator,
            C2M2Subject.class, subjectTableGenerator,
            C2M2Biosample.class, biosampleTableGenerator,
            C2M2File.class, fileTableGenerator
        );
        
        logger.info("TableGenerationService initialized with {} generators", 
                   generatorsByType.size());
    }
    
    /**
     * Generates all C2M2 tables from a collection of mixed entities.
     * 
     * @param entities Collection of C2M2 entities
     * @param outputDirectory Directory where TSV files will be created
     * @return GenerationResult containing statistics and any errors
     * @throws IOException if file operations fail
     */
    public GenerationResult generateAllTables(Collection<Object> entities, Path outputDirectory) 
            throws IOException {
        
        logger.info("Starting table generation for {} entities to {}", 
                   entities.size(), outputDirectory);
        
        // Ensure output directory exists
        Files.createDirectories(outputDirectory);
        
        // Group entities by type
        Map<Class<?>, List<Object>> entitiesByType = groupEntitiesByType(entities);
        
        GenerationResult result = new GenerationResult();
        result.setStartTime(LocalDateTime.now());
        
        // Generate each table type
        for (Map.Entry<Class<?>, List<Object>> entry : entitiesByType.entrySet()) {
            Class<?> entityType = entry.getKey();
            List<Object> entitiesOfType = entry.getValue();
            
            try {
                generateTableForType(entityType, entitiesOfType, outputDirectory, result);
            } catch (Exception e) {
                logger.error("Failed to generate table for type {}: {}", 
                           entityType.getSimpleName(), e.getMessage(), e);
                result.addError(entityType.getSimpleName(), e.getMessage());
            }
        }
        
        result.setEndTime(LocalDateTime.now());
        
        logger.info("Table generation completed. Generated {} tables with {} errors in {} ms",
                   result.getGeneratedTables().size(), 
                   result.getErrors().size(),
                   result.getDurationMillis());
        
        return result;
    }
    
    /**
     * Generates tables for specific entity types only.
     * 
     * @param entitiesByType Map of entity types to their instances
     * @param outputDirectory Directory where TSV files will be created
     * @return GenerationResult containing statistics and any errors
     * @throws IOException if file operations fail
     */
    public GenerationResult generateSpecificTables(Map<Class<?>, List<Object>> entitiesByType, 
                                                  Path outputDirectory) throws IOException {
        
        logger.info("Starting specific table generation for {} types to {}", 
                   entitiesByType.size(), outputDirectory);
        
        Files.createDirectories(outputDirectory);
        
        GenerationResult result = new GenerationResult();
        result.setStartTime(LocalDateTime.now());
        
        for (Map.Entry<Class<?>, List<Object>> entry : entitiesByType.entrySet()) {
            Class<?> entityType = entry.getKey();
            List<Object> entitiesOfType = entry.getValue();
            
            try {
                generateTableForType(entityType, entitiesOfType, outputDirectory, result);
            } catch (Exception e) {
                logger.error("Failed to generate table for type {}: {}", 
                           entityType.getSimpleName(), e.getMessage(), e);
                result.addError(entityType.getSimpleName(), e.getMessage());
            }
        }
        
        result.setEndTime(LocalDateTime.now());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private void generateTableForType(Class<?> entityType, List<Object> entities, 
                                    Path outputDirectory, GenerationResult result) throws IOException {
        
        TableGenerator<Object> generator = (TableGenerator<Object>) generatorsByType.get(entityType);
        if (generator == null) {
            logger.warn("No generator found for entity type: {}", entityType.getSimpleName());
            result.addError(entityType.getSimpleName(), "No generator available");
            return;
        }
        
        // Filter entities that can be generated
        List<Object> validEntities = entities.stream()
                .filter(generator::canGenerate)
                .toList();
        
        if (validEntities.isEmpty()) {
            logger.info("No valid entities found for type: {}", entityType.getSimpleName());
            return;
        }
        
        logger.info("Generating table {} with {} entities", 
                   generator.getTableFileName(), validEntities.size());
        
        generator.generateTable(validEntities, outputDirectory);
        
        TableStatistics stats = new TableStatistics(
            generator.getTableFileName(),
            entityType.getSimpleName(),
            validEntities.size(),
            entities.size() - validEntities.size()
        );
        
        result.addTableStatistics(stats);
    }
    
    /**
     * Groups entities by their class type.
     */
    private Map<Class<?>, List<Object>> groupEntitiesByType(Collection<Object> entities) {
        Map<Class<?>, List<Object>> grouped = new HashMap<>();
        
        for (Object entity : entities) {
            if (entity != null) {
                grouped.computeIfAbsent(entity.getClass(), k -> new ArrayList<>()).add(entity);
            }
        }
        
        logger.debug("Grouped {} entities into {} types", entities.size(), grouped.size());
        return grouped;
    }
    
    /**
     * Gets all supported entity types.
     */
    public Set<Class<?>> getSupportedEntityTypes() {
        return new HashSet<>(generatorsByType.keySet());
    }
    
    /**
     * Gets the generator for a specific entity type.
     */
    public TableGenerator<?> getGenerator(Class<?> entityType) {
        return generatorsByType.get(entityType);
    }
    
    /**
     * Checks if a generator exists for the given entity type.
     */
    public boolean hasGenerator(Class<?> entityType) {
        return generatorsByType.containsKey(entityType);
    }
    
    /**
     * Result of table generation operation.
     */
    public static class GenerationResult {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private final List<TableStatistics> generatedTables = new ArrayList<>();
        private final Map<String, String> errors = new HashMap<>();
        
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public void addTableStatistics(TableStatistics stats) {
            generatedTables.add(stats);
        }
        
        public void addError(String entityType, String errorMessage) {
            errors.put(entityType, errorMessage);
        }
        
        public List<TableStatistics> getGeneratedTables() { return generatedTables; }
        public Map<String, String> getErrors() { return errors; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        
        public long getDurationMillis() {
            if (startTime != null && endTime != null) {
                return java.time.Duration.between(startTime, endTime).toMillis();
            }
            return 0;
        }
        
        public int getTotalEntitiesGenerated() {
            return generatedTables.stream().mapToInt(TableStatistics::validEntities).sum();
        }
        
        public int getTotalEntitiesSkipped() {
            return generatedTables.stream().mapToInt(TableStatistics::skippedEntities).sum();
        }
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        @Override
        public String toString() {
            return String.format(
                "GenerationResult{tables=%d, entities=%d, skipped=%d, errors=%d, duration=%dms}",
                generatedTables.size(), getTotalEntitiesGenerated(), 
                getTotalEntitiesSkipped(), errors.size(), getDurationMillis());
        }
    }
    
    /**
     * Statistics for a single generated table.
     */
    public record TableStatistics(
        String fileName,
        String entityType,
        int validEntities,
        int skippedEntities
    ) {
        public int totalEntities() {
            return validEntities + skippedEntities;
        }
        
        public double validPercentage() {
            return totalEntities() > 0 ? (validEntities * 100.0) / totalEntities() : 0.0;
        }
    }
}