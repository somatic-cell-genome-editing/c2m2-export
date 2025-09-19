package org.scge.c2m2.mapping.mappers;

import org.scge.c2m2.identifiers.IdentifierService;
import org.scge.c2m2.mapping.AbstractMapper;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.model.scge.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps SCGE Study entities to C2M2 Project entities.
 */
@Component
public class StudyToProjectMapper extends AbstractMapper<Study, C2M2Project> {
    
    private static final String ID_NAMESPACE = "scge.org";
    private final IdentifierService identifierService;
    
    @Autowired
    public StudyToProjectMapper(IdentifierService identifierService) {
        this.identifierService = identifierService;
    }
    
    @Override
    public C2M2Project map(Study source) throws MappingException {
        if (source == null) {
            return null;
        }
        
        logMappingStart(source);
        
        try {
            if (!source.isComplete()) {
                throw new MappingException(
                    "Study is missing required fields for C2M2 mapping: " + source);
            }
            
            // Generate identifiers using the identifier service
            IdentifierService.ProjectIdentifiers identifiers = 
                identifierService.generateProjectIdentifiers(source);
            
            C2M2Project project = C2M2Project.builder()
                .idNamespace(ID_NAMESPACE)
                .localId(identifiers.localId())
                .persistentId(identifiers.persistentId())
                .creationTime(source.createdDate() != null ? 
                             formatTimestamp(source.createdDate()) : 
                             identifiers.creationTime())
                .name(safeString(source.name()))
                .description(safeString(source.description()))
                .abbreviation(generateAbbreviation(source.name()))
                .build();
            
            logMappingSuccess(source, project);
            return project;
            
        } catch (Exception e) {
            logMappingError(source, e);
            throw new MappingException("Failed to map Study to C2M2Project", e);
        }
    }
    
    /**
     * Generates a persistent identifier for the study if available.
     */
    private String generatePersistentId(Study study) {
        // If the study has external identifiers, use them
        // For now, we'll use the study ID as a fallback
        if (study.studyId() != null) {
            return "SCGE:STUDY:" + study.studyId();
        }
        return null;
    }
    
    /**
     * Generates an abbreviation from the study name.
     */
    private String generateAbbreviation(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        // Create abbreviation from first letters of words
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            // Single word - take first 3-5 characters
            return words[0].length() > 5 ? 
                   words[0].substring(0, 5).toUpperCase() : 
                   words[0].toUpperCase();
        }
        
        StringBuilder abbrev = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty() && abbrev.length() < 10) {
                abbrev.append(word.charAt(0));
            }
        }
        return abbrev.toString().toUpperCase();
    }
    
    @Override
    public boolean canMap(Study source) {
        return super.canMap(source) && source.isComplete();
    }
}