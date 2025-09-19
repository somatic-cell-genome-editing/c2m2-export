package org.scge.c2m2.model.scge;

import java.time.Instant;

/**
 * Represents a protocol entity from the SCGE database.
 */
public record Protocol(
    Integer protocolId,
    String name,
    String description,
    String content,
    String version,
    String type,
    Instant createdDate,
    Instant lastModified
) {
    
    /**
     * Checks if this protocol has sufficient information for C2M2 mapping.
     */
    public boolean isComplete() {
        return protocolId != null && 
               name != null && !name.trim().isEmpty() &&
               (description != null || content != null);
    }
    
    /**
     * Gets the file name for this protocol when exported as a file.
     */
    public String getFileName() {
        if (name == null) {
            return "protocol_" + protocolId + ".txt";
        }
        String fileName = name.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (version != null) {
            fileName += "_v" + version;
        }
        return fileName + ".txt";
    }
}