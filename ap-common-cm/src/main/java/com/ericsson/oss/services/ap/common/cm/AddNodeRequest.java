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
package com.ericsson.oss.services.ap.common.cm;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Request to add a node which will result in the creation of <code>NetworkElement</code> and <code>AbstractConnectivitiyInformation</code> MOs.
 */
public final class AddNodeRequest {

    private final String nodeFdn;
    private final String nodeName;
    private final String connInfoNamespace;
    private final String connInfoModelName;
    private final Map<String, Object> networkElementAttributes;
    private final Map<String, Object> connInfoAttributes;

    private AddNodeRequest(final String nodeFdn, final String connInfoNamespace, final String connInfoModelName,
            final Map<String, Object> networkElementAttributes, final Map<String, Object> connInfoAttributes) {
        this.nodeFdn = nodeFdn;
        nodeName = FDN.get(nodeFdn).getRdnValue();
        this.connInfoNamespace = connInfoNamespace;
        this.connInfoModelName = connInfoModelName;
        this.networkElementAttributes = networkElementAttributes;
        this.connInfoAttributes = connInfoAttributes;
    }

    /**
     * @return the nodeFdn
     */
    public String getNodeFdn() {
        return nodeFdn;
    }

    /**
     * @return the nodeName
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * @return the connInfoNamespace
     */
    public String getConnInfoNamespace() {
        return connInfoNamespace;
    }

    /**
     * @return the connInfoModelName
     */
    public String getConnInfoModelName() {
        return connInfoModelName;
    }

    /**
     * @return the networkElementAttributes
     */
    public Map<String, Object> getNetworkElementAttributes() {
        return networkElementAttributes;
    }

    /**
     * @return the connInfoAttributes
     */
    public Map<String, Object> getConnInfoAttributes() {
        return connInfoAttributes;
    }

    public static class Builder {

        private final String nodeFdn;
        private String connInfoNamespace;
        private String connInfoModelName;
        private final Map<String, Object> networkElementAttributes = new HashMap<>();
        private final Map<String, Object> connInfoAttributes = new HashMap<>();

        public Builder(final String nodeFdn) {
            this.nodeFdn = nodeFdn;
        }

        public AddNodeRequest build() {
            return new AddNodeRequest(nodeFdn, connInfoNamespace, connInfoModelName, networkElementAttributes, connInfoAttributes);
        }

        /**
         * Sets the namespace of the <code>AbstractConnectivityInformation</code> model, e.g. CPP_MED.
         *
         * @param connInfoNamespace
         *            the namespace of the ConnectivityInformation mode
         * @return <code>Builder</code>
         */
        public Builder connInfoNamespace(final String connInfoNamespace) {
            this.connInfoNamespace = connInfoNamespace;
            return this;
        }

        /**
         * Sets the name of <code>AbstractConnectivityInformation</code> model, e.g. <code>CppConnectivitiyInformation</code>.
         *
         * @param connInfoModelName
         *            the name of the ConnectivityInformation model
         * @return <code>Builder</code>
         */
        public Builder connInfoModelName(final String connInfoModelName) {
            this.connInfoModelName = connInfoModelName;
            return this;
        }

        /**
         * Add a <code>NetworkElement</code> attribute. <i>neType</i>, <i>ossPrefix</i> and <i>ossModelIdentity</i> attributes will be read from the
         * AP Node MO, so only necessary to add any additional attributes if requried.
         *
         * @param attributeName
         *            the attribute name
         * @param attributeValue
         *            the attribute value
         * @return <code>Builder</code>
         */
        public Builder addNetworkElementAttribute(final String attributeName, final Object attributeValue) {
            networkElementAttributes.put(attributeName, attributeValue);
            return this;
        }

        /**
         * Add a <code>AbstractConnectivityInformation</code> attribute. <i>ipAddress</i> will be included by default so only necessary to add
         * additional attributes if requried.
         *
         * @param attributeName
         *            the attribute name
         * @param attributeValue
         *            the attribute value
         * @return <code>Builder</code>
         */
        public Builder addConnInformationAttribute(final String attributeName, final Object attributeValue) {
            connInfoAttributes.put(attributeName, attributeValue);
            return this;
        }
    }
}
