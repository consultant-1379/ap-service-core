/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.csv;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.archive.exception.MalformedCsvException;

/**
 * Unit tests for {@link CsvReader}.
 */
public class CsvReaderTest {

    @Test(expected = IllegalArgumentException.class)
    public void when_project_contains_no_csv_file_then_an_exception_is_thrown() {
        final Archive pa = new Archive(new LinkedHashMap<String, ArchiveArtifact>());
        new CsvReader(pa).readCsv();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_project_contains_multiple_csv_files_then_an_exception_is_thrown() {
        final LinkedHashMap<String, ArchiveArtifact> artifactMap = new LinkedHashMap<>();

        final ArchiveArtifact first = new ArchiveArtifact("/folder_1/first.csv", "");
        artifactMap.put("/folder_1/first.csv", first);

        final ArchiveArtifact second = new ArchiveArtifact("/folder_2/second.CSV", "");
        artifactMap.put("/folder_2/second.CSV", second);

        final Archive pa = new Archive(artifactMap);
        new CsvReader(pa).readCsv();
    }

    @Test
    public void when_wrong_row_requested_then_exception_is_thrown() {
        final Archive pa = createProjectWithCsv("A,B,C,D,E,F");
        final CsvData data = new CsvReader(pa).readCsv();

        try {
            data.getValue(0, 0);
        } catch (final Exception e) {
            assertEquals("Wrong exception message", "Please choose a valid row", e.getMessage());
        }
    }

    @Test(expected = MalformedCsvException.class)
    public void when_csv_is_inconsistent_with_fewer_items_then_exception_is_thrown() {
        final Archive pa = createProjectWithCsv(
                "A,B,C,D,E,F",
                "A,B,C");
        new CsvReader(pa).readCsv();
    }

    @Test(expected = MalformedCsvException.class)
    public void when_csv_is_inconsistent_with_more_items_then_exception_is_thrown() {
        final Archive pa = createProjectWithCsv(
                "A,B,C,D,E,F",
                "A,B,C,D,E,F,G,H");

        new CsvReader(pa).readCsv();
    }

    @Test
    public void when_negative_row_requested_then_exception_is_thrown() {
        final Archive pa = createProjectWithCsv("A,B,C,D,E,F", "A,B,C,D,E,F");
        final CsvData data = new CsvReader(pa).readCsv();

        try {
            data.getValue(-1, 0);
        } catch (final Exception e) {
            assertEquals("Wrong exception message", "Please choose a valid row", e.getMessage());
        }
    }

    @Test
    public void when_wrong_column_requested_then_exception_is_thrown() {
        final Archive pa = createProjectWithCsv("A,B", "A,B");
        final CsvData data = new CsvReader(pa).readCsv();

        try {
            data.getValue(0, 2);
        } catch (final Exception e) {
            assertEquals("Wrong exception message", "Please choose a valid column", e.getMessage());
        }
    }

    @Test
    public void when_negative_column_requested_then_exception_is_thrown() {
        final Archive pa = createProjectWithCsv("A,B,C,D,E,F", "A,B,C,D,E,F");
        final CsvData data = new CsvReader(pa).readCsv();

        try {
            data.getValue(0, -1);
        } catch (final Exception e) {
            assertEquals("Wrong exception message", "Please choose a valid column", e.getMessage());
        }
    }

    @Test
    public void when_project_contains_empty_csv_file_nothing_is_returned() {
        final Archive pa = createProjectWithCsv("");

        final CsvData data = new CsvReader(pa).readCsv();
        assertEquals("Wrong header count", 0, data.getHeaderCount());
    }

    @Test
    public void when_project_contains_csv_only_with_headers_no_records_should_be_returned() {
        final Archive pa = createProjectWithCsv("A,B,C,D,E,F");
        final CsvData data = new CsvReader(pa).readCsv();

        assertEquals("Wrong header count", 6, data.getHeaderCount());
        assertEquals("Wrong header at index 0", "A", data.getHeader(0));
        assertEquals("Wrong header at index 1", "B", data.getHeader(1));
        assertEquals("Wrong header at index 2", "C", data.getHeader(2));
        assertEquals("Wrong header at index 3", "D", data.getHeader(3));
        assertEquals("Wrong header at index 4", "E", data.getHeader(4));
        assertEquals("Wrong header at index 4", "F", data.getHeader(5));

        assertEquals("Wrong row count", 0, data.getRowCount());
    }

    @Test
    public void when_project_contains_csv_all_lines_should_be_returned() {
        final Archive pa = createProjectWithCsv(
                "A,B,C,D,E,F",
                "A1,B1,C1,D1,E1,F1",
                "A2,B2,C2,D2,E2,F2",
                "A3,B3,C3,D3,E3,F3");

        final CsvData data = new CsvReader(pa).readCsv();

        assertEquals("Wrong header count", 6, data.getHeaderCount());

        assertEquals("Wrong row count", 3, data.getRowCount());
        assertEquals("Wrong value at row 0, col 0", "A1", data.getValue(0, 0));
        assertEquals("Wrong value at row 1, col 1", "B2", data.getValue(1, 1));
        assertEquals("Wrong value at row 2, col 5", "F3", data.getValue(2, 5));
    }

    @Test
    public void when_csv_has_line_without_value_the_value_should_return_empty() {
        final Archive pa = createProjectWithCsv(
                "A,B,C,D,E,F",
                "A1,,C1,D1,E1,F1",
                ",B2,C2,D2,E2,F2",
                "A3,,,D3,E3,F3");

        final CsvData data = new CsvReader(pa).readCsv();

        assertEquals("Wrong header count", 6, data.getHeaderCount());

        assertEquals("Wrong row count", 3, data.getRowCount());
        assertEquals("Wrong value at row 0, col 0", "A1", data.getValue(0, 0));
        assertEquals("Row 0, col 1 was not empty", "", data.getValue(0, 1));

        assertEquals("Wrong value at row 1, col 1", "B2", data.getValue(1, 1));
        assertEquals("Row 1, col 0 was not empty", "", data.getValue(1, 0));

        assertEquals("Wrong value at row 2, col 5", "F3", data.getValue(2, 5));
        assertEquals("Row 2, col 2 was not empty", "", data.getValue(2, 2));
    }

    @Test
    public void when_csv_header_has_commas_they_should_be_treated_correctly() {
        final Archive pa = createProjectWithCsv(
                "A,\"SINGLE,COLUMN,WITH,COMMAS\",\"ANOTHER,SINGLE,COLUMN\",B,C",
                "A1,A11,A12,B1,C1",
                "A2,A21,A22,B2,C2",
                "A3,A31,A32,B3,C3");

        final CsvData data = new CsvReader(pa).readCsv();

        assertEquals("Wrong header count", 5, data.getHeaderCount());

        assertEquals("Wrong row count", 3, data.getRowCount());
        assertEquals("Wrong value at row 0, col 0", "A1", data.getValue(0, 0));
        assertEquals("Wrong value at row 1, col 1", "A21", data.getValue(1, 1));
        assertEquals("Wrong value at row 2, col 4", "C3", data.getValue(2, 4));
    }

    @Test
    public void when_csv_line_has_commas_they_should_be_treated_correctly() {
        final Archive pa = createProjectWithCsv(
                "A,B,C,D,E,F",
                "\"A1,A1,A1\",B1,C1,D1,E1,F1",
                "A2,\"B2,B2,B2\",C2,D2,E2,F2",
                "A3,B3,C3,\"D3,D3\",E3,\"F3,F3,F3\"");

        final CsvData data = new CsvReader(pa).readCsv();

        assertEquals("Wrong header count", 6, data.getHeaderCount());

        assertEquals("Wrong row count", 3, data.getRowCount());
        assertEquals("Wrong value at row 0, col 0", "A1,A1,A1", data.getValue(0, 0));
        assertEquals("Wrong value at row 1, col 1", "B2,B2,B2", data.getValue(1, 1));
        assertEquals("Wrong value at row 2, col 5", "F3,F3,F3", data.getValue(2, 5));
    }

    @Test
    public void when_csv_has_special_characters_they_should_be_treated_correctly() {
        final Archive pa = createProjectWithCsv(
                "A,B,C,D,E,F",
                "\"A\r\nA\r\nA\r\n\",B1,C1,D1,E1,F1",
                "A2,\u0021,C2,D2,E2,F2",
                "A3,B3,C3,D3,E3,\u6000");

        final CsvData data = new CsvReader(pa).readCsv();

        assertEquals("Wrong header count", 6, data.getHeaderCount());

        assertEquals("Wrong row count", 3, data.getRowCount());
        assertEquals("Wrong value at row 0, col 0", "A\r\nA\r\nA\r\n", data.getValue(0, 0));
        assertEquals("Wrong value at row 1, col 1", "\u0021", data.getValue(1, 1));
        assertEquals("Wrong value at row 2, col 5", "\u6000", data.getValue(2, 5));
    }

    private Archive createProjectWithCsv(final String header, final String... lines) {
        final Map<String, ArchiveArtifact> artifactMap = new LinkedHashMap<>();
        final StringBuilder content = new StringBuilder(header);
        for (final String line : lines) {
            content.append("\r\n").append(line);
        }

        final ArchiveArtifact artifact = new ArchiveArtifact("/folder/file.csv", content.toString());
        artifactMap.put(artifact.getAbsoluteName(), artifact);

        return new Archive(artifactMap);
    }
}