package org.scge.c2m2.model.c2m2;

/**
 * Represents a C2M2 Biosample entity.
 * Corresponds to the biosample.tsv table in C2M2 specification.
 */
public record C2M2Biosample(
    String idNamespace,
    String localId,
    String persistentId,
    String creationTime,
    String name,
    String description,
    String ageAtSampling,
    String anatomy,
    String disease,
    String subjectLocalId
) {
    
    /**
     * Builder for creating C2M2Biosample instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Gets the globally unique identifier for this biosample.
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
        private String ageAtSampling;
        private String anatomy;
        private String disease;
        private String subjectLocalId;
        
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
        
        public Builder ageAtSampling(String ageAtSampling) {
            this.ageAtSampling = ageAtSampling;
            return this;
        }
        
        public Builder anatomy(String anatomy) {
            this.anatomy = anatomy;
            return this;
        }
        
        public Builder disease(String disease) {
            this.disease = disease;
            return this;
        }
        
        public Builder subjectLocalId(String subjectLocalId) {
            this.subjectLocalId = subjectLocalId;
            return this;
        }
        
        public C2M2Biosample build() {
            return new C2M2Biosample(
                idNamespace,
                localId,
                persistentId,
                creationTime,
                name,
                description,
                ageAtSampling,
                anatomy,
                disease,
                subjectLocalId
            );
        }
    }
}