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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME

import javax.inject.Inject

import org.apache.commons.cli.CommandLine
import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.api.exception.ValidationException
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor
import com.ericsson.oss.services.ap.common.usecase.CommandLogName
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.InvalidNodeStateExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.NodeNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ValidStatesForEventMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ValidationExceptionMapper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
/**
 * Unit tests for {@link OrderCommandHandler}.
 */
class OrderCommandHandlerSpec extends CdiSpecification {

    private static final String FILE_NAME = "archive1.zip"
    private static final String FILE_PATH = "/" + FILE_NAME
    private static final byte[] FILE_CONTENT = new byte[1]
    private static final String ORDER_NODE_COMMAND = "ap order -n "
    private static final String ORDER_PROJECT_COMMAND = "ap order -p "
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private ArgumentResolver argumentResolver

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @MockedImplementation
    DpsQueries dpsQueries

    @MockedImplementation
    DpsQueryExecutor dpsQueryExecutor

    @Inject
    private InvalidNodeStateExceptionMapper invalidNodeStateExceptionMapper

    @Inject
    NodeNotFoundExceptionMapper nodeNotFoundExceptionMapper

    @Inject
    private ValidStatesForEventMapper validStatesForEventMapper

    @Inject
    private ValidationExceptionMapper validationExceptionMapper

    @Inject
    private ResponseDtoBuilder responseDtoBuilder

    @ObjectUnderTest
    private OrderCommandHandler orderCommandHandler

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
        Whitebox.setInternalState(invalidNodeStateExceptionMapper, "validStatesForEventMapper", validStatesForEventMapper)
        Whitebox.setInternalState(validationExceptionMapper, "responseDtoBuilder", responseDtoBuilder)
    }

    def "Order for a node should succeed"() {

        given: "Command to order a node"
            argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> NODE_FDN
            final CliCommand command = new CliCommand(ORDER_NODE_COMMAND + NODE_NAME, null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response is success"
            CommandResponseValidatorTest.verifySuccessAsyncNode(actualCommandResponse, NODE_NAME)
    }

    def "Order for a node when node does not exist should fail"() {

        given: "Command to order a node"
            argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> { throw new NodeNotFoundException()}
            final CliCommand command = new CliCommand(ORDER_NODE_COMMAND + NODE_NAME, null)
            exceptionMapperFactory.find(_) >> nodeNotFoundExceptionMapper
            dpsQueries.findMoByName(_, _, _) >> dpsQueryExecutor
            dpsQueryExecutor.executeCount() >> 0
            Whitebox.setInternalState(nodeNotFoundExceptionMapper, "dpsQueries", dpsQueries)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response returns invalid"
            CommandResponseValidatorTest.verifyNodeNotFoundError(actualCommandResponse)
    }

    def "Order node when ApServiceException is thrown should fail as expected"() {

        given: "Command for order node"
            argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> NODE_FDN
            final CliCommand command = new CliCommand(ORDER_NODE_COMMAND + NODE_NAME, null)

        and: "ApServiceException thrown when node is ordered"
            autoProvisioningCore.orderNode(NODE_FDN) >> { throw new ApServiceException(EXCEPTION_MESSAGE) }
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Order fails as expected"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "Order command with no parameters should fail"() {

        given: "Order command has no parameters"
            final CliCommand command = new CliCommand("ap order", null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response returns invalid syntax"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "order")
    }

    def "Order command with invalid parameters should fail"() {

        given: "Order command has invalid parameters"
            final CliCommand command = new CliCommand("ap order -x " + NODE_NAME, null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response returns invalid syntax"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "order")
    }

    def "Order command with invalid number of parameters should fail"() {

        given: "Order command has invalid number of parameters"
            final CliCommand command = new CliCommand(ORDER_NODE_COMMAND + NODE_NAME + " -p " + PROJECT_NAME, null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response returns invalid syntax"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "order")
    }

    def "Order command with no node specified should fail"() {

        given: "Order command with node name as null"
            final CliCommand command = new CliCommand(ORDER_NODE_COMMAND, null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response returns invalid syntax"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "order")
    }

    def "Order carried out with node in invalid state should fail"() {

        given: "Command created for node in invalid state"
            argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> NODE_FDN
            final CliCommand command = new CliCommand(ORDER_NODE_COMMAND + NODE_NAME, null)

        and: "Node in invalid state for order"
            autoProvisioningCore.orderNode(NODE_FDN) >> { throw new InvalidNodeStateException("error", "ORDER_STARTED")}
            exceptionMapperFactory.find(_ as InvalidNodeStateException) >> invalidNodeStateExceptionMapper

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response returns invalid state error"
            CommandResponseValidatorTest.verifyInvalidStateError(actualCommandResponse, "Order Started", "Order Failed","Order Cancelled")
    }

    def "Order project should succeed"() {

        given: "Order command for a project"
            argumentResolver.resolveFdn(_ as CommandLine, _ as CommandLogName) >> PROJECT_FDN
            final CliCommand command = new CliCommand(ORDER_PROJECT_COMMAND + PROJECT_NAME, null)

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response is success"
            CommandResponseValidatorTest.verifySuccessAsyncProject(actualCommandResponse, PROJECT_NAME)
    }

    def "Order project archive should succeed"() {

        given: "Order command for a project archive"
            final CliCommand command = prepareOrderProjectArchive()
            autoProvisioningCore.orderProject(FILE_NAME, FILE_CONTENT, true) >> PROJECT_FDN

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response is success"
            CommandResponseValidatorTest.verifySuccessAsyncProject(actualCommandResponse, PROJECT_NAME)
    }

    def "Order project archive with no validation should succeed"() {

        given: "Order command for a project archive with no validation specified"
            final CliCommand command = prepareOrderProjectArchiveNoValidation()
            autoProvisioningCore.orderProject(FILE_NAME, FILE_CONTENT, false) >> PROJECT_FDN

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response is success"
            CommandResponseValidatorTest.verifySuccessAsyncProject(actualCommandResponse, PROJECT_NAME)
    }

    def "Order project when ApServiceException is thrown should fail as expected"() {

        given: "Order command for a project archive"
            final CliCommand command = prepareOrderProjectArchive()

        and: "Order project causes ApServiceException"
            autoProvisioningCore.orderProject(FILE_NAME, FILE_CONTENT, true) >> { throw new ApServiceException(new IllegalArgumentException(EXCEPTION_MESSAGE)) }
            exceptionMapperFactory.find(_ as ApServiceException) >> new ApServiceExceptionMapper()

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response fails as expected"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "Order project when ValidationException is thrown should fail as expected"() {

        given: "Order command for a project archive"
            final CliCommand command = prepareOrderProjectArchive()

        and: "Order project causes ValidationException"
            autoProvisioningCore.orderProject(FILE_NAME, FILE_CONTENT, true) >> { throw new ValidationException(Arrays.asList("error"), "error") }
            exceptionMapperFactory.find(_ as ValidationException) >> validationExceptionMapper

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = orderCommandHandler.processCommand(command)

        then: "Response fails as expected"
            CommandResponseValidatorTest.verifyValidationExceptionError(actualCommandResponse)
    }

    private CliCommand prepareOrderProjectArchive() {
        final Map<String, Object> commandProperties = new HashMap<>()
        commandProperties.put("fileName", FILE_NAME)
        commandProperties.put("filePath", FILE_PATH)
        argumentResolver.getFileName(commandProperties) >> FILE_NAME
        argumentResolver.getFileContent(commandProperties) >> FILE_CONTENT
        return new CliCommand("ap order file:" + FILE_NAME, commandProperties)
    }

    private CliCommand prepareOrderProjectArchiveNoValidation() {
        final Map<String, Object> commandProperties = new HashMap<>()
        commandProperties.put("fileName", FILE_NAME)
        commandProperties.put("filePath", FILE_PATH)
        argumentResolver.getFileName(commandProperties) >> FILE_NAME
        argumentResolver.getFileContent(commandProperties) >> FILE_CONTENT
        return new CliCommand("ap order -nv file:" + FILE_NAME, commandProperties)
    }
}
