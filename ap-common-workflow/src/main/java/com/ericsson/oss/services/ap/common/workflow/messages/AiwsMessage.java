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
package com.ericsson.oss.services.ap.common.workflow.messages;

/**
 * Correlation message to trigger integration on receiving AIWS Notification.
 */
public final class AiwsMessage {

    private static final String AIWS_MESSAGE_KEY = "AIWS_NOTIFICATION";

    private AiwsMessage() {

    }

    /**
     * Correlation message key indicating a AIWS notification has been received.
     *
     * @return the message key
     */
    public static String getMessageKey() {
        return AIWS_MESSAGE_KEY;
    }

}
