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
package com.ericsson.oss.services.ap.core.test.download;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.common.DownloadCommonTest;
import com.ericsson.oss.services.ap.core.test.steps.DownloadTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.EnvironmentTestSteps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Test behaviours of download method for AutoProvisioningService EJB.
 */
@RunWith(Cucumber.class)
public class DownloadTest extends DownloadCommonTest {

    @Inject
    private DownloadTestSteps downloadTestSteps;

    @Inject
    private EnvironmentTestSteps environmentTestSteps;

    private String fileId;

    private ManagedObject nodeMo;

    @Given("^the user wants to download the schemas and sample files for a node of type (.+)$")
    public void set_node_type_to_download(final String nodeType) {
        this.nodeType = nodeType;
    }

    @Given("^the user wants to download the schemas and sample files for all node types$")
    public void set_node_type_to_download() {
        nodeType = "";
    }

    @Given("^the user wants to download the raw artifacts for the node '(.+)'$")
    public void set_node_fdn_to_download(final String nodeFdn) {
        this.nodeFdn = nodeFdn;
    }

    @Given("^the node has already been precreated$")
    public void add_node_to_system() {
        nodeMo = environmentTestSteps.create_project(projectName, nodeType, 1, nodeName);
    }

    @Given("^the node (has|has not) been ordered$")
    public void order_node(final String condition) {
        if ("has".equals(condition)) {
            environmentTestSteps.order_node_for_project(nodeMo);
        }
    }

    @When("^the user requests the file containing the schemas and samples$")
    public void request_schema_samples_file() {
        fileId = catchException(downloadTestSteps).download_schema_and_samples(nodeType);
    }

    @When("^the user requests the (.+) artifacts for the specified node$")
    public void request_node_artifacts(final String artifactType) {
        stubbedService.create_download_service_stub(nodeType);
        fileId = catchException(downloadTestSteps).download_node_artifact(nodeFdn, ArtifactBaseType.valueOf(artifactType.toUpperCase()));
    }

    @Then("^the downloaded file id (will|will not) contain '(.+)'$")
    public void validate_file_id(final String condition, final String expectedId) {
        if ("will".equals(condition)) {
            assertThat(fileId).as("Downloaded file Id").containsIgnoringCase(expectedId.toLowerCase());
        } else {
            assertThat(fileId.toLowerCase()).as("Downloaded file Id").doesNotContain(expectedId.toLowerCase());
        }
    }
}
