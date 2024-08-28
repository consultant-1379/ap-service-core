/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.healthcheck.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * A POJO representing the profile details received from the Health Check Service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileDetails {

    private String name;
    private String softwareVersion;
    private long creationTime;
    private String createdBy;
    private List<String> userLabel;
    private String nodeType;

    public String getNodeType() {
        return nodeType;
    }

    public String getName() {
        return name;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public List<String> getUserLabel() {
        return Collections.unmodifiableList(userLabel);
    }

}
