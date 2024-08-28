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

package com.ericsson.oss.services.ap.core.usecase

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.DeleteOptions
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.api.model.ModelData
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.Namespace
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.core.usecase.profile.ProfileStorageHandler

class ModifyProfileUseCaseSpec extends CdiSpecification {

    private static final String PROJECT_NAME = "project1"
    private static final String PROFILE_NAME = "profile1"

    private static def projectFdn
    private static def profileFdn

    @ObjectUnderTest
    private ModifyProfileUseCase modifyProfileUseCase

    @MockedImplementation
    private ProfileStorageHandler profileStorageHandler

    @MockedImplementation
    private static ModelReader modelReader

    @MockedImplementation
    private DpsOperations dpsOperations

    @Inject
    private DpsOperations dps

    @Inject
    private DpsQueries dpsQueries

    @Inject
    DataPersistenceService dataPersistenceService

    RuntimeConfigurableDps dpsRun

    def modelData = new ModelData("ap", "2.0.0")

    def setup() {
        dpsRun = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dpsRun.build()
        Whitebox.setInternalState(dpsQueries, "dps", dataPersistenceService)
        Whitebox.setInternalState(dpsOperations, "dps", dataPersistenceService)
        Whitebox.setInternalState(dps, "dps", dataPersistenceService)

        dps.getDataPersistenceService() >> dataPersistenceService
        modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.PROJECT.toString()) >> modelData
        modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()) >> modelData

        projectFdn = "Project=" + PROJECT_NAME
        profileFdn = projectFdn + ",ConfigurationProfile=" + PROFILE_NAME
    }

    def "when modify profile usecase is executed then configuration profile mo and files are updated"() {

        given: "project exists on system and a profile is associated with the project"
        def projectMo = createTestProjectMo()
        def profileMo = createTestConfigurationProfileMo(projectMo)

        when: "when modify profile usecase is executed"
        def modifiedProfileMo = modifyProfileUseCase.execute(getConfigurationProfileMOForUpdate())

        then: "profile mo and files are updated"
        1 * dpsOperations.existsMoByFdn(profileFdn) >> true

        1 * profileStorageHandler.profileHasFiles(PROJECT_NAME, PROFILE_NAME) >> true
        1 * profileStorageHandler.deleteProfileDirectory(*_)
        1 * profileStorageHandler.saveCiqFile(PROJECT_NAME, PROFILE_NAME, *_) >> "/some/location"

        1 * dpsOperations.deleteMo(profileFdn) >> {
            dataPersistenceService
                    .getLiveBucket()
                    .deleteManagedObject(
                    dataPersistenceService.getLiveBucket().findMoByFdn(profileFdn),
                    DeleteOptions.defaultDelete())
        }

        profileMo.parent.fdn == projectMo.fdn
        profileMo.fdn == modifiedProfileMo.fdn
    }

    def "when modify profile usecase is executed with invalid profileId then throw ProfileNotFoundException"() {
        given: "project exists on system"
        createTestProjectMo()

        when: "when modify profile usecase is executed"
        modifyProfileUseCase.execute(getConfigurationProfileMOForUpdate())

        then: "ProfileNotFoundException is thrown"
        thrown(ProfileNotFoundException)

        1 * dpsOperations.existsMoByFdn(*_) >> false
    }

    private ManagedObject createTestConfigurationProfileMo(final ManagedObject projectMo) {
        return dpsRun.addManagedObject()
                .withFdn(profileFdn)
                .type(MoType.CONFIGURATION_PROFILE.toString())
                .version("2.0.0")
                .parent(projectMo)
                .name(PROFILE_NAME)
                .addAttribute("profileId", PROFILE_NAME)
                .addAttribute("ciq", ["name": "ciq.csv", "content": "dGVzdA=="])
                .build()
    }

    private ManagedObject createTestProjectMo() {
        final Map<String, Object> projectAttributes = new HashMap<String, Object>()

        return dpsRun.addManagedObject()
                .withFdn(projectFdn)
                .type(MoType.PROJECT.toString())
                .namespace(Namespace.AP.toString())
                .version("1.0.0")
                .addAttributes(projectAttributes)
                .build()
    }

    private MoData getConfigurationProfileMOForUpdate() {
        def ciqMap = ["name": "updated_ciq.csv", "content": "VGVzdCBVcGRhdGVkCg=="]

        Map<String, Object> attributes = [
                "profileId"    : PROFILE_NAME,
                "version"      : [
                        "productNumber" : "123",
                        "productRelease": "R345556"
                ],
                "profileStatus": [
                        "isValid"       : false,
                        "profileDetails": ["UPDATED_DETAIL"]
                ],
                "ciq"          : ciqMap
        ]
        final Map<String, Object> profileAttributes = new LinkedHashMap<>()
        profileAttributes.putAll(new TreeMap<>(attributes))
        def modelData = new ModelData("ap", "2.0.0")

        return new MoData(profileFdn, profileAttributes, MoType.CONFIGURATION_PROFILE.toString(), modelData)
    }

}
