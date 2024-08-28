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

import com.ericsson.oss.services.ap.core.usecase.profile.ProfileStorageHandler

import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.resources.Resource
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.ProfileAttribute

class ViewProfilesUseCaseSpec extends CdiSpecification {

    public static final String PROJECT_NAME = "Project1"
    public static final String PROFILE_NAME = "Profile1"
    public static final String CONFIGURATION_PROFILE = "ConfigurationProfile"
    public static final String PROJECT_FDN = "Project=" + PROJECT_NAME
    public static final String INTEGRATION_DATATYPE = "node-plugin-request-action"
    public static final String EXPANSION_DATATYPE = "node-plugin-request-action-expansion"
    public static final String PROFILE_FDN = PROJECT_FDN + "," + CONFIGURATION_PROFILE + "=" + PROFILE_NAME

    public static final String CONFIG_LOCATION = "/some/config"
    public static final String GRAPHIC_LOCATION = "/some/graphic"
    public static final String FILTER_LOCATION = "/some/getConfigScript"

    public static final String TEST_FILE_NAME = "test.xml"
    public static final String TEST_FILE_CONTENT = "Test Content plaintext"

    @ObjectUnderTest
    private ViewProfilesUseCase viewProfilesUseCase

    @Inject
    private DpsQueries dpsQueries

    @Inject
    ProfileStorageHandler profileStorageHandler

    @MockedImplementation
    private ResourceService resourceService

    @MockedImplementation
    private Resource testFileResource

    RuntimeConfigurableDps dps
    private ManagedObject project

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(ResourceService.class, null) >> resourceService
        profileStorageHandler.init()
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        project = addProjectMo()
        setupResourcesService()
    }

    def setupResourcesService() {
        testFileResource.getName() >> TEST_FILE_NAME

        resourceService.listFiles(CONFIG_LOCATION) >> [testFileResource, testFileResource]
        resourceService.listFiles(GRAPHIC_LOCATION) >> [testFileResource]
        resourceService.listFiles(FILTER_LOCATION) >> [testFileResource]
        resourceService.getBytes(*_) >> TEST_FILE_CONTENT.getBytes()
    }

    def "when view profile usecase is executed on project containing profile then valid profile mo returned"() {
        given: "profile created"
        def profile = addConfigurationProfileMo(project)
        List<ManagedObject> profiles = new ArrayList<>()
        profiles.add(profile)

        when: "view profiles usecase is executed with datatype as Integration"
        List<MoData> moData = viewProfilesUseCase.execute(PROJECT_FDN,INTEGRATION_DATATYPE)

        then: "one profile returned in a list"
        moData.size() == 1
        def profileResult = moData.get(0)
        profileResult.type.contains(CONFIGURATION_PROFILE)

        and: "profile contains valid attributes"
        profileResult.fdn.contains(PROFILE_FDN)
        profileResult.attributes.containsKey("name")
        profileResult.attributes.containsKey("properties")

        final graphicMap = (Map<String, String>) profileResult.attributes.get(ProfileAttribute.GRAPHIC.toString())
        graphicMap.get("name") == TEST_FILE_NAME
        graphicMap.get("content") == Base64.getEncoder().encodeToString(TEST_FILE_CONTENT.getBytes())

        final getConfigScriptMap = (Map<String, String>) profileResult.attributes.get(ProfileAttribute.GET_CONFIG_SCRIPT.toString())
        getConfigScriptMap.get("name") == TEST_FILE_NAME
        getConfigScriptMap.get("content") == Base64.getEncoder().encodeToString(TEST_FILE_CONTENT.getBytes())

        final configurations = (List<Map<String, String>>) profileResult.attributes.get(ProfileAttribute.CONFIGURATIONS.toString())
        configurations.size() == 2
        for (Map<String, String> conf : configurations) {
            conf.get("name") == TEST_FILE_NAME
            conf.get("content") == TEST_FILE_CONTENT
        }
    }

    def "when view profile usecase is executed and project has no profiles then empty list returned"() {
        given: "empty profiles list returned as child of project"

        when: "view profiles usecase is executed with datatype as Expansion"
        List<MoData> moData = viewProfilesUseCase.execute(PROJECT_FDN,EXPANSION_DATATYPE)

        then: "no profiles returned - empty list"
        moData.size() == 0
    }

    def "when view profile usecase is executed and project has no data types then empty profile list returned"() {
        given: "empty profiles list returned as child of project"

        when: "view profiles usecase is executed"
        List<MoData> moData = viewProfilesUseCase.execute(PROJECT_FDN,"Data Type")

        then: "no profiles returned - empty list"
        moData.size() == 0
    }

    private ManagedObject addProjectMo() {
        final Map<String, Object> projectAttributes = new HashMap<String, Object>()
        projectAttributes.put("attr1", "value1")
        projectAttributes.put("attr2", "value2")
        return dps.addManagedObject()
                .withFdn(PROJECT_FDN)
                .type(PROJECT.toString())
                .namespace(AP.toString())
                .version("1.0.0")
                .addAttributes(projectAttributes)
                .build()
    }

    private ManagedObject addConfigurationProfileMo(final ManagedObject project) {
        final Map<String, Object> profileAttributes = new HashMap<>()
        profileAttributes.put("name", PROFILE_NAME)
        profileAttributes.put("properties", "{prop1:someprop}")

        profileAttributes.put(ProfileAttribute.GRAPHIC_LOCATION.toString(), GRAPHIC_LOCATION)
        profileAttributes.put(ProfileAttribute.PROFILE_CONTENT_LOCATION.toString(), CONFIG_LOCATION)
        profileAttributes.put(ProfileAttribute.FILTER_LOCATION.toString(), FILTER_LOCATION)

        return dps.addManagedObject()
                .withFdn(PROFILE_FDN)
                .type("ConfigurationProfile")
                .namespace(AP.toString())
                .version("1.0.0")
                .addAttributes(profileAttributes)
                .parent(project)
                .build()
    }
}
