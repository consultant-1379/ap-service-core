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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * View status for all AP projects.
 */
@UseCase(name = UseCaseName.STATUS_ALL_PROJECTS)
public class StatusAllProjectsUseCase {

    private static final String FDN_ATTRIBUTE = "fdn";
    private static final int NODE_STATE_FDN_INDEX = 0;
    private static final int NODE_STATE_STATUS_INDEX = 1;

    @Inject
    private DpsQueries dpsQueries;

    /**
     * Gets summary status details for each project in AP model.
     *
     * @return a {@link List} of {@link ApNodeGroupStatus} containing a {@link NodeStatus} for each node in the project
     */
    public List<ApNodeGroupStatus> execute() {
        try {
            return readStatusOfAllProjects();
        } catch (final Exception e) {
            throw new ApApplicationException("Error reading MOs for projects", e);
        }
    }

    private List<ApNodeGroupStatus> readStatusOfAllProjects() {
        final Map<String, List<NodeStatus>> projectMos = readAllProjectMos();
        final List<Object[]> nodeStatuses = readRawStatusesForAllNodes();

        final Map<String, List<NodeStatus>> projectStatusesWithNodeStatuses = groupNodeStatusesByProjectName(projectMos, nodeStatuses);
        final List<ApNodeGroupStatus> projectStatuses = createProjectStatuses(projectStatusesWithNodeStatuses);
        Collections.sort(projectStatuses, getProjectStatusComparator());

        return projectStatuses;
    }

    private Map<String, List<NodeStatus>> readAllProjectMos() {
        final Iterator<ManagedObject> allProjectMos = dpsQueries.findMosByType(MoType.PROJECT.toString(), AP.toString()).execute();
        final Map<String, List<NodeStatus>> projectWithStatuses = new HashMap<>();

        while (allProjectMos.hasNext()) {
            final ManagedObject projectMo = allProjectMos.next();
            final String projectName = projectMo.getName();
            projectWithStatuses.put(projectName, Collections.<NodeStatus> emptyList());
        }

        return projectWithStatuses;
    }

    private List<Object[]> readRawStatusesForAllNodes() {
        return dpsQueries
                .getMoAttributeValues(Namespace.AP.toString(), MoType.NODE_STATUS.toString(), FDN_ATTRIBUTE, NodeStatusAttribute.STATE.toString())
                .execute();
    }

    private static Map<String, List<NodeStatus>> groupNodeStatusesByProjectName(final Map<String, List<NodeStatus>> projectStatuses,
            final List<Object[]> nodeStatusAttrRawValues) {

        for (final Object[] rawNodeStatus : nodeStatusAttrRawValues) {
            final String nodeStatusFdnValue = (String) rawNodeStatus[NODE_STATE_FDN_INDEX];
            final FDN nodeStatusFdn = new FDN(nodeStatusFdnValue);
            final String nodeParentProjectName = new FDN(nodeStatusFdn.getRoot()).getRdnValue();

            final NodeStatus newNodeStatus = createNodeStatus(nodeParentProjectName, rawNodeStatus, nodeStatusFdn);

            final List<NodeStatus> currentNodeStatuses = projectStatuses.get(nodeParentProjectName);
            final List<NodeStatus> newNodeStatuses = new ArrayList<>();
            newNodeStatuses.addAll(currentNodeStatuses);
            newNodeStatuses.add(newNodeStatus);

            projectStatuses.put(nodeParentProjectName, newNodeStatuses);
        }
        return projectStatuses;
    }

    private static NodeStatus createNodeStatus(final String projectName, final Object[] rawNodeStatus, final FDN nodeStatusFdn) {
        final String nodeName = new FDN(nodeStatusFdn.getParent()).getRdnValue();
        final String nodeStateValue = (String) rawNodeStatus[NODE_STATE_STATUS_INDEX];
        return new NodeStatus(nodeName, projectName, null, nodeStateValue);
    }

    private static List<ApNodeGroupStatus> createProjectStatuses(final Map<String, List<NodeStatus>> projectWithStatuses) {
        final List<ApNodeGroupStatus> projectStatuses = new ArrayList<>(projectWithStatuses.size());

        for (final Entry<String, List<NodeStatus>> projectEntry : projectWithStatuses.entrySet()) {
            final List<NodeStatus> nodeStatuses = projectEntry.getValue();
            final String projectName = projectEntry.getKey();
            final ApNodeGroupStatus projectApNodeGroupStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(projectName, nodeStatuses);
            projectStatuses.add(projectApNodeGroupStatus);
        }

        return projectStatuses;
    }

    private static Comparator<ApNodeGroupStatus> getProjectStatusComparator() {
        return (s1, s2) -> s1.getApNodeGroupName().compareTo(s2.getApNodeGroupName());
    }
}
