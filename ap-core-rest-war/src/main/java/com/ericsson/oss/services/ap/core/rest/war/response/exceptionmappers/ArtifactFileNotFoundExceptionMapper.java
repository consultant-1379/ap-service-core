/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import com.ericsson.oss.services.ap.api.exception.ArtifactFileNotFoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Maps <code>ArtifactFileNotFoundException</code> to <code>Response</code>.
 */
@Provider
public class ArtifactFileNotFoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ArtifactFileNotFoundException>, javax.ws.rs.ext.ExceptionMapper<ArtifactFileNotFoundException> {

    private static final String ERROR_MESSAGE_KEY = "validation.project.error";
    private static final String SOLUTION_MESSAGE_KEY = "validation.project.error.solution";
    private static final String SUGGESTED_SOLUTION = "suggested.solution";

    @Override
    public ErrorResponse toErrorResponse(final ArtifactFileNotFoundException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_MESSAGE_KEY) + " " + exception.getMessage())
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withHttpResponseStatus(Response.Status.BAD_REQUEST.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final ArtifactFileNotFoundException ex) {
        return forbidden(this.toErrorResponse(ex, null));
    }

}
