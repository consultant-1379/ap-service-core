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
package com.ericsson.oss.services.ap.core.test.bind;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;

import java.util.List;

import javax.inject.Inject;

import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.BindTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@RunWith(Cucumber.class)
public class BindTest extends ServiceCoreTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    @Inject
    private BindTestSteps bindTestSteps;

    @Inject
    private Stubs stubs;

    @Inject
    private StubbedServiceSteps stubbedService;

    private BatchBindResult batchResult;
    private String csvContent;

    @Given("^the '(.+)' has a csv file with$")
    public void set_csv_multiline_content(final String userType, final String content) {
        set_csv_content(content);
    }

    @Given("^the user has a csv file with '(.+)'$")
    public void set_csv_content(final String content) {
        csvContent = content + NEW_LINE;
    }

    @When("^the user requests a batch bind$")
    public void request_batch_bind() {
        try {
            stubbedService.createApWorkflowServiceStub(true, "BIND");
            batchResult = bindTestSteps.batchBind("batch_bind.csv", csvContent.getBytes());
        } catch (final Exception e) {
            batchResult = null;
        }
    }

    @When("^the user requests a bind on node (\\d+) from project (\\d+) with hardwareSerialNumber '(.+)'$")
    public void request_bind(final int nodeIndex, final int projectIndex, final String hardwareSerialNumber)
        throws WorkflowMessageCorrelationException {
        stubbedService.createApWorkflowServiceStub(true, "BIND");
        final ManagedObject mo = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        catchException(bindTestSteps).bind(mo.getFdn(), hardwareSerialNumber);
    }

    @Then("^there will be (\\d+) successful bind\\(s\\) and (\\d+) failed bind\\(s\\)$")
    public void validate_batch_bind_results(final int sucessfulBinds, final int failedBinds) {
        assertThat(batchResult.getSuccessfulBinds()).as("sucessful bind count").isEqualTo(sucessfulBinds);
        assertThat(batchResult.getFailedBinds()).as("failed bind count").isEqualTo(failedBinds);
    }

    @Then("^the system list of errors will contain '(.+)'$")
    public void validate_error_message_batch_bind(final String message) {
        boolean found = false;
        final List<String> messages = batchResult.getFailedBindMessages();
        for (int i = 0; (!found) && (i < messages.size()); i++) {
            found = message.equals(messages.get(i));
        }

        assertThat(found).as("List of error contains '" + message + "'").isTrue();
    }

    @Given("^the bind node flow will be stubbed for node (\\d+) from project (\\d+)$")
    public void stub_workflow_and_mock_bind_node_flow(final int nodeIndex, final int projectIndex) throws WorkflowMessageCorrelationException {
        final ManagedObject mo = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        final String nodeFdn = mo.getFdn();
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);

        final WorkflowInstanceServiceLocal workflowService = stubs.injectIntoSystem(WorkflowInstanceServiceLocal.class);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(final InvocationOnMock invocation) {
                bindTestSteps.update_state_in_node_status_mo(nodeFdn, "BIND_COMPLETED");
                return null;
            }
        }).when(workflowService).correlateMessage(eq("BIND"), eq(businessKey), anyMapOf(String.class, Object.class));
    }

    @Given("^node (\\d+) from project (\\d+) has hardwareSerialNumber '(.+)'$")
    public void set_hardware_serial_number(final int nodeIndex, final int projectIndex, final String hwId) {
        final ManagedObject mo = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        bindTestSteps.update_hardware_serial_number(mo.getFdn(), hwId);
    }
}
