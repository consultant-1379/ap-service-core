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

import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>ProfileNotFoundException</code> to <code>Response</code>.
 */
@Provider
public class ProfileNotFoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ProfileNotFoundException>, javax.ws.rs.ext.ExceptionMapper<ProfileNotFoundException> {

    @Override
    public ErrorResponse toErrorResponse(final ProfileNotFoundException exception, final String additionalInformation) {
        return this.buildErrorResponse(
            Response.Status.NOT_FOUND,
            "profile.not.found",
            "suggested.solution",
            "profile.not.found.solution"
        );
    }

    @Override
    public Response toResponse(final ProfileNotFoundException exception) {
        return notFound(this.toErrorResponse(exception, null));
    }
}
