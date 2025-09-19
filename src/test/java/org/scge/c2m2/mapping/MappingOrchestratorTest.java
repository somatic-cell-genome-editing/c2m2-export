package org.scge.c2m2.mapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.model.scge.Study;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MappingOrchestrator.
 */
@ExtendWith(MockitoExtension.class)
class MappingOrchestratorTest {
    
    @Mock
    private MapperFactory mapperFactory;
    
    @Mock
    private EntityMapper<Study, C2M2Project> studyMapper;
    
    private MappingOrchestrator orchestrator;
    
    @BeforeEach
    void setUp() {
        orchestrator = new MappingOrchestrator(mapperFactory);
    }
    
    @Test
    void testMapSingleEntity() throws Exception {
        Study study = Study.of(1, "Test Study", "Description");
        C2M2Project expectedProject = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-1")
            .name("Test Study")
            .build();
        
        when(mapperFactory.getMapperFor(study)).thenReturn(studyMapper);
        when(studyMapper.map(study)).thenReturn(expectedProject);
        
        C2M2Project result = orchestrator.mapEntity(study);
        
        assertNotNull(result);
        assertEquals("Test Study", result.name());
        verify(mapperFactory).getMapperFor(study);
        verify(studyMapper).map(study);
    }
    
    @Test
    void testMapSingleEntityNoMapper() {
        Study study = Study.of(1, "Test Study", "Description");
        
        when(mapperFactory.getMapperFor(study)).thenReturn(null);
        
        C2M2Project result = orchestrator.mapEntity(study);
        
        assertNull(result);
        verify(mapperFactory).getMapperFor(study);
        verify(studyMapper, never()).map(any());
    }
    
    @Test
    void testMapSingleEntityMappingException() throws Exception {
        Study study = Study.of(1, "Test Study", "Description");
        
        when(mapperFactory.getMapperFor(study)).thenReturn(studyMapper);
        when(studyMapper.map(study)).thenThrow(new MappingException("Mapping failed"));
        
        C2M2Project result = orchestrator.mapEntity(study);
        
        assertNull(result);
        verify(mapperFactory).getMapperFor(study);
        verify(studyMapper).map(study);
    }
    
    @Test
    void testMapNullEntity() {
        C2M2Project result = orchestrator.mapEntity(null);
        assertNull(result);
    }
    
    @Test
    void testMapEntitiesCollection() throws Exception {
        Study study1 = Study.of(1, "Study 1", "Description 1");
        Study study2 = Study.of(2, "Study 2", "Description 2");
        List<Study> studies = Arrays.asList(study1, study2);
        
        C2M2Project project1 = C2M2Project.builder().localId("project-1").name("Study 1").build();
        C2M2Project project2 = C2M2Project.builder().localId("project-2").name("Study 2").build();
        
        when(mapperFactory.getMapperFor(study1)).thenReturn(studyMapper);
        when(mapperFactory.getMapperFor(study2)).thenReturn(studyMapper);
        when(studyMapper.map(study1)).thenReturn(project1);
        when(studyMapper.map(study2)).thenReturn(project2);
        
        List<C2M2Project> results = orchestrator.mapEntities(studies);
        
        assertEquals(2, results.size());
        assertEquals("Study 1", results.get(0).name());
        assertEquals("Study 2", results.get(1).name());
    }
    
    @Test
    void testMapEmptyCollection() {
        List<C2M2Project> results = orchestrator.mapEntities(Collections.emptyList());
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testMapNullCollection() {
        List<C2M2Project> results = orchestrator.mapEntities(null);
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testValidateMappability() {
        Study study1 = Study.of(1, "Study 1", "Description 1");
        Study study2 = Study.of(2, "Study 2", "Description 2");
        List<Object> entities = Arrays.asList(study1, study2);
        
        when(mapperFactory.canMap(study1)).thenReturn(true);
        when(mapperFactory.canMap(study2)).thenReturn(true);
        
        MappingOrchestrator.ValidationResult result = orchestrator.validateMappability(entities);
        
        assertTrue(result.allMappable());
        assertEquals(2, result.totalEntities());
        assertEquals(2, result.mappableEntities());
        assertTrue(result.unmappableTypes().isEmpty());
        assertEquals(100.0, result.getMappabilityPercentage());
    }
    
    @Test
    void testValidateMappabilityWithUnmappable() {
        Study study = Study.of(1, "Study 1", "Description 1");
        String unmappableString = "Not mappable";
        List<Object> entities = Arrays.asList(study, unmappableString);
        
        when(mapperFactory.canMap(study)).thenReturn(true);
        when(mapperFactory.canMap(unmappableString)).thenReturn(false);
        
        MappingOrchestrator.ValidationResult result = orchestrator.validateMappability(entities);
        
        assertFalse(result.allMappable());
        assertEquals(2, result.totalEntities());
        assertEquals(1, result.mappableEntities());
        assertEquals(1, result.unmappableTypes().size());
        assertTrue(result.unmappableTypes().contains("String"));
        assertEquals(50.0, result.getMappabilityPercentage());
    }
    
    @Test
    void testGetStatistics() {
        Map<String, MappingOrchestrator.MappingStatistics> stats = orchestrator.getStatistics();
        assertNotNull(stats);
    }
    
    @Test
    void testClearStatistics() {
        orchestrator.clearStatistics();
        assertTrue(orchestrator.getStatistics().isEmpty());
    }
    
    @Test
    void testGetMappingCapabilities() {
        when(mapperFactory.getSupportedSourceTypes()).thenReturn(Collections.singleton(Study.class));
        when(mapperFactory.getMappingInfo()).thenReturn(Collections.singletonMap("StudyMapper", "Study -> C2M2Project"));
        when(mapperFactory.getAllMappers()).thenReturn(Collections.singletonList(studyMapper));
        
        Map<String, Object> capabilities = orchestrator.getMappingCapabilities();
        
        assertNotNull(capabilities);
        assertTrue(capabilities.containsKey("supportedSourceTypes"));
        assertTrue(capabilities.containsKey("availableMappers"));
        assertTrue(capabilities.containsKey("totalMappers"));
    }
}