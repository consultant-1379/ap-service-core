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
package com.ericsson.oss.services.ap.api.status;

import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_CANCELLED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_COMPLETED_WITH_WARNING;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_BIND_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_BIND_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_CANCELLED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_SUSPENDED;
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_PRE_MIGRATION;
import static com.ericsson.oss.services.ap.api.status.State.UNKNOWN;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Defines the phases for a specific activity according to node state.
 */
public enum ActivityPhase {

    PREMIGRATION_PHASE("PreMigration Phase", READY_FOR_PRE_MIGRATION, PRE_MIGRATION_CANCELLED, PRE_MIGRATION_COMPLETED, PRE_MIGRATION_FAILED, PRE_MIGRATION_STARTED, PRE_MIGRATION_SUSPENDED, PRE_MIGRATION_BIND_STARTED, PRE_MIGRATION_BIND_COMPLETED, PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED),
    MIGRATION_PHASE("Migration Phase", MIGRATION_CANCELLED, MIGRATION_COMPLETED, MIGRATION_COMPLETED_WITH_WARNING, MIGRATION_FAILED, MIGRATION_STARTED, MIGRATION_SUSPENDED, MIGRATION_IMPORT_CONFIGURATION_SUSPENDED),
    UNKNOWN_PHASE("Unknown Phase", UNKNOWN);

    private static final List<ActivityPhase> VALUES_AS_LIST = Collections.unmodifiableList(Arrays.asList(values()));

    private final String name;
    private final Set<State> stateNames;

    private ActivityPhase(final String name, final State... states) {
        this.name = name;
        stateNames = EnumSet.copyOf(Arrays.asList(states));
    }

    /**
     * The name of the {@link ActivityPhase}.
     *
     * @return the ActivityPhase as a string
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the {@link ActivityPhase} from an AP node state.
     *
     * @param nodeState
     *        an AP node state
     * @return ActivityPhase the corresponding phase for the given state
     */
    public static ActivityPhase getActivityPhase(final String nodeState) {
        final State state = State.getState(nodeState);
        for (final ActivityPhase phase : ActivityPhase.valuesAsList()) {
            if (phase.stateNames.contains(state)) {
                return phase;
            }
        }
        return UNKNOWN_PHASE;
    }

    /**
     * Returns all {@link ActivityPhase} values as a {@link List}.
     * <p>
     * To be used instead of {@link #values()}, as it does not create a new array for each
     * invocation.
     *
     * @return all enum values
     */
    public static List<ActivityPhase> valuesAsList() {
        return VALUES_AS_LIST;
    }
}
