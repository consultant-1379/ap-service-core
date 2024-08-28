/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.workflow;

/**
 * Interface to provide capability information regarding the node artifacts (SiteBasic and SiteEquipment) validation.
 */
public interface NodeArtifactsValidationCapability {

    /**
     * Check if the node artifacts validation capability is supported for a given node type.
     *
     * @param nodeType
     *            the nodeType (vnf, msrbs_v1, ecim) to check
     * @return true if validation is supported by the node type.
     */
    boolean isSupported(String nodeType);

    /**
     * Check if node artifacts supported by Node Plugin validation are present.
     *
     * @param nodeFdn
     *            The node FDN
     * @return true if the node artifacts exist.
     */
    boolean hasSupportedArtifacts(String nodeFdn);
}
