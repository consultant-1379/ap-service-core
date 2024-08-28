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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.exception.PartialProjectDeletionException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.core.cli.CliCommand;
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper;
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.NodeNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.PartialProjectDeletionExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ProjectNotFoundExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto

/**
 * Unit tests for {@link DeleteCommandHandler}.
 */
public class DeleteCommandHandlerSpec extends CdiSpecification {

    private static final String NODE_NAME_2 = "Node2";

    private static final CliCommand CLI_PROJECT_COMMAND = new CliCommand("ap delete -p " + PROJECT_NAME, Collections.<String, Object> emptyMap());
    private static final CliCommand CLI_NODE_COMMAND = new CliCommand("ap delete -n " + NODE_NAME, Collections.<String, Object> emptyMap());
    private static final CliCommand CLI_NODE_COMMAND_WITH_IGNORE = new CliCommand("ap delete -i -n " + NODE_NAME,
            Collections.<String, Object> emptyMap());
    private static final CliCommand CLI_PROJECT_COMMAND_WITH_IGNORE = new CliCommand("ap delete -i -p " + PROJECT_NAME,
            Collections.<String, Object> emptyMap());
    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @MockedImplementation
    private ArgumentResolver argumentResolver;

    @Inject
    private PartialProjectDeletionExceptionMapper partialProjectDeletionExceptionMapper;

    @Inject
    NodeNotFoundExceptionMapper nodeNotFoundExceptionMapper

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore;

    @ObjectUnderTest
    private DeleteCommandHandler deleteCommandHandler;

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    DpsQueries dpsQueries

    @MockedImplementation
    DpsQueryExecutor dpsQueryExecutor

    @Inject
    private ResponseDtoBuilder responseDtoBuilder

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
    }

    def "Verify delete project is successful"() {
        given:"mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName ) >> PROJECT_FDN

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_PROJECT_COMMAND);

        then: "The project is deleted successfully"
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "Verify delete project fails when project doesn't exist"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> {throw new ProjectNotFoundException("")}
            exceptionMapperFactory.find(_) >> new ProjectNotFoundExceptionMapper()

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_PROJECT_COMMAND);

        then: "verify ProjectNotFound Error"
            CommandResponseValidatorTest.verifyProjectNotFoundError(actualCommandResponse);
    }


    def "when unexpected error happens then delete project fails"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> PROJECT_FDN

        and:"mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.deleteProject(PROJECT_FDN, false) >> {throw new ApServiceException(EXCEPTION_MESSAGE)}
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_PROJECT_COMMAND);

        then: "verify ProjectNotFound Error"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse);
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse);
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse, PROJECT_NAME);
    }

    def "when delete project is partially successful then delete fails and shows failed node names"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> PROJECT_FDN

        and:"mock autoProvisioningCore downloadNodeArtifact"
            final List<String> deletedNodes = new ArrayList<>();
            final List<String> remainingNodes = new ArrayList<>();
            remainingNodes.add(NODE_NAME_2);
            remainingNodes.add(NODE_NAME);
            Whitebox.setInternalState(partialProjectDeletionExceptionMapper, "responseDtoBuilder", responseDtoBuilder)
            autoProvisioningCore.deleteProject(PROJECT_FDN, false) >> {throw new PartialProjectDeletionException(deletedNodes,remainingNodes)}
            exceptionMapperFactory.find(_) >> partialProjectDeletionExceptionMapper

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_PROJECT_COMMAND);

        then: "verify Response contains Error"
            CommandResponseValidatorTest.verifyResponseContainsError(actualCommandResponse, "1 : " + NODE_NAME);
            CommandResponseValidatorTest.verifyResponseContainsError(actualCommandResponse, "2 : " + NODE_NAME_2);
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse, PROJECT_NAME);
    }

    def "Verify Access Control Exception is thrown when user is unauthorised"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> {throw new SecurityViolationException("")}
            exceptionMapperFactory.find(_) >> new SecurityViolationExceptionMappper()

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_PROJECT_COMMAND);

        then: "verify AccessControlException Error"
            CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse);
    }

    def "Verify delete node is successful with valid ap delete command"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> NODE_FDN

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_NODE_COMMAND);

        then: "verify Success"
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "Verify deleting a node fails when the node does not exist"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> {throw new NodeNotFoundException("")}
            exceptionMapperFactory.find(_) >> nodeNotFoundExceptionMapper
            dpsQueries.findMoByName(_, _, _) >> dpsQueryExecutor
            dpsQueryExecutor.executeCount() >> 0
            Whitebox.setInternalState(nodeNotFoundExceptionMapper, "dpsQueries", dpsQueries)

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_NODE_COMMAND);

        then: "verify NodeNotFound Error"
            CommandResponseValidatorTest.verifyNodeNotFoundError(actualCommandResponse);
    }

    def "Verify delete node fails when an unexpected error occurs"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> NODE_FDN

        and:"mock autoProvisioningCore downloadNodeArtifact"
            autoProvisioningCore.deleteNode(NODE_FDN, false) >> {throw new ApServiceException(EXCEPTION_MESSAGE)}
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_NODE_COMMAND);

        then: "verify ServiceException Error"
            CommandResponseValidatorTest.verifyServiceExceptionError(actualCommandResponse);
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse, NODE_NAME);
    }

    def "Verify order fails when an invalid delete command is executed"() {
        when: "An operator executes the ap delete -x"
            final CliCommand cliCommandProj = new CliCommand("ap delete -x " + PROJECT_NAME, null);
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(cliCommandProj);

        then: "verify InvalidSyntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "delete");
    }

    def "Verify delete fails when the command has multiple invalid parameters"() {
        when: "An operator executes the ap delete -p"
            final CliCommand cliCommandProj = new CliCommand("ap delete -p " + PROJECT_NAME + " -n " + NODE_NAME, null);
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(cliCommandProj);

        then: "verify InvalidSyntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "delete");
    }

    def "Verify delete fails when the project id is not specified in the ap delete command"() {
        when: "An operator executes the ap delete -p without specifying the project id"
            final CliCommand cliCommandProj = new CliCommand("ap delete -p", null);
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(cliCommandProj);

        then: "verify InvalidSyntax Error"
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "delete");
    }

    def "Verify deleting a node is successful when the node contains ignore option"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> PROJECT_FDN

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_NODE_COMMAND_WITH_IGNORE);

        then: "verify Success"
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "Verify deleting a project is successful when the project contains ignore option"() {
        given: "mock argumentResolver resolveFdn"
            argumentResolver.resolveFdn(_ as CommandLine ,_ as CommandLogName) >> PROJECT_FDN

        when: "An operator executes the ap delete"
            final CommandResponseDto actualCommandResponse = deleteCommandHandler.processCommand(CLI_PROJECT_COMMAND_WITH_IGNORE);

        then: "verify Success"
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

}
