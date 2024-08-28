/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CliCommandHandler;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Command Handler for commands with invalid AP operation from CLI.
 */
@Handler(name = UseCaseName.UNSUPPORTED)
public class InvalidCommandHandler implements CliCommandHandler {

    private static final String AP_HELP = "help.ap";
    private static final String ERROR_INVALID_SYNTAX = "invalid.command.syntax";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto processCommand(final CliCommand cliCommand) {
        return new CommandResponseDtoBuilder()
                .fullCommand(cliCommand.getFullCommand())
                .statusCode(ResponseStatus.COMMAND_SYNTAX_ERROR)
                .errorCode(CliErrorCodes.INVALID_COMMAND_SYNTAX_ERROR_CODE)
                .statusMessage(apMessages.get(ERROR_INVALID_SYNTAX))
                .solution(apMessages.get(AP_HELP))
                .build();
    }
}
