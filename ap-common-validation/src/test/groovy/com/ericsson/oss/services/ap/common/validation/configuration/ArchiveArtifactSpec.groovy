/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.ap.common.validation.configuration

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotEquals

import java.nio.charset.StandardCharsets

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class ArchiveArtifactSpec extends CdiSpecification {
    private static final String ARCHIVE_NAME = "file.zip"
    private static final String ARCHIVE_FILEPATH = "filepath/" + ARCHIVE_NAME
    private static final String ARCHIVE_STRING_CONTENTS = "fileContents"
    private static final byte[] ARCHIVE_BYTE_CONTENTS = ARCHIVE_STRING_CONTENTS.getBytes(StandardCharsets.UTF_8)

    private final ArchiveArtifact archiveStringArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, ARCHIVE_STRING_CONTENTS)
    private final ArchiveArtifact archiveByteArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, ARCHIVE_BYTE_CONTENTS)

    def "when getName is called then name of artifact is returned without its filepath"() {
        given: "String/ZIP based artifact"

        when: "Getting the artifact name"

        then: "Artifact name is returned without file path"
            assertEquals(ARCHIVE_NAME, archiveStringArtifact.getName())
            assertEquals(ARCHIVE_NAME, archiveByteArtifact.getName())
    }

    def "when getAbsoluteName is called then name of artifact is returned with its filepath"() {
        given: "String/ZIP based artifact"

        when: "Getting the artifacts absolute name"

        then: "Artifact name is returned with file path"
            assertEquals(ARCHIVE_FILEPATH, archiveStringArtifact.getAbsoluteName())
            assertEquals(ARCHIVE_FILEPATH, archiveByteArtifact.getAbsoluteName())
    }

    def "when two artifacts are compared and all fields are the same then both are equal"() {
        given: "String/ZIP based artifact and identical artifacts"
            final ArchiveArtifact secondArchiveStringArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, ARCHIVE_STRING_CONTENTS)
            final ArchiveArtifact secondArchiveByteArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, ARCHIVE_BYTE_CONTENTS)

        when: "Comparing for equality"

        then: "Both original and identical artifacts are equal"
            assertEquals(archiveStringArtifact, secondArchiveStringArtifact)
            assertEquals(archiveByteArtifact, secondArchiveByteArtifact)
    }

    def "when two artifacts are compared and fields are different then both are not equal"() {
        given: "String/ZIP based artifact and identical artifacts"
            final ArchiveArtifact secondArchiveStringArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, "newContents")
            final ArchiveArtifact secondArchiveByteArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, "newContents".getBytes(StandardCharsets.UTF_8))

        when: "Comparing for equality"

        then: "Both original and identical artifacts are not equal"
            assertNotEquals(archiveStringArtifact, secondArchiveStringArtifact)
            assertNotEquals(archiveByteArtifact, secondArchiveByteArtifact)
    }

    def "when two artifacts are compared and second artifact is null then both are not equal"() {
        given: "String/ZIP based artifact"

        when: "Comparing for equality"

        then: "Both artifacts are not equal"
            assertNotEquals(archiveStringArtifact, null)
            assertNotEquals(archiveByteArtifact, null)
    }

    def "when two artifacts are compared and second artifact is a different object then both are not equal"() {
        given: "String/ZIP based artifact"

        when: "Comparing for equality"

        then: "Both artifacts are not equal"
            assertNotEquals(archiveStringArtifact, new Object())
            assertNotEquals(archiveByteArtifact, new Object())
    }

    def "when two artifacts compared and both are equal then hashcodes are equal"() {
        given: "String/ZIP based artifact and identical artifacts"
            final ArchiveArtifact secondArchiveStringArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, ARCHIVE_STRING_CONTENTS)
            final ArchiveArtifact secondArchiveByteArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, ARCHIVE_BYTE_CONTENTS)

        when: "Comparing for equality"

        then: "Both original and identical artifact hashcodes are equal"
            assertEquals(archiveStringArtifact.hashCode(), secondArchiveStringArtifact.hashCode())
            assertEquals(archiveByteArtifact.hashCode(), secondArchiveByteArtifact.hashCode())
    }

    def "when two artifacts compared and both are not equal then hashcodes are not equal"() {
        given: "String/ZIP based artifact and identical artifacts"
            final ArchiveArtifact secondArchiveStringArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, "newContents")
            final ArchiveArtifact secondArchiveByteArtifact = new ArchiveArtifact(ARCHIVE_FILEPATH, "newContents".getBytes(StandardCharsets.UTF_8))

        when: "Comparing for equality"

        then: "Both original and identical artifact hashcodes are not equal"
            assertNotEquals(archiveStringArtifact.hashCode(), secondArchiveStringArtifact.hashCode())
            assertNotEquals(archiveByteArtifact.hashCode(), secondArchiveByteArtifact.hashCode())
    }
}
