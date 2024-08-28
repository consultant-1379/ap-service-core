/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.order;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.TaskIdParser;
import com.ericsson.oss.services.ap.common.workflow.task.common.BpmnErrorKeyHandler;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task for the creation of AP node artifacts.
 * <p>
 * Retrieves the artifact type from the ID of the BPMN task using {@link TaskIdParser#getArtifactType(String)}.
 */
public abstract class AbstractCreateFileArtifactTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.CREATE_FILE_ARTIFACT.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();

        try {
            final String artifactType = TaskIdParser.getArtifactType(execution.getTaskId());
            logger.info("Executing {} for node {} and type {}", getClass().getSimpleName(), apNodeFdn, artifactType);
            createArtifact(apNodeFdn, artifactType);
            ddpTimer.end(apNodeFdn);
        } catch (final Exception e) {
            logger.error("Error executing {} for node {}: {}", getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            final String errorKey = BpmnErrorKeyHandler.handleWorkflowForFailedUseCase(workflowVariables);
            throwBpmnError(errorKey, e.getMessage(), execution);
            ddpTimer.endWithError(apNodeFdn);
        }
    }

    /**
     * Creates the artifact of the given type for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the type of the artifact to be generated
     */
    protected abstract void createArtifact(final String apNodeFdn, final String artifactType);
}
