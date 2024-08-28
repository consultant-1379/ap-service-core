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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Unit tests for {@link DownloadCommandHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DownloadCommandHandlerTest {

    @Mock
    private DownloadArtifactCommandHandler downloadArtifactCommandHandler;

    @Mock
    private DownloadSchemaCommandHandler downloadSchemaCommandHandler;

    @InjectMocks
    private DownloadCommandHandler downloadCommandHandler;

    @Test
    public void testProcessCommandForDownloadArtifact() {
        final CliCommand cliCommand = new CliCommand("download -i -n " + NODE_NAME, null);
        final CommandResponseDto commandResponse = new CommandResponseDto();
        when(downloadArtifactCommandHandler.processCommand(cliCommand)).thenReturn(commandResponse);
        final CommandResponseDto actualCommandResponse = downloadCommandHandler.processCommand(cliCommand);
        assertEquals(actualCommandResponse, commandResponse);
    }

    @Test
    public void testProcessCommandForDownloadSchema() {
        final CliCommand cliCommand = new CliCommand("download -x erbs", null);
        final CommandResponseDto commandResponse = new CommandResponseDto();
        when(downloadSchemaCommandHandler.processCommand(cliCommand)).thenReturn(commandResponse);
        final CommandResponseDto actualCommandResponse = downloadCommandHandler.processCommand(cliCommand);
        assertEquals(actualCommandResponse, commandResponse);
    }
}
