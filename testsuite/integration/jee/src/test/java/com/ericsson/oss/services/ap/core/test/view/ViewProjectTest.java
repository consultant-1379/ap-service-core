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

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;

import cucumber.api.java.en.When;

/**
 * Test behaviour of the view project method in the AutoProvisioningService EJB.
 */
@RunWith(Cucumber.class)
public class ViewProjectTest extends ViewTest {

    public ViewProjectTest() {
        super("projectName", "creator", "description", "creationDate");
    }

    @When("^the user requests a view of project (\\d+)$")
    public void view_project(final int projectIndex) {
        final ManagedObject currentProject = withCurrentData(environmentStatements.get_precreated_project(projectIndex));
        withReturnedData(catchException(viewSteps).view_project(currentProject.getFdn()));
    }
}