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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Maps <code>ValidationException</code> to <code>CommandResponseDto</code>.
 */
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final String ERROR_MESSAGE_KEY = "validation.project.error";
    private static final String SOLUTION_MESSAGE_KEY = "failure.error.solution";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ResponseDtoBuilder responseDtoBuilder;

    @Override
    public CommandResponseDto toCommandResponse(final String command, final ValidationException e) {
        final String commandName = command.split("\\s")[1];

        if (UseCaseName.ORDER.cliCommand().equals(commandName)) {
            final ResponseDto validationErrorResponse = generateValidationErrorResponse(e);
            return new CommandResponseDtoBuilder()
                    .fullCommand(command)
                    .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                    .errorCode(CliErrorCodes.VALIDATION_ERROR_CODE)
                    .statusMessage(apMessages.get(ERROR_MESSAGE_KEY))
                    .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                    .responseDto(validationErrorResponse)
                    .build();
        } else {
            return new CommandResponseDtoBuilder()
                    .fullCommand(command)
                    .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                    .errorCode(CliErrorCodes.VALIDATION_ERROR_CODE)
                    .statusMessage(e.getMessage())
                    .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                    .build();
        }
    }

    private ResponseDto generateValidationErrorResponse(final ValidationException exception) {
        final List<AbstractDto> resultDto = new ArrayList<>(responseDtoBuilder.buildLineDtosIndexed(exception.getValidationFailures()));
        return new ResponseDto(resultDto);
    }
}
