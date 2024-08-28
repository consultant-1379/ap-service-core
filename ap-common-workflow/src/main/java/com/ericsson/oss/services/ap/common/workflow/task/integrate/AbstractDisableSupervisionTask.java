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
package com.ericsson.oss.services.ap.common.workflow.task.integrate;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to disable supervision for specific client.
 */
public abstract class AbstractDisableSupervisionTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private DdpTimer ddpTimer = new DdpTimer(); // NOPMD
    private Logger logger = LoggerFactory.getLogger(getClass()); // NOPMD

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.DISABLE_SUPERVISION.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        logger.info("Executing {} for node {}", this.getClass().getSimpleName(), workflowVariables.getApNodeFdn());
        disableSupervision(workflowVariables, Arrays.asList(SupervisionMoType.values()));
        ddpTimer.end(workflowVariables.getApNodeFdn());
    }

    private void disableSupervision(final AbstractWorkflowVariables workflowVariables,
                                    final List<SupervisionMoType> supervisionMoTypes) {
        try {
            disableSupervision(workflowVariables.getApNodeFdn(), supervisionMoTypes);
        } catch (final Exception e) {
            logger.warn("Error disabling {} supervision for node {}: {}", supervisionMoTypes,
                workflowVariables.getApNodeFdn(), e.getMessage(), e);
            workflowVariables.setPreMigrationTaskWarning(true);
        }
    }

    /**
     * Disable supervision for an AP node.
     *
     * @param apNodeFdn          the FDN of the AP node
     * @param supervisionMoTypes the supervision MO to Disable
     */
    protected abstract void disableSupervision(final String apNodeFdn, final List<SupervisionMoType> supervisionMoTypes);
}

