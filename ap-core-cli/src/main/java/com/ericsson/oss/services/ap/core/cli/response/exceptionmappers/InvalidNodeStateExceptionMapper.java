/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>InvalidNodeStateException</code> to <code>CommandResponseDto</code>.
 */
public class InvalidNodeStateExceptionMapper implements ExceptionMapper<InvalidNodeStateException> {

    private static final String ERROR_MESSAGE_KEY = "node.invalid.state";
    private static final String SOLUTION_MESSAGE_KEY = "node.invalid.state.solution";

    @Inject
    private ValidStatesForEventMapper validStatesForEventMapper;

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final InvalidNodeStateException e) {
        final String commandName = command.split("\\s")[1].toLowerCase();
        final State invalidState = State.getState(e.getInvalidNodeState());

        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.INVALID_STATE_ERROR_CODE)
                .statusMessage(apMessages.format(ERROR_MESSAGE_KEY, invalidState.getDisplayName()))
                .solution(apMessages.format(SOLUTION_MESSAGE_KEY, validStatesForEventMapper.getValidStates(commandName)))
                .build();
    }
}
