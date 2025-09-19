package org.scge.c2m2.output.generators;

import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.scge.c2m2.output.associations.SubjectInProjectAssociation;
import org.springframework.stereotype.Component;

/**
 * Generates the subject_in_project.tsv association table.
 * Links subjects to the projects they participate in.
 */
@Component
public class SubjectInProjectTableGenerator implements TableGenerator<SubjectInProjectAssociation> {
    
    private static final String TABLE_FILENAME = "subject_in_project.tsv";
    
    private static final String[] HEADERS = {
        "project_id_namespace",
        "project_local_id",
        "subject_id_namespace", 
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
    public String[] entityToRecord(SubjectInProjectAssociation association) {
        if (association == null) {
            throw new IllegalArgumentException("SubjectInProjectAssociation cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(association.projectIdNamespace()),
            TsvWriter.safeToString(association.projectLocalId()),
            TsvWriter.safeToString(association.subjectIdNamespace()),
            TsvWriter.safeToString(association.subjectLocalId())
        };
    }
    
    @Override
    public Class<SubjectInProjectAssociation> getEntityType() {
        return SubjectInProjectAssociation.class;
    }
    
    @Override
    public boolean canGenerate(SubjectInProjectAssociation association) {
        return association != null && association.isValid();
    }
}