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
package com.ericsson.oss.services.ap.core.usecase.archive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Unit tests for {@link Archive}.
 */
public class ArchiveTest {

    private static final String FILE1_CONTENTS = "contents1";
    private static final String FILE2_CONTENTS = "contents2";
    private static final String FILE3_CONTENTS = "contents3";

    private static final String DIR1 = "/tmp1";
    private static final String DIR2 = "/tmp2";

    private static final String FILE1 = "art1";
    private static final String FILE2 = "art2";
    private static final String FORWARD_SLASH = "/";

    private static final String ABSOLUTE_PATH_DIR1_FILE1 = DIR1 + FORWARD_SLASH + FILE1;
    private static final String ABSOLUTE_PATH_DIR1_FILE2 = DIR1 + FORWARD_SLASH + FILE2;
    private static final String ABSOLUTE_PATH_DIR2_FILE1 = DIR2 + FORWARD_SLASH + FILE1;

    private Archive archive;

    @Before
    public void setUp() {
        final ArchiveArtifact artifact1 = new ArchiveArtifact(ABSOLUTE_PATH_DIR1_FILE1, FILE1_CONTENTS);
        final ArchiveArtifact artifact2 = new ArchiveArtifact(ABSOLUTE_PATH_DIR1_FILE2, FILE2_CONTENTS);
        final ArchiveArtifact artifact3 = new ArchiveArtifact(ABSOLUTE_PATH_DIR2_FILE1, FILE3_CONTENTS);

        final Map<String, ArchiveArtifact> archiveMap = new LinkedHashMap<>();
        archiveMap.put(ABSOLUTE_PATH_DIR1_FILE1, artifact1);
        archiveMap.put(ABSOLUTE_PATH_DIR1_FILE2, artifact2);
        archiveMap.put(ABSOLUTE_PATH_DIR2_FILE1, artifact3);

        archive = new Archive(archiveMap);
    }

    @Test
    public void whenGetArtifactsInDirThenAllArtifactsReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsInDirectory(DIR1);
        assertEquals(2, result.size());

        for (final ArchiveArtifact archiveArtifact : result) {
            final String archiveArtifactName = archiveArtifact.getName();
            assertTrue(archiveArtifactName.equals(FILE1) || archiveArtifactName.equals(FILE2));
        }
    }

    @Test
    public void whenGetArtifactsInDirAndDirExistsThenNoArtifactsReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsInDirectory("/xxx");
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenGetArtifactContentsThenContentsReturned() {
        final String result = archive.getArtifactContentAsString(ABSOLUTE_PATH_DIR1_FILE1);
        assertEquals(FILE1_CONTENTS, result);
    }

    @Test
    public void whenGetArtifactContentsRequestedInDifferentCaseThenContentsReturned() {
        final String result = archive.getArtifactContentAsString(DIR1.toUpperCase(Locale.US) + "/ART1");
        assertEquals(FILE1_CONTENTS, result);
    }

    @Test
    public void whenGetArtifactContentsAndArtifactDoesNotExitsThenNullReturned() {
        final String result = archive.getArtifactContentAsString("/xxx/yyy");
        assertNull(result);
    }

    @Test
    public void whenGetArtifactOfNameThenAllArtifactsOfThatNameInAnyDirReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsOfName(FILE1);
        assertEquals(2, result.size());

        for (final ArchiveArtifact archiveArtifact : result) {
            assertEquals(FILE1, archiveArtifact.getName());
        }
    }

    @Test
    public void whenGetArtifactOfNameRequestedInDifferentCaseThenAllArtifactsOfThatNameInAnyDirReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsOfName(FILE1.toUpperCase(Locale.US));
        assertEquals(2, result.size());

        for (final ArchiveArtifact archiveArtifact : result) {
            assertEquals(FILE1, archiveArtifact.getName());
        }
    }

    @Test
    public void whenGetArtifactOfNonExistingNameThenNullReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsOfName("xxx");
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenGetArtifactOfNameInDirThenArtifactReturned() {
        final ArchiveArtifact result = archive.getArtifactOfNameInDir(DIR1, FILE2);
        assertEquals(FILE2, result.getName());
    }

    @Test
    public void whenGetArtifactOfNameInDirRequestedInDifferentCaseThenArtifactReturned() {
        final ArchiveArtifact result = archive.getArtifactOfNameInDir(DIR1.toUpperCase(Locale.US), FILE2.toUpperCase(Locale.US));
        assertEquals(FILE2, result.getName());
    }

    @Test
    public void whenGetArtifactOfNameInNonExistingDirThenNullReturned() {
        final ArchiveArtifact result = archive.getArtifactOfNameInDir("/xxx", FILE2);
        assertNull(result);
    }

    @Test
    public void whenGetArtifactOfNameOfNonExistingFileThenNullReturned() {
        final ArchiveArtifact result = archive.getArtifactOfNameInDir(DIR1, "xxx");
        assertNull(result);
    }

    @Test
    public void whenGetArtifactByPatternThenAllArtifactsOfThatNameInAnyDirReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsByPattern(".*1");
        assertEquals(2, result.size());

        for (final ArchiveArtifact archiveArtifact : result) {
            final String archiveArtifactContents = archiveArtifact.getContentsAsString();
            assertTrue(archiveArtifactContents.equals(FILE1_CONTENTS) || archiveArtifactContents.equals(FILE3_CONTENTS));
        }
    }

    @Test
    public void whenGetArtifactByPatternRequestedInDifferentCaseThenAllArtifactsOfThatNameInAnyDirReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsByPattern(DIR1.toUpperCase(Locale.US) + ".*");
        assertEquals(2, result.size());

        for (final ArchiveArtifact archiveArtifact : result) {
            final String archiveArtifactContents = archiveArtifact.getContentsAsString();
            assertTrue(archiveArtifactContents.equals(FILE1_CONTENTS) || archiveArtifactContents.equals(FILE2_CONTENTS));
        }
    }

    @Test
    public void whenGetArtifactByPatternAndNoPatternMatchesThenNoArtifactsReturned() {
        final List<ArchiveArtifact> result = archive.getArtifactsByPattern("xxx");
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenGetNumberOfArtifactsRequestedThenNumberOfArtifactsReturned() {
        final int result = archive.getNumberOfArtifacts();
        assertEquals(3, result);
    }

    @Test
    public void whenGetAllArtifactsRequestedThenAllArtifactsReturned() {
        final List<ArchiveArtifact> result = archive.getAllArtifacts();
        assertEquals(3, result.size());
    }
}
