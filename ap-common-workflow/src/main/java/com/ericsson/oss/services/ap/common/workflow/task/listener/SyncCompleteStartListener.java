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
package com.ericsson.oss.services.ap.common.workflow.task.listener;

import static com.ericsson.oss.services.ap.common.model.CmSyncStatus.SYNCHRONIZED;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static com.ericsson.oss.services.ap.common.workflow.ActivityType.HARDWARE_REPLACE_ACTIVITY;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.services.ap.api.exception.CommonServiceRetryException;
import com.ericsson.oss.services.ap.api.status.APNodePoller;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Listener attached to the <b>start</b> event for the
 * <code>Node Synchronization Notification</code> BPMN wait point.
 * <p>
 * Creates a new status entry on the AP node.
 */
public class SyncCompleteStartListener extends AbstractServiceTask {

    private static final int MAX_RETRIES_CHECK = 3;
    private static final int RETRY_INTERVAL_CHECK = 200;

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();
    private StatusEntryManagerLocal statusEntryManager;
    private final DpsOperations dpsOperations = new DpsOperations();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String nodeFdn = workflowVariables.getApNodeFdn();
        final String activity = workflowVariables.getActivity();
        final Boolean pollTimedOut = (Boolean) execution.getVariable("pollTimedOut");
        updateStatus(nodeFdn, activity, pollTimedOut);
        dpsOperations.updateMo(nodeFdn, NodeAttribute.WAITING_FOR_MESSAGE.toString(), SYNCHRONIZED.toString());
    }

    protected void updateStatus(final String nodeFdn, final String activity, final Boolean pollTimedOut) {
        if (pollTimedOut == null) {
            logger.debug("Update status entry and add node {} to poller", nodeFdn);
            if (HARDWARE_REPLACE_ACTIVITY.getActivityName().equals(activity)) {
                try {
                    final RetryManager retryManager = new RetryManagerBean();
                    final RetryPolicy retryPolicy = RetryPolicy.builder()
                            .attempts(MAX_RETRIES_CHECK)
                            .waitInterval(RETRY_INTERVAL_CHECK, TimeUnit.MILLISECONDS)
                            .retryOn(CommonServiceRetryException.class)
                            .build();

                    retryManager.executeCommand(retryPolicy, new RetriableCommand<Void>() {
                        @Override
                        public Void execute(final RetryContext retryContext) throws Exception {
                            final StatusEntry syncNodeNotificationEntry = getStatusEntryManager().getStatusEntryByNameInNewTx(nodeFdn,
                                    StatusEntryNames.SYNC_NODE_NOTIFICATION.toString());

                            if (syncNodeNotificationEntry == null) {
                                logger.info("Sync Node Notification entry for node {} does not exist, start it with waiting status", nodeFdn);
                                getStatusEntryManager().waitingForNotification(nodeFdn, StatusEntryNames.SYNC_NODE_NOTIFICATION.toString());
                                throw (new CommonServiceRetryException(
                                        String.format("Confirm Node Synchronization Notification entry exists for node %s", nodeFdn)));
                            }
                            return null;
                        }
                    });
                } catch (final RetriableCommandException e) {
                    logger.warn("RetriableCommandException {}", e.getMessage());
                }
            } else {
                getStatusEntryManager().waitingForNotification(nodeFdn, StatusEntryNames.SYNC_NODE_NOTIFICATION.toString());
            }
        }
        final APNodePoller poller = serviceFinder.find(APNodePoller.class, "cmsync");
        poller.addNodeToPoller(nodeFdn);
    }

    protected StatusEntryManagerLocal getStatusEntryManager() {
        if (statusEntryManager == null) {
            statusEntryManager = new ServiceFinderBean().find(StatusEntryManagerLocal.class);
        }
        return statusEntryManager;
    }
}
