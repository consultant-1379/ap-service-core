/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Builds a {@link ResponseDto} for the <code>ap bind file:{@literal <}fileName{@literal >}</code> command.
 */
public class BatchBindResponseDtoBuilder {

    private static final String MESSAGE_BATCH_BIND_SUCCESS = "command.success";
    private static final String MESSAGE_BATCH_ALL_FAILED = "command.failure";
    private static final String MESSAGE_BATCH_PARTIAL_SUCCESS = "command.partial.success";
    private static final String BATCH_BIND_SOLUTION_MESSAGE = "failure.error.solution";
    private static final String GENERAL_BIND_ERROR = "failure.general";

    private final ResponseDtoBuilder responseDtoBuilder = new ResponseDtoBuilder();
    private final ApMessages apMessages = new ApMessages();

    public CommandResponseDto toCommandResponseDto(final String fullCommand, final BatchBindResult batchResult) {
        if (batchResult.isSuccessful()) {
            return buildBatchBindSuccessResponse(fullCommand);
        } else {
            return buildBatchBindFailedResponse(fullCommand, batchResult);
        }
    }

    private CommandResponseDto buildBatchBindSuccessResponse(final String fullCommand) {
        return new CommandResponseDtoBuilder()
                .fullCommand(fullCommand)
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(apMessages.get(MESSAGE_BATCH_BIND_SUCCESS))
                .build();
    }

    private CommandResponseDto buildBatchBindFailedResponse(final String fullCommand, final BatchBindResult batchResult) {
        final String errorStatusMessage = getErrorStatusmessage(batchResult);
        final ResponseDto responseDto = buildValidateCommandResponseDto(batchResult.getFailedBindMessages());

        final CommandResponseDtoBuilder responseBuilder = new CommandResponseDtoBuilder();
        responseBuilder.fullCommand(fullCommand)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                .statusMessage(errorStatusMessage)
                .solution(apMessages.get(BATCH_BIND_SOLUTION_MESSAGE))
                .responseDto(responseDto);

        addLogReferenceToCommandResponse(responseBuilder, batchResult.getFailedBindMessages());

        return responseBuilder.build();
    }

    private void addLogReferenceToCommandResponse(final CommandResponseDtoBuilder responseBuilder, final List<String> failureMessages) {
        for (final String message : failureMessages) {
            if (message.contains(apMessages.format(GENERAL_BIND_ERROR))) {
                responseBuilder.setLogViewerCompatible().setLogReference("Batch bind failed for node");
                return;
            }
        }
    }

    private ResponseDto buildValidateCommandResponseDto(final List<String> bindFailures) {
        final List<AbstractDto> resultDto = new ArrayList<>(responseDtoBuilder.buildLineDtosIndexed(bindFailures));
        return new ResponseDto(resultDto);
    }

    private String getErrorStatusmessage(final BatchBindResult batchResult) {
        if (batchResult.isFailed()) {
            return apMessages.get(MESSAGE_BATCH_ALL_FAILED);
        } else {
            return apMessages.format(MESSAGE_BATCH_PARTIAL_SUCCESS, batchResult.getSuccessfulBinds(), batchResult.getTotalBinds());
        }
    }
}
