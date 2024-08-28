/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client;

import com.ericsson.oss.services.ap.core.rest.client.common.RestRequest;

/**
 * Enumeration class used to build url for {@link RestRequest}
 */
public enum RestUrls {

    DHCP_CONFIGURATION_SERVICE("dhcp-service", 8080, "/dhcp/v1/client"),
    NHC_SERVICE("shmcoreservbal", 8080, "/nhcservice/v1/reports/"),
    NHC_PROFILE_SERVICE("shmservbal", 8080, "/nhcprofileservice/v2/profiles/"),
    IL_REQUEST_SERVICE("shmcoreservbal", 8080, "/oss/shm/rest/il/v1/integrationLkf"),
    NODE_PLUGIN_CAPABILITY_SERVICE("radiosystemnodeconfiguration-service", 8080, "/ect/ai/{nodeType}"),
    NODE_PLUGIN_VALIDATION_SERVICE("radiosystemnodeconfiguration-service", 8080, "/ect/ai/v1/{nodeType}/validate"),
    NODE_PLUGIN_DELTA_VALIDATION_SERVICE("radiosystemnodeconfiguration-service", 8080, "/ect/ai/v1/{nodeType}/validateDelta"),
    SHM_SOFTWARE_PACKAGE_SEARCH_SERVICE("shmservbal", 8080, "/oss/shm/rest/softwarePackage/search"),
    SHM_BACKUP_SOFTWARE_VERSIONS_QUERY_SERVICE("shmservbal", 8080, "/oss/shm/rest/inventory/backupSoftwareVersions");

    private static final String HTTP_PROTOCOL = "http";
    private static final String INTERNAL_URL = "INTERNAL_URL";

    private final String host;
    private final String serverAddress;
    private final String serviceContext;

    RestUrls(final String host, final int port, final String path) {
        this.host = host;
        this.serverAddress = HTTP_PROTOCOL + "://" + host + ":" + port;
        this.serviceContext = path;
    }

    /**
     * @return full url in format: http://{host}:{port}{path}
     */
    public String getFullUrl() {
        return System.getProperty(INTERNAL_URL, this.serverAddress) + serviceContext;
    }

    /**
     * @return url's service context part in format: {path}
     */
    public String getServiceContext() {
        return this.serviceContext;
    }

    /**
     * @return url's host part
     */
    public String getHost() {
        return this.host;
    }
}
