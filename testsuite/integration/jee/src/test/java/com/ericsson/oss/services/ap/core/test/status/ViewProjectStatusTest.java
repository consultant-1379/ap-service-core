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

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;

import cucumber.api.java.en.When;

/**
 * Tests to verify viewing status for single project.
 */
@RunWith(Cucumber.class)
public class ViewProjectStatusTest extends ViewStatusTest {

    @When("^the user requests the status of project (\\d+)$")
    public void get_project_status(final int projectIndex) {
        ManagedObject currentProject = environmentStatements.get_precreated_project(projectIndex);
        catchException(viewStatusSteps).view_status_for_single_project(currentProject.getFdn());
    }
}
