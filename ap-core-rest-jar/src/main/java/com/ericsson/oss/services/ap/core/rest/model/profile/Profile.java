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
package com.ericsson.oss.services.ap.core.rest.model.profile;

import java.util.List;

import com.ericsson.oss.services.ap.common.util.file.File;

/**
 * POJO model for representing a Profile.
 */
public class Profile {

    private String name;
    private String properties;
    private Version version;
    private String graphicLocation;
    private String profileContentLocation;
    private Ciq ciq;
    private Status status;
    private File graphic;
    private List<File> configurations;
    private String ossModelIdentity;
    private String upgradePackageName;
    private String dataType;
    private String configSnapshotStatus;
    private Long dumpTimeStamp;
    private String filterLocation;
    private File getConfigScript;

    public String getDataType() {
        return dataType;
    }

    public Profile setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public Profile() {
        // Needed for JSON serialization
    }

    public String getName() {
        return name;
    }

    public Profile setName(final String name) {
        this.name = name;
        return this;
    }

    public String getProperties() {
        return properties;
    }

    public Profile setProperties(final String properties) {
        this.properties = properties;
        return this;
    }

    public Version getVersion() {
        return version;
    }

    public Profile setVersion(final Version version) {
        this.version = version;
        return this;
    }

    public String getGraphicLocation() {
        return graphicLocation;
    }

    public Profile setGraphicLocation(final String graphicLocation) {
        this.graphicLocation = graphicLocation;
        return this;
    }

    public String getProfileContentLocation() {
        return profileContentLocation;
    }

    public Profile setProfileContentLocation(final String profileContentLocation) {
        this.profileContentLocation = profileContentLocation;
        return this;
    }

    public Ciq getCiq() {
        return ciq;
    }

    public Profile setCiq(final Ciq ciq) {
        this.ciq = ciq;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public Profile setStatus(final Status status) {
        this.status = status;
        return this;
    }

    public File getGraphic() {
        return graphic;
    }

    public Profile setGraphic(final File graphic) {
        this.graphic = graphic;
        return this;
    }

    public List<File> getConfigurations() {
        return configurations;
    }

    public Profile setConfigurations(final List<File> configurations) {
        this.configurations = configurations;
        return this;
    }

    public String getOssModelIdentity() {
        return ossModelIdentity;
    }

    public Profile setOssModelIdentity(final String ossModelIdentity) {
        this.ossModelIdentity = ossModelIdentity;
        return this;
    }

    public String getUpgradePackageName() {
        return upgradePackageName;
    }

    public Profile setUpgradePackageName(final String upgradePackageName) {
        this.upgradePackageName = upgradePackageName;
        return this;
    }

    public String getConfigSnapshotStatus() {
        return configSnapshotStatus;
    }

    public Profile setConfigSnapshotStatus(final String configSnapshotStatus) {
        this.configSnapshotStatus = configSnapshotStatus;
        return this;
    }

    public Long getDumpTimeStamp() {
        return dumpTimeStamp;
    }

    public Profile setDumpTimeStamp(final Long dumpTimeStamp) {
        this.dumpTimeStamp = dumpTimeStamp;
        return this;
    }

    public File getGetConfigScript() {
        return getConfigScript;
    }

    public Profile setGetConfigScript(final File getConfigScript) {
        this.getConfigScript = getConfigScript;
        return this;
    }

    public String getFilterLocation() {
        return filterLocation;
    }

    public Profile setFilterLocation(final String filterLocation) {
        this.filterLocation = filterLocation;
        return this;
    }
}
