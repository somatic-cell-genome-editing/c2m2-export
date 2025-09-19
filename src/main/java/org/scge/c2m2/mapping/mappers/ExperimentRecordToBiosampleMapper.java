package org.scge.c2m2.mapping.mappers;

import org.scge.c2m2.mapping.AbstractMapper;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2Biosample;
import org.scge.c2m2.model.scge.ExperimentRecord;
import org.springframework.stereotype.Component;

/**
 * Maps SCGE ExperimentRecord entities to C2M2 Biosample entities.
 * Experiment records represent biological samples that underwent gene editing.
 */
@Component
public class ExperimentRecordToBiosampleMapper extends AbstractMapper<ExperimentRecord, C2M2Biosample> {
    
    private static final String ID_NAMESPACE = "scge.org";
    private static final String ENTITY_PREFIX = "biosample";
    
    @Override
    public C2M2Biosample map(ExperimentRecord source) throws MappingException {
        if (source == null) {
            return null;
        }
        
        logMappingStart(source);
        
        try {
            if (!source.isComplete()) {
                throw new MappingException(
                    "ExperimentRecord is missing required fields for C2M2 mapping: " + source);
            }
            
            C2M2Biosample biosample = C2M2Biosample.builder()
                .idNamespace(ID_NAMESPACE)
                .localId(generateMappedId(ENTITY_PREFIX, source.experimentRecordId()))
                .persistentId(generatePersistentId(source))
                .creationTime(source.experimentDate() != null ? 
                             formatTimestamp(source.experimentDate()) : 
                             getCurrentTimestamp())
                .name(generateBiosampleName(source))
                .description(generateBiosampleDescription(source))
                .ageAtSampling(null) // Not typically available in experiment records
                .anatomy(mapAnatomyTerm(source.tissueType()))
                .disease(null) // Would need to be determined from experiment context
                .subjectLocalId(generateSubjectReference(source))
                .build();
            
            logMappingSuccess(source, biosample);
            return biosample;
            
        } catch (Exception e) {
            logMappingError(source, e);
            throw new MappingException("Failed to map ExperimentRecord to C2M2Biosample", e);
        }
    }
    
    /**
     * Generates a persistent identifier for the experiment record.
     */
    private String generatePersistentId(ExperimentRecord record) {
        if (record.experimentRecordId() != null) {
            return "SCGE:EXPERIMENT:" + record.experimentRecordId();
        }
        return null;
    }
    
    /**
     * Generates a descriptive name for the biosample.
     */
    private String generateBiosampleName(ExperimentRecord record) {
        StringBuilder name = new StringBuilder();
        
        if (record.experimentType() != null) {
            name.append(record.experimentType());
        }
        
        if (record.tissueType() != null) {
            if (name.length() > 0) name.append(" - ");
            name.append(record.tissueType());
        }
        
        if (record.targetGene() != null) {
            if (name.length() > 0) name.append(" (");
            name.append(record.targetGene());
            if (name.toString().contains("(")) name.append(")");
        }
        
        if (name.length() == 0) {
            name.append("Biosample ").append(record.experimentRecordId());
        }
        
        return name.toString();
    }
    
    /**
     * Generates a description for the biosample.
     */
    private String generateBiosampleDescription(ExperimentRecord record) {
        if (record.description() != null && !record.description().trim().isEmpty()) {
            return record.description();
        }
        
        StringBuilder desc = new StringBuilder();
        desc.append("Biosample from ");
        
        if (record.experimentType() != null) {
            desc.append(record.experimentType()).append(" experiment");
        } else {
            desc.append("gene editing experiment");
        }
        
        if (record.editingTool() != null) {
            desc.append(" using ").append(record.editingTool());
        }
        
        if (record.targetGene() != null) {
            desc.append(" targeting ").append(record.targetGene());
        }
        
        if (record.deliveryMethod() != null) {
            desc.append(" via ").append(record.deliveryMethod());
        }
        
        return desc.toString();
    }
    
    /**
     * Maps tissue type to C2M2 anatomy terms.
     */
    private String mapAnatomyTerm(String tissueType) {
        if (tissueType == null || tissueType.trim().isEmpty()) {
            return null;
        }
        
        // Map common tissue types to standardized anatomy terms
        String normalized = tissueType.trim().toLowerCase();
        return switch (normalized) {
            case "liver", "hepatic" -> "liver";
            case "muscle", "skeletal muscle" -> "skeletal muscle tissue";
            case "brain", "neural", "neuronal" -> "brain";
            case "heart", "cardiac" -> "heart";
            case "kidney", "renal" -> "kidney";
            case "lung", "pulmonary" -> "lung";
            case "skin", "dermal" -> "skin";
            case "blood", "hematopoietic" -> "blood";
            case "bone", "osseous" -> "bone tissue";
            case "eye", "ocular", "retinal" -> "eye";
            default -> tissueType.trim();
        };
    }
    
    /**
     * Generates a reference to the associated subject.
     */
    private String generateSubjectReference(ExperimentRecord record) {
        if (record.modelId() != null) {
            return "model-subject-" + record.modelId();
        }
        return null;
    }
    
    @Override
    public boolean canMap(ExperimentRecord source) {
        return super.canMap(source) && source.isComplete();
    }
}