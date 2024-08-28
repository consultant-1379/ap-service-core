/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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
 * The Enum constant that represents the node Artifact Format.
 */
public enum ArtifactFileFormat {

    UNKNOWN,
    BULK_3GPP,
    NETCONF,
    AMOS_SCRIPT,
    JSON;

    private static final List<ArtifactFileFormat> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));

    /**
     * Get the matching {@link ArtifactFileFormat} for a {@link String} representing a {@link ArtifactFileFormat}.
     *
     * @param fileFormat
     *            the {@link String} representation of the {@link ArtifactFileFormat}
     * @return the matching {@link ArtifactFileFormat}
     */
    public static ArtifactFileFormat getFileFormat(final String fileFormat) {
        for (final ArtifactFileFormat format : VALUES_AS_LIST) {
            if (format.name().equals(fileFormat)) {
                return format;
            }
        }

        if (fileFormat == null) {
            return UNKNOWN;
        }

        throw new ApApplicationException(String.format("Invalid ArtifactFileFormat %s specified", fileFormat));
    }
}
