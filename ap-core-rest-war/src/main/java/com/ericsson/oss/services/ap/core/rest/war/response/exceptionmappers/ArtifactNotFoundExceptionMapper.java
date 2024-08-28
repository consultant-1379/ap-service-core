/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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

import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>ArtifactNotFoundException</code> to Response.
 */
@Provider
public class ArtifactNotFoundExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<ArtifactNotFoundException>, javax.ws.rs.ext.ExceptionMapper<ArtifactNotFoundException> {

    private static final String DOWNLOAD_ERROR_MESSAGE_KEY = "download.artifact.not.permitted";
    private static final String UPLOAD_ERROR_MESSAGE_KEY = "configuration.file.not.found";
    private static final String UPLOAD_SOLUTION_MESSAGE_KEY = "configuration.file.not.found.solution";
    private static final String SUGGESTED_SOLUTION = "suggested.solution";

    @Override
    public ErrorResponse toErrorResponse(final ArtifactNotFoundException exception, final String additionalInformation) {

        if (additionalInformation.equalsIgnoreCase("UPLOAD_ARTIFACT")) {
            return ErrorResponse.builder()
                .withErrorTitle(apUiMessages.format(UPLOAD_ERROR_MESSAGE_KEY, parseFileName(exception.getMessage())))
                .withErrorBody(String.format("%s %s", apUiMessages.get(SUGGESTED_SOLUTION), apUiMessages.get(UPLOAD_SOLUTION_MESSAGE_KEY)))
                .withHttpResponseStatus(Response.Status.FORBIDDEN.getStatusCode())
                .build();
        } else {
            return ErrorResponse.builder()
                .withErrorTitle(apUiMessages.get(DOWNLOAD_ERROR_MESSAGE_KEY))
                .withHttpResponseStatus(Response.Status.FORBIDDEN.getStatusCode())
                .build();
        }
    }

    @Override
    public Response toResponse(final ArtifactNotFoundException ex) {
        return forbidden(this.toErrorResponse(ex, null));
    }

    private String parseFileName(final String message) {
        for (final String splitMessage : message.split(" ")) {
            if (splitMessage.contains("fileName:")) {
                return splitMessage.split(":", 2)[1];
            }
        }
        return "";
    }
}
