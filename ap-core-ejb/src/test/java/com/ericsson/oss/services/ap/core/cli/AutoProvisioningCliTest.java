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
package com.ericsson.oss.services.ap.core.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.scriptengine.spi.dtos.Command;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Unit tests for {@link AutoProvisioningCli}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoProvisioningCliTest {

    @Mock
    private CliCommandHandler cliHandler;

    @Mock
    private CliCommandHandlerFactory cliFactory;

    @InjectMocks
    private final AutoProvisioningCli apCli = new AutoProvisioningCli();

    @Test
    public void testCommandExecution() {
        final Command cmd = new Command("ap", "view");
        final CommandResponseDto respDto = new CommandResponseDto();
        respDto.setStatusCode(0);
        respDto.setStatusMessage("successful");

        when(cliFactory.getCliCommandHandler(any(CliCommand.class))).thenReturn(cliHandler);
        when(cliHandler.processCommand(any(CliCommand.class))).thenReturn(respDto);

        final CommandResponseDto actualCommandResponse = apCli.execute(cmd);
        assertEquals(ResponseStatus.SUCCESS, actualCommandResponse.getStatusCode());
        assertEquals("successful", actualCommandResponse.getStatusMessage());
    }
}
