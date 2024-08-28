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

import java.util.List;

import com.ericsson.oss.services.ap.api.model.MoData;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * View Test Steps.
 */
public class ViewTestSteps extends ServiceCoreTestSteps {

    @Step("Viewing all projects")
    public List<MoData> view_projects() {
        return service.viewAllProjects();
    }

    @Step("Viewing Project {1}")
    public MoData view_project(final String projectFdn) {
        final List<MoData> projectList = service.viewProject(projectFdn);
        return projectList.get(0);
    }

    @Step("Viewing node {0}")
    public MoData view_node(final String nodeFdn) {
        return service.viewNode(nodeFdn).get(0);
    }
}
