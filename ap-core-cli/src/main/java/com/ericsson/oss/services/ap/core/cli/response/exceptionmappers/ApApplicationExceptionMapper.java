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

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Maps {@link ApApplicationException} to {@link CommandResponseDto}.
 */
public class ApApplicationExceptionMapper implements ExceptionMapper<ApApplicationException> {

    @Inject
    private ApServiceExceptionMapper apServiceExceptionMapper;

    @Override
    public CommandResponseDto toCommandResponse(final String command, final ApApplicationException e) {
        return apServiceExceptionMapper.toCommandResponse(command, e);
    }
}
