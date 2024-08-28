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
package com.ericsson.oss.services.ap.core.test.order;

import static com.googlecode.catchexception.CatchException.catchException;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.EnvironmentTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.OrderTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Tests related to order an project archive functionality.
 */
@RunWith(Cucumber.class)
public class OrderExpansionTest extends ServiceCoreTest {

    private static final String FILE_PREFIX_EXPAND = "import/expand/";
    private String fileName;

    @Inject
    private OrderTestSteps orderTestSteps;

    @Inject
    private StubbedServiceSteps stubbedService;

    @Inject
    private EnvironmentTestSteps environmentTestSteps;

    @Given("^the project and node (.+) (.+) (.+)")
    public void set_project_and_node(final String projectName, final String nodeName, final String nodeType) {
        this.projectName = projectName;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
    }

    @Given("^the user has an expansion file named '(.+)'$")
    public void set_expand_filename(final String fileName) {
        this.fileName = fileName;
    }

    @Given("^node pre-exists in the NRM and AP database$")
    public void add_radio_node_to_nrm_and_ap_database() {
        final ManagedObject nodeMo = environmentTestSteps.create_project(projectName, nodeType, 1, nodeName);
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);
        setNodeStatus(nodeMo.getFdn(), State.INTEGRATION_COMPLETED.name());

        createBscControllingNode("DummyBscNode");
        createRncControllingNode("DummyRncNode");
    }

    @Given("^node pre-exists in the NRM and AP database in state (.+)")
    public void add_radio_node_to_nrm_and_ap_database_with_state(final String state) {
        final ManagedObject nodeMo = environmentTestSteps.create_project(projectName, nodeType, 1, nodeName);
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);
        setNodeStatus(nodeMo.getFdn(), state);
    }

    @Given("^node pre-exists in the NRM and AP database but not in sync")
    public void add_radio_node_to_nrm_and_ap_database_not_sync() {
        final ManagedObject nodeMo = environmentTestSteps.create_project(projectName, nodeType, 1, nodeName);
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObjectNotSync(nodeName);
        setNodeStatus(nodeMo.getFdn(), State.INTEGRATION_COMPLETED.name());
    }

    @Given("^node pre-exists in the NRM database$")
    public void add_radio_node_to_nrm_database() {
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);

        createBscControllingNode("DummyBscNode");
        createRncControllingNode("DummyRncNode");
    }

    @When("^the user orders expansion file$")
    public void order_expand_file() {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();

        final byte[] projectFile = orderTestSteps.getProjectFile(FILE_PREFIX_EXPAND + fileName);
        catchException(orderTestSteps).order_project_archive(fileName, projectFile);
    }

    @Then("^the Node artifact MOs are created in order (.+) as defined in nodeInfo$")
    public void checkArtifactMOAreCreatedInOrderExpansion(final String configurationFilesOrder) {
        checkArtifactMOAreCreatedInOrder(configurationFilesOrder);
    }
}
