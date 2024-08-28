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
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ericsson.oss.services.ap.api.exception.UnsupportedArtifactTypeException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link UnsupportedArtifactTypeException} to {@link Response}.
 */
@Provider
public class UnsupportedArtifactTypeExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<UnsupportedArtifactTypeException>,
    javax.ws.rs.ext.ExceptionMapper<UnsupportedArtifactTypeException> {

    private static final String MESSAGE_KEY = "upload.unsupported.type";
    private static final String MESSAGE_KEY_SOLUTION = "upload.unsupported.type.solution";
    private static final String SUGGESTED_SOLUTION = "suggested.solution";

    @Override
    public ErrorResponse toErrorResponse(final UnsupportedArtifactTypeException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(MESSAGE_KEY, exception.getUnsupportedArtifactType()))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(MESSAGE_KEY_SOLUTION)))
            .withHttpResponseStatus(Response.Status.CONFLICT.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final UnsupportedArtifactTypeException ex) {
        return conflict(this.toErrorResponse(ex, null));
    }
}
