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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.PartialProjectDeletionException;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

public class PartialProjectDeletionExceptionMapper implements ExceptionMapper<PartialProjectDeletionException> {

    private static final String SOLUTION_MESSAGE_KEY = "error.solution.log.viewer";
    private static final String INTERNAL_SERVER_ERROR_KEY = "failure.general";

    private final Logger logger = LoggerFactory.getLogger(PartialProjectDeletionExceptionMapper.class);
    private final ApMessages apMessages = new ApMessages();

    @Inject
    private ResponseDtoBuilder responseDtoBuilder;

    @Override
    public CommandResponseDto toCommandResponse(final String command, final PartialProjectDeletionException e) {
        final ResponseDto validationErrorResponse = generateValidationErrorResponse(e);
        logger.error(apMessages.get(INTERNAL_SERVER_ERROR_KEY), e);
        return new CommandResponseDtoBuilder()
                .fullCommand(command)
                .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
                .errorCode(CliErrorCodes.SERVICE_ERROR_CODE)
                .statusMessage(apMessages.get(INTERNAL_SERVER_ERROR_KEY))
                .solution(apMessages.get(SOLUTION_MESSAGE_KEY))
                .responseDto(validationErrorResponse)
                .setLogViewerCompatible()
                .setLogReference(getProjectNameFromCommand(command))
                .build();
    }

    private ResponseDto generateValidationErrorResponse(final PartialProjectDeletionException exception) {
        Collections.sort(exception.getRemainingNodes());
        final List<AbstractDto> resultDto = new ArrayList<>(responseDtoBuilder.buildLineDtosIndexed(
                String.format("Delete failed for %s node(s):", exception.getRemainingNodes().size()), exception.getRemainingNodes()));
        return new ResponseDto(resultDto);
    }

    private String getProjectNameFromCommand(final String command) {
        return command.substring(command.lastIndexOf(' ') + 1);
    }
}
