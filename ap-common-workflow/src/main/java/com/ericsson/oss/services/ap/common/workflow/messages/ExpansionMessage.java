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

public class ExpansionMessage {

    private static final String EXPANSION_MESSAGE_KEY = "EXPANSION";

    private ExpansionMessage() {

    }

    /**
     * Correlation message key to start an expansion.
     *
     * @return the expansion message key
     */
    public static String getMessageKey() {
        return EXPANSION_MESSAGE_KEY;
    }
}
