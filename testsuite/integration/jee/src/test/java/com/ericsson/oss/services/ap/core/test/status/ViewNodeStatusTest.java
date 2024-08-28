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
package com.ericsson.oss.services.ap.core.test.status;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

/**
 * Tests to verify viewing status for single node.
 */
@RunWith(Cucumber.class)
public class ViewNodeStatusTest extends ViewStatusTest {

    private NodeStatus nodeStatus;
    private String[] statusEntries;
    private String[] statusProgress;

    private int nodeIndex, projectIndex;

    @Given("^node (\\d+) from project (\\d+) has the following status entries: (.+)$")
    public void set_status_entries(final int nodeIndex, final int projectIndex, final String statusEntries) {
        this.nodeIndex = nodeIndex;
        this.projectIndex = projectIndex;
        this.statusEntries = statusEntries.replace(" ", "").split(",");
    }

    @Given("^these entries are at these states, respectively: (.+)")
    public void set_status_progress(final String statusProgress) {
        this.statusProgress = statusProgress.replace(" ", "").split(",");

        final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        for (int i = 0; i < statusEntries.length; i++) {
            viewStatusSteps.add_new_status_entry(
                    node.getFdn(),
                    statusEntries[i],
                    this.statusProgress[i],
                    "");
        }
    }

    @When("^the user requests the status of node (\\d+) from project (\\d+)$")
    public void request_node_status(final int nodeIndex, final int projectIndex) {
        final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        nodeStatus = catchException(viewStatusSteps).view_status_for_node(node.getFdn());
    }

    @Then("^the user will see that the node is in state '(.+)'$")
    public void validate_node_state(final String expectedState) {
        assertThat(nodeStatus.getState()).as("Node status").isEqualTo(expectedState);
    }

    @Then("^there won't be any status entries$")
    public void validate_no_status_entries() {
        assertThat(nodeStatus.getStatusEntries()).as("Node's status entries").isEmpty();
    }

    @Then("^there will be (.+) status entries$")
    public void validate_status_entries_count(final String expectedCount) {
        assertThat(nodeStatus.getStatusEntries()).as("Node's status entries").hasSize(Integer.parseInt(expectedCount));
    }

    @Then("^status tasks will be correctly named$")
    public void validate_tasks_names() {
        final List<StatusEntry> actualStatusEntries = nodeStatus.getStatusEntries();
        for (int i = 0; i < statusEntries.length; i++) {
            final StatusEntry actualStatusEntry = actualStatusEntries.get(i);
            assertThat(actualStatusEntry.getTaskName()).as("Task[1] name").isEqualTo(statusEntries[i]);
        }
    }

    @Then("^status tasks will be at the correct state$")
    public void validate_tasks_progress() {
        final List<StatusEntry> actualStatusEntries = nodeStatus.getStatusEntries();
        for (int i = 0; i < statusEntries.length; i++) {
            final StatusEntry actualStatusEntry = actualStatusEntries.get(i);
            assertThat(actualStatusEntry.getTaskProgress()).as("Task[1] state").isEqualTo(statusProgress[i]);
        }
    }
}
