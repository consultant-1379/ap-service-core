/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.workflow.test;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;

import javax.inject.Inject;

import com.ericsson.cds.cdi.support.spock.CdiSpecification;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.slf4j.Logger;

/**
 * Base class which contains methods for starting workflows and correlating messages
 *
 */
class WorkflowUnitTestSpec extends CdiSpecification {

    @Inject
    private Logger logger;

    private final static int THREAD_SLEEP_TIME = 100;
    private final static String WORKFLOW_NOT_FOUND_ERROR_MESSAGE = "Workflow with id %s could not be found";
    protected final static String AP_NODE_FDN = "Project=Proj1,Node=Node1";
    protected final static String BUSINESS_KEY = "AP_NODE1";

    @Rule
    protected ProcessEngineRule processEngineRule = new ProcessEngineRule();

    private String getWorkflowProcessDefinition(final String workflowId) {
        List<ProcessDefinition> definitions = processEngineRule.getRepositoryService().createProcessDefinitionQuery().list();
        for (final ProcessDefinition pd : definitions) {
            if (pd.getId().contains(workflowId) || pd.getName().contains(workflowId)) {
                return pd.getId();
            }
        }
        throw new IllegalStateException(String.format(WORKFLOW_NOT_FOUND_ERROR_MESSAGE, workflowId));
    }

    /**
     * Use this method to start a workflow
     * @param workflowId
     *     the ID of the workflow to start
     * @param workflowVariables
     *     variables to be passed, can be null
     * @return
     *    The ProcessInstance of the running workflow
     */
    protected ProcessInstance startWorkflow(final String workflowId, final Map<String, Object> workflowVariables) {
        return runtimeService()
            .startProcessInstanceById(getWorkflowProcessDefinition(workflowId), BUSINESS_KEY, workflowVariables)
    }

    /**
     * Wait for specified task to start, within the given process instance
     * The task does not have to be the last task to have started
     * @param taskId
     *     the ID of the task
     * @param processInstance
     *     the processInstance object
     * @throws Exception
     */
    protected void waitForTask(final String taskId, final ProcessInstance processInstance) throws Exception {
        boolean taskStarted = false;
        int count = 0;
        while (!taskStarted) {
            List<String> activeIds = runtimeService().getActiveActivityIds(processInstance.getId());
            if (activeIds.contains(taskId)) {
                taskStarted = true;
            }
            logger.info("Current task {}", activeIds.toString());
            count++;
            if (count > 150) {
                throw new ApApplicationException(String.format("Failed waiting for task %s. Stuck at task %s", taskId, activeIds.toString()));
            }
            try {
                Thread.sleep(THREAD_SLEEP_TIME);
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Executes running process instance that have spawned in a separate thread due to
     * an asynchronous event.
     */
    protected void executeJob() {
        Job job = processEngineRule.getManagementService().createJobQuery().singleResult();
        processEngineRule.getManagementService().executeJob(job.getId());
    }

    /**
     * Retrieves a child workflow of the running parentflow
     * Error thrown if a child instance does not exist
     * @param parentFlow
     * @return
     */
    protected ProcessInstance getSubFlow(final ProcessInstance parentFlow) {
        List<ProcessInstance> subFlow = processEngineRule.getRuntimeService().createProcessInstanceQuery().superProcessInstanceId(parentFlow.getId()).list();
        return subFlow.get(0);
    }

    /**
     * Sends a correlation message to the running camunda service
     * @param message
     *     the message to be correlated
     */
    protected void correlateMessage(final String message) {
        runtimeService().createMessageCorrelation(message).processInstanceBusinessKey(BUSINESS_KEY).correlate();
    }

    /**
     * Sends a correlation message to the running camunda service
     * @param message
     *     the message to be correlated
     * @param messageVariables
     *     a map of variables added to the execution
     */
    protected void correlateMessage(final String message, final Map<String, Object> messageVariables) {
        runtimeService().correlateMessage(message, BUSINESS_KEY, messageVariables);
    }
}