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

import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.transaction.RollbackException;

import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerNonCDIImpl;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;

/**
 * EJB managing the state transitions for an AP <code>Node</code>. Retries will be executed in the event of transaction rollback failures.
 */
@Stateless
public class StateTransitionManagerEjb implements StateTransitionManagerLocal {

    @Inject
    private NodeStatusMoUpdater nodeStatusMoUpdater;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void validateAndSetNextState(final String apNodeFdn, final StateTransitionEvent event) {
        final RetriableCommand<Void> retriableCommand = new RetriableCommand<Void>() {

            @Override
            public Void execute(final RetryContext retryContext) {
                nodeStatusMoUpdater.validateAndSetNextState(apNodeFdn, event);
                return null;
            }
        };
        executeRetriableCommand(retriableCommand);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void setStateWithoutValidation(final String apNodeFdn, final State state) {
        final RetriableCommand<Void> retriableCommand = new RetriableCommand<Void>() {

            @Override
            public Void execute(final RetryContext retryContext) {
                nodeStatusMoUpdater.setState(apNodeFdn, NodeStatusAttribute.STATE.toString(), state.name());
                return null;
            }

        };
        executeRetriableCommand(retriableCommand);
    }

    private static void executeRetriableCommand(final RetriableCommand<Void> retriableCommand) {
        final RetryManager retryManager = new RetryManagerNonCDIImpl();
        try {
            retryManager.executeCommand(getRetryPolicy(), retriableCommand);
        } catch (final RetriableCommandException rce) {
            if (rce.getCause() instanceof RuntimeException) {
                throw (RuntimeException) rce.getCause();
            } else {
                throw rce;
            }
        }
    }

    @Override
    public boolean isValidStateTransition(final String currentState, final StateTransitionEvent event) {
        final State nextState = getNextState(State.valueOf(currentState), event);
        return State.UNKNOWN != nextState;
    }

    private static State getNextState(final State currentState, final StateTransitionEvent event) {
        for (final StateTransition transition : NodeStateTransitions.getTransitions()) {
            final String fromState = transition.from();
            if (isValidStateTransition(currentState, event, transition, fromState)) {
                return State.valueOf(transition.to());
            }
        }
        return State.UNKNOWN;
    }

    private static boolean isValidStateTransition(final State currentState, final StateTransitionEvent event, final StateTransition transition,
            final String fromState) {
        return (fromState.equals(currentState.name()) || "*".equals(fromState)) && transition.getEvent().equals(event.name());
    }

    private static RetryPolicy getRetryPolicy() {
        return RetryPolicy.builder()
                .attempts(3)
                .waitInterval(500, TimeUnit.MILLISECONDS)
                .retryOn(RollbackException.class)
                .build();
    }
}
