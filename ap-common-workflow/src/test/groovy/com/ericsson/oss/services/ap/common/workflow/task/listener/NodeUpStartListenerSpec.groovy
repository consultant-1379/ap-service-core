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

import java.util.concurrent.Callable

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal
import com.ericsson.oss.services.ap.api.status.StatusEntryNames
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.workflow.messages.NodeUpMessage

class NodeUpStartListenerSpec extends CdiSpecification{

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = "project=Project1,node=Node1"

    @ObjectUnderTest
    NodeUpStartListener nodeUpStartListener

    @MockedImplementation
    private ServiceFinderSPI serviceFinderSPI

    @Inject
    private StatusEntryManagerLocal statusEntryManager

    @MockedImplementation
    private TransactionalExecutor executor

    private RuntimeConfigurableDps dps
    protected ManagedObject projectMo
    protected ManagedObject nodeMo

    def setup () {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        executor.execute(_ as Callable) >> { Callable call -> call.call() }
        Whitebox.setInternalState(nodeUpStartListener.dpsOperations, "dps", dps.build())
        Whitebox.setInternalState(nodeUpStartListener.dpsOperations, "executor", executor)
        MoCreatorSpec.setDps(dps)

        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(StatusEntryManagerLocal.class, null) >> statusEntryManager
    }

    def "when NodeUpStartListener is executed, verify nodeMO is updated"() {
        given: "An Node MO"
            final Map<String,Object> attributes = new HashMap<>();
            attributes.put(NodeAttribute.WAITING_FOR_MESSAGE.toString(), null)
            projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
            nodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, attributes)

        when:"updateStatus is called"
            nodeUpStartListener.updateStatus(NODE_FDN)

        then: "verify nodeMO attribute WAITING_FOR_MESSAGE is updated"
            1 * statusEntryManager.waitingForNotification(NODE_FDN, StatusEntryNames.NODE_UP.toString())
            nodeMo.getAttribute(NodeAttribute.WAITING_FOR_MESSAGE.toString()) == NodeUpMessage.getMessageKey()
    }
}
