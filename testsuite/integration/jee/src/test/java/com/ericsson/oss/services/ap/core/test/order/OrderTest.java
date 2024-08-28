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
package com.ericsson.oss.services.ap.core.test.order;

import static com.ericsson.oss.services.ap.arquillian.util.data.dps.model.DetachedManagedObject.Builder.newDetachedManagedObject;
import static com.ericsson.oss.services.ap.core.test.data.ImportTestData.NODE_DATA;
import static com.ericsson.oss.services.ap.core.test.data.ImportTestData.NODE_DEFAULT_DATA;
import static com.ericsson.oss.services.ap.core.test.data.ImportTestData.NODE_DEFAULT_DATA_MINIMAL;
import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.Files;
import com.ericsson.oss.services.ap.arquillian.util.data.managedobject.OSSMosGenerator;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeArtifact;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node.NodeConfig;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.model.ProjectData;
import com.ericsson.oss.services.ap.core.test.steps.OrderTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;
import com.google.common.collect.ImmutableMap;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Tests related to the project import functionality.
 */
@RunWith(Cucumber.class)
public class OrderTest extends ServiceCoreTest {

    @Inject
    private Dps dpsHelper;

    @Inject
    private Files fileHelper;

    @Inject
    private OrderTestSteps orderTestSteps;

    @Inject
    private OSSMosGenerator ossMosGenerator;

    @Inject
    private StubbedServiceSteps stubbedService;

    private ProjectData currentData;
    private boolean workflowServiceAvailable = true;
    private String[] nodesNames;
    private File[][] artifacts;

    @Given("^workflow service is unavailable")
    public void set_workflow_service_unavalable() {
        workflowServiceAvailable = false;
    }

    @When("^the user orders project archive (\\d+)")
    public void order_project_archive(final int projectIndex) {
        if (workflowServiceAvailable) {
            stubbedService.create_ap_workflow_service_stub();
            stubbedService.create_workflow_instance_service_stub();
        }
        final ProjectData projectData = zipStatements.get_project_data(projectIndex);
        catchException(orderTestSteps).order_project_archive(projectData.getFileName(), projectData.getProject());
    }

    @When("^the user orders project archive no validation (\\d+)")
    public void order_project_archive_no_validation(final int projectIndex) {
        if (workflowServiceAvailable) {
            stubbedService.create_ap_workflow_service_stub();
            stubbedService.create_workflow_instance_service_stub();
        }
        final ProjectData projectData = zipStatements.get_project_data(projectIndex);
        catchException(orderTestSteps).order_project_archive_no_validation(projectData.getFileName(), projectData.getProject());
    }

    @Given("there already exists a network element of type (.+) with name '(.+)' in the NRM database")
    public void add_network_element_by_name_to_nrm_database(final String nodeType, final String name) {
        final String actualType = "erbs".equalsIgnoreCase(nodeType) ? "ERBS" : nodeType;
        ossMosGenerator.generateNetworkElement(name, actualType, getOssModelIdentity(actualType), "CPP");
    }

    @Given("node identifier (can|cannot) be determined based on (.*)")
    public void get_node_identifier_from_software_query_package_service(final String condition, final String upgradePackage) {
        final String invalidUpgradePackage = "XXX_YYY_ZZZ";
        if ("can".equals(condition)) {
            stubbedService.create_software_query_package(upgradePackage);
        } else if ("cannot".equals(condition)) {
            stubbedService.create_software_query_package(invalidUpgradePackage);
        }
    }

    private String getOssModelIdentity(final String nodeType) {
        if ("erbs".equalsIgnoreCase(nodeType)) {
            return OSSMosGenerator.ERBS_OSS_MODEL_IDENTITY;
        }

        if ("MSRBS_V1".equalsIgnoreCase(nodeType)) {
            return OSSMosGenerator.MSRBS_V1_OSS_MODEL_IDENTITY;
        }

        return OSSMosGenerator.RADIO_NODE_OSS_MODEL_IDENTITY; // RadioNode OMI
    }

    @Given("there already exists a network element of type (.+) with ip (.+) in the NRM database")
    public void add_network_element_by_ip_to_nrm_database(final String nodeType, final String ip) {
        if ("erbs".equalsIgnoreCase(nodeType)) {
            createCppNetworkElement(ip);
        } else {
            createComNetworkElement(ip, nodeType);
        }
    }

    private void createCppNetworkElement(final String ipAddress) {
        final String existingNodeName = "EXISTING_PREVIOUS0001";
        final ManagedObject networkElementMo = newDetachedManagedObject()
            .namespace("OSS_NE_DEF")
            .type("NetworkElement")
            .version("2.0.0")
            .name(existingNodeName)
            .mibRoot(true)
            .parent(null)
            .attributes(
                ImmutableMap.<String, Object> builder()
                    .put("neType", "ERBS")
                    .put("platformType", "CPP")
                    .put("ossModelIdentity", OSSMosGenerator.ERBS_OSS_MODEL_IDENTITY)
                    .build())
            .build();

        dpsHelper.createMo(networkElementMo);

        final ManagedObject mo = dpsHelper.findMoByFdn("NetworkElement=" + existingNodeName);
        final ManagedObject cppConnInfo = newDetachedManagedObject()
            .namespace("CPP_MED")
            .type("CppConnectivityInformation")
            .version(" 1.0.0")
            .name("1")
            .attributes(
                ImmutableMap.<String, Object> builder()
                    .put("CppConnectivityInformationId", "1")
                    .put("ipAddress", ipAddress)
                    .put("port", 80)
                    .build())
            .parent(mo)
            .mibRoot(true)
            .build();

        dpsHelper.createMo(cppConnInfo);
    }

    private void createComNetworkElement(final String ipAddress, final String nodeType) {
        final String existingNodeName = "EXISTING_PREVIOUS0001";
        final ManagedObject networkElementMo = newDetachedManagedObject()
            .namespace("OSS_NE_DEF")
            .type("NetworkElement")
            .version("2.0.0")
            .name(existingNodeName)
            .mibRoot(true)
            .parent(null)
            .attributes(
                ImmutableMap.<String, Object> builder()
                    .put("neType", nodeType)
                    .put("ossModelIdentity", getOssModelIdentity(nodeType))
                    .build())
            .build();

        dpsHelper.createMo(networkElementMo);

        final ManagedObject mo = dpsHelper.findMoByFdn("NetworkElement=" + existingNodeName);
        final ManagedObject cppConnInfo = newDetachedManagedObject()
            .namespace("COM_MED")
            .type("ComConnectivityInformation")
            .version(" 1.1.0")
            .name("1")
            .attributes(
                ImmutableMap.<String, Object> builder()
                    .put("ComConnectivityInformationId", "1")
                    .put("ipAddress", ipAddress)
                    .put("port", 6513)
                    .put("transportProtocol", "TLS")
                    .build())
            .parent(mo)
            .mibRoot(true)
            .build();

        dpsHelper.createMo(cppConnInfo);
    }

    @Then("^the system will create a managed object for each node from project (\\d+)$")
    public void validate_managed_object_for_nodes(final int projectId) {
        for (int i = 0; i < zipStatements.get_project_data(projectId).getNodeCount(); i++) {
            final String nodeName = zipStatements.get_project_data(projectId).getProject().getNode(i).getName();
            final String fdn = String.format("Project=%1$s,Node=%2$s", zipStatements.get_project_data(projectId).getProjectName(), nodeName);
            final ManagedObject mo = dpsHelper.findMoByFdn(fdn);
            assertThat(mo).as("Managed object for Node " + nodeName).isNotNull();
        }
    }

    @Then("^the system will create folders for all nodes from project (\\d+)$")
    public void validate_all_system_folders_exist(final int projectId) {
        for (int i = 1; i <= zipStatements.get_project_data(projectId).getNodeCount(); i++) {
            validate_system_folder_exists(i, projectId);
        }
    }

    @Then("^the system will create a folder for node (\\d+) from project (\\d+)$")
    public void validate_system_folder_exists(final int nodeIndex, final int projectId) {
        currentData = zipStatements.get_project_data(projectId);
        currentData.getRawNodeArtifactsSubdirectories()[nodeIndex - 1] = currentData.getProjectName() + File.separator
            + currentData.getProject().getNode(nodeIndex - 1).getName();

        final File actualFolder = fileHelper.getNodeArtifactFolder(currentData.getRawNodeArtifactsSubdirectories()[nodeIndex - 1], "raw");
        assertThat(actualFolder).as("Node artifacts folder").isNotNull();
        assertThat(actualFolder).as("Node artifacts folder").exists();
    }

    @Then("^the system will create a NodeUserCredentials mo for each node from project (\\d+)$")
    public void validate_NodeUserCredentials_managed_object_for_nodes(final int projectId) {
        for (int i = 0; i < zipStatements.get_project_data(projectId).getNodeCount(); i++) {
            final String nodeName = zipStatements.get_project_data(projectId).getProject().getNode(i).getName();
            final String fdn = String.format("Project=%s,Node=%s,NodeUserCredentials=1",
                zipStatements.get_project_data(projectId).getProjectName(), nodeName);
            final ManagedObject mo = dpsHelper.findMoByFdn(fdn);
            assertThat(mo).as("NodeUserCredentials Managed object for Node " + nodeName).isNotNull();
        }
    }

    @Then("^the system will create a Notification mo for each node from project (\\d+)$")
    public void validate_Notification_managed_object_for_nodes(final int projectId) {
        for (int i = 0; i < zipStatements.get_project_data(projectId).getNodeCount(); i++) {
            final String nodeName = zipStatements.get_project_data(projectId).getProject().getNode(i).getName();
            final String fdn = String.format("Project=%s,Node=%s,Notification=1", zipStatements.get_project_data(projectId).getProjectName(),
                    nodeName);
            final ManagedObject mo = dpsHelper.findMoByFdn(fdn);

            final String nodeType = zipStatements.get_project_data(projectId).getProject().getNode(i).getNodeType();
            if ("erbs".equalsIgnoreCase(nodeType)) {
                assertThat(mo).as("Notification Managed object for Node " + nodeName).isNull();
            } else {
                assertThat(mo).as("Notification Managed object for Node " + nodeName).isNotNull();
            }
        }
    }

    @Then("^this folder will contain all default artifacts$")
    public void validate_folder_contains_default_artifacts() {
        final Map<String, String> defaultData = NODE_DEFAULT_DATA.get(currentData.getNodeType());
        validate_folder_contains_artifacts(defaultData.get("artifacts"), defaultData.get("configurations"));
    }

    @Then("^this folder will contain minimal default artifacts$")
    public void validate_folder_contains_minimal_default_artifacts() {
        final Map<String, String> defaultData = NODE_DEFAULT_DATA_MINIMAL.get(currentData.getNodeType());
        validate_folder_contains_artifacts(defaultData.get("artifacts"), defaultData.get("configurations"));
    }

    @Then("^this folder will contain all (.+) with (.+)$")
    @SuppressWarnings("unchecked")
    public void validate_folder_contains_artifacts(final String artifacts, final String configurations) {
        final List<NodeArtifact> nodeArtifacts = (List<NodeArtifact>) NODE_DATA.get(artifacts);
        final List<NodeConfig> nodeConfigurations = (List<NodeConfig>) NODE_DATA.get(configurations);

        final File[] rawArtifacts = fileHelper.getArtifacts(currentData.getRawNodeArtifactsSubdirectories()[0], "raw");
        assertThat(rawArtifacts).extracting("name").containsOnly(getArtifactNamesFrom(nodeArtifacts, nodeConfigurations));
    }

    @Then("^these created folders will contain all default artifacts$")
    public void validate_all_folders_contain_default_artifacts() {
        final Map<String, String> defaultData = NODE_DEFAULT_DATA.get(currentData.getNodeType());
        validate_all_folders_contains_artifacts(defaultData.get("artifacts"), defaultData.get("configurations"));
    }

    @Then("^these created folders will contain minimal default artifacts$")
    public void validate_all_folders_contain_minimal_default_artifacts() {
        final Map<String, String> defaultData = NODE_DEFAULT_DATA_MINIMAL.get(currentData.getNodeType());
        validate_all_folders_contains_artifacts(defaultData.get("artifacts"), defaultData.get("configurations"));
    }

    @Then("^these folders will contain all (.+) with (.+)$")
    @SuppressWarnings("unchecked")
    public void validate_all_folders_contains_artifacts(final String artifacts, final String configurations) {
        final List<NodeArtifact> nodeArtifacts = (List<NodeArtifact>) NODE_DATA.get(artifacts);
        final List<NodeConfig> nodeConfigurations = (List<NodeConfig>) NODE_DATA.get(configurations);

        for (final String rawNodeArtifactsSubdirectory : currentData.getRawNodeArtifactsSubdirectories()) {
            final File[] rawArtifacts = fileHelper.getArtifacts(rawNodeArtifactsSubdirectory, "raw");
            assertThat(rawArtifacts).extracting("name").containsOnly(getArtifactNamesFrom(nodeArtifacts, nodeConfigurations));
        }
    }

    @Then("^after the user orders project archive (\\d+)$")
    public void user_orders_another_file(final int projectId) {
        order_project_archive(projectId);
    }

    @Then("^all the node folders for (.+) from project (.+) will have been created$")
    public void validate_created_folders(final String nodesNames, final String projectName) {
        this.nodesNames = nodesNames.split(",");
        artifacts = new File[nodesNames.length()][];

        for (int i = 0; i < this.nodesNames.length; i++) {
            artifacts[i] = fileHelper.getArtifacts(projectName + "/" + this.nodesNames[i], "raw");
            assertThat(artifacts[i]).as(this.nodesNames[i] + "' artifacts").isNotNull();
        }
    }

    @Then("^these folders will contain (.+)$")
    public void validate_created_artifacts(final String allArtifacts) {
        final String[] expectedArtifacts = allArtifacts.split(",");
        for (int i = 0; i < nodesNames.length; i++) {
            assertThat(artifacts[i])
                .as(nodesNames[i] + "' artifacts")
                .extracting("name")
                .containsOnly((Object[]) expectedArtifacts);
        }
    }

    @Then("^the system (will|will not) create a managed object for project (\\d+)$")
    public void validate_managed_object_for_project(final String condition, final int projectId) {
        final ManagedObject mo = dpsHelper.findMoByFdn("Project=" + zipStatements.get_project_data(projectId).getProjectName());
        if ("will".equals(condition)) {
            assertThat(mo).as("Managed object for Project " + zipStatements.get_project_data(projectId).getProjectName()).isNotNull();
        } else {
            assertThat(mo).as("Managed object for Project " + zipStatements.get_project_data(projectId).getProjectName()).isNull();
        }
    }

    private Object[] getArtifactNamesFrom(final List<NodeArtifact> artifacts, final List<NodeConfig> configurations) {
        final List<String> result = new ArrayList<>();
        for (final NodeArtifact artifact : artifacts) {
            result.add(artifact.getFileName());
        }

        for (final NodeConfig config : configurations) {
            result.add(config.getFileName());
        }

        return result.toArray(new String[result.size()]);
    }
}
