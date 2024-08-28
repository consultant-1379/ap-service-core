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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class containing data to be validated in ValidationConfigurationService.
 */
public class ValidationData {

    private String nodeType;

    private String productNumber;

    private String revision;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String upgradePackagePath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("preConfiguration")
    private ConfigurationFile preconfigurationFile;

    @JsonProperty("configurations")
    private List<ConfigurationFile> configurationFiles = new ArrayList<>();

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(final String nodeType) {
        this.nodeType = nodeType;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(final String revision) {
        this.revision = revision;
    }

    /**
     * This method returns the upgradePackagePath.
     *
     * @return the upgradePackagePath
     */
    public String getUpgradePackagePath() {
        return upgradePackagePath;
    }

    /**
     * This method sets the upgradePackagePath.
     *
     * @param upgradePackagePath
     *            the upgradePackagePath to set
     */
    public void setUpgradePackagePath(final String upgradePackagePath) {
        this.upgradePackagePath = upgradePackagePath;
    }

    public ConfigurationFile getPreconfigurationFile() {
        return preconfigurationFile;
    }

    public void setPreconfigurationFile(ConfigurationFile preconfigurationFile) {
        this.preconfigurationFile = preconfigurationFile;
    }

    public List<ConfigurationFile> getConfigurationFiles() {
        return configurationFiles;
    }

    public void setConfigurationFiles(final List<ConfigurationFile> configurationFiles) {
        this.configurationFiles = configurationFiles;
    }

    @JsonIgnore
    public boolean isValidateDelta() {
        return preconfigurationFile != null;
    }
}
