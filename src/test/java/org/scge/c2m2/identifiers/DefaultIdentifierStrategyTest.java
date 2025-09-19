package org.scge.c2m2.identifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefaultIdentifierStrategy.
 */
class DefaultIdentifierStrategyTest {
    
    private DefaultIdentifierStrategy strategy;
    
    @BeforeEach
    void setUp() {
        strategy = new DefaultIdentifierStrategy();
    }
    
    @Test
    void testGenerateLocalId() {
        String localId = strategy.generateLocalId("project", 123);
        
        assertNotNull(localId);
        assertEquals("project-123", localId);
    }
    
    @Test
    void testGenerateLocalIdWithStringSourceId() {
        String localId = strategy.generateLocalId("subject", "ABC-123");
        
        assertNotNull(localId);
        assertEquals("subject-abc-123", localId);
    }
    
    @Test
    void testGenerateLocalIdWithSpecialCharacters() {
        String localId = strategy.generateLocalId("biosample", "test@sample#1");
        
        assertNotNull(localId);
        assertEquals("biosample-test-sample-1", localId);
    }
    
    @Test
    void testGenerateLocalIdNullEntityType() {
        assertThrows(IllegalArgumentException.class, 
            () -> strategy.generateLocalId(null, 123));
    }
    
    @Test
    void testGenerateLocalIdNullSourceId() {
        assertThrows(IllegalArgumentException.class, 
            () -> strategy.generateLocalId("project", null));
    }
    
    @Test
    void testGenerateLocalIdUnsupportedEntityType() {
        assertThrows(IllegalArgumentException.class, 
            () -> strategy.generateLocalId("unsupported", 123));
    }
    
    @Test
    void testGeneratePersistentId() {
        String persistentId = strategy.generatePersistentId("scge.org", "project", 123);
        
        assertNotNull(persistentId);
        assertEquals("SCGE.ORG:PROJECT:123", persistentId);
    }
    
    @Test
    void testGeneratePersistentIdWithStringSourceId() {
        String persistentId = strategy.generatePersistentId("test.org", "subject", "ABC123");
        
        assertNotNull(persistentId);
        // String source IDs get converted to hash-based numeric IDs
        assertTrue(persistentId.startsWith("TEST.ORG:SUBJECT:"));
        assertTrue(persistentId.matches("^TEST\\.ORG:SUBJECT:\\d+$"));
    }
    
    @Test
    void testGeneratePersistentIdNullParameters() {
        assertThrows(IllegalArgumentException.class, 
            () -> strategy.generatePersistentId(null, "project", 123));
        
        assertThrows(IllegalArgumentException.class, 
            () -> strategy.generatePersistentId("scge.org", null, 123));
        
        assertThrows(IllegalArgumentException.class, 
            () -> strategy.generatePersistentId("scge.org", "project", null));
    }
    
    @Test
    void testIsValidIdentifierLocalId() {
        assertTrue(strategy.isValidIdentifier("project-123", "project"));
        assertTrue(strategy.isValidIdentifier("subject-abc-def", "subject"));
        
        assertFalse(strategy.isValidIdentifier("wrong-type-123", "project"));
        assertFalse(strategy.isValidIdentifier("project@123", "project"));
        assertFalse(strategy.isValidIdentifier("", "project"));
        assertFalse(strategy.isValidIdentifier(null, "project"));
    }
    
    @Test
    void testIsValidIdentifierPersistentId() {
        assertTrue(strategy.isValidIdentifier("SCGE.ORG:PROJECT:123", "project"));
        assertTrue(strategy.isValidIdentifier("TEST-ORG:SUBJECT:456", "subject"));
        
        assertFalse(strategy.isValidIdentifier("SCGE.ORG:PROJECT:123", "subject"));
        assertFalse(strategy.isValidIdentifier("SCGE.ORG:WRONG:123", "project"));
        assertFalse(strategy.isValidIdentifier("invalid-format", "project"));
    }
    
    @Test
    void testSupportsEntityType() {
        assertTrue(strategy.supportsEntityType("project"));
        assertTrue(strategy.supportsEntityType("subject"));
        assertTrue(strategy.supportsEntityType("biosample"));
        assertTrue(strategy.supportsEntityType("file"));
        assertTrue(strategy.supportsEntityType("PROJECT")); // case insensitive
        
        assertFalse(strategy.supportsEntityType("unsupported"));
        assertFalse(strategy.supportsEntityType(null));
        assertFalse(strategy.supportsEntityType(""));
    }
    
    @Test
    void testGetStrategyName() {
        assertEquals("default", strategy.getStrategyName());
    }
}