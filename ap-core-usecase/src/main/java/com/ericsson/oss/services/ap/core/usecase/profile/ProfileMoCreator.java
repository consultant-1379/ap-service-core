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
package com.ericsson.oss.services.ap.core.usecase.profile;

import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * DAO for reading/writing ConfigurationProfile MOs
 */
public class ProfileMoCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private ModelReader modelReader;

    /**
     * Creates ConfigurationProfile MO
     *
     * @param profileName {@link String} name of profile
     * @param projectFdn {@link String} fdn of parent project
     * @param attributes {@link Map} attributes of profile mo
     * @return ManagedObject contains persisted profile data
     */
    @Transactional(txType = Transactional.TxType.REQUIRES)
    public ManagedObject create(final String profileName, final String projectFdn, final Map<String, Object> attributes) {
        ddpTimer.start(CommandLogName.CREATE_PROFILE.toString());
        final ManagedObject projectMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(projectFdn);
        if (projectMo == null) {
            throw new ProjectNotFoundException(FDN.get(projectFdn).getRdnValueOfType(MoType.PROJECT.toString()));
        }

        final ManagedObject profileMo = createProfileMo(profileName, projectMo, attributes);
        final String profileFdn = profileMo.getFdn();
        ddpTimer.end(profileFdn);
        return profileMo;
    }

    private ManagedObject createProfileMo(final String profileName, final ManagedObject projectMo, final Map<String, Object> profileAttributes) {
        final ModelData apModelData = modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString());
        return dps.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .namespace(apModelData.getNameSpace())
            .version(apModelData.getVersion())
            .type(MoType.CONFIGURATION_PROFILE.toString())
            .name(profileName)
            .parent(projectMo)
            .addAttributes(profileAttributes)
            .create();
    }
}
