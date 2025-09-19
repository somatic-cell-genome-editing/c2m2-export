package org.scge.c2m2.model.c2m2;

/**
 * Represents a C2M2 Subject entity.
 * Corresponds to the subject.tsv table in C2M2 specification.
 */
public record C2M2Subject(
    String idNamespace,
    String localId,
    String persistentId,
    String creationTime,
    String granularity,
    String sex,
    String ethnicity,
    String race,
    String strain,
    String species,
    String ageAtCollection,
    String subjectRoleTaxonomy
) {
    
    /**
     * Builder for creating C2M2Subject instances.
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Gets the globally unique identifier for this subject.
     */
    public String getGlobalId() {
        return idNamespace + ":" + localId;
    }
    
    public static class Builder {
        private String idNamespace;
        private String localId;
        private String persistentId;
        private String creationTime;
        private String granularity;
        private String sex;
        private String ethnicity;
        private String race;
        private String strain;
        private String species;
        private String ageAtCollection;
        private String subjectRoleTaxonomy;
        
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
        
        public Builder granularity(String granularity) {
            this.granularity = granularity;
            return this;
        }
        
        public Builder sex(String sex) {
            this.sex = sex;
            return this;
        }
        
        public Builder ethnicity(String ethnicity) {
            this.ethnicity = ethnicity;
            return this;
        }
        
        public Builder race(String race) {
            this.race = race;
            return this;
        }
        
        public Builder strain(String strain) {
            this.strain = strain;
            return this;
        }
        
        public Builder species(String species) {
            this.species = species;
            return this;
        }
        
        public Builder ageAtCollection(String ageAtCollection) {
            this.ageAtCollection = ageAtCollection;
            return this;
        }
        
        public Builder subjectRoleTaxonomy(String subjectRoleTaxonomy) {
            this.subjectRoleTaxonomy = subjectRoleTaxonomy;
            return this;
        }
        
        public C2M2Subject build() {
            return new C2M2Subject(
                idNamespace,
                localId,
                persistentId,
                creationTime,
                granularity,
                sex,
                ethnicity,
                race,
                strain,
                species,
                ageAtCollection,
                subjectRoleTaxonomy
            );
        }
    }
}