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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link BatchArchiveProcessor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchArchiveProcessorTest {

    private static final ArchiveArtifact CSV_ARTIFACT = new ArchiveArtifact("csvFile.csv", "csvContent");
    private static final ArchiveArtifact PROJECT_INFO_ARTIFACT = new ArchiveArtifact("projectInfo.xml", "projectInfoContent");


    @InjectMocks
    private BatchArchiveProcessor batchArchiveProcessor;

    @Test
    public void whenProcessingArchive_thenReturnATransformedArchive() {
        final Archive archive = getValidBatchArchive();
        final Archive result = batchArchiveProcessor.process(archive);
        assertNotNull(result);
    }

    @Test
    public void whenProcessingArchive_andNoProjectInfoFilesAreFound_thenIllegalArgumentExceptionIsThrown() {
        final Map<String, ArchiveArtifact> archiveContents = new HashMap<>();
        archiveContents.put("csvFile.csv", CSV_ARTIFACT);
        final Archive archive = new Archive(archiveContents);

        verifyException(batchArchiveProcessor).process(archive);
        assertThat((Exception) caughtException())
            .as("No projectInfo.xml file found")
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No projectInfo.xml file found");
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenProcessingArchive_andMultipleProjectInfoFilesAreFound_thenIllegalArgumentExceptionIsThrown() {
        final Map<String, ArchiveArtifact> archiveContents = new HashMap<>();
        archiveContents.put("projectInfo.xml", PROJECT_INFO_ARTIFACT);
        archiveContents.put("projectInfo2.xml", PROJECT_INFO_ARTIFACT);
        archiveContents.put("csvFile.csv", CSV_ARTIFACT);
        final Archive archive = new Archive(archiveContents);

        batchArchiveProcessor.process(archive);
    }

    @Test
    public void whenValidatingBatch_andProjectContainsOneCsvFile_thenReturnTrue() {
        final Archive archive = getValidBatchArchive();
        final boolean result = batchArchiveProcessor.isBatch(archive);
        assertTrue(result);
    }

    @Test
    public void whenValidatingBatch_andProjectHasNoArtifactContent_thenReturnFalse() {
        final Map<String, ArchiveArtifact> archiveContents = new HashMap<>();
        final Archive archive = new Archive(archiveContents);

        final boolean result = batchArchiveProcessor.isBatch(archive);

        assertFalse(result);
    }

    private Archive getValidBatchArchive() {
        final Map<String, ArchiveArtifact> archiveContents = new HashMap<>();
        archiveContents.put("projectInfo.xml", PROJECT_INFO_ARTIFACT);
        archiveContents.put("csvFile.csv", CSV_ARTIFACT);
        return new Archive(archiveContents);
    }
}
