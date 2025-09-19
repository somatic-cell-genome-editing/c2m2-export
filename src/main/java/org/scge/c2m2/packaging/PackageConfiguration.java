package org.scge.c2m2.packaging;

import java.nio.file.Path;
import java.util.*;

/**
 * Configuration for creating C2M2 submission packages.
 * Defines the inputs, outputs, and settings for package generation.
 */
public class PackageConfiguration {
    
    private final String packageId;
    private final Path outputDirectory;
    private final String submissionTitle;
    private final String submissionDescription;
    private final String organization;
    private final String contactEmail;
    
    // File mappings
    private final Map<String, Path> dataFiles;
    private final Map<String, Path> associationFiles;
    private final List<Path> validationReports;
    private final Map<String, Path> additionalFiles;
    
    // Package settings
    private final boolean createArchive;
    private final boolean validatePackage;
    private final boolean generateReadme;
    
    private PackageConfiguration(Builder builder) {
        this.packageId = builder.packageId;
        this.outputDirectory = builder.outputDirectory;
        this.submissionTitle = builder.submissionTitle;
        this.submissionDescription = builder.submissionDescription;
        this.organization = builder.organization;
        this.contactEmail = builder.contactEmail;
        this.dataFiles = Map.copyOf(builder.dataFiles);
        this.associationFiles = Map.copyOf(builder.associationFiles);
        this.validationReports = List.copyOf(builder.validationReports);
        this.additionalFiles = Map.copyOf(builder.additionalFiles);
        this.createArchive = builder.createArchive;
        this.validatePackage = builder.validatePackage;
        this.generateReadme = builder.generateReadme;
    }
    
    /**
     * Creates a new builder for package configuration.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a basic configuration with minimal required settings.
     */
    public static PackageConfiguration basic(String packageId, Path outputDirectory, 
                                           String title, String description) {
        return builder()
            .packageId(packageId)
            .outputDirectory(outputDirectory)
            .submissionTitle(title)
            .submissionDescription(description)
            .build();
    }
    
    // Getters
    public String getPackageId() { return packageId; }
    public Path getOutputDirectory() { return outputDirectory; }
    public String getSubmissionTitle() { return submissionTitle; }
    public String getSubmissionDescription() { return submissionDescription; }
    public String getOrganization() { return organization; }
    public String getContactEmail() { return contactEmail; }
    public Map<String, Path> getDataFiles() { return dataFiles; }
    public Map<String, Path> getAssociationFiles() { return associationFiles; }
    public List<Path> getValidationReports() { return validationReports; }
    public Map<String, Path> getAdditionalFiles() { return additionalFiles; }
    public boolean isCreateArchive() { return createArchive; }
    public boolean isValidatePackage() { return validatePackage; }
    public boolean isGenerateReadme() { return generateReadme; }
    
    /**
     * Gets the total number of files that will be included in the package.
     */
    public int getTotalFileCount() {
        return dataFiles.size() + associationFiles.size() + 
               validationReports.size() + additionalFiles.size() + 
               2; // manifest.json and README.md
    }
    
    /**
     * Validates the configuration for completeness.
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        // Required fields
        if (packageId == null || packageId.trim().isEmpty()) {
            errors.add("Package ID is required");
        }
        if (outputDirectory == null) {
            errors.add("Output directory is required");
        }
        if (submissionTitle == null || submissionTitle.trim().isEmpty()) {
            errors.add("Submission title is required");
        }
        
        // Recommendations
        if (submissionDescription == null || submissionDescription.trim().isEmpty()) {
            warnings.add("Submission description is recommended");
        }
        if (organization == null || organization.trim().isEmpty()) {
            warnings.add("Organization information is recommended");
        }
        if (contactEmail == null || contactEmail.trim().isEmpty()) {
            warnings.add("Contact email is recommended");
        }
        
        // File checks
        if (dataFiles.isEmpty()) {
            warnings.add("No data files specified - package will contain no C2M2 data");
        }
        
        // Validate file paths exist
        for (Map.Entry<String, Path> entry : dataFiles.entrySet()) {
            if (!java.nio.file.Files.exists(entry.getValue())) {
                errors.add("Data file does not exist: " + entry.getValue());
            }
        }
        
        for (Map.Entry<String, Path> entry : associationFiles.entrySet()) {
            if (!java.nio.file.Files.exists(entry.getValue())) {
                errors.add("Association file does not exist: " + entry.getValue());
            }
        }
        
        for (Path reportFile : validationReports) {
            if (!java.nio.file.Files.exists(reportFile)) {
                warnings.add("Validation report file does not exist: " + reportFile);
            }
        }
        
        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings, Map.of(
            "total_files", getTotalFileCount(),
            "data_files", dataFiles.size(),
            "association_files", associationFiles.size(),
            "validation_reports", validationReports.size()
        ));
    }
    
    @Override
    public String toString() {
        return String.format(
            "PackageConfiguration{id='%s', files=%d, output='%s'}",
            packageId, getTotalFileCount(), outputDirectory
        );
    }
    
    /**
     * Builder for creating package configurations.
     */
    public static class Builder {
        private String packageId;
        private Path outputDirectory;
        private String submissionTitle;
        private String submissionDescription;
        private String organization;
        private String contactEmail;
        
        private final Map<String, Path> dataFiles = new HashMap<>();
        private final Map<String, Path> associationFiles = new HashMap<>();
        private final List<Path> validationReports = new ArrayList<>();
        private final Map<String, Path> additionalFiles = new HashMap<>();
        
        private boolean createArchive = true;
        private boolean validatePackage = true;
        private boolean generateReadme = true;
        
        public Builder packageId(String packageId) {
            this.packageId = packageId;
            return this;
        }
        
        public Builder outputDirectory(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }
        
        public Builder submissionTitle(String submissionTitle) {
            this.submissionTitle = submissionTitle;
            return this;
        }
        
        public Builder submissionDescription(String submissionDescription) {
            this.submissionDescription = submissionDescription;
            return this;
        }
        
        public Builder organization(String organization) {
            this.organization = organization;
            return this;
        }
        
        public Builder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }
        
        public Builder addDataFile(String tableType, Path filePath) {
            this.dataFiles.put(tableType, filePath);
            return this;
        }
        
        public Builder addDataFiles(Map<String, Path> dataFiles) {
            this.dataFiles.putAll(dataFiles);
            return this;
        }
        
        public Builder addAssociationFile(String associationType, Path filePath) {
            this.associationFiles.put(associationType, filePath);
            return this;
        }
        
        public Builder addAssociationFiles(Map<String, Path> associationFiles) {
            this.associationFiles.putAll(associationFiles);
            return this;
        }
        
        public Builder addValidationReport(Path reportPath) {
            this.validationReports.add(reportPath);
            return this;
        }
        
        public Builder addValidationReports(List<Path> reportPaths) {
            this.validationReports.addAll(reportPaths);
            return this;
        }
        
        public Builder addAdditionalFile(String relativePath, Path filePath) {
            this.additionalFiles.put(relativePath, filePath);
            return this;
        }
        
        public Builder addAdditionalFiles(Map<String, Path> additionalFiles) {
            this.additionalFiles.putAll(additionalFiles);
            return this;
        }
        
        public Builder createArchive(boolean createArchive) {
            this.createArchive = createArchive;
            return this;
        }
        
        public Builder validatePackage(boolean validatePackage) {
            this.validatePackage = validatePackage;
            return this;
        }
        
        public Builder generateReadme(boolean generateReadme) {
            this.generateReadme = generateReadme;
            return this;
        }
        
        public PackageConfiguration build() {
            return new PackageConfiguration(this);
        }
    }
    
    /**
     * Simple validation result for configuration validation.
     */
    public record ValidationResult(
        boolean isValid,
        List<String> errors,
        List<String> warnings,
        Map<String, Object> metadata
    ) {
        public boolean hasIssues() {
            return !errors.isEmpty() || !warnings.isEmpty();
        }
        
        public String getSummary() {
            return String.format("Configuration validation: %s (errors: %d, warnings: %d)",
                                isValid ? "VALID" : "INVALID", errors.size(), warnings.size());
        }
    }
}