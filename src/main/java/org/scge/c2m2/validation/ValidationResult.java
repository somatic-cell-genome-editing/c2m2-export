package org.scge.c2m2.validation;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Result of a validation operation containing all validation findings.
 * Provides detailed information about validation errors, warnings, and statistics.
 */
public class ValidationResult {
    
    private final LocalDateTime validationTime;
    private final String validationType;
    private final List<ValidationIssue> errors;
    private final List<ValidationIssue> warnings;
    private final List<ValidationIssue> info;
    private final Map<String, Object> metadata;
    private final ValidationStatistics statistics;
    
    public ValidationResult(String validationType) {
        this.validationTime = LocalDateTime.now();
        this.validationType = validationType;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.info = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.statistics = new ValidationStatistics();
    }
    
    /**
     * Adds an error to the validation result.
     */
    public void addError(String code, String message, String entityType, String entityId, String field) {
        errors.add(new ValidationIssue(
            ValidationLevel.ERROR, code, message, entityType, entityId, field, null
        ));
        statistics.incrementErrors();
    }
    
    /**
     * Adds an error with additional context.
     */
    public void addError(String code, String message, String entityType, String entityId, 
                        String field, Map<String, Object> context) {
        errors.add(new ValidationIssue(
            ValidationLevel.ERROR, code, message, entityType, entityId, field, context
        ));
        statistics.incrementErrors();
    }
    
    /**
     * Adds a warning to the validation result.
     */
    public void addWarning(String code, String message, String entityType, String entityId, String field) {
        warnings.add(new ValidationIssue(
            ValidationLevel.WARNING, code, message, entityType, entityId, field, null
        ));
        statistics.incrementWarnings();
    }
    
    /**
     * Adds informational message to the validation result.
     */
    public void addInfo(String code, String message, String entityType, String entityId, String field) {
        info.add(new ValidationIssue(
            ValidationLevel.INFO, code, message, entityType, entityId, field, null
        ));
        statistics.incrementInfo();
    }
    
    /**
     * Adds metadata about the validation process.
     */
    public void addMetadata(String key, Object value) {
        metadata.put(key, value);
    }
    
    /**
     * Checks if validation passed (no errors).
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
    
    /**
     * Checks if there are any issues (errors, warnings, or info).
     */
    public boolean hasIssues() {
        return !errors.isEmpty() || !warnings.isEmpty() || !info.isEmpty();
    }
    
    /**
     * Gets all issues grouped by severity level.
     */
    public Map<ValidationLevel, List<ValidationIssue>> getIssuesByLevel() {
        Map<ValidationLevel, List<ValidationIssue>> grouped = new EnumMap<>(ValidationLevel.class);
        grouped.put(ValidationLevel.ERROR, new ArrayList<>(errors));
        grouped.put(ValidationLevel.WARNING, new ArrayList<>(warnings));
        grouped.put(ValidationLevel.INFO, new ArrayList<>(info));
        return grouped;
    }
    
    /**
     * Gets all issues grouped by entity type.
     */
    public Map<String, List<ValidationIssue>> getIssuesByEntityType() {
        Map<String, List<ValidationIssue>> grouped = new HashMap<>();
        
        getAllIssues().forEach(issue -> {
            grouped.computeIfAbsent(
                issue.entityType() != null ? issue.entityType() : "unknown", 
                k -> new ArrayList<>()
            ).add(issue);
        });
        
        return grouped;
    }
    
    /**
     * Gets all issues as a single list.
     */
    public List<ValidationIssue> getAllIssues() {
        List<ValidationIssue> allIssues = new ArrayList<>();
        allIssues.addAll(errors);
        allIssues.addAll(warnings);
        allIssues.addAll(info);
        return allIssues;
    }
    
    /**
     * Gets issues for a specific entity.
     */
    public List<ValidationIssue> getIssuesForEntity(String entityType, String entityId) {
        return getAllIssues().stream()
                .filter(issue -> Objects.equals(issue.entityType(), entityType) && 
                               Objects.equals(issue.entityId(), entityId))
                .toList();
    }
    
    /**
     * Merges another validation result into this one.
     */
    public void merge(ValidationResult other) {
        if (other != null) {
            this.errors.addAll(other.errors);
            this.warnings.addAll(other.warnings);
            this.info.addAll(other.info);
            this.metadata.putAll(other.metadata);
            this.statistics.merge(other.statistics);
        }
    }
    
    /**
     * Creates a summary of the validation result.
     */
    public String getSummary() {
        return String.format(
            "Validation %s: %d errors, %d warnings, %d info messages. Valid: %s",
            validationType,
            errors.size(),
            warnings.size(), 
            info.size(),
            isValid() ? "YES" : "NO"
        );
    }
    
    // Getters
    public LocalDateTime getValidationTime() { return validationTime; }
    public String getValidationType() { return validationType; }
    public List<ValidationIssue> getErrors() { return new ArrayList<>(errors); }
    public List<ValidationIssue> getWarnings() { return new ArrayList<>(warnings); }
    public List<ValidationIssue> getInfo() { return new ArrayList<>(info); }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public ValidationStatistics getStatistics() { return statistics; }
    
    @Override
    public String toString() {
        return getSummary();
    }
    
    /**
     * Statistics about the validation process.
     */
    public static class ValidationStatistics {
        private int errorCount = 0;
        private int warningCount = 0;
        private int infoCount = 0;
        private int entitiesValidated = 0;
        private long validationDurationMs = 0;
        
        public void incrementErrors() { errorCount++; }
        public void incrementWarnings() { warningCount++; }
        public void incrementInfo() { infoCount++; }
        public void incrementEntitiesValidated() { entitiesValidated++; }
        public void setValidationDuration(long durationMs) { this.validationDurationMs = durationMs; }
        
        public void merge(ValidationStatistics other) {
            if (other != null) {
                this.errorCount += other.errorCount;
                this.warningCount += other.warningCount;
                this.infoCount += other.infoCount;
                this.entitiesValidated += other.entitiesValidated;
                this.validationDurationMs += other.validationDurationMs;
            }
        }
        
        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public int getInfoCount() { return infoCount; }
        public int getEntitiesValidated() { return entitiesValidated; }
        public long getValidationDurationMs() { return validationDurationMs; }
        public int getTotalIssues() { return errorCount + warningCount + infoCount; }
    }
}