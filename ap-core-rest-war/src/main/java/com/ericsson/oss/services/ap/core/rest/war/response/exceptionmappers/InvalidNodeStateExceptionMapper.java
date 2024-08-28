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

import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>InvalidNodeStateException</code> to <code>Response</code>.
 */
@Provider
public class InvalidNodeStateExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<InvalidNodeStateException>, javax.ws.rs.ext.ExceptionMapper<InvalidNodeStateException> {

    private static final String SUGGESTED_SOLUTION = "suggested.solution";
    private static final String ERROR_MESSAGE_KEY = "node.invalid.state";
    private static final String SOLUTION_MESSAGE_KEY = "node.invalid.state.solution";

    @Inject
    private ValidStatesForEventMapper validStatesForEventMapper;

    @Override
    public ErrorResponse toErrorResponse(final InvalidNodeStateException exception, final String usecase) {
        final State invalidState = State.getState(exception.getInvalidNodeState());
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(ERROR_MESSAGE_KEY, invalidState.getDisplayName()))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.format(SOLUTION_MESSAGE_KEY, validStatesForEventMapper.getValidStates(usecase))))
            .withHttpResponseStatus(Response.Status.FORBIDDEN.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final InvalidNodeStateException ex) {
        return forbidden(this.toErrorResponse(ex, null));
    }
}
