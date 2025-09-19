package org.scge.c2m2.packaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.scge.c2m2.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for creating and managing C2M2 submission packages.
 * Handles the complete packaging workflow from TSV files to submission-ready packages.
 */
@Service
public class PackagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(PackagingService.class);
    
    private final ObjectMapper objectMapper;
    
    public PackagingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        logger.info("PackagingService initialized");
    }
    
    /**
     * Creates a complete C2M2 submission package from generated TSV files.
     */
    public C2M2Package createSubmissionPackage(PackageConfiguration config) throws IOException {
        logger.info("Creating C2M2 submission package: {}", config.getPackageId());
        
        // Create package directory structure
        Path packagePath = createPackageDirectory(config);
        
        // Create manifest
        C2M2Manifest manifest = createManifest(config);
        
        // Create package instance
        C2M2Package c2m2Package = new C2M2Package(config.getPackageId(), packagePath, manifest);
        
        // Copy and organize files
        copyDataFiles(config, c2m2Package, packagePath);
        copyAssociationFiles(config, c2m2Package, packagePath);
        copyValidationReports(config, c2m2Package, packagePath);
        copyAdditionalFiles(config, c2m2Package, packagePath);
        
        // Generate manifest file
        writeManifest(manifest, packagePath);
        c2m2Package.addFile("manifest.json", packagePath.resolve("manifest.json"), 
                           "metadata", "C2M2 submission manifest");
        
        // Generate README
        generateReadme(c2m2Package, packagePath);
        c2m2Package.addFile("README.md", packagePath.resolve("README.md"), 
                           "documentation", "Package documentation");
        
        // Update manifest with final file list
        updateManifestWithPackageInfo(manifest, c2m2Package);
        writeManifest(manifest, packagePath);
        
        logger.info("C2M2 package created successfully: {} files, {} total records", 
                   c2m2Package.getFileCount(), c2m2Package.getMetadata().getTotalRecords());
        
        return c2m2Package;
    }
    
    /**
     * Creates a ZIP archive of the submission package.
     */
    public Path createPackageArchive(C2M2Package c2m2Package, Path outputPath) throws IOException {
        logger.info("Creating package archive: {}", outputPath);
        
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(outputPath))) {
            for (C2M2Package.PackageFile file : c2m2Package.getFiles().values()) {
                if (file.exists()) {
                    ZipEntry entry = new ZipEntry(file.relativePath());
                    zipOut.putNextEntry(entry);
                    Files.copy(file.absolutePath(), zipOut);
                    zipOut.closeEntry();
                    logger.debug("Added to archive: {}", file.relativePath());
                }
            }
        }
        
        logger.info("Package archive created: {} (size: {} bytes)", 
                   outputPath, Files.size(outputPath));
        
        return outputPath;
    }
    
    /**
     * Validates package completeness and integrity.
     */
    public PackageValidationResult validatePackage(C2M2Package c2m2Package) {
        logger.info("Validating package: {}", c2m2Package.getPackageId());
        
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Check required files
        if (!c2m2Package.getFiles().containsKey("manifest.json")) {
            errors.add("Missing required manifest.json file");
        }
        
        if (c2m2Package.getFilesByType("data").isEmpty()) {
            errors.add("No data files found in package");
        }
        
        // Check file existence
        for (C2M2Package.PackageFile file : c2m2Package.getFiles().values()) {
            if (!file.exists()) {
                errors.add("File does not exist: " + file.relativePath());
            }
        }
        
        // Check manifest consistency
        C2M2Manifest manifest = c2m2Package.getManifest();
        if (manifest.getTotalFiles() != c2m2Package.getFileCount()) {
            warnings.add("Manifest file count mismatch");
        }
        
        // Check for recommended files
        if (!c2m2Package.getFiles().containsKey("README.md")) {
            warnings.add("Missing recommended README.md file");
        }
        
        boolean isValid = errors.isEmpty();
        
        logger.info("Package validation completed: {} (errors: {}, warnings: {})", 
                   isValid ? "VALID" : "INVALID", errors.size(), warnings.size());
        
        return new PackageValidationResult(isValid, errors, warnings);
    }
    
    /**
     * Creates the package directory structure.
     */
    private Path createPackageDirectory(PackageConfiguration config) throws IOException {
        Path packagePath = config.getOutputDirectory().resolve(config.getPackageId());
        
        // Create main directory
        Files.createDirectories(packagePath);
        
        // Create subdirectories
        Files.createDirectories(packagePath.resolve("data"));
        Files.createDirectories(packagePath.resolve("associations"));
        Files.createDirectories(packagePath.resolve("validation"));
        Files.createDirectories(packagePath.resolve("docs"));
        
        logger.debug("Created package directory structure: {}", packagePath);
        return packagePath;
    }
    
    /**
     * Creates the initial manifest.
     */
    private C2M2Manifest createManifest(PackageConfiguration config) {
        C2M2Manifest manifest = new C2M2Manifest(
            config.getPackageId(),
            config.getSubmissionTitle(),
            config.getSubmissionDescription()
        );
        
        manifest.setOrganization(config.getOrganization());
        manifest.setContactEmail(config.getContactEmail());
        
        return manifest;
    }
    
    /**
     * Copies data files to the package.
     */
    private void copyDataFiles(PackageConfiguration config, C2M2Package c2m2Package, Path packagePath) 
            throws IOException {
        
        Path dataDir = packagePath.resolve("data");
        
        for (Map.Entry<String, Path> entry : config.getDataFiles().entrySet()) {
            String tableType = entry.getKey();
            Path sourceFile = entry.getValue();
            
            if (Files.exists(sourceFile)) {
                String fileName = tableType + ".tsv";
                Path targetFile = dataDir.resolve(fileName);
                
                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                
                int recordCount = countRecords(sourceFile);
                c2m2Package.addDataFile("data/" + fileName, targetFile, tableType, recordCount);
                
                logger.debug("Copied data file: {} ({} records)", fileName, recordCount);
            }
        }
    }
    
    /**
     * Copies association files to the package.
     */
    private void copyAssociationFiles(PackageConfiguration config, C2M2Package c2m2Package, Path packagePath) 
            throws IOException {
        
        Path associationsDir = packagePath.resolve("associations");
        
        for (Map.Entry<String, Path> entry : config.getAssociationFiles().entrySet()) {
            String associationType = entry.getKey();
            Path sourceFile = entry.getValue();
            
            if (Files.exists(sourceFile)) {
                String fileName = associationType + ".tsv";
                Path targetFile = associationsDir.resolve(fileName);
                
                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                
                int recordCount = countRecords(sourceFile);
                c2m2Package.addAssociationFile("associations/" + fileName, targetFile, 
                                             associationType, recordCount);
                
                logger.debug("Copied association file: {} ({} records)", fileName, recordCount);
            }
        }
    }
    
    /**
     * Copies validation reports to the package.
     */
    private void copyValidationReports(PackageConfiguration config, C2M2Package c2m2Package, Path packagePath) 
            throws IOException {
        
        Path validationDir = packagePath.resolve("validation");
        
        for (Path reportFile : config.getValidationReports()) {
            if (Files.exists(reportFile)) {
                String fileName = reportFile.getFileName().toString();
                Path targetFile = validationDir.resolve(fileName);
                
                Files.copy(reportFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                c2m2Package.addValidationReport("validation/" + fileName);
                
                logger.debug("Copied validation report: {}", fileName);
            }
        }
    }
    
    /**
     * Copies additional files to the package.
     */
    private void copyAdditionalFiles(PackageConfiguration config, C2M2Package c2m2Package, Path packagePath) 
            throws IOException {
        
        for (Map.Entry<String, Path> entry : config.getAdditionalFiles().entrySet()) {
            String relativePath = entry.getKey();
            Path sourceFile = entry.getValue();
            
            if (Files.exists(sourceFile)) {
                Path targetFile = packagePath.resolve(relativePath);
                Files.createDirectories(targetFile.getParent());
                
                Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                c2m2Package.addFile(relativePath, targetFile, "additional", 
                                  "Additional package file");
                
                logger.debug("Copied additional file: {}", relativePath);
            }
        }
    }
    
    /**
     * Writes the manifest to a JSON file.
     */
    private void writeManifest(C2M2Manifest manifest, Path packagePath) throws IOException {
        Path manifestFile = packagePath.resolve("manifest.json");
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(manifestFile.toFile(), manifest);
        logger.debug("Wrote manifest file: {}", manifestFile);
    }
    
    /**
     * Updates the manifest with final package information.
     */
    private void updateManifestWithPackageInfo(C2M2Manifest manifest, C2M2Package c2m2Package) {
        // Add table information
        PackageMetadata metadata = c2m2Package.getMetadata();
        for (Map.Entry<String, Integer> entry : metadata.getTableCounts().entrySet()) {
            manifest.addTable(entry.getKey(), entry.getValue(), 
                            "C2M2 " + entry.getKey() + " table");
        }
        
        // Add association information
        for (Map.Entry<String, Integer> entry : metadata.getAssociationCounts().entrySet()) {
            manifest.addAssociation(entry.getKey(), entry.getValue(), 
                                  "C2M2 " + entry.getKey() + " association");
        }
    }
    
    /**
     * Generates a README file for the package.
     */
    private void generateReadme(C2M2Package c2m2Package, Path packagePath) throws IOException {
        StringBuilder readme = new StringBuilder();
        
        readme.append("# C2M2 Submission Package\n\n");
        readme.append("**Package ID:** ").append(c2m2Package.getPackageId()).append("\n");
        readme.append("**Created:** ").append(c2m2Package.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
        readme.append("**Generator:** SCGE C2M2 Submission Generator\n\n");
        
        readme.append("## Package Contents\n\n");
        
        C2M2Package.PackageSummary summary = c2m2Package.getSummary();
        readme.append("- **Total Files:** ").append(summary.totalFiles()).append("\n");
        readme.append("- **Total Size:** ").append(summary.getFormattedSize()).append("\n");
        readme.append("- **Total Records:** ").append(summary.totalRecords()).append("\n");
        readme.append("- **Package Complete:** ").append(summary.isComplete() ? "Yes" : "No").append("\n\n");
        
        readme.append("### File Types\n\n");
        for (Map.Entry<String, Integer> entry : summary.filesByType().entrySet()) {
            readme.append("- **").append(entry.getKey()).append(":** ").append(entry.getValue()).append(" files\n");
        }
        
        readme.append("\n## Directory Structure\n\n");
        readme.append("```\n");
        readme.append(c2m2Package.getPackageId()).append("/\n");
        readme.append("├── manifest.json          # Package manifest and metadata\n");
        readme.append("├── README.md              # This file\n");
        readme.append("├── data/                  # C2M2 entity tables\n");
        readme.append("├── associations/          # C2M2 association tables\n");
        readme.append("├── validation/            # Validation reports\n");
        readme.append("└── docs/                  # Additional documentation\n");
        readme.append("```\n\n");
        
        readme.append("## Usage\n\n");
        readme.append("This package contains C2M2-compliant data generated from the SCGE database. ");
        readme.append("The manifest.json file contains detailed metadata about all included files. ");
        readme.append("Validation reports in the validation/ directory provide information about data quality.\n\n");
        
        readme.append("For more information about the C2M2 standard, visit: https://info.cfde.cloud/documentation/C2M2\n");
        
        Path readmeFile = packagePath.resolve("README.md");
        Files.writeString(readmeFile, readme.toString());
        
        logger.debug("Generated README file: {}", readmeFile);
    }
    
    /**
     * Counts the number of records in a TSV file (excluding header).
     */
    private int countRecords(Path file) {
        try {
            long lineCount = Files.lines(file).count();
            return Math.max(0, (int) lineCount - 1); // Subtract header line
        } catch (IOException e) {
            logger.warn("Could not count records in file: {}", file, e);
            return 0;
        }
    }
    
    /**
     * Result of package validation.
     */
    public record PackageValidationResult(
        boolean isValid,
        List<String> errors,
        List<String> warnings
    ) {
        public boolean hasIssues() {
            return !errors.isEmpty() || !warnings.isEmpty();
        }
        
        public String getSummary() {
            return String.format("Package validation: %s (errors: %d, warnings: %d)", 
                                isValid ? "VALID" : "INVALID", errors.size(), warnings.size());
        }
    }
}