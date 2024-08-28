/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.restore;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.cluster.restore.RestoreManager;
import com.ericsson.oss.itpf.sdk.cluster.restore.ServiceRestoreCompletionStatus;
import com.ericsson.oss.itpf.sdk.cluster.restore.ServiceRestoreResponse;
import com.ericsson.oss.itpf.sdk.cluster.restore.ServiceRestoreStatus;
import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.ap.api.restore.RestoreController;

/**
 * Is responsible for coordinating the AP restore process. Checks if a restore is allowed and if there are any suspended workflows relating to AP.
 * Calls the <code>RestoreService</code> to delegate the restore of the suspended workflows.
 */
@Local
@EService
@Startup
@Singleton
public class RestoreControllerEjb implements RestoreController {

    private static final long INITIAL_EXPIRY = MINUTES.toMillis(5L);
    private static final int MAX_RESTORE_DURATION = (int) HOURS.toSeconds(2L);

    private List<WorkflowRestoreResult> cancelledNodeRestores = null;
    private List<WorkflowRestoreResult> resumedNodeRestores = null;

    @Inject
    private Logger logger;

    @Inject
    private RestoreLogger restoreLogger;

    @Inject
    private RestoreManager restoreManager;

    @Inject
    private RetryManager retryManager;

    @Inject
    private RestoreExecutor restoreService;

    @Inject
    private SuspendedWorkflowResolver suspendedWorkflowResolver;

    @Resource
    private TimerService timerService;

    private int retryIntervalInSeconds = (int) MINUTES.toSeconds(5L);
    private int maxRestoreExecutionCount = MAX_RESTORE_DURATION / retryIntervalInSeconds;
    private List<String> suspendedWfInstanceIds;

    @PostConstruct
    public void init() {
        timerService.createSingleActionTimer(INITIAL_EXPIRY, new TimerConfig());
    }

    private boolean isRestoreAllowed() {
        final ServiceRestoreResponse serviceRestoreResponse = restoreManager.tryRestore();
        logger.info("Restore status {}", serviceRestoreResponse.getStatus().name());
        return serviceRestoreResponse.getStatus().equals(ServiceRestoreStatus.ALLOWED);
    }

    @Override
    @Timeout
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void startRestore() {

        if (!isRestoreAllowed()) {
            return;
        }

        suspendedWfInstanceIds = suspendedWorkflowResolver.getSuspendedWorkflows();

        if (suspendedWfInstanceIds.isEmpty()) {
            logger.info("No suspended workflows to restore");
            restoreManager.finishRestoreWith(ServiceRestoreCompletionStatus.SUCCESS);
            restoreLogger.logToCommandLogNoWorkflowsToRestore();
            return;
        }

        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(maxRestoreExecutionCount)
                .waitInterval(retryIntervalInSeconds, TimeUnit.SECONDS)
                .retryOn(IllegalStateException.class)
                .build();

        retryManager.executeCommand(policy, new RetriableCommand<Void>() {

            @Override
            public Void execute(final RetryContext retryContext) {
                final boolean lastRestoreAttempt = maxRestoreExecutionCount == retryContext.getCurrentAttempt();
                executeRestore(lastRestoreAttempt);
                return null;
            }

        });
    }

    private void executeRestore(final boolean lastRestoreAttempt) {
        final List<WorkflowRestoreResult> responses = restoreService.execute(suspendedWfInstanceIds, lastRestoreAttempt);
        processResponses(responses);

        if (isRestoresPending()) {
            if (lastRestoreAttempt) {
                logger.warn("Restore for AutoProvisioning is about to finish, but there are still pending restore(s)");
            } else {
                throw new IllegalStateException("Restore not complete, pending restores exist");
            }
        }

        restoreManager.finishRestoreWith(ServiceRestoreCompletionStatus.SUCCESS);
        restoreLogger.logToCommandLog(resumedNodeRestores, cancelledNodeRestores, suspendedWfInstanceIds);
    }

    private boolean isRestoresPending() {
        return !suspendedWfInstanceIds.isEmpty();
    }

    private void processResponses(final List<WorkflowRestoreResult> restoreResults) {
        for (final WorkflowRestoreResult restoreResult : restoreResults) {
            if (restoreResult.getResult() != RestoreResult.PENDING) {
                if (restoreResult.getResult() == RestoreResult.CANCELLED) {
                    addCancelledRestore(restoreResult);
                } else if (restoreResult.getResult() == RestoreResult.RESUMED) {
                    addResumedRestore(restoreResult);
                }
                suspendedWfInstanceIds.remove(restoreResult.getSuspendedWorkflowInstanceId());
            }
        }
    }

    private void addCancelledRestore(final WorkflowRestoreResult restoreResult) {
        if (cancelledNodeRestores == null) {
            cancelledNodeRestores = new ArrayList<>();
        }
        cancelledNodeRestores.add(restoreResult);
    }

    private void addResumedRestore(final WorkflowRestoreResult restoreResult) {
        if (resumedNodeRestores == null) {
            resumedNodeRestores = new ArrayList<>();
        }
        resumedNodeRestores.add(restoreResult);
    }

    /**
     * Set the max number of attempts for the restore execution.
     *
     * @param attempts
     *              the max retry attempts
     */
    @Override
    public void setMaxRestoreRetryAttempts(final int attempts) {
        maxRestoreExecutionCount = attempts;
    }

    /**
     * Set the interval in seconds between restore execution attempts.
     *
     * @param retryInSeconds
     *              the retry interval
     */
    @Override
    public void setRestoreRetryInterval(final int retryInSeconds) {
        retryIntervalInSeconds = retryInSeconds;
    }
}
