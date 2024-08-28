/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import com.ericsson.oss.services.ap.api.exception.IllegalDownloadArtifactException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>IllegalDownloadArtifactException</code> to <code>CommandResponseDto</code>.
 */
public class IllegalDownloadArtifactExceptionMapper implements ExceptionMapper<IllegalDownloadArtifactException> {

    private static final String ILLEGAL_DOWNLOAD_ARTIFACT_ERROR_MESSAGE_KEY = "download.artifact.not.permitted";
    private static final String ILLEGAL_DOWNLOAD_ARTIFACT_SOLUTION_MESSAGE_KEY = "download.artifact.not.permitted.solution";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final IllegalDownloadArtifactException e) {
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                .statusMessage(apMessages.get(ILLEGAL_DOWNLOAD_ARTIFACT_ERROR_MESSAGE_KEY))
                .solution(apMessages.get(ILLEGAL_DOWNLOAD_ARTIFACT_SOLUTION_MESSAGE_KEY))
                .build();
    }
}
