package org.scge.c2m2.output;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for generating C2M2 table files from entities.
 * Each C2M2 entity type should have its own table generator implementation.
 * 
 * @param <T> The entity type this generator handles
 */
public interface TableGenerator<T> {
    
    /**
     * Gets the filename for this table (e.g., "project.tsv").
     * 
     * @return The table filename
     */
    String getTableFileName();
    
    /**
     * Gets the column headers for this table.
     * 
     * @return Array of column header names
     */
    String[] getHeaders();
    
    /**
     * Converts an entity to a record (array of string values).
     * 
     * @param entity The entity to convert
     * @return Array of string values representing the entity
     */
    String[] entityToRecord(T entity);
    
    /**
     * Converts a list of entities to records.
     * 
     * @param entities The entities to convert
     * @return List of string arrays representing the entities
     */
    default List<String[]> entitiesToRecords(List<T> entities) {
        return entities.stream()
                .map(this::entityToRecord)
                .toList();
    }
    
    /**
     * Generates a TSV file from a list of entities.
     * 
     * @param entities The entities to write
     * @param outputDirectory The directory where the TSV file will be created
     * @throws IOException if file writing fails
     */
    default void generateTable(List<T> entities, Path outputDirectory) throws IOException {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        
        Path outputPath = outputDirectory.resolve(getTableFileName());
        String[] headers = getHeaders();
        List<String[]> records = entitiesToRecords(entities);
        
        TsvWriter.validateRecords(headers, records);
        TsvWriter.writeTsv(outputPath, headers, records);
    }
    
    /**
     * Gets the entity type this generator handles.
     * 
     * @return The entity class
     */
    Class<T> getEntityType();
    
    /**
     * Validates that an entity can be converted to a record.
     * 
     * @param entity The entity to validate
     * @return true if the entity is valid for conversion
     */
    default boolean canGenerate(T entity) {
        return entity != null;
    }
}