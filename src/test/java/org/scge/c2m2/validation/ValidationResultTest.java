package org.scge.c2m2.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationResult.
 */
class ValidationResultTest {
    
    private ValidationResult result;
    
    @BeforeEach
    void setUp() {
        result = new ValidationResult("TestValidation");
    }
    
    @Test
    void testNewValidationResultIsValid() {
        assertTrue(result.isValid());
        assertFalse(result.hasIssues());
        assertEquals("TestValidation", result.getValidationType());
        assertNotNull(result.getValidationTime());
    }
    
    @Test
    void testAddError() {
        result.addError("ERR001", "Test error", "project", "proj-1", "name");
        
        assertFalse(result.isValid());
        assertTrue(result.hasIssues());
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getStatistics().getErrorCount());
        
        ValidationIssue error = result.getErrors().get(0);
        assertEquals(ValidationLevel.ERROR, error.level());
        assertEquals("ERR001", error.code());
        assertEquals("Test error", error.message());
        assertEquals("project", error.entityType());
        assertEquals("proj-1", error.entityId());
        assertEquals("name", error.field());
    }
    
    @Test
    void testAddWarning() {
        result.addWarning("WARN001", "Test warning", "subject", "subj-1", "description");
        
        assertTrue(result.isValid()); // Warnings don't affect validity
        assertTrue(result.hasIssues());
        assertEquals(1, result.getWarnings().size());
        assertEquals(1, result.getStatistics().getWarningCount());
    }
    
    @Test
    void testAddInfo() {
        result.addInfo("INFO001", "Test info", "biosample", "bio-1", "metadata");
        
        assertTrue(result.isValid());
        assertTrue(result.hasIssues());
        assertEquals(1, result.getInfo().size());
        assertEquals(1, result.getStatistics().getInfoCount());
    }
    
    @Test
    void testAddErrorWithContext() {
        Map<String, Object> context = Map.of("expected", "string", "actual", "null");
        result.addError("ERR002", "Invalid value", "file", "file-1", "filename", context);
        
        ValidationIssue error = result.getErrors().get(0);
        assertNotNull(error.context());
        assertEquals("string", error.getContext("expected"));
        assertEquals("null", error.getContext("actual"));
        assertTrue(error.hasContext("expected"));
        assertFalse(error.hasContext("nonexistent"));
    }
    
    @Test
    void testGetIssuesByLevel() {
        result.addError("ERR001", "Error", "project", "1", "field");
        result.addWarning("WARN001", "Warning", "subject", "2", "field");
        result.addInfo("INFO001", "Info", "biosample", "3", "field");
        
        Map<ValidationLevel, List<ValidationIssue>> byLevel = result.getIssuesByLevel();
        
        assertEquals(1, byLevel.get(ValidationLevel.ERROR).size());
        assertEquals(1, byLevel.get(ValidationLevel.WARNING).size());
        assertEquals(1, byLevel.get(ValidationLevel.INFO).size());
    }
    
    @Test
    void testGetIssuesByEntityType() {
        result.addError("ERR001", "Error 1", "project", "1", "field");
        result.addError("ERR002", "Error 2", "project", "2", "field");
        result.addWarning("WARN001", "Warning", "subject", "1", "field");
        
        Map<String, List<ValidationIssue>> byType = result.getIssuesByEntityType();
        
        assertEquals(2, byType.get("project").size());
        assertEquals(1, byType.get("subject").size());
    }
    
    @Test
    void testGetIssuesForEntity() {
        result.addError("ERR001", "Error 1", "project", "proj-1", "name");
        result.addWarning("WARN001", "Warning 1", "project", "proj-1", "description");
        result.addError("ERR002", "Error 2", "project", "proj-2", "name");
        
        List<ValidationIssue> proj1Issues = result.getIssuesForEntity("project", "proj-1");
        assertEquals(2, proj1Issues.size());
        
        List<ValidationIssue> proj2Issues = result.getIssuesForEntity("project", "proj-2");
        assertEquals(1, proj2Issues.size());
    }
    
    @Test
    void testMerge() {
        result.addError("ERR001", "Error 1", "project", "1", "field");
        
        ValidationResult other = new ValidationResult("OtherValidation");
        other.addWarning("WARN001", "Warning 1", "subject", "2", "field");
        other.addInfo("INFO001", "Info 1", "biosample", "3", "field");
        
        result.merge(other);
        
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getWarnings().size());
        assertEquals(1, result.getInfo().size());
        assertEquals(3, result.getAllIssues().size());
    }
    
    @Test
    void testAddMetadata() {
        result.addMetadata("total_entities", 100);
        result.addMetadata("validation_duration", "5s");
        
        Map<String, Object> metadata = result.getMetadata();
        assertEquals(100, metadata.get("total_entities"));
        assertEquals("5s", metadata.get("validation_duration"));
    }
    
    @Test
    void testGetSummary() {
        result.addError("ERR001", "Error", "project", "1", "field");
        result.addWarning("WARN001", "Warning", "subject", "2", "field");
        
        String summary = result.getSummary();
        assertTrue(summary.contains("TestValidation"));
        assertTrue(summary.contains("1 errors"));
        assertTrue(summary.contains("1 warnings"));
        assertTrue(summary.contains("Valid: NO"));
    }
    
    @Test
    void testStatistics() {
        result.addError("ERR001", "Error", "project", "1", "field");
        result.addWarning("WARN001", "Warning", "subject", "2", "field");
        result.addInfo("INFO001", "Info", "biosample", "3", "field");
        
        ValidationResult.ValidationStatistics stats = result.getStatistics();
        assertEquals(1, stats.getErrorCount());
        assertEquals(1, stats.getWarningCount());
        assertEquals(1, stats.getInfoCount());
        assertEquals(3, stats.getTotalIssues());
    }
}