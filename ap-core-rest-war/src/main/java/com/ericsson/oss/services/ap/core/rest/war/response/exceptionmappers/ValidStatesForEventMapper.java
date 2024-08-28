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
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.core.status.NodeStateTransitions;
import com.ericsson.oss.services.ap.core.status.StateTransition;

/**
 * Provides functionality for {@link InvalidNodeStateExceptionMapper} to get a list of valid states for the exception thrown.
 */
public class ValidStatesForEventMapper {

    private static final Map<String, List<StateTransitionEvent>> COMMAND_TRANSITION_EVENTS = new HashMap<>();

    static {
        COMMAND_TRANSITION_EVENTS.put("delete", Arrays.asList(StateTransitionEvent.DELETE_STARTED));
        COMMAND_TRANSITION_EVENTS.put("bind", Arrays.asList(StateTransitionEvent.BIND_STARTED, StateTransitionEvent.PRE_MIGRATION_BIND_STARTED));
        COMMAND_TRANSITION_EVENTS.put("order", Arrays.asList(StateTransitionEvent.ORDER_STARTED));
        COMMAND_TRANSITION_EVENTS.put("expansion", Arrays.asList(StateTransitionEvent.EXPANSION_STARTED));
        COMMAND_TRANSITION_EVENTS.put("premigration", Arrays.asList(StateTransitionEvent.PRE_MIGRATION_STARTED));
        COMMAND_TRANSITION_EVENTS.put("replace", Arrays.asList(StateTransitionEvent.HARDWARE_REPLACE_STARTED));
    }

    /**
     * Returns a comma separated list of valid states for the command to execute.
     *
     * @param commandName
     *            the name of the command
     * @return comma-separated list of valid states
     */
    public String getValidStates(final String commandName) {
        final List<StateTransitionEvent> stateTransitionEventList = COMMAND_TRANSITION_EVENTS.get(commandName.toLowerCase(Locale.ENGLISH));

        if (stateTransitionEventList == null) {
            return "";
        }

        return buildValidStatesResult(stateTransitionEventList);
    }

    private static String buildValidStatesResult(final List<StateTransitionEvent> stateTransitionEventList) {
        final List<StateTransition> internalStateTransitions = NodeStateTransitions.getInternalTransitions();
        final StringBuilder validStatesStringBuilder = new StringBuilder();

        for (StateTransitionEvent stateTransitionEvent:
            stateTransitionEventList) {
            for (final StateTransition transition : NodeStateTransitions.getTransitions()) {
                if (isValidTransitionEvent(stateTransitionEvent.name(), internalStateTransitions, transition)) {
                    buildResultString(validStatesStringBuilder, transition);
                }
            }
        }
        return validStatesStringBuilder.toString();
    }

    private static boolean isValidTransitionEvent(final String transitionEvent, final List<StateTransition> internalStateTransitions,
        final StateTransition transition) {
        return transitionEvent.equals(transition.getEvent()) && !internalStateTransitions.contains(transition);
    }

    private static void buildResultString(final StringBuilder validStatesStringBuilder, final StateTransition transition) {
        if (validStatesStringBuilder.length() > 0) {
            validStatesStringBuilder.append(", ");
        }
        validStatesStringBuilder.append(State.getState(transition.from()).getDisplayName());
    }
}
