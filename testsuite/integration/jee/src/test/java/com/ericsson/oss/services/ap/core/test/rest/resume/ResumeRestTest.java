/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.rest.resume;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.EnvironmentTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import cucumber.api.java.en.Given;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Cucumber.class)
public class ResumeRestTest extends ServiceCoreTest {

    @Inject
    private EnvironmentTestSteps environmentTestSteps;

    @Inject
    private StubbedServiceSteps stubbedService;

    @Given("^the project and node (.+) (.+) (.+)")
    public void set_project_and_node(final String projectName, final String nodeName, final String nodeType) {
        this.projectName = projectName;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
    }

    @Given("^node pre-exists in the NRM and AP database$")
    public void add_radio_node_to_nrm_and_ap_database() {
        final ManagedObject nodeMo = environmentTestSteps.create_project(projectName, nodeType, 1, nodeName);
        createNetworkElementMo(nodeName, nodeType);
        createCMManagedObject(nodeName);
        setNodeStatus(nodeMo.getFdn(), State.INTEGRATION_COMPLETED.name());
    }

    @Given("^(error|success) workflow stubs set up for node (\\d+) from project (\\d+)$")
    public void workflow_stubbed_message_correlation_exception(final String testType, final int nodeIndex, final int projectIndex) throws WorkflowMessageCorrelationException {
        if (testType.equals("success")){
            stubbedService.createApWorkflowServiceStub(true, "RESUME");
        } else {
            stubbedService.createApWorkflowServiceStub(false, "RESUME");
        }
    }
}
