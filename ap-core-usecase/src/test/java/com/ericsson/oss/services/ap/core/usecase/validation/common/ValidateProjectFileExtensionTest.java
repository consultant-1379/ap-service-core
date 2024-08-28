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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey;

/**
 * Unit tests for {@link ValidateProjectFileExtension}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateProjectFileExtensionTest {

    @InjectMocks
    private final ValidateProjectFileExtension validateZipFileExtension = new ValidateProjectFileExtension();

    private ValidationContext context;

    @Test
    public void testExecuteValidZipFileExtension() {
        final String validZipFileName = "test.zip";
        final Map<String, Object> zipData = new HashMap<>();

        zipData.put(ImportProjectTargetKey.FILENAME.toString(), validZipFileName);
        zipData.put(ImportProjectTargetKey.FILE_CONTENT.toString(), "test".getBytes());
        context = new ValidationContext("ValidateProjectFormat", zipData);

        validateZipFileExtension.execute(context);

        assertTrue(context.getValidationErrors().isEmpty());
    }

    @Test
    public void testExecuteInValidZipFileExtension() {
        final String validZipFileName = "test.zap";
        final Map<String, Object> zipData = new HashMap<>();

        zipData.put(ImportProjectTargetKey.FILENAME.toString(), validZipFileName);
        zipData.put(ImportProjectTargetKey.FILE_CONTENT.toString(), "test".getBytes());

        final String expectedMessage = String.format("Invalid file extension %s. Only .zip supported", validZipFileName);
        context = new ValidationContext("ValidateProjectFormat", zipData);

        validateZipFileExtension.execute(context);

        assertEquals(1, context.getValidationErrors().size());
        assertEquals(expectedMessage, context.getValidationErrors().get(0));
    }
}
