/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;

/**
 * Defines the valid provisioning states for a node.
 * <p>
 * Provides a mapping between state and phase for a node.
 */
public enum State {

    BIND_COMPLETED("Bind Completed"),
    BIND_STARTED("Bind Started"),
    DELETE_FAILED("Delete Failed"),
    DELETE_STARTED("Delete Started"),
    EXPANSION_CANCELLED("Expansion Cancelled"),
    EXPANSION_COMPLETED("Expansion Completed"),
    EXPANSION_FAILED("Expansion Failed"),
    EXPANSION_STARTED("Expansion Started"),
    EXPANSION_SUSPENDED("Expansion Suspended"),
    HARDWARE_REPLACE_COMPLETED("Hardware Replace Completed"),
    HARDWARE_REPLACE_BIND_COMPLETED("Hardware Replace Bind Completed"),
    HARDWARE_REPLACE_FAILED("Hardware Replace Failed"),
    HARDWARE_REPLACE_ROLLBACK_FAILED("Hardware Replace Failed with Rollback Failed"),
    HARDWARE_REPLACE_STARTED("Hardware Replace Started"),
    HARDWARE_REPLACE_SUSPENDED("Hardware Replace Suspended"),
    INTEGRATION_CANCELLED("Integration Cancelled"),
    INTEGRATION_COMPLETED("Integration Completed"),
    INTEGRATION_COMPLETED_WITH_WARNING("Integration Completed with Warning"),
    INTEGRATION_FAILED("Integration Failed"),
    INTEGRATION_STARTED("Integration Started"),
    INTEGRATION_SUSPENDED("Integration Suspended"),
    INVALID_CONFIGURATION("Invalid Configuration"),
    ORDER_COMPLETED("Order Completed"),
    ORDER_FAILED("Order Failed"),
    ORDER_CANCELLED("Order Cancelled"),
    ORDER_ROLLBACK_FAILED("Rollback Failed after Order Failed"),
    ORDER_STARTED("Order Started"),
    ORDER_SUSPENDED("Order Suspended"),
    READY_FOR_EOI_INTEGRATION("Ready for Eoi Integration"),
    EOI_INTEGRATION_STARTED("Eoi Integration Started"),
    EOI_INTEGRATION_FAILED("Eoi Integration Failed"),
    EOI_INTEGRATION_COMPLETED ("Eoi Integration Completed"),
    EOI_INTEGRATION_ROLLBACK_FAILED("Eoi Integration Failed after Integration Failed"),
    PRE_MIGRATION_STARTED("Pre Migration Started"),
    PRE_MIGRATION_FAILED("Pre Migration Failed"),
    READY_FOR_EXPANSION("Ready for Expansion"),
    READY_FOR_HARDWARE_REPLACE("Ready for Hardware Replace"),
    READY_FOR_ORDER("Ready for Order"),
    READY_FOR_PRE_MIGRATION("Ready for Pre Migration"),
    PRE_MIGRATION_SUSPENDED("Pre Migration Suspended"),
    PRE_MIGRATION_CANCELLED("Pre Migration Cancelled"),
    PRE_MIGRATION_COMPLETED("Pre Migration Completed"),
    PRE_MIGRATION_BIND_STARTED("Pre Migration Bind Started"),
    PRE_MIGRATION_BIND_COMPLETED("Pre Migration Bind Completed"),
    MIGRATION_STARTED("Migration Started"),
    MIGRATION_SUSPENDED("Migration Suspended"),
    MIGRATION_CANCELLED("Migration Cancelled"),
    MIGRATION_COMPLETED("Migration Completed"),
    MIGRATION_COMPLETED_WITH_WARNING("Migration Completed with warning"),
    MIGRATION_FAILED("Migration Failed"),
    EXPANSION_IMPORT_CONFIGURATION_SUSPENDED("Expansion Import Configuration Suspended"),
    INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED("Integration Import Configuration Suspended"),
    PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED("Pre Migration Import Configuration Suspended"),
    MIGRATION_IMPORT_CONFIGURATION_SUSPENDED("Migration Import Configuration Suspended"),
    UNKNOWN("Unknown");

    private String displayName;

    State(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the {@link State} in a form to be shown to the operator.
     *
     * @return the display name of the {@link State}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the matching {@link State} for a {@link String} representing a {@link State}.
     *
     * @param nodeState
     *            the {@link String} representation of the {@link State}
     * @return the matching {@link State}
     */
    public static State getState(final String nodeState) {
        try {
            return State.valueOf(nodeState);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ApApplicationException(String.format("Invalid state %s specified", nodeState));
        }
    }
}
