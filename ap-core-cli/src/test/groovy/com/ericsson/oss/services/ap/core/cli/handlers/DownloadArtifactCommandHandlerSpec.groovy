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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;

import javax.inject.Inject;
import org.apache.commons.cli.CommandLine;
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException;
import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper;
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.NodeNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileDownloadRequestDto;
import org.mockito.internal.util.reflection.Whitebox

/**
 * Unit tests for {@link DownloadArtifactCommandHandler}.
 */
public class DownloadArtifactCommandHandlerSpec extends CdiSpecification {

    private final static String DUMMY_UNIQUE_ID = "dummyUniqueId";
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private ArgumentResolver argumentResolver;

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore;

    @ObjectUnderTest
    private DownloadArtifactCommandHandler downloadArtifactCommandHandler;

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    DpsQueries dpsQueries

    @MockedImplementation
    DpsQueryExecutor dpsQueryExecutor


    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    @Inject
    NodeNotFoundExceptionMapper nodeNotFoundExceptionMapper

    def setup(){
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
    }

    def "Verify downloading raw node artifact is successful"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.RAW) >> DUMMY_UNIQUE_ID

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName ) >> NODE_FDN

        when: "An operator executes the ap download -i -n"
            final CliCommand cliCommand = new CliCommand("ap download -i -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);
            final List<AbstractDto> requestDto = actualCommandResponse.getResponseDto().getElements();
            final FileDownloadRequestDto fileDownloadRequestDto = (FileDownloadRequestDto) requestDto.get(0);

        then: "Successful Download"
            "ap" == fileDownloadRequestDto.getApplicationId()
            null != fileDownloadRequestDto.getFileId()
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "Verify site install generated artifact is downloaded successfully"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.GENERATED) >> DUMMY_UNIQUE_ID
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.RAW) >> DUMMY_UNIQUE_ID

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName ) >> NODE_FDN

        when: "An operator executes the ap download -o -n"
            final CliCommand cliCommand = new CliCommand("ap download -o -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);
            final List<AbstractDto> requestDto = actualCommandResponse.getResponseDto().getElements();
            final FileDownloadRequestDto fileDownloadRequestDto = (FileDownloadRequestDto) requestDto.get(0);

        then: "Successful Download"
            "ap" == fileDownloadRequestDto.getApplicationId()
            null != fileDownloadRequestDto.getFileId()
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "verify node not found error occurs when ap download command is executed and the node does not exist"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.RAW) >> DUMMY_UNIQUE_ID

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> {throw new NodeNotFoundException("")}
            exceptionMapperFactory.find(_) >> nodeNotFoundExceptionMapper
            dpsQueries.findMoByName(_, _, _) >> dpsQueryExecutor
            dpsQueryExecutor.executeCount() >> 0
            Whitebox.setInternalState(nodeNotFoundExceptionMapper, "dpsQueries", dpsQueries)

        when: "An operator executes the ap download -i -n"
            final CliCommand cliCommand = new CliCommand("ap download -i -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);

        then: "verify NodeNotFound Error"
            CommandResponseValidatorTest.verifyNodeNotFoundError(actualCommandResponse);
    }

    def "verify AP node not found error occurs when ap download command is executed and the node does not exist"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.RAW) >> DUMMY_UNIQUE_ID

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> {throw new NodeNotFoundException("")}
            exceptionMapperFactory.find(_) >> nodeNotFoundExceptionMapper
            dpsQueries.findMoByName(_, _, _) >> dpsQueryExecutor
            dpsQueryExecutor.executeCount() >> 1
            Whitebox.setInternalState(nodeNotFoundExceptionMapper, "dpsQueries", dpsQueries)

        when: "An operator executes the ap download -i -n"
            final CliCommand cliCommand = new CliCommand("ap download -i -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);

        then: "verify NodeNotFound Error"
            CommandResponseValidatorTest.verifyApNodeNotFoundError(actualCommandResponse);
    }

    def "Verify adding extra spaces in command does not throw an error"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.RAW) >> DUMMY_UNIQUE_ID

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName ) >> NODE_FDN

        when: "An operator executes the ap download -i -n"
            final CliCommand cliCommand = new CliCommand("ap download -i       -n   " + NODE_NAME + "  ", null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);
            final List<AbstractDto> requestDto = actualCommandResponse.getResponseDto().getElements();
            final FileDownloadRequestDto fileDownloadRequestDto = (FileDownloadRequestDto) requestDto.get(0);

        then: "Successful Download"
            "ap" == fileDownloadRequestDto.getApplicationId()
            null != fileDownloadRequestDto.getFileId()
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "verify error reading node"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.RAW) >> DUMMY_UNIQUE_ID

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> {throw new ApServiceException(EXCEPTION_MESSAGE)}
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "An operator executes the ap download -o -n"
            final CliCommand cliCommand = new CliCommand("ap download -o -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);

        then: "verify ServiceException Error and verify LogReferenceAndCompatibilitySet "
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse);
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse);
    }

    def "verify unauthorised access throws error"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.GENERATED) >> {throw new SecurityViolationException("")}
            exceptionMapperFactory.find(_) >> new SecurityViolationExceptionMappper()

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> NODE_FDN


        when: "An operator executes the ap download -o -n"
            final CliCommand cliCommand = new CliCommand("ap download -o -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);

        then: "verify AccessControlException Error"
            CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse);
    }

    def "verify Error When Downloading NodeArtifact"() {
        given: "mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.downloadNodeArtifact(NODE_FDN, ArtifactBaseType.GENERATED) >> {throw new ApServiceException(EXCEPTION_MESSAGE)}
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        and:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> NODE_FDN


        when: "An operator executes the ap download -o -n"
            final CliCommand cliCommand = new CliCommand("ap download -o -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(cliCommand);

        then: "verify service Exception Error"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse);
    }

    def "verify syntax error when no arguments in the command"() {
        when: "An operator executes the ap download -o -n"
            final CliCommand command = new CliCommand("ap download", null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(command);

        then: "verify Invalid Syntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "download");
    }

    def "verify syntax error when extra command options are included"() {
        when: "An operator executes the ap download -i -n"
            final CliCommand command = new CliCommand("ap download -i -n " + NODE_NAME + " -v 1.1.1", null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(command);

        then: "verify Invalid Syntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "download");
    }

    def "verify syntax error when Raw And GeneratedOptions are Specified"() {
        when: "An operator executes the ap download -o -n"
            final CliCommand command = new CliCommand("ap download -i -o -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(command);

        then: "verify Invalid Syntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "download");
    }

    def "verify syntax error when node option is not specified"() {
        when: "An operator executes the ap download -o -n"
            final CliCommand command = new CliCommand("ap download -i", null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(command);

        then: "verify Invalid Syntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "download");
    }

    def "verify syntax error when raw or generatedOption is not Specified"() {
        when: "An operator executes the ap download -o -n"
            final CliCommand command = new CliCommand("ap download -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = downloadArtifactCommandHandler.processCommand(command);

        then: "verify Invalid Syntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "download");
    }
}
