package org.scge.c2m2.output.generators;

import org.scge.c2m2.model.c2m2.C2M2Project;
import org.scge.c2m2.output.TableGenerator;
import org.scge.c2m2.output.TsvWriter;
import org.springframework.stereotype.Component;

/**
 * Generates the project.tsv table for C2M2 submissions.
 * Projects represent high-level research initiatives or studies.
 */
@Component
public class ProjectTableGenerator implements TableGenerator<C2M2Project> {
    
    private static final String TABLE_FILENAME = "project.tsv";
    
    private static final String[] HEADERS = {
        "id_namespace",
        "local_id", 
        "persistent_id",
        "creation_time",
        "name",
        "description",
        "abbreviation"
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
    public String[] entityToRecord(C2M2Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }
        
        return new String[] {
            TsvWriter.safeToString(project.idNamespace()),
            TsvWriter.safeToString(project.localId()),
            TsvWriter.safeToString(project.persistentId()),
            TsvWriter.safeToString(project.creationTime()),
            TsvWriter.safeToString(project.name()),
            TsvWriter.safeToString(project.description()),
            TsvWriter.safeToString(project.abbreviation())
        };
    }
    
    @Override
    public Class<C2M2Project> getEntityType() {
        return C2M2Project.class;
    }
    
    @Override
    public boolean canGenerate(C2M2Project project) {
        return project != null && 
               project.idNamespace() != null && 
               project.localId() != null;
    }
}