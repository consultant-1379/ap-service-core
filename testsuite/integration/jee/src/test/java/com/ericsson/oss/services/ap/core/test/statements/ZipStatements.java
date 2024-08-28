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
package com.ericsson.oss.services.ap.core.test.statements;

import static com.ericsson.oss.services.ap.arquillian.util.data.project.model.NodeBuilder.newNodes;
import static com.ericsson.oss.services.ap.arquillian.util.data.project.model.Project.Builder.newProject;
import static com.ericsson.oss.services.ap.core.test.data.ImportTestData.NODE_DATA;
import static com.ericsson.oss.services.ap.core.test.util.TestUtils.toMapList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.AutoIntegrationOptions;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeArtifact;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeConfig;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeUserCredentials;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.Notification;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.SecurityConfiguration;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.NodeBuilder;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Project;
import com.ericsson.oss.services.ap.core.test.model.ProjectData;

import cucumber.api.DataTable;
import cucumber.api.java.en.Given;

@SuppressWarnings("unchecked")
public class ZipStatements extends ServiceCoreTestStatements {

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    private static final List<ProjectData> AP_PROJECTS = new ArrayList<>();
    private static final int MAX_VALUE_THRESHOLD = 256;

    private static int currentIpAddress = 0;
    private static String ipRange = "1.1.";
    private static long hardwareSerialNumber = 1000000L;

    @Given("^the user has (?:a|another) zip file named '(.+)' for a project of type (.+)$")
    public void create_project_builder(final String fileName, final String nodeType) {
        AP_PROJECTS.add(new ProjectData(fileName, nodeType));
    }

    @Given("^project (\\d+) has (\\d+) nodes with default configuration$")
    public void create_default_project(final int projectIndex, final int nodeQuantity) {
        final ProjectData projectData = AP_PROJECTS.get(projectIndex - 1);
        projectData.setProjectName(getDefaultProjectName(projectData.getNodeType()));
        projectData.setNodeCount(nodeQuantity);
        projectData.setNodeBuilder(getDefaultNodeBuilder(nodeQuantity, projectData.getNodeType()));
    }

    @Given("^project (\\d+) does not have a name$")
    public void define_unnamed_project(final int projectId) {
        set_project_name(projectId, null);
    }

    @Given("^project (\\d+) is named '(.+)'$")
    public void set_project_name(final int projectId, final String projectName) {
        AP_PROJECTS.get(projectId - 1).setProjectName(projectName);
    }

    @Given("^the zip file for project (\\d+) is broken$")
    public void break_zip_file(final int projectId) {
        AP_PROJECTS.get(projectId - 1).setBrokenZip(true);
    }

    @Given("^node (\\d+) from project (\\d+) does not have an identifier$")
    public void set_node_without_identifier(final int nodeIndex, final int projectId) {
        AP_PROJECTS.get(projectId - 1).getNodeBuilder().withNodeIdentifier(nodeIndex - 1, null);
    }

    @Given("^project (\\d+) does not contain a file named '(.+)'$")
    public void exclude_file_from_project(final int projectId, final String fileName) {
        AP_PROJECTS.get(projectId - 1).addExcludedFile(fileName);
    }

    @Given("^project (\\d+) has its nodes configured like this:$")
    public void configure_project_nodes(final int projectIndex, final DataTable dt) {
        final List<Map<String, String>> rows = toMapList(dt);

        final ProjectData project = AP_PROJECTS.get(projectIndex - 1);
        project.setNodeCount(rows.size());
        project.setNodeBuilder(newNodes(rows.size(), project.getNodeType()));
        for (int i = 1; i <= rows.size(); i++) {
            processRow(i, project.getNodeBuilder(), rows.get(i - 1));
        }
    }

    @Given("^project (\\d+) has an extra file called '(.+)' in folder '(.+)' with content '(.+)'$")
    public void add_extra_file_with_content(final int projectId, final String fileName, final String folder, final String content) {
        final String actualFolder = folder.endsWith("/") ? folder : folder + "/";
        AP_PROJECTS.get(projectId - 1).addIncludedFile(actualFolder + fileName, content);
    }

    public ProjectData get_project_data(final int projectId) {
        final ProjectData projectData = AP_PROJECTS.get(projectId - 1);
        if (projectData.getProject() == null) {
            final Project.Builder projectBuilder = newProject().withName(projectData.getProjectName());
            addNodes(projectData, projectBuilder);
            addExtraFiles(projectData, projectBuilder);
            removeUnwantedFiles(projectData, projectBuilder);
            projectData.setProject(projectBuilder.build());
        }

        return projectData;
    }

    private void processRow(final int nodeIndex, final NodeBuilder nodeBuilder, final Map<String, String> row) {
        for (final Entry<String, String> entry : row.entrySet()) {
            if ("NodeName".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withName(nodeIndex - 1, entry.getValue());
            } else if ("IpAddress".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withIpAddress(nodeIndex - 1, entry.getValue());
            } else if ("HardwareSerialNumber".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withHardwareSerialNumber(nodeIndex - 1, entry.getValue());
            } else if ("NodeIdentifier".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withNodeIdentifier(nodeIndex - 1, entry.getValue());
            } else if ("NodeArtifacts".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withArtifacts(nodeIndex - 1, (List<NodeArtifact>) NODE_DATA.get(entry.getValue()));
            } else if ("AutoIntegrationOptions".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withAutoIntegrationOptions(nodeIndex - 1, (AutoIntegrationOptions) NODE_DATA.get(entry.getValue()));
            } else if ("NodeUserCredentials".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withNodeUserCredentials(nodeIndex - 1, (NodeUserCredentials) NODE_DATA.get(entry.getValue()));
            } else if ("SecurityConfig".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withSecurityConfiguration(nodeIndex - 1, (SecurityConfiguration) NODE_DATA.get(entry.getValue()));
            } else if ("NodeConfig".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withConfigurations(nodeIndex - 1, (List<NodeConfig>) NODE_DATA.get(entry.getValue()));
            } else if ("Notification".equalsIgnoreCase(entry.getKey())) {
                nodeBuilder.withNotification(nodeIndex - 1, (Notification) NODE_DATA.get(entry.getValue()));
            }
        }
    }

    private NodeBuilder getDefaultNodeBuilder(final int nodeQuantity, final String nodeType) {
        final NodeBuilder nodeBuilder = newNodes(nodeQuantity, nodeType);
        final String internalType = nodeTypeMapper.getInternalRepresentationFor(nodeType.toUpperCase());
        for (int nodeIndex = 1; nodeIndex <= nodeQuantity; nodeIndex++) {
            nodeBuilder.withName(nodeIndex - 1, getDefaultNodeName(nodeIndex, nodeType));
            nodeBuilder.withIpAddress(nodeIndex - 1, getNextAvailableIpAddress());
            nodeBuilder.withHardwareSerialNumber(nodeIndex - 1, getNextHardwareSerialNumber());
            nodeBuilder.withNodeIdentifier(nodeIndex - 1, (String) NODE_DATA.get(internalType + "NodeIdentifier"));
            nodeBuilder.withArtifacts(nodeIndex - 1, (List<NodeArtifact>) NODE_DATA.get(internalType + "Artifacts"));
            nodeBuilder.withAutoIntegrationOptions(nodeIndex - 1, (AutoIntegrationOptions) NODE_DATA.get(internalType + "AutoIntegrationOptions"));
            nodeBuilder.withSecurityConfiguration(nodeIndex - 1, (SecurityConfiguration) NODE_DATA.get(internalType + "SecurityConfig"));
            nodeBuilder.withConfigurations(nodeIndex - 1, (List<NodeConfig>) NODE_DATA.get(internalType + "ConfigurationsMinimal"));
        }

        return nodeBuilder;
    }

    private String getDefaultProjectName(final String nodeType) {
        return nodeType.toUpperCase() + "Project" + System.currentTimeMillis();
    }

    private synchronized String getNextHardwareSerialNumber() {
        return String.format("ABC%d", hardwareSerialNumber++);
    }

    private String getDefaultNodeName(final int nodeId, final String nodeType) {
        return nodeType.toUpperCase() + "Node" + nodeId;
    }

    private String getNextAvailableIpAddress() {
        currentIpAddress++;
        String nextAvailableIPAddress = "";
        if ((currentIpAddress / 256) < MAX_VALUE_THRESHOLD) {
            nextAvailableIPAddress = ipRange + (currentIpAddress / 256) + "." + (currentIpAddress % 256);
        }

        return nextAvailableIPAddress;
    }

    private void removeUnwantedFiles(final ProjectData projectData, final Project.Builder projectBuilder) {
        for (final String excludedFile : projectData.getExcludedFiles()) {
            projectBuilder.withoutFile(excludedFile);
        }
    }

    private void addExtraFiles(final ProjectData projectData, final Project.Builder projectBuilder) {
        for (final Entry<String, String> entry : projectData.getIncludedFiles().entrySet()) {
            projectBuilder.withExtraFile(entry.getKey(), entry.getValue());
        }
    }

    private void addNodes(final ProjectData projectData, final Project.Builder projectBuilder) {
        if (projectData.getNodeBuilder() != null) {
            projectBuilder.withNodes(projectData.getNodeBuilder().build());
        }
    }

    @Override
    public void clear() {
        AP_PROJECTS.clear();
    }
}
