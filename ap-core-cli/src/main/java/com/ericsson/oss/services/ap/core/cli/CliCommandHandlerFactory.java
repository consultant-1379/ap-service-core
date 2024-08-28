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
package com.ericsson.oss.services.ap.core.cli;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;

/**
 * This factory is responsible to provide the {@link CliCommandHandler} for the specified operation.
 */
public class CliCommandHandlerFactory {

    @Inject
    @Any
    private Instance<CliCommandHandler> cliCommandHandlers;

    /**
     * This method takes a {@link CliCommand} as the input parameter and return the appropriate instance of {@link CliCommandHandler}.
     *
     * @param cliCommand
     *            object representing the command given by the user
     * @return {@link CliCommandHandler} object for the provided command, or the default handler for invalid commands
     */
    public CliCommandHandler getCliCommandHandler(final CliCommand cliCommand) {
        return getHandler(cliCommand.getOperation());
    }

    private CliCommandHandler getHandler(final String operation) {
        CliCommandHandler commandHandler = null;
        for (final CliCommandHandler cliCommandHandler : cliCommandHandlers) {
            if (cliCommandHandler.getClass().isAnnotationPresent(Handler.class)
                    && cliCommandHandler.getClass().getAnnotation(Handler.class).name().cliCommand().equalsIgnoreCase(operation)) {
                commandHandler = cliCommandHandler;
                continue;
            }
            cliCommandHandlers.destroy(cliCommandHandler);
        }
         return (commandHandler== null) ? getHandler(UseCaseName.UNSUPPORTED.cliCommand()) : commandHandler;
    }
}
