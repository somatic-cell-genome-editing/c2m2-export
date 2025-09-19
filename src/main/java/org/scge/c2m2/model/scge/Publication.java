package org.scge.c2m2.model.scge;

import java.time.Instant;

/**
 * Represents a publication entity from the SCGE database.
 */
public record Publication(
    Integer publicationId,
    String title,
    String authors,
    String journal,
    String doi,
    String pmid,
    Instant publicationDate,
    String abstractText,
    String publicationType
) {
    
    /**
     * Checks if this publication has sufficient information for C2M2 mapping.
     */
    public boolean isComplete() {
        return publicationId != null && 
               title != null && !title.trim().isEmpty() &&
               (doi != null || pmid != null);
    }
    
    /**
     * Gets the primary identifier for this publication (DOI preferred, then PMID).
     */
    public String getPrimaryIdentifier() {
        if (doi != null && !doi.trim().isEmpty()) {
            return doi;
        }
        if (pmid != null && !pmid.trim().isEmpty()) {
            return "PMID:" + pmid;
        }
        return null;
    }
}