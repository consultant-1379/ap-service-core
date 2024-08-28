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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Project;
import com.ericsson.oss.services.ap.core.test.steps.NodeAttributeTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.NodeStateTestSteps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

public class NodeStatements extends ServiceCoreTestStatements {

    public interface NodeStatementListener {
        void stateChanged(final int projectIndex, final int nodeIndex, final ManagedObject node, final String state);
    }

    @Inject
    private EnvironmentStatements environmentStatements;

    @Inject
    private ZipStatements zipStatements;

    @Inject
    private NodeStateTestSteps nodeStateStep;

    @Inject
    private NodeAttributeTestSteps nodeAttributeStep;

    @Inject
    private Dps dpsHelper;

    private static final List<NodeStatementListener> listeners = new ArrayList<>();

    @Given("^node (\\d+) from project (\\d+) is in state '(.+)'$")
    public void set_node_state(final int nodeIndex, final int projectIndex, final String state) {
        final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        nodeStateTestSteps.update_node_state(node.getFdn(), state);
        fireListeners(projectIndex, nodeIndex, node, state);
    }

    @Given("^node with name '(.+)' from project (\\d+) is in state '(.+)'$")
    public void set_node_state_by_node_name(final String nodeName, final int projectIndex, final String state) {
        final int nodeIndex = environmentStatements.find_index_for_node_name(projectIndex, nodeName);
        if (nodeIndex != -1) {
            final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
            nodeStateTestSteps.update_node_state(node.getFdn(), state);
            fireListeners(projectIndex, nodeIndex, node, state);
        }
    }

    @Given("^node (\\d+) from project (\\d+) will have deployment set to '(.+)'$")
    public void set_node_deployment_attribute(final int nodeIndex, final int projectIndex, final String deployment) {
        final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        nodeAttributeStep.updateNodeDeployment(node.getFdn(), deployment);
    }

    @Given("^node (\\d+) from project (\\d+) will (?:still )be in state '(.+)'$")
    public void validate_node_state(final int nodeIndex, final int projectIndex, final String expectedState) {
        final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        final String currentState = nodeStateTestSteps.get_node_state(node.getFdn());
        assertThat(currentState).as("Node state").isEqualTo(expectedState);
    }

    private void fireListeners(final int projectIndex, final int nodeIndex, final ManagedObject node, final String state) {
        for (final NodeStatementListener listener : listeners) {
            listener.stateChanged(projectIndex, nodeIndex, node, state);
        }
    }

    public void addListener(final NodeStatementListener listener) {
        listeners.add(listener);
    }

    @Then("^the node state for each node in project (\\d+) is '(.+)' \\(wait timeout: (\\d+)ms\\)")
    public void waitForAllNodeInState(final int projectIndex, final String targetNodeState, final long timeout) {
        final Project project = zipStatements.get_project_data(projectIndex).getProject();
        final boolean result = nodeStateStep.wait_until_all_nodes_are_in_expected_state(project, targetNodeState, timeout);
        assertThat(result).as("Check all node in project %s are in state %s ", project.getName(), targetNodeState).isTrue();
    }

    @Then("^the wfsInstanceId for each node in project (\\d+) (is|is not) empty")
    public void checkwfsInstanceId(final int projectIndex, final String condition) {
        for (int i = 0; i < zipStatements.get_project_data(projectIndex).getNodeCount(); i++) {
            final String nodeName = zipStatements.get_project_data(projectIndex).getProject().getNode(i).getName();
            final String fdn = String.format("Project=%1$s,Node=%2$s", zipStatements.get_project_data(projectIndex).getProjectName(), nodeName);
            final ManagedObject mo = dpsHelper.findMoByFdn(fdn);
            final List<String> workflowInstanceIdList = mo.getAttribute("workflowInstanceIdList");
            if ("is".equals(condition)) {
                assertThat(workflowInstanceIdList).isEmpty();
            } else {
                assertThat(workflowInstanceIdList).isNotEmpty();
            }
        }
    }

    @Override
    public void clear() {
        listeners.clear();
    }
}
