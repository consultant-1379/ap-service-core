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

import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link IllegalArgumentException} to {@link Response}.
 */
@Provider
public class IllegalArgumentExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<IllegalArgumentException>, javax.ws.rs.ext.ExceptionMapper<IllegalArgumentException> {

    private static final String ERROR_MESSAGE_KEY = "action.not.supported.for.type";

    @Override
    public ErrorResponse toErrorResponse(final IllegalArgumentException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_MESSAGE_KEY))
            .withHttpResponseStatus(Response.Status.BAD_REQUEST.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final IllegalArgumentException ex) {
        return badRequest(this.toErrorResponse(ex, null));
    }
}
