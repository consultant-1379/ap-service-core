/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.response.BatchBindResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Handles requests from the CLI for batch bind. This is called from BindCommandHandler and therefore is not annotated with {@link Handler}.
 */
public class BatchBindCommandHandler extends AbstractCommandHandler {

    @Inject
    private ArgumentResolver argumentResolver;

    @Inject
    private BatchBindResponseDtoBuilder responseDtoBuilder;

    @Override
    protected CommandLine validateCommand(final CliCommand command) throws ParseException {
        final CommandLine commandOptions = super.validateCommand(command);

        if (!isFileInArgs(commandOptions.getArgs())) {
            throw new ParseException("No file argument supplied");
        }
        return commandOptions;
    }

    private static boolean isFileInArgs(final String[] args) {
        return args.length == 1 && args[0].startsWith("file:");
    }

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandLine) {
        final String fileName = argumentResolver.getFileName(commandProperties);
        final byte[] bindCsvFile = argumentResolver.getFileContent(commandProperties);
        final BatchBindResult batchResult = getAutoProvisioningService().batchBind(fileName, bindCsvFile);
        return responseDtoBuilder.toCommandResponseDto(fullCommand, batchResult);
    }

    @Override
    protected CommandOptions getCommandOptions() {
        return new CommandOptions(new Options(), false);
    }
}
