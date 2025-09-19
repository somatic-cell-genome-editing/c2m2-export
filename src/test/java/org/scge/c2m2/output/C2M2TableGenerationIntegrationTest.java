package org.scge.c2m2.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scge.c2m2.model.c2m2.*;
import org.scge.c2m2.output.generators.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test demonstrating C2M2 table generation functionality.
 */
class C2M2TableGenerationIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testCompleteC2M2TableGeneration() throws IOException {
        // Create sample C2M2 entities
        C2M2Project project = C2M2Project.builder()
            .idNamespace("scge.org")
            .localId("project-1")
            .persistentId("SCGE:PROJECT:1")
            .creationTime("2023-01-15T10:00:00Z")
            .name("CRISPR Gene Editing Study")
            .description("Comprehensive study on CRISPR-based gene editing techniques")
            .abbreviation("CGES")
            .build();
        
        C2M2Subject subject = C2M2Subject.builder()
            .idNamespace("scge.org")
            .localId("subject-1")
            .persistentId("SCGE:SUBJECT:1")
            .creationTime("2023-01-15T11:00:00Z")
            .granularity("cell line")
            .species("Homo sapiens")
            .build();
        
        C2M2Biosample biosample = C2M2Biosample.builder()
            .idNamespace("scge.org")
            .localId("biosample-1")
            .persistentId("SCGE:BIOSAMPLE:1")
            .creationTime("2023-01-15T12:00:00Z")
            .name("HEK293 cells")
            .description("Human embryonic kidney cells")
            .anatomy("kidney")
            .subjectLocalId("subject-1")
            .build();
        
        C2M2File file = C2M2File.builder()
            .idNamespace("scge.org")
            .localId("file-1")
            .persistentId("SCGE:FILE:1")
            .creationTime("2023-01-15T13:00:00Z")
            .filename("experiment_data.csv")
            .fileFormat("CSV")
            .dataType("experimental data")
            .assayType("gene editing efficiency")
            .mimeType("text/csv")
            .sizeInBytes(1024L)
            .description("Gene editing efficiency measurements")
            .build();
        
        // Create table generators
        ProjectTableGenerator projectGenerator = new ProjectTableGenerator();
        SubjectTableGenerator subjectGenerator = new SubjectTableGenerator();
        BiosampleTableGenerator biosampleGenerator = new BiosampleTableGenerator();
        FileTableGenerator fileGenerator = new FileTableGenerator();
        
        // Generate individual tables
        projectGenerator.generateTable(List.of(project), tempDir);
        subjectGenerator.generateTable(List.of(subject), tempDir);
        biosampleGenerator.generateTable(List.of(biosample), tempDir);
        fileGenerator.generateTable(List.of(file), tempDir);
        
        // Verify all tables were created
        Path projectFile = tempDir.resolve("project.tsv");
        Path subjectFile = tempDir.resolve("subject.tsv");
        Path biosampleFile = tempDir.resolve("biosample.tsv");
        Path fileTableFile = tempDir.resolve("file.tsv");
        
        assertTrue(Files.exists(projectFile), "project.tsv should be created");
        assertTrue(Files.exists(subjectFile), "subject.tsv should be created");
        assertTrue(Files.exists(biosampleFile), "biosample.tsv should be created");
        assertTrue(Files.exists(fileTableFile), "file.tsv should be created");
        
        // Verify project table content
        List<String> projectLines = Files.readAllLines(projectFile);
        assertEquals(2, projectLines.size()); // header + 1 record
        assertTrue(projectLines.get(0).contains("id_namespace"));
        assertTrue(projectLines.get(1).contains("CRISPR Gene Editing Study"));
        
        // Verify subject table content
        List<String> subjectLines = Files.readAllLines(subjectFile);
        assertEquals(2, subjectLines.size());
        assertTrue(subjectLines.get(0).contains("id_namespace"));
        assertTrue(subjectLines.get(1).contains("Homo sapiens"));
        
        // Verify biosample table content
        List<String> biosampleLines = Files.readAllLines(biosampleFile);
        assertEquals(2, biosampleLines.size());
        assertTrue(biosampleLines.get(0).contains("id_namespace"));
        assertTrue(biosampleLines.get(1).contains("HEK293 cells"));
        
        // Verify file table content
        List<String> fileLines = Files.readAllLines(fileTableFile);
        assertEquals(2, fileLines.size());
        assertTrue(fileLines.get(0).contains("id_namespace"));
        assertTrue(fileLines.get(1).contains("experiment_data.csv"));
        
        System.out.println("✓ C2M2 table generation integration test completed successfully");
        System.out.println("✓ Generated files: project.tsv, subject.tsv, biosample.tsv, file.tsv");
    }
}