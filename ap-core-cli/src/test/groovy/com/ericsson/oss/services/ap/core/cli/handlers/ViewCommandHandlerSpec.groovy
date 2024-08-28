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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.common.usecase.CommandLogName
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.response.ViewResponseDtoBuilder
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto

import org.apache.commons.cli.CommandLine

/**
 * Unit tests for {@link ViewCommandHandler}.
 */
class ViewCommandHandlerSpec extends CdiSpecification {

    private static final ResponseDto DEFAULT_RESPONSE_DTO = new ResponseDto(Collections.<MoData> emptyList())
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private ArgumentResolver argumentResolver; // NOPMD

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @MockedImplementation
    private ViewResponseDtoBuilder viewResponseDtoBuilder // NOPMD

    @ObjectUnderTest
    private ViewCommandHandler viewCommandHandler

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
    }

    def "Running view command for projects when no projects are present should return correct message"() {

        given: "Command for view all projects. View should return empty list"
        autoProvisioningCore.viewAllProjects() >> Collections.<MoData> emptyList()
        viewResponseDtoBuilder.buildViewForAllProjects(Collections.<MoData> emptyList()) >> DEFAULT_RESPONSE_DTO
        final CliCommand cliCommand = new CliCommand("ap view", null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns OK with no projects"
        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "0 project(s) found")
    }

    def "Running view command for a single project should return correct message"() {

        given: "Command for view single project"
        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> NODE_FDN;
        autoProvisioningCore.viewProject(_ as String) >> Collections.<MoData> emptyList()
        viewResponseDtoBuilder.buildViewForProject(_ as List<MoData>) >> DEFAULT_RESPONSE_DTO
        final CliCommand cliCommand = new CliCommand("ap view -p " + PROJECT_NAME, null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns success"
        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "") // Empty DTO
    }

    def "Running view command for single node should return correct message"() {

        given: "Command for view single node"
        argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> NODE_FDN;
        autoProvisioningCore.viewNode(_ as String) >> Collections.<MoData> emptyList()
        viewResponseDtoBuilder.buildViewForNode(_ as List<MoData>) >> DEFAULT_RESPONSE_DTO
        final CliCommand cliCommand = new CliCommand("ap view -n " + NODE_NAME, null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns success"
        CommandResponseValidatorTest.verifySuccessWithCustomMessage(actualCommandResponse, "") // Empty DTO
    }

    def "Running view project command with invalid option should return invalid syntax message"() {

        given: "Command with invalid parameter syntax - option is invalid"
        final CliCommand cliCommand = new CliCommand("ap view -pp " + PROJECT_NAME, null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns invalid syntax"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "view")
    }

    def "Running view project with invalid additional option should return invalid syntax message"() {

        given: "Command with invalid parameter syntax - additional option"
        final CliCommand cliCommand = new CliCommand("ap view -p " + PROJECT_NAME + " asdfsafsd", null)

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns invalid"
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "view")
    }

    def "Running view command returns apServiceException then error is as expected"() {

        given: "View all projects command should return ApServiceException"
        final CliCommand cliCommand = new CliCommand("ap view", null)
        autoProvisioningCore.viewAllProjects() >> { throw new ApServiceException(EXCEPTION_MESSAGE) }
        exceptionMapperFactory.find(_ as ApServiceException) >> new ApServiceExceptionMapper()

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns exception as expected"
        CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
        CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "Running view command returns SecurityViolationException then not authorized response is returned"() {

        given: "view all projects command should return SecurityViolationException"
        final CliCommand cliCommand = new CliCommand("ap view", null)
        autoProvisioningCore.viewAllProjects() >> { throw new SecurityViolationException() }
        exceptionMapperFactory.find(_ as SecurityViolationException) >> new SecurityViolationExceptionMappper()

        when: "Command is executed"
        final CommandResponseDto actualCommandResponse = viewCommandHandler.processCommand(cliCommand)

        then: "Response returns access control exception as expected"
        CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse)
    }

}
