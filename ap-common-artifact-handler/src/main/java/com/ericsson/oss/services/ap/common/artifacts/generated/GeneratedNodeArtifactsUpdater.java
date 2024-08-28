/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.artifacts.generated;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;

/**
 * Handles updating of generated artifacts for a single artifact type.
 */
public class GeneratedNodeArtifactsUpdater {

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private GeneratedArtifactCreator generatedArtifactCreator;

    /**
     * Updates the artifact in the generated directory - deletes the old file first, then creates the new file.
     *
     * @param artifact
     *            artifact details used as content for the artifact file
     * @param previousFileLocation
     *            the old generated file to be deleted
     */
    public void updateArtifactInGeneratedDirectory(final String previousFileLocation, final ArtifactDetails artifact) {
        artifactResourceOperations.deleteFile(previousFileLocation);
        generatedArtifactCreator.createArtifactInGeneratedDir(artifact);
    }
}
