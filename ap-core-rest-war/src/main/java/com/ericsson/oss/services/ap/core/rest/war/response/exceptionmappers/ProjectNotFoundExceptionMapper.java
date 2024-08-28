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

import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>ProjectNotFoundException</code> to <code>Response</code>.
 */
@Provider
public class ProjectNotFoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ProjectNotFoundException>, javax.ws.rs.ext.ExceptionMapper<ProjectNotFoundException> {

    @Override
    public ErrorResponse toErrorResponse(final ProjectNotFoundException exception, final String additionalInformation) {
        return this.buildErrorResponse(
            Response.Status.NOT_FOUND,
            "project.not.found",
            "suggested.solution",
            "project.not.found.solution"
        );
    }

    @Override
    public Response toResponse(final ProjectNotFoundException exception) {
        return notFound(this.toErrorResponse(exception, null));
    }
}
