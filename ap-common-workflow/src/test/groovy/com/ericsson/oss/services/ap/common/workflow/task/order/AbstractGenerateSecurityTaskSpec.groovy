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
package com.ericsson.oss.services.ap.common.workflow.task.order


import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractGenerateSecurityTaskSpec extends CdiSpecification {

    @Inject
    private ManagedObject nodeMo

    @Inject
    private TaskExecution execution

    AbstractGenerateSecurityTask abstractGenerateSecurityTask = new AbstractGenerateSecurityTask() {

        /**
         * Generates security for an AP node.
         *
         * @param apNodeFdn
         *            the FDN of the AP node
         * @param hardwareSerialNumber
         *            the hardware serial number of the node
         */
        @Override
        protected void generateSecurity(String apNodeFdn, String hardwareSerialNumber) {
            if (apNodeFdn.contains("FAILED")) {
                throw new ApApplicationException("test")
            }
        }
    }
    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String INVALID_NODE_FDN = "Project=Project1,Node=FAILED"

	def "when generating security is success"() {
		given: "Node MO details"
		nodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) >> true
		workflowVariables.setApNodeFdn(NODE_FDN);
		workflowVariables.setHardwareSerialNumber("SCB187654321")
		execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
		workflowVariables.setMigrationNode(true)
		workflowVariables.isMigrationNodeUsecase() >> true

		when: "invoking generate security task"
		abstractGenerateSecurityTask.executeTask(execution)

		then: "Validated successfully"
		workflowVariables.isPreMigrationSuccessful() == true
	}

    def "when generating security for migration node is failed then pre migration is marked as false"() {
        given: "Node MO details"
        nodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) >> true
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
        workflowVariables.setMigrationNode(true)
        workflowVariables.isMigrationNodeUsecase() >> true

        when: "invoking generate security task"
        abstractGenerateSecurityTask.executeTask(execution)

        then: "Validated successfully"
        thrown ClassCastException
        workflowVariables.isPreMigrationSuccessful() == false
    }

    def "when generating security for non migration node is failed then order is marked as false"() {
        given: "Node MO details"
        nodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) >> false
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
        workflowVariables.setMigrationNode(false)
        workflowVariables.isMigrationNodeUsecase() >> false

        when: "invoking generate security task"
        abstractGenerateSecurityTask.executeTask(execution)

        then: "Validated successfully"
        thrown ClassCastException
        workflowVariables.isOrderSuccessful() == false
    }
}
