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
package com.ericsson.oss.services.ap.core.test.rest.delete;

import static org.assertj.core.api.Assertions.assertThat;

import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.DeleteTestSteps;
import cucumber.api.java.en.Then;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Cucumber.class)
public class DeleteProjectRestTest extends ServiceCoreTest {

    @Inject
    private DeleteTestSteps deleteTestSteps;

    @Then("^project '(.+)' is deleted$")
    public void project_will_be_deleted(final String id) throws Throwable {
        final String fdn = "Project="+ id;
        assertThat(deleteTestSteps.does_mo_exist(fdn)).as(fdn + " should not exist").isFalse();
    }

}
