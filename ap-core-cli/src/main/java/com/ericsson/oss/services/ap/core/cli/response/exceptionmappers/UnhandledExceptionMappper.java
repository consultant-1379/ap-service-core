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

import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>Throwable</code> to <code>CommandResponseDto</code>.
 */
@DefaultExceptionMapper
public class UnhandledExceptionMappper implements ExceptionMapper<Throwable> {

    private static final String SOLUTION_MESSAGE_KEY = "error.solution.log.viewer";
    private static final String INTERNAL_SERVER_ERROR_KEY = "failure.general";

    private final Logger logger = LoggerFactory.getLogger(UnhandledExceptionMappper.class);
    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final Throwable e) {
        final String errorMessage = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
        logger.error(apMessages.format(INTERNAL_SERVER_ERROR_KEY), e);

        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                .statusMessage(errorMessage)
                .setLogViewerCompatible()
                .setLogReference(apMessages.format(INTERNAL_SERVER_ERROR_KEY))
                .build();
    }
}
