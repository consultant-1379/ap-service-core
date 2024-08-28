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
package com.ericsson.oss.services.ap.core.usecase.mapper.profile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.core.usecase.profile.ProfileStorageHandler;

/**
 * Class for mapping a profile {@link ManagedObject} to a profile {@link MoData}.
 */
public class ProfileMoMapper {

    @Inject
    private ProfileStorageHandler profileStorageHandler;

    /**
     * Converts a Profile {@link ManagedObject} to {@link MoData}.
     * <p>
     * Notice this mapper retrieves the profile files (Configuration, Graphic),
     * converts them to base64 and populate them into the profile properties.
     *
     * @param profileMo {@link ManagedObject} of a Profile
     * @return MoData of the Profile with files encoded and populated
     */
    public MoData toMoData(final ManagedObject profileMo) {
        return new MoData(
            profileMo.getFdn(),
            buildResponseAttributes(profileMo),
            profileMo.getType(),
            new ModelData(profileMo.getNamespace(), profileMo.getVersion())
        );
    }

    private Map<String, Object> buildResponseAttributes(final ManagedObject profileMo) {
        final Map<String, Object> profileAttributes = profileMo.getAllAttributes();
        return addFileContentsToProfileAttributes(profileAttributes);
    }

    private Map<String, Object> addFileContentsToProfileAttributes(final Map<String, Object> profileAttributes) {

        final String configurationLocation = (String) profileAttributes.get(ProfileAttribute.PROFILE_CONTENT_LOCATION.toString());
        final List<Map<String, Object>> configurationsList = profileStorageHandler.getFilePathsInLocation(configurationLocation);

        final String graphicLocation = (String) profileAttributes.get(ProfileAttribute.GRAPHIC_LOCATION.toString());
        final List<Map<String, Object>> graphicFiles = profileStorageHandler.getFilePathsInLocation(graphicLocation);
        final Map<String, Object> graphicMap = CollectionUtils.isEmpty(graphicFiles) ? Collections.emptyMap() : graphicFiles.get(0);

        final String filterLocation = (String) profileAttributes.get(ProfileAttribute.FILTER_LOCATION.toString());
        final List<Map<String, Object>> getConfigScriptFiles = profileStorageHandler.getFilePathsInLocation(filterLocation);
        final Map<String, Object> getConfigScriptMap = CollectionUtils.isEmpty(getConfigScriptFiles) ? Collections.emptyMap() : getConfigScriptFiles.get(0);

        profileAttributes.put(ProfileAttribute.GRAPHIC.toString(), graphicMap);
        profileAttributes.put(ProfileAttribute.CONFIGURATIONS.toString(), configurationsList);
        profileAttributes.put(ProfileAttribute.GET_CONFIG_SCRIPT.toString(), getConfigScriptMap);

        return profileAttributes;
    }
}
