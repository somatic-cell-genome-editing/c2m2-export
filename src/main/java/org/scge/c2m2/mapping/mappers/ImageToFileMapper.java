package org.scge.c2m2.mapping.mappers;

import org.scge.c2m2.mapping.AbstractMapper;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2File;
import org.scge.c2m2.model.scge.Image;
import org.springframework.stereotype.Component;

/**
 * Maps SCGE Image entities to C2M2 File entities.
 * Images are treated as binary files in the C2M2 model.
 */
@Component
public class ImageToFileMapper extends AbstractMapper<Image, C2M2File> {
    
    private static final String ID_NAMESPACE = "scge.org";
    private static final String ENTITY_PREFIX = "image-file";
    
    @Override
    public C2M2File map(Image source) throws MappingException {
        if (source == null) {
            return null;
        }
        
        logMappingStart(source);
        
        try {
            if (!source.isComplete()) {
                throw new MappingException(
                    "Image is missing required fields for C2M2 mapping: " + source);
            }
            
            C2M2File file = C2M2File.builder()
                .idNamespace(ID_NAMESPACE)
                .localId(generateMappedId(ENTITY_PREFIX, source.imageId()))
                .persistentId(generatePersistentId(source))
                .creationTime(source.uploadDate() != null ? 
                             formatTimestamp(source.uploadDate()) : 
                             getCurrentTimestamp())
                .filename(source.fileName())
                .fileFormat(mapFileFormat(source.getFileExtension()))
                .compressionFormat(null) // Images use internal compression
                .dataType("image")
                .assayType(mapAssayType(source.imageType()))
                .analysisType(null) // Images are typically raw data, not analysis results
                .mimeType(source.getEffectiveMimeType())
                .sizeInBytes(source.fileSize())
                .sha256(null) // Would need to compute from file content
                .md5(null) // Would need to compute from file content
                .description(generateFileDescription(source))
                .build();
            
            logMappingSuccess(source, file);
            return file;
            
        } catch (Exception e) {
            logMappingError(source, e);
            throw new MappingException("Failed to map Image to C2M2File", e);
        }
    }
    
    /**
     * Generates a persistent identifier for the image.
     */
    private String generatePersistentId(Image image) {
        if (image.imageId() != null) {
            return "SCGE:IMAGE:" + image.imageId();
        }
        return null;
    }
    
    /**
     * Maps file extension to C2M2 file format.
     */
    private String mapFileFormat(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return "UNKNOWN";
        }
        
        String normalized = extension.trim().toLowerCase();
        return switch (normalized) {
            case "jpg", "jpeg" -> "JPEG";
            case "png" -> "PNG";
            case "gif" -> "GIF";
            case "tiff", "tif" -> "TIFF";
            case "bmp" -> "BMP";
            case "svg" -> "SVG";
            case "pdf" -> "PDF";
            case "eps" -> "EPS";
            default -> extension.toUpperCase();
        };
    }
    
    /**
     * Maps image type to assay type.
     */
    private String mapAssayType(String imageType) {
        if (imageType == null || imageType.trim().isEmpty()) {
            return "imaging";
        }
        
        String normalized = imageType.trim().toLowerCase();
        return switch (normalized) {
            case "microscopy", "microscope" -> "microscopy";
            case "fluorescence", "fluorescent" -> "fluorescence imaging";
            case "brightfield" -> "brightfield imaging";
            case "confocal" -> "confocal microscopy";
            case "electron", "em" -> "electron microscopy";
            case "live", "live cell" -> "live cell imaging";
            case "timelapse", "time lapse" -> "time-lapse imaging";
            case "histology", "histological" -> "histological imaging";
            case "gel", "gel electrophoresis" -> "gel electrophoresis";
            case "western", "western blot" -> "western blot";
            case "pcr", "gel pcr" -> "PCR gel imaging";
            default -> imageType.trim() + " imaging";
        };
    }
    
    /**
     * Generates a description for the file.
     */
    private String generateFileDescription(Image image) {
        StringBuilder desc = new StringBuilder();
        
        if (image.description() != null && !image.description().trim().isEmpty()) {
            desc.append(image.description().trim());
        } else {
            desc.append("Image file");
            
            if (image.imageType() != null) {
                desc.append(" from ").append(image.imageType()).append(" imaging");
            }
        }
        
        // Add technical details if description is generic
        if (desc.length() < 50) {
            if (image.getFileExtension() != null) {
                desc.append(" (").append(image.getFileExtension().toUpperCase()).append(" format");
                
                if (image.fileSize() != null) {
                    desc.append(", ").append(formatFileSize(image.fileSize()));
                }
                
                desc.append(")");
            }
        }
        
        return desc.toString();
    }
    
    /**
     * Formats file size in human-readable format.
     */
    private String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null || sizeInBytes <= 0) {
            return "unknown size";
        }
        
        if (sizeInBytes < 1024) {
            return sizeInBytes + " bytes";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    @Override
    public boolean canMap(Image source) {
        return super.canMap(source) && source.isComplete();
    }
}