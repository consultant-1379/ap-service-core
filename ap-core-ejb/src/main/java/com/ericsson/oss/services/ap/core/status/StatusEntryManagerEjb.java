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
package com.ericsson.oss.services.ap.core.status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.transaction.RollbackException;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerNonCDIImpl;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;

/**
 * EJB managing the update of the NodeStatus MO status entries, for the <code>ap status -n</code> usecase. Each method is executed in its own
 * transaction
 * <p>
 * In the event we are unable to create/update a status entry, we will only catch and log the exception.
 */
@Stateless
public class StatusEntryManagerEjb implements StatusEntryManagerLocal {

    @Inject
    private DpsOperations dps;

    @Inject
    private NodeStatusMoUpdater nodeStatusMoUpdater;

    @Inject
    private StatusEntryFormatter statusEntryFormatter;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void taskStarted(final String apNodeFdn, final String statusEntryName) {
        updateStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.STARTED, "");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void taskStarted(final String apNodeFdn, final String statusEntryName, final String additionalInfo) {
        updateStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.STARTED, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void taskCompleted(final String apNodeFdn, final String taskName) {
        taskCompleted(apNodeFdn, taskName, "");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void taskCompleted(final String apNodeFdn, final String statusEntryName, final String additionalInfo) {
        updateStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.COMPLETED, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void waitingForNotification(final String apNodeFdn, final String statusEntryName) {
        updateExistingStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.WAITING, "");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void waitingForNotification(final String apNodeFdn, final String statusEntryName, final String additionalInfo) {
        updateExistingStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.WAITING, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void notificationReceived(final String apNodeFdn, final String statusEntryName) {
        notificationReceived(apNodeFdn, statusEntryName, "");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void notificationReceived(final String apNodeFdn, final String statusEntryName, final String additionalInfo) {
        updateExistingStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.RECEIVED, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void notificationCancelled(final String apNodeFdn, final String statusEntryName) {
        notificationCancelled(apNodeFdn, statusEntryName, "");
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void notificationCancelled(final String apNodeFdn, final String statusEntryName, final String additionalInfo) {
        updateExistingStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.CANCELLED, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void notificationFailed(final String nodeFdn, final String eventName, final String additionalInfo) {
        updateStatusEntries(nodeFdn, eventName, StatusEntryProgress.FAILED, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void taskFailed(final String apNodeFdn, final String statusEntryName, final String additionalInfo) {
        updateStatusEntries(apNodeFdn, statusEntryName, StatusEntryProgress.FAILED, additionalInfo);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void clearStatusEntries(final String apNodeFdn) {
        final RetriableCommand<Void> clearEntriesCommand = retryContext -> {
            nodeStatusMoUpdater.clearAllEntries(apNodeFdn);
            return null;
        };

        executeRetriableCommand(clearEntriesCommand);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void updateAdditionalInformation(final String nodeFdn, final String taskName, final String additionalInformation) {
        final RetriableCommand<Void> updateAdditionalInformationCommand = retryContext -> {
            nodeStatusMoUpdater.updateAdditionalInformation(nodeFdn, taskName, additionalInformation);
            return null;
        };

        executeRetriableCommand(updateAdditionalInformationCommand);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public StatusEntry getStatusEntryByName(final String apNodeFdn, final String taskName) {
        final List<StatusEntry> statusEntries = getAllStatusEntries(apNodeFdn);
        for (final StatusEntry statusEntry : statusEntries) {
            if (statusEntry.getTaskName().equals(taskName)) {
                return statusEntry;
            }
        }
        return null;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public StatusEntry getStatusEntryByNameInNewTx(final String apNodeFdn, final String taskName) {
        final List<StatusEntry> statusEntries = getAllStatusEntries(apNodeFdn);
        for (final StatusEntry statusEntry : statusEntries) {
            if (statusEntry.getTaskName().equals(taskName)) {
                return statusEntry;
            }
        }
        return null;
    }

    @Override
    public void updateEndStateTask(final String nodeFdn, final String taskName, final StatusEntryProgress statusEntryProgress,
            final String newAdditionalInfo) {
        updateExistingStatusEntries(nodeFdn, taskName, statusEntryProgress, newAdditionalInfo);
    }

    @Override
    public void updateStartedStateTask(final String nodeFdn, final String taskName, final StatusEntryProgress statusEntryProgress,
            final String newAdditionalInfo) {
        updateStartedStatusEntries(nodeFdn, taskName, statusEntryProgress, newAdditionalInfo);
    }

    private void updateStartedStatusEntries(final String apNodeFdn, final String statusEntryName, final StatusEntryProgress statusEntryProgress,
            final String additionalInfo) {
        final RetriableCommand<Void> addOrUpdateEntryCommand = retryContext -> {
            nodeStatusMoUpdater.updateExistingStartedEntry(apNodeFdn, statusEntryName, statusEntryProgress, additionalInfo);
            return null;
        };

        executeRetriableCommand(addOrUpdateEntryCommand);
    }

    private void updateExistingStatusEntries(final String apNodeFdn, final String statusEntryName, final StatusEntryProgress statusEntryProgress,
            final String additionalInfo) {
        final RetriableCommand<Void> addOrUpdateEntryCommand = retryContext -> {
            nodeStatusMoUpdater.updateExistingEntry(apNodeFdn, statusEntryName, statusEntryProgress, additionalInfo);
            return null;
        };

        executeRetriableCommand(addOrUpdateEntryCommand);
    }

    private void updateStatusEntries(final String apNodeFdn, final String statusEntryName, final StatusEntryProgress statusEntryProgress,
            final String additionalInfo) {
        final RetriableCommand<Void> addOrUpdateEntryCommand = retryContext -> {
            nodeStatusMoUpdater.addOrUpdateEntry(apNodeFdn, statusEntryName, statusEntryProgress, additionalInfo);
            return null;
        };

        executeRetriableCommand(addOrUpdateEntryCommand);
    }

    private static <T> void executeRetriableCommand(final RetriableCommand<T> retriableCommand) {
        // Retry to cater for OptimisticLockException, using RollbackException since OptimisticLockException is not propagated in exception
        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(3)
                .waitInterval(500, TimeUnit.MILLISECONDS)
                .retryOn(RollbackException.class)
                .build();

        final RetryManager retryManager = new RetryManagerNonCDIImpl();
        retryManager.executeCommand(policy, retriableCommand);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<StatusEntry> getAllStatusEntries(final String apNodeFdn) {
        final String nodeStatusMoFdn = getNodeStatusMoFdn(apNodeFdn);
        final ManagedObject nodeStatusMo = readNodeStatusMo(nodeStatusMoFdn);

        final List<String> jsonStatusEntries = nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString());
        final List<StatusEntry> nodeStatusEntries = new ArrayList<>(jsonStatusEntries.size());

        for (final String statusEntryJson : jsonStatusEntries) {
            final StatusEntry statusEntry = statusEntryFormatter.fromJsonString(statusEntryJson);
            nodeStatusEntries.add(statusEntry);
        }

        return nodeStatusEntries;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<StatusEntry> getAllStatusEntriesInNewTx(final String apNodeFdn) {
        final String nodeStatusMoFdn = getNodeStatusMoFdn(apNodeFdn);
        final ManagedObject nodeStatusMo = readNodeStatusMo(nodeStatusMoFdn);

        final List<String> jsonStatusEntries = nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString());
        final List<StatusEntry> nodeStatusEntries = new ArrayList<>(jsonStatusEntries.size());

        for (final String statusEntryJson : jsonStatusEntries) {
            final StatusEntry statusEntry = statusEntryFormatter.fromJsonString(statusEntryJson);
            nodeStatusEntries.add(statusEntry);
        }

        return nodeStatusEntries;
    }

    private static String getNodeStatusMoFdn(final String apNodeFdn) {
        return apNodeFdn + "," + MoType.NODE_STATUS.toString() + "=1";
    }

    private ManagedObject readNodeStatusMo(final String nodeStatusMoFdn) {
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeStatusMoFdn);
        if (nodeStatusMo == null) {
            throw new NodeNotFoundException(nodeStatusMoFdn);
        }
        return nodeStatusMo;
    }

    @Override
    public void printNodeState(final String nodeFdn, final State newState) {
        updateStatusEntries(nodeFdn, newState.getDisplayName(), StatusEntryProgress.EMPTY, "");
    }
}
