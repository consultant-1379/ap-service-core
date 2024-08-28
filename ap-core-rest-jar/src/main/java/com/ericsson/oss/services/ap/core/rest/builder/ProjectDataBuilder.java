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
package com.ericsson.oss.services.ap.core.rest.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.node.Node;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.core.rest.constant.ApAttributes;
import com.ericsson.oss.services.ap.core.rest.model.IntegrationPhaseSummary;
import com.ericsson.oss.services.ap.core.rest.model.NodeSummary;
import com.ericsson.oss.services.ap.core.rest.model.Project;
import com.ericsson.oss.services.ap.core.rest.model.ProjectData;
import com.ericsson.oss.services.ap.core.rest.model.ProjectNode;
import com.ericsson.oss.services.ap.core.rest.model.ProjectProperty;
import com.ericsson.oss.services.ap.core.rest.model.ProjectStatus;
import com.ericsson.oss.services.ap.core.rest.model.ProjectStatusSummary;

/**
 * Class used to build a project data object. The project data contains a list of Project instances, and each Project instance contains the data
 * related to that AP project.
 */
public class ProjectDataBuilder {

    /**
     * Builds an Array of {@link Project} attributes retrieved from a list of Project MOData.
     *
     * @param projectData List of Project MOs
     * @return ProjectData as a list of {@link Project} which is unmarshalled as JSON.
     */
    public ProjectData buildProjectData(final List<MoData> projectData) {
        final List<Project> projectResponseData = new ArrayList<>();
        for (final MoData projectMo : projectData) {
            projectResponseData.add(buildProject(projectMo));
        }
        return new ProjectData(projectResponseData);
    }

    /**
     * Transfers data from {@link MoData} to {@link Project}
     *
     * @param projectMo ProjectMo containing project data
     * @return {@link Project}
     */
    public Project buildProject(final MoData projectMo) {
        final String projectId = getAttributeFromMo(projectMo, ApAttributes.PROJECT_NAME);
        final String creationDate = getAttributeFromMo(projectMo, ApAttributes.CREATION_DATE);
        final String creator = getAttributeFromMo(projectMo, ApAttributes.CREATOR);
        final String description = getAttributeFromMo(projectMo, ApAttributes.DESCRIPTION);
        final String generatedby = getAttributeFromMo(projectMo, ApAttributes.GENERATED_BY);
        final int projectNodeQuantity = projectMo.getAttributes().containsKey(ApAttributes.NODE_QUANTITY.getAttributeName()) ?
            Integer.parseInt((String) projectMo.getAttribute(ApAttributes.NODE_QUANTITY.getAttributeName())) : 0;
        final String integrationProfile = getAttributeFromMo(projectMo, ApAttributes.INTEGRATION_PROFILE_ID);
        final String expansionProfile = getAttributeFromMo(projectMo, ApAttributes.EXPANSION_PROFILE_ID);
        final List<Node> nodes = (List<Node>) projectMo.getAttribute(ApAttributes.NODES.getAttributeName()); // NOPMD

        Map<String, Object> attributes = new HashMap<>();

        attributes.put(Project.PROJECT_ID,projectId);
        attributes.put(Project.CREATION_DATE,creationDate);
        attributes.put(Project.PROJECT_CREATOR, creator);
        attributes.put(Project.PROJECT_DESC, description);
        attributes.put(Project.GENERATED_BY, generatedby);
        attributes.put(Project.PROJECT_NODE_QUANTITY, projectNodeQuantity);
        attributes.put(Project.INTEGRATION_PROFILE, integrationProfile);
        attributes.put(Project.EXPANSION_PROFILE, expansionProfile);
        attributes.put(Project.NODES_LIST, nodes);

        return new Project(attributes);
    }

    /**
     * Builds a data list of {@link ProjectStatusSummary} attributes retrieved from a list of {@link ApNodeGroupStatus}.
     *
     * @param apNodeGroupStatuses List of all projects status data
     * @return A data list of {@link ProjectStatusSummary} which is unmarshalled as JSON
     */
    public List<ProjectStatusSummary> buildStatusAllProjects(final List<ApNodeGroupStatus> apNodeGroupStatuses) {

        return apNodeGroupStatuses != null ? apNodeGroupStatuses.stream().map(projectStatus -> {
            final String projectId = projectStatus.getApNodeGroupName();
            final int numberOfNodes = projectStatus.getNumberOfNodes();
            final int cancelledPhase = projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.CANCELLED);
            final int failedPhase = projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.FAILED);
            final int inProgressPhase = projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.IN_PROGRESS);
            final int successfulPhase = projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.SUCCESSFUL);
            final int suspendedPhase = projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.SUSPENDED);

            final IntegrationPhaseSummary integrationPhaseSummary = new IntegrationPhaseSummary(cancelledPhase, failedPhase, inProgressPhase, successfulPhase, suspendedPhase);
            return new ProjectStatusSummary(projectId, integrationPhaseSummary, numberOfNodes);

        }).collect(Collectors.toList()) : Collections.emptyList();

    }

    /**
     * Builds a Data Object of {@link ProjectProperty} attributes retrieved from a Project MoData.
     *
     * @param projectData List of Project and its Node MOs
     * @return {@link ProjectProperty} which is unmarshalled as JSON
     */
    public ProjectProperty buildProjectProperties(final List<MoData> projectData) {
        final List<ProjectNode> projectNodeList = new ArrayList<>();
        final MoData projectMoData = getProjectMo(projectData);
        final String projectId = getAttributeFromMo(projectMoData, ApAttributes.PROJECT_NAME);
        final String creationDate = getAttributeFromMo(projectMoData, ApAttributes.CREATION_DATE);
        final String creator = getAttributeFromMo(projectMoData, ApAttributes.CREATOR);
        final String generatedby = getAttributeFromMo(projectMoData, ApAttributes.GENERATED_BY);
        final String description = getAttributeFromMo(projectMoData, ApAttributes.DESCRIPTION);

        for (int i = 1; i < projectData.size(); i++) {
            final MoData nodeMoData = projectData.get(i);
            final String nodeId = (String) nodeMoData.getAttribute(ApAttributes.NODE_ID.getAttributeName());
            final String nodeType = (String) nodeMoData.getAttribute(ApAttributes.NODE_TYPE.getAttributeName());
            final String nodeIdentifier = (String) nodeMoData.getAttribute(ApAttributes.NODE_IDENTIFIER.getAttributeName());
            final String ipAddress = (String) nodeMoData.getAttribute(ApAttributes.IP_ADDRESS.getAttributeName());
            final ProjectNode node = new ProjectNode(nodeId, nodeType, nodeIdentifier, ipAddress);
            projectNodeList.add(node);
        }
        return new ProjectProperty(projectId, description, generatedby, creator, creationDate, projectNodeList);
    }


    /**
     * Build project status from a list of {@link NodeStatus}.
     *
     * @param projectStatusList a list containing {@link NodeStatus}
     * @param projectId         the project ID
     * @return {@link ProjectStatus}
     */
    public ProjectStatus buildProjectStatus(final List<NodeStatus> projectStatusList, final String projectId) {
        final List<NodeSummary> nodeSummaryList = new ArrayList<>();
        for (final NodeStatus projectStatus : projectStatusList) {
            final String id = projectStatus.getNodeName();
            final String status = projectStatus.getIntegrationPhase().getName();
            final String state = State.getState(projectStatus.getState()).getDisplayName();

            final NodeSummary nodeSummary = new NodeSummary(id, status, state);
            nodeSummaryList.add(nodeSummary);
        }
        return new ProjectStatus(projectId, nodeSummaryList);
    }

    private MoData getProjectMo(final List<MoData> projectData) {
        return projectData.get(0);
    }

    private String getAttributeFromMo(final MoData moData, final ApAttributes attribute) {
        return (String) moData.getAttribute(attribute.getAttributeName());
    }
}
