package org.scge.c2m2.mapping.mappers;

import org.scge.c2m2.mapping.AbstractMapper;
import org.scge.c2m2.mapping.MappingException;
import org.scge.c2m2.model.c2m2.C2M2File;
import org.scge.c2m2.model.scge.Protocol;
import org.springframework.stereotype.Component;

/**
 * Maps SCGE Protocol entities to C2M2 File entities.
 * Protocols are treated as text files in the C2M2 model.
 */
@Component
public class ProtocolToFileMapper extends AbstractMapper<Protocol, C2M2File> {
    
    private static final String ID_NAMESPACE = "scge.org";
    private static final String ENTITY_PREFIX = "protocol-file";
    
    @Override
    public C2M2File map(Protocol source) throws MappingException {
        if (source == null) {
            return null;
        }
        
        logMappingStart(source);
        
        try {
            if (!source.isComplete()) {
                throw new MappingException(
                    "Protocol is missing required fields for C2M2 mapping: " + source);
            }
            
            C2M2File file = C2M2File.builder()
                .idNamespace(ID_NAMESPACE)
                .localId(generateMappedId(ENTITY_PREFIX, source.protocolId()))
                .persistentId(generatePersistentId(source))
                .creationTime(source.createdDate() != null ? 
                             formatTimestamp(source.createdDate()) : 
                             getCurrentTimestamp())
                .filename(source.getFileName())
                .fileFormat("TXT")
                .compressionFormat(null) // Protocols are typically not compressed
                .dataType("protocol")
                .assayType(mapAssayType(source.type()))
                .analysisType(null) // Protocols are not analysis results
                .mimeType("text/plain")
                .sizeInBytes(estimateFileSize(source))
                .sha256(null) // Would need to compute from content
                .md5(null) // Would need to compute from content
                .description(generateFileDescription(source))
                .build();
            
            logMappingSuccess(source, file);
            return file;
            
        } catch (Exception e) {
            logMappingError(source, e);
            throw new MappingException("Failed to map Protocol to C2M2File", e);
        }
    }
    
    /**
     * Generates a persistent identifier for the protocol.
     */
    private String generatePersistentId(Protocol protocol) {
        if (protocol.protocolId() != null) {
            return "SCGE:PROTOCOL:" + protocol.protocolId();
        }
        return null;
    }
    
    /**
     * Maps protocol type to assay type.
     */
    private String mapAssayType(String protocolType) {
        if (protocolType == null || protocolType.trim().isEmpty()) {
            return "experimental protocol";
        }
        
        String normalized = protocolType.trim().toLowerCase();
        return switch (normalized) {
            case "gene editing", "crispr", "editing" -> "gene editing protocol";
            case "delivery", "injection" -> "delivery protocol";
            case "analysis", "analytical" -> "analysis protocol";
            case "sample preparation", "prep" -> "sample preparation protocol";
            case "imaging" -> "imaging protocol";
            case "sequencing" -> "sequencing protocol";
            default -> protocolType.trim() + " protocol";
        };
    }
    
    /**
     * Estimates the file size based on content length.
     */
    private Long estimateFileSize(Protocol protocol) {
        long size = 0;
        
        if (protocol.name() != null) {
            size += protocol.name().length();
        }
        
        if (protocol.description() != null) {
            size += protocol.description().length();
        }
        
        if (protocol.content() != null) {
            size += protocol.content().length();
        }
        
        // Add overhead for formatting, headers, etc.
        size += 1000;
        
        return size > 0 ? size : null;
    }
    
    /**
     * Generates a description for the file.
     */
    private String generateFileDescription(Protocol protocol) {
        StringBuilder desc = new StringBuilder();
        
        desc.append("Protocol file");
        
        if (protocol.type() != null) {
            desc.append(" for ").append(protocol.type());
        }
        
        if (protocol.version() != null) {
            desc.append(" (version ").append(protocol.version()).append(")");
        }
        
        if (protocol.description() != null && !protocol.description().trim().isEmpty()) {
            desc.append(": ").append(protocol.description().trim());
        }
        
        return desc.toString();
    }
    
    @Override
    public boolean canMap(Protocol source) {
        return super.canMap(source) && source.isComplete();
    }
}