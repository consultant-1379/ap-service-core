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

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;

import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.core.cli.CommandOptions;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadRequestDto;

/**
 * Handle commands to download Auto Provisioning node artifacts.
 * <p>
 * File to be downloaded is stored in a staging area, and a response to script-engine containing a unique ID indicating the specific file download
 * request to be initiated.
 * <p>
 * The file download request is handled by <code>AutoProvisioningFileDownloadExecutor</code> which will retrieve the file based on a unique ID.
 */
public class DownloadArtifactCommandHandler extends AbstractCommandHandler {

    private static final String DOWNLOAD_SUCCESS_MESSAGE = "command.success";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ArgumentResolver argumentResolver;

    @Override
    protected CommandResponseDto executeCommand(final String fullCommand, final Map<String, Object> commandProperties, final CommandLine command) {
        return downloadArtifacts(fullCommand, command);
    }

    private CommandResponseDto downloadArtifacts(final String fullCommand, final CommandLine cmd) {
        final String nodeFdn = argumentResolver.resolveFdn(cmd, CommandLogName.DOWNLOAD_ARTIFACT);
        final ResponseDto responseDto = executeArtifactDownload(cmd, nodeFdn);

        return new CommandResponseDtoBuilder()
            .fullCommand(fullCommand)
            .statusCode(ResponseStatus.SUCCESS)
            .statusMessage(apMessages.get(DOWNLOAD_SUCCESS_MESSAGE))
            .responseDto(responseDto)
            .build();
    }

    private ResponseDto executeArtifactDownload(final CommandLine cmd, final String nodeFdn) {
        final String downloadableArtifactId = getAutoProvisioningService().downloadNodeArtifact(nodeFdn, parseArtifactState(cmd));
        final List<AbstractDto> fileDownloadRequests = new ArrayList<>(1);
        fileDownloadRequests.add(new FileDownloadRequestDto("ap", downloadableArtifactId));
        return new ResponseDto(fileDownloadRequests);
    }

    private static ArtifactBaseType parseArtifactState(final CommandLine cmd) {
        return cmd.hasOption(CliCommandOption.INITIAL_ARTIFACT.getShortForm()) ? ArtifactBaseType.RAW : ArtifactBaseType.GENERATED;
    }

    @Override
    protected CommandOptions getCommandOptions() {
        final Options downloadArtifactOptions = new Options();

        final OptionGroup downloadArtifactOptionGroup = new OptionGroup();
        final Option initialArtifactPhaseOption = createOption(CliCommandOption.INITIAL_ARTIFACT, false);
        downloadArtifactOptionGroup.addOption(initialArtifactPhaseOption);

        final Option orderedArtifactPhaseOption = createOption(CliCommandOption.ORDERED_ARTIFACT, false);
        downloadArtifactOptionGroup.addOption(orderedArtifactPhaseOption);
        downloadArtifactOptionGroup.setRequired(true);

        downloadArtifactOptions.addOptionGroup(downloadArtifactOptionGroup);
        final Option nodeOption = createOption(CliCommandOption.NODE, true);
        nodeOption.setRequired(true);
        downloadArtifactOptions.addOption(nodeOption);

        return new CommandOptions(downloadArtifactOptions, true);
    }
}
