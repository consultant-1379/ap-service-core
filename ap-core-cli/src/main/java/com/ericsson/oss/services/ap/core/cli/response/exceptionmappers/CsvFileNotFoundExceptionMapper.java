/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
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

import com.ericsson.oss.services.ap.api.exception.CsvFileNotFoundException;
import com.ericsson.oss.services.ap.common.util.exception.ApExceptionUtils;
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.ap.core.cli.response.CommandResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadRequestDto;

/**
 * Maps <code>CsvFileNotFoundException</code> to <code>CommandResponseDto</code>.
 */
public class CsvFileNotFoundExceptionMapper implements ExceptionMapper<CsvFileNotFoundException> {

    private static final String SOLUTION_WITH_PROVIDING_CSV = "Please include the node specific values in the csv file returned from this command and reorder the project with the csv included.";

    private static final String SOLUTION_WITHOUT_PROVIDING_CSV = "Please include the csv file and reorder the project with the csv included.";

    @Override
    public CommandResponseDto toCommandResponse(final String command, final CsvFileNotFoundException e) {
        return new CommandResponseDtoBuilder().fullCommand(command)
            .statusCode(ResponseStatus.COMMAND_EXECUTION_ERROR)
            .errorCode(CliErrorCodes.VALIDATION_ERROR_CODE)
            .statusMessage(e.getMessage())
            .solution(buildSolution(e))
            .setLogReference(buildLogReference(e))
            .setLogViewerCompatible()
            .responseDto(buildResponseDto(e))
            .build();
    }

    private ResponseDto buildResponseDto(final CsvFileNotFoundException e) {
        final String generatedCsvName = e.getGeneratedCsvName();

        if (generatedCsvName != null) {
            final List<AbstractDto> fileDownloadRequests = new ArrayList<>(1);
            fileDownloadRequests.add(new FileDownloadRequestDto("ap", generatedCsvName));
            return new ResponseDto(fileDownloadRequests);
        }

        return new ResponseDto(Collections.<AbstractDto>emptyList());
    }

    private String buildLogReference(final CsvFileNotFoundException e) {
        if (e.getCause() == null) {
            return ApExceptionUtils.getRootCause(e);
        }

        return ApExceptionUtils.getRootCause(e.getCause());
    }

    private String buildSolution(final CsvFileNotFoundException e) {
        if (e.getGeneratedCsvName() != null) {
            return SOLUTION_WITH_PROVIDING_CSV;
        }

        return SOLUTION_WITHOUT_PROVIDING_CSV;
    }
}
