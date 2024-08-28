package com.ericsson.oss.services.ap.common.workflow.task.eoi

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractEoiRemoveNodeTaskSpec extends CdiSpecification{
    @Inject
    private TaskExecution execution;

    AbstractEoiRemoveNodeTask abstractEoiRemoveNodeTask = new AbstractEoiRemoveNodeTask() {
        @Override
        protected void eoiRemoveNode(String apNodeFdn) {
            if (apNodeFdn.contains("")) {
                throw new ApApplicationException("test");
            }
        }
    }

    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1";

    def "when eoiRemoveNode executed successfully"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables

        when: "invoking remove node task"
        abstractEoiRemoveNodeTask.executeTask(execution)

        then: "Abstract class called successfully"
        thrown ClassCastException

    }

}
