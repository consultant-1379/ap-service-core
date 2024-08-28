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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.archive.exception.MalformedCsvException;

/**
 * Reads a csv file contained within a project archive.
 */
public class CsvReader {

    private static final String CSV_FILE_EXTENSION_PATTERN = "^.+\\.csv";

    private final Archive archive;

    public CsvReader(final Archive archive) {
        this.archive = archive;
    }

    /**
     * Reads the csv file contained in the project archive
     *
     * @return the {@link CsvData} that encapsulates the data read from the csv
     */
    public CsvData readCsv() {
        final List<ArchiveArtifact> csvFilesInArchive = archive.getArtifactsByPattern(CSV_FILE_EXTENSION_PATTERN);
        if (csvFilesInArchive.isEmpty()) {
            throw new IllegalArgumentException("Csv file not found in archive");
        }

        if (csvFilesInArchive.size() > 1) {
            throw new IllegalArgumentException("Multiple csv files found in archive");
        }

        final String csvFile = csvFilesInArchive.get(0).getContentsAsString();
        return readData(csvFile);
    }

    private static CsvData readData(final String csv) {
        try (final CSVParser parser = CSVParser.parse(csv, CSVFormat.EXCEL)) {
            final List<String[]> data = new ArrayList<>();

            String[] headers = null;
            for (final CSVRecord csvRecord : parser.getRecords()) {
                if (headers == null) {
                    headers = extractHeaders(csvRecord);
                    data.add(headers);
                } else {
                    data.add(extractRow(headers, csvRecord));
                }
            }

            if (headers == null) {
                return new CsvData(new String[0][0]);
            }

            return new CsvData(data.toArray(new String[data.size()][headers.length]));
        } catch (final IOException e) {
            throw new IllegalArgumentException(String.format("Error reading csv file: %s", csv), e);
        }
    }

    private static String[] extractRow(final String[] headers, final CSVRecord record) {
        final int numberOfHeaders = headers.length;
        if (record.size() != numberOfHeaders) {
            throw new MalformedCsvException(String.format("Expected %d columns, but found %d", numberOfHeaders, record.size()));
        }

        final String[] row = new String[numberOfHeaders];
        for (int i = 0; i < numberOfHeaders; i++) {
            row[i] = record.get(i);
        }

        return row;
    }

    private static String[] extractHeaders(final CSVRecord csvRecord) {
        final String[] headers = new String[csvRecord.size()];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = csvRecord.get(i);
        }

        return headers;
    }
}
