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
package com.ericsson.oss.services.ap.core.usecase.archive;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Unit tests for {@link ArchiveReader}.
 */
public class ArchiveReaderTest {

    private static final String NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String PROJECTINFO_FILENAME = "projectInfo.xml";

    @Test(expected = IOException.class)
    public void whenInputIsEmptyThenIoExceptionIsThrown() throws IOException {
        ArchiveReader.read(new byte[0]);
    }

    @Test
    public void whenInputIsValidAndProjectHasNoDirectoriesThenArchiveIsReturned() throws IOException {
        try (final InputStream archiveStream = this.getClass().getResourceAsStream("/projectWithNoDirectories.zip")) {
            final byte[] archiveContents = IOUtils.toByteArray(archiveStream);
            final Archive archive = ArchiveReader.read(archiveContents);
            archiveStream.close();

            assertEquals(PROJECTINFO_FILENAME, archive.getAllArtifacts().get(0).getName());
        }
    }

    @Test
    public void whenInputIsValidAndProjectHasDirectoriesThenArchiveIsReturned() throws IOException {
        try (final InputStream archiveStream = this.getClass().getResourceAsStream("/projectWithDirectories.zip")) {
            final byte[] archiveContents = IOUtils.toByteArray(archiveStream);
            final Archive archive = ArchiveReader.read(archiveContents);
            archiveStream.close();

            final List<ArchiveArtifact> archiveArtifacts = archive.getAllArtifacts();
            assertEquals(2, archiveArtifacts.size());
            assertEquals(NODEINFO_FILENAME, archiveArtifacts.get(0).getName());
            assertEquals(PROJECTINFO_FILENAME, archiveArtifacts.get(1).getName());
        }
    }
}
