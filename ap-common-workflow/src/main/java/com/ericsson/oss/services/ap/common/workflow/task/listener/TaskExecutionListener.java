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
package com.ericsson.oss.services.ap.common.workflow.task.listener;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.ActivityPhase;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract listener class that can be attached to start/end events for tasks in BPMN workflows.
 * <p>
 * Typically used to add/update a statusEntry for an AP <code>Node</code> MO.
 */
public abstract class TaskExecutionListener extends AbstractServiceTask {

    private StatusEntryManagerLocal statusEntryManager;

    private StateTransitionManagerLocal stateTransitionManager;

    private final DpsOperations dps = new DpsOperations();

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String nodeFdn = workflowVariables.getApNodeFdn();
        updateStatus(nodeFdn);
    }

    /**
     * Adds a new status entry for the AP Node MO, or updates an existing entry.
     *
     * @param nodeFdn
     *        the AP Node whose status to update
     */
    protected abstract void updateStatus(final String nodeFdn);

    /**
     * Sets an activity to a started state.
     *
     * @param apNodeFdn
     *        the AP Node whose state to update
     * @param activity
     *        the activity which is being executed
     */
    protected void setActivityStartedState(final String apNodeFdn, final String activity) {
        StateTransitionEvent stateTransitionEvent = null;
        switch (ActivityType.from(activity)) {
        case EXPANSION_ACTIVITY:
            stateTransitionEvent = StateTransitionEvent.EXPANSION_STARTED;
            break;

        case MIGRATION_ACTIVITY:
            stateTransitionEvent = ActivityPhase.PREMIGRATION_PHASE.equals(ActivityPhase
                    .getActivityPhase(getCurrentNodeState(apNodeFdn)))
                            ? StateTransitionEvent.PRE_MIGRATION_STARTED
                            : StateTransitionEvent.MIGRATION_STARTED;
            break;

        default:
            stateTransitionEvent = StateTransitionEvent.INTEGRATION_STARTED;
            break;
        }

        getStateTransitionManager().validateAndSetNextState(apNodeFdn, stateTransitionEvent);

    }

    /**
     * * Sets an activity to a suspended state.
     *
     * @param apNodeFdn
     *        the AP Node whose state to update
     * @param activity
     *        the activity which is being executed
     */
    protected void setActivitySuspendedState(final String apNodeFdn, final String activity) {
        State nodeState = null;
        StateTransitionEvent stateTransitionEvent = null;
        switch (ActivityType.from(activity)) {
        case EXPANSION_ACTIVITY:
            nodeState = State.EXPANSION_SUSPENDED;
            stateTransitionEvent = StateTransitionEvent.EXPANSION_SUSPENDED;
            break;

        case MIGRATION_ACTIVITY:
            if (ActivityPhase.PREMIGRATION_PHASE.equals(ActivityPhase
                    .getActivityPhase(getCurrentNodeState(apNodeFdn)))) {
                nodeState = State.PRE_MIGRATION_SUSPENDED;
                stateTransitionEvent = StateTransitionEvent.PRE_MIGRATION_SUSPENDED;
            } else {
                nodeState = State.MIGRATION_SUSPENDED;
                stateTransitionEvent = StateTransitionEvent.MIGRATION_SUSPENDED;
            }
            break;

        default:
            nodeState = State.INTEGRATION_SUSPENDED;
            stateTransitionEvent = StateTransitionEvent.INTEGRATION_SUSPENDED;
            break;
        }

        getStatusEntryManager().printNodeState(apNodeFdn, nodeState);
        getStateTransitionManager().validateAndSetNextState(apNodeFdn, stateTransitionEvent);
    }

    /**
     * * Sets an activity to a suspended state on import failure.
     *
     * @param apNodeFdn
     *            the AP Node whose state to update
     * @param activity
     *            the activity which is being executed
     */
    protected void setActivitySuspendedStateOnImportFailure(final String apNodeFdn, final String activity) {
        State nodeState = null;
        StateTransitionEvent stateTransitionEvent = null;
        switch (ActivityType.from(activity)) {
        case EXPANSION_ACTIVITY:
            nodeState = State.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED;
            stateTransitionEvent = StateTransitionEvent.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED;
            break;

        case MIGRATION_ACTIVITY:
            if (ActivityPhase.PREMIGRATION_PHASE
                    .equals(ActivityPhase.getActivityPhase(getCurrentNodeState(apNodeFdn)))) {
                nodeState = State.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
                stateTransitionEvent = StateTransitionEvent.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
            } else {
                nodeState = State.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
                stateTransitionEvent = StateTransitionEvent.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED;
            }
            break;

        default:
            nodeState = State.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED;
            stateTransitionEvent = StateTransitionEvent.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED;
            break;
        }

        getStatusEntryManager().printNodeState(apNodeFdn, nodeState);
        getStateTransitionManager().validateAndSetNextState(apNodeFdn, stateTransitionEvent);
    }

    private String getCurrentNodeState(final String nodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final ManagedObject nodeStatusMo = nodeMo.getChild(MoType.NODE_STATUS.toString() + "=1");
        return nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

    /**
     * Returns an instance of {@link StatusEntryManagerLocal}.
     *
     * @return {@link StatusEntryManagerLocal}
     */
    protected StatusEntryManagerLocal getStatusEntryManager() {
        if (statusEntryManager == null) {
            statusEntryManager = new ServiceFinderBean().find(StatusEntryManagerLocal.class);
        }
        return statusEntryManager;
    }

    /**
     * Returns an instance of {@link StatusEntryManagerLocal}.
     *
     * @return {@link StatusEntryManagerLocal}
     */
    protected StateTransitionManagerLocal getStateTransitionManager() {
        if (stateTransitionManager == null) {
            stateTransitionManager = new ServiceFinderBean().find(StateTransitionManagerLocal.class);
        }
        return stateTransitionManager;
    }
}
