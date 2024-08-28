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
package com.ericsson.oss.services.ap.core.test.steps;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.util.Dps;
import com.ericsson.oss.services.ap.arquillian.util.Files;
import com.ericsson.oss.services.ap.arquillian.util.ProjectGenerator;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * Delete test steps.
 */
public class DeleteTestSteps extends ServiceCoreTestSteps {

    @Inject
    private ProjectGenerator projectGenerator;

    @Inject
    private Files files;

    @Inject
    private Dps dps;

    @Step("Delete a node")
    public void delete_node(final String nodeFdn) {
        service.deleteNode(nodeFdn, false);
    }

    @Step("Delete a project")
    public void delete_project(final String projectFdn) {
        service.deleteProject(projectFdn, false);
    }

    @Step("Check whether mo for fdn {0} exists")
    public boolean does_mo_exist(final String fdn) {
        final ManagedObject mo = dps.findMoByFdn(fdn);
        return mo != null;
    }
}
