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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Unit tests for {@link CliCommandHandlerFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CliCommandHandlerFactoryTest {

    @Mock
    private Instance<CliCommandHandler> cliCommandHandlersMock;

    @Mock
    private CliCommand cliCommand;

    @InjectMocks
    private final CliCommandHandlerFactory handlerFactory = new CliCommandHandlerFactory();

    private final Collection<CliCommandHandler> handlers = new ArrayList<>();

    @Before
    public void setUp() {
        handlers.add(new InvalidCommandHandlerImpl());
        handlers.add(new OrderCommandHandlerImpl());
        handlers.add(new StatusCommandHandlerImpl());
        when(cliCommandHandlersMock.iterator()).thenReturn(handlers.iterator()).thenReturn(handlers.iterator());
    }

    @Test
    public void testFoundCommandHandler() {
        when(cliCommand.getOperation()).thenReturn(UseCaseName.ORDER.cliCommand());

        final CliCommandHandler actualCliCommandHandler = handlerFactory.getCliCommandHandler(cliCommand);
        assertNotNull(actualCliCommandHandler);
        assertTrue(actualCliCommandHandler instanceof OrderCommandHandlerImpl);
    }

    @Test
    public void testFoundStatusCommandHandler() {
        when(cliCommand.getOperation()).thenReturn(UseCaseName.STATUS.cliCommand());

        final CliCommandHandler actualCliCommandHandler = handlerFactory.getCliCommandHandler(cliCommand);
        assertNotNull(actualCliCommandHandler);
        assertTrue(actualCliCommandHandler instanceof StatusCommandHandlerImpl);
    }

    @Test
    public void testNotFoundCommandHandler() {
        when(cliCommand.getOperation()).thenReturn(UseCaseName.VIEW.cliCommand());

        final CliCommandHandler actualCliCommandHandler = handlerFactory.getCliCommandHandler(cliCommand);
        assertNotNull(actualCliCommandHandler);
        assertTrue(actualCliCommandHandler instanceof InvalidCommandHandlerImpl);
    }

    @Handler(name = UseCaseName.ORDER)
    private static class OrderCommandHandlerImpl implements CliCommandHandler {

        @Override
        public CommandResponseDto processCommand(final CliCommand cliCommand) {
            return null;
        }

    }

    @Handler(name = UseCaseName.STATUS)
    private static class StatusCommandHandlerImpl implements CliCommandHandler {

        @Override
        public CommandResponseDto processCommand(final CliCommand cliCommand) {
            return null;
        }
    }

    @Handler(name = UseCaseName.UNSUPPORTED)
    private static class InvalidCommandHandlerImpl implements CliCommandHandler {

        @Override
        public CommandResponseDto processCommand(final CliCommand cliCommand) {
            return null;
        }
    }
}
