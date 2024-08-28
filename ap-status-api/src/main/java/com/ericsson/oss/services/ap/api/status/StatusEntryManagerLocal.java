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
package com.ericsson.oss.services.ap.api.status;

import java.util.List;

import javax.ejb.Local;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

/**
 * Provides methods to read and update the <code>NodeStatus</code> MO <i>statusEntries</i> attribute.
 * <p>
 * Can be used to add new status entries, updating existing ones or retrieve entries for a single node.
 */
@Local
@EService
public interface StatusEntryManagerLocal {

    /**
     * Retrieves all status entries from the <code>NodeStatus</code> MO <i>statusEntry</i> attribute for an AP node.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @return a {@link List} of {@link StatusEntry} objects
     */
    List<StatusEntry> getAllStatusEntries(final String nodeFdn);

    /**
     * Retrieves all status entries from the <code>NodeStatus</code> MO <i>statusEntry</i> attribute for an AP node in a new transaction.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @return a {@link List} of {@link StatusEntry} objects
     */
    List<StatusEntry> getAllStatusEntriesInNewTx(final String nodeFdn);

    /**
     * Retrieves a {@link StatusEntry} for a particular task from the <code>NodeStatus</code> MO.
     * <p>
     * Will return null if {@link StatusEntry} is not found.
     *
     * @param nodeFdn
     *            the FDN of the node
     * @param taskName
     *            the name of the task
     * @return the {@link StatusEntry} for the supplied task name
     */
    StatusEntry getStatusEntryByName(final String nodeFdn, final String taskName);

    /**
     * Retrieves a {@link StatusEntry} for a particular task from the <code>NodeStatus</code> MO in a new transaction.
     * <p>
     * Will return null if {@link StatusEntry} is not found.
     *
     * @param nodeFdn
     *            the FDN of the node
     * @param taskName
     *            the name of the task
     * @return the {@link StatusEntry} for the supplied task name
     */
    StatusEntry getStatusEntryByNameInNewTx(final String nodeFdn, final String taskName);

    /**
     * Clears all status entries from the <code>NodeStatus</code> MO <i>statusEntry</i> attribute for an AP node.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     */
    void clearStatusEntries(final String nodeFdn);

    /**
     * Updates the "Additional Information" of an existing status entry.
     * <p>
     * Does not change the "Task Progress".
     *
     * @param nodeFdn
     *            the FDN of the node
     * @param taskName
     *            the name of the task
     * @param newAdditionalInfo
     *            the additional information to update the status entry with
     */
    void updateAdditionalInformation(final String nodeFdn, final String taskName, final String newAdditionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is completed or failed.
     * <p>
     * Sets the "Task Progress" to {@link StatusEntryProgress#COMPLETED} or {@link StatusEntryProgress#FAILED}, and updates the "Additional Information".
     * <p>
     * <b>Note:</b> The taskName passed in does not need to be the same task that calls this method. It is possible for a task to update the status
     * entry of another task, if the use case requires it.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry in the MO to be updated
     * @param statusEntryProgress
     *            the task progress to update
     * @param newAdditionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void updateEndStateTask(final String nodeFdn, final String taskName, final StatusEntryProgress statusEntryProgress,
            final String newAdditionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is started.
     * <p>
     * Keeps the "Task Progress" as {@link StatusEntryProgress#STARTED}, and updates the "Additional Information".
     * <p>
     * <b>Note:</b> The taskName passed in does not need to be the same task that calls this method. It is possible for a task to update the status
     * entry of another task, if the use case requires it.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry in the MO to be updated
     * @param statusEntryProgress
     *            the task progress to update
     * @param newAdditionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void updateStartedStateTask(final String nodeFdn, final String taskName, final StatusEntryProgress statusEntryProgress,
            final String newAdditionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is completed.
     * <p>
     * Sets the "Task Progress" to {@link StatusEntryProgress#COMPLETED}, and has an empty "Additional Information".
     * <p>
     * <b>Note:</b> The taskName passed in does not need to be the same task that calls this method. It is possible for a task to update the status
     * entry of another task, if the use case requires it.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry in the MO to be updated
     */
    void taskCompleted(final String nodeFdn, final String taskName);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is completed.
     * <p>
     * Sets the "Task Progress" to {@link StatusEntryProgress#COMPLETED}, and updates the "Additional Information".
     * <p>
     * <b>Note:</b> The taskName passed in does not need to be the same task that calls this method. It is possible for a task to update the status
     * entry of another task, if the use case requires it.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry in the MO to be updated
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void taskCompleted(final String nodeFdn, final String taskName, final String additionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is completed.
     * <p>
     * Sets the "Task Progress" to {@link StatusEntryProgress#FAILED}, and updates the "Additional Information".
     * <p>
     * <b>Note:</b> The taskName passed in does not need to be the same task that calls this method. It is possible for a task to update the status
     * entry of another task, if the use case requires it.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry in the MO to be updated
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void taskFailed(final String nodeFdn, final String taskName, final String additionalInfo);

    /**
     * Adds a new status entry to the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is started.
     * <p>
     * Sets the "Task Progress" to {@link StatusEntryProgress#STARTED}.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry
     */
    void taskStarted(final String nodeFdn, final String taskName);

    /**
     * Adds a new status entry to the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a service task is started.
     * <p>
     * Sets the "Task Progress" to {@link StatusEntryProgress#STARTED} and updates the additional information.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param taskName
     *            the name of the status entry
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void taskStarted(final String nodeFdn, final String taskName, final String additionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when an event has received a notification.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#CANCELLED}, and has an empty "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry in the MO to be updated
     */
    void notificationCancelled(final String nodeFdn, final String eventName);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when an event has received a notification.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#CANCELLED}, and updates the "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry in the MO to be updated
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void notificationCancelled(final String nodeFdn, final String eventName, final String additionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when an event notification has failed.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#FAILED}, and updates the "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry in the MO to be updated
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void notificationFailed(final String nodeFdn, final String eventName, final String additionalInfo);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when an event has received a notification.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#RECEIVED}, and has an empty "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry in the MO to be updated
     */
    void notificationReceived(final String nodeFdn, final String eventName);

    /**
     * Updates an existing status entry in the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when an event has received a notification.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#RECEIVED}, and updates the "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry in the MO to be updated
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void notificationReceived(final String nodeFdn, final String eventName, final String additionalInfo);

    /**
     * Adds a new status entry to the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a notification event is waiting.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#WAITING}, and has an empty "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry
     */
    void waitingForNotification(final String nodeFdn, final String eventName);

    /**
     * Adds a new status entry to the <code>NodeStatus</code> MO <i>statusEntry</i> attribute when a notification event is waiting.
     * <p>
     * Sets the "Event Progress" to {@link StatusEntryProgress#WAITING}, and updates the "Additional Information".
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param eventName
     *            the name of the status entry
     * @param additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    void waitingForNotification(final String nodeFdn, final String eventName, final String additionalInfo);

    /**
     * Adds a new status entry to the <code>NodeStatus</code> MO <i>statusEntry</i> attribute to highlight the current AP node state.
     * <p>
     * The "Event Progress", "Timestamp" and "Additional Information" will all be blank, so only the node state will be shown for that status entry.
     *
     * @param nodeFdn
     *            the FDN of the node to update
     * @param newState
     *            the state of the node to add
     */
    void printNodeState(final String nodeFdn, final State newState);
}
