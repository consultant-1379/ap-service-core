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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.util.file.File;
import com.ericsson.oss.services.ap.core.rest.model.profile.Ciq;
import com.ericsson.oss.services.ap.core.rest.model.profile.Profile;
import com.ericsson.oss.services.ap.core.rest.model.profile.ProfileData;
import com.ericsson.oss.services.ap.core.rest.model.profile.Status;
import com.ericsson.oss.services.ap.core.rest.model.profile.Version;

/**
 * Class used to build profile data for REST Response.
 */
public class ProfileDataBuilder {

    /**
     * Transfers data from {@link MoData} to {@link Profile}
     *
     * @param profileMo {@link MoData} Mo containing profile data
     * @return {@link Profile} a pojo to represent a Profile
     */
    public Profile buildProfile(final MoData profileMo) {

        final Map<String, Object> attributes = profileMo.getAttributes();

        final String profileName = (String) attributes.get(ProfileAttribute.PROFILE_ID.toString());
        final String properties = (String) attributes.get(ProfileAttribute.PROPERTIES.toString());
        final String graphicLocation = (String) attributes.get(ProfileAttribute.GRAPHIC_LOCATION.toString());
        final String profileContentLocation = (String) attributes.get(ProfileAttribute.PROFILE_CONTENT_LOCATION.toString());
        final Map<String, Object> ciq = (Map<String, Object>) attributes.get(ProfileAttribute.CIQ.toString());
        final String ciqLocation = ciq != null ? (String) ciq.get(ProfileAttribute.CIQ_LOCATION.toString()) : null;
        final String ossModelIdentity = (String) attributes.get(ProfileAttribute.OSS_MODEL_IDENTITY.toString());
        final String upgradePackageName = (String) attributes.get(ProfileAttribute.UPGRADE_PACKAGE_NAME.toString());
        final String dataType = getDataType(attributes.get(ProfileAttribute.DATATYPE.toString()));
        final String filterLocation = (String) attributes.get(ProfileAttribute.FILTER_LOCATION.toString());

        //-Version Attributes
        final Map<String, Object> version = (Map<String, Object>) attributes.get(ProfileAttribute.VERSION.toString());
        final String productNumber = (String) version.get(ProfileAttribute.PRODUCT_NUMBER.toString());
        final String productRelease = (String) version.get(ProfileAttribute.PRODUCT_RELEASE.toString());

        //-Status Attributes
        final Map<String, Object> status = (Map<String, Object>) attributes.get(ProfileAttribute.STATUS.toString());
        final boolean isValid = (boolean) status.get(ProfileAttribute.IS_VALID.toString());
        final List<String> profileDetails = (List<String>) status.get(ProfileAttribute.PROFILE_DETAILS.toString());

        //-File Content Attributes
        final File graphicContent = File.fromMap((Map<String, Object>) attributes.get(ProfileAttribute.GRAPHIC.toString()));
        final List<File> configurationsContent = File.fromMapList((List<Map<String, Object>>) attributes.get(ProfileAttribute.CONFIGURATIONS.toString()));
        final File getConfigScript = File.fromMap((Map<String, Object>) attributes.get(ProfileAttribute.GET_CONFIG_SCRIPT.toString()));

        //-Snapshot Attributes
        final String snapshotStatus = (String) attributes.get(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString());
        final Long dumpTime = (Long) attributes.get(ProfileAttribute.DUMP_TIMESTAMP.toString());

        return ProfileBuilder.newBuilder()
            .with(pb -> {
                pb.name = profileName;
                pb.properties = properties;
                pb.graphicLocation = graphicLocation;
                pb.profileContentLocation = profileContentLocation;
                pb.ciq = new Ciq(ciqLocation);
                pb.version = new Version(productNumber, productRelease);
                pb.status = new Status(isValid, profileDetails);
                pb.graphic = graphicContent;
                pb.configurations = configurationsContent;
                pb.ossModelIdentity = ossModelIdentity;
                pb.upgradePackageName = upgradePackageName;
                pb.dataType = dataType;
                pb.configSnapshotStatus = snapshotStatus;
                pb.dumpTimeStamp = dumpTime;
                pb.getConfigScript = getConfigScript;
                pb.filterLocation = filterLocation;
            })
            .build();
    }

    /**
     * Builds an ArrayList of {@link Profile} attributes retrieved from a list of {@link MoData} objects containing profile data
     *
     * @param profileData List of {@link MoData} objects containing profiles data.
     * @return {@link ProfileData} as a list of {@link Profile} which is unmarshalled as JSON.
     */
    public ProfileData buildProfileData(final List<MoData> profileData) {
        final List<Profile> profileResponseData = new ArrayList<>();
        for (final MoData profileMo : profileData) {
            profileResponseData.add(buildProfile(profileMo));
        }
        return new ProfileData(profileResponseData);
    }

    private String getDataType(final Object dataTypeMOAttribute) {
        String dataType = "INTEGRATION";

        if (dataTypeMOAttribute != null && !((String) dataTypeMOAttribute).trim().isEmpty()) {
            dataType = (String) dataTypeMOAttribute;
        }

        return dataType;
    }
}
