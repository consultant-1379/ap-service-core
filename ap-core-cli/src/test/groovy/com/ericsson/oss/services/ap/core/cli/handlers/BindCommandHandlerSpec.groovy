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

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.InvalidNodeStateExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.HwIdAlreadyBoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.HwIdInvalidFormatExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.NodeNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ValidStatesForEventMapper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException
import com.ericsson.oss.services.ap.api.exception.HwIdInvalidFormatException
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import org.mockito.internal.util.reflection.Whitebox

import javax.inject.Inject

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME

/**
 * Unit tests for {@link BindCommandHandler}.
 */
public class BindCommandHandlerSpec extends CdiSpecification {

    private static final String AP_BIND_COMMAND = "ap bind ";
    private static final String BIND_NODE_COMMAND = AP_BIND_COMMAND + "-n ";
    private static final String BIND_FILE_COMMAND = AP_BIND_COMMAND + "file:";
    private static final String NODE2_NAME = "Node2"
    private static final String BIND_COMMAND = BIND_NODE_COMMAND + NODE_NAME + " -s " + HARDWARE_SERIAL_NUMBER_VALUE
    private static final CliCommand CLI_BIND_COMMAND = new CliCommand(BIND_COMMAND, Collections.<String, Object> emptyMap())
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private ArgumentResolver argumentResolver

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @MockedImplementation
    BatchBindCommandHandler batchBindCommandHandler

    @ObjectUnderTest
    private BindCommandHandler bindCommandHandler

    @MockedImplementation
    ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    DpsQueries dpsQueries

    @MockedImplementation
    DpsQueryExecutor dpsQueryExecutor

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @Inject
    ValidStatesForEventMapper validStatesForEventMapper

    @Inject
    InvalidNodeStateExceptionMapper invalidNodeStateExceptionMapper

    @Inject
    NodeNotFoundExceptionMapper nodeNotFoundExceptionMapper

    @Inject
    private HwIdAlreadyBoundExceptionMapper hwIdAlreadyBoundExceptionMapper

    @Inject
    private ResponseDtoBuilder responseDtoBuilder

    def setup(){
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
        Whitebox.setInternalState(hwIdAlreadyBoundExceptionMapper, "responseDtoBuilder", responseDtoBuilder)
    }

    def "when bind succeeds then success response returned"() {

        given:
        argumentResolver.resolveFdn(_, _) >> NODE_FDN

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)

        then:
        CommandResponseValidatorTest.verifySuccess(actualCommandResponse)
    }

    def "when bind with already bound hardwareId then error response returned"() {

        given:
        argumentResolver.resolveFdn(_, _) >> NODE_FDN

        and:
        autoProvisioningCore.bind(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE) >> { throw new HwIdAlreadyBoundException(Arrays.asList(String.format("The hardware serial number %s is already bound to node %s", HARDWARE_SERIAL_NUMBER_VALUE, NODE2_NAME)),"message")}
        exceptionMapperFactory.find(_) >> hwIdAlreadyBoundExceptionMapper

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)


        then:
        final String errorMessage = String.format("The hardware serial number %s is already bound", HARDWARE_SERIAL_NUMBER_VALUE)
        CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(actualCommandResponse, errorMessage,
                "Provide a hardware serial number that is not bound to another node")
    }

    def "when bind with incorrect formatted hardwareId then error response returned"() {

        given:
        argumentResolver.resolveFdn(_, _) >> NODE_FDN

        autoProvisioningCore.bind(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE) >> {
            throw new HwIdInvalidFormatException(String.format("The hardware serial number %s is not valid", HARDWARE_SERIAL_NUMBER_VALUE))
        }
        exceptionMapperFactory.find(_) >> new HwIdInvalidFormatExceptionMapper()

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)

        then:
        final String expectedErrorMessage = String.format("The hardware serial number %s is not valid", HARDWARE_SERIAL_NUMBER_VALUE)
        CommandResponseValidatorTest.verifyExecutionError(actualCommandResponse, expectedErrorMessage, "See AP online help for the correct format",
                CliErrorCodes.VALIDATION_ERROR_CODE)

    }

    def "when bind not authorized then error response returned"() {

        given:
        argumentResolver.resolveFdn(_, _) >> {
            throw new SecurityViolationException("")
        }
        exceptionMapperFactory.find(_) >> new SecurityViolationExceptionMappper()

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)

        then:
        CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse)
    }

    def "when node does not exist then error response returned"() {

        given:
        argumentResolver.resolveFdn(_, _) >> {
            throw new NodeNotFoundException("")
        }
        exceptionMapperFactory.find(_) >> nodeNotFoundExceptionMapper
        dpsQueries.findMoByName(_, _, _) >> dpsQueryExecutor
        dpsQueryExecutor.executeCount() >> 0
        Whitebox.setInternalState(nodeNotFoundExceptionMapper, "dpsQueries", dpsQueries)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)

        then:
        CommandResponseValidatorTest.verifyNodeNotFoundError(actualCommandResponse)
    }

    def "when bind node and bind is batch then batch bind handler is invoked"() {

        given:
        argumentResolver.resolveFdn(_, _) >> NODE_FDN

        when:
        final CliCommand cliCommand = new CliCommand(BIND_FILE_COMMAND + ":file.csv", Collections.<String, Object> emptyMap())
        bindCommandHandler.processCommand(cliCommand)

        then:
        1 * batchBindCommandHandler.processCommand(_)
    }

    def "when bind has unexpected error then general bind error response returned"() {

        given:
        argumentResolver.resolveFdn(_, _) >> NODE_FDN
        autoProvisioningCore.bind(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE) >> {
            throw new ApServiceException(EXCEPTION_MESSAGE)
        }
        exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)

        then:
        CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
        CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "when bind with invalid command parameter then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + " -x " + NODE_NAME, null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when bind with extra command parameter then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(BIND_NODE_COMMAND + NODE_NAME + " -s " + HARDWARE_SERIAL_NUMBER_VALUE + " proj", null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when bind with only node specified then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(BIND_NODE_COMMAND + NODE_NAME, null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when bind with missing hardware serial number then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(BIND_NODE_COMMAND + NODE_NAME + " -s", null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when bind with invalid file parameter then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + " f:file", null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when batch bind with no file then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(BIND_FILE_COMMAND, null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when bind with no params specified then error message returned"() {

        given:
        final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND, null)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(cliCommand)

        then:
        CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind")
    }

    def "when bind with node in invalid state then invalid state error returned"() {

        given:
        autoProvisioningCore.bind(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE) >> {
            throw new InvalidNodeStateException("error", "ORDER_STARTED")
        }
        argumentResolver.resolveFdn(_, _) >> NODE_FDN
        exceptionMapperFactory.find(_) >> invalidNodeStateExceptionMapper
        Whitebox.setInternalState(invalidNodeStateExceptionMapper, "validStatesForEventMapper", validStatesForEventMapper)

        when:
        final CommandResponseDto actualCommandResponse = bindCommandHandler.processCommand(CLI_BIND_COMMAND)

        then:
        CommandResponseValidatorTest.verifyInvalidStateError(actualCommandResponse, "Order Started", "Bind Completed", "Order Completed", "Hardware Replace Bind Completed", "Pre Migration Bind Completed", "Pre Migration Completed")

    }

}
