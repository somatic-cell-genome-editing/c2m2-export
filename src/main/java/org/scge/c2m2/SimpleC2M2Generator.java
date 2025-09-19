package org.scge.c2m2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Comprehensive C2M2 generator that maps the entire SCGE database to C2M2 entities.
 * Generates all C2M2-compliant TSV files from the complete database.
 */
public class SimpleC2M2Generator {
    
    // Database configuration - use environment variables or config file
    private static final String DB_URL = System.getenv("DB_URL") != null ? 
        System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/scgedb";
    private static final String DB_USERNAME = System.getenv("DB_USERNAME") != null ?
        System.getenv("DB_USERNAME") : "dbuser";
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD") != null ?
        System.getenv("DB_PASSWORD") : "dbpass";
    
    public static void main(String[] args) {
        try {
            System.out.println("=== SCGE C2M2 Simple Generator ===");
            
            // Create output directory
            Path outputDir = Paths.get("scge_metadata");
            Files.createDirectories(outputDir);
            
            System.out.println("Output directory: " + outputDir.toAbsolutePath());
            
            // Connect to database and generate C2M2 files
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                System.out.println("✓ Connected to database");
                
                // Generate all C2M2 entity files
                generateDccFile(conn, outputDir);
                generateIdNamespaceFile(conn, outputDir);
                generateProjectFile(conn, outputDir);
                generateProjectInProjectFile(conn, outputDir);
                generateSubjectFile(conn, outputDir);
                generateBiosampleFile(conn, outputDir);
                // generateFileFile(conn, outputDir); // Excluded per user request
                // generateCollectionFile(conn, outputDir); // Removed per user request
                
                // Generate all association files
                generateAssociationFiles(conn, outputDir);
                
                // Copy missing blank files to ensure complete C2M2 schema
                copyMissingBlankFiles(outputDir);
                
                // Create comprehensive manifest
                createManifest(outputDir);
                
                System.out.println("=== Generation Complete ===");
                System.out.println("Check directory: " + outputDir.toAbsolutePath());
                
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void generateDccFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating dcc.tsv...");
        
        Path dccFile = outputDir.resolve("dcc.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with ALL C2M2 dcc columns (all are required)
        lines.add("id\tdcc_name\tdcc_abbreviation\tdcc_description\tcontact_email\tcontact_name\tdcc_url\tproject_id_namespace\tproject_local_id");
        
        // SCGE DCC information - single row as required
        String id = "cfde_registry_dcc:scge";
        String dccName = "Somatic Cell Genome Editing";
        String dccAbbreviation = "SCGE";
        String dccDescription = "The goal of the Somatic Cell Genome Editing (SCGE) program is to accelerate the development of safer and more effective methods to edit the genomes of disease-relevant somatic cells and tissues in patients. The SCGE Toolkit serves as the hub to promote the novel strategies and technologies funded by the NIH Common Fund SCGE program.";
        String contactEmail = "scge@mcw.edu"; // Update with actual contact email
        String contactName = "SCGE Data Coordination Center"; // Update with actual contact name
        String dccUrl = "https://scge.mcw.edu";
        String projectIdNamespace = "scge.mcw.edu";
        String projectLocalId = "SCGE:1"; // Assuming the first study is the overarching project
        
        lines.add(id + "\t" + dccName + "\t" + dccAbbreviation + "\t" + dccDescription + "\t" + 
                 contactEmail + "\t" + contactName + "\t" + dccUrl + "\t" + 
                 projectIdNamespace + "\t" + projectLocalId);
        
        Files.write(dccFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated dcc.tsv with DCC information");
    }
    
    private static void generateIdNamespaceFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating id_namespace.tsv...");
        
        Path idNamespaceFile = outputDir.resolve("id_namespace.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with all C2M2 id_namespace columns
        lines.add("id\tabbreviation\tname\tdescription");
        
        // SCGE namespace information
        String id = "scge.mcw.edu";
        String abbreviation = "SCGE";
        String name = "Somatic Cell Genome Editing";
        String description = "The official namespace for the SCGE program data coordination center at the Medical College of Wisconsin";
        
        lines.add(id + "\t" + abbreviation + "\t" + name + "\t" + description);
        
        Files.write(idNamespaceFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated id_namespace.tsv with namespace information");
    }
    
    private static void generateProjectFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating project.tsv...");
        
        Path projectFile = outputDir.resolve("project.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with ALL C2M2 columns in correct order (required + optional)
        lines.add("id_namespace\tlocal_id\tpersistent_id\tcreation_time\tabbreviation\tname\tdescription");
        
        int count = 0;
        
        // Add top-level SCGE project
        String topLevelLocalId = "SCGE:1";
        String topLevelName = "Somatic Cell Genome Editing";
        String topLevelPersistentId = "https://scge.mcw.edu/toolkit/";
        String topLevelCreationTime = "";
        String topLevelAbbrev = "SCGE";
        String topLevelDesc = "The Somatic Cell Genome Editing (SCGE) program is a comprehensive research initiative funded by the NIH Common Fund to develop safer and more effective methods for editing the genomes of disease-relevant somatic cells and tissues in patients.";
        
        lines.add("scge.mcw.edu\t" + topLevelLocalId + "\t" + topLevelPersistentId + "\t" + topLevelCreationTime + "\t" + topLevelAbbrev + "\t" + topLevelName + "\t" + topLevelDesc);
        count++;
        
        // Add studies as projects (tier 4 only)
        String studySql = "SELECT study_id, study, reference FROM study WHERE tier = 4 ORDER BY study_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(studySql)) {
            
            while (rs.next()) {
                Integer studyId = rs.getInt("study_id");
                String study = rs.getString("study");
                String reference = rs.getString("reference");
                
                if (studyId != null) {
                    String localId = "SCGE:" + studyId;
                    String cleanName = cleanValue(study != null ? study : "Study " + studyId) + " (SCGE:" + studyId + ")";
                    String persistentId = "https://scge.mcw.edu/toolkit/data/study/" + studyId;
                    String creationTime = ""; // No creation time available
                    String abbrev = "SCGE" + studyId; // Use SCGE + studyId as abbreviation without colon
                    String cleanDesc = cleanValue(reference != null ? reference : "SCGE study " + studyId);
                    
                    lines.add("scge.mcw.edu\t" + localId + "\t" + persistentId + "\t" + creationTime + "\t" + abbrev + "\t" + cleanName + "\t" + cleanDesc);
                    count++;
                }
            }
        }
        
        // Add experiments as projects (tier 4 only - inherited from study)
        String expSql = "SELECT e.experiment_id, e.name, e.description FROM experiment e JOIN study s ON e.study_id = s.study_id WHERE s.tier = 4 ORDER BY e.experiment_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(expSql)) {
            
            while (rs.next()) {
                Long expId = rs.getLong("experiment_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                
                if (expId != null) {
                    // Skip the specific experiment ID 18010000000
                    if (expId == 18010000000L) {
                        continue;
                    }
                    
                    String localId = "SCGE:" + expId;
                    String cleanName = cleanValue(name != null ? name : "Experiment " + expId) + " (SCGE:" + expId + ")";
                    String persistentId = "https://scge.mcw.edu/toolkit/data/experiments/experiment/" + expId; // Create URL with experiment_id
                    String creationTime = ""; // No creation time available
                    String abbrev = "SCGE" + expId; // Use SCGE + expId as abbreviation without colon
                    String cleanDesc = cleanValue(description != null ? description : "SCGE experiment " + expId);
                    
                    lines.add("scge.mcw.edu\t" + localId + "\t" + persistentId + "\t" + creationTime + "\t" + abbrev + "\t" + cleanName + "\t" + cleanDesc);
                    count++;
                }
            }
        }
        
        Files.write(projectFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated project.tsv with " + count + " records (studies + experiments)");
    }
    
    private static void generateProjectInProjectFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating project_in_project.tsv...");
        
        Path projectInProjectFile = outputDir.resolve("project_in_project.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with C2M2 required columns
        lines.add("parent_project_id_namespace\tparent_project_local_id\tchild_project_id_namespace\tchild_project_local_id");
        
        int count = 0;
        
        // Map all studies as children of the top-level SCGE:1 project (tier 4 only)
        String studyParentSql = "SELECT study_id FROM study WHERE tier = 4 ORDER BY study_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(studyParentSql)) {
            
            while (rs.next()) {
                Integer studyId = rs.getInt("study_id");
                
                if (studyId != null) {
                    // SCGE:1 is parent, study is child
                    lines.add("scge.mcw.edu\tSCGE:1\tscge.mcw.edu\tSCGE:" + studyId);
                    count++;
                }
            }
        }
        
        // Map experiments to studies using the study_id column in experiment table (tier 4 only)
        String sql = "SELECT e.experiment_id, e.study_id FROM experiment e JOIN study s ON e.study_id = s.study_id WHERE e.study_id IS NOT NULL AND s.tier = 4 ORDER BY e.study_id, e.experiment_id";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Long expId = rs.getLong("experiment_id");
                Integer studyId = rs.getInt("study_id");
                
                if (expId != null && studyId != null) {
                    // Skip the specific experiment ID 18010000000
                    if (expId == 18010000000L) {
                        continue;
                    }
                    // Study is parent, experiment is child
                    lines.add("scge.mcw.edu\tSCGE:" + studyId + "\tscge.mcw.edu\tSCGE:" + expId);
                    count++;
                }
            }
        }
        
        Files.write(projectInProjectFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated project_in_project.tsv with " + count + " associations");
    }
    
    private static void generateSubjectFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating subject.tsv...");
        
        Path subjectFile = outputDir.resolve("subject.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with ALL C2M2 subject columns in correct order (required + optional)
        lines.add("id_namespace\tlocal_id\tproject_id_namespace\tproject_local_id\tpersistent_id\tcreation_time\tgranularity\tsex\tethnicity\tage_at_enrollment");
        
        int count = 0;
        
        // Add subjects from model table - each subject only once, all associated with SCGE:1 project (tier 4 only)
        String modelSql = """
            SELECT DISTINCT m.model_id, m.name, m.organism, m.sex, m.rrid
            FROM model m
            WHERE m.tier = 4
            ORDER BY m.model_id
            """;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(modelSql)) {
            
            while (rs.next()) {
                Long modelId = rs.getLong("model_id");
                String name = rs.getString("name");
                String organism = rs.getString("organism");
                String sex = rs.getString("sex");
                String rrid = rs.getString("rrid");
                
                if (modelId != null) {
                    String localId = "SCGE:" + modelId;
                    String projectIdNamespace = "scge.mcw.edu";
                    String projectLocalId = "SCGE:1"; // All subjects associated with top-level SCGE project
                    
                    String granularity = "individual"; // Default granularity for model subjects
                    String persistentId = "https://scge.mcw.edu/toolkit/data/models/model?id=" + modelId; // Create URL with model_id
                    String creationTime = ""; // No creation time available
                    String cleanSex = cleanValue(sex);
                    String ethnicity = ""; // No ethnicity data available
                    String ageAtEnrollment = ""; // No age at enrollment data available
                    
                    lines.add("scge.mcw.edu\t" + localId + "\t" + projectIdNamespace + "\t" + projectLocalId + "\t" + 
                             persistentId + "\t" + creationTime + "\t" + granularity + "\t" + cleanSex + "\t" + 
                             ethnicity + "\t" + ageAtEnrollment);
                    count++;
                }
            }
        }
        
        Files.write(subjectFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated subject.tsv with " + count + " records (unique subjects, all associated with SCGE:1)");
    }
    
    private static void generateBiosampleFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating biosample.tsv...");
        
        Path biosampleFile = outputDir.resolve("biosample.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with ALL C2M2 biosample columns (required + optional)
        lines.add("id_namespace\tlocal_id\tproject_id_namespace\tproject_local_id\tpersistent_id\tcreation_time\tsample_prep_method\tanatomy\tbiofluid");
        
        int count = 0;
        
        // Add biosamples from experiment_record table (tier 4 only)
        String expRecordSql = "SELECT er.experiment_record_id, er.experiment_id, er.editor_id, er.model_id, er.tissue_id, er.organ_system FROM experiment_record er JOIN experiment e ON er.experiment_id = e.experiment_id JOIN study s ON e.study_id = s.study_id WHERE s.tier = 4 ORDER BY er.experiment_record_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(expRecordSql)) {
            
            while (rs.next()) {
                Long recordId = rs.getLong("experiment_record_id");
                Long expId = rs.getLong("experiment_id");
                Long editorId = rs.getLong("editor_id");
                Long modelId = rs.getLong("model_id");
                
                // Handle tissue_id and organ_system as UBERON identifier strings
                String tissueId = rs.getString("tissue_id");
                String organSystem = rs.getString("organ_system");
                
                if (recordId != null && expId != null) {
                    // Skip biosamples from the specific experiment ID 18010000000
                    if (expId == 18010000000L) {
                        continue;
                    }
                    
                    String localId = "SCGE:" + recordId;
                    String projectIdNamespace = "scge.mcw.edu";
                    String projectLocalId = "SCGE:" + expId;
                    String persistentId = "https://scge.mcw.edu/toolkit/data/experiments/experiment/" + expId + "/record/" + recordId; // Create URL with experiment_id and record_id
                    String creationTime = ""; // No creation time available
                    String samplePrepMethod = ""; // No sample prep method available
                    String anatomy = cleanValue(tissueId); // Use tissue_id UBERON ID from experiment_record
                    String biofluid = ""; // No biofluid mapping
                    
                    lines.add("scge.mcw.edu\t" + localId + "\t" + projectIdNamespace + "\t" + projectLocalId + "\t" + 
                             persistentId + "\t" + creationTime + "\t" + samplePrepMethod + "\t" + anatomy + "\t" + biofluid);
                    count++;
                }
            }
        }
        
        // Note: Tissue table records are not included in biosample.tsv per user request
        // Only experiment_record entries are included as biosamples
        
        Files.write(biosampleFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated biosample.tsv with " + count + " records");
    }
    
    private static void generateFileFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating file.tsv...");
        
        Path fileFile = outputDir.resolve("file.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header
        lines.add("id_namespace\tlocal_id\tname\tfile_format\tdata_type\tfile_size\tmd5\tsha256\tfilename");
        
        int count = 0;
        
        // Add files from images table
        String imagesSql = "SELECT scge_id, file_name, file_type, title FROM images ORDER BY scge_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(imagesSql)) {
            
            while (rs.next()) {
                Long scgeId = rs.getLong("scge_id");
                String fileName = rs.getString("file_name");
                String fileType = rs.getString("file_type");
                String title = rs.getString("title");
                
                if (scgeId != null) {
                    String localId = "image-" + scgeId;
                    String name = buildImageName(fileName, title, scgeId);
                    String format = fileType != null ? fileType : determineFileTypeFromName(fileName);
                    
                    lines.add("scge.org\t" + localId + "\t" + cleanValue(name) + "\t" + 
                             cleanValue(format) + "\timage\t\t\t\t" + cleanValue(fileName));
                    count++;
                }
            }
        }
        
        Files.write(fileFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated file.tsv with " + count + " records");
    }
    
    private static void generateCollectionFile(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating collection.tsv...");
        
        Path collectionFile = outputDir.resolve("collection.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header
        lines.add("id_namespace\tlocal_id\tname\tdescription\thas_time_series_data");
        
        int count = 0;
        
        // Experiments are now in project.tsv, so we skip them here
        
        // Add collections from delivery_system table
        String deliverySql = "SELECT ds_id, ds_name, ds_type, ds_description FROM delivery_system ORDER BY ds_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(deliverySql)) {
            
            while (rs.next()) {
                Long dsId = rs.getLong("ds_id");
                String dsName = rs.getString("ds_name");
                String dsType = rs.getString("ds_type");
                String dsDescription = rs.getString("ds_description");
                
                if (dsId != null) {
                    String localId = "SCGE:" + dsId;
                    String cleanName = cleanValue(dsName != null ? dsName : "Delivery System " + dsId);
                    String cleanDesc = cleanValue(dsDescription != null ? dsDescription : 
                                                 (dsType != null ? dsType + " delivery system" : "Delivery system"));
                    
                    lines.add("scge.mcw.edu\t" + localId + "\t" + cleanName + "\t" + cleanDesc + "\tfalse");
                    count++;
                }
            }
        }
        
        // Add collections from vector table
        String vectorSql = "SELECT vector_id, name, type, description FROM vector ORDER BY vector_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(vectorSql)) {
            
            while (rs.next()) {
                Long vectorId = rs.getLong("vector_id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                String description = rs.getString("description");
                
                if (vectorId != null) {
                    String localId = "SCGE:" + vectorId;
                    String cleanName = cleanValue(name != null ? name : "Vector " + vectorId);
                    String cleanDesc = cleanValue(description != null ? description : 
                                                 (type != null ? type + " vector" : "Vector"));
                    
                    lines.add("scge.mcw.edu\t" + localId + "\t" + cleanName + "\t" + cleanDesc + "\tfalse");
                    count++;
                }
            }
        }
        
        Files.write(collectionFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated collection.tsv with " + count + " records");
    }
    
    private static void generateAssociationFiles(Connection conn, Path outputDir) throws Exception {
        System.out.println("Generating association files...");
        
        // Note: subject_in_project.tsv is not part of C2M2 specification - removed
        
        // Generate biosample associations
        generateBiosampleAssociations(conn, outputDir);
        
        // generateFileAssociations(conn, outputDir); // Excluded per user request
        
        // generateCollectionAssociations(conn, outputDir); // Removed per user request
    }
    
    
    private static void generateBiosampleAssociations(Connection conn, Path outputDir) throws Exception {
        // biosample_from_subject associations
        Path biosampleSubjectFile = outputDir.resolve("biosample_from_subject.tsv");
        List<String> lines = new ArrayList<>();
        
        // Add header with ALL C2M2 biosample_from_subject columns (required + optional)
        lines.add("biosample_id_namespace\tbiosample_local_id\tsubject_id_namespace\tsubject_local_id\tage_at_sampling");
        
        String sql = "SELECT er.experiment_record_id, er.model_id FROM experiment_record er JOIN experiment e ON er.experiment_id = e.experiment_id JOIN study s ON e.study_id = s.study_id JOIN model m ON er.model_id = m.model_id WHERE er.model_id IS NOT NULL AND s.tier = 4 AND m.tier = 4";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Long recordId = rs.getLong("experiment_record_id");
                Long modelId = rs.getLong("model_id");
                
                if (recordId != null && modelId != null) {
                    String biosampleIdNamespace = "scge.mcw.edu";
                    String biosampleLocalId = "SCGE:" + recordId;
                    String subjectIdNamespace = "scge.mcw.edu";
                    String subjectLocalId = "SCGE:" + modelId;
                    String ageAtSampling = ""; // No age mapping
                    
                    lines.add(biosampleIdNamespace + "\t" + biosampleLocalId + "\t" + 
                             subjectIdNamespace + "\t" + subjectLocalId + "\t" + ageAtSampling);
                }
            }
        }
        
        Files.write(biosampleSubjectFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated biosample_from_subject.tsv with " + (lines.size() - 1) + " associations");
        
        // biosample_in_project associations (experiments are now projects)
        Path biosampleProjectFile = outputDir.resolve("biosample_in_project.tsv");
        lines = new ArrayList<>();
        lines.add("project\tbiosample");
        
        String projSql = "SELECT er.experiment_id, er.experiment_record_id FROM experiment_record er JOIN experiment e ON er.experiment_id = e.experiment_id JOIN study s ON e.study_id = s.study_id WHERE er.experiment_id IS NOT NULL AND s.tier = 4";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(projSql)) {
            
            while (rs.next()) {
                Long expId = rs.getLong("experiment_id");
                Long recordId = rs.getLong("experiment_record_id");
                
                if (expId != null && recordId != null) {
                    // Skip associations with the specific experiment ID 18010000000
                    if (expId == 18010000000L) {
                        continue;
                    }
                    lines.add("scge.mcw.edu:SCGE:" + expId + "\tscge.mcw.edu:SCGE:" + recordId);
                }
            }
        }
        
        Files.write(biosampleProjectFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated biosample_in_project.tsv with " + (lines.size() - 1) + " associations");
    }
    
    private static void generateFileAssociations(Connection conn, Path outputDir) throws Exception {
        // file_describes_collection associations (images describing experiments)
        Path fileCollectionFile = outputDir.resolve("file_describes_collection.tsv");
        List<String> lines = new ArrayList<>();
        lines.add("file\tcollection");
        
        // Link images to experiments (simplified mapping)
        String sql = "SELECT i.scge_id, e.experiment_id FROM images i, experiment e ORDER BY i.scge_id LIMIT 100";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Long scgeId = rs.getLong("scge_id");
                Long expId = rs.getLong("experiment_id");
                
                if (scgeId != null && expId != null) {
                    lines.add("scge.org:image-" + scgeId + "\tscge.org:exp-" + expId);
                }
            }
        }
        
        Files.write(fileCollectionFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated file_describes_collection.tsv with " + (lines.size() - 1) + " associations");
    }
    
    private static void generateCollectionAssociations(Connection conn, Path outputDir) throws Exception {
        // collection_in_project associations
        Path collectionProjectFile = outputDir.resolve("collection_in_project.tsv");
        List<String> lines = new ArrayList<>();
        lines.add("project\tcollection");
        
        // Link delivery systems to studies
        String deliverySql = "SELECT s.study_id, ds.ds_id FROM study s, delivery_system ds ORDER BY s.study_id, ds.ds_id LIMIT 100";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(deliverySql)) {
            
            while (rs.next()) {
                Integer studyId = rs.getInt("study_id");
                Long deliveryId = rs.getLong("ds_id");
                
                if (studyId != null && deliveryId != null) {
                    lines.add("scge.mcw.edu:SCGE:" + studyId + "\tscge.mcw.edu:SCGE:" + deliveryId);
                }
            }
        }
        
        // Link vectors to studies
        String vectorSql = "SELECT s.study_id, v.vector_id FROM study s, vector v ORDER BY s.study_id, v.vector_id LIMIT 100";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(vectorSql)) {
            
            while (rs.next()) {
                Integer studyId = rs.getInt("study_id");
                Long vectorId = rs.getLong("vector_id");
                
                if (studyId != null && vectorId != null) {
                    lines.add("scge.mcw.edu:SCGE:" + studyId + "\tscge.mcw.edu:SCGE:" + vectorId);
                }
            }
        }
        
        Files.write(collectionProjectFile, String.join("\n", lines).getBytes());
        System.out.println("  Generated collection_in_project.tsv with " + (lines.size() - 1) + " associations");
    }
    
    private static void createManifest(Path outputDir) throws Exception {
        System.out.println("Creating manifest...");
        
        Path manifestFile = outputDir.resolve("manifest.json");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        String manifest = """
            {
              "manifest_version": "1.0",
              "submission_id": "scge-comprehensive-%s",
              "submission_title": "SCGE Comprehensive C2M2 Submission",
              "submission_description": "Complete C2M2 submission generated from entire SCGE database",
              "data_coordinating_center": "SCGE",
              "organization": "Somatic Cell Genome Editing Program",
              "created_at": "%s",
              "c2m2_version": "2.0",
              "generator_info": {
                "name": "SCGE Comprehensive C2M2 Generator",
                "version": "2.0.0",
                "source_database": "SCGE PostgreSQL",
                "total_tables_mapped": 8,
                "entity_types_generated": ["project", "subject", "biosample", "collection"]
              },
              "files": [
                {"path": "project.tsv", "type": "data", "description": "C2M2 project table from study and experiment tables"},
                {"path": "project_in_project.tsv", "type": "association", "description": "Project hierarchy with experiments as children of studies"},
                {"path": "subject.tsv", "type": "data", "description": "C2M2 subject table from model table"},
                {"path": "biosample.tsv", "type": "data", "description": "C2M2 biosample table from experiment_record and tissue tables"},
                {"path": "biosample_from_subject.tsv", "type": "association", "description": "Biosample-subject associations"},
                {"path": "biosample_in_project.tsv", "type": "association", "description": "Biosample-project associations"},
              ]
            }
            """.formatted(timestamp.replace(":", ""), timestamp);
        
        Files.write(manifestFile, manifest.getBytes());
        System.out.println("  Created manifest.json");
    }
    
    private static String cleanValue(String value) {
        if (value == null) return "";
        return value.replace("\\t", " ").replace("\\n", " ").replace("\\r", " ").trim();
    }
    
    /**
     * Builds a meaningful tissue name from the available tissue table columns.
     */
    private static String buildTissueName(String tissueTerm, String parentTissueTerm, Long tissueId) {
        if (tissueTerm != null && !tissueTerm.trim().isEmpty() && !tissueTerm.equalsIgnoreCase("general")) {
            if (parentTissueTerm != null && !parentTissueTerm.trim().isEmpty()) {
                return parentTissueTerm + " - " + tissueTerm;
            } else {
                return tissueTerm;
            }
        } else if (parentTissueTerm != null && !parentTissueTerm.trim().isEmpty()) {
            return parentTissueTerm;
        } else {
            return "Tissue " + tissueId;
        }
    }
    
    /**
     * Builds a descriptive description from the tissue table columns.
     */
    private static String buildTissueDescription(String tissueSystem, String parentTissueTerm, String tissueTerm) {
        StringBuilder desc = new StringBuilder();
        
        if (tissueSystem != null && !tissueSystem.trim().isEmpty()) {
            desc.append("Tissue from ").append(tissueSystem);
        } else {
            desc.append("Tissue sample");
        }
        
        if (parentTissueTerm != null && !parentTissueTerm.trim().isEmpty()) {
            desc.append(", specifically from ").append(parentTissueTerm);
        }
        
        if (tissueTerm != null && !tissueTerm.trim().isEmpty() && !tissueTerm.equalsIgnoreCase("general")) {
            desc.append(" (").append(tissueTerm).append(")");
        }
        
        return desc.toString();
    }
    
    /**
     * Builds a meaningful name for image files from available columns.
     */
    private static String buildImageName(String fileName, String title, Long scgeId) {
        if (title != null && !title.trim().isEmpty()) {
            return title.trim();
        } else if (fileName != null && !fileName.trim().isEmpty()) {
            return fileName.trim();
        } else {
            return "Image " + scgeId;
        }
    }
    
    /**
     * Determines file type from filename extension if file_type is null.
     */
    private static String determineFileTypeFromName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "image";
        }
        
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerName.endsWith(".png")) {
            return "image/png";
        } else if (lowerName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerName.endsWith(".tiff") || lowerName.endsWith(".tif")) {
            return "image/tiff";
        } else if (lowerName.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerName.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "image";
        }
    }
    
    /**
     * Copies any missing blank C2M2 files from the blank_non-auto-generated_C2M2_tables directory
     * to the output directory to ensure complete C2M2 schema compliance.
     */
    private static void copyMissingBlankFiles(Path outputDir) throws Exception {
        System.out.println("Copying missing blank C2M2 files...");
        
        Path blankFilesDir = Paths.get("blank_non-auto-generated_C2M2_tables");
        if (!Files.exists(blankFilesDir)) {
            System.out.println("  Warning: blank_non-auto-generated_C2M2_tables directory not found");
            return;
        }
        
        int copiedCount = 0;
        
        // Get list of all blank files
        try (var stream = Files.list(blankFilesDir)) {
            var blankFiles = stream.filter(Files::isRegularFile)
                                  .filter(p -> p.toString().endsWith(".tsv"))
                                  .toList();
            
            for (Path blankFile : blankFiles) {
                String fileName = blankFile.getFileName().toString();
                Path targetFile = outputDir.resolve(fileName);
                
                // Only copy if the file doesn't exist in the output directory
                if (!Files.exists(targetFile)) {
                    Files.copy(blankFile, targetFile);
                    copiedCount++;
                    System.out.println("  Copied blank file: " + fileName);
                }
            }
        }
        
        System.out.println("  Copied " + copiedCount + " blank C2M2 files to ensure complete schema");
    }
}