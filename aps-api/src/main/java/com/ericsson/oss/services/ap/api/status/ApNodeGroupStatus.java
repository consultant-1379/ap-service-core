/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The detailed status of a group of nodes, containing the comprehensive node status of each of the nodes in the collection and a brief statistics
 * summary of the group.
 */
public class ApNodeGroupStatus implements Serializable {

    private static final long serialVersionUID = 6915384715995875229L;

    private final String apNodeGroupName;
    private final List<NodeStatus> nodeStatuses;
    private final Map<IntegrationPhase, Integer> integrationPhaseSummary;
    private final ApNodeGroupType apNodeGroupType;

    /**
     * Constructs an instance of {@link ApNodeGroupStatus}.
     *
     * @param apNodeGroupName
     *            the name of the project
     * @param nodeStatuses
     *            a list of the node status of each node within the project
     * @return the ApNodeGroupStatus instance for a project group
     */
    public static ApNodeGroupStatus getProjectApNodeGroupStatus(final String apNodeGroupName, final List<NodeStatus> nodeStatuses) {
        return new ApNodeGroupStatus(apNodeGroupName, nodeStatuses, ApNodeGroupType.PROJECT);
    }

    /**
     * Constructs an instance of {@link ApNodeGroupStatus}.
     *
     * @param apNodeGroupName
     *            the name of the deployment
     * @param nodeStatuses
     *            a list of the node status of each node within the deployment
     * @return the ApNodeGroupStatus instance for a deployment group
     */
    public static ApNodeGroupStatus getDeploymentApNodeGroupStatus(final String apNodeGroupName, final List<NodeStatus> nodeStatuses) {
        return new ApNodeGroupStatus(apNodeGroupName, nodeStatuses, ApNodeGroupType.DEPLOYMENT);
    }

    private ApNodeGroupStatus(final String apNodeGroupName, final List<NodeStatus> nodeStatuses, final ApNodeGroupType apNodeGroupType) {
        this.apNodeGroupName = apNodeGroupName;
        this.nodeStatuses = new ArrayList<>(nodeStatuses);
        this.integrationPhaseSummary = calculateNumerOfNodesInIntegrationPhases();
        this.apNodeGroupType = apNodeGroupType;
    }

    /**
     * The name of the node group.
     *
     * @return the node group name
     */
    public String getApNodeGroupName() {
        return apNodeGroupName;
    }

    /**
     * The number of nodes within the group.
     *
     * @return the number of nodes
     */
    public int getNumberOfNodes() {
        return nodeStatuses.size();
    }

    /**
     * Returns a list of {@link NodeStatus} objects for each of the nodes within the node group. The nodes will be sorted alphabetically by their
     * names.
     *
     * @return a list of NodeStatuses
     */
    public List<NodeStatus> getNodesStatus() {
        Collections.sort(nodeStatuses, new Comparator<NodeStatus>() { //NOSONAR

            @Override
            public int compare(final NodeStatus nodeStatus1, final NodeStatus nodeStatus2) {
                return nodeStatus1.getNodeName().compareTo(nodeStatus2.getNodeName());
            }
        });
        return Collections.unmodifiableList(nodeStatuses);
    }

    /**
     * Is the node group a project
     * 
     * @return true if a project group
     */
    public boolean isProjectGroup() {
        return apNodeGroupType == ApNodeGroupType.PROJECT;
    }

    /**
     * Is the node group a deployment
     * 
     * @return true if a deployment group
     */
    public boolean isDeploymentGroup() {
        return apNodeGroupType == ApNodeGroupType.DEPLOYMENT;
    }

    /**
     * Returns a summary of the {@link IntegrationPhase} of the nodes within the node group. Will count the nodes in each phase, and return a map of
     * each phase with the corresponding amount of nodes.
     *
     * @return the summary of {@link IntegrationPhase} to number of nodes in that phase.
     */
    public Map<IntegrationPhase, Integer> getIntegrationPhaseSummary() {
        return integrationPhaseSummary;
    }

    private Map<IntegrationPhase, Integer> calculateNumerOfNodesInIntegrationPhases() {
        final Map<IntegrationPhase, Integer> integrationSummary = new EnumMap<>(IntegrationPhase.class);
        for (final IntegrationPhase integrationPhase : IntegrationPhase.valuesAsList()) {
            integrationSummary.put(integrationPhase, 0);
        }

        for (final NodeStatus nodeStatus : nodeStatuses) {
            final IntegrationPhase nodeIntegrationPhase = nodeStatus.getIntegrationPhase();
            integrationSummary.put(nodeIntegrationPhase, integrationSummary.get(nodeIntegrationPhase) + 1);
        }

        return integrationSummary;
    }

    /**
     * Enum to determine the type of node collection DEPLOYMENT PROJECT
     */
    private enum ApNodeGroupType {

        DEPLOYMENT,
        PROJECT
    }
}
