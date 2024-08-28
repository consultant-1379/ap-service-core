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
package com.ericsson.oss.services.ap.core.usecase.delete;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;

public class DeleteSkipStateOptions {

    protected StateTransitionManagerLocal stateTransitionManager;

    protected static final Set<String> SKIP_DELETE_WORKFLOW_STATES = new HashSet<>();

    private ServiceFinderBean serviceFinder = new ServiceFinderBean();

    static {
        SKIP_DELETE_WORKFLOW_STATES.add(State.EXPANSION_CANCELLED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.EXPANSION_COMPLETED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.EXPANSION_FAILED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.EXPANSION_STARTED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.EXPANSION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.HARDWARE_REPLACE_COMPLETED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.HARDWARE_REPLACE_FAILED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.INTEGRATION_COMPLETED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.INTEGRATION_COMPLETED_WITH_WARNING.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.INTEGRATION_CANCELLED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.ORDER_FAILED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.ORDER_CANCELLED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.READY_FOR_EXPANSION.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.READY_FOR_HARDWARE_REPLACE.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.INTEGRATION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.PRE_MIGRATION_FAILED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.PRE_MIGRATION_CANCELLED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.PRE_MIGRATION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.MIGRATION_COMPLETED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.MIGRATION_COMPLETED_WITH_WARNING.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.MIGRATION_CANCELLED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.MIGRATION_SUSPENDED.toString());
        SKIP_DELETE_WORKFLOW_STATES.add(State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString());
    }

    @PostConstruct
    public void init() {
        stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
    }
}
