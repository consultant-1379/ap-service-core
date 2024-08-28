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

import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>HwIdAlreadyBoundException</code> to <code>Response</code>.
 */
@Provider
public class HwIdAlreadyBoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<HwIdAlreadyBoundException>, javax.ws.rs.ext.ExceptionMapper<HwIdAlreadyBoundException> {

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String ERROR_MESSAGE_KEY = "The hardware serial number %s is already bound.";
    private static final String SOLUTION_MESSAGE_KEY = "hwid.already.used.solution";

    @Override
    public ErrorResponse toErrorResponse(final HwIdAlreadyBoundException exception, final String hardwareSerialNumber) {
        return ErrorResponse.builder()
            .withErrorTitle(String.format(ERROR_MESSAGE_KEY, hardwareSerialNumber))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withErrorDetails(exception.getBindFailures())
            .withHttpResponseStatus(Response.Status.CONFLICT.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final HwIdAlreadyBoundException ex) {
        return conflict(this.toErrorResponse(ex, null));
    }
}
