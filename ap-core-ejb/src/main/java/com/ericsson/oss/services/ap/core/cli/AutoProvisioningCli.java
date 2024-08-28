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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceQualifier;
import com.ericsson.oss.services.scriptengine.spi.CommandHandler;
import com.ericsson.oss.services.scriptengine.spi.dtos.Command;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Remote EJB to receive CLI client requests. Acts as intermediary for all communication between the Script Engine and Auto Provisioning.
 * <p>
 * <b>IMPORTANT</b>: Do not change qualifier from "ap". It must match CLI command name.
 */
@Stateless
@EServiceQualifier("ap")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AutoProvisioningCli implements CommandHandler {

    @Inject
    private CliCommandHandlerFactory cliCommandHandlerFactory;

    @Override
    public CommandResponseDto execute(final Command spiCommand) {
        final String fullCommand = spiCommand.getCommandContext().trim() + " " + spiCommand.getCommand().trim();
        final CliCommand cliCommand = new CliCommand(fullCommand, spiCommand.getProperties());
        final CliCommandHandler cliCommandHandler = cliCommandHandlerFactory.getCliCommandHandler(cliCommand);

        return cliCommandHandler.processCommand(cliCommand);
    }
}