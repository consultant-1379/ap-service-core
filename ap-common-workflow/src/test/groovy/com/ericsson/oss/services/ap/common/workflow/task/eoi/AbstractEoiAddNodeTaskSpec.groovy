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
package com.ericsson.oss.services.ap.common.workflow.task.eoi;

import com.ericsson.cds.cdi.support.spock.CdiSpecification;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

import javax.inject.Inject;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;


class AbstractEoiAddNodeTaskSpec extends CdiSpecification {

    @Inject
    private TaskExecution execution;
    AbstractEoiAddNodeTask abstractEoiAddNodeTask = new AbstractEoiAddNodeTask() {
        /**
         * Creates the node user credentials for the specified AP node.
         *
         * @param apNodeFdn the FDN of the AP node
         */
        @Override
        protected void eoiAddNode(String apNodeFdn, String nodeType) {
            if (apNodeFdn.contains("")) {
                throw new ApApplicationException("test");
            }
        }
    }

        private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

        protected static final String NODE_FDN = "Project=Project1,Node=Node1";
        protected static final String NODE_TYPE = "SharedCNF";

    def "when creating AddNode success"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(NODE_FDN);
        workflowVariables.setNodeType(NODE_TYPE)
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables

        when: "invoking node credentials task"
        abstractEoiAddNodeTask.executeTask(execution)

        then: "Abstract class called successfully"
        thrown ClassCastException

    }

}

