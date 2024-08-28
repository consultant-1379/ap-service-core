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

import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.MoType

class DeleteProfileUseCaseSpec extends CdiSpecification {

    private static final String PROJECT_NAME = "project1"
    private static final String PROFILE_NAME = "profile1"

    private static def projectFdn
    private static def profileFdn

    @ObjectUnderTest
    private DeleteProfileUseCase deleteProfileUseCase

    @MockedImplementation
    private ArtifactResourceOperations artifactResourceOperations

    @MockedImplementation
    private DpsOperations dpsOperations

    @Inject
    private DpsQueries dpsQueries

    @Inject
    DataPersistenceService dataPersistenceService

    RuntimeConfigurableDps dps

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        Whitebox.setInternalState(dpsQueries, "dps", dataPersistenceService)

        dpsOperations.existsMoByFdn(*_) >> true
    }

    def "when delete profile usecase is executed then configuration profile mo and files are deleted"() {

        given: "project exists on system and a profile is associated with the project"
        def projectMo = createTestProjectMo()
        def profileMo = createTestConfigurationProfileMo(projectMo)

        when: "when delete profile usecase is executed"
        deleteProfileUseCase.execute(PROJECT_NAME, PROFILE_NAME)

        then: "profile mo and files are deleted"
        1 * artifactResourceOperations.directoryExists(*_) >> true
        1 * artifactResourceOperations.directoryExistAndNotEmpty(*_) >> false
        2 * artifactResourceOperations.deleteDirectory(*_)

        1 * dpsOperations.deleteMo(profileMo.fdn)
    }

    def "when delete profile usecase is executed and more than one profile exists then delete profile and files but don't delete the root profile folder"() {

        given: "project exists on system and a profile is associated with the project"
        def projectMo = createTestProjectMo()
        def profileMo = createTestConfigurationProfileMo(projectMo)

        when: "when delete profile usecase is executed"
        deleteProfileUseCase.execute(PROJECT_NAME, PROFILE_NAME)

        then: "profile mo and files are deleted"
        1 * artifactResourceOperations.directoryExists(*_) >> true
        1 * artifactResourceOperations.directoryExistAndNotEmpty(*_) >> true
        1 * artifactResourceOperations.deleteDirectory(*_)

        1 * dpsOperations.deleteMo(profileMo.fdn)
    }

    def "when delete profile usecase is executed with invalid profileId then throw ProfileNotFoundException"() {
        given: "project exists on system"
        createTestProjectMo()

        when: "when delete profile usecase is executed"
        deleteProfileUseCase.execute(PROJECT_NAME, PROFILE_NAME)

        then: "ProfileNotFoundException is thrown"
        thrown(ProfileNotFoundException)

        1 * dpsOperations.existsMoByFdn(*_) >> false
    }

    private ManagedObject createTestConfigurationProfileMo(final ManagedObject projectMo) {
        profileFdn = projectFdn + ",ConfigurationProfile=" + PROFILE_NAME

        return dps.addManagedObject()
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
        projectFdn = "Project=" + PROJECT_NAME
        final Map<String, Object> projectAttributes = new HashMap<String, Object>()

        return dps.addManagedObject()
                .withFdn(projectFdn)
                .type(PROJECT.toString())
                .namespace(AP.toString())
                .version("1.0.0")
                .addAttributes(projectAttributes)
                .build()
    }

}
