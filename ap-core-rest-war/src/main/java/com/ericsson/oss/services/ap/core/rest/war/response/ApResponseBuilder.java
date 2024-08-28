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
package com.ericsson.oss.services.ap.core.rest.war.response;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ExceptionMapperFactory;

/**
 * Class to build response scenarios.
 */
public class ApResponseBuilder {

    @Inject
    private ExceptionMapperFactory exceptionMapperFactory;

    @Inject
    private Logger logger;

    /**
     * Build OK response.
     *
     * @param entity
     *            the entity object to build
     * @param <T>
     *            the entity that was built
     * @return successful response build OK 200
     */
    public <T> Response buildOk(final T entity) {
        final Response response = Response.status(Response.Status.OK).entity(entity).build();
        logResponse(response, entity);
        return response;
    }

    /**
     * Build Created response.
     *
     * @param entity
     *            the entity object to build
     * @param <T>
     *            the entity that was built
     * @return successful response build CREATED 201
     */
    public <T> Response buildCreated(final T entity) {
        final Response response = Response.status(Response.Status.CREATED).entity(entity).build();
        logResponse(response, entity);
        return response;
    }

    /**
     * Build the service error.
     *
     * @param usecase
     *            the usecase being executed
     * @param exception
     *            the Exception thrown
     * @return error response build
     */
    public ErrorResponse buildServiceError(final String usecase, final Exception exception) {
        logger.error("Request Failed, buildServiceError - exception: {}", exception.getMessage(), exception);
        return exceptionMapperFactory.find(exception).toErrorResponse(exception, usecase);
    }

    /**
     * Build the service error for bind failure
     *
     * @param exception
     *            the Exception thrown
     * @param serialNumber
     *            the hardware serial number
     * @return error response build
     */
    public ErrorResponse buildBindServiceError(final Exception exception, final String serialNumber) {
        logger.error("Request Failed, buildBindServiceError - exception: {0}", exception);
        return exceptionMapperFactory.find(exception).toErrorResponse(exception, serialNumber);
    }

    private <T> void logResponse(final Response response, final T entity) {
        logger.info("Response Builder Status:{}, Reason Phrase Catalog:({}), Entity:{}",
            response.getStatus(),
            Response.Status.fromStatusCode(response.getStatus()).getReasonPhrase(),
            entity != null ? entity : "<empty-body>");
    }
}
