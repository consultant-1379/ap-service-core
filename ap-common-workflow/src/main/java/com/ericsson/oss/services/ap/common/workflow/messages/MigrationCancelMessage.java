/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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
 * Correlation message to cancel a migration.
 */
public final class MigrationCancelMessage {

    private static final String MIGRATION_CANCEL_MESSAGE_KEY = "MIGRATION_CANCEL";

    private MigrationCancelMessage() {

    }

    /**
     * Correlation message key to cancel a migration.
     *
     * @return the migration cancel message key
     */
    public static String getMessageKey() {
        return MIGRATION_CANCEL_MESSAGE_KEY;
    }
}