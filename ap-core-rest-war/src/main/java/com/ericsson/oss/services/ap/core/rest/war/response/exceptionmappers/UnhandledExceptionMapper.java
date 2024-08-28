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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>Throwable</code> to <code>Response</code>.
 */
@Provider
@DefaultExceptionMapper
public class UnhandledExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<Throwable>, javax.ws.rs.ext.ExceptionMapper<Throwable> {

    private static final String INTERNAL_SERVER_ERROR_KEY = "failure.general";
    private final Logger logger = LoggerFactory.getLogger(UnhandledExceptionMapper.class);

    @Override
    public ErrorResponse toErrorResponse(final Throwable exception, final String additionalInformation) {
        logger.error(apUiMessages.format(INTERNAL_SERVER_ERROR_KEY), exception); //NOSONAR
        return this.buildErrorResponse(
            Response.Status.INTERNAL_SERVER_ERROR,
            INTERNAL_SERVER_ERROR_KEY,
            "suggested.solution",
            "error.solution.log.viewer"
        );
    }

    @Override
    public Response toResponse(Throwable throwable) {
        return internalServerError(this.toErrorResponse(throwable, null));
    }
}
