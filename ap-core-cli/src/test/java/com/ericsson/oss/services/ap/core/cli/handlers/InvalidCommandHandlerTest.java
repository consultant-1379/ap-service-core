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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Unit tests for {@link InvalidCommandHandler}
 */
@RunWith(MockitoJUnitRunner.class)
public class InvalidCommandHandlerTest {

    @InjectMocks
    private InvalidCommandHandler invalidCommandHandler;

    @Test
    public void whenNoApOperationIsEntered_thenInvalidSyntaxResponseIsReturned() {
        final CliCommand cliCommand = new CliCommand("ap", null);
        final CommandResponseDto actualCommandResponse = invalidCommandHandler.processCommand(cliCommand);
        CommandResponseValidatorTest.verifyInvalidSyntaxErrorWithCustomSolution(actualCommandResponse,
                "For more information on available commands run 'help ap'");
    }

    @Test
    public void whenInvalidApOperationIsEntered_thenInvalidSyntaxResponseIsReturned() {
        final CliCommand cliCommand = new CliCommand("ap invalidOperation", null);
        final CommandResponseDto actualCommandResponse = invalidCommandHandler.processCommand(cliCommand);
        CommandResponseValidatorTest.verifyInvalidSyntaxErrorWithCustomSolution(actualCommandResponse,
                "For more information on available commands run 'help ap'");
    }
}
