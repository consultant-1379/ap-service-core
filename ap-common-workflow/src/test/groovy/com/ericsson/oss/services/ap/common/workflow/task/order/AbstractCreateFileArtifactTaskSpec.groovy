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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException
import com.ericsson.oss.services.ap.common.workflow.StubbedAbstractWorkflowVariables
import com.ericsson.oss.services.wfs.task.api.TaskExecution

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY

class AbstractCreateFileArtifactTaskSpec extends CdiSpecification {

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @Inject
    private TaskExecution execution;

    AbstractCreateFileArtifactTask abstractCreateFileArtifactTask = new AbstractCreateFileArtifactTask() {

        /**
         * Validates BulkCM configuration files for the specified AP node.
         *
         * @param apNodeFdn
         *            the FDN of the AP node
         */
        protected void createArtifact(String apNodeFdn, final String artifactType) {
            if (apNodeFdn.contains("FAILED") && artifactType == null) {
                throw new ArtifactNotFoundException("Artifact details are not found");
            }
        }
    }
    private StubbedAbstractWorkflowVariables workflowVariables = new StubbedAbstractWorkflowVariables()

    protected static final String NODE_FDN = "Project=Project1,Node=Node1"
    protected static final String INVALID_NODE_FDN = "Project=Project1,Node=FAILED"
    private static final String ARTIFACT_TYPE = "RbsSummary";
    private static final String VALID_TASK_ID = "CreateFileArtifact__type_" + ARTIFACT_TYPE;

    def "when creating artifact is success then pre migration succesful is true"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;
        execution.getTaskId() >> VALID_TASK_ID;

        when: "invoking createArtifact method"
        abstractCreateFileArtifactTask.executeTask(execution);

        then: "creating artifact is success"
        workflowVariables.isPreMigrationSuccessful() == true;
    }

    def "when creating artifact for migration node is failed then pre migration succesful is false"() {
        given: "Node MO details"
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        workflowVariables.setMigrationNode(true);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking createArtifact method"
        abstractCreateFileArtifactTask.executeTask(execution);

        then: "exception is thrown and migration is failed"
        thrown ClassCastException
        workflowVariables.isPreMigrationSuccessful() == false;
    }

    def "when creating artifact for order is failed then order succesful is false"() {
        given: "Order Node MO details"
        workflowVariables.setApNodeFdn(INVALID_NODE_FDN);
        execution.getVariable(WORKFLOW_VARIABLES_KEY) >> workflowVariables;

        when: "invoking createArtifact method"
        abstractCreateFileArtifactTask.executeTask(execution);

        then: "exception is thrown and migration is failed"
        thrown ClassCastException
        workflowVariables.isOrderSuccessful() == false;
    }

}
