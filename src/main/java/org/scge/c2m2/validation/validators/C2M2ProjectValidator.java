package org.scge.c2m2.validation.validators;

import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.validation.ValidationResult;
import org.scge.c2m2.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validates C2M2 Project entities for compliance with C2M2 specification.
 */
@Component
public class C2M2ProjectValidator implements Validator<C2M2Project> {
    
    private static final Logger logger = LoggerFactory.getLogger(C2M2ProjectValidator.class);
    
    // Validation patterns
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");
    private static final Pattern PERSISTENT_ID_PATTERN = Pattern.compile("^[A-Z0-9._-]+:[A-Z]+:[0-9a-f-]+$");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z?$");
    
    // Validation error codes
    private static final String ERR_MISSING_NAMESPACE = "PROJECT_001";
    private static final String ERR_MISSING_LOCAL_ID = "PROJECT_002";
    private static final String ERR_INVALID_LOCAL_ID = "PROJECT_003";
    private static final String ERR_INVALID_PERSISTENT_ID = "PROJECT_004";
    private static final String ERR_INVALID_TIMESTAMP = "PROJECT_005";
    private static final String ERR_MISSING_NAME = "PROJECT_006";
    private static final String ERR_NAME_TOO_LONG = "PROJECT_007";
    private static final String ERR_DESCRIPTION_TOO_LONG = "PROJECT_008";
    private static final String ERR_ABBREVIATION_TOO_LONG = "PROJECT_009";
    
    // Warning codes
    private static final String WARN_MISSING_DESCRIPTION = "PROJECT_W001";
    private static final String WARN_MISSING_ABBREVIATION = "PROJECT_W002";
    private static final String WARN_MISSING_PERSISTENT_ID = "PROJECT_W003";
    private static final String WARN_MISSING_CREATION_TIME = "PROJECT_W004";
    
    // Field length limits
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final int MAX_ABBREVIATION_LENGTH = 50;
    
    @Override
    public ValidationResult validate(C2M2Project project) {
        ValidationResult result = new ValidationResult("C2M2Project");
        
        if (project == null) {
            result.addError("PROJECT_000", "Project entity is null", "project", null, null);
            return result;
        }
        
        String projectId = project.localId();
        result.getStatistics().incrementEntitiesValidated();
        
        logger.debug("Validating C2M2 Project: {}", projectId);
        
        // Required field validations
        validateRequiredFields(project, result);
        
        // Format validations
        validateFormats(project, result);
        
        // Length validations
        validateLengths(project, result);
        
        // Optional field warnings
        validateOptionalFields(project, result);
        
        // Add metadata
        result.addMetadata("validated_entity_type", "C2M2Project");
        result.addMetadata("validated_entity_id", projectId);
        
        logger.debug("Project validation completed: {} issues found", result.getAllIssues().size());
        
        return result;
    }
    
    /**
     * Validates required fields.
     */
    private void validateRequiredFields(C2M2Project project, ValidationResult result) {
        String projectId = project.localId();
        
        // id_namespace is required
        if (project.idNamespace() == null || project.idNamespace().trim().isEmpty()) {
            result.addError(ERR_MISSING_NAMESPACE, 
                "Project id_namespace is required", 
                "project", projectId, "id_namespace");
        }
        
        // local_id is required
        if (project.localId() == null || project.localId().trim().isEmpty()) {
            result.addError(ERR_MISSING_LOCAL_ID, 
                "Project local_id is required", 
                "project", projectId, "local_id");
        }
        
        // name is required
        if (project.name() == null || project.name().trim().isEmpty()) {
            result.addError(ERR_MISSING_NAME, 
                "Project name is required", 
                "project", projectId, "name");
        }
    }
    
    /**
     * Validates field formats.
     */
    private void validateFormats(C2M2Project project, ValidationResult result) {
        String projectId = project.localId();
        
        // Validate local_id format
        if (project.localId() != null && !ID_PATTERN.matcher(project.localId()).matches()) {
            result.addError(ERR_INVALID_LOCAL_ID, 
                "Project local_id contains invalid characters. Only alphanumeric, underscore, hyphen, and period are allowed", 
                "project", projectId, "local_id",
                Map.of("invalid_value", project.localId()));
        }
        
        // Validate persistent_id format
        if (project.persistentId() != null && !project.persistentId().trim().isEmpty() &&
            !PERSISTENT_ID_PATTERN.matcher(project.persistentId()).matches()) {
            result.addError(ERR_INVALID_PERSISTENT_ID, 
                "Project persistent_id format is invalid. Expected format: NAMESPACE:TYPE:ID", 
                "project", projectId, "persistent_id",
                Map.of("invalid_value", project.persistentId()));
        }
        
        // Validate creation_time format
        if (project.creationTime() != null && !project.creationTime().trim().isEmpty() &&
            !TIMESTAMP_PATTERN.matcher(project.creationTime()).matches()) {
            result.addError(ERR_INVALID_TIMESTAMP, 
                "Project creation_time format is invalid. Expected ISO 8601 format", 
                "project", projectId, "creation_time",
                Map.of("invalid_value", project.creationTime()));
        }
    }
    
    /**
     * Validates field lengths.
     */
    private void validateLengths(C2M2Project project, ValidationResult result) {
        String projectId = project.localId();
        
        // Validate name length
        if (project.name() != null && project.name().length() > MAX_NAME_LENGTH) {
            result.addError(ERR_NAME_TOO_LONG, 
                String.format("Project name exceeds maximum length of %d characters", MAX_NAME_LENGTH), 
                "project", projectId, "name",
                Map.of("current_length", project.name().length(), "max_length", MAX_NAME_LENGTH));
        }
        
        // Validate description length
        if (project.description() != null && project.description().length() > MAX_DESCRIPTION_LENGTH) {
            result.addError(ERR_DESCRIPTION_TOO_LONG, 
                String.format("Project description exceeds maximum length of %d characters", MAX_DESCRIPTION_LENGTH), 
                "project", projectId, "description",
                Map.of("current_length", project.description().length(), "max_length", MAX_DESCRIPTION_LENGTH));
        }
        
        // Validate abbreviation length
        if (project.abbreviation() != null && project.abbreviation().length() > MAX_ABBREVIATION_LENGTH) {
            result.addError(ERR_ABBREVIATION_TOO_LONG, 
                String.format("Project abbreviation exceeds maximum length of %d characters", MAX_ABBREVIATION_LENGTH), 
                "project", projectId, "abbreviation",
                Map.of("current_length", project.abbreviation().length(), "max_length", MAX_ABBREVIATION_LENGTH));
        }
    }
    
    /**
     * Validates optional fields and generates warnings.
     */
    private void validateOptionalFields(C2M2Project project, ValidationResult result) {
        String projectId = project.localId();
        
        // Warning for missing description
        if (project.description() == null || project.description().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_DESCRIPTION, 
                "Project description is recommended for better metadata", 
                "project", projectId, "description");
        }
        
        // Warning for missing abbreviation
        if (project.abbreviation() == null || project.abbreviation().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_ABBREVIATION, 
                "Project abbreviation is recommended for display purposes", 
                "project", projectId, "abbreviation");
        }
        
        // Warning for missing persistent_id
        if (project.persistentId() == null || project.persistentId().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_PERSISTENT_ID, 
                "Project persistent_id is recommended for external referencing", 
                "project", projectId, "persistent_id");
        }
        
        // Warning for missing creation_time
        if (project.creationTime() == null || project.creationTime().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_CREATION_TIME, 
                "Project creation_time is recommended for temporal tracking", 
                "project", projectId, "creation_time");
        }
    }
    
    @Override
    public Class<C2M2Project> getValidatedType() {
        return C2M2Project.class;
    }
    
    @Override
    public String getValidatorName() {
        return "C2M2 Project Validator";
    }
    
    @Override
    public String getValidationRules() {
        return """
            C2M2 Project Validation Rules:
            - id_namespace: Required, non-empty string
            - local_id: Required, alphanumeric with underscore/hyphen/period only
            - persistent_id: Optional, format NAMESPACE:TYPE:ID
            - creation_time: Optional, ISO 8601 timestamp format
            - name: Required, max 255 characters
            - description: Optional, max 2000 characters
            - abbreviation: Optional, max 50 characters
            """;
    }
}