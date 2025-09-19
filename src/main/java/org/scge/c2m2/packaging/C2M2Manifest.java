package org.scge.c2m2.packaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.*;

/**
 * C2M2 submission manifest containing metadata about the submission package.
 * Based on the C2M2 specification for submission metadata.
 */
public class C2M2Manifest {
    
    @JsonProperty("manifest_version")
    private String manifestVersion = "1.0";
    
    @JsonProperty("submission_id")
    private String submissionId;
    
    @JsonProperty("submission_title")
    private String submissionTitle;
    
    @JsonProperty("submission_description")
    private String submissionDescription;
    
    @JsonProperty("data_coordinating_center")
    private String dataCoordinatingCenter = "SCGE";
    
    @JsonProperty("organization")
    private String organization;
    
    @JsonProperty("contact_email")
    private String contactEmail;
    
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonProperty("c2m2_version")
    private String c2m2Version = "2.0";
    
    @JsonProperty("generator_info")
    private GeneratorInfo generatorInfo;
    
    @JsonProperty("files")
    private List<FileInfo> files;
    
    @JsonProperty("tables")
    private Map<String, TableInfo> tables;
    
    @JsonProperty("associations")
    private Map<String, AssociationInfo> associations;
    
    @JsonProperty("validation_info")
    private ValidationInfo validationInfo;
    
    public C2M2Manifest() {
        this.createdAt = LocalDateTime.now();
        this.files = new ArrayList<>();
        this.tables = new HashMap<>();
        this.associations = new HashMap<>();
        this.generatorInfo = new GeneratorInfo();
        this.validationInfo = new ValidationInfo();
    }
    
    public C2M2Manifest(String submissionId, String title, String description) {
        this();
        this.submissionId = submissionId;
        this.submissionTitle = title;
        this.submissionDescription = description;
    }
    
    /**
     * Adds a file to the manifest.
     */
    public void addFile(C2M2Package.PackageFile packageFile) {
        FileInfo fileInfo = new FileInfo(
            packageFile.relativePath(),
            packageFile.fileType(),
            packageFile.description(),
            packageFile.getFileSize()
        );
        files.add(fileInfo);
    }
    
    /**
     * Adds table information to the manifest.
     */
    public void addTable(String tableName, int recordCount, String description) {
        tables.put(tableName, new TableInfo(tableName, recordCount, description));
    }
    
    /**
     * Adds association information to the manifest.
     */
    public void addAssociation(String associationName, int recordCount, String description) {
        associations.put(associationName, new AssociationInfo(associationName, recordCount, description));
    }
    
    /**
     * Sets validation information.
     */
    public void setValidationInfo(boolean isValid, int errorCount, int warningCount, List<String> reportFiles) {
        this.validationInfo = new ValidationInfo(isValid, errorCount, warningCount, reportFiles);
    }
    
    /**
     * Gets the total number of records across all tables.
     */
    public int getTotalRecords() {
        return tables.values().stream().mapToInt(TableInfo::recordCount).sum() +
               associations.values().stream().mapToInt(AssociationInfo::recordCount).sum();
    }
    
    /**
     * Gets the total number of files.
     */
    public int getTotalFiles() {
        return files.size();
    }
    
    /**
     * Gets the total size of all files.
     */
    public long getTotalSize() {
        return files.stream().mapToLong(FileInfo::sizeBytes).sum();
    }
    
    // Getters and setters
    public String getManifestVersion() { return manifestVersion; }
    public void setManifestVersion(String manifestVersion) { this.manifestVersion = manifestVersion; }
    
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    
    public String getSubmissionTitle() { return submissionTitle; }
    public void setSubmissionTitle(String submissionTitle) { this.submissionTitle = submissionTitle; }
    
    public String getSubmissionDescription() { return submissionDescription; }
    public void setSubmissionDescription(String submissionDescription) { this.submissionDescription = submissionDescription; }
    
    public String getDataCoordinatingCenter() { return dataCoordinatingCenter; }
    public void setDataCoordinatingCenter(String dataCoordinatingCenter) { this.dataCoordinatingCenter = dataCoordinatingCenter; }
    
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getC2m2Version() { return c2m2Version; }
    public void setC2m2Version(String c2m2Version) { this.c2m2Version = c2m2Version; }
    
    public GeneratorInfo getGeneratorInfo() { return generatorInfo; }
    public void setGeneratorInfo(GeneratorInfo generatorInfo) { this.generatorInfo = generatorInfo; }
    
    public List<FileInfo> getFiles() { return files; }
    public void setFiles(List<FileInfo> files) { this.files = files; }
    
    public Map<String, TableInfo> getTables() { return tables; }
    public void setTables(Map<String, TableInfo> tables) { this.tables = tables; }
    
    public Map<String, AssociationInfo> getAssociations() { return associations; }
    public void setAssociations(Map<String, AssociationInfo> associations) { this.associations = associations; }
    
    public ValidationInfo getValidationInfo() { return validationInfo; }
    public void setValidationInfo(ValidationInfo validationInfo) { this.validationInfo = validationInfo; }
    
    // Nested classes for manifest components
    
    public static class GeneratorInfo {
        @JsonProperty("name")
        private String name = "SCGE C2M2 Submission Generator";
        
        @JsonProperty("version")
        private String version = "1.0.0";
        
        @JsonProperty("source_database")
        private String sourceDatabase = "SCGE PostgreSQL";
        
        @JsonProperty("generation_time")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime generationTime = LocalDateTime.now();
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getSourceDatabase() { return sourceDatabase; }
        public void setSourceDatabase(String sourceDatabase) { this.sourceDatabase = sourceDatabase; }
        
        public LocalDateTime getGenerationTime() { return generationTime; }
        public void setGenerationTime(LocalDateTime generationTime) { this.generationTime = generationTime; }
    }
    
    public record FileInfo(
        @JsonProperty("path") String path,
        @JsonProperty("type") String type,
        @JsonProperty("description") String description,
        @JsonProperty("size_bytes") long sizeBytes
    ) {}
    
    public record TableInfo(
        @JsonProperty("name") String name,
        @JsonProperty("record_count") int recordCount,
        @JsonProperty("description") String description
    ) {}
    
    public record AssociationInfo(
        @JsonProperty("name") String name,
        @JsonProperty("record_count") int recordCount,
        @JsonProperty("description") String description
    ) {}
    
    public static class ValidationInfo {
        @JsonProperty("is_valid")
        private boolean isValid = true;
        
        @JsonProperty("error_count")
        private int errorCount = 0;
        
        @JsonProperty("warning_count")
        private int warningCount = 0;
        
        @JsonProperty("validation_reports")
        private List<String> validationReports = new ArrayList<>();
        
        @JsonProperty("validated_at")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime validatedAt = LocalDateTime.now();
        
        public ValidationInfo() {}
        
        public ValidationInfo(boolean isValid, int errorCount, int warningCount, List<String> reportFiles) {
            this.isValid = isValid;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.validationReports = reportFiles != null ? new ArrayList<>(reportFiles) : new ArrayList<>();
            this.validatedAt = LocalDateTime.now();
        }
        
        // Getters and setters
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        
        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }
        
        public List<String> getValidationReports() { return validationReports; }
        public void setValidationReports(List<String> validationReports) { this.validationReports = validationReports; }
        
        public LocalDateTime getValidatedAt() { return validatedAt; }
        public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }
    }
}