package org.scge.c2m2.model.c2m2;

/**
 * Represents a C2M2 Project entity.
 * Corresponds to the project.tsv table in C2M2 specification.
 */
public record C2M2Project(
    String idNamespace,
    String localId,
    String persistentId,
    String creationTime,
    String name,
    String description,
    String abbreviation
) {
    
    /**
     * Builder for creating C2M2Project instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Gets the globally unique identifier for this project.
     */
    public String getGlobalId() {
        return idNamespace + ":" + localId;
    }
    
    public static class Builder {
        private String idNamespace;
        private String localId;
        private String persistentId;
        private String creationTime;
        private String name;
        private String description;
        private String abbreviation;
        
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
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder abbreviation(String abbreviation) {
            this.abbreviation = abbreviation;
            return this;
        }
        
        public C2M2Project build() {
            return new C2M2Project(
                idNamespace,
                localId,
                persistentId,
                creationTime,
                name,
                description,
                abbreviation
            );
        }
    }
}