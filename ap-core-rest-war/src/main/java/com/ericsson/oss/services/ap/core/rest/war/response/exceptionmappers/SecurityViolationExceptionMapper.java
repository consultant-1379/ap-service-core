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

import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link SecurityViolationException} to {@link Response}.
 */
@Provider
public class SecurityViolationExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<SecurityViolationException>, javax.ws.rs.ext.ExceptionMapper<SecurityViolationException> {

    @Override
    public ErrorResponse toErrorResponse(final SecurityViolationException exception, final String additionalInformation) {
        return this.buildErrorResponse(
            Response.Status.UNAUTHORIZED,
            "access.control.not.authorized",
            "suggested.solution",
            "access.control.not.authorized.solution"
        );
    }

    @Override
    public Response toResponse(SecurityViolationException e) {
        return unauthorized(toErrorResponse(e, null));
    }
}
