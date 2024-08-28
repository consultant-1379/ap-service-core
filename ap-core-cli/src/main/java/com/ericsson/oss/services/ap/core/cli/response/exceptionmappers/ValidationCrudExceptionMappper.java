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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.common.util.exception.ApExceptionUtils;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>ValidationCrudException</code> to <code>CommandResponseDto</code>.
 */
public class ValidationCrudExceptionMappper implements ExceptionMapper<ValidationCrudException> {

    private static final String SOLUTION_MESSAGE_KEY = "error.solution.log.viewer";
    private static final String INTERNAL_SERVER_ERROR_KEY = "failure.general";

    private final Logger logger = LoggerFactory.getLogger(ValidationCrudExceptionMappper.class);
    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final ValidationCrudException e) {
        logger.error(apMessages.get(INTERNAL_SERVER_ERROR_KEY), e);
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.CRUD_ERROR_CODE)
                .statusMessage( (e.getCause() == null ? e.getMessage() : e.getCause().getMessage()))
                .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                .setLogViewerCompatible()
                .setLogReference(ApExceptionUtils.getRootCause(e))
                .build();
    }
}
