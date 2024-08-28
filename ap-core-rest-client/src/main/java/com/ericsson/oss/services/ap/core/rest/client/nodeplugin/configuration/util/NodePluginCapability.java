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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util;

import java.util.Collections;
import java.util.List;

public class NodePluginCapability {
    private String versionOfInterface;
    private List<String> capabilities;
    private String applicationUri;

    /**
     * @return the versionOfInterface
     */
    public String getVersionOfInterface() {
        return versionOfInterface;
    }

    /**
     * @param versionOfInterface
     *            the versionOfInterface to set
     */
    public void setVersionOfInterface(final String versionOfInterface) {
        this.versionOfInterface = versionOfInterface;
    }

    /**
     * @return the capabilities
     */
    public List<String> getCapabilities() {
        return Collections.unmodifiableList(capabilities);
    }

    /**
     * @param capabilities
     *            the capabilities to set
     */
    public void setCapabilities(final List<String> capabilities) {
        this.capabilities = Collections.unmodifiableList(capabilities);
    }

    /**
     * @return the applicationUri
     */
    public String getApplicationUri() {
        return applicationUri;
    }

    /**
     * @param applicationUri
     *            the applicationUri to set
     */
    public void setApplicationUri(final String applicationUri) {
        this.applicationUri = applicationUri;
    }

    @Override
    public String toString() {
        final String capabilitiesString = String.join(",", getCapabilities());
        return "{versionOfInterface =" +
               getVersionOfInterface() +
               ", capabilities =[" +
               capabilitiesString +
               "], applicationUri =" +
               getApplicationUri() +
               "}";
    }

}
