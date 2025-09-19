package org.scge.c2m2.output.associations;

/**
 * Represents a file derived from a biosample.
 * This generates the file_describes_biosample.tsv association table.
 */
public record FileFromBiosampleAssociation(
    String fileIdNamespace,
    String fileLocalId,
    String biosampleIdNamespace,
    String biosampleLocalId
) {
    
    public static FileFromBiosampleAssociation of(String fileIdNamespace, String fileLocalId,
                                                String biosampleIdNamespace, String biosampleLocalId) {
        return new FileFromBiosampleAssociation(fileIdNamespace, fileLocalId,
                                              biosampleIdNamespace, biosampleLocalId);
    }
    
    public boolean isValid() {
        return fileIdNamespace != null && !fileIdNamespace.trim().isEmpty() &&
               fileLocalId != null && !fileLocalId.trim().isEmpty() &&
               biosampleIdNamespace != null && !biosampleIdNamespace.trim().isEmpty() &&
               biosampleLocalId != null && !biosampleLocalId.trim().isEmpty();
    }
}