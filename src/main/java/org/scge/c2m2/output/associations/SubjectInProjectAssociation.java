package org.scge.c2m2.output.associations;

/**
 * Represents a subject's participation in a project.
 * This generates the subject_in_project.tsv association table.
 */
public record SubjectInProjectAssociation(
    String projectIdNamespace,
    String projectLocalId,
    String subjectIdNamespace,
    String subjectLocalId
) {
    
    public static SubjectInProjectAssociation of(String projectIdNamespace, String projectLocalId,
                                               String subjectIdNamespace, String subjectLocalId) {
        return new SubjectInProjectAssociation(projectIdNamespace, projectLocalId,
                                             subjectIdNamespace, subjectLocalId);
    }
    
    public boolean isValid() {
        return projectIdNamespace != null && !projectIdNamespace.trim().isEmpty() &&
               projectLocalId != null && !projectLocalId.trim().isEmpty() &&
               subjectIdNamespace != null && !subjectIdNamespace.trim().isEmpty() &&
               subjectLocalId != null && !subjectLocalId.trim().isEmpty();
    }
}