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

import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;

/**
 * Maps exception to <code>Response</code>
 *
 * @param <E> Exception
 */
public interface ExceptionMapper<E extends Throwable> {

    /**
     *
     * Converts exception to container for http response
     *
     * @param exception
     *            The exception thrown
     * @param additionalInformation
     *            Additional Information for Exception Handling
     * @return {@code Response}
     */
    ErrorResponse toErrorResponse(final E exception, final String additionalInformation);
}
