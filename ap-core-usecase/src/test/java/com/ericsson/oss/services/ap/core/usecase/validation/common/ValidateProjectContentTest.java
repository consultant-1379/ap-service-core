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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey;

/**
 * Unit tests for {@link ValidateProjectContent}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateProjectContentTest {

    private static final String PROJECT_ARCHIVE_FILE_NAME = "test.zip";

    private final Map<String, Object> target = new HashMap<>();

    @Mock
    private Archive archiveReader;

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private ValidateProjectContent validateProjectFileContent;

    private ValidationContext context;

    @Before
    public void init() {
        target.put(ImportProjectTargetKey.FILENAME.toString(), PROJECT_ARCHIVE_FILE_NAME);
        target.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archiveReader);
        context = new ValidationContext("ValidateProjectFormat", target);
    }

    @Test
    public void testValidationSuccessWhenAtLeastOneFileInProjectArchive() {
        final Collection<ArchiveArtifact> projectArtifacts = new ArrayList<>();
        projectArtifacts.add(new ArchiveArtifact("", ""));
        when(archiveReader.getNumberOfArtifacts()).thenReturn(1);

        final boolean result = validateProjectFileContent.execute(context);

        assertTrue(result);
    }

    @Test
    public void testValidationFailureWhenNoFilesInProjectArchive() {
        doReturn(Collections.emptyList()).when(archiveReader).getAllArtifacts();
        final boolean result = validateProjectFileContent.execute(context);
        assertFalse(result);
    }

    @Test
    public void testValidationContextContainsErrorMessageWhenValidationFails() {
        doReturn(Collections.<ArchiveArtifact> emptyList()).when(archiveReader).getAllArtifacts();
        validateProjectFileContent.execute(context);
        assertEquals("Project file is empty", context.getValidationErrors().get(0));
    }
}
