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
package com.ericsson.oss.services.ap.api.download;

/**
 * Interface to be implemented by each node type. This service will specify if download is supported for the node type.
 */
public interface DownloadArtifactService {

    /**
     * Returns true if download of ordered artifact is supported.
     * 
     * @return true if possible to download ordered artifact
     */
    boolean isOrderedArtifactSupported();

}
