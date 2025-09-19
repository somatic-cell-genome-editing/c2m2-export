package org.scge.c2m2.output.associations;

/**
 * Represents a biosample derived from a subject.
 * This generates the biosample_from_subject.tsv association table.
 */
public record BiosampleFromSubjectAssociation(
    String biosampleIdNamespace,
    String biosampleLocalId,
    String subjectIdNamespace,
    String subjectLocalId,
    String ageAtSampling
) {
    
    public static BiosampleFromSubjectAssociation of(String biosampleIdNamespace, String biosampleLocalId,
                                                   String subjectIdNamespace, String subjectLocalId) {
        return new BiosampleFromSubjectAssociation(biosampleIdNamespace, biosampleLocalId,
                                                  subjectIdNamespace, subjectLocalId, null);
    }
    
    public static BiosampleFromSubjectAssociation of(String biosampleIdNamespace, String biosampleLocalId,
                                                   String subjectIdNamespace, String subjectLocalId,
                                                   String ageAtSampling) {
        return new BiosampleFromSubjectAssociation(biosampleIdNamespace, biosampleLocalId,
                                                  subjectIdNamespace, subjectLocalId, ageAtSampling);
    }
    
    public boolean isValid() {
        return biosampleIdNamespace != null && !biosampleIdNamespace.trim().isEmpty() &&
               biosampleLocalId != null && !biosampleLocalId.trim().isEmpty() &&
               subjectIdNamespace != null && !subjectIdNamespace.trim().isEmpty() &&
               subjectLocalId != null && !subjectLocalId.trim().isEmpty();
    }
}