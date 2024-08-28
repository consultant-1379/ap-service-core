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
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.shm.licenseservice.remoteapi.ImportLicenseRemoteResponse
import com.ericsson.oss.services.shm.licenseservice.remoteapi.exception.ImportLicenseException
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractImportLicenseKeyFileTaskSpec extends CdiSpecification {


    @Inject
    private TaskExecution execution


    AbstractImportLicenseKeyFileTask abstractImportLicenseKeyFileTask = new AbstractImportLicenseKeyFileTask() {

        /**
         * Import License Key File for the specified AP node.
         *
         * @param apNodeFdn
         *            the FDN of the AP node
         * @return ImportLicenseRemoteResponse*             the Import License Remote Response from SHM
         */
        @Override
        protected ImportLicenseRemoteResponse importLicenseKeyFile(String apNodeFdn) {
            if (apNodeFdn.contains("FAILED")) {
                throw new ImportLicenseException("test")
            }
        }
    }
    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String INVALID_NODE_FDN = "Project=Project1,Node=FAILED"


    def "when license import for non migration node is failed then order succesful is false"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        workflowVariables.setMigrationNode(false)
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking import license files"
        abstractImportLicenseKeyFileTask.executeTask(execution);

        then: "exception is thrown and order is failed"
        thrown ClassCastException
        workflowVariables.isOrderSuccessful() == false
    }

    def "when license import for non migration node is failed for non migration node"() {
        given: "Node MO details"
        workflowVariables.setMigrationNode(false)
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking import license files"
        abstractImportLicenseKeyFileTask.executeTask(execution);

        then: "exception is thrown and order is failed"
        thrown ClassCastException
        workflowVariables.isOrderSuccessful() == false
    }

    def "when import license key file executed successfully"() {
        given: "Node MO details"
        workflowVariables.setMigrationNode(true)
        workflowVariables.setApNodeFdn(NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking import license files"
        abstractImportLicenseKeyFileTask.executeTask(execution);

        then: "Executed ImportLicenseKeyFile"
        workflowVariables.isOrderSuccessful() == true
    }

}
