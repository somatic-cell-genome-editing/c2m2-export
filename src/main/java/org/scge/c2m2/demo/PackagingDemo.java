package org.scge.c2m2.demo;

import org.scge.c2m2.packaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demo application showing the C2M2 packaging system in action.
 * Creates sample TSV files and packages them into a C2M2 submission.
 */
public class PackagingDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(PackagingDemo.class);
    
    public static void main(String[] args) {
        try {
            logger.info("Starting C2M2 Packaging Demo");
            
            // Create demo directory structure
            Path demoDir = Paths.get("demo-output");
            Path dataDir = demoDir.resolve("sample-data");
            Path packagesDir = demoDir.resolve("packages");
            
            Files.createDirectories(dataDir);
            Files.createDirectories(packagesDir);
            
            // Create sample data files
            createSampleDataFiles(dataDir);
            
            // Initialize packaging service
            PackagingService packagingService = new PackagingService();
            
            // Create package configuration
            PackageConfiguration config = PackageConfiguration.builder()
                .packageId("scge-demo-submission-001")
                .outputDirectory(packagesDir)
                .submissionTitle("SCGE Demo C2M2 Submission")
                .submissionDescription("A demonstration C2M2 submission package created from sample SCGE data")
                .organization("Somatic Cell Genome Editing Program")
                .contactEmail("demo@scge.org")
                .addDataFile("project", dataDir.resolve("project.tsv"))
                .addDataFile("subject", dataDir.resolve("subject.tsv"))
                .addDataFile("biosample", dataDir.resolve("biosample.tsv"))
                .addAssociationFile("subject_in_project", dataDir.resolve("subject_in_project.tsv"))
                .addAssociationFile("biosample_from_subject", dataDir.resolve("biosample_from_subject.tsv"))
                .addValidationReport(dataDir.resolve("validation_summary.json"))
                .createArchive(true)
                .validatePackage(true)
                .generateReadme(true)
                .build();
            
            logger.info("Package configuration created: {}", config);
            
            // Validate configuration
            PackageConfiguration.ValidationResult configValidation = config.validate();
            if (!configValidation.isValid()) {
                logger.error("Configuration validation failed: {}", configValidation.getSummary());
                configValidation.errors().forEach(error -> logger.error("  - {}", error));
                return;
            }
            logger.info("Configuration validation passed: {}", configValidation.getSummary());
            
            // Create submission package
            logger.info("Creating C2M2 submission package...");
            C2M2Package c2m2Package = packagingService.createSubmissionPackage(config);
            
            // Display package information
            logger.info("Package created successfully!");
            logger.info("Package ID: {}", c2m2Package.getPackageId());
            logger.info("Package Path: {}", c2m2Package.getPackagePath());
            logger.info("Total Files: {}", c2m2Package.getFileCount());
            logger.info("Total Size: {}", c2m2Package.getSummary().getFormattedSize());
            logger.info("Total Records: {}", c2m2Package.getMetadata().getTotalRecords());
            logger.info("Package Complete: {}", c2m2Package.isComplete());
            
            // Show file breakdown
            logger.info("File breakdown:");
            c2m2Package.getSummary().filesByType().forEach((type, count) -> 
                logger.info("  {}: {} files", type, count));
            
            // Show table information
            logger.info("Data tables:");
            c2m2Package.getMetadata().getTableCounts().forEach((table, count) ->
                logger.info("  {}: {} records", table, count));
            
            if (!c2m2Package.getMetadata().getAssociationCounts().isEmpty()) {
                logger.info("Association tables:");
                c2m2Package.getMetadata().getAssociationCounts().forEach((assoc, count) ->
                    logger.info("  {}: {} records", assoc, count));
            }
            
            // Validate the package
            logger.info("Validating package integrity...");
            PackagingService.PackageValidationResult validation = packagingService.validatePackage(c2m2Package);
            logger.info("Package validation: {}", validation.getSummary());
            
            if (validation.hasIssues()) {
                validation.errors().forEach(error -> logger.error("  Error: {}", error));
                validation.warnings().forEach(warning -> logger.warn("  Warning: {}", warning));
            }
            
            // Create archive
            if (validation.isValid()) {
                logger.info("Creating package archive...");
                Path archivePath = packagesDir.resolve(c2m2Package.getPackageId() + ".zip");
                Path createdArchive = packagingService.createPackageArchive(c2m2Package, archivePath);
                
                logger.info("Archive created: {}", createdArchive);
                logger.info("Archive size: {} bytes", Files.size(createdArchive));
                
                logger.info("=== Demo completed successfully! ===");
                logger.info("Check the following locations:");
                logger.info("  Package directory: {}", c2m2Package.getPackagePath());
                logger.info("  Package archive: {}", createdArchive);
                logger.info("  Manifest file: {}", c2m2Package.getPackagePath().resolve("manifest.json"));
                logger.info("  README file: {}", c2m2Package.getPackagePath().resolve("README.md"));
            } else {
                logger.error("Package validation failed - skipping archive creation");
            }
            
        } catch (Exception e) {
            logger.error("Demo failed with error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    private static void createSampleDataFiles(Path dataDir) throws IOException {
        logger.info("Creating sample data files in: {}", dataDir);
        
        // Create project.tsv
        String projectTsv = """
            id_namespace\tlocal_id\tname\tdescription\tabbreviation
            scge.org\tproject-001\tCRISPR Knockout Study\tSystematic CRISPR knockout study in human cell lines\tCKS
            scge.org\tproject-002\tBase Editing Analysis\tAnalysis of base editing efficiency and outcomes\tBEA
            scge.org\tproject-003\tPrime Editing Optimization\tOptimization of prime editing protocols\tPEO
            """;
        Files.writeString(dataDir.resolve("project.tsv"), projectTsv);
        
        // Create subject.tsv
        String subjectTsv = """
            id_namespace\tlocal_id\tgranularity\tspecies\tsex
            scge.org\tsubject-001\tindividual\tHomo sapiens\tmale
            scge.org\tsubject-002\tindividual\tHomo sapiens\tfemale
            scge.org\tsubject-003\tindividual\tHomo sapiens\tmale
            scge.org\tsubject-004\tindividual\tHomo sapiens\tfemale
            scge.org\tsubject-005\tindividual\tHomo sapiens\tmale
            """;
        Files.writeString(dataDir.resolve("subject.tsv"), subjectTsv);
        
        // Create biosample.tsv
        String biosampleTsv = """
            id_namespace\tlocal_id\tname\tdescription\tspecies
            scge.org\tbiosample-001\tHEK293T cells\tHuman embryonic kidney 293T cells\tHomo sapiens
            scge.org\tbiosample-002\tHeLa cells\tHeLa cervical cancer cell line\tHomo sapiens
            scge.org\tbiosample-003\tK562 cells\tChronic myelogenous leukemia cell line\tHomo sapiens
            scge.org\tbiosample-004\tU2OS cells\tHuman osteosarcoma cell line\tHomo sapiens
            """;
        Files.writeString(dataDir.resolve("biosample.tsv"), biosampleTsv);
        
        // Create subject_in_project.tsv
        String subjectInProjectTsv = """
            project\tsubject
            scge.org:project-001\tscge.org:subject-001
            scge.org:project-001\tscge.org:subject-002
            scge.org:project-002\tscge.org:subject-003
            scge.org:project-002\tscge.org:subject-004
            scge.org:project-003\tscge.org:subject-005
            """;
        Files.writeString(dataDir.resolve("subject_in_project.tsv"), subjectInProjectTsv);
        
        // Create biosample_from_subject.tsv
        String biosampleFromSubjectTsv = """
            biosample\tsubject
            scge.org:biosample-001\tscge.org:subject-001
            scge.org:biosample-002\tscge.org:subject-002
            scge.org:biosample-003\tscge.org:subject-003
            scge.org:biosample-004\tscge.org:subject-004
            """;
        Files.writeString(dataDir.resolve("biosample_from_subject.tsv"), biosampleFromSubjectTsv);
        
        // Create validation report
        String validationReport = """
            {
              "validation_summary": {
                "is_valid": true,
                "total_errors": 0,
                "total_warnings": 3,
                "total_info": 2,
                "validated_at": "2024-01-15T14:30:00"
              },
              "entity_validation": {
                "project": {
                  "total_entities": 3,
                  "valid_entities": 3,
                  "invalid_entities": 0,
                  "warnings": 1
                },
                "subject": {
                  "total_entities": 5,
                  "valid_entities": 5,
                  "invalid_entities": 0,
                  "warnings": 1
                },
                "biosample": {
                  "total_entities": 4,
                  "valid_entities": 4,
                  "invalid_entities": 0,
                  "warnings": 1
                }
              },
              "relationship_validation": {
                "subject_in_project": {
                  "total_relationships": 5,
                  "valid_relationships": 5,
                  "orphaned_references": 0
                },
                "biosample_from_subject": {
                  "total_relationships": 4,
                  "valid_relationships": 4,
                  "orphaned_references": 0
                }
              },
              "recommendations": [
                "Consider adding more descriptive names for projects",
                "Add tissue type information to biosamples where applicable"
              ]
            }
            """;
        Files.writeString(dataDir.resolve("validation_summary.json"), validationReport);
        
        logger.info("Sample data files created successfully");
    }
}