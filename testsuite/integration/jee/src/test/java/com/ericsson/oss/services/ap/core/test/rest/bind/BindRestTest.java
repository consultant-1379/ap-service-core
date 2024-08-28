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
package com.ericsson.oss.services.ap.core.test.rest.bind;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.BindTestSteps;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

import cucumber.api.java.en.Given;

/**
 * Tests related to import order a project archive through REST.
 */
@RunWith(Cucumber.class)
public class BindRestTest extends ServiceCoreTest {

    @Inject
    private BindTestSteps bindTestSteps;

    @Inject
    private Stubs stubs;

    @Given("^node (\\d+) from project (\\d+) has hardwareSerialNumber '(.+)'$")
    public void set_hardware_serial_number(final int nodeIndex, final int projectIndex, final String hwId) {
        final ManagedObject mo = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        bindTestSteps.update_hardware_serial_number(mo.getFdn(), hwId);
    }

    @Given("^the bind node flow will be stubbed for node (\\d+) from project (\\d+)$")
    public void stub_workflow_and_mock_bind_node_flow(final int nodeIndex, final int projectIndex) throws WorkflowMessageCorrelationException {
        final ManagedObject mo = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        final String nodeFdn = mo.getFdn();
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);

        final WorkflowInstanceServiceLocal workflowService = stubs.injectIntoSystem(WorkflowInstanceServiceLocal.class);
        doAnswer((Answer<Object>) invocation -> {
            bindTestSteps.update_state_in_node_status_mo(nodeFdn, "BIND_COMPLETED");
            return null;
        }).when(workflowService).correlateMessage(eq("BIND"), eq(businessKey), anyMapOf(String.class, Object.class));
    }

}
