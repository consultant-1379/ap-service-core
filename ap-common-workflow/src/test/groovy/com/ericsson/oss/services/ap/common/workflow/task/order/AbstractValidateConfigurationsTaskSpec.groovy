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
import com.ericsson.oss.services.ap.api.exception.ValidationException
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractValidateConfigurationsTaskSpec extends CdiSpecification {

    @Inject
    private TaskExecution execution;

    AbstractValidateConfigurationsTask abstractValidateConfigurationsTask = new AbstractValidateConfigurationsTask() {

        /**
         * Validates BulkCM configuration files for the specified AP node.
         *
         * @param apNodeFdn
         *            the FDN of the AP node
         */
        @Override
        protected void validateNodeConfigurations(String apNodeFdn) {
            if (apNodeFdn.contains("FAILED")) {
                throw new ValidationException("test");
            }
        }
    }
    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String INVALID_NODE_FDN = "Project=Project1,Node=FAILED"

    def "when validation for migration node is successful then pre migration is successful"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;
        workflowVariables.setPreMigrationSuccessful(true);

        when: "invoking validateNodeConfigurations"
        abstractValidateConfigurationsTask.executeTask(execution);

        then: "Validated succssfully"
        workflowVariables.isPreMigrationSuccessful() == true;

    }

    def "when validation for migration node is failed then pre migration is marked as false"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        workflowVariables.setMigrationNode(true)
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking validateNodeConfigurations"
        abstractValidateConfigurationsTask.executeTask(execution);

        then: "Pre Migration Validation failed"
        thrown ClassCastException
        workflowVariables.isPreMigrationSuccessful() == false;

    }

    def "when validation for non migration node is failed then order succesful is false"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking validateNodeConfigurations"
        abstractValidateConfigurationsTask.executeTask(execution);

        then: "exception is thrown and order is failed"
        thrown ClassCastException
        workflowVariables.isOrderSuccessful() == false
    }
}
