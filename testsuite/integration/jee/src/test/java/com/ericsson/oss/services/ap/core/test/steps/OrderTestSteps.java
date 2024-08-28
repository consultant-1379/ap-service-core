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

import static com.ericsson.oss.services.ap.arquillian.util.data.project.ProjectZipGenerator.generate;

import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.arquillian.util.data.project.model.Project;

import ru.yandex.qatools.allure.annotations.Attachment;
import ru.yandex.qatools.allure.annotations.Step;

/**
 * Includes all the steps needed for the order node test cases.
 */
public class OrderTestSteps extends ServiceCoreTestSteps {

    @Step("Generate a dynamic zip file based on project")
    @Attachment(value = "generated_file.zip", type = "application/zip")
    public byte[] generate_dynamic_zip_file_for_project(final Project project) {
        return generate(project);
    }

    @Step("Retrieving file '{0}' from resources")
    @Attachment(value = "imported_file.zip", type = "application/zip")
    public byte[] getProjectFile(final String zipFileName) {
        return Resources.getResourceAsBytes(zipFileName);
    }

    @Step("Order node {0}")
    public void order_node(final String nodeFdn) {
        service.orderNode(nodeFdn);
    }

    @Step("Order project {0}")
    public void order_project(final String projectFdn) {
        service.orderProject(projectFdn);
    }

    @Step("Order project {0}")
    public void order_project_archive(final String fileName, final Project project) {
        service.orderProject(fileName, generate(project), true);
    }

    @Step("Order project {0}")
    public void order_project_archive(final String fileName, final byte[] project) {
        service.orderProject(fileName, project, true);
    }

    @Step("Order project {0}")
    public void order_project_archive_no_validation(final String fileName, final Project project) {
        service.orderProject(fileName, generate(project), false);
    }

    @Step("Order project {0}")
    public void order_project_archive_no_validation(final String fileName, final byte[] project) {
        service.orderProject(fileName, project, false);
    }
}
