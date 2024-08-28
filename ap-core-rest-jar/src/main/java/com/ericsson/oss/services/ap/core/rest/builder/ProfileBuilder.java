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
package com.ericsson.oss.services.ap.core.rest.builder;

import java.util.List;
import java.util.function.Consumer;

import com.ericsson.oss.services.ap.common.util.file.File;
import com.ericsson.oss.services.ap.core.rest.model.profile.Ciq;
import com.ericsson.oss.services.ap.core.rest.model.profile.Profile;
import com.ericsson.oss.services.ap.core.rest.model.profile.Status;
import com.ericsson.oss.services.ap.core.rest.model.profile.Version;

/**
 * Builder for creating a Profile.
 */
public class ProfileBuilder {

    protected String name;
    protected String properties;
    protected Version version;
    protected String graphicLocation;
    protected String profileContentLocation;
    protected Ciq ciq;
    protected Status status;
    protected File graphic;
    protected List<File> configurations;
    protected String ossModelIdentity;
    protected String upgradePackageName;
    protected String dataType;
    protected String configSnapshotStatus;
    protected Long dumpTimeStamp;
    protected String filterLocation;
    protected File getConfigScript;

    private ProfileBuilder() {
    }

    /**
     * Creates a new instance of this builder.
     *
     * @return ProfileBuilder
     */
    public static ProfileBuilder newBuilder() {
        return new ProfileBuilder();
    }

    /**
     * Provides a consumer of {@link ProfileBuilder} to
     * populate the properties of the {@link Profile}.
     *
     * @param builderConsumer {@link Consumer}
     * @return ProfileBuilder
     */
    public ProfileBuilder with(final Consumer<ProfileBuilder> builderConsumer) {
        builderConsumer.accept(this);
        return this;
    }

    /**
     * Builds and returns the Profile populated using method "with".
     *
     * @return Profile
     */
    public Profile build() {
        return new Profile()
            .setName(name)
            .setProperties(properties)
            .setVersion(version)
            .setGraphicLocation(graphicLocation)
            .setProfileContentLocation(profileContentLocation)
            .setCiq(ciq)
            .setStatus(status)
            .setGraphic(graphic)
            .setConfigurations(configurations)
            .setOssModelIdentity(ossModelIdentity)
            .setUpgradePackageName(upgradePackageName)
            .setDataType(dataType)
            .setConfigSnapshotStatus(configSnapshotStatus)
            .setDumpTimeStamp(dumpTimeStamp)
            .setGetConfigScript(getConfigScript)
            .setFilterLocation(filterLocation);
    }
}
