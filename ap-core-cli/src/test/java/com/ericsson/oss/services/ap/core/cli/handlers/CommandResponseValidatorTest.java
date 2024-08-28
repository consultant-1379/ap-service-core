/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import com.ericsson.oss.services.ap.core.cli.CliErrorCodes;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Helper class to validate the Command Responses produced by the various XxxCommandHandler test classes.
 */
public final class CommandResponseValidatorTest {

    private static final String EXCEPTION_MESSAGE = "Exception Message";

    private CommandResponseValidatorTest() {

    }

    public static void verifySuccess(final CommandResponseDto actualCommandResponse) {
        verifySuccessWithCustomMessage(actualCommandResponse, "Successful");
    }

    public static void verifySuccessAsyncNode(final CommandResponseDto actualCommandResponse, final String nodeName) {
        verifySuccessWithCustomMessage(actualCommandResponse, String.format("Initiated. Run 'ap status -n %s' for progress", nodeName));
    }

    public static void verifySuccessAsyncProject(final CommandResponseDto actualCommandResponse, final String projectName) {
        verifySuccessWithCustomMessage(actualCommandResponse, String.format("Initiated. Run 'ap status -p %s' for progress", projectName));
    }

    public static void verifySuccessWithCustomMessage(final CommandResponseDto actualCommandResponse, final String expectedStatusMessage) {
        assertEquals(ResponseStatus.SUCCESS, actualCommandResponse.getStatusCode());
        assertEquals(expectedStatusMessage, actualCommandResponse.getStatusMessage());
        assertEquals(0, actualCommandResponse.getErrorCode());
        assertNull(actualCommandResponse.getSolution());
    }

    public static void verifyValidationErrors(final CommandResponseDto actualCommandResponse, final String expectedValidationError) {
        final String actualValidationError = actualCommandResponse.getResponseDto().getElements().get(0).toString();
        assertEquals(expectedValidationError, actualValidationError);
    }

    public static void verifyInvalidSyntaxError(final CommandResponseDto actualCommandResponse, final String commandName) {
        verifyInvalidSyntaxErrorWithCustomSolution(actualCommandResponse, String.format("For correct command syntax run 'help ap %s'", commandName));
    }

    public static void verifyInvalidSyntaxErrorWithCustomSolution(final CommandResponseDto actualCommandResponse, final String solutionMessage) {
        verifyResponse(actualCommandResponse,
                "Invalid command syntax",
                solutionMessage,
                CliErrorCodes.INVALID_COMMAND_SYNTAX_ERROR_CODE,
                ResponseStatus.COMMAND_SYNTAX_ERROR);
    }

    public static void verifyUnexpectedError(final CommandResponseDto actualCommandResponse, final String expectedStatusMessage,
            final String expectedSolutionMessage, final int errorCode) {
        verifyResponse(actualCommandResponse,
                expectedStatusMessage,
                expectedSolutionMessage,
                errorCode,
                ResponseStatus.UNEXPECTED_ERROR);
    }

    public static void verifyExecutionError(final CommandResponseDto actualCommandResponse, final String expectedStatusMessage,
            final String expectedSolutionMessage, final int errorCode) {
        verifyResponse(actualCommandResponse,
                expectedStatusMessage,
                expectedSolutionMessage,
                errorCode,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    public static void verifyProjectNotFoundError(final CommandResponseDto actualCommandResponse) {
        verifyEntityNotFoundError(actualCommandResponse, "Project does not exist", "Provide a valid project name");
    }

    public static void verifyNodeNotFoundError(final CommandResponseDto actualCommandResponse) {
        verifyEntityNotFoundError(actualCommandResponse, "Node does not exist", "Provide a valid node name");
    }

    public static void verifyApNodeNotFoundError(final CommandResponseDto actualCommandResponse) {
        verifyEntityNotFoundError(actualCommandResponse, "Node is not managed by Auto Provisioning", "Provide a node managed by Auto Provisioning");
    }

    private static void verifyEntityNotFoundError(final CommandResponseDto actualCommandResponse, final String expectedStatusMessage,
            final String expectedSolutionMessage) {
        verifyResponse(actualCommandResponse,
                expectedStatusMessage,
                expectedSolutionMessage,
                CliErrorCodes.ENTITY_NOT_FOUND_ERROR_CODE,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    public static void verifyAccessControlExceptionError(final CommandResponseDto actualCommandResponse) {
        verifyResponse(actualCommandResponse,
                "Insufficient access rights to execute the command",
                "Contact the system administrator to update the user profile",
                CliErrorCodes.NO_AUTHORIZATION_ERROR_CODE,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    public static void verifyNoImportFileError(final CommandResponseDto actualCommandResponse, final String expectedStatusMessage,
            final String expectedSolutionMessage) {
        verifyResponse(actualCommandResponse,
                expectedStatusMessage,
                expectedSolutionMessage,
                CliErrorCodes.NO_ATTACHED_FILE_ERROR_CODE,
                ResponseStatus.COMMAND_SYNTAX_ERROR);
    }

    public static void verifyServiceExceptionError(final CommandResponseDto actualCommandResponse) {
        verifyServiceExceptionErrorWithCustomErrorMessage(actualCommandResponse, EXCEPTION_MESSAGE);
    }

    public static void verifyServiceExceptionErrorWithCustomErrorMessage(final CommandResponseDto actualCommandResponse,
            final String expectedErrorMessage) {
        verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(actualCommandResponse, expectedErrorMessage,
                "Use Log Viewer for more information");
    }

    public static void verifyServiceExceptionErrorWithCustomErrorAndSolutionMessage(final CommandResponseDto actualCommandResponse,
            final String expectedErrorMessage, final String expectedSolutionMessage) {
        verifyResponse(actualCommandResponse,
                expectedErrorMessage,
                expectedSolutionMessage,
                CliErrorCodes.SERVICE_ERROR_CODE,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    public static void verifyValidationExceptionError(final CommandResponseDto actualCommandResponse) {
        verifyResponse(actualCommandResponse,
                "Error(s) found validating project",
                "Fix errors and execute the command again",
                CliErrorCodes.VALIDATION_ERROR_CODE,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    public static void verifyIllegalOperationExceptionError(final CommandResponseDto actualCommandResponse, final String expectedStatusMessage,
            final String expectedSolutionMessage) {
        verifyResponse(actualCommandResponse,
                expectedStatusMessage,
                expectedSolutionMessage,
                CliErrorCodes.ILLEGAL_OPERATION_ERROR_CODE,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    public static void verifyInvalidStateError(final CommandResponseDto actualCommandResponse, final String currentInvalidState,
            final String... validStates) {
        verifyResponse(actualCommandResponse,
                String.format("Node is not in the correct state to perform the operation [%s]", currentInvalidState),
                String.format("Ensure node is in correct state before executing the command. Valid state(s) are %s",
                        Arrays.asList(validStates).toString()),
                CliErrorCodes.INVALID_STATE_ERROR_CODE,
                ResponseStatus.COMMAND_EXECUTION_ERROR);
    }

    private static void verifyResponse(final CommandResponseDto actualCommandResponse, final String statusMessage, final String solutionMessage,
            final int errorCode, final int responseStatus) {
        assertEquals(statusMessage, actualCommandResponse.getStatusMessage());
        assertEquals(solutionMessage, actualCommandResponse.getSolution());
        assertEquals(errorCode, actualCommandResponse.getErrorCode());
        assertEquals(responseStatus, actualCommandResponse.getStatusCode());
    }

    public static void verifyResponseContainsError(final CommandResponseDto actualCommandResponse, final String expectedErrorMessage) {
        final ResponseDto responseDto = actualCommandResponse.getResponseDto();
        for (final AbstractDto element : responseDto.getElements()) {
            if (element.toString().contains(expectedErrorMessage)) {
                return;
            }
        }
        fail(String.format("No match for [%s] in [%s]", expectedErrorMessage, actualCommandResponse.getResponseDto().toString()));
    }

    public static void verifyLogReferenceAndCompatibilitySet(final CommandResponseDto actualCommandResponse) {
        assertTrue(actualCommandResponse.isLogViewerCompatible());
        assertNotNull(actualCommandResponse.getLogReference());
    }
    public static void verifyLogReferenceAndCompatibilitySet(final CommandResponseDto actualCommandResponse, final String logReference) {
        assertTrue(actualCommandResponse.isLogViewerCompatible());
        assertEquals(actualCommandResponse.getLogReference(), logReference);
    }
}
