/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
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

import com.ericsson.oss.services.ap.api.exception.ApNodeExistsException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>ApNodeExistsException</code> to <code>Response</code>.
 */
@Provider
public class ApNodeExistsExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ApNodeExistsException>, javax.ws.rs.ext.ExceptionMapper<ApNodeExistsException> {

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String ERROR_MESSAGE_KEY = "ap.node.exists";
    private static final String SOLUTION_MESSAGE_KEY = "ap.node.exists.solution";

    @Override
    public ErrorResponse toErrorResponse(final ApNodeExistsException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_MESSAGE_KEY, exception.getNodename()))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withHttpResponseStatus(Response.Status.CONFLICT.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final ApNodeExistsException ex) {
        return conflict(this.toErrorResponse(ex, null));
    }
}
