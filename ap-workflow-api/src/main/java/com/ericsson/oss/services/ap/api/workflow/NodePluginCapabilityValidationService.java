/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.workflow;

import com.ericsson.oss.services.ap.api.NodePluginCapabilityType;
import com.ericsson.oss.services.ap.api.NodePluginCapabilityVersion;

public interface NodePluginCapabilityValidationService {
    /**
     * Given the nodeType, version and capability the service validates if the capability is supported by Node Plugin.
     *
     * @param nodeType
     *            the input data to be used to send the request for the specified node type
     * @param version
     *            the input data to be used to identify the version used
     * @param capability
     *            the input data to be used to identify the capability to be validated.
     * @return true if the capability is ready otherwise false.
     */
    boolean validateCapability(final String nodeType, final NodePluginCapabilityVersion version, final NodePluginCapabilityType capability);

}
