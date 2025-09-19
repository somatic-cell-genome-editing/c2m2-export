package org.scge.c2m2.output.associations;

/**
 * Represents an association between a Project and a Subject in C2M2.
 * This is used to generate the project_in_project.tsv association table.
 */
public record ProjectSubjectAssociation(
    String projectIdNamespace,
    String projectLocalId,
    String subjectIdNamespace,
    String subjectLocalId
) {
    
    public static ProjectSubjectAssociation of(String projectIdNamespace, String projectLocalId,
                                             String subjectIdNamespace, String subjectLocalId) {
        return new ProjectSubjectAssociation(projectIdNamespace, projectLocalId,
                                           subjectIdNamespace, subjectLocalId);
    }
    
    /**
     * Validates that all required fields are present.
     */
    public boolean isValid() {
        return projectIdNamespace != null && !projectIdNamespace.trim().isEmpty() &&
               projectLocalId != null && !projectLocalId.trim().isEmpty() &&
               subjectIdNamespace != null && !subjectIdNamespace.trim().isEmpty() &&
               subjectLocalId != null && !subjectLocalId.trim().isEmpty();
    }
}