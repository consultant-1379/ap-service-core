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
package com.ericsson.oss.services.ap.core.test.rest.delete;

import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.DeleteTestSteps;
import cucumber.api.java.en.Then;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Cucumber.class)
public class DeleteNodeRestTest extends ServiceCoreTest {

    @Inject
    private DeleteTestSteps deleteTestSteps;

    @Then("^node '(.+)' in project '(.+)' is deleted$")
    public void node_will_be_deleted(final String nodeId, final String projectId) throws Throwable {
        final String nodeFdn = new StringBuilder("Project=").append(projectId).append(",").append("Node=").append(nodeId).toString();
        assertThat(deleteTestSteps.does_mo_exist(nodeFdn)).as(nodeFdn + " should not exist").isFalse();
    }

}
