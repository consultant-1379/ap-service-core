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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadRequestDto;

/**
 * Handle commands to download all Auto Provisioning artifact samples (XML) and artifact schemas (XSD) for all versions.
 * <p>
 * Saves the file to the download staging area and returns response to script-engine containing the unique file name indicating that a file download
 * request should be initiated.
 * <p>
 * The file download request is handled by <code>AutoProvisioningFileDownloadExecutor</code> which will retrieve the file using the unique file name
 * returned in the CommandResponse.
 */
public class DownloadSchemaCommandHandler extends AbstractCommandHandler {

    private static final String DOWNLOAD_SUCCESS_MESSAGE = "command.success";

    private final ApMessages apMessages = new ApMessages();

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties, final CommandLine command) {
        return downloadArtifacts(fullCommand, command);
    }

    private CommandResponseDto downloadArtifacts(final String fullCommand, final CommandLine cmd) {
        final ResponseDto responseDto = executeDownloadSchemaArtifacts(cmd);
        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.get(DOWNLOAD_SUCCESS_MESSAGE))
                .responseDto(responseDto)
                .build();
    }

    private ResponseDto executeDownloadSchemaArtifacts(final CommandLine cmd) {
        final String nodeType = cmd.getOptionValue(CliCommandOption.SAMPLES_AND_SCHEMAS.getShortForm(), ""); // x is mandatory
        final String downloadableFileId = getAutoProvisioningService().downloadSchemaAndSamples(nodeType);
        final List<AbstractDto> fileDownloadRequestDtos = new ArrayList<>(1);
        fileDownloadRequestDtos.add(new FileDownloadRequestDto("ap", downloadableFileId));

        return new ResponseDto(fileDownloadRequestDtos);
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Option schemaOption = createOption(CliCommandOption.SAMPLES_AND_SCHEMAS, true);
        schemaOption.setRequired(true);
        schemaOption.setArgs(1);
        schemaOption.setOptionalArg(true);

        final Options downloadSchemaOptions = new Options();
        downloadSchemaOptions.addOption(schemaOption);

        return new CommandOptions(downloadSchemaOptions, true);
    }
}
