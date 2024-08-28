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

/**
 * Defines the events which cause a transition from one state to another.
 */
public enum StateTransitionEvent {

    BIND_FAILED,
    BIND_STARTED,
    BIND_SUCCESSFUL,
    DELETE_FAILED,
    DELETE_STARTED,
    EXPANSION_CANCELLED,
    EXPANSION_FAILED,
    EXPANSION_STARTED,
    EXPANSION_SUCCESSFUL,
    EXPANSION_SUSPENDED,
    HARDWARE_REPLACE_FAILED,
    HARDWARE_REPLACE_ROLLBACK_FAILED,
    HARDWARE_REPLACE_STARTED,
    HARDWARE_REPLACE_BIND_SUCCESSFUL,
    HARDWARE_REPLACE_SUCCESSFUL,
    HARDWARE_REPLACE_SUSPENDED,
    IMPORT_STARTED,
    IMPORT_SUCCESSFUL,
    INTEGRATION_CANCELLED,
    INTEGRATION_FAILED,
    INTEGRATION_STARTED,
    INTEGRATION_SUCCESSFUL,
    INTEGRATION_SUCCESSFUL_WITH_WARNING,
    INTEGRATION_SUSPENDED,
    ORDER_CANCELLED,
    ORDER_FAILED,
    ORDER_ROLLBACK_FAILED,
    ORDER_STARTED,
    ORDER_SUCCESSFUL,
    ORDER_SUSPENDED,
    EOI_INTEGRATION_FAILED,
    EOI_INTEGRATION_ROLLBACK_FAILED,
    EOI_INTEGRATION_STARTED,
    EOI_INTEGRATION_SUCCESSFUL,
    PRE_MIGRATION_STARTED,
    PRE_MIGRATION_FAILED,
    PRE_MIGRATION_SUSPENDED,
    PRE_MIGRATION_CANCELLED,
    PRE_MIGRATION_SUCCESSFUL,
    MIGRATION_STARTED,
    MIGRATION_SUSPENDED,
    MIGRATION_CANCELLED,
    MIGRATION_SUCCESSFUL,
    MIGRATION_SUCCESSFUL_WITH_WARNING,
    MIGRATION_FAILED,
    PRE_MIGRATION_BIND_FAILED,
    PRE_MIGRATION_BIND_STARTED,
    PRE_MIGRATION_BIND_SUCCESSFUL,
    INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED,
    EXPANSION_IMPORT_CONFIGURATION_SUSPENDED,
    PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED,
    MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
}
