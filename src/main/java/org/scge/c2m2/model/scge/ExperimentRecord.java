package org.scge.c2m2.model.scge;

import java.time.Instant;

/**
 * Represents an experiment record entity from the SCGE database.
 */
public record ExperimentRecord(
    Integer experimentRecordId,
    Integer studyId,
    Integer modelId,
    String experimentType,
    String description,
    String deliveryMethod,
    String targetGene,
    String editingTool,
    Instant experimentDate,
    String status,
    String tissueType,
    String cellType
) {
    
    /**
     * Checks if this experiment record has sufficient information for C2M2 mapping.
     */
    public boolean isComplete() {
        return experimentRecordId != null && 
               studyId != null &&
               experimentType != null && !experimentType.trim().isEmpty();
    }
    
    /**
     * Gets a display name for the experiment.
     */
    public String getDisplayName() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        StringBuilder name = new StringBuilder();
        if (experimentType != null) {
            name.append(experimentType);
        }
        if (targetGene != null) {
            if (name.length() > 0) name.append(" - ");
            name.append(targetGene);
        }
        if (editingTool != null) {
            if (name.length() > 0) name.append(" (");
            name.append(editingTool);
            if (name.toString().contains("(")) name.append(")");
        }
        return name.length() > 0 ? name.toString() : "Experiment " + experimentRecordId;
    }
}