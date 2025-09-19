package org.scge.c2m2.validation;

import org.scge.c2m2.model.c2m2.*;
import org.scge.c2m2.output.associations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validates relationships and associations between C2M2 entities.
 * Ensures referential integrity and proper entity linking.
 */
@Component
public class RelationshipValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(RelationshipValidator.class);
    
    // Error codes for relationship validation
    private static final String ERR_MISSING_PROJECT_REF = "REL_001";
    private static final String ERR_MISSING_SUBJECT_REF = "REL_002";
    private static final String ERR_MISSING_BIOSAMPLE_REF = "REL_003";
    private static final String ERR_MISSING_FILE_REF = "REL_004";
    private static final String ERR_ORPHANED_ENTITY = "REL_005";
    private static final String ERR_CIRCULAR_DEPENDENCY = "REL_006";
    private static final String ERR_INVALID_ASSOCIATION = "REL_007";
    
    // Warning codes
    private static final String WARN_UNREFERENCED_ENTITY = "REL_W001";
    private static final String WARN_MISSING_ASSOCIATION = "REL_W002";
    
    /**
     * Validates all relationships in a complete C2M2 dataset.
     */
    public ValidationResult validateDatasetRelationships(C2M2Dataset dataset) {
        ValidationResult result = new ValidationResult("C2M2Relationships");
        
        if (dataset == null) {
            result.addError("REL_000", "Dataset is null", "dataset", null, null);
            return result;
        }
        
        logger.info("Validating relationships in C2M2 dataset with {} entities", dataset.getTotalEntityCount());
        
        // Create lookup maps for efficient reference checking
        EntityLookups lookups = createEntityLookups(dataset);
        
        // Validate each entity type's relationships
        validateProjectRelationships(dataset.getProjects(), lookups, result);
        validateSubjectRelationships(dataset.getSubjects(), lookups, result);
        validateBiosampleRelationships(dataset.getBiosamples(), lookups, result);
        validateFileRelationships(dataset.getFiles(), lookups, result);
        
        // Validate associations
        validateAssociations(dataset, lookups, result);
        
        // Check for orphaned entities
        checkOrphanedEntities(dataset, result);
        
        result.addMetadata("total_entities_checked", dataset.getTotalEntityCount());
        result.addMetadata("projects_count", dataset.getProjects().size());
        result.addMetadata("subjects_count", dataset.getSubjects().size());
        result.addMetadata("biosamples_count", dataset.getBiosamples().size());
        result.addMetadata("files_count", dataset.getFiles().size());
        
        logger.info("Relationship validation completed: {} issues found", result.getAllIssues().size());
        
        return result;
    }
    
    /**
     * Validates subject-in-project associations.
     */
    public ValidationResult validateSubjectInProjectAssociations(
            List<SubjectInProjectAssociation> associations,
            Collection<C2M2Project> projects,
            Collection<C2M2Subject> subjects) {
        
        ValidationResult result = new ValidationResult("SubjectInProjectAssociations");
        
        // Create lookup maps
        Set<String> projectIds = createProjectIdSet(projects);
        Set<String> subjectIds = createSubjectIdSet(subjects);
        
        for (SubjectInProjectAssociation association : associations) {
            validateSubjectInProjectAssociation(association, projectIds, subjectIds, result);
        }
        
        return result;
    }
    
    /**
     * Validates a single subject-in-project association.
     */
    private void validateSubjectInProjectAssociation(
            SubjectInProjectAssociation association,
            Set<String> projectIds,
            Set<String> subjectIds,
            ValidationResult result) {
        
        if (!association.isValid()) {
            result.addError(ERR_INVALID_ASSOCIATION,
                "Subject-in-project association has missing required fields",
                "association", null, null);
            return;
        }
        
        String projectKey = association.projectIdNamespace() + ":" + association.projectLocalId();
        String subjectKey = association.subjectIdNamespace() + ":" + association.subjectLocalId();
        
        // Check if referenced project exists
        if (!projectIds.contains(projectKey)) {
            result.addError(ERR_MISSING_PROJECT_REF,
                "Referenced project does not exist: " + projectKey,
                "association", null, "project_id",
                Map.of("referenced_project", projectKey));
        }
        
        // Check if referenced subject exists
        if (!subjectIds.contains(subjectKey)) {
            result.addError(ERR_MISSING_SUBJECT_REF,
                "Referenced subject does not exist: " + subjectKey,
                "association", null, "subject_id",
                Map.of("referenced_subject", subjectKey));
        }
    }
    
    /**
     * Creates entity lookup maps for efficient validation.
     */
    private EntityLookups createEntityLookups(C2M2Dataset dataset) {
        return new EntityLookups(
            createProjectIdSet(dataset.getProjects()),
            createSubjectIdSet(dataset.getSubjects()),
            createBiosampleIdSet(dataset.getBiosamples()),
            createFileIdSet(dataset.getFiles())
        );
    }
    
    private Set<String> createProjectIdSet(Collection<C2M2Project> projects) {
        Set<String> ids = new HashSet<>();
        for (C2M2Project project : projects) {
            if (project.idNamespace() != null && project.localId() != null) {
                ids.add(project.idNamespace() + ":" + project.localId());
            }
        }
        return ids;
    }
    
    private Set<String> createSubjectIdSet(Collection<C2M2Subject> subjects) {
        Set<String> ids = new HashSet<>();
        for (C2M2Subject subject : subjects) {
            if (subject.idNamespace() != null && subject.localId() != null) {
                ids.add(subject.idNamespace() + ":" + subject.localId());
            }
        }
        return ids;
    }
    
    private Set<String> createBiosampleIdSet(Collection<C2M2Biosample> biosamples) {
        Set<String> ids = new HashSet<>();
        for (C2M2Biosample biosample : biosamples) {
            if (biosample.idNamespace() != null && biosample.localId() != null) {
                ids.add(biosample.idNamespace() + ":" + biosample.localId());
            }
        }
        return ids;
    }
    
    private Set<String> createFileIdSet(Collection<C2M2File> files) {
        Set<String> ids = new HashSet<>();
        for (C2M2File file : files) {
            if (file.idNamespace() != null && file.localId() != null) {
                ids.add(file.idNamespace() + ":" + file.localId());
            }
        }
        return ids;
    }
    
    /**
     * Validates project-specific relationships.
     */
    private void validateProjectRelationships(Collection<C2M2Project> projects, EntityLookups lookups, ValidationResult result) {
        for (C2M2Project project : projects) {
            // Projects are top-level entities, no required relationships to validate
            // Could add business logic validations here
        }
    }
    
    /**
     * Validates subject-specific relationships.
     */
    private void validateSubjectRelationships(Collection<C2M2Subject> subjects, EntityLookups lookups, ValidationResult result) {
        for (C2M2Subject subject : subjects) {
            // Subjects should be associated with at least one project (warning)
            // This would require checking the association tables
        }
    }
    
    /**
     * Validates biosample-specific relationships.
     */
    private void validateBiosampleRelationships(Collection<C2M2Biosample> biosamples, EntityLookups lookups, ValidationResult result) {
        for (C2M2Biosample biosample : biosamples) {
            // Check if referenced subject exists
            if (biosample.subjectLocalId() != null) {
                // We need the namespace to create the full key
                // This is a limitation of the current model - we might need to store the full subject reference
                result.addInfo("REL_I001", 
                    "Biosample references subject but cannot validate without namespace", 
                    "biosample", biosample.localId(), "subject_local_id");
            }
        }
    }
    
    /**
     * Validates file-specific relationships.
     */
    private void validateFileRelationships(Collection<C2M2File> files, EntityLookups lookups, ValidationResult result) {
        for (C2M2File file : files) {
            // Files should be associated with biosamples or other entities
            // This validation would be done through association tables
        }
    }
    
    /**
     * Validates association consistency.
     */
    private void validateAssociations(C2M2Dataset dataset, EntityLookups lookups, ValidationResult result) {
        // This would validate that all associations reference existing entities
        // Implementation depends on how associations are stored in the dataset
        result.addInfo("REL_I002", "Association validation not yet implemented", "dataset", null, null);
    }
    
    /**
     * Checks for orphaned entities that aren't referenced by any associations.
     */
    private void checkOrphanedEntities(C2M2Dataset dataset, ValidationResult result) {
        // This would identify entities that exist but aren't connected to the rest of the graph
        // Implementation depends on having access to all association data
        result.addInfo("REL_I003", "Orphaned entity check not yet implemented", "dataset", null, null);
    }
    
    /**
     * Helper record for entity lookups.
     */
    private record EntityLookups(
        Set<String> projectIds,
        Set<String> subjectIds,
        Set<String> biosampleIds,
        Set<String> fileIds
    ) {}
    
    /**
     * Represents a complete C2M2 dataset for validation.
     * This is a simplified interface - in practice this might be replaced
     * with a more comprehensive dataset representation.
     */
    public static class C2M2Dataset {
        private final List<C2M2Project> projects;
        private final List<C2M2Subject> subjects;
        private final List<C2M2Biosample> biosamples;
        private final List<C2M2File> files;
        
        public C2M2Dataset(List<C2M2Project> projects, List<C2M2Subject> subjects, 
                          List<C2M2Biosample> biosamples, List<C2M2File> files) {
            this.projects = projects != null ? projects : new ArrayList<>();
            this.subjects = subjects != null ? subjects : new ArrayList<>();
            this.biosamples = biosamples != null ? biosamples : new ArrayList<>();
            this.files = files != null ? files : new ArrayList<>();
        }
        
        public List<C2M2Project> getProjects() { return projects; }
        public List<C2M2Subject> getSubjects() { return subjects; }
        public List<C2M2Biosample> getBiosamples() { return biosamples; }
        public List<C2M2File> getFiles() { return files; }
        
        public int getTotalEntityCount() {
            return projects.size() + subjects.size() + biosamples.size() + files.size();
        }
    }
}