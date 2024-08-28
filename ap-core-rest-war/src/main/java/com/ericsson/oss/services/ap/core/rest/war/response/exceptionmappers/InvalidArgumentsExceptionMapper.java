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
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ericsson.oss.services.ap.api.exception.InvalidArgumentsException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>InvalidArgumentsException</code> to <code>Response</code>.
 */
@Provider
public class InvalidArgumentsExceptionMapper extends RestExceptionMapper
implements ExceptionMapper<InvalidArgumentsException>, javax.ws.rs.ext.ExceptionMapper<InvalidArgumentsException> {

    private static final String ERROR_TITLE = "invalid.arguments.title";
    private static final String SUGGESTED_SOLUTION = "invalid.arguments.suggested_solution";

    @Override
    public ErrorResponse toErrorResponse(final InvalidArgumentsException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.get(ERROR_TITLE))
            .withErrorBody(String.format("%s %s", exception.getMessage(), apUiMessages.get(SUGGESTED_SOLUTION)))
            .withHttpResponseStatus(exception.getHttpCode())
            .build();
    }

    @Override
    public Response toResponse(final InvalidArgumentsException ex) {
        return badRequest(this.toErrorResponse(ex, null));
    }
}
