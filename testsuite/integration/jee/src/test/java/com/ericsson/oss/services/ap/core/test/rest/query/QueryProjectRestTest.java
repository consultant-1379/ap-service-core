/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.rest.query;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.arquillian.util.Stubs;
import com.ericsson.oss.services.ap.core.test.view.ViewTest;

import cucumber.api.java.en.When;

@RunWith(Cucumber.class)
public class QueryProjectRestTest extends ViewTest {

    @Inject
    private Stubs stubs;

    public QueryProjectRestTest() {
        super("id", "creator", "description", "creationDate", "type", "identifier", "ipAddress");
    }

    @When("^an internal service exception occurs on the server while querying a project")
    public void internal_service_exception_occurs_on_the_server_while_querying_project(){
        final AutoProvisioningService autoProvisioningService = stubs.injectIntoSystem(AutoProvisioningService.class);
        doThrow(ApServiceException.class).when(autoProvisioningService).viewProject(anyString());
    }
}
