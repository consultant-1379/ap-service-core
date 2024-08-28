/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.restore;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.CmFunctionAttribute;
import com.ericsson.oss.services.ap.common.model.CmSyncStatus;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Determines the current condition of the AP <code>Node</code> i.e. is the node synchronised, is the node waiting for node up.
 * <p>
 * The restore algorithm will use this information to decide on what action to take with the node's associated workflow.
 */
class NodeCondition {

    private static final String CM_FUNCTION_FDN_FORMAT = "NetworkElement=%s,CmFunction=1";

    @Inject
    private DpsOperations dps;

    private StatusEntryManagerLocal statusEntryManagerLocal;

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        statusEntryManagerLocal = new ServiceFinderBean().find(StatusEntryManagerLocal.class);
    }

    /**
     * Determine if the AP <code>Node</code> is synchronised.
     * <p>
     * Checks if the <i>syncStatus</code> of the <code>CmFunction</code> for the AP <code>Node</code> is equal to <b>SYNCHRONIZED</b>.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return true if node is synchronised
     */
    boolean isNodeSynchronized(final String apNodeFdn) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final String cmFunctionFdn = String.format(CM_FUNCTION_FDN_FORMAT, nodeName);
        final ManagedObject cmFunctionMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(cmFunctionFdn);
        final String syncStatus = cmFunctionMo.getAttribute(CmFunctionAttribute.SYNC_STATUS.toString());

        return CmSyncStatus.SYNCHRONIZED.toString().equals(syncStatus);
    }

    /**
     * Determine if AP <code>Node</code> is waiting for a Node Up notification.
     * <p>
     * Checks if the AP <code>NodeStatus</code> <i>statusEntries</i> contains a task with name {@link StatusEntryNames#NODE_UP} which is in progress
     * {@link StatusEntryProgress#WAITING}.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return true if node is waiting for the Node Up notification
     */
    boolean isWaitingForNodeUp(final String apNodeFdn) {
        final List<StatusEntry> statusEntries = getStatusEntriesForNode(apNodeFdn);
        Collections.reverse(statusEntries); // Node Up status entry is usually one of the last entries, so search from the bottom
        for (final StatusEntry statusEntry : statusEntries) {
            final String taskName = statusEntry.getTaskName();
            final String taskProgress = statusEntry.getTaskProgress();

            if (StatusEntryNames.NODE_UP.toString().equals(taskName)) {
                return StatusEntryProgress.WAITING.toString().equals(taskProgress);
            }
        }

        return false;
    }

    private List<StatusEntry> getStatusEntriesForNode(final String apNodeFdn) {
        try {
            return statusEntryManagerLocal.getAllStatusEntries(apNodeFdn);
        } catch (final NodeNotFoundException e) {
            logger.warn("No AP node found for FDN {}", apNodeFdn, e);
            return Collections.<StatusEntry> emptyList();
        }
    }
}
