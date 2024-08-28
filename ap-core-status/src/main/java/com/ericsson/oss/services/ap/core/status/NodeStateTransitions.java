/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.status;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;

import static com.ericsson.oss.services.ap.api.status.StateTransitionEvent.*;


/**
 * Defines all valid state transitions for an Auto Provisioning node.
 */
public final class NodeStateTransitions {

    private static final List<StateTransition> VALID_NODE_STATE_TRANSITIONS = new ArrayList<>(75); // The number of #addTransition() calls in the static block below
    private static final List<StateTransition> INTERNAL_NODE_STATE_TRANSITIONS = new ArrayList<>(7);

    static {
        addTransition(DELETE_STARTED, "*", State.DELETE_STARTED.name()); // "*" means all states are allowed
        addTransition(DELETE_FAILED, State.DELETE_STARTED.name(), State.DELETE_FAILED.name());

        addTransition(ORDER_FAILED, State.ORDER_STARTED.name(), State.ORDER_FAILED.name());
        addTransition(ORDER_FAILED, State.ORDER_FAILED.name(), State.ORDER_FAILED.name());
        addTransition(ORDER_ROLLBACK_FAILED, State.ORDER_STARTED.name(), State.ORDER_ROLLBACK_FAILED.name());
        addTransition(ORDER_STARTED, State.READY_FOR_ORDER.name(), State.ORDER_STARTED.name());
        addTransition(ORDER_STARTED, State.ORDER_FAILED.name(), State.ORDER_STARTED.name());
        addTransition(ORDER_SUCCESSFUL, State.ORDER_STARTED.name(), State.ORDER_COMPLETED.name());

        addTransition(ORDER_CANCELLED, State.ORDER_STARTED.name(), State.ORDER_CANCELLED.name());
        addTransition(ORDER_CANCELLED, State.ORDER_SUSPENDED.name(), State.ORDER_CANCELLED.name());
        addTransition(ORDER_STARTED, State.ORDER_CANCELLED.name(), State.ORDER_STARTED.name());
        addTransition(ORDER_STARTED, State.ORDER_SUSPENDED.name(), State.ORDER_STARTED.name());
        addTransition(ORDER_SUSPENDED, State.ORDER_STARTED.name(), State.ORDER_SUSPENDED.name());

        addTransition(HARDWARE_REPLACE_FAILED, State.HARDWARE_REPLACE_FAILED.name(), State.HARDWARE_REPLACE_FAILED.name());
        addTransition(HARDWARE_REPLACE_FAILED, State.HARDWARE_REPLACE_STARTED.name(), State.HARDWARE_REPLACE_FAILED.name());
        addTransition(HARDWARE_REPLACE_ROLLBACK_FAILED, State.HARDWARE_REPLACE_STARTED.name(), State.HARDWARE_REPLACE_ROLLBACK_FAILED.name());
        addTransition(HARDWARE_REPLACE_STARTED, State.READY_FOR_ORDER.name(), State.HARDWARE_REPLACE_STARTED.name());
        addTransition(HARDWARE_REPLACE_STARTED, State.HARDWARE_REPLACE_SUSPENDED.name(), State.HARDWARE_REPLACE_STARTED.name());
        addTransition(HARDWARE_REPLACE_STARTED, State.READY_FOR_HARDWARE_REPLACE.name(), State.HARDWARE_REPLACE_STARTED.name());
        addTransition(HARDWARE_REPLACE_BIND_SUCCESSFUL, State.HARDWARE_REPLACE_STARTED.name(), State.HARDWARE_REPLACE_BIND_COMPLETED.name());
        addTransition(HARDWARE_REPLACE_SUCCESSFUL, State.HARDWARE_REPLACE_STARTED.name(), State.HARDWARE_REPLACE_COMPLETED.name());
        addTransition(HARDWARE_REPLACE_SUCCESSFUL, State.HARDWARE_REPLACE_BIND_COMPLETED.name(), State.HARDWARE_REPLACE_COMPLETED.name());
        addTransition(HARDWARE_REPLACE_SUCCESSFUL, State.BIND_COMPLETED.name(), State.HARDWARE_REPLACE_COMPLETED.name());
        addTransition(HARDWARE_REPLACE_SUSPENDED, State.HARDWARE_REPLACE_STARTED.name(), State.HARDWARE_REPLACE_SUSPENDED.name());

        addTransition(BIND_FAILED, State.BIND_STARTED.name(), State.ORDER_COMPLETED.name());
        addTransition(BIND_STARTED, State.BIND_COMPLETED.name(), State.BIND_STARTED.name());
        addTransition(BIND_STARTED, State.ORDER_COMPLETED.name(), State.BIND_STARTED.name());
        addTransition(BIND_STARTED, State.HARDWARE_REPLACE_BIND_COMPLETED.name(), State.BIND_STARTED.name());
        addTransition(BIND_SUCCESSFUL, State.BIND_STARTED.name(), State.BIND_COMPLETED.name());

        addTransition(PRE_MIGRATION_BIND_FAILED, State.PRE_MIGRATION_BIND_STARTED.name(), State.PRE_MIGRATION_COMPLETED.name());
        addTransition(PRE_MIGRATION_BIND_STARTED, State.PRE_MIGRATION_BIND_COMPLETED.name(), State.PRE_MIGRATION_BIND_STARTED.name());
        addTransition(PRE_MIGRATION_BIND_STARTED, State.PRE_MIGRATION_COMPLETED.name(), State.PRE_MIGRATION_BIND_STARTED.name());
        addTransition(PRE_MIGRATION_BIND_SUCCESSFUL, State.PRE_MIGRATION_BIND_STARTED.name(), State.PRE_MIGRATION_BIND_COMPLETED.name());
        addTransition(MIGRATION_STARTED, State.PRE_MIGRATION_BIND_COMPLETED.name(), State.MIGRATION_STARTED.name());


        addTransition(INTEGRATION_CANCELLED, State.INTEGRATION_STARTED.name(), State.INTEGRATION_CANCELLED.name());
        addTransition(INTEGRATION_CANCELLED, State.INTEGRATION_SUSPENDED.name(), State.INTEGRATION_CANCELLED.name());
        addTransition(INTEGRATION_FAILED, State.INTEGRATION_STARTED.name(), State.INTEGRATION_FAILED.name());
        addTransition(INTEGRATION_FAILED, State.ORDER_COMPLETED.name(), State.INTEGRATION_FAILED.name());
        addTransition(INTEGRATION_STARTED, State.INTEGRATION_SUSPENDED.name(), State.INTEGRATION_STARTED.name());
        addTransition(INTEGRATION_STARTED, State.BIND_COMPLETED.name(), State.INTEGRATION_STARTED.name());
        addTransition(INTEGRATION_STARTED, State.ORDER_COMPLETED.name(), State.INTEGRATION_STARTED.name());
        addTransition(INTEGRATION_SUCCESSFUL, State.INTEGRATION_STARTED.name(), State.INTEGRATION_COMPLETED.name());
        addTransition(INTEGRATION_SUCCESSFUL_WITH_WARNING, State.INTEGRATION_STARTED.name(), State.INTEGRATION_COMPLETED_WITH_WARNING.name());
        addTransition(INTEGRATION_SUSPENDED, State.INTEGRATION_STARTED.name(), State.INTEGRATION_SUSPENDED.name());
        addTransition(INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED, State.INTEGRATION_STARTED.name(), State.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED.name());
        addTransition(INTEGRATION_STARTED, State.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), State.INTEGRATION_STARTED.name());
        addTransition(INTEGRATION_CANCELLED, State.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), State.INTEGRATION_CANCELLED.name());

        addTransition(EXPANSION_STARTED, State.EXPANSION_CANCELLED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.EXPANSION_COMPLETED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.EXPANSION_FAILED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.EXPANSION_SUSPENDED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.INTEGRATION_COMPLETED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.INTEGRATION_COMPLETED_WITH_WARNING.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.INTEGRATION_FAILED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.INTEGRATION_CANCELLED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_STARTED, State.READY_FOR_EXPANSION.name(), State.EXPANSION_STARTED.name());

        addTransition(EXPANSION_CANCELLED, State.EXPANSION_STARTED.name(), State.EXPANSION_CANCELLED.name());
        addTransition(EXPANSION_CANCELLED, State.EXPANSION_SUSPENDED.name(), State.EXPANSION_CANCELLED.name());
        addTransition(EXPANSION_FAILED, State.EXPANSION_STARTED.name(), State.EXPANSION_FAILED.name());
        addTransition(EXPANSION_SUCCESSFUL, State.EXPANSION_STARTED.name(), State.EXPANSION_COMPLETED.name());
        addTransition(EXPANSION_SUSPENDED, State.EXPANSION_STARTED.name(), State.EXPANSION_SUSPENDED.name());
        addTransition(EXPANSION_IMPORT_CONFIGURATION_SUSPENDED, State.EXPANSION_STARTED.name(), State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.name());
        addTransition(EXPANSION_STARTED, State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.name(), State.EXPANSION_STARTED.name());
        addTransition(EXPANSION_CANCELLED, State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.name(), State.EXPANSION_CANCELLED.name());

        /**
         * Migration Node State Transitions
         */
        addTransition(PRE_MIGRATION_CANCELLED, "*", State.PRE_MIGRATION_CANCELLED.name());
        addTransition(PRE_MIGRATION_STARTED, State.READY_FOR_PRE_MIGRATION.name(), State.PRE_MIGRATION_STARTED.name());
        addTransition(PRE_MIGRATION_STARTED, State.PRE_MIGRATION_FAILED.name(), State.PRE_MIGRATION_STARTED.name());
        addTransition(PRE_MIGRATION_STARTED, State.PRE_MIGRATION_SUSPENDED.name(), State.PRE_MIGRATION_STARTED.name());
        addTransition(PRE_MIGRATION_FAILED, State.PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_FAILED.name());
        addTransition(PRE_MIGRATION_FAILED, State.PRE_MIGRATION_FAILED.name(), State.PRE_MIGRATION_FAILED.name());
        addTransition(PRE_MIGRATION_SUSPENDED, State.PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_SUSPENDED.name());
        addTransition(PRE_MIGRATION_SUCCESSFUL, State.PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_COMPLETED.name());
        addTransition(PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED, State.PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name());
        addTransition(PRE_MIGRATION_STARTED, State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), State.PRE_MIGRATION_STARTED.name());

        addTransition(MIGRATION_CANCELLED, "*", State.MIGRATION_CANCELLED.name());
        addTransition(MIGRATION_STARTED, State.PRE_MIGRATION_COMPLETED.name(), State.MIGRATION_STARTED.name());
        addTransition(MIGRATION_STARTED, State.MIGRATION_SUSPENDED.name(), State.MIGRATION_STARTED.name());
        addTransition(MIGRATION_FAILED, State.PRE_MIGRATION_COMPLETED.name(), State.MIGRATION_FAILED.name());
        addTransition(MIGRATION_FAILED, State.MIGRATION_STARTED.name(), State.MIGRATION_FAILED.name());
        addTransition(MIGRATION_SUCCESSFUL, State.MIGRATION_STARTED.name(), State.MIGRATION_COMPLETED.name());
        addTransition(MIGRATION_SUCCESSFUL_WITH_WARNING, State.MIGRATION_STARTED.name(), State.MIGRATION_COMPLETED_WITH_WARNING.name());
        addTransition(MIGRATION_SUSPENDED, State.MIGRATION_STARTED.name(), State.MIGRATION_SUSPENDED.name());
        addTransition(MIGRATION_IMPORT_CONFIGURATION_SUSPENDED, State.MIGRATION_STARTED.name(), State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name());
        addTransition(MIGRATION_STARTED, State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), State.MIGRATION_STARTED.name());

        addTransition(EOI_INTEGRATION_FAILED, State.EOI_INTEGRATION_STARTED.name(), State.EOI_INTEGRATION_FAILED.name());
        addTransition(EOI_INTEGRATION_FAILED, State.EOI_INTEGRATION_FAILED.name(), State.EOI_INTEGRATION_FAILED.name());
        addTransition(EOI_INTEGRATION_ROLLBACK_FAILED, State.EOI_INTEGRATION_STARTED.name(), State.EOI_INTEGRATION_ROLLBACK_FAILED.name());
        addTransition(EOI_INTEGRATION_STARTED, State.READY_FOR_EOI_INTEGRATION.name(), State.EOI_INTEGRATION_STARTED.name());
        addTransition(EOI_INTEGRATION_STARTED, State.EOI_INTEGRATION_FAILED.name(), State.EOI_INTEGRATION_STARTED.name());
        addTransition(EOI_INTEGRATION_SUCCESSFUL, State.EOI_INTEGRATION_STARTED.name(), State.EOI_INTEGRATION_COMPLETED.name());


        INTERNAL_NODE_STATE_TRANSITIONS
            .add(new StateTransition(State.EXPANSION_SUSPENDED.name(), EXPANSION_STARTED.name(), State.EXPANSION_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS
            .add(new StateTransition(State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.name(), EXPANSION_STARTED.name(), State.EXPANSION_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS
            .add(new StateTransition(State.READY_FOR_EXPANSION.name(), EXPANSION_STARTED.name(), State.EXPANSION_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS.add(new StateTransition(State.READY_FOR_ORDER.name(), ORDER_STARTED.name(), State.ORDER_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS.add(new StateTransition(State.ORDER_SUSPENDED.name(), ORDER_STARTED.name(), State.ORDER_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS
            .add(new StateTransition(State.READY_FOR_HARDWARE_REPLACE.name(), HARDWARE_REPLACE_STARTED.name(), State.HARDWARE_REPLACE_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS.add(new StateTransition(State.READY_FOR_PRE_MIGRATION.name(), PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS.add(new StateTransition(State.PRE_MIGRATION_SUSPENDED.name(), PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS
            .add(new StateTransition(State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.name(), PRE_MIGRATION_STARTED.name(), State.PRE_MIGRATION_STARTED.name()));
        INTERNAL_NODE_STATE_TRANSITIONS
            .add(new StateTransition(State.READY_FOR_EOI_INTEGRATION.name(), EOI_INTEGRATION_STARTED.name(), State.EOI_INTEGRATION_STARTED.name()));

    }

    private NodeStateTransitions() {

    }

    private static void addTransition(final StateTransitionEvent eventName, final String fromState, final String toState) {
        VALID_NODE_STATE_TRANSITIONS.add(new StateTransition(fromState, eventName.name(), toState));
    }

    /**
     * Get all valid AP state transitions.
     *
     * @return valid state transitions
     */
    public static List<StateTransition> getTransitions() {
        return VALID_NODE_STATE_TRANSITIONS;
    }

    public static List<StateTransition> getInternalTransitions() {
        return INTERNAL_NODE_STATE_TRANSITIONS;
    }
}
