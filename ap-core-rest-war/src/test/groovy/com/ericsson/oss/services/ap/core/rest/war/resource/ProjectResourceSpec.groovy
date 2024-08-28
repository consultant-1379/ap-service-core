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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.InvalidArgumentsException
import com.ericsson.oss.services.ap.api.exception.ProjectExistsException
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException
import com.ericsson.oss.services.ap.api.exception.ValidationException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus
import com.ericsson.oss.services.ap.api.status.NodeStatus
import com.ericsson.oss.services.ap.api.status.StatusEntry
import com.ericsson.oss.services.ap.common.util.log.MRDefinition
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver
import com.ericsson.oss.services.ap.core.rest.model.Project
import com.ericsson.oss.services.ap.core.rest.model.request.DeleteProjectRequest
import com.ericsson.oss.services.ap.core.rest.model.request.ProjectRequest
import com.ericsson.oss.services.ap.core.rest.model.request.order.configurations.Configuration
import com.ericsson.oss.services.ap.core.rest.model.request.order.configurations.OrderNodeConfigurationsRequest
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ProjectExistsExceptionMapper
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ProjectNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ValidationExceptionMapper

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.Paths

class ProjectResourceSpec extends CdiSpecification {

    private static final String DESCRIPTION = "some description"
    private static final String CREATOR = "john"
    private static final String CREATION_DATE = "2018-08-17 14:45:34"
    private static final String PROJECT_NAME = "Project1"

    private static List<ApNodeGroupStatus> apNodeGroupStatuses  = new ArrayList<>()
    private static projectRequest = new ProjectRequest()
    private static MoData moData
    private static OrderNodeConfigurationsRequest orderNodeConfigurationsRequest = new OrderNodeConfigurationsRequest()
    private static Configuration configuration = new Configuration()
    private static Configuration configurationTwo = new Configuration()

    @ObjectUnderTest
    ProjectResource projectResource

    @Inject
    @EServiceRef(qualifier = "apcore")
    private AutoProvisioningService service

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    private MRExecutionRecorder recorder

    @MockedImplementation
    private ArgumentResolver argumentResolver

    def setupSpec() {
        projectRequest.name = PROJECT_NAME
        projectRequest.description = DESCRIPTION
        projectRequest.creator = CREATOR

        final Map<String, Object> attributes = ["projectName" : PROJECT_NAME,
            "description" : DESCRIPTION,
            "creationDate": CREATION_DATE,
            "creator"     : CREATOR]

        moData = new MoData("Project=" + PROJECT_NAME,
                        attributes, null, null)

        def content = readFileEncoded("Files.zip")
        configuration.setName("Node1")
        configuration.setContent(content)
        configurationTwo.setName("Node2")
        configurationTwo.setContent(content)
        List<Configuration> configs = new ArrayList<>()
        configs.add(configuration)
        configs.add(configurationTwo)
        orderNodeConfigurationsRequest.setConfigurations(configs)

        List<StatusEntry> statusEntries = new ArrayList<>()
        statusEntries.add(new StatusEntry("Task1", "Progress", "2018-08-30 10:58:10.99", "Additional Info"))
        List<NodeStatus> nodeStatuses = new ArrayList<>()
        nodeStatuses.add(new NodeStatus("Node01", "Project01", statusEntries, "READY_FOR_ORDER"))
        apNodeGroupStatuses.add(new ApNodeGroupStatus("Project01", nodeStatuses, ApNodeGroupStatus.ApNodeGroupType.PROJECT))

    }

    def "Create a single project successfully"() {

        given: "the create project service will return a managed object data"
            service.createProject(projectRequest.name, projectRequest.creator, projectRequest.description) >> moData

        when: "the create project endpoint is called"
            def response = projectResource.createProject(projectRequest)

        then: "the status code should be 201"
            0 * recorder.recordMRExecution(_)
            response.status == 201

        and: "the response contains the created project"
            def project = ((Project) response.entity)
            project.id == PROJECT_NAME
            project.creationDate == CREATION_DATE
            project.description == DESCRIPTION
            project.creator == CREATOR
    }

    def "Get all projects successfully using the properties filter"() {

        given: "the service returns all projects"
            service.viewAllProjects() >> [moData]

        when: "the all projects endpoint is called"
            def response = projectResource.queryAllProjects("properties")

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should be reported in the payload"
            def project = ((Project) response.entity.projects[0])
            project.id == PROJECT_NAME
            project.creationDate == CREATION_DATE
            project.description == DESCRIPTION
            project.creator == CREATOR
    }

    def "Get the status of all projects successfully"() {

        given: "the service returns the status of all projects"
            service.statusAllProjects() >> { apNodeGroupStatuses }

        when: "the status for all projects endpoint is called"
            def response = projectResource.queryAllProjects("status")

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should be reported in the payload"
            response.entity[0].projectId == "Project01"
            response.entity[0].numberOfNodes == 1
            response.entity[0].integrationPhaseSummary.cancelled == 0
            response.entity[0].integrationPhaseSummary.inProgress == 1
            response.entity[0].integrationPhaseSummary.failed == 0
            response.entity[0].integrationPhaseSummary.successful == 0
            response.entity[0].integrationPhaseSummary.suspended == 0
    }

    def "Invalid arguments exception when an invalid query parameter is given"() {

        when: "the status for all projects endpoint is called with an invalid value"
            projectResource.queryAllProjects("status10")

        then: "Invalid arguments exception is thrown"
            thrown(InvalidArgumentsException.class)
    }

    def "Service exception error when getting the status of all projects"() {

        given: "the service throws AP service exception"
            service.statusAllProjects() >> { throw new ApServiceException("Service exception occurred") }

        when: "the status for all projects endpoint is called"
            projectResource.queryAllProjects("status")

        then: "AP service exception is thrown"
            thrown(ApServiceException.class)
    }

    def "Get project properties successfully"() {

        given: "the service returns the project properties"
            service.viewProject(*_) >> [moData]
            argumentResolver.resolveFdn(*_) >> null

        when: "the project properties endpoint is called"
            def response = projectResource.queryProject("Project01", "properties")

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should be reported in the payload"
            response.entity.id == PROJECT_NAME
            response.entity.creationDate == CREATION_DATE
            response.entity.description == DESCRIPTION
            response.entity.creator == CREATOR
    }

    def "Get project status successfully"() {

        given: "the service returns the projects status"
            service.statusProject(*_) >> apNodeGroupStatuses.get(0)
            argumentResolver.resolveFdn(*_) >> null

        when: "the projects status endpoint is called"
            def response = projectResource.queryProject("Project01", "status")

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should be reported in the payload"
            response.entity.id == "Project01"
            response.entity.nodeSummary[0].id == "Node01"
            response.entity.nodeSummary[0].status == "In Progress"
            response.entity.nodeSummary[0].state == "Ready for Order"
    }

    def "Project already exists exception when creating a project"() {

        given: "the service throws project exists exception"
            exceptionMapperFactory.find(_) >> new ProjectExistsExceptionMapper()
            service.createProject(projectRequest.name, projectRequest.creator, projectRequest.description) >> {throw new ProjectExistsException(PROJECT_NAME)}

        when: "the create project endpoint is called"
            def response = projectResource.createProject(projectRequest)

        then: "the status code should be 409"
            response.status == 409

        and: "the response contains the error details"
            response.entity.errorTitle == String.format("Project \"%s\" already exists.", PROJECT_NAME)
            response.entity.errorBody == "Suggested Solution : Please choose a different project name"
    }

    def "Order node configurations successfully"() {

        given: "the order project service returns the project name"
            service.orderProject("project.zip", _ as Byte, true) >> PROJECT_NAME

        when: "the order endpoint is called"
            def response = projectResource.order(PROJECT_NAME, orderNodeConfigurationsRequest, validation)

        then: "the status code should be 201"
            1 * recorder.recordMRExecution(MRDefinition.AP_INTEGRATED_PROVISIONING)
            response.status == 201
        where:
            validation | _
            true       | _
            false      | _
    }

    def "Project not found exception when ordering node configurations"() {

        given: "the order project service throws project not found exception"
            exceptionMapperFactory.find(_) >> new ProjectNotFoundExceptionMapper()
            service.orderProject("project.zip", _, true) >> { throw new ProjectNotFoundException() }

        when: "the order endpoint is called"
            def response = projectResource.order(PROJECT_NAME, orderNodeConfigurationsRequest, true)

        then: "the status code should be 404"
            response.status == 404

        and: "the response contains the error details"
            response.entity.errorTitle == "Project does not exist."
            response.entity.errorBody == "Suggested Solution : Perform action with a valid project name."
    }

    def "Validation exception when ordering node configurations"() {

        given: "the order project service throws validation exception"
            exceptionMapperFactory.find(_) >> new ValidationExceptionMapper()
            service.orderProject("project.zip", _, true) >> { throw new ValidationException("Validation Error") }

        when: "the order endpoint is called"
            def response = projectResource.order(PROJECT_NAME, orderNodeConfigurationsRequest, true)

        then: "the status code should be 417"
            response.status == 417

        and: "the response contains the error details"
            response.entity.errorTitle == "Error(s) found validating project."
            response.entity.errorBody == "Suggested Solution : Fix error(s) and try again."
    }

    def "Delete a single project successfully"() {

        when: "the delete endpoint is called to delete a single project"
            def response = projectResource.deleteProjects(new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ["myID"]))

        then: "the corresponding service method is called once"
            1 * service.deleteProject("Project=myID", false)

        and: "the status code should be 204"
            0 * recorder.recordMRExecution(_)
            response.status == 204
    }

    def "Delete multiple projects successfully"() {

        when: "the delete endpoint is called to delete 3 projects"
            def response = projectResource.deleteProjects(new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ["myID-1", "myID-2", "myID-3"]))

        then: "the corresponding service method is called once for each project ID"
            1 * service.deleteProject("Project=myID-1", false)
            1 * service.deleteProject("Project=myID-2", false)
            1 * service.deleteProject("Project=myID-3", false)

        and: "the status code should be 204"
            response.status == 204
    }

    def "Delete duplicated projects successfully"() {

        when: "the delete endpoint is called to delete 2 projects with the same IDs"
            def response = projectResource.deleteProjects(new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ["myID", "myID"]))

        then: "the corresponding service method was called only once"
            1 * service.deleteProject("Project=myID", false)

        and: "the status code should be 204"
            response.status == 204
    }

    def "Delete using invalid project IDs"() {

        given: "the exception mapper is mocked as @Any injection points are not supported by the test framework"
            exceptionMapperFactory.find(*_) >> new ProjectNotFoundExceptionMapper()

        when: "the delete project service throws an exception when called"
            service.deleteProject(*_) >> { throw new ProjectNotFoundException("Project not found.") }

        and: "the delete project endpoint is called to delete a project"
            def response = projectResource.deleteProjects(new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ["invalid"]))

        then: "the status code should be 500"
            response.status == 500

        and: "the ID and error message should be reported in the payload"
            response.entity?.size() == 1
            response.entity[0].id == "invalid"
            response.entity[0].errorMessage == "Project does not exist."
    }

    def "Delete valid and invalid projects"() {

        given: "the exception mapper is mocked as @Any injection points are not supported by the test framework"
            exceptionMapperFactory.find(*_) >> new ProjectNotFoundExceptionMapper()

        when: "the delete endpoint is called with a valid and invalid project IDs"
            def response = projectResource.deleteProjects(new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ["myID", "invalid"]))

        then: "the project with \"myID\" ID should be deleted successfully"
            1 * service.deleteProject("Project=myID", false)

        then: "the project with \"invalid\" ID should fail"
            1 * service.deleteProject("Project=invalid", false) >> { throw new ProjectNotFoundException("Project not found.") }

        then: "status code should be 500"
            response.status == 500

        and: "only the id \"invalid\" should be reported with error"
            response.entity?.size() == 1
            response.entity[0].id == "invalid"
            response.entity[0].errorMessage == "Project does not exist."
    }

    def "Delete a single project successfully using singular endpoint"() {

        when: "the delete endpoint is called to delete a single project"
             def response = projectResource.deleteProject("Project1")

        then: "the corresponding service method is called once"
             1 * service.deleteProject("Project=Project1", false)

        and: "the status code should be 204"
             0 * recorder.recordMRExecution(_)
             response.status == 204
    }

    def "Delete invalid project using singular endpoint"() {

        given: "the exception mapper is mocked as @Any injection points are not supported by the test framework"
            exceptionMapperFactory.find(*_) >> new ProjectNotFoundExceptionMapper()

        when: "the delete project service throws an exception when called"
            service.deleteProject(*_) >> { throw new ProjectNotFoundException("Project not found.") }

        and: "the delete project endpoint is called to delete a project"
            def response = projectResource.deleteProject("Invalid")

        then: "the status code should be 404"
            response.status == 404

        and: "the ID and error message should be reported in the payload"
            response.entity.errorTitle == "Project does not exist."
            response.entity.errorBody == "Suggested Solution : Perform action with a valid project name."
    }

    private static String readFileEncoded(final String fileName) {
        try {
            return Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get("src/test/resources/files/" + fileName)))
        } catch (final IOException e) {
            throw new IOException(e.getMessage())
        }
    }
}
