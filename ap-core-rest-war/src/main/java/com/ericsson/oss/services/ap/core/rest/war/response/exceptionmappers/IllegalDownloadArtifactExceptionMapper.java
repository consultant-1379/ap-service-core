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

import com.ericsson.oss.services.ap.api.exception.IllegalDownloadArtifactException;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps <code>IllegalDownloadArtifactException</code> to appropriate http response.
 */
@Provider
public class IllegalDownloadArtifactExceptionMapper extends RestExceptionMapper
    implements ExceptionMapper<IllegalDownloadArtifactException>,
    javax.ws.rs.ext.ExceptionMapper<IllegalDownloadArtifactException> {

    private static final String ILLEGAL_DOWNLOAD_ARTIFACT_MESSAGE_KEY = "download.artifact.not.permitted";

    @Override
    public ErrorResponse toErrorResponse(final IllegalDownloadArtifactException exception, final String usecase) {
        return ErrorResponse.builder()
            .withErrorTitle(apUiMessages.get(ILLEGAL_DOWNLOAD_ARTIFACT_MESSAGE_KEY))
            .withHttpResponseStatus(Response.Status.FORBIDDEN.getStatusCode())
            .build();
    }

    @Override
    public Response toResponse(final IllegalDownloadArtifactException ex) {
        return forbidden(this.toErrorResponse(ex, null));
    }
}
