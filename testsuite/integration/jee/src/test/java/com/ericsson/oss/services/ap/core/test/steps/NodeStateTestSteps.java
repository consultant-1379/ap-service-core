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
package com.ericsson.oss.services.ap.core.test.steps;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import javax.ejb.EJB;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Node;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Project;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * Test step to update and retrieve the <i>state</i> attribute of the <code>NodeStatus</code> MO for an AP <code>Node</code>.
 */
public class NodeStateTestSteps {

    class NodePhaseChecker implements Callable<Boolean> {
        private final IntegrationPhase fromPhase;
        private final List<String> projectFdns;

        NodePhaseChecker(final List<String> projectFdns, final IntegrationPhase fromPhase) {
            this.fromPhase = fromPhase;
            this.projectFdns = projectFdns;
        }

        @Override
        public Boolean call() {
            final long toPark = MILLISECONDS.toNanos(500);

            List<String> currentProjects = new ArrayList<>(projectFdns);
            while (!currentProjects.isEmpty()) {
                currentProjects = findProjectsStillInPhase(currentProjects, fromPhase);
                LockSupport.parkNanos(toPark);
            }

            return (currentProjects.isEmpty());
        }
    }

    class NodeStateChecker implements Callable<Boolean> {
        private final List<String> nodeFdns;
        private final String expectedNodeMoState;

        NodeStateChecker(final String nodeFdn, final String expectedNodeMoState) {
            nodeFdns = new ArrayList<>(Arrays.asList(nodeFdn));
            this.expectedNodeMoState = expectedNodeMoState;
        }

        NodeStateChecker(final List<String> nodeFdns, final String expectedNodeMoState) {
            this.nodeFdns = nodeFdns;
            this.expectedNodeMoState = expectedNodeMoState;
        }

        @Override
        public Boolean call() {
            final long toPark = MILLISECONDS.toNanos(200);
            while (!nodeFdns.isEmpty()) {
                for (final Iterator<String> it = nodeFdns.iterator(); it.hasNext();) {
                    final String currentNodeStatusMoState = get_node_state(it.next());
                    if (currentNodeStatusMoState.equals(expectedNodeMoState)) {
                        it.remove();
                    }
                }

                LockSupport.parkNanos(toPark);
            }

            return (nodeFdns.isEmpty());
        }
    }

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

    public static final String READY_FOR_ORDER = "READY_FOR_ORDER";

    private final static String NODE_STATUS_EQUALS_1 = ",NodeStatus=1";
    private final static String STATE_ATTRIBUTE = "state";
    private final static int MAX_WAIT_TIME = 50000;

    @EJB
    protected AutoProvisioningService service;

    @Inject
    private Dps dpsHelper;

    @Step("Update the node status to {1} for node {0}")
    public void update_node_state(final String nodeFdn, final String state) {
        final Map<String, Object> nodeStatusAttributes = new HashMap<>();
        nodeStatusAttributes.put(STATE_ATTRIBUTE, state);
        final String nodeStatusFdn = new StringBuilder(nodeFdn).append(NODE_STATUS_EQUALS_1).toString();
        dpsHelper.updateMo(nodeStatusFdn, nodeStatusAttributes);
    }

    /**
     * Gets the current node state for the given node.
     *
     * @param nodeFdn
     *            the fdn of the node in ap model
     * @return the node state
     */
    public String get_node_state(final String nodeFdn) {
        final ManagedObject nodeMo = dpsHelper.findMoByFdn(nodeFdn + NODE_STATUS_EQUALS_1);
        return nodeMo.getAttribute(STATE_ATTRIBUTE);
    }

    /**
     * Waits until a node is in an expected state.
     *
     * @param nodeFdn
     *            The node FDN
     * @param expectedNodeMoState
     *            The expected state of the node. eg ORDER_STARTED
     * @return true if the node is in the required state, false otherwise
     */
    @Step("Waiting for the node {0} to reach state {1}")
    public boolean wait_until_node_is_in_expected_state(final String nodeFdn, final String expectedNodeMoState) {
        return getCallableResult(new NodeStateChecker(nodeFdn, expectedNodeMoState));
    }

    /**
     * Waits until all nodes in a project have change from the specified phase.
     *
     * @param projectFdns
     *            The project FDNs
     * @param fromPhase
     *            the {@link IntegrationPhase} the projects' nodes are currently in
     * @return true if the nodes changed from specified state, false otherwise
     */
    @Step("Waiting for the projects {0} to change from state {1}")
    public boolean wait_until_all_projects_have_changed_from_phase(final List<String> projectFdns, final IntegrationPhase fromPhase) {
        return getCallableResult(new NodePhaseChecker(projectFdns, fromPhase));
    }

    /**
     * Waits until all nodes in a project are in a specific state.
     *
     * @param project
     *            The project whose nodes are to be analyzed
     * @param expectedNodeMoState
     *            the expected state
     * @param timeout
     *            maximum amount of time to wait for the function to be complete
     * @return true if the nodes changed from specified state, false otherwise
     */
    @Step("Waiting for the node {0} to reach state {1}")
    public boolean wait_until_all_nodes_are_in_expected_state(final Project project, final String expectedNodeMoState, final long timeout) {
        final List<String> nodeFdns = new ArrayList<>();
        for (final Node node : project.getNodes()) {
            nodeFdns.add(String.format("Project=%s,Node=%s", project.getName(), node.getName()));
        }

        return getCallableResult(new NodeStateChecker(nodeFdns, expectedNodeMoState), timeout);
    }

    private boolean getCallableResult(final Callable<Boolean> callable) {
        return getCallableResult(callable, MAX_WAIT_TIME);
    }

    private boolean getCallableResult(final Callable<Boolean> callable, final long timeout) {
        final Future<Boolean> future = EXECUTOR_SERVICE.submit(callable);

        try {
            return future.get(timeout, MILLISECONDS);
        } catch (final TimeoutException | ExecutionException | InterruptedException e) {
            return false;
        }
    }

    private List<String> findProjectsStillInPhase(final List<String> projectFdns, final IntegrationPhase phase) {
        final List<String> projectsStillInPhase = new ArrayList<>(projectFdns);

        final Iterator<String> it = projectsStillInPhase.iterator();
        while (it.hasNext()) {
            final ApNodeGroupStatus projectStatus = service.statusProject(it.next());
            final Map<IntegrationPhase, Integer> phaseSummary = projectStatus.getIntegrationPhaseSummary();
            if ((phaseSummary.containsKey(phase)) && (phaseSummary.get(phase) == 0)) {
                it.remove();
            }
        }

        return projectsStillInPhase;
    }
}
