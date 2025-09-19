package org.scge.c2m2;

import org.scge.c2m2.database.DatabaseService;
import org.scge.c2m2.mapping.*;
import org.scge.c2m2.model.c2m2.*;
import org.scge.c2m2.model.scge.*;
import org.scge.c2m2.output.TableGenerationService;
import org.scge.c2m2.packaging.*;
import org.scge.c2m2.validation.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Main Spring Boot application for generating C2M2 submissions from SCGE PostgreSQL database.
 * 
 * Usage:
 * 1. Configure database connection in src/main/resources/application.yml
 * 2. Run: ./gradlew bootRun
 * 
 * The application will:
 * - Connect to your PostgreSQL database
 * - Extract SCGE data and map to C2M2 format
 * - Generate C2M2-compliant TSV files
 * - Create a complete submission package with validation
 * - Produce a ZIP archive ready for submission
 */
@SpringBootApplication
public class C2M2SubmissionGeneratorApplication {
    
    private static final Logger logger = LoggerFactory.getLogger(C2M2SubmissionGeneratorApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting SCGE C2M2 Submission Generator");
        SpringApplication.run(C2M2SubmissionGeneratorApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner run(
            DatabaseService databaseService,
            MappingOrchestrator mappingOrchestrator,
            TableGenerationService tableGenerationService,
            PackagingService packagingService,
            ValidationService validationService) {
        
        return args -> {
            try {
                logger.info("=== SCGE C2M2 Submission Generator Started ===");
                
                // Check database connectivity
                if (!databaseService.isHealthy()) {
                    logger.error("Database connection failed. Please check your configuration.");
                    return;
                }
                logger.info("✓ Database connection established");
                
                // Create output directories
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                Path outputDir = Paths.get("c2m2-output");
                Path submissionDir = outputDir.resolve("submission-" + timestamp);
                Path tsvDir = submissionDir.resolve("tsv-files");
                Path packageDir = outputDir.resolve("packages");
                
                Files.createDirectories(tsvDir);
                Files.createDirectories(packageDir);
                
                logger.info("Output directories created:");
                logger.info("  TSV files: {}", tsvDir);
                logger.info("  Packages: {}", packageDir);
                
                // Step 1: Extract and map data from database
                logger.info("=== Step 1: Extracting and mapping data from database ===");
                
                Map<Class<?>, List<?>> mappedData = extractAndMapData(databaseService, mappingOrchestrator);
                
                // Step 2: Generate TSV files
                logger.info("=== Step 2: Generating C2M2 TSV files ===");
                
                Map<String, Path> generatedFiles = generateTsvFiles(tableGenerationService, mappedData, tsvDir);
                
                // Step 3: Create C2M2 submission package
                logger.info("=== Step 3: Creating C2M2 submission package ===");
                
                C2M2Package submissionPackage = createSubmissionPackage(
                    packagingService, generatedFiles, packageDir, timestamp);
                
                // Step 4: Validate and finalize
                logger.info("=== Step 4: Validation and finalization ===");
                
                finalizeSubmission(packagingService, submissionPackage);
                
                logger.info("=== C2M2 Submission Generation Completed Successfully ===");
                logger.info("Package location: {}", submissionPackage.getPackagePath());
                logger.info("Archive location: {}", packageDir.resolve(submissionPackage.getPackageId() + ".zip"));
                
            } catch (Exception e) {
                logger.error("C2M2 submission generation failed: {}", e.getMessage(), e);
                System.exit(1);
            }
        };
    }
    
    private Map<Class<?>, List<?>> extractAndMapData(DatabaseService databaseService, 
                                                    MappingOrchestrator mappingOrchestrator) throws Exception {
        
        logger.info("Extracting SCGE data from database...");
        
        // Extract SCGE entities from database using the correct method signature
        List<Study> studies = databaseService.executeQueryForList(
            "SELECT * FROM study ORDER BY study_id LIMIT 10",
            rs -> new Study(
                rs.getInt("study_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("status"),
                null, // createdDate
                rs.getString("tier"),
                rs.getString("pi"),
                rs.getString("institution")
            )
        );
        
        List<Person> persons = databaseService.executeQueryForList(
            "SELECT * FROM person ORDER BY person_id LIMIT 10", 
            rs -> new Person(
                rs.getInt("person_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("organization"),
                rs.getString("role")
            )
        );
        
        List<Model> models = databaseService.executeQueryForList(
            "SELECT * FROM model ORDER BY model_id LIMIT 10",
            rs -> new Model(
                rs.getInt("model_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("species"),
                rs.getString("strain"),
                rs.getString("sex"),
                rs.getString("tissue_type"),
                rs.getString("developmental_stage")
            )
        );
        
        List<ExperimentRecord> experiments = databaseService.executeQueryForList(
            "SELECT * FROM experiment ORDER BY experiment_id LIMIT 10", // Limit for demo
            rs -> new ExperimentRecord(
                rs.getInt("experiment_id"),
                rs.getInt("study_id"),
                rs.getInt("model_id"),
                rs.getString("experiment_type"),
                rs.getString("description"),
                rs.getString("delivery_method"),
                rs.getString("target_gene"),
                rs.getString("editing_tool"),
                null, // experimentDate - will be null for now
                rs.getString("status"),
                rs.getString("tissue_type"),
                rs.getString("cell_type")
            )
        );
        
        logger.info("Extracted from database:");
        logger.info("  Studies: {}", studies.size());
        logger.info("  Persons: {}", persons.size());
        logger.info("  Models: {}", models.size());
        logger.info("  Experiments: {}", experiments.size());
        
        // Map SCGE entities to C2M2 entities
        logger.info("Mapping SCGE entities to C2M2 format...");
        
        List<C2M2Project> projects = mappingOrchestrator.mapEntities(studies);
        List<C2M2Subject> subjectsFromPersons = mappingOrchestrator.mapEntities(persons);
        List<C2M2Subject> subjectsFromModels = mappingOrchestrator.mapEntities(models);
        List<C2M2Biosample> biosamples = mappingOrchestrator.mapEntities(experiments);
        
        // Combine subjects from different sources
        List<C2M2Subject> allSubjects = new java.util.ArrayList<>(subjectsFromPersons);
        allSubjects.addAll(subjectsFromModels);
        
        logger.info("Mapped to C2M2 format:");
        logger.info("  Projects: {}", projects.size());
        logger.info("  Subjects: {}", allSubjects.size());
        logger.info("  Biosamples: {}", biosamples.size());
        
        return Map.of(
            C2M2Project.class, projects,
            C2M2Subject.class, allSubjects,
            C2M2Biosample.class, biosamples
        );
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Path> generateTsvFiles(TableGenerationService tableGenerationService,
                                             Map<Class<?>, List<?>> mappedData,
                                             Path outputDir) throws Exception {
        
        logger.info("Generating TSV files in: {}", outputDir);
        
        // Convert to the format expected by TableGenerationService
        Map<Class<?>, List<Object>> entityMap = new HashMap<>();
        for (Map.Entry<Class<?>, List<?>> entry : mappedData.entrySet()) {
            entityMap.put(entry.getKey(), new ArrayList<>((List<Object>) entry.getValue()));
        }
        
        // Generate all tables using the service
        TableGenerationService.GenerationResult result = 
            tableGenerationService.generateSpecificTables(entityMap, outputDir);
        
        logger.info("TSV generation result: {}", result);
        
        // Build the file map for packaging
        Map<String, Path> generatedFiles = new HashMap<>();
        for (TableGenerationService.TableStatistics stats : result.getGeneratedTables()) {
            String fileName = stats.fileName();
            String tableName = fileName.replace(".tsv", "");
            generatedFiles.put(tableName, outputDir.resolve(fileName));
            logger.info("  Generated {} with {} records", fileName, stats.validEntities());
        }
        
        if (result.hasErrors()) {
            logger.warn("TSV generation had errors:");
            result.getErrors().forEach((type, error) -> 
                logger.warn("  {}: {}", type, error));
        }
        
        logger.info("TSV generation completed. Generated {} files", generatedFiles.size());
        return generatedFiles;
    }
    
    private C2M2Package createSubmissionPackage(PackagingService packagingService,
                                               Map<String, Path> generatedFiles,
                                               Path packageDir,
                                               String timestamp) throws Exception {
        
        String packageId = "scge-submission-" + timestamp;
        
        PackageConfiguration.Builder configBuilder = PackageConfiguration.builder()
            .packageId(packageId)
            .outputDirectory(packageDir)
            .submissionTitle("SCGE C2M2 Data Submission " + timestamp)
            .submissionDescription("C2M2-compliant data submission generated from SCGE PostgreSQL database")
            .organization("Somatic Cell Genome Editing Program")
            .contactEmail("data@scge.org")
            .createArchive(true)
            .validatePackage(true)
            .generateReadme(true);
        
        // Add data files
        for (Map.Entry<String, Path> entry : generatedFiles.entrySet()) {
            String fileType = entry.getKey();
            Path filePath = entry.getValue();
            
            if (fileType.contains("_")) {
                // Association file
                configBuilder.addAssociationFile(fileType, filePath);
            } else {
                // Data file
                configBuilder.addDataFile(fileType, filePath);
            }
        }
        
        PackageConfiguration config = configBuilder.build();
        
        logger.info("Creating submission package: {}", packageId);
        C2M2Package submissionPackage = packagingService.createSubmissionPackage(config);
        
        logger.info("Package created successfully:");
        logger.info("  Package ID: {}", submissionPackage.getPackageId());
        logger.info("  Total files: {}", submissionPackage.getFileCount());
        logger.info("  Total records: {}", submissionPackage.getMetadata().getTotalRecords());
        logger.info("  Package size: {}", submissionPackage.getSummary().getFormattedSize());
        
        return submissionPackage;
    }
    
    private void finalizeSubmission(PackagingService packagingService, C2M2Package submissionPackage) 
            throws Exception {
        
        // Validate package
        PackagingService.PackageValidationResult validation = 
            packagingService.validatePackage(submissionPackage);
        
        logger.info("Package validation: {}", validation.getSummary());
        
        if (validation.hasIssues()) {
            validation.errors().forEach(error -> logger.error("  Error: {}", error));
            validation.warnings().forEach(warning -> logger.warn("  Warning: {}", warning));
        }
        
        if (validation.isValid()) {
            // Create ZIP archive
            Path archivePath = submissionPackage.getPackagePath()
                .getParent()
                .resolve(submissionPackage.getPackageId() + ".zip");
            
            Path createdArchive = packagingService.createPackageArchive(submissionPackage, archivePath);
            
            logger.info("Archive created: {}", createdArchive);
            logger.info("Archive size: {} bytes", Files.size(createdArchive));
            
            // Display final summary
            C2M2Package.PackageSummary summary = submissionPackage.getSummary();
            logger.info("Final package summary:");
            logger.info("  Total files: {}", summary.totalFiles());
            logger.info("  Total records: {}", summary.totalRecords());
            logger.info("  Package complete: {}", summary.isComplete());
            
            summary.filesByType().forEach((type, count) ->
                logger.info("  {}: {} files", type, count));
            
        } else {
            logger.error("Package validation failed - archive not created");
        }
    }
}