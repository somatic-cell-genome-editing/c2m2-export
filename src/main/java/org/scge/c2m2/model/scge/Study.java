package org.scge.c2m2.model.scge;

import java.time.Instant;

/**
 * Represents a study entity from the SCGE database.
 */
public record Study(
    Integer studyId,
    String name,
    String description,
    String status,
    Instant createdDate,
    String tier,
    String pi,
    String institution
) {
    
    /**
     * Creates a Study with minimal required information.
     */
    public static Study of(Integer studyId, String name, String description) {
        return new Study(studyId, name, description, null, null, null, null, null);
    }
    
    /**
     * Checks if this study has complete information for C2M2 mapping.
     */
    public boolean isComplete() {
        return studyId != null && 
               name != null && !name.trim().isEmpty() &&
               description != null && !description.trim().isEmpty();
    }
}