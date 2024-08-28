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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.ericsson.oss.services.ap.api.status.State.*;

/**
 * Defines the overall project level integration phases for node stateNames.
 */
public enum IntegrationPhase {

    IN_PROGRESS(
        "In Progress", READY_FOR_ORDER, INVALID_CONFIGURATION, DELETE_STARTED, ORDER_STARTED, ORDER_COMPLETED,
        HARDWARE_REPLACE_STARTED, BIND_STARTED, BIND_COMPLETED, INTEGRATION_STARTED, UNKNOWN, HARDWARE_REPLACE_BIND_COMPLETED,
        EXPANSION_STARTED, READY_FOR_EXPANSION, READY_FOR_HARDWARE_REPLACE, READY_FOR_PRE_MIGRATION, PRE_MIGRATION_STARTED, PRE_MIGRATION_COMPLETED, MIGRATION_STARTED,
        PRE_MIGRATION_BIND_STARTED, PRE_MIGRATION_BIND_COMPLETED, READY_FOR_EOI_INTEGRATION, EOI_INTEGRATION_STARTED),
    SUSPENDED(
        "Suspended", ORDER_SUSPENDED, HARDWARE_REPLACE_SUSPENDED, INTEGRATION_SUSPENDED, EXPANSION_SUSPENDED, MIGRATION_SUSPENDED, PRE_MIGRATION_SUSPENDED,
        INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED, EXPANSION_IMPORT_CONFIGURATION_SUSPENDED, PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED, MIGRATION_IMPORT_CONFIGURATION_SUSPENDED),
    SUCCESSFUL(
        "Successful", INTEGRATION_COMPLETED, INTEGRATION_COMPLETED_WITH_WARNING, HARDWARE_REPLACE_COMPLETED,
        EXPANSION_COMPLETED, MIGRATION_COMPLETED, MIGRATION_COMPLETED_WITH_WARNING, EOI_INTEGRATION_COMPLETED),
    FAILED(
        "Failed", ORDER_FAILED, ORDER_ROLLBACK_FAILED, INTEGRATION_FAILED, DELETE_FAILED, HARDWARE_REPLACE_FAILED,
        HARDWARE_REPLACE_ROLLBACK_FAILED, EXPANSION_FAILED, PRE_MIGRATION_FAILED, MIGRATION_FAILED, EOI_INTEGRATION_FAILED, EOI_INTEGRATION_ROLLBACK_FAILED),
    CANCELLED("Cancelled", INTEGRATION_CANCELLED, ORDER_CANCELLED, EXPANSION_CANCELLED, PRE_MIGRATION_CANCELLED, MIGRATION_CANCELLED);

    private static final List<IntegrationPhase> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));

    private final String name;
    private final Set<State> stateNames;

    private IntegrationPhase(final String name, final State... states) {
        this.name = name;
        stateNames = EnumSet.copyOf(Arrays.asList(states));
    }

    /**
     * The name of the {@link IntegrationPhase}.
     *
     * @return the IntegrationPhase as a string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the {@link IntegrationPhase} from an AP node state.
     *
     * @param nodeState
     *            an AP node state
     * @return IntegrationPhase the corresponding phase for the given state
     */
    public static IntegrationPhase getIntegrationPhase(final String nodeState) {
        final State state = State.valueOf(nodeState);
        for (final IntegrationPhase phase : IntegrationPhase.valuesAsList()) {
            if (phase.stateNames.contains(state)) {
                return phase;
            }
        }
        throw new IllegalArgumentException(String.format("State %s is not in the list of Integration Phases", nodeState));
    }

    /**
     * Returns all {@link IntegrationPhase} values as a {@link List}.
     * <p>
     * To be used instead of {@link #values()}, as it does not create a new array for each invocation.
     *
     * @return all enum values
     */
    public static List<IntegrationPhase> valuesAsList() {
        return VALUES_AS_LIST;
    }
}
