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
package com.ericsson.oss.services.ap.common.artifacts;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ArtifactDetails}.
 */
public class ArtifactDetailsTest {

    private static final String FILE_NAME = "fileName";
    private static final String FILE_NAME_WITH_EXTENSION = FILE_NAME + ".xml";
    private static final String FILE_TYPE = "fileType";
    private static final String LOCATION = "fileLocation";

    private static final String FILE_CONTENT_STRING = "fileContent";
    private static final byte[] FILE_CONTENT_BYTES = FILE_CONTENT_STRING.getBytes();
    private static final List<String> importErrorMessage = Arrays.asList("1", "Import Failure");

    private ArtifactDetails artifactDetails;

    @Before
    public void setUp() {
        artifactDetails = new ArtifactDetails.ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .name(FILE_NAME_WITH_EXTENSION)
            .encrypted(false)
            .exportable(true)
            .location(LOCATION)
            .type(FILE_TYPE)
            .artifactContent(FILE_CONTENT_STRING)
            .importProgress(ArtifactImportProgress.IN_PROGRESS)
            .importErrorMsg(importErrorMessage)
            .ignoreError(false)
            .build();
    }

    @Test
    public void whenGetNameThenNameIsReturnedWithoutFileExtension() {
        assertEquals("fileName", artifactDetails.getName());
    }

    @Test
    public void whenGetNameWithExtensionThenNameIsReturnedWithFileExtension() {
        assertEquals(FILE_NAME_WITH_EXTENSION, artifactDetails.getNameWithExtension());
    }

    @Test
    public void whenGetFileExtensionThenFileExtensionIsReturned() {
        assertEquals("xml", artifactDetails.getExtension());
    }

    @Test
    public void whenGetFdnThenFdnIsReturned() {
        assertEquals(NODE_FDN, artifactDetails.getApNodeFdn());
    }

    @Test
    public void whenGetTypeThenTypeIsReturned() {
        assertEquals(FILE_TYPE, artifactDetails.getType());
    }

    @Test
    public void whenGetLocationThenLocationIsReturned() {
        assertEquals(LOCATION, artifactDetails.getLocation());
    }

    @Test
    public void whenGetIsExportableAndExportableIsTrueThenTrueIsReturned() {
        assertTrue(artifactDetails.isExportable());
    }

    @Test
    public void whenGetIsEncryptedAndEncryptedIsFalseThenFalseIsReturned() {
        assertFalse(artifactDetails.isEncrypted());
    }

    @Test
    public void whenGetArtifactAsBytesAndInputContentIsStringThenContentsAreReturnedAsBytes() {
        final ArtifactDetails artifactDetails2 = new ArtifactDetails.ArtifactBuilder().artifactContent(FILE_CONTENT_STRING).build();
        assertArrayEquals(FILE_CONTENT_BYTES, artifactDetails2.getArtifactContentAsBytes());
    }

    @Test
    public void whenGetArtifactAsBytesAndInputContentIsBytesThenContentsAreReturnedAsBytes() {
        final ArtifactDetails artifactDetails3 = new ArtifactDetails.ArtifactBuilder().artifactContent(FILE_CONTENT_BYTES).build();
        assertArrayEquals(FILE_CONTENT_BYTES, artifactDetails3.getArtifactContentAsBytes());
    }

    @Test
    public void whenGetArtifactAsStringAndInputContentIsStringThenContentsAreReturnedAsString() {
        final ArtifactDetails artifactDetails4 = new ArtifactDetails.ArtifactBuilder().artifactContent(FILE_CONTENT_STRING).build();
        assertEquals(FILE_CONTENT_STRING, artifactDetails4.getArtifactContent());
    }

    @Test
    public void whenGetArtifactAsStringAndInputContentIsBytesThenContentsAreReturnedAsString() {
        final ArtifactDetails artifactDetails5 = new ArtifactDetails.ArtifactBuilder().artifactContent(FILE_CONTENT_BYTES).build();
        assertEquals(FILE_CONTENT_STRING, artifactDetails5.getArtifactContent());
    }

    @Test
    public void whenGetArtifactContentAndInputIsNullThenNullIsReturned() {
        final String content = null;
        final ArtifactDetails artifactDetails6 = new ArtifactDetails.ArtifactBuilder().artifactContent(content).build();
        assertNull(artifactDetails6.getArtifactContent());
    }

    @Test
    public void whenGetFileNameWithoutExtensionAndInputContentHasNoExtensionThenNameIsReturned() {
        final ArtifactDetails artifactDetails7 = new ArtifactDetails.ArtifactBuilder().name(FILE_NAME).build();
        assertEquals(artifactDetails7.getNameWithExtension(), artifactDetails.getName());
    }

    @Test
    public void whenGetFileExtenstionAndInputContentHasNoExtensionThenNullIsReturned() {
        final ArtifactDetails artifactDetails8 = new ArtifactDetails.ArtifactBuilder().name(FILE_NAME).build();
        assertNull(artifactDetails8.getExtension());
    }

    @Test
    public void whenCompareTwoArtifactDetailsWithSameAttributesThenTrueIsReturned() {
        final ArtifactDetails artifactDetails9 = new ArtifactDetails.ArtifactBuilder()
                .apNodeFdn(NODE_FDN)
                .name(FILE_NAME_WITH_EXTENSION)
                .encrypted(false)
                .exportable(true)
                .location(LOCATION)
                .type(FILE_TYPE)
                .importProgress(ArtifactImportProgress.IN_PROGRESS)
                .importErrorMsg(importErrorMessage)
                .ignoreError(false)
                .artifactContent(FILE_CONTENT_STRING)
                .build();
        assertTrue(artifactDetails9.equals(artifactDetails));
        assertTrue(artifactDetails9.hashCode() == artifactDetails.hashCode());
    }
}
