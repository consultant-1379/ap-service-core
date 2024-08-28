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
package com.ericsson.oss.services.ap.core.test.view;

import static com.googlecode.catchexception.CatchException.catchException;

import org.junit.runner.RunWith;

import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;

import cucumber.api.java.en.When;

/**
 * Test behaviour of the view method in the AutoProvisioningService EJB.
 */
@RunWith(Cucumber.class)
public class ViewProjectsTest extends ViewTest {

    public ViewProjectsTest() {
        super("projectName", "creator", "creationDate", "description");
    }

    @When("^the user requests to view all projects$")
    public void view_projects() {
        withCurrentData(environmentStatements.get_precreated_projects());
        withReturnedData(catchException(viewSteps).view_projects());
    }
}
