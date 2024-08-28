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
package com.ericsson.oss.services.ap.api.exception;

/**
 * Throw to indicate that the artifact type is unsupported for a given operation.
 */
public class UnsupportedArtifactTypeException extends ApApplicationException {

    private static final long serialVersionUID = -7737885960842782652L;

    private final String unsupportedArtifactType;

    /**
     * Exception with message information and the invalid artifact type.
     *
     * @param message
     *            description of the exception
     * @param unsupportedArtifactType
     *            the artifact type that is not supported
     */
    public UnsupportedArtifactTypeException(final String message, final String unsupportedArtifactType) {
        super(message);
        this.unsupportedArtifactType = unsupportedArtifactType;
    }

    /**
     * The invalid artifact type.
     *
     * @return the artifact type
     */
    public String getUnsupportedArtifactType() {
        return unsupportedArtifactType;
    }
}
