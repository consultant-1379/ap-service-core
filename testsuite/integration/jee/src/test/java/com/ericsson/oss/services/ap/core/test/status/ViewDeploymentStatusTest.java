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
package com.ericsson.oss.services.ap.core.test.status;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.runner.RunWith;

import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Tests to verify viewing status for single deployment.
 */
@RunWith(Cucumber.class)
public class ViewDeploymentStatusTest extends ViewStatusTest {

    private ApNodeGroupStatus actualStatus;

    @When("^the user requests the status of deployment '(.+)'$")
    public void get_deployment_status(final String deployment) {
        actualStatus = catchException(viewStatusSteps).view_status_for_deployment(deployment);
    }

    @Then("^the status will inform the deployment has (\\d+) nodes$")
    public void validate_node_count(final int expectedCount) {
        assertThat(actualStatus.getNumberOfNodes()).as("Deployments's node quantity").isEqualTo(expectedCount);
    }

    @Then("^the status will inform that node (\\d+) is in state '(.+)'$")
    public void validate_node_state(final int nodeIndex, final String expectedState) {
        final String expectedNodeName = getNodeName(nodeIndex);
        for (final NodeStatus toInspect : actualStatus.getNodesStatus()) {
            if (expectedNodeName.equals(toInspect.getNodeName())) {
                assertThat(toInspect.getState()).isEqualTo(expectedState);
            }
        }
    }

    @Then("^the status summary will say there (?:is|are) (\\d+) (?:node|nodes) in phase '(.+)'$")
    public void validate_status_summary(final int nodeCount, final String phaseName) {
        final Map<IntegrationPhase, Integer> phaseSummary = actualStatus.getIntegrationPhaseSummary();
        final IntegrationPhase phase = IntegrationPhase.valueOf(phaseName);
        assertThat(phaseSummary.get(phase)).as("Number of nodes in phaseName " + phaseName).isEqualTo(nodeCount);
    }
}
