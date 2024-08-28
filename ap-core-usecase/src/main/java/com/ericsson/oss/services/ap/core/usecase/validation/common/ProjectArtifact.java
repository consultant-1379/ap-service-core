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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

/**
 * Artifacts in project archive.
 */
public enum ProjectArtifact {

    NODEINFO("nodeInfo.xml"),
    PROJECTINFO("projectInfo.xml");

    private final String artifactName;

    private ProjectArtifact(final String artifactName) {
        this.artifactName = artifactName;
    }

    public String artifactName() {
        return artifactName;
    }

    @Override
    public String toString() {
        return artifactName;
    }
}
