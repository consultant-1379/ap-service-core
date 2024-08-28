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
package com.ericsson.oss.services.ap.common.workflow.messages;

/**
 * Correlation message to cancel an integration.
 */
public final class CancelMessage {

    private static final String CANCEL_MESSAGE_KEY = "CANCEL";

    private CancelMessage() {

    }

    /**
     * Correlation message key to cancel an integration.
     *
     * @return the cancel message key
     */
    public static String getMessageKey() {
        return CANCEL_MESSAGE_KEY;
    }
}
