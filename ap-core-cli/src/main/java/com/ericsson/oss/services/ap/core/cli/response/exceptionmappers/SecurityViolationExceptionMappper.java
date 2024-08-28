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

import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps {@link SecurityViolationException} to {@link CommandResponseDto}.
 */
public class SecurityViolationExceptionMappper implements ExceptionMapper<SecurityViolationException> {

    private static final String AUTHORIZATION_ERROR_MESSAGE_KEY = "access.control.not.authorized";
    private static final String AUTHORIZATION_SOLUTION_MESSAGE_KEY = "access.control.not.authorized.solution";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final SecurityViolationException e) {
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.NO_AUTHORIZATION_ERROR_CODE)
                .statusMessage(apMessages.get(AUTHORIZATION_ERROR_MESSAGE_KEY))
                .solution(apMessages.get(AUTHORIZATION_SOLUTION_MESSAGE_KEY))
                .build();
    }
}
