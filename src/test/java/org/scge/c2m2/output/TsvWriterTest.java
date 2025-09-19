package org.scge.c2m2.output;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TsvWriter utility class.
 */
class TsvWriterTest {
    
    @TempDir
    Path tempDir;
    
    @Test
    void testWriteTsv() throws IOException {
        String[] headers = {"id", "name", "description"};
        List<String[]> records = Arrays.asList(
            new String[]{"1", "Test Name", "Test Description"},
            new String[]{"2", "Another Name", "Another Description"}
        );
        
        Path outputFile = tempDir.resolve("test.tsv");
        TsvWriter.writeTsv(outputFile, headers, records);
        
        assertTrue(Files.exists(outputFile));
        
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size()); // headers + 2 records
        assertEquals("id\tname\tdescription", lines.get(0));
        assertEquals("1\tTest Name\tTest Description", lines.get(1));
        assertEquals("2\tAnother Name\tAnother Description", lines.get(2));
    }
    
    @Test
    void testWriteTsvWithEmptyRecords() throws IOException {
        String[] headers = {"id", "name"};
        List<String[]> records = Collections.emptyList();
        
        Path outputFile = tempDir.resolve("empty.tsv");
        TsvWriter.writeTsv(outputFile, headers, records);
        
        assertTrue(Files.exists(outputFile));
        
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(1, lines.size()); // headers only
        assertEquals("id\tname", lines.get(0));
    }
    
    @Test
    void testWriteTsvWithSpecialCharacters() throws IOException {
        String[] headers = {"id", "description"};
        List<String[]> records = Arrays.asList(
            new String[]{"1", "Text with\ttabs and \"quotes\""},
            new String[]{"2", "Text with\nnewlines"}
        );
        
        Path outputFile = tempDir.resolve("special.tsv");
        TsvWriter.writeTsv(outputFile, headers, records);
        
        assertTrue(Files.exists(outputFile));
        
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(3, lines.size()); // headers + 2 records
        assertTrue(lines.get(1).contains("\"Text with\ttabs and \"\"quotes\"\"\""));
    }
    
    @Test
    void testToTsvString() throws IOException {
        String[] headers = {"id", "name"};
        List<String[]> records = Arrays.asList(
            new String[]{"1", "Test"},
            new String[]{"2", "Another"}
        );
        
        String result = TsvWriter.toTsvString(headers, records);
        
        String[] lines = result.split("\n");
        assertEquals(3, lines.length);
        assertEquals("id\tname", lines[0]);
        assertEquals("1\tTest", lines[1]);
        assertEquals("2\tAnother", lines[2]);
    }
    
    @Test
    void testValidateRecords() {
        String[] headers = {"id", "name", "description"};
        List<String[]> validRecords = Arrays.asList(
            new String[]{"1", "Test", "Description"},
            new String[]{"2", "Another", "Another Description"}
        );
        
        // Should not throw
        assertDoesNotThrow(() -> TsvWriter.validateRecords(headers, validRecords));
    }
    
    @Test
    void testValidateRecordsWithWrongColumnCount() {
        String[] headers = {"id", "name", "description"};
        List<String[]> invalidRecords = Arrays.asList(
            new String[]{"1", "Test"}, // Missing description
            new String[]{"2", "Another", "Description"}
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> TsvWriter.validateRecords(headers, invalidRecords));
        assertTrue(exception.getMessage().contains("Record 0 has 2 columns, expected 3"));
    }
    
    @Test
    void testValidateRecordsWithNullHeaders() {
        List<String[]> records = List.of(new String[]{"1", "Test"});
        
        assertThrows(IllegalArgumentException.class, 
            () -> TsvWriter.validateRecords(null, records));
    }
    
    @Test
    void testValidateRecordsWithNullRecord() {
        String[] headers = {"id", "name"};
        List<String[]> recordsWithNull = Arrays.asList(
            new String[]{"1", "Test"},
            null
        );
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> TsvWriter.validateRecords(headers, recordsWithNull));
        assertTrue(exception.getMessage().contains("Record 1 is null"));
    }
    
    @Test
    void testSafeToString() {
        assertEquals("test", TsvWriter.safeToString("test"));
        assertEquals("123", TsvWriter.safeToString(123));
        assertEquals("", TsvWriter.safeToString(null));
    }
    
    @Test
    void testFormatBoolean() {
        assertEquals("true", TsvWriter.formatBoolean(true));
        assertEquals("false", TsvWriter.formatBoolean(false));
        assertEquals("", TsvWriter.formatBoolean(null));
    }
    
    @Test
    void testFormatNumber() {
        assertEquals("123", TsvWriter.formatNumber(123));
        assertEquals("45.67", TsvWriter.formatNumber(45.67));
        assertEquals("", TsvWriter.formatNumber(null));
    }
}