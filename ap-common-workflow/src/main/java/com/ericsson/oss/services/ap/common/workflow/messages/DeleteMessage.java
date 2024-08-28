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
 * Correlation message to delete an integration.
 */
public final class DeleteMessage {

    private static final String DELETE_MESSAGE_KEY = "DELETE";

    private DeleteMessage() {

    }

    /**
     * Correlation message key to delete an integration.
     *
     * @return the delete message key
     */
    public static String getMessageKey() {
        return DELETE_MESSAGE_KEY;
    }
}
