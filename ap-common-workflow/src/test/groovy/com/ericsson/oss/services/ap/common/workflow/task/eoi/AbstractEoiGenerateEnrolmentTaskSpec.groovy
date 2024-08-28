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
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractEoiGenerateEnrolmentTaskSpec extends CdiSpecification{
    @Inject
    private TaskExecution execution;
    private AbstractWorkflowVariables workflowVariables ;

    AbstractEoiGenerateEnrollmentInfoTask abstractEoiGenerateEnrollmentInfoTask = new AbstractEoiGenerateEnrollmentInfoTask() {
        @Override
        protected void eoiGenerateEnrollmentInfoTask(String nodeFdn,String baseUrl,String cookieValue) {
            return
        }
    }

    protected static final String NODE_FDN = "Project=Project1,Node=Node1";

    def "when creating node user credentials success"() {
        given: "Node MO details"
        def workflowVariables =new AbstractWorkflowVariables() {

        }
        workflowVariables.setApNodeFdn(NODE_FDN)
        workflowVariables.setBaseUrl("https://")
        workflowVariables.setSessionId("iPlanetDirectoryPro=xyz")
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables



        when: "invoking node credentials task"
        abstractEoiGenerateEnrollmentInfoTask.executeTask(execution)

        then: "Abstract class called successfully"
        return

    }



}
