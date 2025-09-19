package org.scge.c2m2.model.scge;

/**
 * Represents a model (animal model) entity from the SCGE database.
 */
public record Model(
    Integer modelId,
    String name,
    String description,
    String species,
    String strain,
    String sex,
    String tissueType,
    String developmentalStage
) {
    
    /**
     * Checks if this model has sufficient information for C2M2 mapping.
     */
    public boolean isComplete() {
        return modelId != null && 
               name != null && !name.trim().isEmpty() &&
               species != null && !species.trim().isEmpty();
    }
    
    /**
     * Gets a display name for the model.
     */
    public String getDisplayName() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        if (species != null && strain != null) {
            return species + " (" + strain + ")";
        }
        return species != null ? species : "Unknown Model";
    }
}