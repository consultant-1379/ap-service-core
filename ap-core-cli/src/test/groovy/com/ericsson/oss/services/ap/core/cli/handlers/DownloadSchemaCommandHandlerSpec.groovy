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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.UnsupportedNodeTypeException
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.UnsupportedNodeTypeExceptionMapper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto

class DownloadSchemaCommandHandlerSpec extends CdiSpecification {

    private static final String UNIQUE_FILE_ID = "UniqueFileId"
    private static final String UNKNOWN_NODE_TYPE = "UNKNOWN_NODE"
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @ObjectUnderTest
    private DownloadSchemaCommandHandler downloadSchemaCommandHandler

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    private ModelReader modelReader

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @Inject
    private UnsupportedNodeTypeExceptionMapper unsupportedNodeTypeExceptionMapper

    def setup(){
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
    }

    def "When running a download command for a valid nodetype then schemas are downloaded successfully"() {

        given: "Execute command with valid parameter syntax"
            autoProvisioningCore.downloadSchemaAndSamples(VALID_NODE_TYPE) >> UNIQUE_FILE_ID
            final CliCommand cliCommand = new CliCommand("ap download -x " + VALID_NODE_TYPE, null)

        when: "Download command executed"
            final CommandResponseDto actualCommandResponse = downloadSchemaCommandHandler.processCommand(cliCommand)

        then: "Response returns valid"
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse)
    }

    def "When download unsupported node type schemas then response should contain unsupported node type error"() {

        given: "Node type downloaded is not supported"
            def List<String> nodeTypes = Arrays.asList("ERBS")
            Whitebox.setInternalState(unsupportedNodeTypeExceptionMapper, "modelReader", modelReader)
            autoProvisioningCore.downloadSchemaAndSamples(UNKNOWN_NODE_TYPE) >> { throw new UnsupportedNodeTypeException("Error") }
            exceptionMapperFactory.find(_ as UnsupportedNodeTypeException) >> unsupportedNodeTypeExceptionMapper
            modelReader.getSupportedNodeTypes() >> nodeTypes
            final CliCommand cliCommand = new CliCommand("ap download -x " + UNKNOWN_NODE_TYPE, null)

        when: "Download command executed"
            final CommandResponseDto actualCommandResponse = downloadSchemaCommandHandler.processCommand(cliCommand)

        then: "Response returns expected error"
            CommandResponseValidatorTest.verifyExecutionError(actualCommandResponse, "Error", "Provide a valid node type. Valid types are: [ERBS]",
                        CliErrorCodes.UNSUPPORTED_NODE_TYPE_ERROR_CODE)
    }

    def "When download schemas with APServiceException then response should return service error"() {

        given: "Download command executed with invalid parameter syntax"
            exceptionMapperFactory.find(_ as ApServiceException) >> new ApServiceExceptionMapper()
            autoProvisioningCore.downloadSchemaAndSamples(VALID_NODE_TYPE) >> { throw new ApServiceException(EXCEPTION_MESSAGE) }
            final CliCommand cliCommand = new CliCommand("ap download -x " + VALID_NODE_TYPE, null)

        when: "Download command executed"
            final CommandResponseDto actualCommandResponse = downloadSchemaCommandHandler.processCommand(cliCommand)

        then: "Response returns error"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse)
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse)
    }

    def "When download schemas with no nodetype then response is successful"() {

        given: "Download command executed with no node type specified"
            autoProvisioningCore.downloadSchemaAndSamples() >> UNIQUE_FILE_ID
            final CliCommand cliCommand = new CliCommand("ap download -x ", null)

        when: "Download command executed"
            final CommandResponseDto actualCommandResponse = downloadSchemaCommandHandler.processCommand(cliCommand)

        then: "Response returns valid"
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse)
    }

    def "When download returns SecurityViolationException then not authorized response is returned"() {

        given: "Download samples command should return SecurityViolationException"
            final CliCommand cliCommand = new CliCommand("ap download -x " + VALID_NODE_TYPE, null)
            autoProvisioningCore.downloadSchemaAndSamples(VALID_NODE_TYPE) >> { throw new SecurityViolationException() }
            exceptionMapperFactory.find(_ as SecurityViolationException) >> new SecurityViolationExceptionMappper()

        when: "Command is executed"
            final CommandResponseDto actualCommandResponse = downloadSchemaCommandHandler.processCommand(cliCommand)

        then: "Response returns access control exception as expected"
            CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse)
    }
}
