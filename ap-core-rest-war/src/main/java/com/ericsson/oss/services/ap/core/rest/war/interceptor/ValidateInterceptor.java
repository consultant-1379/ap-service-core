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
package com.ericsson.oss.services.ap.core.rest.war.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.war.properties.ApUiMessages;

/**
 * Interceptor to validate annotated pojo fields using Bean validation
 */
@Validate
@Interceptor
public class ValidateInterceptor {

    @Inject
    private Validator validator;

    @Inject
    private Logger logger;

    private final ApUiMessages apUiMessages = new ApUiMessages();

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String SOLUTION_MESSAGE_KEY = "validation.project.error.solution";
    private static final String VALIDATION_ERROR_OCCURRED = "validation.error.occurred";

    /**
     * Uses bean validation to validate against constraints set on fields in request pojo
     *
     * @param invocationContext Contextual information about a method invocation
     * @return Object Either to proceed to method or {@link Response} if validation fails
     * @throws Exception Exception
     */
    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
        final Object[] parameters = invocationContext.getParameters();

        if (parameters.length == 0)
            return invocationContext.proceed();

        final Set<ConstraintViolation<Object>> constraintViolations = validator.validate(parameters[parameters.length - 1]);

        if (constraintViolations.isEmpty())
            return invocationContext.proceed();

        final List<String> validationFailures = new ArrayList<>(constraintViolations.size());
        for (ConstraintViolation<?> violation : constraintViolations) {
            final String validationFailure = String.format("field: %s, value: %s, message: %s", violation.getPropertyPath().toString(), violation.getInvalidValue(), violation.getMessage());
            validationFailures.add(validationFailure);
            logger.info(validationFailure);
        }
        final ErrorResponse errorResponse = ErrorResponse.builder()
            .withErrorTitle(apUiMessages.get(VALIDATION_ERROR_OCCURRED))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(SOLUTION_MESSAGE_KEY)))
            .withErrorDetails(validationFailures)
            .withHttpResponseStatus(Response.Status.BAD_REQUEST.getStatusCode())
            .build();
        return Response.status(errorResponse.getHttpResponseStatus())
            .entity(errorResponse).build();

    }
}
