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

import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Maps exception to <code>CommandResponseDto</code>
 *
 * @param <E>
 */
public interface ExceptionMapper<E extends Throwable> {

    /**
     * Maps the exception to a command response.
     *
     * @param command
     *            the full command
     * @param e
     *            the exception
     * @return {@code CommandResponseDto}
     */
    CommandResponseDto toCommandResponse(final String command, final E e);
}
