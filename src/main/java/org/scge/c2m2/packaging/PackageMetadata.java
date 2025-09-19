package org.scge.c2m2.packaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata container for C2M2 package information.
 * Tracks statistics and information about the package contents.
 */
public class PackageMetadata {
    
    private final Map<String, Integer> tableCounts;
    private final Map<String, Integer> associationCounts;
    private final Map<String, Object> customMetadata;
    
    public PackageMetadata() {
        this.tableCounts = new HashMap<>();
        this.associationCounts = new HashMap<>();
        this.customMetadata = new HashMap<>();
    }
    
    /**
     * Adds information about a data table.
     */
    public void addTableInfo(String tableType, int recordCount) {
        tableCounts.put(tableType, recordCount);
    }
    
    /**
     * Adds information about an association table.
     */
    public void addAssociationInfo(String associationType, int recordCount) {
        associationCounts.put(associationType, recordCount);
    }
    
    /**
     * Adds custom metadata.
     */
    public void addCustomMetadata(String key, Object value) {
        customMetadata.put(key, value);
    }
    
    /**
     * Gets the total number of records across all tables.
     */
    public int getTotalRecords() {
        return tableCounts.values().stream().mapToInt(Integer::intValue).sum() +
               associationCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Gets the number of data tables.
     */
    public int getTableCount() {
        return tableCounts.size();
    }
    
    /**
     * Gets the number of association tables.
     */
    public int getAssociationCount() {
        return associationCounts.size();
    }
    
    /**
     * Gets record count for a specific table.
     */
    public int getTableRecordCount(String tableType) {
        return tableCounts.getOrDefault(tableType, 0);
    }
    
    /**
     * Gets record count for a specific association.
     */
    public int getAssociationRecordCount(String associationType) {
        return associationCounts.getOrDefault(associationType, 0);
    }
    
    /**
     * Gets all table information.
     */
    public Map<String, Integer> getTableCounts() {
        return new HashMap<>(tableCounts);
    }
    
    /**
     * Gets all association information.
     */
    public Map<String, Integer> getAssociationCounts() {
        return new HashMap<>(associationCounts);
    }
    
    /**
     * Gets all custom metadata.
     */
    public Map<String, Object> getCustomMetadata() {
        return new HashMap<>(customMetadata);
    }
    
    /**
     * Gets custom metadata value.
     */
    public Object getCustomMetadata(String key) {
        return customMetadata.get(key);
    }
    
    /**
     * Checks if custom metadata exists for a key.
     */
    public boolean hasCustomMetadata(String key) {
        return customMetadata.containsKey(key);
    }
    
    /**
     * Creates a summary of the metadata.
     */
    public String getSummary() {
        return String.format(
            "PackageMetadata{tables=%d, associations=%d, totalRecords=%d, customKeys=%d}",
            getTableCount(), getAssociationCount(), getTotalRecords(), customMetadata.size()
        );
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}