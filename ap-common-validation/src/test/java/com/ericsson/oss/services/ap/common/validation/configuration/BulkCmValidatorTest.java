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
package com.ericsson.oss.services.ap.common.validation.configuration;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.Mockito.when;

import com.ericsson.oss.services.cm.bulkimport.api.ImportService;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportValidationSpecification;

@RunWith(MockitoJUnitRunner.class)
public class BulkCmValidatorTest {

    private static final String BULKCM_FILE_NAME = "sampleBulkCmFile.xml";
    private static final String BULKCM_FILE_CONTENTS = "fileContents";

    @Mock
    private ImportService importService; // NOPMD

    @Mock
    private ImportValidationSpecification importSpecification;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ExecutorService executor;

    @Mock
    private Future<Object> future;

    @InjectMocks
    private BulkCmValidator bulkCmValidator;

    @SuppressWarnings("unchecked")
    @Test
    public void whenValidateBulkCmFileFailsTimeoutExceptionIsCaught() throws InterruptedException, ExecutionException, TimeoutException {

        final ArchiveArtifact bulkCmFile = new ArchiveArtifact(BULKCM_FILE_NAME, BULKCM_FILE_CONTENTS);

        when(executor.submit(any(Callable.class))).thenReturn(future);
        when(future.get(BulkCmValidator.getMaximumValidationTimeInSeconds(), TimeUnit.SECONDS)).thenThrow(new TimeoutException());

        final String actualResult = (String) bulkCmValidator.validateBulkCmFile(importSpecification, bulkCmFile);
        final String expectedResult = String.format("Validation of BulkCmFile %s has timed out", bulkCmFile);

        assertThat(actualResult, containsString(expectedResult));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenValidateBulkCmFileFailsExceptionIsCaught() throws InterruptedException, ExecutionException, TimeoutException {

        final ArchiveArtifact bulkCmFile = new ArchiveArtifact(BULKCM_FILE_NAME, BULKCM_FILE_CONTENTS);

        when(executor.submit(any(Callable.class))).thenReturn(future);
        when(future.get(BulkCmValidator.getMaximumValidationTimeInSeconds(), TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        final String actualResult = (String) bulkCmValidator.validateBulkCmFile(importSpecification, bulkCmFile);
        final String expectedResult = String.format("Validation failed while attempting to validate BulkCmFile %s", bulkCmFile);

        assertThat(actualResult, containsString(expectedResult));
    }
}