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
package com.ericsson.oss.services.ap.core.test.delete;

import static com.googlecode.catchexception.CatchException.catchException;

import javax.inject.Inject;

import org.junit.runner.RunWith;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.cucumber.client.Cucumber;
import com.ericsson.oss.services.ap.core.test.ServiceCoreTest;
import com.ericsson.oss.services.ap.core.test.steps.DeleteTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;

import cucumber.api.java.en.When;

/**
 * Test behaviours of the <code>ap delete</code> methods for AutoProvisioningService EJB.
 */
@RunWith(Cucumber.class)
public class DeleteTest extends ServiceCoreTest {

    @Inject
    private DeleteTestSteps deleteTestSteps;

    @Inject
    private StubbedServiceSteps stubbedService;

    @When("^the user requests node (\\d+) from project (\\d+) to be deleted$")
    public void deleteNode(final int nodeIndex, final int projectIndex) {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        final ManagedObject node = environmentStatements.get_precreated_node(projectIndex, nodeIndex);
        catchException(deleteTestSteps).delete_node(node.getFdn());
    }

    @When("^the user requests project (\\d+) to be deleted$")
    public void deleteProject(final int projectIndex) {
        stubbedService.create_ap_workflow_service_stub();
        stubbedService.create_workflow_instance_service_stub();
        final ManagedObject project = environmentStatements.get_precreated_project(projectIndex);
        catchException(deleteTestSteps).delete_project(project.getFdn());
    }
}
