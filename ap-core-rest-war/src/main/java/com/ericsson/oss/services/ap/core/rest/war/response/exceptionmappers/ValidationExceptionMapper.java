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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link ValidationException} with validation errors for Project or Node to appropriate http response.
 */
@Provider
public class ValidationExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ValidationException>, javax.ws.rs.ext.ExceptionMapper<ValidationException> {

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String SOLUTION_MESSAGE_KEY = "validation.project.error.solution";
    private static final String VALIDATION_PROJECT_ERROR = "validation.project.error";

    @Override
    public ErrorResponse toErrorResponse(final ValidationException exception, final String additionalInformation) {
        final String errorTitle = additionalInformation != null && (additionalInformation.equalsIgnoreCase("ORDER_PROJECT")|| additionalInformation.equalsIgnoreCase("CREATE_PROJECT")) ?
            apUiMessages.get(VALIDATION_PROJECT_ERROR) :
            exception.getMessage();

        return ErrorResponse.builder()
            .withErrorTitle(errorTitle)
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withErrorDetails(exception.getValidationFailures())
            .withHttpResponseStatus(HttpServletResponse.SC_EXPECTATION_FAILED)
            .build();
    }

    @Override
    public Response toResponse(final ValidationException e) {
        return entityWithStatus(
            HttpServletResponse.SC_EXPECTATION_FAILED,
            this.toErrorResponse(e, null)
        );
    }
}
