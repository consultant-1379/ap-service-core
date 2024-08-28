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
import com.ericsson.oss.services.ap.api.bind.BatchBindResult
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.core.cli.CliCommand
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ApServiceExceptionMapper
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.SecurityViolationExceptionMappper
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME
/**
 * Unit tests for {@link BatchBindCommandHandler}.
 */
class BatchBindCommandHandlerSpec extends CdiSpecification {

    private static final String AP_BIND_COMMAND = "ap bind file:";
    private static final String BIND_FAILURE_MESSAGE = "Error1";
    private static final String BIND_INTERNAL_FAILURE = "Internal Server Error";
    private static final String EXPECTED_BATCH_CVS_ERROR_MESSAGE = "1 : " + BIND_FAILURE_MESSAGE;
    private static final String EXPECTED_BATCH_CVS_ERROR_MESSAGE_INTERNAL_ERROR = "1 : " + BIND_INTERNAL_FAILURE;
    private static final String FILE_NAME_KEY = "fileName";
    private static final String FILE_PATH_KEY = "filePath";
    private static final String BATCH_FILE_NAME = "\"batch.csv\"";
    private static final String BATCH_FILE_PATH = "/" + BATCH_FILE_NAME;
    private static final byte[] BATCH_FILE_CONTENT = [0];

    @MockedImplementation
    private ArgumentResolver argumentResolver;

    @MockedImplementation
    private AutoProvisioningService autoProvisioningCore

    @ObjectUnderTest
    private BatchBindCommandHandler batchBindHandler;

    @MockedImplementation
    ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    private final Map<String, Object> commandProperties = new HashMap<>();

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(AutoProvisioningService.class, "apcore") >> autoProvisioningCore
        commandProperties.put(FILE_PATH_KEY, BATCH_FILE_PATH);
        commandProperties.put(FILE_NAME_KEY, BATCH_FILE_NAME);
        argumentResolver.getFileName(commandProperties) >> BATCH_FILE_NAME
        argumentResolver.getFileContent(commandProperties) >> BATCH_FILE_CONTENT
    }

    def "When batch bind succeeds then success response returned"() {

        given:
            final BatchBindResult batchResult = new BatchBindResult.Builder()
                .withBindSuccess(HARDWARE_SERIAL_NUMBER_VALUE, NODE_NAME)
                .build();
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> batchResult

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifySuccess(actualCommandResponse);
    }

    def "When batch bind partial success then partial failure response returned"() {

        given:
            final BatchBindResult batchResult = new BatchBindResult.Builder()
                .withBindFailure(BIND_FAILURE_MESSAGE)
                .withBindSuccess(HARDWARE_SERIAL_NUMBER_VALUE, NODE_NAME)
                .build();
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> batchResult

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(actualCommandResponse, "Successful for 1/2 node(s)",
            "Fix errors and execute the command again");
    }

    def "when batch bind partial success then bind errors returned in response"() {

        given:
        final BatchBindResult batchResult = new BatchBindResult.Builder()
            .withBindFailure(BIND_FAILURE_MESSAGE)
            .withBindSuccess(HARDWARE_SERIAL_NUMBER_VALUE, NODE_NAME)
            .build();
        autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> batchResult
        when:
        final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
        final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);
        then:
        CommandResponseValidatorTest.verifyValidationErrors(actualCommandResponse, EXPECTED_BATCH_CVS_ERROR_MESSAGE);
    }

    def "when batch bind partial success with internal error then link to log viewer included"() {

        given:
            final BatchBindResult batchResult = new BatchBindResult.Builder()
                .withBindFailure(BIND_INTERNAL_FAILURE)
                .withBindSuccess(HARDWARE_SERIAL_NUMBER_VALUE, NODE_NAME)
                .build();
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> batchResult

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifyValidationErrors(actualCommandResponse, EXPECTED_BATCH_CVS_ERROR_MESSAGE_INTERNAL_ERROR);
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse);
    }

    def "when batch bind failed then failure response returned"() {

        given:
            final BatchBindResult batchResult = new BatchBindResult.Builder()
                .withBindFailure(BIND_FAILURE_MESSAGE)
                .build();
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> batchResult

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(actualCommandResponse, "Failure",
                "Fix errors and execute the command again");
    }

    def "when batch bind failed with internal error then link to log viewer returned"() {

        given:
            final BatchBindResult batchResult = new BatchBindResult.Builder()
                .withBindFailure(BIND_INTERNAL_FAILURE)
                .build();
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> batchResult

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(actualCommandResponse, "Failure",
                "Fix errors and execute the command again");
            CommandResponseValidatorTest.verifyLogReferenceAndCompatibilitySet(actualCommandResponse);
    }

    def "when batch bind has unexpected error then response returned with error from exception"() {

        given:
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> { throw new ApServiceException("error") }
            exceptionMapperFactory.find(_) >> new ApServiceExceptionMapper()

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand)

        then:
            CommandResponseValidatorTest.verifyServiceExceptionErrorWithCustomErrorMessage(actualCommandResponse, "error")

    }

    def "when batch bind not authorized then error response returned"() {

        given:
            autoProvisioningCore.batchBind(BATCH_FILE_NAME, BATCH_FILE_CONTENT) >> { throw new SecurityViolationException() }
            exceptionMapperFactory.find(_) >> new SecurityViolationExceptionMappper()

        when:
            final CliCommand cliCommand = new CliCommand(AP_BIND_COMMAND + BATCH_FILE_NAME, commandProperties);
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifyAccessControlExceptionError(actualCommandResponse);
    }

    def "when batch bind is executed and command does not contain file then ParseException is thrown and InvalidSyntaxResponse is returned"() {

        given:
            final CliCommand cliCommand = new CliCommand("ap bind", commandProperties);

        when:
            final CommandResponseDto actualCommandResponse = batchBindHandler.processCommand(cliCommand);

        then:
            CommandResponseValidatorTest.verifyInvalidSyntaxError(actualCommandResponse, "bind");
    }
}
