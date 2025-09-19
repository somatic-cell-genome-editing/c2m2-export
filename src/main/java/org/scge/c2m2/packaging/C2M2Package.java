package org.scge.c2m2.packaging;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a complete C2M2 submission package.
 * Contains all necessary files, metadata, and validation information for submission.
 */
public class C2M2Package {
    
    private final String packageId;
    private final LocalDateTime createdAt;
    private final Path packagePath;
    private final C2M2Manifest manifest;
    private final Map<String, PackageFile> files;
    private final PackageMetadata metadata;
    private final List<String> validationReports;
    
    public C2M2Package(String packageId, Path packagePath, C2M2Manifest manifest) {
        this.packageId = packageId;
        this.createdAt = LocalDateTime.now();
        this.packagePath = packagePath;
        this.manifest = manifest;
        this.files = new LinkedHashMap<>();
        this.metadata = new PackageMetadata();
        this.validationReports = new ArrayList<>();
    }
    
    /**
     * Adds a file to the package.
     */
    public void addFile(String relativePath, Path absolutePath, String fileType, String description) {
        PackageFile packageFile = new PackageFile(relativePath, absolutePath, fileType, description);
        files.put(relativePath, packageFile);
        manifest.addFile(packageFile);
    }
    
    /**
     * Adds a TSV data file to the package.
     */
    public void addDataFile(String fileName, Path filePath, String tableType, int recordCount) {
        addFile(fileName, filePath, "data", 
               String.format("C2M2 %s table with %d records", tableType, recordCount));
        metadata.addTableInfo(tableType, recordCount);
    }
    
    /**
     * Adds an association file to the package.
     */
    public void addAssociationFile(String fileName, Path filePath, String associationType, int recordCount) {
        addFile(fileName, filePath, "association", 
               String.format("C2M2 %s association with %d records", associationType, recordCount));
        metadata.addAssociationInfo(associationType, recordCount);
    }
    
    /**
     * Adds a validation report to the package.
     */
    public void addValidationReport(String reportPath) {
        validationReports.add(reportPath);
        addFile(reportPath, packagePath.resolve(reportPath), "validation", "Validation report");
    }
    
    /**
     * Gets all files in the package.
     */
    public Map<String, PackageFile> getFiles() {
        return new HashMap<>(files);
    }
    
    /**
     * Gets files of a specific type.
     */
    public List<PackageFile> getFilesByType(String fileType) {
        return files.values().stream()
                   .filter(file -> fileType.equals(file.fileType()))
                   .toList();
    }
    
    /**
     * Gets the total number of files in the package.
     */
    public int getFileCount() {
        return files.size();
    }
    
    /**
     * Gets the total size of all files in bytes.
     */
    public long getTotalSize() {
        return files.values().stream()
                   .mapToLong(PackageFile::getFileSize)
                   .sum();
    }
    
    /**
     * Checks if the package contains all required C2M2 files.
     */
    public boolean isComplete() {
        // Check for required C2M2 files
        boolean hasManifest = files.containsKey("manifest.json");
        boolean hasDataFiles = !getFilesByType("data").isEmpty();
        
        return hasManifest && hasDataFiles;
    }
    
    /**
     * Gets a summary of the package contents.
     */
    public PackageSummary getSummary() {
        Map<String, Integer> filesByType = new HashMap<>();
        for (PackageFile file : files.values()) {
            filesByType.merge(file.fileType(), 1, Integer::sum);
        }
        
        return new PackageSummary(
            packageId,
            createdAt,
            files.size(),
            getTotalSize(),
            filesByType,
            metadata.getTotalRecords(),
            isComplete()
        );
    }
    
    // Getters
    public String getPackageId() { return packageId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Path getPackagePath() { return packagePath; }
    public C2M2Manifest getManifest() { return manifest; }
    public PackageMetadata getMetadata() { return metadata; }
    public List<String> getValidationReports() { return new ArrayList<>(validationReports); }
    
    /**
     * Represents a single file within the package.
     */
    public record PackageFile(
        String relativePath,
        Path absolutePath,
        String fileType,
        String description
    ) {
        /**
         * Gets the file size in bytes.
         */
        public long getFileSize() {
            try {
                return java.nio.file.Files.size(absolutePath);
            } catch (Exception e) {
                return 0L;
            }
        }
        
        /**
         * Gets the file name without path.
         */
        public String getFileName() {
            return absolutePath.getFileName().toString();
        }
        
        /**
         * Checks if the file exists.
         */
        public boolean exists() {
            return java.nio.file.Files.exists(absolutePath);
        }
    }
    
    /**
     * Summary information about the package.
     */
    public record PackageSummary(
        String packageId,
        LocalDateTime createdAt,
        int totalFiles,
        long totalSizeBytes,
        Map<String, Integer> filesByType,
        int totalRecords,
        boolean isComplete
    ) {
        public String getFormattedSize() {
            if (totalSizeBytes < 1024) {
                return totalSizeBytes + " B";
            } else if (totalSizeBytes < 1024 * 1024) {
                return String.format("%.1f KB", totalSizeBytes / 1024.0);
            } else if (totalSizeBytes < 1024 * 1024 * 1024) {
                return String.format("%.1f MB", totalSizeBytes / (1024.0 * 1024.0));
            } else {
                return String.format("%.1f GB", totalSizeBytes / (1024.0 * 1024.0 * 1024.0));
            }
        }
        
        @Override
        public String toString() {
            return String.format(
                "C2M2Package{id='%s', files=%d (%s), records=%d, complete=%s}",
                packageId, totalFiles, getFormattedSize(), totalRecords, isComplete
            );
        }
    }
}