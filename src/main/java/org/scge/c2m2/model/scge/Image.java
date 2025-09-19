package org.scge.c2m2.model.scge;

import java.time.Instant;

/**
 * Represents an image entity from the SCGE database.
 */
public record Image(
    Integer imageId,
    String fileName,
    String filePath,
    String description,
    String imageType,
    Long fileSize,
    String mimeType,
    Instant uploadDate,
    Integer experimentRecordId
) {
    
    /**
     * Checks if this image has sufficient information for C2M2 mapping.
     */
    public boolean isComplete() {
        return imageId != null && 
               fileName != null && !fileName.trim().isEmpty() &&
               filePath != null && !filePath.trim().isEmpty();
    }
    
    /**
     * Gets the file extension from the fileName.
     */
    public String getFileExtension() {
        if (fileName == null) {
            return null;
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(lastDot + 1).toLowerCase();
    }
    
    /**
     * Gets the MIME type, inferring from file extension if not provided.
     */
    public String getEffectiveMimeType() {
        if (mimeType != null && !mimeType.trim().isEmpty()) {
            return mimeType;
        }
        
        String extension = getFileExtension();
        if (extension != null) {
            return switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "tiff", "tif" -> "image/tiff";
                case "bmp" -> "image/bmp";
                case "svg" -> "image/svg+xml";
                default -> "application/octet-stream";
            };
        }
        
        return "application/octet-stream";
    }
}