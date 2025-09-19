package org.scge.c2m2.validation.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.validation.ValidationResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for C2M2ProjectValidator.
 */
class C2M2ProjectValidatorTest {
    
    private C2M2ProjectValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new C2M2ProjectValidator();
    }
    
    @Test
    void testValidProject() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .persistentId("SCGE.ORG:PROJECT:123")
            .creationTime("2023-01-15T10:00:00Z")
            .name("Test Project")
            .description("A test project for validation")
            .abbreviation("TP")
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
        // May have warnings for optional fields, but should be valid
    }
    
    @Test
    void testNullProject() {
        ValidationResult result = validator.validate(null);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals("PROJECT_000", result.getErrors().get(0).code());
    }
    
    @Test
    void testMissingRequiredFields() {
        C2M2Project project = C2M2Project.builder()
            .build(); // All fields null
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() >= 3); // Missing namespace, local_id, name
        
        // Check for specific error codes
        boolean hasNamespaceError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_001".equals(issue.code()));
        boolean hasLocalIdError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_002".equals(issue.code()));
        boolean hasNameError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_006".equals(issue.code()));
        
        assertTrue(hasNamespaceError, "Should have namespace error");
        assertTrue(hasLocalIdError, "Should have local_id error");
        assertTrue(hasNameError, "Should have name error");
    }
    
    @Test
    void testInvalidLocalIdFormat() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project@123") // Invalid character @
            .name("Test Project")
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        boolean hasFormatError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_003".equals(issue.code()));
        assertTrue(hasFormatError, "Should have local_id format error");
    }
    
    @Test
    void testInvalidPersistentIdFormat() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .persistentId("invalid-format") // Invalid format
            .name("Test Project")
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        boolean hasFormatError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_004".equals(issue.code()));
        assertTrue(hasFormatError, "Should have persistent_id format error");
    }
    
    @Test
    void testInvalidTimestampFormat() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .creationTime("invalid-timestamp") // Invalid format
            .name("Test Project")
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        boolean hasFormatError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_005".equals(issue.code()));
        assertTrue(hasFormatError, "Should have timestamp format error");
    }
    
    @Test
    void testNameTooLong() {
        String longName = "a".repeat(300); // Exceeds 255 character limit
        
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .name(longName)
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        boolean hasLengthError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_007".equals(issue.code()));
        assertTrue(hasLengthError, "Should have name length error");
    }
    
    @Test
    void testDescriptionTooLong() {
        String longDescription = "a".repeat(2100); // Exceeds 2000 character limit
        
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .name("Test Project")
            .description(longDescription)
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        boolean hasLengthError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_008".equals(issue.code()));
        assertTrue(hasLengthError, "Should have description length error");
    }
    
    @Test
    void testAbbreviationTooLong() {
        String longAbbreviation = "a".repeat(60); // Exceeds 50 character limit
        
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .name("Test Project")
            .abbreviation(longAbbreviation)
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertFalse(result.isValid());
        boolean hasLengthError = result.getErrors().stream()
            .anyMatch(issue -> "PROJECT_009".equals(issue.code()));
        assertTrue(hasLengthError, "Should have abbreviation length error");
    }
    
    @Test
    void testWarningsForMissingOptionalFields() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .name("Test Project")
            // Missing optional fields: description, abbreviation, persistent_id, creation_time
            .build();
        
        ValidationResult result = validator.validate(project);
        
        assertTrue(result.isValid()); // Should be valid despite warnings
        assertTrue(result.getWarnings().size() >= 4); // Should have warnings for missing optional fields
        
        // Check for specific warning codes
        boolean hasDescriptionWarning = result.getWarnings().stream()
            .anyMatch(issue -> "PROJECT_W001".equals(issue.code()));
        boolean hasAbbreviationWarning = result.getWarnings().stream()
            .anyMatch(issue -> "PROJECT_W002".equals(issue.code()));
        boolean hasPersistentIdWarning = result.getWarnings().stream()
            .anyMatch(issue -> "PROJECT_W003".equals(issue.code()));
        boolean hasCreationTimeWarning = result.getWarnings().stream()
            .anyMatch(issue -> "PROJECT_W004".equals(issue.code()));
        
        assertTrue(hasDescriptionWarning, "Should have description warning");
        assertTrue(hasAbbreviationWarning, "Should have abbreviation warning");
        assertTrue(hasPersistentIdWarning, "Should have persistent_id warning");
        assertTrue(hasCreationTimeWarning, "Should have creation_time warning");
    }
    
    @Test
    void testValidatorMetadata() {
        assertEquals(C2M2Project.class, validator.getValidatedType());
        assertEquals("C2M2 Project Validator", validator.getValidatorName());
        assertNotNull(validator.getValidationRules());
        assertTrue(validator.canValidate(C2M2Project.class));
        assertFalse(validator.canValidate(String.class));
    }
    
    @Test
    void testValidTimestampFormats() {
        String[] validTimestamps = {
            "2023-01-15T10:00:00Z",
            "2023-01-15T10:00:00.123Z",
            "2023-01-15T10:00:00",
            "2023-12-31T23:59:59.999Z"
        };
        
        for (String timestamp : validTimestamps) {
            C2M2Project project = C2M2Project.builder()
                .idNamespace("scge.org")
                .localId("project-123")
                .name("Test Project")
                .creationTime(timestamp)
                .build();
            
            ValidationResult result = validator.validate(project);
            assertTrue(result.isValid(), "Timestamp should be valid: " + timestamp);
        }
    }
    
    @Test
    void testValidLocalIdFormats() {
        String[] validLocalIds = {
            "project-123",
            "project_123",
            "project.123",
            "PROJECT-123",
            "my-project-v1.2"
        };
        
        for (String localId : validLocalIds) {
            C2M2Project project = C2M2Project.builder()
                .idNamespace("scge.org")
                .localId(localId)
                .name("Test Project")
                .build();
            
            ValidationResult result = validator.validate(project);
            assertTrue(result.isValid(), "Local ID should be valid: " + localId);
        }
    }
}