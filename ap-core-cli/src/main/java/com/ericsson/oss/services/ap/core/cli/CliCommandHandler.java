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

import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * This interface must be implemented by all the command handlers available for Auto Provisioning.
 */
public interface CliCommandHandler {

    /**
     * Processes the input {@link CliCommand}, and returns a {@link CommandResponseDto} to the CLI.
     *
     * @param cliCommand
     *            the input command
     * @return the CLI response
     */
    CommandResponseDto processCommand(final CliCommand cliCommand);
}
