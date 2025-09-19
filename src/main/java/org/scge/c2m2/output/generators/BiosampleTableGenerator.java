package org.scge.c2m2.output.generators;

import org.scge.c2m2.model.c2m2.C2M2Biosample;
import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.springframework.stereotype.Component;

/**
 * Generates the biosample.tsv table for C2M2 submissions.
 * Biosamples represent biological samples used in experiments.
 */
@Component
public class BiosampleTableGenerator implements TableGenerator<C2M2Biosample> {
    
    private static final String TABLE_FILENAME = "biosample.tsv";
    
    private static final String[] HEADERS = {
        "id_namespace",
        "local_id",
        "persistent_id",
        "creation_time",
        "name",
        "description",
        "age_at_sampling",
        "anatomy",
        "disease",
        "subject_local_id"
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
    public String[] entityToRecord(C2M2Biosample biosample) {
        if (biosample == null) {
            throw new IllegalArgumentException("Biosample cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(biosample.idNamespace()),
            TsvWriter.safeToString(biosample.localId()),
            TsvWriter.safeToString(biosample.persistentId()),
            TsvWriter.safeToString(biosample.creationTime()),
            TsvWriter.safeToString(biosample.name()),
            TsvWriter.safeToString(biosample.description()),
            TsvWriter.safeToString(biosample.ageAtSampling()),
            TsvWriter.safeToString(biosample.anatomy()),
            TsvWriter.safeToString(biosample.disease()),
            TsvWriter.safeToString(biosample.subjectLocalId())
        };
    }
    
    @Override
    public Class<C2M2Biosample> getEntityType() {
        return C2M2Biosample.class;
    }
    
    @Override
    public boolean canGenerate(C2M2Biosample biosample) {
        return biosample != null && 
               biosample.idNamespace() != null && 
               biosample.localId() != null;
    }
}