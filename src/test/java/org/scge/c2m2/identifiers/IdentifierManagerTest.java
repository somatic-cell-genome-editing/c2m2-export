package org.scge.c2m2.identifiers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IdentifierManager.
 */
class IdentifierManagerTest {
    
    private IdentifierManager manager;
    
    @BeforeEach
    void setUp() {
        // Create manager with test strategies
        DefaultIdentifierStrategy defaultStrategy = new DefaultIdentifierStrategy();
        UuidIdentifierStrategy uuidStrategy = new UuidIdentifierStrategy();
        
        manager = new IdentifierManager(Arrays.asList(defaultStrategy, uuidStrategy));
    }
    
    @Test
    void testGenerateLocalIdDefault() {
        String localId = manager.generateLocalId("project", 123);
        
        assertNotNull(localId);
        assertEquals("project-123", localId);
    }
    
    @Test
    void testGenerateLocalIdWithStrategy() {
        String localId = manager.generateLocalId("project", 123, "uuid");
        
        assertNotNull(localId);
        // UUID format: 8-4-4-4-12 characters
        assertTrue(localId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }
    
    @Test
    void testGeneratePersistentIdDefault() {
        String persistentId = manager.generatePersistentId("project", 123);
        
        assertNotNull(persistentId);
        assertEquals("SCGE.ORG:PROJECT:123", persistentId);
    }
    
    @Test
    void testGeneratePersistentIdWithNamespaceAndStrategy() {
        String persistentId = manager.generatePersistentId("test.org", "project", 123, "default");
        
        assertNotNull(persistentId);
        assertEquals("TEST.ORG:PROJECT:123", persistentId);
    }
    
    @Test
    void testIsValidIdentifier() {
        assertTrue(manager.isValidIdentifier("project-123", "project"));
        assertTrue(manager.isValidIdentifier("SCGE.ORG:PROJECT:123", "project"));
        
        assertFalse(manager.isValidIdentifier("invalid", "project"));
        assertFalse(manager.isValidIdentifier(null, "project"));
    }
    
    @Test
    void testIsValidIdentifierWithStrategy() {
        // Test with UUID strategy
        String uuidId = manager.generateLocalId("project", 123, "uuid");
        assertTrue(manager.isValidIdentifier(uuidId, "project", "uuid"));
        
        // Test with default strategy
        assertTrue(manager.isValidIdentifier("project-123", "project", "default"));
    }
    
    @Test
    void testIsIdentifierUsed() {
        String localId = manager.generateLocalId("project", 123);
        
        assertTrue(manager.isIdentifierUsed("project", localId, false));
        assertFalse(manager.isIdentifierUsed("project", "unused-id", false));
    }
    
    @Test
    void testReserveIdentifier() {
        String testId = "reserved-project-1";
        
        assertFalse(manager.isIdentifierUsed("project", testId, false));
        
        manager.reserveIdentifier("project", testId, false);
        
        assertTrue(manager.isIdentifierUsed("project", testId, false));
    }
    
    @Test
    void testGetGeneratedIdentifiers() {
        manager.generateLocalId("project", 123);
        manager.generateLocalId("project", 456);
        
        Set<String> generated = manager.getGeneratedIdentifiers("project", false);
        
        assertEquals(2, generated.size());
        assertTrue(generated.contains("project-123"));
        assertTrue(generated.contains("project-456"));
    }
    
    @Test
    void testSetEntityTypeStrategy() {
        manager.setEntityTypeStrategy("project", "uuid");
        
        // Now generating a project ID should use UUID strategy by default
        String localId = manager.generateLocalId("project", 123);
        assertTrue(localId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }
    
    @Test
    void testSetEntityTypeStrategyInvalidStrategy() {
        assertThrows(IllegalArgumentException.class, 
            () -> manager.setEntityTypeStrategy("project", "nonexistent"));
    }
    
    @Test
    void testSetEntityTypeStrategyUnsupportedEntityType() {
        // This should work since both strategies support all entity types
        assertDoesNotThrow(() -> manager.setEntityTypeStrategy("project", "uuid"));
    }
    
    @Test
    void testGetAvailableStrategies() {
        Set<String> strategies = manager.getAvailableStrategies();
        
        assertTrue(strategies.contains("default"));
        assertTrue(strategies.contains("uuid"));
        assertEquals(2, strategies.size());
    }
    
    @Test
    void testGetStatistics() {
        manager.generateLocalId("project", 123);
        manager.generateLocalId("subject", 456);
        manager.generatePersistentId("project", 789);
        
        IdentifierManager.IdentifierStatistics stats = manager.getStatistics();
        
        assertEquals(3, stats.totalGenerated());
        assertTrue(stats.countsByType().containsKey("project_local"));
        assertTrue(stats.countsByType().containsKey("subject_local"));
        assertTrue(stats.countsByType().containsKey("project_persistent"));
    }
    
    @Test
    void testClearTrackedIdentifiers() {
        manager.generateLocalId("project", 123);
        
        assertTrue(manager.isIdentifierUsed("project", "project-123", false));
        
        manager.clearTrackedIdentifiers();
        
        assertFalse(manager.isIdentifierUsed("project", "project-123", false));
    }
    
    @Test
    void testGetDefaultNamespace() {
        assertEquals("scge.org", manager.getDefaultNamespace());
    }
    
    @Test
    void testGetDefaultStrategyName() {
        assertEquals("default", manager.getDefaultStrategyName());
    }
}