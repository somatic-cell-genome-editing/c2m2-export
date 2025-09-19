package org.scge.c2m2.validation;

import org.junit.jupiter.api.Test;
import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.model.c2m2.C2M2Subject;
import org.scge.c2m2.validation.validators.C2M2ProjectValidator;
import org.scge.c2m2.validation.validators.C2M2SubjectValidator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete validation system.
 */
class ValidationSystemIntegrationTest {
    
    @Test
    void testCompleteValidationWorkflow() {
        // Create validators
        C2M2ProjectValidator projectValidator = new C2M2ProjectValidator();
        C2M2SubjectValidator subjectValidator = new C2M2SubjectValidator();
        
        // Create relationship validator
        RelationshipValidator relationshipValidator = new RelationshipValidator();
        
        // Create validation service
        ValidationService validationService = new ValidationService(
            Arrays.asList(projectValidator, subjectValidator),
            relationshipValidator
        );
        
        // Test individual entity validation
        C2M2Project validProject = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .name("Test Project")
            .description("A comprehensive test project")
            .abbreviation("TP")
            .build();
        
        ValidationResult projectResult = validationService.validateEntity(validProject);
        assertTrue(projectResult.isValid());
        System.out.println("✓ Valid project validation passed");
        
        // Test invalid entity validation
        C2M2Project invalidProject = C2M2Project.builder()
            .localId("project@invalid") // Invalid character
            .build(); // Missing required fields
        
        ValidationResult invalidProjectResult = validationService.validateEntity(invalidProject);
        assertFalse(invalidProjectResult.isValid());
        assertTrue(invalidProjectResult.getErrors().size() > 0);
        System.out.println("✓ Invalid project validation correctly failed with " + 
                          invalidProjectResult.getErrors().size() + " errors");
        
        // Test collection validation
        C2M2Subject validSubject = C2M2Subject.builder()
            .idNamespace("scge.org")
            .localId("subject-456")
            .granularity("individual")
            .species("Homo sapiens")
            .build();
        
        C2M2Subject invalidSubject = C2M2Subject.builder()
            .idNamespace("scge.org")
            .localId("subject-789")
            .granularity("invalid_granularity") // Invalid vocabulary
            .build();
        
        List<C2M2Subject> subjects = Arrays.asList(validSubject, invalidSubject);
        ValidationResult collectionResult = validationService.validateEntities(subjects, C2M2Subject.class);
        
        assertFalse(collectionResult.isValid()); // Collection invalid due to one invalid entity
        assertEquals(2, collectionResult.getMetadata().get("total_entities"));
        assertEquals(1, collectionResult.getMetadata().get("valid_entities"));
        assertEquals(1, collectionResult.getMetadata().get("invalid_entities"));
        System.out.println("✓ Collection validation passed: " + collectionResult.getSummary());
        
        // Test mixed entity validation
        List<Object> mixedEntities = Arrays.asList(validProject, validSubject, invalidProject, invalidSubject);
        ValidationResult mixedResult = validationService.validateMixedEntities(mixedEntities);
        
        assertFalse(mixedResult.isValid());
        assertEquals(4, mixedResult.getMetadata().get("total_entities"));
        System.out.println("✓ Mixed entity validation passed: " + mixedResult.getSummary());
        
        // Test dataset validation
        RelationshipValidator.C2M2Dataset dataset = new RelationshipValidator.C2M2Dataset(
            Arrays.asList(validProject, invalidProject),
            Arrays.asList(validSubject, invalidSubject),
            Arrays.asList(), // No biosamples
            Arrays.asList()  // No files
        );
        
        ValidationResult datasetResult = validationService.validateDataset(dataset);
        assertFalse(datasetResult.isValid());
        assertEquals(4, datasetResult.getMetadata().get("dataset_total_entities"));
        System.out.println("✓ Dataset validation passed: " + datasetResult.getSummary());
        
        // Test validation service statistics
        ValidationService.ValidationServiceStatistics stats = validationService.getStatistics();
        assertEquals(2, stats.registeredValidators());
        assertTrue(stats.supportedEntityTypes().contains("C2M2Project"));
        assertTrue(stats.supportedEntityTypes().contains("C2M2Subject"));
        System.out.println("✓ Validation service statistics: " + stats);
        
        // Test validator lookup
        assertTrue(validationService.hasValidator(C2M2Project.class));
        assertTrue(validationService.hasValidator(C2M2Subject.class));
        assertFalse(validationService.hasValidator(String.class));
        
        Validator<?> projectValidatorFromService = validationService.getValidator(C2M2Project.class);
        assertNotNull(projectValidatorFromService);
        assertEquals("C2M2 Project Validator", projectValidatorFromService.getValidatorName());
        
        System.out.println("✓ Validation system integration test completed successfully");
        
        // Print detailed results for verification
        System.out.println("\nDetailed Validation Results:");
        System.out.println("============================");
        
        printValidationResult("Valid Project", projectResult);
        printValidationResult("Invalid Project", invalidProjectResult);
        printValidationResult("Subject Collection", collectionResult);
        printValidationResult("Mixed Entities", mixedResult);
        printValidationResult("Complete Dataset", datasetResult);
    }
    
    @Test
    void testValidationCaching() {
        C2M2ProjectValidator projectValidator = new C2M2ProjectValidator();
        ValidationService validationService = new ValidationService(
            Arrays.asList(projectValidator),
            new RelationshipValidator()
        );
        
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-cache-test")
            .name("Cache Test Project")
            .build();
        
        // First validation (should be cached)
        ValidationResult result1 = validationService.validateWithCache(project, "cache-key-1");
        assertTrue(result1.isValid());
        
        // Second validation with same key (should return cached result)
        ValidationResult result2 = validationService.validateWithCache(project, "cache-key-1");
        assertSame(result1, result2); // Should be the exact same object from cache
        
        // Check cache statistics
        ValidationService.ValidationServiceStatistics stats = validationService.getStatistics();
        assertEquals(1, stats.cachedResults());
        assertTrue(stats.hasCachedResults());
        
        // Clear cache
        validationService.clearCache();
        ValidationService.ValidationServiceStatistics statsAfterClear = validationService.getStatistics();
        assertEquals(0, statsAfterClear.cachedResults());
        assertFalse(statsAfterClear.hasCachedResults());
        
        System.out.println("✓ Validation caching test completed successfully");
    }
    
    private void printValidationResult(String testName, ValidationResult result) {
        System.out.println("\n" + testName + ":");
        System.out.println("  Valid: " + result.isValid());
        System.out.println("  Errors: " + result.getErrors().size());
        System.out.println("  Warnings: " + result.getWarnings().size());
        System.out.println("  Info: " + result.getInfo().size());
        
        if (!result.getErrors().isEmpty()) {
            System.out.println("  Error details:");
            result.getErrors().forEach(error -> 
                System.out.println("    - " + error.getDisplayString()));
        }
        
        if (!result.getWarnings().isEmpty() && result.getWarnings().size() <= 3) {
            System.out.println("  Warning details:");
            result.getWarnings().forEach(warning -> 
                System.out.println("    - " + warning.getDisplayString()));
        }
    }
}