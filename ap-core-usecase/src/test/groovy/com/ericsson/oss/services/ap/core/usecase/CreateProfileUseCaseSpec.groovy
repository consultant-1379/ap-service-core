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

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.services.ap.core.usecase.profile.ProfileStorageHandler

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.ProfileExistsException
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.api.model.ModelData
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.Namespace
import com.ericsson.oss.services.ap.common.model.ProfileAttribute
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.core.usecase.testutils.TestConfigurationProfileFactory

class CreateProfileUseCaseSpec extends CdiSpecification {

    private static final String PROPERTIES = "{\"prop\":\"prop1\"}"
    private static final String PROJECT_NAME = "project1"
    private static final String USERNAME = "user"
    private static final String DESCRIPTION = "some description"
    private static final String PROFILE_NAME = "profile1"
    private static final String PROFILE_NAME_2 = "profile2"
    private static final String DETAIL = "detail1"

    @ObjectUnderTest
    private CreateProfileUseCase createProfileUseCase

    @MockedImplementation
    private ModelReader modelReader

    @Inject
    private CreateProjectUseCase createProjectUseCase

    @MockedImplementation
    private ResourceService resourceService

    @Inject
    private DpsQueries dpsQueries

    RuntimeConfigurableDps dps

    @Inject
    ProfileStorageHandler profileStorageHandler

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @MockedImplementation
    private ArtifactResourceOperations artifactResourceOperations

    private static MoData moData
    private static MoData moData2
    private static MoData moDataWithoutFiles
    private static def profile1Fdn
    private static def profile2Fdn
    def modelData = new ModelData("ap", "2.0.0")

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(ResourceService.class, null) >> resourceService
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())

        modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.PROJECT.toString()) >> modelData
        modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()) >> modelData

        artifactResourceOperations.writeArtifact(_, _) >> null

        profile1Fdn = "Project=project1,ConfigurationProfile=" + PROFILE_NAME
        profile2Fdn = "Project=project1,ConfigurationProfile=" + PROFILE_NAME_2

        moData = TestConfigurationProfileFactory.setupFullProfile(profile1Fdn, PROFILE_NAME, PROPERTIES, DETAIL, modelData)
        moData2 = TestConfigurationProfileFactory.setupFullProfile(profile2Fdn, PROFILE_NAME_2, PROPERTIES, DETAIL, modelData)
        moDataWithoutFiles = TestConfigurationProfileFactory.setupProfileWithoutFiles(profile1Fdn, PROFILE_NAME, PROPERTIES, DETAIL, modelData)
    }

    def "when create profile usecase is executed then profile mo is created"() {

        given: "project exists on system"
            createProjectUseCase.execute(PROJECT_NAME, USERNAME, DESCRIPTION)
            profileStorageHandler.init()

        when: "when create profile usecase is executed"
            modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()) >> new ModelData("ap", "2.0.0")
            def profileMo = createProfileUseCase.execute(moData)

        then: "profile mo created is returned"
            profileMo.fdn == profile1Fdn
            def attributes = profileMo.attributes
            def status = (Map<String, Object>) attributes.get("profileStatus")
            status.get("isValid") == true
            status.get("profileDetails")[0] == DETAIL
            attributes.get("properties") == PROPERTIES
    }

    def "when create profile usecase is executed without files then profile mo is created"() {

        given: "project exists on system"
            createProjectUseCase.execute(PROJECT_NAME, USERNAME, DESCRIPTION)

        when: "when create profile usecase is executed with an empty file"
            modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()) >> new ModelData("ap", "2.0.0")

            def profileMo = createProfileUseCase.execute(moDataWithoutFiles)

        then: "profile mo created is returned"
            profileMo.fdn == profile1Fdn
            def attributes = profileMo.attributes
            def status = (Map<String, Object>) attributes.get("profileStatus")
            status.get("isValid") == true
            status.get("profileDetails")[0] == DETAIL
            attributes.get("properties") == PROPERTIES
            attributes.get(ProfileAttribute.CIQ_LOCATION) == null
            attributes.get(ProfileAttribute.GRAPHIC_LOCATION) == null
            attributes.get(ProfileAttribute.PROFILE_CONTENT_LOCATION) == null
            attributes.get(ProfileAttribute.FILTER_LOCATION) == null
    }

    def "when profile more than one Profile is created under project then exception is thrown"() {

        moData = TestConfigurationProfileFactory.setupFullProfile(profile1Fdn, PROFILE_NAME, PROPERTIES, DETAIL, modelData)
        moData2 = TestConfigurationProfileFactory.setupFullProfile(profile1Fdn, PROFILE_NAME, PROPERTIES, DETAIL, modelData)

        given: "project exists on system"
            createProjectUseCase.execute(PROJECT_NAME, USERNAME, DESCRIPTION)
            profileStorageHandler.init()

        and: "profile is created"
            modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()) >> modelData
            createProfileUseCase.execute(moData)

        when: "create second profile under same project"
            createProfileUseCase.execute(moData2)
        then: "ProfileExistsException is thrown"
            thrown(ProfileExistsException)
    }

    def "when project does not exist, exception is thrown"() {

        when: "create profile usecase is executed"
            createProfileUseCase.execute(moData)

        then: "ProfileExistsException is thrown"
            thrown(ProjectNotFoundException)
    }

    def "when profile contains files that are corrupt"() {
        given: "project exists on system"
            createProjectUseCase.execute(PROJECT_NAME, USERNAME, DESCRIPTION)
        and: "file content is invalid"
            Map<String, Object> configuration = moData.attributes.get("configurations")[0]
            configuration.replace("content", "kjdjhd,,,")

        when: "create profile usecase is executed"
            modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()) >> modelData
            createProfileUseCase.execute(moData)

        then: "IllegalArgumentException is thrown"
            thrown(IllegalArgumentException)
    }
}
