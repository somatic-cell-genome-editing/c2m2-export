package org.scge.c2m2.validation.validators;

import org.scge.c2m2.model.c2m2.C2M2Subject;
import org.scge.c2m2.validation.ValidationResult;
import org.scge.c2m2.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates C2M2 Subject entities for compliance with C2M2 specification.
 */
@Component
public class C2M2SubjectValidator implements Validator<C2M2Subject> {
    
    private static final Logger logger = LoggerFactory.getLogger(C2M2SubjectValidator.class);
    
    // Validation patterns
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");
    private static final Pattern PERSISTENT_ID_PATTERN = Pattern.compile("^[A-Z0-9._-]+:[A-Z]+:[0-9a-f-]+$");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{3})?Z?$");
    
    // Valid controlled vocabulary values
    private static final Set<String> VALID_GRANULARITIES = Set.of(
        "individual", "population", "cohort", "cell line", "tissue sample", "organism"
    );
    
    private static final Set<String> VALID_SEX_VALUES = Set.of(
        "male", "female", "intersex", "unknown", "not applicable"
    );
    
    // Validation error codes
    private static final String ERR_MISSING_NAMESPACE = "SUBJECT_001";
    private static final String ERR_MISSING_LOCAL_ID = "SUBJECT_002";
    private static final String ERR_INVALID_LOCAL_ID = "SUBJECT_003";
    private static final String ERR_INVALID_PERSISTENT_ID = "SUBJECT_004";
    private static final String ERR_INVALID_TIMESTAMP = "SUBJECT_005";
    private static final String ERR_INVALID_GRANULARITY = "SUBJECT_006";
    private static final String ERR_INVALID_SEX = "SUBJECT_007";
    
    // Warning codes
    private static final String WARN_MISSING_GRANULARITY = "SUBJECT_W001";
    private static final String WARN_MISSING_SPECIES = "SUBJECT_W002";
    private static final String WARN_MISSING_PERSISTENT_ID = "SUBJECT_W003";
    private static final String WARN_MISSING_CREATION_TIME = "SUBJECT_W004";
    
    @Override
    public ValidationResult validate(C2M2Subject subject) {
        ValidationResult result = new ValidationResult("C2M2Subject");
        
        if (subject == null) {
            result.addError("SUBJECT_000", "Subject entity is null", "subject", null, null);
            return result;
        }
        
        String subjectId = subject.localId();
        result.getStatistics().incrementEntitiesValidated();
        
        logger.debug("Validating C2M2 Subject: {}", subjectId);
        
        // Required field validations
        validateRequiredFields(subject, result);
        
        // Format validations
        validateFormats(subject, result);
        
        // Controlled vocabulary validations
        validateControlledVocabulary(subject, result);
        
        // Optional field warnings
        validateOptionalFields(subject, result);
        
        // Add metadata
        result.addMetadata("validated_entity_type", "C2M2Subject");
        result.addMetadata("validated_entity_id", subjectId);
        
        logger.debug("Subject validation completed: {} issues found", result.getAllIssues().size());
        
        return result;
    }
    
    /**
     * Validates required fields.
     */
    private void validateRequiredFields(C2M2Subject subject, ValidationResult result) {
        String subjectId = subject.localId();
        
        // id_namespace is required
        if (subject.idNamespace() == null || subject.idNamespace().trim().isEmpty()) {
            result.addError(ERR_MISSING_NAMESPACE, 
                "Subject id_namespace is required", 
                "subject", subjectId, "id_namespace");
        }
        
        // local_id is required
        if (subject.localId() == null || subject.localId().trim().isEmpty()) {
            result.addError(ERR_MISSING_LOCAL_ID, 
                "Subject local_id is required", 
                "subject", subjectId, "local_id");
        }
    }
    
    /**
     * Validates field formats.
     */
    private void validateFormats(C2M2Subject subject, ValidationResult result) {
        String subjectId = subject.localId();
        
        // Validate local_id format
        if (subject.localId() != null && !ID_PATTERN.matcher(subject.localId()).matches()) {
            result.addError(ERR_INVALID_LOCAL_ID, 
                "Subject local_id contains invalid characters. Only alphanumeric, underscore, hyphen, and period are allowed", 
                "subject", subjectId, "local_id",
                Map.of("invalid_value", subject.localId()));
        }
        
        // Validate persistent_id format
        if (subject.persistentId() != null && !subject.persistentId().trim().isEmpty() &&
            !PERSISTENT_ID_PATTERN.matcher(subject.persistentId()).matches()) {
            result.addError(ERR_INVALID_PERSISTENT_ID, 
                "Subject persistent_id format is invalid. Expected format: NAMESPACE:TYPE:ID", 
                "subject", subjectId, "persistent_id",
                Map.of("invalid_value", subject.persistentId()));
        }
        
        // Validate creation_time format
        if (subject.creationTime() != null && !subject.creationTime().trim().isEmpty() &&
            !TIMESTAMP_PATTERN.matcher(subject.creationTime()).matches()) {
            result.addError(ERR_INVALID_TIMESTAMP, 
                "Subject creation_time format is invalid. Expected ISO 8601 format", 
                "subject", subjectId, "creation_time",
                Map.of("invalid_value", subject.creationTime()));
        }
    }
    
    /**
     * Validates controlled vocabulary fields.
     */
    private void validateControlledVocabulary(C2M2Subject subject, ValidationResult result) {
        String subjectId = subject.localId();
        
        // Validate granularity
        if (subject.granularity() != null && !subject.granularity().trim().isEmpty() &&
            !VALID_GRANULARITIES.contains(subject.granularity().toLowerCase())) {
            result.addError(ERR_INVALID_GRANULARITY, 
                "Subject granularity value is not in the allowed vocabulary", 
                "subject", subjectId, "granularity",
                Map.of("invalid_value", subject.granularity(), "valid_values", VALID_GRANULARITIES));
        }
        
        // Validate sex
        if (subject.sex() != null && !subject.sex().trim().isEmpty() &&
            !VALID_SEX_VALUES.contains(subject.sex().toLowerCase())) {
            result.addError(ERR_INVALID_SEX, 
                "Subject sex value is not in the allowed vocabulary", 
                "subject", subjectId, "sex",
                Map.of("invalid_value", subject.sex(), "valid_values", VALID_SEX_VALUES));
        }
    }
    
    /**
     * Validates optional fields and generates warnings.
     */
    private void validateOptionalFields(C2M2Subject subject, ValidationResult result) {
        String subjectId = subject.localId();
        
        // Warning for missing granularity
        if (subject.granularity() == null || subject.granularity().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_GRANULARITY, 
                "Subject granularity is recommended for proper classification", 
                "subject", subjectId, "granularity");
        }
        
        // Warning for missing species
        if (subject.species() == null || subject.species().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_SPECIES, 
                "Subject species is recommended for biological context", 
                "subject", subjectId, "species");
        }
        
        // Warning for missing persistent_id
        if (subject.persistentId() == null || subject.persistentId().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_PERSISTENT_ID, 
                "Subject persistent_id is recommended for external referencing", 
                "subject", subjectId, "persistent_id");
        }
        
        // Warning for missing creation_time
        if (subject.creationTime() == null || subject.creationTime().trim().isEmpty()) {
            result.addWarning(WARN_MISSING_CREATION_TIME, 
                "Subject creation_time is recommended for temporal tracking", 
                "subject", subjectId, "creation_time");
        }
    }
    
    @Override
    public Class<C2M2Subject> getValidatedType() {
        return C2M2Subject.class;
    }
    
    @Override
    public String getValidatorName() {
        return "C2M2 Subject Validator";
    }
    
    @Override
    public String getValidationRules() {
        return """
            C2M2 Subject Validation Rules:
            - id_namespace: Required, non-empty string
            - local_id: Required, alphanumeric with underscore/hyphen/period only
            - persistent_id: Optional, format NAMESPACE:TYPE:ID
            - creation_time: Optional, ISO 8601 timestamp format
            - granularity: Optional, controlled vocabulary: individual, population, cohort, cell line, tissue sample, organism
            - sex: Optional, controlled vocabulary: male, female, intersex, unknown, not applicable
            - ethnicity: Optional, free text
            - race: Optional, free text
            - strain: Optional, free text
            - species: Optional, free text (recommended)
            - age_at_collection: Optional, free text
            - subject_role_taxonomy: Optional, free text
            """;
    }
}