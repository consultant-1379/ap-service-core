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

import com.ericsson.oss.services.ap.api.exception.ApNodeExistsException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>ApNodeExistsException</code> to <code>CommandResponseDto</code>.
 */
public class ApNodeExistsExceptionMapper implements ExceptionMapper<ApNodeExistsException> {

    private static final String ERROR_MESSAGE_KEY = "ap.node.exists";
    private static final String SOLUTION_MESSAGE_KEY = "ap.node.exists.solution";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final ApNodeExistsException e) {
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                .statusMessage(apMessages.format(ERROR_MESSAGE_KEY, e.getNodename()))
                .solution(apMessages.format(SOLUTION_MESSAGE_KEY, e.getNodename()))
                .build();
    }
}
