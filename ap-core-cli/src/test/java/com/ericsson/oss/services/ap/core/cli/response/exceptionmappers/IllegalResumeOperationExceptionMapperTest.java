/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;

import org.junit.Test;

import com.ericsson.oss.services.ap.api.exception.IllegalResumeOperationException;
import com.ericsson.oss.services.ap.core.cli.handlers.CommandResponseValidatorTest;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Unit tests for {@link IllegalCancelOperationExceptionMapper}.
 */
public class IllegalResumeOperationExceptionMapperTest {

    private final IllegalResumeOperationExceptionMapper illegalResumeOperationExceptionMapper = new IllegalResumeOperationExceptionMapper();

    @Test
    public void whenIllegalCancelOperationExceptionIsThrown_thenMapperReturnsResponseWithCorrectStatusAndSolutionMessages() {
        final CommandResponseDto result = illegalResumeOperationExceptionMapper.toCommandResponse("ap resume -n " + NODE_NAME,
                new IllegalResumeOperationException(null));
        CommandResponseValidatorTest.verifyIllegalOperationExceptionError(result, "Node is not waiting for resume or cancel",
                "Command can only be used on node waiting for resume or cancel");
    }
}
