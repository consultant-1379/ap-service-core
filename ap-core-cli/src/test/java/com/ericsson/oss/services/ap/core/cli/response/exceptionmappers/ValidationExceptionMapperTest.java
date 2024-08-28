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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.core.cli.handlers.CommandResponseValidatorTest;
import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;

/**
 * Unit tests for {@link ValidationExceptionMapper}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationExceptionMapperTest {

    @Mock
    private ValidationException validationException;

    @Spy
    private ResponseDtoBuilder responseDtoBuilder; // NOPMD

    @InjectMocks
    private ValidationExceptionMapper validationExceptionMapper;

    private final List<String> validationFailures = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        validationFailures.add("Error - 1");
        validationFailures.add("Error - 2");
    }

    @Test
    public void when_expception_thrown_for_order_command_then_order_message_failure() {
        when(validationException.getValidationFailures()).thenReturn(validationFailures);
        final CommandResponseDto commandResponse = validationExceptionMapper.toCommandResponse("ap order file:kkk.zip", validationException);
        CommandResponseValidatorTest.verifyValidationExceptionError(commandResponse);
    }

    @Test
    public void when_expception_thrown_for_upload_command_then_upload_message_failure() {
        when(validationException.getMessage()).thenReturn("Validation Error Message");
        final CommandResponseDto commandResponse = validationExceptionMapper.toCommandResponse("ap upload -n node1", validationException);
        CommandResponseValidatorTest.verifyValidationErrors(commandResponse, "");
    }
}
