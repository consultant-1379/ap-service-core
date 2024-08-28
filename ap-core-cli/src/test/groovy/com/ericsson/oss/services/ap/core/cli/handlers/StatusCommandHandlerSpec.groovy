/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus
import com.ericsson.oss.services.ap.api.status.NodeStatus
import com.ericsson.oss.services.ap.api.status.StatusEntry
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor
import com.ericsson.oss.services.ap.common.usecase.CommandLogName
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.NodeNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ProjectNotFoundExceptionMapper
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
import com.ericsson.oss.services.scriptengine.spi.dtos.HeaderRowDto
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus
import com.ericsson.oss.services.scriptengine.spi.dtos.RowCell
import com.ericsson.oss.services.scriptengine.spi.dtos.RowDto
import org.apache.commons.cli.CommandLine
import org.mockito.internal.util.reflection.Whitebox

import javax.inject.Inject

class StatusCommandHandlerSpec extends CdiSpecification {

    private static final String AP_STATUS_COMMAND = "ap status "
    private static final String AP_STATUS_COMMAND_FOR_PROJECT = "ap status -p "
    private static final String AP_STATUS_COMMAND_FOR_NODE = "ap status -n "
    private static final String AP_STATUS_COMMAND_FOR_DEPLOYMENT = "ap status -d "
    private static final String SECOND_PROJECT_NAME = PROJECT_NAME + "2"
    private static final String DEPLOYMENT_NAME = "deployment1"
    public static final String READY_FOR_ORDER = "Ready for Order"
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    private final StatusEntry statusEntry1 = new StatusEntry("task1", "status1", "time1", "additionalText1")
    private final StatusEntry statusEntry2 = new StatusEntry("task2", "status2", "time2", "additionalText2")

    @MockedImplementation
    private ArgumentResolver argumentResolver

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    DpsQueries dpsQueries

    @MockedImplementation
    DpsQueryExecutor dpsQueryExecutor

    @Inject
    NodeNotFoundExceptionMapper nodeNotFoundExceptionMapper

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @ObjectUnderTest
    private StatusCommandHandler statusCommandHandler

    private ApNodeGroupStatus deploymentStatus
    private List<NodeStatus> nodeStatusList
    private List<StatusEntry> tasksStatus

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
        nodeStatusList = new ArrayList<>()
        tasksStatus = new ArrayList<>()
        tasksStatus.add(statusEntry1)
        tasksStatus.add(statusEntry2)
        nodeStatusList.add(new NodeStatus(NODE_NAME, PROJECT_NAME, tasksStatus, "READY_FOR_ORDER"))
        nodeStatusList.add(new NodeStatus(NODE_NAME, PROJECT_NAME, tasksStatus, "READY_FOR_ORDER"))
        nodeStatusList.add(new NodeStatus(NODE_NAME, PROJECT_NAME, tasksStatus, "ORDER_COMPLETED"))
    }

    def "Running node status command with invalid option should have syntax error" () {
        given: "Command with invalid syntax"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND + " -nn " + NODE_NAME, null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "status")
    }

    def "Running node status command with invalid extra options should have syntax error" () {
        given: "Command with invalid syntax - additional extra option"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_NODE + NODE_NAME + " affsfs", null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "status")
    }

    def "Running project status command with invalid extra options should have syntax error" () {
        given: "Command with invalid syntax - additional extra option"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_PROJECT + PROJECT_NAME + " abcd", null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "status")
    }

    def "Running node status command that doesn't exist should return project not found response" () {
        given: "Command created to get status of a node. When fdn is accessed NodeNotFoundException should be thrown"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_NODE + NODE_NAME, null)
        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> { throw new NodeNotFoundException() }
        exceptionMapperFactory.find(_) >> nodeNotFoundExceptionMapper
        dpsQueries.findMoByName(_, _, _) >> dpsQueryExecutor
        dpsQueryExecutor.executeCount() >> 0
        Whitebox.setInternalState(nodeNotFoundExceptionMapper, "dpsQueries", dpsQueries)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be node not found"
        CommandResponseValidatorTest.verifyNodeNotFoundError(actualCommandResponse)
    }

    def "Successful node status response should contain all required data in correct format" () {
        given: "Command created to get node status"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_NODE + NODE_NAME, null)
        nodeStatusList.add(new NodeStatus(NODE_NAME, PROJECT_NAME, tasksStatus, "READY_FOR_ORDER"))
        final NodeStatus nodeStatus = nodeStatusList.get(0)

        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> NODE_FDN
        autoProvisioningCore.statusNode(NODE_FDN) >> nodeStatus

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)
        actualCommandResponse.addEmptyLine()

        then: "returned commandResponseDto should be of the correct format- 3 nameValueDtos, 1 empty lineDto, and 1 tableDto (for task statuses)"
        final ResponseDto commandResultDto = actualCommandResponse.getResponseDto()
        final List<AbstractDto> allDtos = commandResultDto.getElements()
        allDtos.size() == 11

        and: "response dtos contain data in correct format"
        allDtos.get(0).toString().matches("(Node Name).*(" + NODE_NAME + ").*")
        allDtos.get(1).toString().matches("(Project Name).*(" + PROJECT_NAME + ").*")
        allDtos.get(2).toString().matches("(State).*(Ready for Order).*")

        and: "response messages match expected status entry"
        final RowDto rowDto = (RowDto) allDtos.get(5)
        final List<RowCell> cellList = rowDto.getElements()

        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "")
        cellList.get(0).getValue() == statusEntry1.getTaskName()
        cellList.get(1).getValue() == statusEntry1.getTaskProgress()
        cellList.get(2).getValue() == statusEntry1.getTimeStamp()
        cellList.get(3).getValue() == statusEntry1.getAdditionalInfo()
    }

    def "Running project status command for project that doesn't exist should should return project not found response" () {
        given: "Command created to get status of a project. When fdn is accessed ProjectNotFoundException should be thrown"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_PROJECT + PROJECT_NAME, null)
        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> { throw new ProjectNotFoundException() }
        exceptionMapperFactory.find(_ as ProjectNotFoundException) >> new ProjectNotFoundExceptionMapper()

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then:"Response should be project not found"
        CommandResponseValidatorTest.verifyProjectNotFoundError(actualCommandResponse)
    }

    def "Project status command fails with correct message when Service Exception is thrown from status project" () {
        given: "Command created to get status of a project. Status Project returns service exception"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_PROJECT + PROJECT_NAME, null)
        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> PROJECT_FDN
        autoProvisioningCore.statusProject(PROJECT_FDN) >> { throw new ApServiceException(EXCEPTION_MESSAGE) }
        exceptionMapperFactory.find(_ as ApServiceException) >> new ApServiceExceptionMapper()

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be as expected"
        CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
        CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "Status command fails with correct message when ApServiceException is thrown from status all projects" () {
        given: "Command created to get status of a project. Status all Projects returns service exception"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND, null)
        autoProvisioningCore.statusAllProjects() >> { throw new ApServiceException(EXCEPTION_MESSAGE) }
        exceptionMapperFactory.find(_ as ApServiceException) >> new ApServiceExceptionMapper()

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be as expected"
        CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
        CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "Status command response has correct message when status of all projects called and no projects exist" () {
        given: "Command created to get status of all projects. Returns empty set"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND, null)
        autoProvisioningCore.statusAllProjects() >> Collections.<ApNodeGroupStatus>emptyList()

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be as expected"
        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "0 project(s) found")

    }

    def "Command response is correct structure when status all projects called and one project exists" () {
        given: "Command created to get status of all projects"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND, null)
        final List<ApNodeGroupStatus> projectStatuses = new ArrayList<>()

        final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME, nodeStatusList)
        projectStatuses.add(projectStatus)
        autoProvisioningCore.statusAllProjects() >> projectStatuses

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be as expected"
        final List<AbstractDto> allDtos = actualCommandResponse.getResponseDto().getElements()

        and: "Status header is as expected"
        verifyAllProjectStatusesHeader(allDtos)

        and: "Table content is as expected"
        verifyAllProjectTableContent((String[]) [PROJECT_NAME, "3", "3", "0", "0", "0", "0" ].toArray(), 1, allDtos)

        and: "Message is as expected"
        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "1 project(s) found")
    }

    def "Status command response is the correct structure when status for all projects is called and two projects exist" () {
        given: "Command for status all projects should return two project status objects"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND, null)
        final List<ApNodeGroupStatus> projectStatuses = new ArrayList<>()

        final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME, nodeStatusList)
        projectStatuses.add(projectStatus)
        final List<NodeStatus> nodeStatusesTwo = new ArrayList<>()

        final ApNodeGroupStatus projectStatusTwo = ApNodeGroupStatus.getProjectApNodeGroupStatus(SECOND_PROJECT_NAME, nodeStatusesTwo)
        projectStatuses.add(projectStatusTwo)
        autoProvisioningCore.statusAllProjects() >> projectStatuses

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be as expected"
        final List<AbstractDto> abstractDtos = actualCommandResponse.getResponseDto().getElements()

        and: "Status header is as expected"
        verifyAllProjectStatusesHeader(abstractDtos)

        and: "Table content is as expected"
        verifyAllProjectTableContent((String[]) [PROJECT_NAME, "3", "3", "0", "0", "0", "0" ].toArray(), 1, abstractDtos)
        verifyAllProjectTableContent((String[]) [SECOND_PROJECT_NAME, "0", "0", "0", "0", "0", "0" ].toArray(), 1, abstractDtos)

        and: "Message is as expected"
        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "2 project(s) found")
    }

    def "Command response is correct structure when running command for status of a project with multiple nodes" () {

        given: "Command for status project should return two project status object with nodes"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_PROJECT + PROJECT_NAME, null)
        final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME, nodeStatusList)
        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> PROJECT_FDN
        autoProvisioningCore.statusProject(PROJECT_FDN) >> projectStatus

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response should be as expected"
        final List<AbstractDto> allDtos = actualCommandResponse.getResponseDto().getElements()

        and: "project header is correct structure"
        final HeaderRowDto headerRow = (HeaderRowDto) allDtos.get(0)
        final List<RowCell> headerElements = headerRow.getElements()
        "Project Name" == headerElements.get(0).getValue()
        "Node Quantity" == headerElements.get(1).getValue()
        "In Progress" == headerElements.get(2).getValue()
        "Suspended" == headerElements.get(3).getValue()
        "Successful" == headerElements.get(4).getValue()
        "Failed" == headerElements.get(5).getValue()
        "Cancelled" == headerElements.get(6).getValue()

        and: "Project table content is as expected"
        final RowDto rowDto = (RowDto) allDtos.get(1)
        PROJECT_NAME == rowDto.getElements().get(0).getValue()
        "3" == rowDto.getElements().get(1).getValue()
        "3" == rowDto.getElements().get(2).getValue()
        "0" == rowDto.getElements().get(3).getValue()
        "0" == rowDto.getElements().get(4).getValue()

        and: "Node table header content is as expected"
        final HeaderRowDto nodeTableHeaderRowDto = (HeaderRowDto) allDtos.get(2)
        "Node Name" == nodeTableHeaderRowDto.getElements().get(0).getValue()
        "Status" == nodeTableHeaderRowDto.getElements().get(1).getValue()
        "State" == nodeTableHeaderRowDto.getElements().get(2).getValue()

        and: "Node table content is as expected"
        final RowDto rowDtoEight = (RowDto) allDtos.get(3)
        NODE_NAME == rowDtoEight.getElements().get(0).getValue()
        READY_FOR_ORDER == rowDtoEight.getElements().get(2).getValue()

        final RowDto rowDtoNine = (RowDto) allDtos.get(4);
        NODE_NAME == rowDtoNine.getElements().get(0).getValue()
        READY_FOR_ORDER == rowDtoNine.getElements().get(2).getValue()

        final RowDto rowDtoTen = (RowDto) allDtos.get(5)
        NODE_NAME == rowDtoTen.getElements().get(0).getValue()
        "Order Completed" == rowDtoTen.getElements().get(2).getValue()
    }

    def "Command response is successful when valid deployment name given"() {

        given: "node with deployment exists"
        deploymentStatus = ApNodeGroupStatus.getDeploymentApNodeGroupStatus(DEPLOYMENT_NAME, nodeStatusList)
        autoProvisioningCore.statusDeployment(DEPLOYMENT_NAME) >> deploymentStatus

        when: "An operator executes the ap download -ciq file: command"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_DEPLOYMENT+" " + DEPLOYMENT_NAME, null)
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response is successful"
        0 * argumentResolver.resolveFdn(null, null)
        ResponseStatus.SUCCESS == actualCommandResponse.getStatusCode()
    }

    def "Command response is failed when invalid deployment name given"() {

        given: "node with deployment exists"
        deploymentStatus = ApNodeGroupStatus.getDeploymentApNodeGroupStatus(DEPLOYMENT_NAME, Collections.EMPTY_LIST)
        autoProvisioningCore.statusDeployment(DEPLOYMENT_NAME) >> deploymentStatus

        when: "An operator executes the ap download -ciq file: command"
        final CliCommand cliCommand = new CliCommand(AP_STATUS_COMMAND_FOR_DEPLOYMENT+" "+ DEPLOYMENT_NAME, null)
        final CommandResponseDto actualCommandResponse = statusCommandHandler.processCommand(cliCommand)

        then: "Response is error"
        ResponseStatus.COMMAND_EXECUTION_ERROR == actualCommandResponse.getStatusCode()
    }

    private void verifyAllProjectStatusesHeader(final List<AbstractDto> allDtos) {
        final HeaderRowDto headerRow = (HeaderRowDto) allDtos.get(0)
        final List<RowCell> headerElements = headerRow.getElements()
        "Project Name" == headerElements.get(0).getValue()
        "Node Quantity" == headerElements.get(1).getValue()
        "In Progress" == headerElements.get(2).getValue()
        "Suspended" == headerElements.get(3).getValue()
        "Successful" == headerElements.get(4).getValue()
        "Failed" == headerElements.get(5).getValue()
        "Cancelled" == headerElements.get(6).getValue()
    }

    private void verifyAllProjectTableContent(final String[] expectedValues, final int dtoPosition, final List<AbstractDto> allDtos) {
        final RowDto statusRow = (RowDto) allDtos.get(dtoPosition)
        final List<RowCell> rowElements = statusRow.getElements()
        for (int i = 0; i < rowElements.size(); i++) {
            expectedValues[i] == rowElements.get(i).getValue()
        }
    }
}
