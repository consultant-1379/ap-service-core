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

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link ApApplicationException} to {@link Response}.
 */
@Provider
public class ApApplicationExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ApApplicationException>, javax.ws.rs.ext.ExceptionMapper<ApApplicationException> {

    @Inject
    private ApServiceExceptionMapper apServiceExceptionMapper;

    @Override
    public ErrorResponse toErrorResponse(final ApApplicationException exception, final String additionalInformation) {
        return apServiceExceptionMapper.toErrorResponse(exception, additionalInformation);
    }

    @Override
    public Response toResponse(ApApplicationException exception) {
        return badRequest(this.toErrorResponse(exception, null));
    }
}
