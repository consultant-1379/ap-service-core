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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.AlreadyDefinedException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ProfileExistsException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.mapper.profile.ProfileMoMapper;
import com.ericsson.oss.services.ap.core.usecase.profile.ProfileMoCreator;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Creates ConfigurationProfile MO and files
 */
@UseCase(name = UseCaseName.CREATE_PROFILE)
public class CreateProfileUseCase extends BaseProfileUseCase {

    @Inject
    private Logger logger;

    @Inject
    private ProfileMoCreator profileMoCreator;

    @Inject
    private ProfileMoMapper profileMoMapper;

    /**
     * Creates profile MO in database
     *
     * @param profileMoData object containing ConfigurationProfile data
     * @return MoData object containing created MO
     */
    public MoData execute(final MoData profileMoData) {
        final FDN fdn = FDN.get(profileMoData.getFdn());
        final String projectName = fdn.getRdnValueOfType(MoType.PROJECT.toString());
        final String profileName = fdn.getRdnValue();

        /*
         * Current solution only requires us to allows for one profile per project.
         * This code ensures only one Profile can be created under a Project.
         * This Check can be removed when requirements allow for more than one Profile per Project.
         * See TORF-362174 for details.
         */
        final Iterator<ManagedObject> profileMos = dpsQueries.findChildMosOfTypes(fdn.getParent(), MoType.CONFIGURATION_PROFILE.toString()).execute();
        List<Iterator<ManagedObject>> list = Arrays.asList(profileMos);
        int count = list.size();
        if (count > 0) {
            while(profileMos.hasNext()){
                final ManagedObject profileMO = profileMos.next();
                final String existingProfileType = profileMO.getAttribute(ProfileAttribute.DATATYPE.toString());
                logger.info("existing profile type from the user name is {} ", existingProfileType);
                if (count <= 2) {
                    final String newProfileType = (String) profileMoData.getAttribute(ProfileAttribute.DATATYPE.toString());
                    logger.info("new profile type from the user name is {} ", newProfileType);
                    if (newProfileType.equals(existingProfileType)) {
                        throw new ProfileExistsException(projectName, profileName);
                    }
                }
            }
        }

        try {
            final Map<String, Object> profileAttributes = extractAttributes(profileMoData);
            final Map<String, Object> profileFileAttributes = persistFilesFromProfileData(profileMoData, projectName, profileName);
            profileAttributes.putAll(profileFileAttributes);

            final ManagedObject profileMo = profileMoCreator.create(profileName, fdn.getRoot(), profileAttributes);

            return profileMoMapper.toMoData(profileMo);

        } catch (final AlreadyDefinedException e) {
            logger.error(profileName, projectName, "Profile with name {0} already exists under {1}", e);
            throw new ProfileExistsException(projectName, profileName);
        }
    }
}
