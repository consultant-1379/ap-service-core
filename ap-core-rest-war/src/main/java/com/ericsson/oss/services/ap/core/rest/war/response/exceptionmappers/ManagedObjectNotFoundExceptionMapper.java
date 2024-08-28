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

import com.ericsson.oss.services.ap.api.exception.ManagedObjectNotFoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>ManagedObjectNotFoundException</code> to <code>Response</code>.
 */
@Provider
public class ManagedObjectNotFoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ManagedObjectNotFoundException>, javax.ws.rs.ext.ExceptionMapper<ManagedObjectNotFoundException> {

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String ERROR_MESSAGE_KEY = "mo.not.found";
    private static final String SOLUTION_MESSAGE_KEY = "mo.not.found.solution";

    @Override
    public ErrorResponse toErrorResponse(final ManagedObjectNotFoundException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_MESSAGE_KEY, exception.getMoType(), exception.getNetworkElementName()))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withHttpResponseStatus(Response.Status.NOT_FOUND.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final ManagedObjectNotFoundException ex) {
        return notFound(this.toErrorResponse(ex, null));
    }
}