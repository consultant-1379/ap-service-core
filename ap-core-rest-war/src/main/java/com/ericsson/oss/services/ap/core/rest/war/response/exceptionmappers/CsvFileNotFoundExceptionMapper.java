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
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import com.ericsson.oss.services.ap.api.exception.CsvFileNotFoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>CsvFileNotFoundException</code> to appropriate http response.
 */
@Provider
public class CsvFileNotFoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<CsvFileNotFoundException>, javax.ws.rs.ext.ExceptionMapper<CsvFileNotFoundException> {

    private static final String CSV_ERROR = "validation.batch.csv.missing.project";
    private static final String CSV_SUGGESTED_SOLUTION = "validation.batch.csv.missing.project.solution";
    private static final String SUGGESTED_SOLUTION = "suggested.solution";

    @Override
    public ErrorResponse toErrorResponse(final CsvFileNotFoundException exception, final String usecase) {

        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.get(CSV_ERROR))
            .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(CSV_SUGGESTED_SOLUTION)))
            .withHttpResponseStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final CsvFileNotFoundException ex) {
        return internalServerError(this.toErrorResponse(ex, null));
    }
}
