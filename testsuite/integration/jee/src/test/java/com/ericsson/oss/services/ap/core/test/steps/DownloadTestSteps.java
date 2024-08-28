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

import com.ericsson.oss.services.ap.api.ArtifactBaseType;

import ru.yandex.qatools.allure.annotations.Step;

/**
 * Class of download test steps.
 */
public class DownloadTestSteps extends ServiceCoreTestSteps {

    @Step("Download schema and samples for node {0}")
    public String download_schema_and_samples(final String nodeType) {
        return service.downloadSchemaAndSamples(nodeType);
    }

    @Step("Download node artifacts of type {1} for node {0}")
    public String download_node_artifact(final String nodeFdn, final ArtifactBaseType artifactBaseType) {
        return service.downloadNodeArtifact(nodeFdn, artifactBaseType);
    }

}
