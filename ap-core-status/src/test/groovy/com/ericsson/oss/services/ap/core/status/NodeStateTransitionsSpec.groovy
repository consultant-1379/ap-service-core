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
package com.ericsson.oss.services.ap.core.status


import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.status.State
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent

import static com.ericsson.oss.services.ap.api.status.StateTransitionEvent.PRE_MIGRATION_STARTED

class NodeStateTransitionsSpec extends CdiSpecification {

    def "test to check states"() {
        given: "given the event and current state of the node"
        StateTransitionEvent eventName = PRE_MIGRATION_STARTED;
        final State currentState = State.READY_FOR_PRE_MIGRATION;

        when: "Invoking all the possible state transitions"
        List<StateTransition> stateTransitionList = NodeStateTransitions.getTransitions();

        then: "check whether the to state is valid or not"
        String toState = null;
        for (final StateTransition transition : stateTransitionList) {
            final String fromState = transition.from();
            if (isValidStateTransition(currentState, eventName, transition, fromState)) {
                toState = State.valueOf(transition.to());
            }
        }
        assert toState != null && toState == State.PRE_MIGRATION_STARTED.name()
    }

    private boolean isValidStateTransition(final State currentState, final StateTransitionEvent event, final StateTransition transition,
                                           final String fromState) {
        return (fromState.equals(currentState.name()) || "*".equals(fromState)) && transition.getEvent().equals(event.name());
    }
}
