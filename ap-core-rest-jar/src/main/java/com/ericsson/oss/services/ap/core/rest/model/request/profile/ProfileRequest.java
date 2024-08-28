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
package com.ericsson.oss.services.ap.core.rest.model.request.profile;

import java.util.List;

import com.ericsson.oss.services.ap.common.util.file.File;
import com.ericsson.oss.services.ap.core.rest.model.profile.Status;
import com.ericsson.oss.services.ap.core.rest.model.profile.Version;
import com.ericsson.oss.services.ap.core.rest.validation.ValidName;

/**
 * Represents the Body payload used in the create profile endpoint
 */
public class ProfileRequest {

    @ValidName
    private String name;
    private String properties;
    private File graphic;
    private File ciq;
    private List<File> configurations;
    private Status status;
    private Version version;
    private String ossModelIdentity;
    private String upgradePackageName;
    private String dataType;
    private String configSnapshotStatus;
    private Long dumpTimeStamp;
    private File getConfigScript;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(final String properties) {
        this.properties = properties;
    }

    public File getGraphic() {
        return graphic;
    }

    public void setGraphic(final File graphic) {
        this.graphic = graphic;
    }

    public File getCiq() {
        return ciq;
    }

    public void setCiq(final File ciq) {
        this.ciq = ciq;
    }

    public List<File> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(final List<File> configurations) {
        this.configurations = configurations;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(final Version version) {
        this.version = version;
    }

    public String getOssModelIdentity() {
        return ossModelIdentity;
    }

    public void setOssModelIdentity(final String ossModelIdentity) {
        this.ossModelIdentity = ossModelIdentity;
    }

    public String getUpgradePackageName() {
        return upgradePackageName;
    }

    public void setUpgradePackageName(final String upgradePackageName) {
        this.upgradePackageName = upgradePackageName;
    }

    public String getConfigSnapshotStatus() {
        return configSnapshotStatus;
    }

    public void setConfigSnapshotStatus(final String configSnapshotStatus) {
        this.configSnapshotStatus = configSnapshotStatus;
    }

    public Long getDumpTimeStamp() {
        return dumpTimeStamp;
    }

    public void setDumpTimeStamp(final Long dumpTimeStamp) {
        this.dumpTimeStamp = dumpTimeStamp;
    }

    public File getGetConfigScript() {
        return getConfigScript;
    }

    public void setGetConfigScript(final File getConfigScript) {
        this.getConfigScript = getConfigScript;
    }
}
