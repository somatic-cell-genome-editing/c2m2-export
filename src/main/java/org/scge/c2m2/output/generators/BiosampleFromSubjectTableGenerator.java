package org.scge.c2m2.output.generators;

import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.scge.c2m2.output.associations.BiosampleFromSubjectAssociation;
import org.springframework.stereotype.Component;

/**
 * Generates the biosample_from_subject.tsv association table.
 * Links biosamples to the subjects they were derived from.
 */
@Component
public class BiosampleFromSubjectTableGenerator implements TableGenerator<BiosampleFromSubjectAssociation> {
    
    private static final String TABLE_FILENAME = "biosample_from_subject.tsv";
    
    private static final String[] HEADERS = {
        "biosample_id_namespace",
        "biosample_local_id",
        "subject_id_namespace",
        "subject_local_id",
        "age_at_sampling"
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
    public String[] entityToRecord(BiosampleFromSubjectAssociation association) {
        if (association == null) {
            throw new IllegalArgumentException("BiosampleFromSubjectAssociation cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(association.biosampleIdNamespace()),
            TsvWriter.safeToString(association.biosampleLocalId()),
            TsvWriter.safeToString(association.subjectIdNamespace()),
            TsvWriter.safeToString(association.subjectLocalId()),
            TsvWriter.safeToString(association.ageAtSampling())
        };
    }
    
    @Override
    public Class<BiosampleFromSubjectAssociation> getEntityType() {
        return BiosampleFromSubjectAssociation.class;
    }
    
    @Override
    public boolean canGenerate(BiosampleFromSubjectAssociation association) {
        return association != null && association.isValid();
    }
}