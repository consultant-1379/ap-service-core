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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.common.util.exception.ApExceptionUtils;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>ApServiceException</code> to <code>CommandResponseDto</code>.
 */
public class ApServiceExceptionMapper implements ExceptionMapper<ApServiceException> {

    private static final String SOLUTION_MESSAGE_KEY = "error.solution.log.viewer";

    private final ApMessages apMessages = new ApMessages();
    private final Logger logger = LoggerFactory.getLogger(ApServiceExceptionMapper.class);

    @Override
    public CommandResponseDto toCommandResponse(final String command, final ApServiceException e) {
        final String errorMessage = getErrorMessage(e);
        logger.error(errorMessage, e);

        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                .statusMessage(errorMessage)
                .setLogViewerCompatible()
                .setLogReference(getLogReference(command, e))
                .build();
    }

    private String getErrorMessage(final ApServiceException e) {
        return ApExceptionUtils.getRootCause(e);
    }

    private String getLogReference(final String command, final ApServiceException e) {
        final String commandName = command.split("\\s")[1];

        if (UseCaseName.DELETE.cliCommand().equals(commandName)) {
            return command.substring(command.lastIndexOf(' ') + 1);
        }
        return ApExceptionUtils.getRootCause(e);
    }
}
