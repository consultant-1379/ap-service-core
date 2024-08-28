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

import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.mapper.profile.ProfileMoMapper;
import com.ericsson.oss.services.ap.core.usecase.profile.ProfileMoCreator;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Modifies ConfigurationProfile MO and files
 */
@UseCase(name = UseCaseName.MODIFY_PROFILE)
public class ModifyProfileUseCase extends BaseProfileUseCase {

    @Inject
    private ProfileMoMapper profileMoMapper;

    @Inject
    private ProfileMoCreator profileMoCreator;

    /**
     * Modifies the ConfigurationProfile MO in database and update it's files.
     *
     * @param profileMoData object containing ConfigurationProfile data
     * @return MoData object containing created MO
     */
    public MoData execute(final MoData profileMoData) {
        final FDN fdn = FDN.get(profileMoData.getFdn());
        final String projectName = fdn.getRdnValueOfType(MoType.PROJECT.toString());
        final String profileName = fdn.getRdnValue();

        this.checkIfProfileExists(profileMoData.getFdn());

        if (profileStorageHandler.profileHasFiles(projectName, profileName)) {
            profileStorageHandler.deleteProfileDirectory(projectName, profileName);
        }

        final Map<String, Object> profileAttributes = extractAttributes(profileMoData);
        final Map<String, Object> profileFileAttributes = persistFilesFromProfileData(profileMoData, projectName, profileName);
        profileAttributes.putAll(profileFileAttributes);

        dpsOperations.deleteMo(profileMoData.getFdn());
        final ManagedObject profileMo = profileMoCreator.create(profileName, fdn.getRoot(), profileAttributes);

        return profileMoMapper.toMoData(profileMo);
    }
}
