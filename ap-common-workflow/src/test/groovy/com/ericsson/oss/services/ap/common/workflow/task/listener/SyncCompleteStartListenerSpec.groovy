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

import static com.ericsson.oss.services.ap.common.model.CmSyncStatus.SYNCHRONIZED;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY
import java.util.concurrent.Callable

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.status.StatusEntry
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal
import com.ericsson.oss.services.ap.api.status.StatusEntryNames
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.services.wfs.task.api.TaskExecution
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.ap.api.status.APNodePoller

class SyncCompleteStartListenerSpec extends CdiSpecification {

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = "project=Project1,node=Node1"

    @ObjectUnderTest
    SyncCompleteStartListener syncCompleteStartListener;

    @MockedImplementation
    private ServiceFinderSPI serviceFinderSPI

    @Inject
    private StatusEntryManagerLocal statusEntryManager

    @Inject
    private  APNodePoller poller

    @MockedImplementation
    private TransactionalExecutor executor

    @MockedImplementation
    private TaskExecution execution

    private RuntimeConfigurableDps dps
    protected ManagedObject projectMo
    protected ManagedObject nodeMo

    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    def setup () {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        executor.execute(_ as Callable) >> { Callable call -> call.call() }
        Whitebox.setInternalState(syncCompleteStartListener.dpsOperations, "dps", dps.build())
        Whitebox.setInternalState(syncCompleteStartListener.dpsOperations, "executor", executor)
        MoCreatorSpec.setDps(dps)

        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(StatusEntryManagerLocal.class, null) >> statusEntryManager
        serviceFinderSPI.find(APNodePoller.class, "cmsync") >> poller
    }

    def "when SyncCompleteStartListener is executed for hardwareReplace activity and SYNC_NODE_NOTIFICATION status entry does not exist, verify nodeMO is updated"() {
        given: "An Node MO and hardwareReplace activity"
            workflowVariables.setApNodeFdn(NODE_FDN)
            workflowVariables.setActivity("hardwareReplace")
            execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
            final Map<String,Object> attributes = new HashMap<>();
            attributes.put(NodeAttribute.WAITING_FOR_MESSAGE.toString(), null)
            projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
            nodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, attributes)
        and: "SYNC_NODE_NOTIFICATION status entry does not exist"
            statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.SYNC_NODE_NOTIFICATION.toString()) >> null

        when:"executeTask is called"
            syncCompleteStartListener.executeTask(execution)

        then: "verify nodeMO attribute WAITING_FOR_MESSAGE is updated"
            nodeMo.getAttribute(NodeAttribute.WAITING_FOR_MESSAGE.toString()) == SYNCHRONIZED.toString()
    }

    def "when SyncCompleteStartListener is executed for hardwareReplace activity and SYNC_NODE_NOTIFICATION status entry in Waiting status, verify nodeMO is updated"() {
        given: "An Node MO and hardwareReplace activity"
            workflowVariables.setApNodeFdn(NODE_FDN)
            workflowVariables.setActivity("hardwareReplace")
            execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
            final Map<String,Object> attributes = new HashMap<>();
            attributes.put(NodeAttribute.WAITING_FOR_MESSAGE.toString(), null)
            projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
            nodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, attributes)
        and: "SYNC_NODE_NOTIFICATION status entry in Waiting status"
            statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.SYNC_NODE_NOTIFICATION.toString()) >>
                new StatusEntry(StatusEntryNames.SYNC_NODE_NOTIFICATION.toString(), "Waiting", "", "")

        when:"executeTask is called"
            syncCompleteStartListener.executeTask(execution)

        then: "verify nodeMO attribute WAITING_FOR_MESSAGE is updated"
            nodeMo.getAttribute(NodeAttribute.WAITING_FOR_MESSAGE.toString()) == SYNCHRONIZED.toString()
    }

    def "when SyncCompleteStartListener is executed for greenfield activity, verify nodeMO is updated"() {
        given: "An Node MO and greenfield activity"
            workflowVariables.setApNodeFdn(NODE_FDN)
            workflowVariables.setActivity("greenfield")
            execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
            final Map<String,Object> attributes = new HashMap<>();
            attributes.put(NodeAttribute.WAITING_FOR_MESSAGE.toString(), null)
            projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
            nodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, attributes)

        when:"executeTask is called"
            syncCompleteStartListener.executeTask(execution)

        then: "verify nodeMO attribute WAITING_FOR_MESSAGE is updated"
            nodeMo.getAttribute(NodeAttribute.WAITING_FOR_MESSAGE.toString()) == SYNCHRONIZED.toString()
    }
}
