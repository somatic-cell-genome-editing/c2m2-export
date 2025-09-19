package org.scge.c2m2.packaging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the complete packaging system.
 * Tests the end-to-end workflow from configuration to packaged submission.
 */
class PackagingIntegrationTest {
    
    @TempDir
    Path tempDir;
    
    private PackagingService packagingService;
    private Path outputDir;
    private Path testDataDir;
    
    @BeforeEach
    void setUp() throws IOException {
        packagingService = new PackagingService();
        outputDir = tempDir.resolve("packages");
        testDataDir = tempDir.resolve("test-data");
        
        Files.createDirectories(outputDir);
        Files.createDirectories(testDataDir);
        
        // Create test TSV files
        createTestDataFiles();
    }
    
    @Test
    void testCompletePackagingWorkflow() throws IOException {
        // Create package configuration
        PackageConfiguration config = PackageConfiguration.builder()
            .packageId("test-package-001")
            .outputDirectory(outputDir)
            .submissionTitle("Test C2M2 Submission")
            .submissionDescription("A test submission generated for integration testing")
            .organization("Test Organization")
            .contactEmail("test@example.com")
            .addDataFile("project", testDataDir.resolve("project.tsv"))
            .addDataFile("subject", testDataDir.resolve("subject.tsv"))
            .addAssociationFile("project_in_project", testDataDir.resolve("project_in_project.tsv"))
            .addValidationReport(testDataDir.resolve("validation_report.json"))
            .createArchive(true)
            .validatePackage(true)
            .generateReadme(true)
            .build();
        
        // Validate configuration
        PackageConfiguration.ValidationResult configValidation = config.validate();
        assertTrue(configValidation.isValid(), "Configuration should be valid: " + configValidation.getSummary());
        
        // Create submission package
        C2M2Package c2m2Package = packagingService.createSubmissionPackage(config);
        
        // Verify package structure
        assertNotNull(c2m2Package);
        assertEquals("test-package-001", c2m2Package.getPackageId());
        assertTrue(c2m2Package.isComplete());
        
        // Check package files
        Map<String, C2M2Package.PackageFile> files = c2m2Package.getFiles();
        assertTrue(files.containsKey("manifest.json"));
        assertTrue(files.containsKey("README.md"));
        assertTrue(files.containsKey("data/project.tsv"));
        assertTrue(files.containsKey("data/subject.tsv"));
        assertTrue(files.containsKey("associations/project_in_project.tsv"));
        assertTrue(files.containsKey("validation/validation_report.json"));
        
        // Verify all files exist
        for (C2M2Package.PackageFile file : files.values()) {
            assertTrue(file.exists(), "File should exist: " + file.relativePath());
        }
        
        // Check package metadata
        PackageMetadata metadata = c2m2Package.getMetadata();
        assertEquals(2, metadata.getTableCount()); // project, subject
        assertEquals(1, metadata.getAssociationCount()); // project_in_project
        assertEquals(5, metadata.getTotalRecords()); // 2 + 2 + 1 from test data
        
        // Check manifest content
        C2M2Manifest manifest = c2m2Package.getManifest();
        assertEquals("test-package-001", manifest.getSubmissionId());
        assertEquals("Test C2M2 Submission", manifest.getSubmissionTitle());
        assertEquals("Test Organization", manifest.getOrganization());
        assertEquals("test@example.com", manifest.getContactEmail());
        assertTrue(manifest.getTotalFiles() > 0);
        assertTrue(manifest.getTotalRecords() > 0);
        
        // Validate package
        PackagingService.PackageValidationResult validation = packagingService.validatePackage(c2m2Package);
        assertTrue(validation.isValid(), "Package should be valid: " + validation.getSummary());
        
        // Create archive
        Path archivePath = outputDir.resolve("test-package-001.zip");
        Path createdArchive = packagingService.createPackageArchive(c2m2Package, archivePath);
        
        assertTrue(Files.exists(createdArchive));
        assertTrue(Files.size(createdArchive) > 0);
        assertEquals(archivePath, createdArchive);
        
        // Test package summary
        C2M2Package.PackageSummary summary = c2m2Package.getSummary();
        assertEquals("test-package-001", summary.packageId());
        assertEquals(6, summary.totalFiles()); // 2 data + 1 association + 1 validation + 1 manifest + 1 readme
        assertEquals(5, summary.totalRecords());
        assertTrue(summary.isComplete());
        assertTrue(summary.getFormattedSize().endsWith("B") || summary.getFormattedSize().endsWith("KB"));
        
        System.out.println("✓ Complete packaging workflow test passed");
        System.out.println("Package summary: " + summary);
        System.out.println("Archive created: " + createdArchive + " (" + Files.size(createdArchive) + " bytes)");
    }
    
    @Test
    void testMinimalPackageConfiguration() throws IOException {
        // Create minimal configuration
        PackageConfiguration minimalConfig = PackageConfiguration.basic(
            "minimal-package",
            outputDir,
            "Minimal Test Package",
            "A minimal package for testing"
        );
        
        // Add one data file
        PackageConfiguration config = PackageConfiguration.builder()
            .packageId(minimalConfig.getPackageId())
            .outputDirectory(minimalConfig.getOutputDirectory())
            .submissionTitle(minimalConfig.getSubmissionTitle())
            .submissionDescription(minimalConfig.getSubmissionDescription())
            .addDataFile("project", testDataDir.resolve("project.tsv"))
            .build();
        
        // Validate and create package
        PackageConfiguration.ValidationResult validation = config.validate();
        assertTrue(validation.isValid());
        
        C2M2Package minimalPackage = packagingService.createSubmissionPackage(config);
        
        assertNotNull(minimalPackage);
        assertTrue(minimalPackage.isComplete());
        assertEquals(3, minimalPackage.getFileCount()); // data file + manifest + readme
        
        System.out.println("✓ Minimal package configuration test passed");
    }
    
    @Test
    void testPackageValidationFailures() throws IOException {
        // Create package with missing files
        PackageConfiguration configWithMissingFiles = PackageConfiguration.builder()
            .packageId("invalid-package")
            .outputDirectory(outputDir)
            .submissionTitle("Invalid Package")
            .submissionDescription("Package with missing files")
            .addDataFile("nonexistent", testDataDir.resolve("nonexistent.tsv"))
            .build();
        
        // Configuration validation should fail
        PackageConfiguration.ValidationResult configValidation = configWithMissingFiles.validate();
        assertFalse(configValidation.isValid());
        assertTrue(configValidation.errors().size() > 0);
        
        System.out.println("✓ Package validation failure test passed");
        System.out.println("Expected validation errors: " + configValidation.errors());
    }
    
    @Test
    void testLargePackageHandling() throws IOException {
        // Create configuration with multiple file types
        PackageConfiguration largeConfig = PackageConfiguration.builder()
            .packageId("large-package")
            .outputDirectory(outputDir)
            .submissionTitle("Large Test Package")
            .submissionDescription("Package with many file types")
            .organization("Large Test Organization")
            .contactEmail("large@example.com")
            .addDataFile("project", testDataDir.resolve("project.tsv"))
            .addDataFile("subject", testDataDir.resolve("subject.tsv"))
            .addAssociationFile("project_in_project", testDataDir.resolve("project_in_project.tsv"))
            .addValidationReport(testDataDir.resolve("validation_report.json"))
            .addAdditionalFile("docs/schema.json", testDataDir.resolve("schema.json"))
            .addAdditionalFile("docs/metadata.xml", testDataDir.resolve("metadata.xml"))
            .build();
        
        // Create additional test files
        Files.writeString(testDataDir.resolve("schema.json"), "{\"version\": \"1.0\"}");
        Files.writeString(testDataDir.resolve("metadata.xml"), "<metadata><version>1.0</version></metadata>");
        
        C2M2Package largePackage = packagingService.createSubmissionPackage(largeConfig);
        
        // Verify comprehensive package
        assertTrue(largePackage.getFileCount() >= 8); // 2 data + 1 association + 1 validation + 2 additional + manifest + readme
        assertTrue(largePackage.isComplete());
        
        // Check file types
        List<C2M2Package.PackageFile> dataFiles = largePackage.getFilesByType("data");
        List<C2M2Package.PackageFile> associationFiles = largePackage.getFilesByType("association");
        List<C2M2Package.PackageFile> additionalFiles = largePackage.getFilesByType("additional");
        
        assertEquals(2, dataFiles.size());
        assertEquals(1, associationFiles.size());
        assertEquals(2, additionalFiles.size());
        
        // Validate and create archive
        PackagingService.PackageValidationResult validation = packagingService.validatePackage(largePackage);
        assertTrue(validation.isValid());
        
        Path archivePath = packagingService.createPackageArchive(largePackage, 
                                                               outputDir.resolve("large-package.zip"));
        assertTrue(Files.exists(archivePath));
        
        System.out.println("✓ Large package handling test passed");
        System.out.println("Large package files: " + largePackage.getFileCount());
        System.out.println("Archive size: " + Files.size(archivePath) + " bytes");
    }
    
    private void createTestDataFiles() throws IOException {
        // Create project.tsv
        String projectTsv = """
            id_namespace\tlocal_id\tname\tdescription\tabbreviation
            scge.org\tproject-1\tTest Project 1\tFirst test project\tTP1
            scge.org\tproject-2\tTest Project 2\tSecond test project\tTP2
            """;
        Files.writeString(testDataDir.resolve("project.tsv"), projectTsv);
        
        // Create subject.tsv
        String subjectTsv = """
            id_namespace\tlocal_id\tgranularity\tspecies
            scge.org\tsubject-1\tindividual\tHomo sapiens
            scge.org\tsubject-2\tindividual\tHomo sapiens
            """;
        Files.writeString(testDataDir.resolve("subject.tsv"), subjectTsv);
        
        // Create project_in_project.tsv
        String projectInProjectTsv = """
            parent_project\tchild_project
            scge.org:project-1\tscge.org:project-2
            """;
        Files.writeString(testDataDir.resolve("project_in_project.tsv"), projectInProjectTsv);
        
        // Create validation report
        String validationReport = """
            {
              "validation_summary": {
                "is_valid": true,
                "total_errors": 0,
                "total_warnings": 2,
                "validated_at": "2024-01-01T12:00:00"
              },
              "entity_results": {
                "project": {"valid": 2, "invalid": 0},
                "subject": {"valid": 2, "invalid": 0}
              }
            }
            """;
        Files.writeString(testDataDir.resolve("validation_report.json"), validationReport);
    }
}