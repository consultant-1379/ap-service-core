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
package com.ericsson.oss.services.ap.core.test.order;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.Files;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.model.ProjectData;
import com.ericsson.oss.services.ap.core.test.steps.OrderTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;
import com.google.common.collect.ImmutableMap;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Tests related to order an project archive functionality.
 */
@RunWith(Cucumber.class)
public class OrderProjectArchiveTest extends ServiceCoreTest {

    private static final String FILE_PREFIX = "import/batch/";
    private static final String FILE_PREFIX_STANDARD = "import/standard/";
    private static final String FILE_PREFIX_RECONFIG = "import/reconfig/";
    private static final String FILE_PREFIX_MIXED = "import/mixed/";
    private String fileName;
    private boolean workflowServiceAvailable = true;
    private String[] nodesNames;
    private File[][] artifacts;

    @Inject
    private Files fileHelper;

    @Inject
    private OrderTestSteps orderTestSteps;

    @Inject
    private StubbedServiceSteps stubbedService;

    @Inject
    private Dps dpsHelper;

    @Given("^the user has a batch file named '(.+)'$")
    public void set_batch_filename(final String fileName) {
        this.fileName = fileName;
    }

    @Given("^workflow service is unavailable")
    public void set_workflow_service_unavalable() {
        workflowServiceAvailable = false;
    }

    @Given("^the project and node (.+) (.+) (.+) pre-exists user has a project zip file named '(.+)'$")
    public void set_project_zip_filename(final String projectName, final String nodeName, final String nodeType, final String fileName) {
        this.projectName = projectName;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.fileName = fileName;
    }

    @Given("^the project (.+) and node (.+) (.+) and remote node (.+) (.+) pre-exists user has a project zip file named '(.+)'$")
    public void setProjectZipFilename(final String projectName, final String nodeName, final String nodeType, final String remoteNodeName,
        final String remoteNodeType, final String fileName) {
        this.projectName = projectName;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.remoteNodeName = remoteNodeName;
        this.remoteNodeType = remoteNodeType;
        this.fileName = fileName;
    }

    @Given("^the user has a reconfiguration file named '(.+)'$")
    public void set_reconfig_filename(final String fileName) {
        this.fileName = fileName;
    }

    @When("^the user orders project archive (\\d+)")
    public void order_project_archive(final int projectIndex) {
        if (workflowServiceAvailable) {
            stubbedService.create_ap_workflow_service_stub();
            stubbedService.create_workflow_instance_service_stub();
            stubbedService.create_workflow_query_service_stub();
        }
        final ProjectData projectData = zipStatements.get_project_data(projectIndex);
        catchException(orderTestSteps).order_project_archive(projectData.getFileName(), projectData.getProject());
    }

    @When("^the user orders project archive no validation (\\d+)")
    public void order_project_archive_no_validation(final int projectIndex) {
        if (workflowServiceAvailable) {
            stubbedService.create_ap_workflow_service_stub();
            stubbedService.create_workflow_instance_service_stub();
            stubbedService.create_workflow_query_service_stub();
        }
        final ProjectData projectData = zipStatements.get_project_data(projectIndex);
        catchException(orderTestSteps).order_project_archive_no_validation(projectData.getFileName(), projectData.getProject());
    }

    @Then("^after the user orders project archive (\\d+)$")
    public void user_orders_another_file(final int projectId) {
        order_project_archive(projectId);
    }

    @When("^the user orders batch file")
    public void order_batch_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders a batch file no validation")
    public void order_batch_file_no_validation() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX + fileName);
        catchException(orderTestSteps).order_project_archive_no_validation(fileName, projectFile);
    }

    @When("^the user orders standard greenfield file")
    public void order_standard_greenfield_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        createBscControllingNode("DummyBscNode");
        createRncControllingNode("DummyRncNode");

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_STANDARD + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders reconfiguration file")
    public void order_reconfig_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);

        createBscControllingNode("DummyBscNode");
        createRncControllingNode("DummyRncNode");

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_RECONFIG + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders reconfiguration invalid file")
    public void order_reconfig_invalid_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_RECONFIG + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders a reconfiguration file no validation")
    public void order_reconfig_file_no_validation() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_RECONFIG + fileName);
        catchException(orderTestSteps).order_project_archive_no_validation(fileName, projectFile);
    }

    @When("^the user orders mixed project file without referenced project existing")
    public void order_mixed_project_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final String nodeFdn = "Project=" + projectName + ",Node=" + nodeName;
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);
        setReconfigurationStatus(nodeFdn);

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_MIXED + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders mixed project file with referenced project existing")
    public void order_mixed_project_file_with_referenced_project_existing() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        createProjectMo(projectName);

        final String nodeFdn = "Project=" + projectName + ",Node=" + nodeName;
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);
        setReconfigurationStatus(nodeFdn);

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_MIXED + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders greenfield file with remote node configuration in Netconf format")
    public void orderGreenfieldFileWithRemoteNodeConfigurationSync() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        createProjectMo(projectName);
        createNetworkElementMo(remoteNodeName, remoteNodeType);
        createCMManagedObject(remoteNodeName);

        createBscControllingNode("DummyBscNode");
        createRncControllingNode("DummyRncNode");

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_STANDARD + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @When("^the user orders greenfield file with the Netconf remote node configuration but the node is not synced")
    public void orderGreenfieldFileWithRemoteNodeConfigurationNotSync() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        createProjectMo(projectName);
        createNetworkElementMo(remoteNodeName, remoteNodeType);
        createCMManagedObjectNotSync(remoteNodeName);

        createBscControllingNode("DummyBscNode");
        createRncControllingNode("DummyRncNode");

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_STANDARD + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
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

    @Then("^the Node artifact MOs are created in (.+) as defined in nodeInfo$")
    public void checkArtifactMOAreCreatedInOrderIntegration(final String configurationFilesOrder) {
        checkArtifactMOAreCreatedInOrder(configurationFilesOrder);
    }

    private void setReconfigurationStatus(final String nodeFdn) {
        dpsHelper.updateMo(nodeFdn + "," + MoType.NODE_STATUS.toString() + "=1", ImmutableMap.<String, Object> builder()
            .put(NodeStatusAttribute.STATE.toString(), State.INTEGRATION_COMPLETED.name())
            .build());
    }

    @Then("^node '(.+)' will exist$")
    public void node_exists(final String fdn) {
        final ManagedObject mo = dpsHelper.findMoByFdn(fdn);
        assertThat(mo).isNotNull();
    }
}
