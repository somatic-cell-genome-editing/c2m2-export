package org.scge.c2m2.output.generators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scge.c2m2.model.c2m2.C2M2Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ProjectTableGenerator.
 */
class ProjectTableGeneratorTest {
    
    @TempDir
    Path tempDir;
    
    private ProjectTableGenerator generator;
    
    @BeforeEach
    void setUp() {
        generator = new ProjectTableGenerator();
    }
    
    @Test
    void testGetTableFileName() {
        assertEquals("project.tsv", generator.getTableFileName());
    }
    
    @Test
    void testGetHeaders() {
        String[] headers = generator.getHeaders();
        
        assertNotNull(headers);
        assertEquals(7, headers.length);
        assertEquals("id_namespace", headers[0]);
        assertEquals("local_id", headers[1]);
        assertEquals("persistent_id", headers[2]);
        assertEquals("creation_time", headers[3]);
        assertEquals("name", headers[4]);
        assertEquals("description", headers[5]);
        assertEquals("abbreviation", headers[6]);
    }
    
    @Test
    void testEntityToRecord() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .persistentId("SCGE:PROJECT:123")
            .creationTime("2023-01-15T10:00:00Z")
            .name("Test Project")
            .description("Test Description")
            .abbreviation("TP")
            .build();
        
        String[] record = generator.entityToRecord(project);
        
        assertNotNull(record);
        assertEquals(7, record.length);
        assertEquals("scge.org", record[0]);
        assertEquals("project-123", record[1]);
        assertEquals("SCGE:PROJECT:123", record[2]);
        assertEquals("2023-01-15T10:00:00Z", record[3]);
        assertEquals("Test Project", record[4]);
        assertEquals("Test Description", record[5]);
        assertEquals("TP", record[6]);
    }
    
    @Test
    void testEntityToRecordWithNullProject() {
        assertThrows(IllegalArgumentException.class, 
            () -> generator.entityToRecord(null));
    }
    
    @Test
    void testEntityToRecordWithNullFields() {
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .build();
        
        String[] record = generator.entityToRecord(project);
        
        assertNotNull(record);
        assertEquals(7, record.length);
        assertEquals("scge.org", record[0]);
        assertEquals("project-123", record[1]);
        assertEquals("", record[2]); // null persistent_id becomes empty string
        assertEquals("", record[3]); // null creation_time becomes empty string
        assertEquals("", record[4]); // null name becomes empty string
        assertEquals("", record[5]); // null description becomes empty string
        assertEquals("", record[6]); // null abbreviation becomes empty string
    }
    
    @Test
    void testCanGenerate() {
        // Valid project
        C2M2Project validProject = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-123")
            .name("Test Project")
            .build();
        
        assertTrue(generator.canGenerate(validProject));
        
        // Project with missing id_namespace
        C2M2Project invalidProject1 = C2M2Project.builder()
            .localId("project-123")
            .name("Test Project")
            .build();
        
        assertFalse(generator.canGenerate(invalidProject1));
        
        // Project with missing local_id
        C2M2Project invalidProject2 = C2M2Project.builder()
            .idNamespace("scge.org")
            .name("Test Project")
            .build();
        
        assertFalse(generator.canGenerate(invalidProject2));
        
        // Null project
        assertFalse(generator.canGenerate(null));
    }
    
    @Test
    void testGetEntityType() {
        assertEquals(C2M2Project.class, generator.getEntityType());
    }
    
    @Test
    void testGenerateTable() throws IOException {
        List<C2M2Project> projects = Arrays.asList(
            C2M2Project.builder()
                .idNamespace("scge.org")
                .localId("project-1")
                .name("Project 1")
                .description("First project")
                .build(),
            C2M2Project.builder()
                .idNamespace("scge.org")
                .localId("project-2")
                .name("Project 2")
                .description("Second project")
                .build()
        );
        
        generator.generateTable(projects, tempDir);
        
        Path outputFile = tempDir.resolve("project.tsv");
        assertTrue(Files.exists(outputFile));
        
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size()); // header + 2 data rows
        
        // Check header
        assertEquals("id_namespace\tlocal_id\tpersistent_id\tcreation_time\tname\tdescription\tabbreviation", 
                    lines.get(0));
        
        // Check first project record
        assertTrue(lines.get(1).startsWith("scge.org\tproject-1"));
        assertTrue(lines.get(1).contains("Project 1"));
        assertTrue(lines.get(1).contains("First project"));
        
        // Check second project record
        assertTrue(lines.get(2).startsWith("scge.org\tproject-2"));
        assertTrue(lines.get(2).contains("Project 2"));
        assertTrue(lines.get(2).contains("Second project"));
    }
    
    @Test
    void testGenerateTableWithEmptyList() throws IOException {
        generator.generateTable(Arrays.asList(), tempDir);
        
        Path outputFile = tempDir.resolve("project.tsv");
        assertFalse(Files.exists(outputFile)); // No file should be created for empty list
    }
}