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
package com.ericsson.oss.services.ap.common.util.exception;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;

public final class ApExceptionUtils {

    private ApExceptionUtils() {

    }

    @SuppressWarnings("unchecked")
    public static String getRootCause(final Throwable e) {
        final List<Throwable> exceptions = ExceptionUtils.getThrowableList(e);

        for (int i = exceptions.size()-1; i >=0; i--) {
            final Throwable t = exceptions.get(i);
            if (t.getMessage() != null) {
                return t.getMessage();
            }
        }
        return e.getClass().getSimpleName();
    }

}
