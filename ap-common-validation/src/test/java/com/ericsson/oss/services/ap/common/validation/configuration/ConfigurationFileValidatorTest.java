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

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportValidationSpecification;
import com.ericsson.oss.services.cm.bulkimport.response.ImportServiceValidationResponse;

/**
 * Unit tests for {@link ConfigurationFileValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationFileValidatorTest {

    private static final String CONFIGURATION_FILE_CONTENTS = "fileContents";
    private static final String CONFIGURATION_FILE_NAME = "configuration.xml";

    @Mock
    private ArtifactResourceOperations artifactResourceOperations;

    @Mock
    private BulkCmValidator bulkCmValidator;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> queryExecutor;

    @Mock
    private Logger logger; //NOPMD injected for test

    @InjectMocks
    private ConfigurationFileValidator configurationFileValidator;

    @Test
    public void whenValidateFileThenTemporaryFileIsCreatedAndTemporaryFileIsDeletedAfterValidation() {
        final ImportServiceValidationResponse validationResponse = mock(ImportServiceValidationResponse.class);

        final ArchiveArtifact configurationFile = new ArchiveArtifact(CONFIGURATION_FILE_NAME, CONFIGURATION_FILE_CONTENTS);
        when(bulkCmValidator.validateBulkCmFile(any(ImportValidationSpecification.class), eq(configurationFile))).thenReturn(validationResponse);
        when(validationResponse.getStatusCode()).thenReturn(0);
        when(dpsQueries.findMoByName(NODE_NAME, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString())).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(new ArrayList<ManagedObject>().iterator());

        configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER_VALUE, configurationFile);

        final String expectedTempDirectory = DirectoryConfiguration.getTemporaryDirectory() + File.separator + PROJECT_NAME + File.separator
                + NODE_NAME + File.separator + CONFIGURATION_FILE_NAME;
        final InOrder inOrder = inOrder(artifactResourceOperations, bulkCmValidator);
        inOrder.verify(artifactResourceOperations).writeArtifact(eq(expectedTempDirectory), any(byte[].class));
        inOrder.verify(bulkCmValidator).validateBulkCmFile(any(ImportValidationSpecification.class), eq(configurationFile));
        inOrder.verify(artifactResourceOperations).deleteFile(expectedTempDirectory);
    }

    @Test
    public void whenValidateFileThenAndResponseIsNotOfTypeImportServiceValidationResponseThenResponseIsAddedAsErrorMessage() {
        final ArchiveArtifact configurationFile = new ArchiveArtifact(CONFIGURATION_FILE_NAME, CONFIGURATION_FILE_CONTENTS);
        when(bulkCmValidator.validateBulkCmFile(any(ImportValidationSpecification.class), eq(configurationFile))).thenReturn("Error");
        when(dpsQueries.findMoByName(NODE_NAME, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString())).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(new ArrayList<ManagedObject>().iterator());
        final List<String> errorMessages = configurationFileValidator.validateFile(PROJECT_NAME, NODE_NAME, NODE_IDENTIFIER_VALUE, configurationFile);
        assertEquals("Error", errorMessages.get(0));
    }

    @Test
    public void whenRemoveErrorCodeFromErrorMessageAndMessageHasErrorCodeThenErrorCodeIsRemoved() {
        final String input = "Error 7000 : errorMessage";
        final String result = configurationFileValidator.removeErrorCodeFromErrorMessage(input);
        assertEquals("errorMessage", result);
    }

    @Test
    public void whenRemoveErrorCodeFromErrorMessageAndMessageHasNoErrorCodeThenInputIsReturned() {
        final String input = "errorMessage";
        final String result = configurationFileValidator.removeErrorCodeFromErrorMessage(input);
        assertEquals("errorMessage", result);
    }
}