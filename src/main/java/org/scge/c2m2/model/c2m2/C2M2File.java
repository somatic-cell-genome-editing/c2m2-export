package org.scge.c2m2.model.c2m2;

/**
 * Represents a C2M2 File entity.
 * Corresponds to the file.tsv table in C2M2 specification.
 */
public record C2M2File(
    String idNamespace,
    String localId,
    String persistentId,
    String creationTime,
    String filename,
    String fileFormat,
    String compressionFormat,
    String dataType,
    String assayType,
    String analysisType,
    String mimeType,
    Long sizeInBytes,
    String sha256,
    String md5,
    String description
) {
    
    /**
     * Builder for creating C2M2File instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Gets the globally unique identifier for this file.
     */
    public String getGlobalId() {
        return idNamespace + ":" + localId;
    }
    
    public static class Builder {
        private String idNamespace;
        private String localId;
        private String persistentId;
        private String creationTime;
        private String filename;
        private String fileFormat;
        private String compressionFormat;
        private String dataType;
        private String assayType;
        private String analysisType;
        private String mimeType;
        private Long sizeInBytes;
        private String sha256;
        private String md5;
        private String description;
        
        public Builder idNamespace(String idNamespace) {
            this.idNamespace = idNamespace;
            return this;
        }
        
        public Builder localId(String localId) {
            this.localId = localId;
            return this;
        }
        
        public Builder persistentId(String persistentId) {
            this.persistentId = persistentId;
            return this;
        }
        
        public Builder creationTime(String creationTime) {
            this.creationTime = creationTime;
            return this;
        }
        
        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }
        
        public Builder fileFormat(String fileFormat) {
            this.fileFormat = fileFormat;
            return this;
        }
        
        public Builder compressionFormat(String compressionFormat) {
            this.compressionFormat = compressionFormat;
            return this;
        }
        
        public Builder dataType(String dataType) {
            this.dataType = dataType;
            return this;
        }
        
        public Builder assayType(String assayType) {
            this.assayType = assayType;
            return this;
        }
        
        public Builder analysisType(String analysisType) {
            this.analysisType = analysisType;
            return this;
        }
        
        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }
        
        public Builder sizeInBytes(Long sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
            return this;
        }
        
        public Builder sha256(String sha256) {
            this.sha256 = sha256;
            return this;
        }
        
        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public C2M2File build() {
            return new C2M2File(
                idNamespace,
                localId,
                persistentId,
                creationTime,
                filename,
                fileFormat,
                compressionFormat,
                dataType,
                assayType,
                analysisType,
                mimeType,
                sizeInBytes,
                sha256,
                md5,
                description
            );
        }
    }
}