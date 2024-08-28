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
package com.ericsson.oss.services.ap.core.usecase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.mapper.profile.ProfileMoMapper;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * View the profiles for a project.
 */
@UseCase(name = UseCaseName.VIEW_PROFILES)
public class ViewProfilesUseCase extends BaseProfileUseCase{

    @Inject
    private ProfileMoMapper profileMoMapper;
    /**
     * Reads the model for an AP project and extracts child profiles
     *
     * @param projectFdn
     *            the FDN of the project in AP model
     * @return a {@link List} of {@link MoData} containing profile data
     */
    public List<MoData> execute(final String projectFdn, final String dataType) {
        return buildMoData(projectFdn,dataType);
    }

    private List<MoData> buildMoData(final String projectFdn, final String dataType) {
        final String type = getProfileType(dataType);
        final List <MoData> profiles = new ArrayList<>();
        final Iterator<ManagedObject> profileMos = getAllProfileMOs(projectFdn);
        profileMos.forEachRemaining(mo -> {
            MoData profile = profileMoMapper.toMoData(mo);
            final String profileType = (String) profile.getAttribute(ProfileAttribute.DATATYPE.toString());
            if (profileType == null || profileType.equalsIgnoreCase(type)) {
                profiles.add(profile);
            }
        });
        return profiles;
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
