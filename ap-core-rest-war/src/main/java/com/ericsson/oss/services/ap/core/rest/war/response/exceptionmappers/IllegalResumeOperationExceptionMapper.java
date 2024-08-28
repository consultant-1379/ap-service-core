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

import com.ericsson.oss.services.ap.api.exception.IllegalResumeOperationException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>InvalidNodeStateException</code> to <code>Response</code>.
 */
@Provider
public class IllegalResumeOperationExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<IllegalResumeOperationException>,
    javax.ws.rs.ext.ExceptionMapper<IllegalResumeOperationException> {

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String ERROR_MESSAGE_KEY = "not.waiting.for.cancel.resume";
    private static final String SOLUTION_MESSAGE_KEY = "not.waiting.for.cancel.resume.solution";

    @Override
    public ErrorResponse toErrorResponse(final IllegalResumeOperationException exception, final String usecase) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_MESSAGE_KEY))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withHttpResponseStatus(Response.Status.FORBIDDEN.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final IllegalResumeOperationException ex) {
        return forbidden(this.toErrorResponse(ex, null));
    }
}
