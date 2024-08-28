/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
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
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.resources.FileResourceBuilder
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean

import java.nio.charset.StandardCharsets

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest

class GetSnapshotUseCaseSpec extends CdiSpecification {

    private static final String PROJECT_NAME = "project1"
    private static final String PROFILE_NAME = "profile1"
    private static final String PROFILE_FDN = "Project=project1,ConfigurationProfile=profile1"
    private static final String NODE_NAME = "NODE1"
    private static final String MANAGED_ELEMENT_FDN = "ManagedElement=" + NODE_NAME;
    private static final String NODE_TYPE = "RadioNode"
    private static final String SNAPSHOT_CONTENT = "<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\"><managedElementId>LTE01dg2ERBS00008</managedElementId><SystemFunctions><systemFunctionsId>1</systemFunctionsId><Lm xmlns=\"urn:com:ericsson:ecim:RcsLM\"><lmId>1</lmId><fingerprint>Site2</fingerprint></Lm></SystemFunctions></ManagedElement>"
    private static final Long DEFAULT_DUMP_TIMESTAMP = Long.valueOf(0)

    @ObjectUnderTest
    private GetSnapshotUseCase getSnapshotUseCase

    @MockedImplementation
    private Resource mockResource

    @MockedImplementation
    private FileResourceBuilder mockResourceBuilder

    @MockedImplementation
    private ResourceService resourceService

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @Inject
    private DpsQueries dpsQueries

    @Inject
    protected DpsOperations dpsOperations;

    RuntimeConfigurableDps dps

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(ResourceService.class, null) >> resourceService
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        Resources.fileResourceBuilder = this.mockResourceBuilder
        mockResourceBuilder.getResource(_) >> mockResource
    }

    def "Get node configuration snapshot content successfully and delete file" (){
        given: "init"
            def projectMO = addProjectMo("Project="+PROJECT_NAME)
            def profileMo = createProfileMo(projectMO, PROFILE_NAME)
            def inputStream = new ByteArrayInputStream(SNAPSHOT_CONTENT.getBytes(StandardCharsets.UTF_8));
            def snapshotBase64 = Base64.getEncoder().encodeToString(SNAPSHOT_CONTENT.getBytes("UTF-8"));
            getSnapshotUseCase.init()
        and: "Polling remote file system successfully "
            resourceService.write(_, _, false) >> getSnapshotUseCase.DATA_SYNC_FILE_USAGE_MSG.size()
        and: "Read snapshot content successfully"
            mockResource.getInputStream() >> inputStream
            mockResource.exists() >> true
        and: "Snapshot file and directory cleanup successfully"
            resourceService.exists(_) >> true
            resourceService.delete(_) >> true
            resourceService.isDirectoryExists(_) >> true
            resourceService.deleteDirectoryIfEmpty(_) >> true

        when: "GetSnapshotUseCase is executed"
            def response = getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "Get snapshot content successfully"
            response.length() == snapshotBase64.length()
        and: "ProfileMo attribute configSnapshotStatus is set correctly"
            profileMo.getAttribute("configSnapshotStatus") == "NOT_STARTED"
            notThrown(Exception)
    }

    def "Get node configuration snapshot content successfully but fail to delete file" (){
        given: "init"
            def projectMO = addProjectMo("Project="+PROJECT_NAME)
            def profileMo = createProfileMo(projectMO, PROFILE_NAME)
            def inputStream = new ByteArrayInputStream(SNAPSHOT_CONTENT.getBytes(StandardCharsets.UTF_8));
            def snapshotBase64 = Base64.getEncoder().encodeToString(SNAPSHOT_CONTENT.getBytes("UTF-8"));
            getSnapshotUseCase.init()
        and: "Polling remote file system successfully "
            resourceService.write(_, _, false) >> getSnapshotUseCase.DATA_SYNC_FILE_USAGE_MSG.size()
        and: "Read snapshot content successfully"
            mockResource.exists() >> true
            mockResource.getInputStream() >> inputStream
        and: "Snapshot file cleanup successfully"
            resourceService.exists(_) >> true
            resourceService.delete(_) >> true
        and: "Snapshot directory cleanup failed"
            resourceService.isDirectoryExists(_) >> false

        when: "GetSnapshotUseCase is executed"
            def response = getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "Get snapshot content successfully"
            response.length() == snapshotBase64.length()
        and: "ProfileMo attribute configSnapshotStatus is set correctly"
            profileMo.getAttribute("configSnapshotStatus") == "FAILED"
            notThrown(Exception)
    }

    def "Snapshot file not exist and return null" (){
        given: "init"
            getSnapshotUseCase.init()
        and: "Polling remote file system successfully "
            resourceService.write(_, _, false) >> getSnapshotUseCase.DATA_SYNC_FILE_USAGE_MSG.size()
        and: "Snapshot file could not be detected in file system"
            mockResource.exists() >> false

        when: "GetSnapshotUseCase is executed"
            def response = getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "get no snapshot content"
            response == null
            notThrown(Exception)
    }

    def "Profile does not exist exception when getting node configuration snapshot"() {
        given: "init"
            addProjectMo("Project="+PROJECT_NAME)
            final InputStream inputStream = new ByteArrayInputStream(SNAPSHOT_CONTENT.getBytes(StandardCharsets.UTF_8));
            getSnapshotUseCase.init()
        and: "Polling remote file system successfully "
            resourceService.write(_, _, false) >> getSnapshotUseCase.DATA_SYNC_FILE_USAGE_MSG.size()
            mockResource.exists() >> true
            mockResource.getInputStream() >> inputStream

        when: "usecase is executed"
            getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "the profile not found exception is thrown"
            thrown(ProfileNotFoundException.class)
    }

    def "Ap applicationException exception when getting node configuration snapshot"() {
        given: "init"
            getSnapshotUseCase.init()
            resourceService.write(_, _, false) >> getSnapshotUseCase.DATA_SYNC_FILE_USAGE_MSG.size()

        when: "usecase is executed"
            mockResource.exists() >> {throw new ApApplicationException("File does not exist")}
            getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "the ap application exception is thrown"
            thrown(ApApplicationException.class)
    }

    def "Exception when getting node configuration snapshot"() {
        given: "init"
            getSnapshotUseCase.init()
            resourceService.write(_, _, false) >> getSnapshotUseCase.DATA_SYNC_FILE_USAGE_MSG.size()

        when: "usecase is executed"
            mockResource.exists() >> {throw new IOException("File does not exist")}
            getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "the ap application exception is thrown"
            thrown(ApApplicationException.class)
    }

    def "Polling file system failed" (){
        given: "init"
            def projectMO = addProjectMo("Project="+PROJECT_NAME)
            def profileMo = createProfileMo(projectMO, PROFILE_NAME)
            getSnapshotUseCase.init()
        and: "Polling remote file system failed "
            resourceService.write(_, _, false) >> 0

        when: "GetSnapshotUseCase is executed"
            def response = getSnapshotUseCase.execute(PROJECT_NAME, PROFILE_FDN, NODE_NAME)

        then: "get no snapshot content"
            response == null
            profileMo.getAttribute("configSnapshotStatus") == "IN_PROGRESS"
            notThrown(Exception)
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
        profileAttributes.put("configSnapshotStatus", "IN_PROGRESS")
        profileAttributes.put("dumpTimeStamp", DEFAULT_DUMP_TIMESTAMP)

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
