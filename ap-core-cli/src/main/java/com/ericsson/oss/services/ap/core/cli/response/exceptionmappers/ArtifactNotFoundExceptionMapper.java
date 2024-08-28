/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import static com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.CommandArgs.getFilename;

import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>ArtifactNotFoundException</code> to <code>CommandResponseDto</code>.
 */
public class ArtifactNotFoundExceptionMapper implements ExceptionMapper<ArtifactNotFoundException> {

    private static final String DOWNLOAD_ERROR_MESSAGE_KEY = "download.artifact.not.permitted";
    private static final String DOWNLOAD_SOLUTION_MESSAGE_KEY = "download.artifact.not.permitted.solution";
    private static final String UPLOAD_ERROR_MESSAGE_KEY = "configuration.file.not.found";
    private static final String UPLOAD_SOLUTION_MESSAGE_KEY = "configuration.file.not.found.solution";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final ArtifactNotFoundException e) {
        final String commandName = command.split("\\s")[1];

        if (UseCaseName.UPLOAD_ARTIFACT.cliCommand().equals(commandName)) {
            return new CommandResponseDtoBuilder()
                    .fullCommand(command)
                    .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                    .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                    .statusMessage(apMessages.format(UPLOAD_ERROR_MESSAGE_KEY, getFilename(command)))
                    .solution(apMessages.get(UPLOAD_SOLUTION_MESSAGE_KEY))
                    .build();
        } else {
            return new CommandResponseDtoBuilder()
                    .fullCommand(command)
                    .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                    .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                    .statusMessage(apMessages.get(DOWNLOAD_ERROR_MESSAGE_KEY))
                    .solution(apMessages.get(DOWNLOAD_SOLUTION_MESSAGE_KEY))
                    .build();
        }
    }
}
