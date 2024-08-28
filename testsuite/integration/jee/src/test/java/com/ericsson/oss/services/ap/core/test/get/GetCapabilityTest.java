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
package com.ericsson.oss.services.ap.core.test.get;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import static com.jayway.restassured.RestAssured.given;

import cucumber.api.java.en.When;
import org.junit.runner.RunWith;

import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.GetCapabilityTestStep;

/**
 * Tests related to get capabilities.
 */
@RunWith(Cucumber.class)
public class GetCapabilityTest extends ServiceCoreTest {

    @Inject
    private GetCapabilityTestStep getCapabilityTestStep;

    @When("^the user requests supported node list$")
    public void getSupportedNodes() {
        getCapabilityTestStep.createCapabilityServiceStubProfileManagement();

        restStatements.setResponse(given().when()
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .get("/auto-provisioning/v1/capability/profile"));
    }
}
