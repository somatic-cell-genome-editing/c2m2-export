package org.scge.c2m2.mapping.mappers;

import org.scge.c2m2.mapping.AbstractMapper;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2Subject;
import org.scge.c2m2.model.scge.Person;
import org.springframework.stereotype.Component;

/**
 * Maps SCGE Person entities to C2M2 Subject entities.
 * Note: Persons are typically researchers, not subjects of study,
 * but this mapper exists for cases where person data needs to be 
 * represented as subjects in C2M2.
 */
@Component
public class PersonToSubjectMapper extends AbstractMapper<Person, C2M2Subject> {
    
    private static final String ID_NAMESPACE = "scge.org";
    private static final String ENTITY_PREFIX = "person-subject";
    
    @Override
    public C2M2Subject map(Person source) throws MappingException {
        if (source == null) {
            return null;
        }
        
        logMappingStart(source);
        
        try {
            if (!source.isComplete()) {
                throw new MappingException(
                    "Person is missing required fields for C2M2 mapping: " + source);
            }
            
            C2M2Subject subject = C2M2Subject.builder()
                .idNamespace(ID_NAMESPACE)
                .localId(generateMappedId(ENTITY_PREFIX, source.personId()))
                .persistentId(generatePersistentId(source))
                .creationTime(getCurrentTimestamp())
                .granularity("person") // Person-level granularity
                .sex(null) // Not typically available for researchers
                .ethnicity(null) // Not typically available
                .race(null) // Not typically available
                .strain(null) // Not applicable for humans
                .species("Homo sapiens") // Assuming human researchers
                .ageAtCollection(null) // Not typically available
                .subjectRoleTaxonomy("researcher") // Custom role for researchers
                .build();
            
            logMappingSuccess(source, subject);
            return subject;
            
        } catch (Exception e) {
            logMappingError(source, e);
            throw new MappingException("Failed to map Person to C2M2Subject", e);
        }
    }
    
    /**
     * Generates a persistent identifier for the person.
     */
    private String generatePersistentId(Person person) {
        // Use email as persistent ID if available
        if (person.email() != null && !person.email().trim().isEmpty()) {
            return "mailto:" + person.email().trim();
        }
        
        // Fallback to person ID
        if (person.personId() != null) {
            return "SCGE:PERSON:" + person.personId();
        }
        
        return null;
    }
    
    @Override
    public boolean canMap(Person source) {
        return super.canMap(source) && source.isComplete();
    }
}