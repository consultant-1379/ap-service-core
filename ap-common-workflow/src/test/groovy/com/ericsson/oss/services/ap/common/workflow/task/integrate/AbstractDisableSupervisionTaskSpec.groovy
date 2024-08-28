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
package com.ericsson.oss.services.ap.common.workflow.task.integrate

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.common.model.SupervisionMoType
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

public class AbstractDisableSupervisionTaskSpec extends CdiSpecification {

    private static final String NODE_FDN = "Project=" + PROJECT_NAME + "," + "Node=" + NODE_NAME
    private static final String NODE_NAME = "Node1"
    private static final String PROJECT_NAME = "Project1"
    private static final String NODE_FDN_FAILED = "Project=" + PROJECT_NAME + "," + "Node=FAILED"

    AbstractDisableSupervisionTask disableSupervisionTask = new AbstractDisableSupervisionTask() {
        private Map supervisionDisableMap = new HashMap<>();

        @Override
        protected void disableSupervision(String apNodeFdn, List<SupervisionMoType> supervisionToDisable) {

            if (apNodeFdn.contains("FAILED")) {
                throw new NodeNotFoundException("Node NotFound");
            }
            for (SupervisionMoType supervisionMo : supervisionToDisable)
                supervisionDisableMap.put(supervisionMo, true);
        }
    };

    @MockedImplementation
    private TaskExecution taskExecution;

    @Inject
    private StubbedAbstractWorkflowVariables workflowVariables;

    def "When AbstractDisableSupervisionTask is executed for PM disableSupervisionTask method executeTask() is  called"() {
        given: "Execution have set node FDN"
            taskExecution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;
            workflowVariables.setApNodeFdn(NODE_FDN);

        when: "disableSupervisionTask is executed"
            disableSupervisionTask.executeTask(taskExecution);

        then: "supervisionDisableMap is executed in disableSupervisionTask"
            disableSupervisionTask.supervisionDisableMap.get(SupervisionMoType.PM) == true
            disableSupervisionTask.supervisionDisableMap.get(SupervisionMoType.FM) == true
    }

    def "Test exception thrown from workflow"() {
        given: "Execution have set node FDN"
            workflowVariables = new StubbedAbstractWorkflowVariables()
            taskExecution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;
            workflowVariables.setApNodeFdn(NODE_FDN_FAILED);
            workflowVariables.setPreMigrationTaskWarning(false)

        when: "disableSupervisionTask is executed"
            disableSupervisionTask.executeTask(taskExecution);

        then: "Exception will be thrown"
            workflowVariables.isPreMigrationTaskWarning() == true
            workflowVariables.setDisableSupervision(SupervisionMoType.PM ,true);
    }
}
