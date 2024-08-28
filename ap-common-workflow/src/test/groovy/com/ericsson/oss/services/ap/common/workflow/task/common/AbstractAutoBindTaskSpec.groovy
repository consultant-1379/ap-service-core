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
package com.ericsson.oss.services.ap.common.workflow.task.common

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractAutoBindTaskSpec extends CdiSpecification {

    @Inject
    private ManagedObject nodeMo;

    @Inject
    private TaskExecution execution;

    private boolean isNodeBound = false

    AbstractAutoBindTask abstractAutoBindTask = new AbstractAutoBindTask() {

        /**
         * Binds the supplied hardware serial number to the given node.
         *
         * @param apNodeFdn
         *            the FDN of the AP node
         * @param hardwareSerialNumber
         *            the hardware serial number to bind
         * @param bindWithNodeName
         *            performs Bind with node name if true
         */
        @Override
        protected void bindNode(String apNodeFdn, String hardwareSerialNumber, boolean bindWithNodeName) {
            isNodeBound = false
            if (apNodeFdn.contains("FAILED")) {
                throw new ApApplicationException("test")
            }
        }
    }

    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String INVALID_NODE_FDN = "Project=Project1,Node=FAILED"

    def "Auto Bind task for migration node is failed then pre migration is marked as false"() {
        given: "Node MO details"
        System.setProperty("autoProvisioning_bind_nodeName", "true")
        workflowVariables.setNodeType("RadioNode")
        nodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) >> true
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
        workflowVariables.setMigrationNode(true)
        workflowVariables.isMigrationNodeUsecase() >> true

        when: "invoking generate security task"
        abstractAutoBindTask.executeTask(execution)

        then: "Validated succssfully"
        thrown ClassCastException
        workflowVariables.isPreMigrationSuccessful() == false
    }

    def "Auto Bind task for non migration node is failed then order is marked as false"() {
        given: "Node MO details"
        System.setProperty("autoProvisioning_bind_nodeName", "true")
        workflowVariables.setNodeType("RadioNode")
        nodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) >> false
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
        workflowVariables.setMigrationNode(false)
        workflowVariables.isMigrationNodeUsecase() >> false

        when: "invoking generate security task"
        abstractAutoBindTask.executeTask(execution)

        then: "Validated succssfully"
        thrown ClassCastException
        workflowVariables.isOrderSuccessful() == false
    }

}
