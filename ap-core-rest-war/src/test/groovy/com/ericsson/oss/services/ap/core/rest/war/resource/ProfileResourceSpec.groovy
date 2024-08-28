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

package com.ericsson.oss.services.ap.core.rest.war.resource

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.ProfileExistsException
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.Namespace
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder
import com.ericsson.oss.services.ap.common.util.log.MRDefinition

import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP
import com.ericsson.oss.services.ap.core.rest.model.NodeSnapshot
import com.ericsson.oss.services.ap.core.rest.model.profile.Profile
import com.ericsson.oss.services.ap.core.rest.model.request.profile.ProfileRequest
import com.ericsson.oss.services.ap.core.rest.model.request.node.NodeRequest
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ProfileNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ArtifactNotFoundExceptionMapper

import javax.inject.Inject
import javax.ws.rs.core.Response

class ProfileResourceSpec extends CdiSpecification {

    private static final String CIQ_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/ciq/ciq.csv"
    private static final String DETAIL1 = "detail1"
    private static final String FILE_CIQ_CSV = "CIQ_Project1_Profile1_201906041200.csv"
    private static final String FILE_CIQ_XLSX = "CIQ_Project2_Profile2_202101201200.xlsx"

    private static final String GRAPHIC_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/graphic.png"
    private static final String PRODUCT_NUMBER = "R23456"
    private static final String PROFILE_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/profile1.zip"
    private static final String PROFILE_NAME = "profile1"
    private static final String PROJECT_FDN = "Project=Project1"
    private static final String PROJECT_NAME = "project1"
    private static final String PROPERTIES = "{\"prop\":\"prop1\"}"
    private static final String DATA_TYPE = "node-plugin-request-action"
    private static final String NODE_ID1 = "NODE1"
    private static final List<String> NODE_IDS = Arrays.asList(NODE_ID1)
    private static final String RPC_RESPONSE1 = "<ManagedElement xmlns=\"urn:com:ericsson:ecim:ComTop\"><managedElementId>NODE1</managedElementId></ManagedElement>".getBytes("UTF-8")
    private static final SNAPSHOT_NAME = "NodeConfigurationSnapshot.xml"
    private static final SNAPSHOT_STATUS_NOT_STARTED = "NOT_STARTED"
    private static MoData moData
    private static List<MoData> moDataList
    private static String fdn
    private static ProfileRequest profileRequest = new ProfileRequest()
    private static NodeRequest nodeConfigurationRequest = new NodeRequest()
    private static final Long DEFAULT_DUMP_TIMESTAMP = Long.valueOf(0)
    private static final String FILTER_LOCATION = "ericsson/autoprovisioning/projects/project1/profiles/profile1/get-node-config-snapshot.xml"

    @ObjectUnderTest
    ProfileResource profileResource

    @Inject
    @EServiceRef(qualifier = "apcore")
    private AutoProvisioningService service

    @Inject
    private RuntimeConfigurableDps dps

    @Inject
    private DpsQueries dpsQueries

    @Inject
    protected DpsOperations dpsOperations;

    @Inject
    private DataPersistenceService dataPersistenceService

    @MockedImplementation
    private static ModelReader modelReader

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    private MRExecutionRecorder recorder

    def setupSpec() {
        fdn = String.format("Project=%s,Profile=%s", PROJECT_NAME, PROFILE_NAME)
        Map<String, Object> attributes = [
            "profileId"             : PROFILE_NAME,
            "version"               : [
                "productNumber" : PRODUCT_NUMBER,
                "productRelease": "R345556"
            ],
            "properties"            : PROPERTIES,
            "graphicLocation"       : GRAPHIC_LOCATION,
            "profileContentLocation": PROFILE_LOCATION,
            "ciq"                   : [
                "ciqLocation": CIQ_LOCATION
            ],
            "profileStatus"         : [
                "isValid"       : true,
                "profileDetails": [DETAIL1]],
            "dataType" : DATA_TYPE,
            "configSnapshotStatus": "NOT_STARTED",
            "dumpTimeStamp": DEFAULT_DUMP_TIMESTAMP,
            "filterLocation"       : FILTER_LOCATION
        ]
        final Map<String, Object> profileAttributes = new LinkedHashMap<>()
        profileAttributes.putAll(new TreeMap<>(attributes))
        moData = new MoData(fdn, profileAttributes, MoType.CONFIGURATION_PROFILE.toString(), null)
        moDataList = new ArrayList<>()
        moDataList.add(moData)
    }

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
    }

    def "Create a single profile successfully"() {
        given: "The create profile service will return mock MoData"
            service.createProfile(_ as MoData) >> moData

        when: "the create profile endpoint is called"
            profileRequest.name = PROFILE_NAME
            def response = profileResource.createProfile(PROJECT_NAME, profileRequest)

        then: "the status code is 201"
            response.status == 201

        and: "the response contains the created profile"
            Profile profile = (Profile) response.entity
            profile.name == PROFILE_NAME
            profile.version.productNumber == PRODUCT_NUMBER
            profile.properties == PROPERTIES
            profile.graphicLocation == GRAPHIC_LOCATION
            profile.profileContentLocation == PROFILE_LOCATION
            profile.ciq.ciqLocation == CIQ_LOCATION
            profile.status.details[0] == DETAIL1
            profile.filterLocation == FILTER_LOCATION

        and: "the model reader is called once"
            1 * modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString())
    }

    def "Profile already exists exception is thrown when creating a profile"() {
        given: "The create profile service throws profile exists exception"
            service.createProfile(_ as MoData) >> { throw new ProfileExistsException(PROJECT_NAME, PROFILE_NAME) }

        when: "the create profile endpoint is called"
            profileResource.createProfile(PROJECT_NAME, profileRequest)

        then: "the profile exists exception is thrown"
            thrown(ProfileExistsException.class)
    }

    def "Project does not exist exception is thrown when creating a profile"() {
        given: "the create profile service throws project not found exception"
            service.createProfile(_ as MoData) >> { throw new ProjectNotFoundException() }

        when: "the create profile endpoint is called"
            profileResource.createProfile(PROJECT_NAME, profileRequest)

        then: "the project not found exception is thrown"
            thrown(ProjectNotFoundException.class)
    }

    def "Delete a profile successfully"() {
        given: "a project and profile exists"
            service.deleteProfile(_ as String, _ as String) >> null

        when: "the delete profile endpoint is called"
            def response = profileResource.deleteProfile(PROJECT_NAME, PROFILE_NAME)

        then: "the status code should be 204"
            response.status == 204

        and: "the delete profile usecase is called once"
            1 * service.deleteProfile(PROJECT_NAME, PROFILE_NAME)
    }

    def "Profile does not exist exception when deleting a profile"() {
        given: "the delete profile service throws profile not found exception"
            service.deleteProfile(_ as String, _ as String) >> { throw new ProfileNotFoundException() }

        when: "the delete profile endpoint is called"
            profileResource.deleteProfile(PROJECT_NAME, PROFILE_NAME)

        then: "the profile not found exception is thrown"
            thrown(ProfileNotFoundException.class)
    }

    def "Export profile CIQ CSV file successfully"() {
        given: "the export profile CIQ service returns the CSV file name"
            service.exportProfileCIQ(_ as String, _ as String) >> FILE_CIQ_CSV

        when: "the export CIQ endpoint is called"
            Response response = profileResource.exportCIQ("Project1", "Profile1", PROJECT_FDN)

        then: "the status code should be 200 and Content-Type not set for CSV file"
            response.status == 200
            !response.getMetadata().containsKey("Content-Type")
    }

    def "Export profile CIQ XLSX file successfully"() {
        given: "the export profile CIQ service returns the XLSX file name"
            service.exportProfileCIQ(_ as String, _ as String) >> FILE_CIQ_XLSX

        when: "the export CIQ endpoint is called"
            Response response = profileResource.exportCIQ("Project2", "Profile2", PROJECT_FDN)

        then: "the status code should be 200 and Content-Type spreadsheetml.sheet"
            response.status == 200
            response.getMetadata().get("Content-Type").contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    def "Export profile CIQ throws exception when CIQ file is not found"() {
        given: "the export profile CIQ service throws AP application exception"
            service.exportProfileCIQ(_ as String, _ as String) >> {throw new ApApplicationException("No CIQ found to export")}

        when: "the export CIQ endpoint is called"
            profileResource.exportCIQ("Project1", "Profile1", PROJECT_FDN)

        then: "AP service exception is thrown"
            thrown(ApServiceException.class)
    }

    def "View profile successfully"() {
        given: "create view profile based on profile type"
        service.viewProfilesByProfileType(_ as String, _ as String) >> moDataList

        when: "the view profile by profile type is called"
        Response response = profileResource.viewProfilesByProfileType(PROJECT_NAME,PROJECT_FDN,DATA_TYPE)

        then: "the status code should be 200"
        response.status == 200
    }

    def "Trigger dumping node configuration snapshot successfully"() {
        given: "Trigger dumping node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "NOT_STARTED")
            profileAttributes.put("dumpTimeStamp", DEFAULT_DUMP_TIMESTAMP)
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            service.snapshotDump(_ as String, _ as String, _ as String, _ as String) >> null

        when: "the view profile by profile type is called"
            nodeConfigurationRequest.nodeIds = NODE_IDS
            Response response = profileResource.triggerNodeConfigurationDump(PROJECT_NAME,PROFILE_NAME,nodeConfigurationRequest)

        then: "the status code should be 200 OK"
            1 * recorder.recordMRExecution(MRDefinition.AP_ENHANCED_EXPANSION)
            response.status == 200
    }

    def "Trigger dumping node configuration snapshot throw exception"() {
        given: "Trigger dumping node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "FAILED")
            profileAttributes.put("dumpTimeStamp", DEFAULT_DUMP_TIMESTAMP)
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            service.dumpSnapshot(_ as String, _ as String, _ as String<>, _ as String) >> {throw new ApApplicationException("Node does not exist")}
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "Trigger dumping node configuration snapshot is called"
            nodeConfigurationRequest.nodeIds = NODE_IDS
            Response response = profileResource.triggerNodeConfigurationDump(PROJECT_NAME,PROFILE_NAME,nodeConfigurationRequest)

        then: "the status code should be 400"
            response.status == 400
    }

    def "Fail to trigger dumping node configuration snapshot because of multiple trigger request"() {
        given: "Trigger dumping node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "IN_PROGRESS")
            profileAttributes.put("dumpTimeStamp", System.currentTimeMillis())
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "Trigger dumping node configuration snapshot is called"
            nodeConfigurationRequest.nodeIds = NODE_IDS
            Response response = profileResource.triggerNodeConfigurationDump(PROJECT_NAME,PROFILE_NAME,nodeConfigurationRequest)

        then: "the status code should be 400"
            response.status == 400
    }

    def "Retrigger dumping node configuration snapshot successfully when timestamp checking timeout"() {
        given: "Trigger dumping node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "IN_PROGRESS")
            profileAttributes.put("dumpTimeStamp", DEFAULT_DUMP_TIMESTAMP)
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            service.dumpSnapshot(_ as String, _ as String, _ as String, _ as String) >> null

        when: "Trigger dumping node configuration snapshot is called"
            nodeConfigurationRequest.nodeIds = NODE_IDS
            Response response = profileResource.triggerNodeConfigurationDump(PROJECT_NAME,PROFILE_NAME,nodeConfigurationRequest)

        then: "the status code should be 200 OK"
            response.status == 200
    }

    def "Retrigger dumping node configuration snapshot failure when timestamp checking failed"() {
        given: "Trigger dumping node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "IN_PROGRESS")
            profileAttributes.put("dumpTimeStamp", null)
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "Trigger dumping node configuration snapshot is called"
            nodeConfigurationRequest.nodeIds = NODE_IDS
            Response response = profileResource.triggerNodeConfigurationDump(PROJECT_NAME,PROFILE_NAME,nodeConfigurationRequest)

        then: "the status code should be 400"
            response.status == 400
    }

    def "Get the node configuration snapshot successfully"() {
        given: "Get node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "COMPLETED")
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            String snapshotContent = Base64.getEncoder().encodeToString(RPC_RESPONSE1.getBytes("UTF-8"))
            service.getSnapshot(_ as String, _ as String, NODE_ID1) >> snapshotContent

        when: "Getting node configuration snapshot is called"
            nodeConfigurationRequest.nodeIds = NODE_IDS
            NodeSnapshot snapshot = new NodeSnapshot(SNAPSHOT_NAME, snapshotContent)
            Response response = profileResource.getNodeConfigurationSnapshot(PROJECT_NAME,PROFILE_NAME,NODE_ID1)

        then: "the status code should be 200"
            response.status == 200
            response.entity.getName() == SNAPSHOT_NAME
            response.entity.getContent() == snapshotContent
    }

    def "Fail to get the node configuration snapshot when fail to read snapshot content"() {
        given: "Get node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "COMPLETED")
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            service.getSnapshot(_ as String, _ as String, NODE_ID1) >> null

        when: "Getting node configuration snapshot is called"
            Response response = profileResource.getNodeConfigurationSnapshot(PROJECT_NAME,PROFILE_NAME,NODE_ID1)

        then: "the status code should be 202 ACCEPTED"
            response.status == 202
    }

    def "Fail to get the node configuration snapshot when snapshot dumping is ongoing"() {
        given: "Get node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "IN_PROGRESS")
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)

        when: "Getting node configuration snapshot is called"
            Response response = profileResource.getNodeConfigurationSnapshot(PROJECT_NAME,PROFILE_NAME,NODE_ID1)

        then: "the status code should be 202 ACCEPTED"
            response.status == 202
    }

    def "Fail to get the node configuration snapshot when snapshot dumping failed"() {
        given: "Get node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "FAILED")
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "Getting node configuration snapshot is called"
            Response response = profileResource.getNodeConfigurationSnapshot(PROJECT_NAME,PROFILE_NAME,NODE_ID1)

        then: "the status code should be 400"
            response.status == 400
    }

    def "Get the node configuration snapshot throw exception when get snapshot exception"() {
        given: "Get node configuration snapshot based on nodeId"
            final ManagedObject projectMO = addProjectMo("Project="+PROJECT_NAME)
            final Map<String, Object> profileAttributes = new HashMap<>()
            profileAttributes.put("configSnapshotStatus", "COMPLETED")
            createProfileMo(projectMO, PROFILE_NAME, profileAttributes)
            service.getSnapshot(_ as String, _ as String, NODE_ID1) >> {throw new ArtifactNotFoundException("File does not exist")}
            exceptionMapperFactory.find(_) >> new ArtifactNotFoundExceptionMapper()

        when: "Getting node configuration snapshot is called"
            Response response = profileResource.getNodeConfigurationSnapshot(PROJECT_NAME,PROFILE_NAME,NODE_ID1)

        then: "the status code should be 403"
            response.status == 403
    }

    def "Get the node configuration snapshot throw exception when profile does not exist"() {
        given: "Get node configuration snapshot based on nodeId"
            service.getSnapshot(_ as String, _ as String, NODE_ID1) >> null
            exceptionMapperFactory.find(*_) >> new ProfileNotFoundExceptionMapper()

        when: "Getting node configuration snapshot is called"
            Response response = profileResource.getNodeConfigurationSnapshot(PROJECT_NAME,PROFILE_NAME,NODE_ID1)

        then: "the status code should be 404"
            response.status == 404
            notThrown(ProfileNotFoundException)
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

    private ManagedObject createProfileMo(final ManagedObject parentMo, final String profileName, final Map<String, Object> profileAttributes) {
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
