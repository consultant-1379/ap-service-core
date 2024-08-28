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

import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.common.util.exception.ApExceptionUtils;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link ApServiceException} to appropriate http response.
 */
@Provider
public class ApServiceExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ApServiceException>, javax.ws.rs.ext.ExceptionMapper<ApServiceException> {

    @Override
    public ErrorResponse toErrorResponse(final ApServiceException exception, final String additionalInformation) {
        final String error = getLogReference(exception);
        return buildErrorResponse(
            Response.Status.BAD_REQUEST.getStatusCode(),
            error,
            "suggested.solution",
            "error.solution.log.viewer",
            null
        );
    }

    private String getLogReference(final ApServiceException exception) {
        return ApExceptionUtils.getRootCause(exception);
    }

    @Override
    public Response toResponse(final ApServiceException exception) {
        return badRequest(toErrorResponse(exception, null));
    }
}
