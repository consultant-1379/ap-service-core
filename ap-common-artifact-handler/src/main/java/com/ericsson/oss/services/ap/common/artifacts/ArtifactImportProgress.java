/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.artifacts;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;

/**
 * The Enum constant that represents the node Artifact Import Progress.
 */
public enum ArtifactImportProgress {

    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED;

    private static final List<ArtifactImportProgress> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));

    /**
     * Get the matching {@link ArtifactImportProgress} for a {@link String} representing a {@link ArtifactImportProgress}.
     *
     * @param importProgress
     *            the {@link String} representation of the {@link ArtifactImportProgress}
     * @return the matching {@link ArtifactImportProgress}
     */
    public static ArtifactImportProgress getImportProgress(final String importProgress) {
        for (final ArtifactImportProgress progress : VALUES_AS_LIST) {
            if (progress.name().equals(importProgress)) {
                return progress;
            }
        }

        if (importProgress == null) {
            return NOT_STARTED;
        }

        throw new ApApplicationException(String.format("Invalid ArtifactImportProgress %s specified", importProgress));
    }
}
