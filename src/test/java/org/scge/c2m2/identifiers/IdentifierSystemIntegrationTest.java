package org.scge.c2m2.identifiers;

import org.junit.jupiter.api.Test;
import org.scge.c2m2.model.scge.Study;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the identifier system.
 */
class IdentifierSystemIntegrationTest {
    
    @Test
    void testCompleteIdentifierWorkflow() {
        // Create identifier strategies
        DefaultIdentifierStrategy defaultStrategy = new DefaultIdentifierStrategy();
        UuidIdentifierStrategy uuidStrategy = new UuidIdentifierStrategy();
        
        // Create identifier manager with manual configuration
        IdentifierManager manager = new IdentifierManager(Arrays.asList(defaultStrategy, uuidStrategy));
        
        // The manager needs configuration values, but since this is a unit test without Spring context,
        // we'll use reflection to set the values for testing
        try {
            java.lang.reflect.Field namespaceField = IdentifierManager.class.getDeclaredField("defaultNamespace");
            namespaceField.setAccessible(true);
            namespaceField.set(manager, "scge.org");
            
            java.lang.reflect.Field strategyField = IdentifierManager.class.getDeclaredField("defaultStrategyName");
            strategyField.setAccessible(true);
            strategyField.set(manager, "default");
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure IdentifierManager for test", e);
        }
        
        // Create identifier service
        IdentifierService service = new IdentifierService(manager);
        
        // Test default strategy
        String localId1 = manager.generateLocalId("project", 123);
        String persistentId1 = manager.generatePersistentId("project", 123);
        
        assertNotNull(localId1);
        assertNotNull(persistentId1);
        assertEquals("project-123", localId1);
        assertTrue(persistentId1.contains("PROJECT:123"));
        
        // Test UUID strategy
        String localId2 = manager.generateLocalId("subject", 456, "uuid");
        String persistentId2 = manager.generatePersistentId("test.org", "subject", 456, "uuid");
        
        assertNotNull(localId2);
        assertNotNull(persistentId2);
        assertTrue(localId2.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        assertTrue(persistentId2.matches("^TEST\\.ORG:SUBJECT:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        
        // Test identifier service with SCGE entities
        Study testStudy = Study.of(789, "Test Study", "Test Description");
        IdentifierService.ProjectIdentifiers projectIds = service.generateProjectIdentifiers(testStudy);
        
        assertNotNull(projectIds);
        assertNotNull(projectIds.localId());
        assertNotNull(projectIds.persistentId());
        assertNotNull(projectIds.creationTime());
        assertEquals("project-789", projectIds.localId());
        assertTrue(projectIds.persistentId().contains("PROJECT:789"));
        
        // Test validation
        assertTrue(manager.isValidIdentifier(localId1, "project"));
        assertTrue(manager.isValidIdentifier(persistentId1, "project"));
        assertFalse(manager.isValidIdentifier("invalid-id", "project"));
        
        // Test duplicate tracking
        assertTrue(manager.isIdentifierUsed("project", localId1, false));
        assertFalse(manager.isIdentifierUsed("project", "unused-id", false));
        
        // Test statistics
        IdentifierManager.IdentifierStatistics stats = manager.getStatistics();
        assertTrue(stats.totalGenerated() > 0);
        assertFalse(stats.countsByType().isEmpty());
        
        System.out.println("✓ Identifier system integration test completed successfully");
        System.out.println("✓ Generated identifiers: local=" + localId1 + ", persistent=" + persistentId1);
        System.out.println("✓ UUID identifiers: local=" + localId2 + ", persistent=" + persistentId2);
        System.out.println("✓ Project identifiers: " + projectIds);
        System.out.println("✓ Statistics: " + stats);
    }
    
    @Test
    void testIdentifierStrategies() {
        DefaultIdentifierStrategy defaultStrategy = new DefaultIdentifierStrategy();
        UuidIdentifierStrategy uuidStrategy = new UuidIdentifierStrategy();
        
        // Test default strategy
        assertEquals("default", defaultStrategy.getStrategyName());
        assertTrue(defaultStrategy.supportsEntityType("project"));
        assertTrue(defaultStrategy.supportsEntityType("subject"));
        
        String defaultLocalId = defaultStrategy.generateLocalId("biosample", 123);
        String defaultPersistentId = defaultStrategy.generatePersistentId("scge.org", "biosample", 123);
        
        assertEquals("biosample-123", defaultLocalId);
        assertEquals("SCGE.ORG:BIOSAMPLE:123", defaultPersistentId);
        
        // Test UUID strategy
        assertEquals("uuid", uuidStrategy.getStrategyName());
        assertTrue(uuidStrategy.supportsEntityType("file"));
        
        String uuidLocalId = uuidStrategy.generateLocalId("file", 456);
        String uuidPersistentId = uuidStrategy.generatePersistentId("test.org", "file", 456);
        
        assertTrue(uuidLocalId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        assertTrue(uuidPersistentId.matches("^TEST\\.ORG:FILE:[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
        
        // Test validation
        assertTrue(defaultStrategy.isValidIdentifier(defaultLocalId, "biosample"));
        assertTrue(defaultStrategy.isValidIdentifier(defaultPersistentId, "biosample"));
        assertTrue(uuidStrategy.isValidIdentifier(uuidLocalId, "file"));
        assertTrue(uuidStrategy.isValidIdentifier(uuidPersistentId, "file"));
        
        System.out.println("✓ Identifier strategies test completed successfully");
        System.out.println("✓ Default strategy: " + defaultLocalId + " / " + defaultPersistentId);
        System.out.println("✓ UUID strategy: " + uuidLocalId + " / " + uuidPersistentId);
    }
}