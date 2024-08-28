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
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.resources.Resource
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries

class ExportCIQUsecaseSpec extends CdiSpecification {

    private static final String PROFILE_CIQ_DIRECTORY = "/ericsson/autoprovisioning/projects/%s/profiles/%s/ciq"
    private static final String CIQ_FILE_NAME = "nodePluginCIQ.csv"
    private static final String CIQ_FILE_CONTENT = "Header1, Header2, Header3"
    private static final String PROJECT1_FDN = "Project=Project1"

    @ObjectUnderTest
    private ExportCIQUsecase exportCIQUsecase

    @MockedImplementation
    private ResourceService resourceService

    @Inject
    private DpsQueries dpsQueries

    @Inject
    protected DpsOperations dpsOperations;

    @MockedImplementation
    private Resource resource

    @Inject
    DataPersistenceService dataPersistenceService

    RuntimeConfigurableDps dps

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(ResourceService.class, null) >> resourceService
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
    }

    def "when export CIQ successful then file has correct filename and timestamp" (){
        given: "Required MOs are created"
            final ManagedObject projectMO = addProjectMo(PROJECT1_FDN)
            createProfileMo(projectMO, "Profile1")
            final String regex = /\d*/
            final String fileName = "CIQ_Project1_Profile1_" + regex + ".csv"
            exportCIQUsecase.init()

        when: "usecase is executed"
            final List<Resource> resources = new ArrayList<>()
            resources.add(resource)
            resourceService.supportsWriteOperations(_ as String) >> true
            resourceService.listFiles(_ as String) >> resources
            resource.getName() >> CIQ_FILE_NAME
            resource.getBytes() >> CIQ_FILE_CONTENT.getBytes()
            final String uniqueFileId = exportCIQUsecase.execute(PROJECT1_FDN, "Profile1")

        then: "unique file id is correct"
            uniqueFileId ==~ fileName
    }

    def "when directory is not accesible then AP application exception is thrown" (){
        given: "Required MOs are created"
            final ManagedObject projectMO = addProjectMo("Project=Project1")
            createProfileMo(projectMO, "Profile1")

        when: "directory is not accessible"
            resourceService.supportsWriteOperations(_ as String) >> false
            exportCIQUsecase.execute("Project1","Profile1")

        then: "ApApplicationException is thrown"
            thrown(ApApplicationException)
    }

    def "when profile mo does not exist then ProfileNotFoundException is thrown" (){
        given: "Project MO is created"
            addProjectMo("Project=Project1")

        when: "directory is not accessible"
            resourceService.supportsWriteOperations(_ as String) >> true
            exportCIQUsecase.execute("Project1","Profile1")

        then: "ProfileNotFoundException is thrown"
            thrown(ProfileNotFoundException)
    }

    def "when CIQ file does not exist then AP application exception is thrown" (){
        given: "Required MOs are created"
            final ManagedObject projectMO = addProjectMo("Project=Project1")
            createProfileMoWithNoCIQ(projectMO, "Profile1")

        when: "directory is not accessible"
            resourceService.supportsWriteOperations(_ as String) >> true
            exportCIQUsecase.execute("Project1","Profile1")

        then: "ApApplicationException is thrown"
            thrown(ApApplicationException)
    }

    private ManagedObject addProjectMo(final String fdn) {
        final Map<String, Object> projectAttributes = new HashMap<String, Object>()
        return dps.addManagedObject()
                        .withFdn(fdn)
                        .type(PROJECT.toString())
                        .namespace(AP.toString())
                        .version("1.0.0")
                        .addAttributes(projectAttributes)
                        .build()
    }

    private ManagedObject createProfileMo(final ManagedObject parentMo, final String profileName) {
        final Map<String, Object> profileAttributes = new HashMap<>()
        profileAttributes.put("profileId", profileName)
        profileAttributes.put("ciq", ["ciqLocation" : String.format(PROFILE_CIQ_DIRECTORY, "Project1", "Profile1")])

        return dps.addManagedObject()
                        .withFdn("Project=" + parentMo.getName() + ",ConfigurationProfile=" + profileName)
                        .type("ConfigurationProfile")
                        .version("2.0.0")
                        .parent(parentMo)
                        .name(profileName)
                        .addAttributes(profileAttributes)
                        .build()
    }

    private ManagedObject createProfileMoWithNoCIQ(final ManagedObject parentMo, final String profileName) {
        final Map<String, Object> profileAttributes = new HashMap<>()
        profileAttributes.put("profileId", profileName)

        return dps.addManagedObject()
                        .withFdn("Project=" + parentMo.getName() + ",ConfigurationProfile=" + profileName)
                        .type("ConfigurationProfile")
                        .version("2.0.0")
                        .parent(parentMo)
                        .name(profileName)
                        .addAttributes(profileAttributes)
                        .build()
    }
}
