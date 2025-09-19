package org.scge.c2m2.output.generators;

import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.scge.c2m2.output.associations.FileFromBiosampleAssociation;
import org.springframework.stereotype.Component;

/**
 * Generates the file_describes_biosample.tsv association table.
 * Links files to the biosamples they describe or were derived from.
 */
@Component
public class FileDescribesBiosampleTableGenerator implements TableGenerator<FileFromBiosampleAssociation> {
    
    private static final String TABLE_FILENAME = "file_describes_biosample.tsv";
    
    private static final String[] HEADERS = {
        "file_id_namespace",
        "file_local_id",
        "biosample_id_namespace",
        "biosample_local_id"
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
    public String[] entityToRecord(FileFromBiosampleAssociation association) {
        if (association == null) {
            throw new IllegalArgumentException("FileFromBiosampleAssociation cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(association.fileIdNamespace()),
            TsvWriter.safeToString(association.fileLocalId()),
            TsvWriter.safeToString(association.biosampleIdNamespace()),
            TsvWriter.safeToString(association.biosampleLocalId())
        };
    }
    
    @Override
    public Class<FileFromBiosampleAssociation> getEntityType() {
        return FileFromBiosampleAssociation.class;
    }
    
    @Override
    public boolean canGenerate(FileFromBiosampleAssociation association) {
        return association != null && association.isValid();
    }
}