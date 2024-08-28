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
package com.ericsson.oss.services.ap.core.cli.handlers

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
import com.ericsson.oss.services.ap.api.exception.ApServiceException

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME

/**
 * Unit tests for {@link SkipCommandHandler}.
 */
class SkipCommandHandlerSpec extends CdiSpecification {

    private static final String AP_SKIP_COMMAND = "ap skip -n ";
    private static final CliCommand CLI_SKIP_COMMAND = new CliCommand(AP_SKIP_COMMAND + NODE_NAME, null)
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private ArgumentResolver argumentResolver

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @ObjectUnderTest
    private SkipCommandHandler skipCommandHandler

    @MockedImplementation
    ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI


    def setup(){
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
    }

    def "Running skip command with valid parameter syntax should return valid response"() {

        given:"skip command has valid parameter syntax"
            argumentResolver.resolveFdn(_, _) >> NODE_FDN

        when:"Command is executed"
            final CommandResponseDto actualCommandResponse = skipCommandHandler.processCommand(CLI_SKIP_COMMAND)

        then:"Response is successful"
            CommandResponseValidatorTest.verifySuccessAsyncNode(actualCommandResponse, NODE_NAME)
    }

    def "Running skip command with invalid syntax should throw ParseException with invalid syntax response returned"() {

        given: "skip command has invalid parameter syntax"
            final CliCommand cliCommand = new CliCommand("ap skip -y", null);

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = skipCommandHandler.processCommand(cliCommand);

        then: "Response returns invalid syntax"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "skip");
    }

    def "Running skip command with invalid additional option should return invalid syntax message"() {

        given: "Command with invalid parameter syntax - additional option"
            final CliCommand cliCommand = new CliCommand(AP_SKIP_COMMAND + NODE_NAME + " -additonal asdfsafsd", null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = skipCommandHandler.processCommand(cliCommand)

        then: "Response returns invalid"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "skip")
            }

    def "Running skip command returns apServiceException then error is as expected"() {

        given: "skip command should return ApServiceException"
            argumentResolver.resolveFdn(_, _) >> NODE_FDN
            autoProvisioningCore.skip(NODE_FDN) >> { throw new ApServiceException(EXCEPTION_MESSAGE) }
            exceptionMapperFactory.find(_ as ApServiceException) >> new ApServiceExceptionMapper()

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = skipCommandHandler.processCommand(CLI_SKIP_COMMAND)

        then: "Response returns exception as expected"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "Running skip command returns SecurityViolationException then not authorized response is returned"() {

        given: "skip command should return SecurityViolationException"
            argumentResolver.resolveFdn(_, _) >> NODE_FDN
            autoProvisioningCore.skip(NODE_FDN) >> { throw new SecurityViolationException() }
            exceptionMapperFactory.find(_ as SecurityViolationException) >> new SecurityViolationExceptionMappper()

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = skipCommandHandler.processCommand(CLI_SKIP_COMMAND)

        then: "Response returns access control exception as expected"
            CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse)
    }

}
