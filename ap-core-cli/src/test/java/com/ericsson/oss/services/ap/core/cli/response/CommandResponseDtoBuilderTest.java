/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus;

/**
 * Unit tests for {@link CommandResponseDtoBuilder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandResponseDtoBuilderTest {

    private final CommandResponseDtoBuilder builder = new CommandResponseDtoBuilder();

    @Test
    public void testMinimalResponse() {
        final CommandResponseDto response = builder.build();

        assertEquals("Minimal response should have command == 'Undefined'", "Undefined", response.getCommand());
        assertEquals("Minimal response should have statusCode == 0", 0, response.getStatusCode());
        assertNull("Minimal response should have statusMessage null", response.getStatusMessage());
        assertNotNull("Minimal response should have responseDto not null", response.getResponseDto());
        assertNull("Minimal response should have solution null", response.getSolution());
        assertEquals("Minimal response should have errorCode == 0", 0, response.getErrorCode());
    }

    @Test
    public void testErrorResponseWithErrorLines() {
        final int errorCode = 1000;
        final String statusMessage = "message";
        final String solution = "solution";

        final CommandResponseDto response = builder
                .errorCode(errorCode)
                .statusMessage(statusMessage)
                .solution(solution)
                .build();

        final ResponseDto responseDto = response.getResponseDto();
        assertNotNull("ResponseDto should never be null", response.getResponseDto());

        final List<AbstractDto> elements = responseDto.getElements();
        assertNotNull("ResponseDto.elements should not be null", elements);

        final LineDto[] elementsArray = elements.toArray(new LineDto[elements.size()]);
        assertEquals("ResponseDto.elements should have three elements", 3, elementsArray.length);

        final LineDto emptyLine = elementsArray[0];
        assertNotNull("Empty line on responseDto should not be null", emptyLine);
        assertEquals("Empty line on responseDto should be equals to ''", "", emptyLine.getValue());

        final LineDto errorLine = elementsArray[1];
        assertNotNull("Error line on responseDto should not be null", errorLine);
        assertEquals("Error line on responseDto not in correct format", format("Error %s : %s", errorCode, statusMessage), errorLine.getValue());

        final LineDto solutionLine = elementsArray[2];
        assertNotNull("Solution line on responseDto should not be null", solutionLine);
        assertEquals("Solution line on responseDto not in correct format", format("Suggested Solution : %s", solution), solutionLine.getValue());
    }

    @Test
    public void testSuccessResponseWithSuccessLines() {
        final String statusMessage = "message";

        final CommandResponseDto response = builder
                .statusCode(ResponseStatus.SUCCESS)
                .statusMessage(statusMessage)
                .build();

        final ResponseDto responseDto = response.getResponseDto();
        assertNotNull("ResponseDto should never be null", response.getResponseDto());

        final List<AbstractDto> elements = responseDto.getElements();
        assertNotNull("ResponseDto.elements should not be null", elements);

        final LineDto[] elementsArray = elements.toArray(new LineDto[elements.size()]);
        assertEquals("ResponseDto.elements should have three elements", 3, elementsArray.length);

        final LineDto emptyLine1 = elementsArray[0];
        assertNotNull("First empty line on responseDto should not be null", emptyLine1);
        assertEquals("First empty line on responseDto should be equals to ''", "", emptyLine1.getValue());

        final LineDto statusLine = elementsArray[1];
        assertNotNull("Status line on responseDto should not be null", statusLine);
        assertEquals("Status line on responseDto not in correct format", statusMessage, statusLine.getValue());

        final LineDto emptyLine2 = elementsArray[2];
        assertNotNull("Second empty line on responseDto should not be null", emptyLine2);
        assertEquals("Second empty line on responseDto should be equals to ''", "", emptyLine2.getValue());
    }
}
