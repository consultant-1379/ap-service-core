/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import com.ericsson.oss.services.ap.api.exception.IllegalUploadNodeStateException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps <code>IllegalUploadNodeStateException</code> to <code>CommandResponseDto</code>.
 */
public class IllegalUploadNodeStateExceptionMapper implements ExceptionMapper<IllegalUploadNodeStateException> {

    private static final String ERROR_MESSAGE_KEY = "node.invalid.state";
    private static final String SOLUTION_MESSAGE_KEY = "node.invalid.state.solution";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final IllegalUploadNodeStateException e) {
        final State invalidState = State.getState(e.getInvalidNodeState());
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.INVALID_STATE_ERROR_CODE)
                .statusMessage(apMessages.format(ERROR_MESSAGE_KEY, invalidState.getDisplayName()))
                .solution(apMessages.format(SOLUTION_MESSAGE_KEY, buildValidDisplayStates(e)))
                .build();
    }

    private String buildValidDisplayStates(final IllegalUploadNodeStateException e) {
        final List<String> validDisplayStates = new ArrayList<>();
        for (String state : e.getValidNodeStates()) {
            validDisplayStates.add(State.getState(state).getDisplayName());
        }
        return StringUtils.join(validDisplayStates,", ");
    }
}
