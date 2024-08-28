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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.api.exception.IllegalUploadNodeStateException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps {@link IllegalUploadNodeStateException} to {@link Response}.
 */
@Provider
public class IllegalUploadNodeStateExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<IllegalUploadNodeStateException>,
    javax.ws.rs.ext.ExceptionMapper<IllegalUploadNodeStateException> {

    private static final String MESSAGE_KEY = "node.invalid.state";
    private static final String MESSAGE_KEY_SOLUTION = "node.invalid.state.solution";
    private static final String SUGGESTED_SOLUTION = "suggested.solution";

    @Override
    public ErrorResponse toErrorResponse(final IllegalUploadNodeStateException exception, final String additionalInformation) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.format(MESSAGE_KEY, "upload"))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.format(MESSAGE_KEY_SOLUTION, buildValidDisplayStates(exception))))
            .withHttpResponseStatus(Response.Status.NOT_ACCEPTABLE.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final IllegalUploadNodeStateException ex) {
        return entityWithStatus(Response.Status.NOT_ACCEPTABLE, this.toErrorResponse(ex, null));
    }

    private String buildValidDisplayStates(final IllegalUploadNodeStateException e) {
        final List<String> validDisplayStates = new ArrayList<>();
        for (String state : e.getValidNodeStates()) {
            validDisplayStates.add(State.getState(state).getDisplayName());
        }
        return StringUtils.join(validDisplayStates, ", ");
    }


}
