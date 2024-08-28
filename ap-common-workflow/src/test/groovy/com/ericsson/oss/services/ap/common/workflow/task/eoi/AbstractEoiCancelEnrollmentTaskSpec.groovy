/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.eoi

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractEoiCancelEnrollmentTaskSpec extends CdiSpecification{

    @Inject
    private TaskExecution execution;
    AbstractEoiCancelEnrollmentTask abstractEoiCancelEnrollmentTask = new AbstractEoiCancelEnrollmentTask() {
        /**
         * Creates the node user credentials for the specified AP node.
         *
         * @param apNodeFdn the FDN of the AP node
         */
        @Override
        protected void eoiCancelEnrollment(String apNodeFdn,final String baseUrl ,final String cookieValue) {
            return;
        }
    }


    protected static final String NODE_FDN = "Project=Project1,Node=Node1";

    def "when cancel Enrollment task is success"() {
        given: "Node MO details"
        def workflowVariables =new AbstractWorkflowVariables() {

        }
        workflowVariables.setApNodeFdn(NODE_FDN);
        workflowVariables.setBaseUrl("https://")
        workflowVariables.setSessionId("iPlanetDirectoryPro=xyz")
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables

        when: "invoking cancel enrollment task"
        abstractEoiCancelEnrollmentTask.executeTask(execution)

        then: "Abstract class called successfully"
        return


    }

}
