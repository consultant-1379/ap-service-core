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
package com.ericsson.oss.services.ap.core.rest.model.request.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.model.ConfigSnapshotStatus;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.util.file.File;
import com.ericsson.oss.services.ap.core.rest.model.profile.Status;
import com.ericsson.oss.services.ap.core.rest.model.profile.Version;
import com.ericsson.oss.services.ap.core.rest.model.request.profile.ProfileRequest;

/**
 * Builds MO containing profile data
 */
public final class ProfileMoDataBuilder {

    @Inject
    private ModelReader modelReader;

    /**
     * Builds MO to be passed to services layer
     *
     * @param projectFdn
     *            profile fdn
     * @param profileRequest
     *            POJO representing POST request body
     * @return MoData containing profile data
     */
    public MoData buildMoData(final String projectFdn, final ProfileRequest profileRequest) {

        final Map<String, Object> graphicAttributes = File.toMap(profileRequest.getGraphic());
        final Map<String, Object> ciqAttributes = File.toMap(profileRequest.getCiq());
        final Map<String, Object> profileAttributes = new HashMap<>();
        profileAttributes.put(ProfileAttribute.GRAPHIC.toString(), graphicAttributes);
        profileAttributes.put(ProfileAttribute.CIQ.toString(), ciqAttributes);

        final List<Map<String, Object>> configurations = profileRequest.getConfigurations() == null ? Collections.emptyList()
            : profileRequest.getConfigurations().stream().map(File::toMap).collect(Collectors.toList());
        profileAttributes.put(ProfileAttribute.CONFIGURATIONS.toString(), configurations);
        profileAttributes.put(ProfileAttribute.PROPERTIES.toString(), profileRequest.getProperties() == null ? "" : profileRequest.getProperties());
        final Map<String, Object> getConfigScriptAttributes = File.toMap(profileRequest.getGetConfigScript());
        profileAttributes.put(ProfileAttribute.GET_CONFIG_SCRIPT.toString(), getConfigScriptAttributes);

        final Status status = profileRequest.getStatus();
        final Map<String, Object> statusAttributes = new HashMap<>(2);
        if (status != null) {
            statusAttributes.put(ProfileAttribute.IS_VALID.toString(), status.getIsValid());
            statusAttributes.put(ProfileAttribute.PROFILE_DETAILS.toString(), status.getDetails());
        }
        profileAttributes.put(ProfileAttribute.STATUS.toString(), statusAttributes);

        final Map<String, Object> versionAttributes = new HashMap<>(2);
        final Version version = profileRequest.getVersion();
        if (version != null) {
            versionAttributes.put(ProfileAttribute.PRODUCT_NUMBER.toString(), version.getProductNumber());
            versionAttributes.put(ProfileAttribute.PRODUCT_RELEASE.toString(), version.getProductRelease());
        }
        profileAttributes.put(ProfileAttribute.VERSION.toString(), versionAttributes);

        profileAttributes.put(ProfileAttribute.OSS_MODEL_IDENTITY.toString(), profileRequest.getOssModelIdentity());
        profileAttributes.put(ProfileAttribute.UPGRADE_PACKAGE_NAME.toString(), profileRequest.getUpgradePackageName());
        String profileType = getProfileType(profileRequest.getDataType());

        profileAttributes.put(ProfileAttribute.DATATYPE.toString(), profileType);

        profileAttributes.put(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString(), profileRequest.getConfigSnapshotStatus() == null ?
                                                         ConfigSnapshotStatus.NOT_STARTED.toString() : profileRequest.getConfigSnapshotStatus());
        profileAttributes.put(ProfileAttribute.DUMP_TIMESTAMP.toString(), profileRequest.getDumpTimeStamp() == null ? Long.valueOf(0) : profileRequest.getDumpTimeStamp());

        final ModelData apModelData = modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString());
        return new MoData(projectFdn, profileAttributes, MoType.CONFIGURATION_PROFILE.toString(), apModelData);
    }

    private String getProfileType(String dataType) {
        if (dataType != null) {
            if (dataType.trim().equalsIgnoreCase("node-plugin-request-action")) {
                return "INTEGRATION";
            }
            if (dataType.trim().equalsIgnoreCase("node-plugin-request-action-expansion")
                    || dataType.trim().equalsIgnoreCase("node-plugin-request-action-expansion-extended")) {
                return "EXPANSION";
            }
        }
        return null;
    }
}
