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

import com.ericsson.oss.services.ap.api.exception.ProjectExistsException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>ProjectExistsException</code> to <code>Response</code>.
 */
@Provider
public class ProjectExistsExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ProjectExistsException>, javax.ws.rs.ext.ExceptionMapper<ProjectExistsException> {

    private static final String ERROR_TITLE = "validation.project.exists.failure";
    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String SOLUTION_MESSAGE_KEY = "validation.project.exists.solution";

    @Override
    public ErrorResponse toErrorResponse(final ProjectExistsException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_TITLE, exception.getProjectName()))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withHttpResponseStatus(Response.Status.CONFLICT.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final ProjectExistsException ex) {
        return conflict(this.toErrorResponse(ex, null));
    }
}
