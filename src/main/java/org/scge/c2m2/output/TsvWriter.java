package org.scge.c2m2.output;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility class for writing C2M2 data to TSV files.
 * Handles CSV formatting with tab delimiters as required by C2M2.
 */
public class TsvWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(TsvWriter.class);
    
    private static final CSVFormat C2M2_TSV_FORMAT = CSVFormat.Builder.create()
            .setDelimiter('\t')
            .setQuote('"')
            .setRecordSeparator("\n")
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .build();
    
    /**
     * Writes data to a TSV file.
     * 
     * @param outputPath The path where the TSV file will be written
     * @param headers The column headers
     * @param records The data records
     * @throws IOException if writing fails
     */
    public static void writeTsv(Path outputPath, String[] headers, List<String[]> records) 
            throws IOException {
        
        logger.info("Writing TSV file: {} with {} records", outputPath, records.size());
        
        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());
        
        try (FileWriter fileWriter = new FileWriter(outputPath.toFile());
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, C2M2_TSV_FORMAT)) {
            
            // Write headers
            csvPrinter.printRecord((Object[]) headers);
            
            // Write data records
            for (String[] record : records) {
                csvPrinter.printRecord((Object[]) record);
            }
            
            csvPrinter.flush();
            logger.info("Successfully wrote {} records to {}", records.size(), outputPath);
            
        } catch (IOException e) {
            logger.error("Failed to write TSV file: {}", outputPath, e);
            throw e;
        }
    }
    
    /**
     * Converts data to TSV string format.
     * 
     * @param headers The column headers
     * @param records The data records
     * @return TSV formatted string
     * @throws IOException if formatting fails
     */
    public static String toTsvString(String[] headers, List<String[]> records) throws IOException {
        try (StringWriter stringWriter = new StringWriter();
             CSVPrinter csvPrinter = new CSVPrinter(stringWriter, C2M2_TSV_FORMAT)) {
            
            // Write headers
            csvPrinter.printRecord((Object[]) headers);
            
            // Write data records
            for (String[] record : records) {
                csvPrinter.printRecord((Object[]) record);
            }
            
            csvPrinter.flush();
            return stringWriter.toString();
            
        } catch (IOException e) {
            logger.error("Failed to convert data to TSV string", e);
            throw e;
        }
    }
    
    /**
     * Validates that all records have the expected number of columns.
     * 
     * @param headers The expected headers
     * @param records The data records to validate
     * @throws IllegalArgumentException if validation fails
     */
    public static void validateRecords(String[] headers, List<String[]> records) {
        if (headers == null || headers.length == 0) {
            throw new IllegalArgumentException("Headers cannot be null or empty");
        }
        
        if (records == null) {
            throw new IllegalArgumentException("Records cannot be null");
        }
        
        int expectedColumns = headers.length;
        
        for (int i = 0; i < records.size(); i++) {
            String[] record = records.get(i);
            if (record == null) {
                throw new IllegalArgumentException("Record " + i + " is null");
            }
            
            if (record.length != expectedColumns) {
                throw new IllegalArgumentException(
                    String.format("Record %d has %d columns, expected %d. Record: %s", 
                                 i, record.length, expectedColumns, String.join(",", record)));
            }
        }
        
        logger.debug("Validated {} records with {} columns each", records.size(), expectedColumns);
    }
    
    /**
     * Safely converts a value to string, handling nulls.
     * 
     * @param value The value to convert
     * @return String representation, empty string for null
     */
    public static String safeToString(Object value) {
        return value != null ? value.toString() : "";
    }
    
    /**
     * Formats a boolean value for C2M2 TSV (true/false lowercase).
     * 
     * @param value The boolean value
     * @return "true", "false", or empty string for null
     */
    public static String formatBoolean(Boolean value) {
        return value != null ? value.toString().toLowerCase() : "";
    }
    
    /**
     * Formats a numeric value, ensuring proper decimal representation.
     * 
     * @param value The numeric value
     * @return Formatted string or empty for null
     */
    public static String formatNumber(Number value) {
        return value != null ? value.toString() : "";
    }
}