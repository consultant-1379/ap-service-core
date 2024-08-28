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
import com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.ap.common.workflow.task.integrate.AbstractSnmpConfigurationTask
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractSnmpConfigurationTaskSpec extends CdiSpecification {
    @Inject
    private ManagedObject nodeMo

    @Inject
    private TaskExecution execution

    AbstractSnmpConfigurationTask abstractSnmpConfigurationTask = new AbstractSnmpConfigurationTask() {

        /**
         * Creates the SNMP Configuration for the specified AP node.
         *
         * @param apNodeFdn
         *            the FDN of the AP node
         */
        @Override
        protected void configureSnmp(String apNodeFdn) {
            if (apNodeFdn.contains("FAILED")) {
                throw new ApApplicationException("test")
            }
        }
    }

    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String INVALID_NODE_FDN = "Project=Project1,Node=FAILED"
    private static final String CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME = "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME";
    private static final String IS_SUPPORTED = "isSupported"

    def "snmp configuration for migration node is failed then pre migration is marked as warning"() {
        given: "Node MO details"
        workflowVariables.setNodeType("RadioNode")
        nodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) >> true
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables
        workflowVariables.setMigrationNode(true)
        workflowVariables.isMigrationNodeUsecase() >> true
        NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(workflowVariables.getNodeType(), CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME,
                IS_SUPPORTED) >> true

        when: "invoking snmp configuration task"
        abstractSnmpConfigurationTask.executeTask(execution)

        then: "Validated succssfully"
        workflowVariables.isMigrationTaskWarning() == true
    }
}
