package org.scge.c2m2.output.generators;

import org.scge.c2m2.model.c2m2.C2M2Subject;
import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.springframework.stereotype.Component;

/**
 * Generates the subject.tsv table for C2M2 submissions.
 * Subjects represent research subjects, patients, or experimental entities.
 */
@Component
public class SubjectTableGenerator implements TableGenerator<C2M2Subject> {
    
    private static final String TABLE_FILENAME = "subject.tsv";
    
    private static final String[] HEADERS = {
        "id_namespace",
        "local_id",
        "persistent_id", 
        "creation_time",
        "granularity",
        "sex",
        "ethnicity",
        "race",
        "strain",
        "species",
        "age_at_collection",
        "subject_role_taxonomy"
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
    public String[] entityToRecord(C2M2Subject subject) {
        if (subject == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(subject.idNamespace()),
            TsvWriter.safeToString(subject.localId()),
            TsvWriter.safeToString(subject.persistentId()),
            TsvWriter.safeToString(subject.creationTime()),
            TsvWriter.safeToString(subject.granularity()),
            TsvWriter.safeToString(subject.sex()),
            TsvWriter.safeToString(subject.ethnicity()),
            TsvWriter.safeToString(subject.race()),
            TsvWriter.safeToString(subject.strain()),
            TsvWriter.safeToString(subject.species()),
            TsvWriter.safeToString(subject.ageAtCollection()),
            TsvWriter.safeToString(subject.subjectRoleTaxonomy())
        };
    }
    
    @Override
    public Class<C2M2Subject> getEntityType() {
        return C2M2Subject.class;
    }
    
    @Override
    public boolean canGenerate(C2M2Subject subject) {
        return subject != null && 
               subject.idNamespace() != null && 
               subject.localId() != null;
    }
}