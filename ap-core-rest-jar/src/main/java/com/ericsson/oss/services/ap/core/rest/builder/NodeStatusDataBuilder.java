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
package com.ericsson.oss.services.ap.core.rest.builder;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.core.rest.model.NodeStatusData;
import com.ericsson.oss.services.ap.core.rest.model.StatusEntryData;
/**
 *
 * Class used to build a node status data object.
 */
public class NodeStatusDataBuilder {

    /**
     * Builds {@link NodeStatusData} retrieved from {@link NodeStatus}.
     *
     * @param nodeStatus
     *                   status of node data
     * @return NodeStatusData
     *                   data of Node status which is unmarshalled as JSON.
     */
    public NodeStatusData buildNodeStatusData(final NodeStatus nodeStatus) {

        final List<StatusEntryData> statusEntriesData = new ArrayList<>();
        final List<StatusEntry> statusEntries = nodeStatus.getStatusEntries();

        for (final StatusEntry statusEntry : statusEntries) {

            final StatusEntryData statusEntryData = new StatusEntryData(statusEntry.getTaskName(),
                statusEntry.getTaskProgress(),
                statusEntry.getTimeStamp(),
                statusEntry.getAdditionalInfo());

            statusEntriesData.add(statusEntryData);
        }

        return new NodeStatusData(nodeStatus.getNodeName(),
            nodeStatus.getProjectName(), State.getState(nodeStatus.getState()).getDisplayName(), statusEntriesData);
    }
}
