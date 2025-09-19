package org.scge.c2m2.mapping.mappers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.model.scge.Study;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StudyToProjectMapper.
 */
class StudyToProjectMapperTest {
    
    private StudyToProjectMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new StudyToProjectMapper();
    }
    
    @Test
    void testMapValidStudy() throws MappingException {
        Study study = new Study(
            123,
            "Gene Editing in Cancer Research",
            "A comprehensive study on CRISPR-based gene editing for cancer therapy",
            "active",
            Instant.parse("2023-01-15T10:00:00Z"),
            "tier1",
            "Dr. Jane Smith",
            "University Medical Center"
        );
        
        C2M2Project result = mapper.map(study);
        
        assertNotNull(result);
        assertEquals("scge.org", result.idNamespace());
        assertEquals("project-123", result.localId());
        assertEquals("SCGE:STUDY:123", result.persistentId());
        assertNotNull(result.creationTime());
        assertEquals("Gene Editing in Cancer Research", result.name());
        assertEquals("A comprehensive study on CRISPR-based gene editing for cancer therapy", result.description());
        assertEquals("GEICR", result.abbreviation());
    }
    
    @Test
    void testMapStudyWithMinimalData() throws MappingException {
        Study study = Study.of(456, "Minimal Study", "Basic description");
        
        C2M2Project result = mapper.map(study);
        
        assertNotNull(result);
        assertEquals("scge.org", result.idNamespace());
        assertEquals("project-456", result.localId());
        assertEquals("SCGE:STUDY:456", result.persistentId());
        assertEquals("Minimal Study", result.name());
        assertEquals("Basic description", result.description());
        assertEquals("MS", result.abbreviation());
    }
    
    @Test
    void testMapNullStudy() throws MappingException {
        C2M2Project result = mapper.map(null);
        assertNull(result);
    }
    
    @Test
    void testMapIncompleteStudy() {
        Study incompleteStudy = new Study(null, null, null, null, null, null, null, null);
        
        assertThrows(MappingException.class, () -> mapper.map(incompleteStudy));
    }
    
    @Test
    void testCanMapValidStudy() {
        Study validStudy = Study.of(123, "Test Study", "Test Description");
        assertTrue(mapper.canMap(validStudy));
    }
    
    @Test
    void testCanMapInvalidStudy() {
        Study invalidStudy = new Study(null, null, null, null, null, null, null, null);
        assertFalse(mapper.canMap(invalidStudy));
    }
    
    @Test
    void testCanMapNull() {
        assertFalse(mapper.canMap(null));
    }
    
    @Test
    void testGetSourceType() {
        assertEquals(Study.class, mapper.getSourceType());
    }
    
    @Test
    void testGetTargetType() {
        assertEquals(C2M2Project.class, mapper.getTargetType());
    }
}