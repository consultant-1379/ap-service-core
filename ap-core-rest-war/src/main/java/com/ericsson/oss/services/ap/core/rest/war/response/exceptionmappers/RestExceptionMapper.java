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

import java.util.List;

import javax.ws.rs.core.Response;

import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.war.properties.ApUiMessages;

/**
 * A base rest exception mapper with helper methods.
 */
public abstract class RestExceptionMapper {

    protected final ApUiMessages apUiMessages = new ApUiMessages();

    /**
     * Wrapper for building an {@link ErrorResponse} object.
     *
     * @param status            {@link javax.ws.rs.core.Response.Status} the Http status to be returned
     * @param title             Error Title, this maps the title to apUiMessages
     * @param suggestedSolution Error Body solution title
     * @param solutionMessage   Error Body solution message
     * @return ErrorResponse
     */
    public ErrorResponse buildErrorResponse(final Response.Status status,
                                            final String title,
                                            final String suggestedSolution,
                                            final String solutionMessage) {
        return buildErrorResponse(status.getStatusCode(), apUiMessages.get(title), suggestedSolution, solutionMessage, null);
    }

    /**
     * Wrapper for building an {@link ErrorResponse} object.
     *
     * @param statusCode        int the Http status code to be returned
     * @param title             Error Title, this can be any custom title
     * @param suggestedSolution Error Body solution title
     * @param solutionMessage   Error Body solution message
     * @param errorDetails      List of errors to be displayed
     * @return ErrorResponse
     */
    public ErrorResponse buildErrorResponse(final int statusCode,
                                            final String title,
                                            final String suggestedSolution,
                                            final String solutionMessage,
                                            final List<String> errorDetails) {
        return ErrorResponse.builder()
            .withErrorTitle(title)
            .withErrorBody(String.format("%s %s", apUiMessages.get(suggestedSolution), apUiMessages.get(solutionMessage)))
            .withHttpResponseStatus(statusCode)
            .withErrorDetails(errorDetails)
            .build();
    }

    /**
     * Creates a new Response with the given status and entity.
     *
     * @param status {@link javax.ws.rs.core.Response.Status}
     * @param entity Object request body entity
     * @return Response
     */
    protected Response entityWithStatus(final Response.Status status, final Object entity) {
        return entityWithStatus(status.getStatusCode(), entity);
    }

    /**
     * Creates a new Response with the given status and entity.
     *
     * @param statusCode int Http status code
     * @param entity     Object request body entity
     * @return Response
     */
    protected Response entityWithStatus(final int statusCode, final Object entity) {
        return Response.status(statusCode)
            .entity(entity)
            .build();
    }

    /**
     * Creates a new Http BAD_REQUEST response with the given entity.
     *
     * @param entity Object request body entity
     * @return Response
     */
    public Response badRequest(final Object entity) {
        return entityWithStatus(Response.Status.BAD_REQUEST, entity);
    }

    /**
     * Creates a new Http NOT_FOUND response with the given entity.
     *
     * @param entity Object request body entity
     * @return Response
     */
    public Response notFound(final Object entity) {
        return entityWithStatus(Response.Status.NOT_FOUND, entity);
    }

    /**
     * Creates a new Http CONFLICT response with the given entity.
     *
     * @param entity Object request body entity
     * @return Response
     */
    public Response conflict(final Object entity) {
        return entityWithStatus(Response.Status.CONFLICT, entity);
    }

    /**
     * Creates a new Http UNAUTHORIZED response with the given entity.
     *
     * @param entity Object request body entity
     * @return Response
     */
    public Response unauthorized(final Object entity) {
        return entityWithStatus(Response.Status.UNAUTHORIZED, entity);
    }

    /**
     * Creates a new Http FORBIDDEN response with the given entity.
     *
     * @param entity Object request body entity
     * @return Response
     */
    public Response forbidden(final Object entity) {
        return entityWithStatus(Response.Status.FORBIDDEN, entity);
    }

    /**
     * Creates a new Http INTERNAL_SERVER_ERROR response with the given entity.
     *
     * @param entity Object request body entity
     * @return Response
     */
    public Response internalServerError(final Object entity) {
        return entityWithStatus(Response.Status.INTERNAL_SERVER_ERROR, entity);
    }

}
