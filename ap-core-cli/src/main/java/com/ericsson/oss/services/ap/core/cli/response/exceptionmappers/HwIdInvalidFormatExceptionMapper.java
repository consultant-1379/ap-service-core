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

import static com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.CommandArgs.getHarwareSerialNumber;

import com.ericsson.oss.services.ap.api.exception.HwIdInvalidFormatException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>HwIdInvalidFormatException</code> to <code>CommandResponseDto</code>.
 */
public class HwIdInvalidFormatExceptionMapper implements ExceptionMapper<HwIdInvalidFormatException> {

    private static final String ERROR_MESSAGE_KEY = "hwid.invalid.format";
    private static final String SOLUTION_MESSAGE_KEY = "hwid.invalid.format.solution";

    private final ApMessages apMessages = new ApMessages();

    @Override
    public CommandResponseDto toCommandResponse(final String command, final HwIdInvalidFormatException e) {
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.VALIDATION_ERROR_CODE)
                .statusMessage(apMessages.format(ERROR_MESSAGE_KEY, getHarwareSerialNumber(command)))
                .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                .build();
    }
}
