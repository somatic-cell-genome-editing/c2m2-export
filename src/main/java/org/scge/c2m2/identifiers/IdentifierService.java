package org.scge.c2m2.identifiers;

import org.scge.c2m2.model.c2m2.*;
import org.scge.c2m2.model.scge.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * High-level service for generating C2M2 identifiers for specific entity types.
 * Provides convenience methods for common identifier generation patterns.
 */
@Service
public class IdentifierService {
    
    private static final Logger logger = LoggerFactory.getLogger(IdentifierService.class);
    
    private final IdentifierManager identifierManager;
    
    @Autowired
    public IdentifierService(IdentifierManager identifierManager) {
        this.identifierManager = identifierManager;
        logger.info("IdentifierService initialized");
    }
    
    /**
     * Generates complete identifiers for a C2M2 Project from an SCGE Study.
     */
    public ProjectIdentifiers generateProjectIdentifiers(Study study) {
        if (study == null || study.studyId() == null) {
            throw new IllegalArgumentException("Study and study ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("project", study.studyId());
        String persistentId = identifierManager.generatePersistentId("project", study.studyId());
        String creationTime = getCurrentTimestamp();
        
        logger.debug("Generated project identifiers for study {}: local={}, persistent={}", 
                    study.studyId(), localId, persistentId);
        
        return new ProjectIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Generates complete identifiers for a C2M2 Subject from an SCGE Person.
     */
    public SubjectIdentifiers generateSubjectIdentifiers(Person person) {
        if (person == null || person.personId() == null) {
            throw new IllegalArgumentException("Person and person ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("subject", person.personId());
        String persistentId = identifierManager.generatePersistentId("subject", person.personId());
        String creationTime = getCurrentTimestamp();
        
        logger.debug("Generated subject identifiers for person {}: local={}, persistent={}", 
                    person.personId(), localId, persistentId);
        
        return new SubjectIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Generates complete identifiers for a C2M2 Subject from an SCGE Model.
     */
    public SubjectIdentifiers generateSubjectIdentifiers(Model model) {
        if (model == null || model.modelId() == null) {
            throw new IllegalArgumentException("Model and model ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("subject", model.modelId(), "model");
        String persistentId = identifierManager.generatePersistentId("subject", model.modelId(), "model");
        String creationTime = getCurrentTimestamp();
        
        logger.debug("Generated subject identifiers for model {}: local={}, persistent={}", 
                    model.modelId(), localId, persistentId);
        
        return new SubjectIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Generates complete identifiers for a C2M2 Biosample from an SCGE ExperimentRecord.
     */
    public BiosampleIdentifiers generateBiosampleIdentifiers(ExperimentRecord experiment) {
        if (experiment == null || experiment.experimentRecordId() == null) {
            throw new IllegalArgumentException("Experiment and experiment record ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("biosample", experiment.experimentRecordId());
        String persistentId = identifierManager.generatePersistentId("biosample", experiment.experimentRecordId());
        String creationTime = getCurrentTimestamp();
        
        logger.debug("Generated biosample identifiers for experiment {}: local={}, persistent={}", 
                    experiment.experimentRecordId(), localId, persistentId);
        
        return new BiosampleIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Generates complete identifiers for a C2M2 File from an SCGE Protocol.
     */
    public FileIdentifiers generateFileIdentifiers(Protocol protocol) {
        if (protocol == null || protocol.protocolId() == null) {
            throw new IllegalArgumentException("Protocol and protocol ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("file", protocol.protocolId(), "protocol");
        String persistentId = identifierManager.generatePersistentId("file", protocol.protocolId(), "protocol");
        String creationTime = getCurrentTimestamp();
        
        logger.debug("Generated file identifiers for protocol {}: local={}, persistent={}", 
                    protocol.protocolId(), localId, persistentId);
        
        return new FileIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Generates complete identifiers for a C2M2 File from an SCGE Image.
     */
    public FileIdentifiers generateFileIdentifiers(Image image) {
        if (image == null || image.imageId() == null) {
            throw new IllegalArgumentException("Image and image ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("file", image.imageId(), "image");
        String persistentId = identifierManager.generatePersistentId("file", image.imageId(), "image");
        String creationTime = getCurrentTimestamp();
        
        logger.debug("Generated file identifiers for image {}: local={}, persistent={}", 
                    image.imageId(), localId, persistentId);
        
        return new FileIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Generates identifiers using a specific strategy.
     */
    public ProjectIdentifiers generateProjectIdentifiers(Study study, String strategy) {
        if (study == null || study.studyId() == null) {
            throw new IllegalArgumentException("Study and study ID cannot be null");
        }
        
        String localId = identifierManager.generateLocalId("project", study.studyId(), strategy);
        String persistentId = identifierManager.generatePersistentId(
            identifierManager.getDefaultNamespace(), "project", study.studyId(), strategy);
        String creationTime = getCurrentTimestamp();
        
        return new ProjectIdentifiers(localId, persistentId, creationTime);
    }
    
    /**
     * Validates that all identifiers in a C2M2 entity are valid.
     */
    public boolean validateC2M2ProjectIdentifiers(C2M2Project project) {
        if (project == null) {
            return false;
        }
        
        return identifierManager.isValidIdentifier(project.localId(), "project") &&
               (project.persistentId() == null || 
                identifierManager.isValidIdentifier(project.persistentId(), "project"));
    }
    
    /**
     * Validates that all identifiers in a C2M2 entity are valid.
     */
    public boolean validateC2M2SubjectIdentifiers(C2M2Subject subject) {
        if (subject == null) {
            return false;
        }
        
        return identifierManager.isValidIdentifier(subject.localId(), "subject") &&
               (subject.persistentId() == null || 
                identifierManager.isValidIdentifier(subject.persistentId(), "subject"));
    }
    
    /**
     * Gets the current timestamp in ISO format.
     */
    private String getCurrentTimestamp() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
    }
    
    /**
     * Gets the identifier manager for advanced operations.
     */
    public IdentifierManager getIdentifierManager() {
        return identifierManager;
    }
    
    // Record classes for identifier sets
    
    public record ProjectIdentifiers(String localId, String persistentId, String creationTime) {}
    
    public record SubjectIdentifiers(String localId, String persistentId, String creationTime) {}
    
    public record BiosampleIdentifiers(String localId, String persistentId, String creationTime) {}
    
    public record FileIdentifiers(String localId, String persistentId, String creationTime) {}
}