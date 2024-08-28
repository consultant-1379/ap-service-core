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

import javax.ejb.Local;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

import com.ericsson.oss.services.ap.api.exception.ApServiceException; //NOPMD
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException; //NOPMD

/**
 * Provides centralised management of states, controlling the transition from one state to another.
 * <p>
 * State transitions are driven by events as defined in {@link StateTransitionEvent}.
 */
@Local
@EService
public interface StateTransitionManagerLocal {

    /**
     * Verify if it is an allowed state transition.
     *
     * @param currentState
     *            the current state of a node
     * @param event
     *            the state transition event
     * @return boolean
     *            true if valid transition
     * @throws ApServiceException
     *             if there is an error reading the state
     */
    boolean isValidStateTransition(final String currentState, final StateTransitionEvent event);

    /**
     * Sets the state to the specified value. No validation of state transition is performed.
     *
     * @param nodeFdn
     *            the FDN of the node whose state is to be updated
     * @param state
     *            new state to set
     * @throws ApServiceException
     *             if there is an error updating the state
     */
    void setStateWithoutValidation(final String nodeFdn, final State state);

    /**
     * Validates a transition exists from the current state for the specified event prior to setting the next state.
     *
     * @param nodeFdn
     *            the FDN of the node whose state is to be updated
     * @param event
     *            the state transition event
     * @throws ApServiceException
     *             if there is an error reading or updating the state
     * @throws InvalidNodeStateException
     *             if no valid transition exists for the event in the node's current state
     */
    void validateAndSetNextState(final String nodeFdn, StateTransitionEvent event);
}
