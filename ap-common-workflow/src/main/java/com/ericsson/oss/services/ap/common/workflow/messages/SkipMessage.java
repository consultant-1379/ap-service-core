/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.messages;

/**
 * Correlation message to skip importing the failed artifact.
 */
public final class SkipMessage {

    private static final String SKIP_MESSAGE_KEY = "SKIP";

    private SkipMessage() {

    }

    /**
     * Correlation message key to skip importing the failed artifact.
     *
     * @return the skip message key
     */
    public static String getMessageKey() {
        return SKIP_MESSAGE_KEY;
    }
}
