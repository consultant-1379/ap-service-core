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

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Upload artifact handler to upload a single artifact request from CLI.
 */
@Handler(name = UseCaseName.UPLOAD_ARTIFACT)
public class UploadArtifactCommandHandler extends AbstractCommandHandler {

    private static final String UPLOAD_SUCCESS_MESSAGE = "command.success";
    private static final String FILE_COMMAND_PATTERN = "^file:.+";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Override
    protected CommandLine validateCommand(final CliCommand command) throws ParseException {
        final CommandLine commandOptions = super.validateCommand(command);
        final String fileName = getFileArg(commandOptions);

        if (!fileName.matches(FILE_COMMAND_PATTERN)) {
            throw new ParseException("Invalid file argument format");
        }

        return commandOptions;
    }

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties,
            final CommandLine commandOptions) {
        final String fileName = argumentResolver.getFileName(commandProperties);
        final byte[] fileContent = argumentResolver.getFileContent(commandProperties);
        final String nodeFdn = argumentResolver.resolveFdn(commandOptions, CommandLogName.UPLOAD_ARTIFACT);
        getAutoProvisioningService().uploadArtifact(nodeFdn, fileName, fileContent);

        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.format(UPLOAD_SUCCESS_MESSAGE, fileName))
                .build();
    }

    private static String getFileArg(final CommandLine command) throws ParseException {
        final String[] options = command.getOptionValues(CliCommandOption.NODE.getShortForm());
        if (options.length == 2) {
            return options[1];
        }
        throw new ParseException("Invalid file argument format");
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options uploadArtifactOptions = new Options();

        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setRequired(true);
        nodeOption.setArgs(2);

        uploadArtifactOptions.addOption(nodeOption);
        return new CommandOptions(uploadArtifactOptions, true);
    }
}
