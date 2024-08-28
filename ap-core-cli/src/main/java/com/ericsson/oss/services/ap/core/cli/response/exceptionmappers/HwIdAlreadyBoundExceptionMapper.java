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

import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps <code>HwIdAlreadyBoundException</code> to <code>CommandResponseDto</code>.
 */
public class HwIdAlreadyBoundExceptionMapper implements ExceptionMapper<HwIdAlreadyBoundException> {

    private static final String ERROR_MESSAGE_KEY = "The hardware serial number %s is already bound";
    private static final String SOLUTION_MESSAGE_KEY = "hwid.already.used.solution";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ResponseDtoBuilder responseDtoBuilder;

    @Override
    public CommandResponseDto toCommandResponse(final String command, final HwIdAlreadyBoundException e) {
        return new CommandResponseDtoBuilder()
            .fullCommand(command)
            .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
            .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
            .statusMessage(String.format(ERROR_MESSAGE_KEY, getHarwareSerialNumber(command)))
            .responseDto(generateBindErrorResponse(e))
            .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
            .build();
    }

    private ResponseDto generateBindErrorResponse(final HwIdAlreadyBoundException exception) {
        final List<AbstractDto> resultDto = new ArrayList<>(responseDtoBuilder.buildLineDtosIndexed(exception.getBindFailures()));
        return new ResponseDto(resultDto);
    }
}
