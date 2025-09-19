package org.scge.c2m2.mapping.mappers;

import org.scge.c2m2.mapping.AbstractMapper;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2Subject;
import org.scge.c2m2.model.scge.Model;
import org.springframework.stereotype.Component;

/**
 * Maps SCGE Model entities to C2M2 Subject entities.
 * Animal models are the primary subjects in SCGE research.
 */
@Component
public class ModelToSubjectMapper extends AbstractMapper<Model, C2M2Subject> {
    
    private static final String ID_NAMESPACE = "scge.org";
    private static final String ENTITY_PREFIX = "model-subject";
    
    @Override
    public C2M2Subject map(Model source) throws MappingException {
        if (source == null) {
            return null;
        }
        
        logMappingStart(source);
        
        try {
            if (!source.isComplete()) {
                throw new MappingException(
                    "Model is missing required fields for C2M2 mapping: " + source);
            }
            
            C2M2Subject subject = C2M2Subject.builder()
                .idNamespace(ID_NAMESPACE)
                .localId(generateMappedId(ENTITY_PREFIX, source.modelId()))
                .persistentId(generatePersistentId(source))
                .creationTime(getCurrentTimestamp())
                .granularity(determineGranularity(source))
                .sex(normalizeSex(source.sex()))
                .ethnicity(null) // Not applicable for animal models
                .race(null) // Not applicable for animal models
                .strain(safeString(source.strain()))
                .species(normalizeSpecies(source.species()))
                .ageAtCollection(source.developmentalStage()) // Map developmental stage to age
                .subjectRoleTaxonomy(mapSubjectRole(source))
                .build();
            
            logMappingSuccess(source, subject);
            return subject;
            
        } catch (Exception e) {
            logMappingError(source, e);
            throw new MappingException("Failed to map Model to C2M2Subject", e);
        }
    }
    
    /**
     * Generates a persistent identifier for the model.
     */
    private String generatePersistentId(Model model) {
        if (model.modelId() != null) {
            return "SCGE:MODEL:" + model.modelId();
        }
        return null;
    }
    
    /**
     * Determines the granularity level for the subject.
     */
    private String determineGranularity(Model model) {
        // Most animal models represent individual organisms
        return "organism";
    }
    
    /**
     * Normalizes sex values to C2M2 standard terms.
     */
    private String normalizeSex(String sex) {
        if (sex == null || sex.trim().isEmpty()) {
            return null;
        }
        
        String normalized = sex.trim().toLowerCase();
        return switch (normalized) {
            case "m", "male", "males" -> "male";
            case "f", "female", "females" -> "female";
            case "mixed", "both", "m/f", "f/m" -> "mixed";
            case "unknown", "not specified", "n/a" -> "unknown";
            default -> normalized;
        };
    }
    
    /**
     * Normalizes species names to standard taxonomic names.
     */
    private String normalizeSpecies(String species) {
        if (species == null || species.trim().isEmpty()) {
            return "unknown";
        }
        
        String normalized = species.trim().toLowerCase();
        return switch (normalized) {
            case "mouse", "mice", "mus musculus" -> "Mus musculus";
            case "rat", "rats", "rattus norvegicus" -> "Rattus norvegicus";
            case "pig", "pigs", "swine", "sus scrofa" -> "Sus scrofa";
            case "zebrafish", "danio rerio" -> "Danio rerio";
            case "human", "humans", "homo sapiens" -> "Homo sapiens";
            case "macaque", "rhesus macaque", "macaca mulatta" -> "Macaca mulatta";
            default -> species.trim();
        };
    }
    
    /**
     * Maps the model to an appropriate subject role taxonomy.
     */
    private String mapSubjectRole(Model model) {
        // For animal models, the role is typically "model organism"
        if (model.species() != null) {
            String species = normalizeSpecies(model.species());
            if ("Homo sapiens".equals(species)) {
                return "research participant";
            }
        }
        return "model organism";
    }
    
    @Override
    public boolean canMap(Model source) {
        return super.canMap(source) && source.isComplete();
    }
}