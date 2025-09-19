package org.scge.c2m2.output.generators;

import org.scge.c2m2.model.c2m2.C2M2File;
import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.springframework.stereotype.Component;

/**
 * Generates the file.tsv table for C2M2 submissions.
 * Files represent data files, documents, and other digital assets.
 */
@Component
public class FileTableGenerator implements TableGenerator<C2M2File> {
    
    private static final String TABLE_FILENAME = "file.tsv";
    
    private static final String[] HEADERS = {
        "id_namespace",
        "local_id",
        "persistent_id",
        "creation_time",
        "filename",
        "file_format",
        "compression_format",
        "data_type",
        "assay_type",
        "analysis_type",
        "mime_type",
        "size_in_bytes",
        "sha256",
        "md5",
        "description"
    };
    
    @Override
    public String getTableFileName() {
        return TABLE_FILENAME;
    }
    
    @Override
    public String[] getHeaders() {
        return HEADERS.clone();
    }
    
    @Override
    public String[] entityToRecord(C2M2File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(file.idNamespace()),
            TsvWriter.safeToString(file.localId()),
            TsvWriter.safeToString(file.persistentId()),
            TsvWriter.safeToString(file.creationTime()),
            TsvWriter.safeToString(file.filename()),
            TsvWriter.safeToString(file.fileFormat()),
            TsvWriter.safeToString(file.compressionFormat()),
            TsvWriter.safeToString(file.dataType()),
            TsvWriter.safeToString(file.assayType()),
            TsvWriter.safeToString(file.analysisType()),
            TsvWriter.safeToString(file.mimeType()),
            TsvWriter.formatNumber(file.sizeInBytes()),
            TsvWriter.safeToString(file.sha256()),
            TsvWriter.safeToString(file.md5()),
            TsvWriter.safeToString(file.description())
        };
    }
    
    @Override
    public Class<C2M2File> getEntityType() {
        return C2M2File.class;
    }
    
    @Override
    public boolean canGenerate(C2M2File file) {
        return file != null && 
               file.idNamespace() != null && 
               file.localId() != null;
    }
}