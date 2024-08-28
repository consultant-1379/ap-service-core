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

import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.CliCommandHandler;
import com.ericsson.oss.services.ap.core.cli.Handler;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Used to redirect calls to download usecase sub-handlers {@link DownloadArtifactCommandHandler} and {@link DownloadSchemaCommandHandler}.
 * <p>
 * Caches details of the file to be downloaded and returns response to script-engine containing a generated ID indicating that a file download request
 * should be initiated.
 * <p>
 * The file download request is handled by <code>AutoProvisioningFileDownloadExecutor</code> which will retrieve the file details from the cache.
 */
@Handler(name = UseCaseName.DOWNLOAD)
public class DownloadCommandHandler implements CliCommandHandler {

    @Inject
    private DownloadArtifactCommandHandler downloadArtifactCommandHandler;

    @Inject
    private DownloadSchemaCommandHandler downloadSchemaCommandHandler;

    @Override
    public CommandResponseDto processCommand(final CliCommand cliCommand) {
        if (cliCommand.getFullCommand().contains(CliCommandOption.NODE.getShortFlag())) {
            return downloadArtifactCommandHandler.processCommand(cliCommand);
        }
        return downloadSchemaCommandHandler.processCommand(cliCommand);
    }
}
