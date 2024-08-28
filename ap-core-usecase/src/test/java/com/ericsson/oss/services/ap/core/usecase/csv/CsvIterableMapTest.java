/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link CsvIterableMap}.
 */
public class CsvIterableMapTest {

    private static final String DEFAULT_DATA = "data1,data2";
    private static final String HEADERS = "header1,header2";

    @Test
    public void whenGetMapAndThereIsNoDataThenEmptyMapIsReturned() {
        final CsvIterableMap csvMap = createCsvMapWithData();
        assertTrue(csvMap.getMap().isEmpty());
    }

    @Test
    public void whenGetMapThenMapIsReturnedWithCorrectData() {
        final CsvIterableMap csvMap = createCsvMapWithData(DEFAULT_DATA);
        csvMap.next();
        final Map<String, String> actualMap = csvMap.getMap();

        assertEquals("data2", actualMap.get("header2"));
    }

    @Test
    public void whenCheckIfMapHasNextAndMapHasAnotherEntryThenTrueIsReturned() {
        final CsvIterableMap csvMap = createCsvMapWithData(DEFAULT_DATA);
        assertTrue(csvMap.hasNext());
    }

    @Test
    public void whenCheckIfMapHasNextAndMapHasNoEntryThenFalseIsReturned() {
        final CsvIterableMap csvMap = createCsvMapWithData();
        assertFalse(csvMap.hasNext());
    }

    private CsvIterableMap createCsvMapWithData(final String... data) {
        final Map<String, ArchiveArtifact> artifactMap = new LinkedHashMap<>();

        final StringBuilder content = new StringBuilder(HEADERS);
        for (final String line : data) {
            content.append("\r\n").append(line);
        }

        final ArchiveArtifact artifact = new ArchiveArtifact("/folder/file.csv", content.toString());
        artifactMap.put(artifact.getAbsoluteName(), artifact);

        final CsvData csvData = new CsvReader(new Archive(artifactMap)).readCsv();
        return new CsvIterableMap(csvData);
    }
}