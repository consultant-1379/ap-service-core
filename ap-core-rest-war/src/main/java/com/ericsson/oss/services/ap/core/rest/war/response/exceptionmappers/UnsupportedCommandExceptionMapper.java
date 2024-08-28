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

import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link UnsupportedCommandException} to {@link Response}.
 */
@Provider
public class UnsupportedCommandExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<UnsupportedCommandException>,
    javax.ws.rs.ext.ExceptionMapper<UnsupportedCommandException> {

    private static final String ERROR_MESSAGE_KEY = "action.not.supported.for.type";

    @Override
    public ErrorResponse toErrorResponse(final UnsupportedCommandException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.get(ERROR_MESSAGE_KEY))
            .withHttpResponseStatus(Response.Status.FORBIDDEN.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final UnsupportedCommandException ex) {
        return forbidden(this.toErrorResponse(ex, null));
    }
}
