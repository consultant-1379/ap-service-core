/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api;

import java.util.Set;

/**
 * Interface to be implemented by each node type. This service will specify what
 * artifacts can be uploaded, and from what state. Creating the generated
 * artifact is also handled by this service
 */
public interface UploadArtifactService {
    /**
     * Returns what artifact types are supported
     *
     * @return set String
     *     containing supported artifact types for upload
     */
    Set<String> getSupportedUploadTypes();

    /**
     * Returns a set of valid states for the upload command, from the artifactType given
     *
     * @param artifactType
     *     the artifact type
     * @return set String
     *     containing valid states for upload of artifact type
     */
    Set<String> getValidStatesForUpload(final String artifactType);

    /**
     * Returns true if artifactType is a node artifact
     *
     * @param artifactType
     *     the artifact type
     * @return boolean
     *     true if artifact is required for node boot (i.e. site equipment)
     */
    boolean isNodeArtifactFile(final String artifactType);

    /**
     * Manages the creation of the generated artifact during the upload command
     *
     * @param artifactType
     *     the artifact type
     * @param apNodeFdn
     *     the fdn of the AP node
     */
    void createGeneratedArtifact(final String artifactType, final String apNodeFdn);
}
