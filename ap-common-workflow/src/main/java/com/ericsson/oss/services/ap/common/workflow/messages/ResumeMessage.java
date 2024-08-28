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
 * Correlation message to resume an integration.
 */
public final class ResumeMessage {

    private static final String RESUME_MESSAGE_KEY = "RESUME";

    private ResumeMessage() {

    }

    /**
     * Correlation message key to resume an integration.
     *
     * @return the resume message key
     */
    public static String getMessageKey() {
        return RESUME_MESSAGE_KEY;
    }
}
