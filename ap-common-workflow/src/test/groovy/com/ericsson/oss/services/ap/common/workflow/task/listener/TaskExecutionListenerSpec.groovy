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
package com.ericsson.oss.services.ap.common.workflow.task.listener

import javax.inject.Inject
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import org.mockito.internal.util.reflection.Whitebox
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal
import com.ericsson.oss.services.ap.common.workflow.ActivityType
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class TaskExecutionListenerSpec extends CdiSpecification {

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @Inject
    private StatusEntryManagerLocal statusEntryManager;

    @MockedImplementation
    private StateTransitionManagerLocal stateTransitionManager;

    @Inject
    private TaskExecution execution;

    @Inject
    private DpsOperations dpsOperations

    @Inject
    private ServiceFinderSPI serviceFinderSPI

    @Inject
    DataPersistenceService dataPersistenceService
    protected static final String PROJECT_FDN = "Project=Project1"

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"

    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    final Map<String,Object> attributes = new HashMap<>();
    private ManagedObject projectMo;
    private ManagedObject apNodeMo;
    private ManagedObject nodeStatusMo
    private RuntimeConfigurableDps configurableDps;

    TaskExecutionListener taskExecutionListener = new TaskExecutionListener() {

        /**
         * Adds a new status entry for the AP Node MO, or updates an existing entry.
         *
         * @param nodeFdn
         *            the AP Node whose status to update
         */
        @Override
        protected void updateStatus(String nodeFdn) {

        }
    }

    def setup() {
        configurableDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = configurableDps.build()
        Whitebox.setInternalState(dpsOperations, "dps", dataPersistenceService)
        MoCreatorSpec.setDps(configurableDps)
        projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
        apNodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(StatusEntryManagerLocal.class, null) >> statusEntryManager
        serviceFinderSPI.find(StateTransitionManagerLocal.class, null) >> stateTransitionManager
        serviceFinderSPI.find(DataPersistenceService.class, null) >> dataPersistenceService
    }

    def "when activity started state is selected based on the node activity"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(NODE_FDN);
        workflowVariables.setActivity(ActivityType.MIGRATION_ACTIVITY.getActivityName())
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking setActivityStartedState method"
        attributes.put(NodeStatusAttribute.STATE.toString(), nodeState)
        nodeStatusMo = MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, attributes)
        taskExecutionListener.setActivityStartedState(ap_node_fdn, activity)

        then: "Migration Activity Started"
        1 * stateTransitionManager.validateAndSetNextState(ap_node_fdn, state)

        where:
        ap_node_fdn | activity                                           | nodeState                                                                    | state
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.MIGRATION_SUSPENDED.toString()                          | StateTransitionEvent.MIGRATION_STARTED
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.PRE_MIGRATION_SUSPENDED.toString()                      | StateTransitionEvent.PRE_MIGRATION_STARTED
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.PRE_MIGRATION_FAILED.toString()                         | StateTransitionEvent.PRE_MIGRATION_STARTED
        NODE_FDN    | ActivityType.GREENFIELD_ACTIVITY.getActivityName() | StateTransitionEvent.INTEGRATION_SUSPENDED.toString()                        | StateTransitionEvent.INTEGRATION_STARTED
        NODE_FDN    | ActivityType.EXPANSION_ACTIVITY.getActivityName()  | StateTransitionEvent.EXPANSION_SUSPENDED.toString()                          | StateTransitionEvent.EXPANSION_STARTED
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString()     | StateTransitionEvent.MIGRATION_STARTED
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString() | StateTransitionEvent.PRE_MIGRATION_STARTED
        NODE_FDN    | ActivityType.GREENFIELD_ACTIVITY.getActivityName() | StateTransitionEvent.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED.toString()   | StateTransitionEvent.INTEGRATION_STARTED
        NODE_FDN    | ActivityType.EXPANSION_ACTIVITY.getActivityName()  | StateTransitionEvent.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED.toString()     | StateTransitionEvent.EXPANSION_STARTED
    }

    def "when activity suspended state is set based on the node activity"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(NODE_FDN);
        workflowVariables.setActivity(ActivityType.MIGRATION_ACTIVITY.getActivityName())
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking setActivitySuspendedStateOnImportFailure method"
        attributes.put(NodeStatusAttribute.STATE.toString(), nodeState)
        nodeStatusMo = MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, attributes)
        taskExecutionListener.setActivitySuspendedStateOnImportFailure(ap_node_fdn, activity)

        then: "Migration Activity Suspended"
        1 * stateTransitionManager.validateAndSetNextState(ap_node_fdn, state)

        where:
        ap_node_fdn | activity                                           | nodeState                                               | state
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.MIGRATION_STARTED.toString()       | StateTransitionEvent.MIGRATION_IMPORT_CONFIGURATION_SUSPENDED
        NODE_FDN    | ActivityType.MIGRATION_ACTIVITY.getActivityName()  | StateTransitionEvent.PRE_MIGRATION_STARTED.toString()   | StateTransitionEvent.PRE_MIGRATION_IMPORT_CONFIGURATION_SUSPENDED
        NODE_FDN    | ActivityType.GREENFIELD_ACTIVITY.getActivityName() | StateTransitionEvent.INTEGRATION_STARTED.toString()     | StateTransitionEvent.INTEGRATION_IMPORT_CONFIGURATION_SUSPENDED
        NODE_FDN    | ActivityType.EXPANSION_ACTIVITY.getActivityName()  | StateTransitionEvent.EXPANSION_STARTED.toString()       | StateTransitionEvent.EXPANSION_IMPORT_CONFIGURATION_SUSPENDED
    }
}
