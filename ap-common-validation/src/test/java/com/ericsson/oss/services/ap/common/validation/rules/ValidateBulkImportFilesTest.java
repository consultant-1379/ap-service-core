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
package com.ericsson.oss.services.ap.common.validation.rules;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.common.validation.configuration.ConfigurationFileValidator;

/**
 * Unit tests for {@link ValidateBulkImportFiles}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBulkImportFilesTest {

    private static final String CONFIGURATION_TYPE = "type";
    private static final String INVALID_CONFIGURATION_NAME = "invalidConfig.xml";
    private static final String VALID_CONFIGURATION_NAME = "validConfig.xml";

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ResourceService resourceService;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ConfigurationFileValidator configurationFileValidator;

    @InjectMocks
    @Spy
    private ValidateBulkImportFiles validateBulkImportFiles;

    private ValidationContext validationContext;

    @Before
    public void setUp() {
        final List<String> bulkFiles = new ArrayList<>();
        bulkFiles.add("file1");
        bulkFiles.add("File2");
        bulkFiles.add("File3");
        final Map<String, Object> contextTarget = new HashMap<>();
        contextTarget.put("configFiles", bulkFiles);
        contextTarget.put("nodeFdn", "NetworkElement=Node1,ManagedElement=Node1");

        validationContext = new ValidationContext("", contextTarget);

        when(resourceService.getAsText(any(String.class))).thenReturn("fileContents");

        final ManagedObject nodeMo = Mockito.mock(ManagedObject.class);
        final ManagedObject projectMo = Mockito.mock(ManagedObject.class);
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn("NetworkElement=Node1,ManagedElement=Node1")).thenReturn(nodeMo);
        when(nodeMo.getParent()).thenReturn(projectMo);
        when(nodeMo.getAttribute(NodeAttribute.NODE_IDENTIFIER.toString())).thenReturn("6607-651-025");
        when(projectMo.getName()).thenReturn(PROJECT_NAME);
    }

    @Test
    public void whenValidatingProjectAndImportServiceValidationSucceedsThenValidationPasses() {

        final Map<String, List<String>> configurationsByType = new HashMap<>();
        final List<String> configurations = new ArrayList<>();
        configurations.add(VALID_CONFIGURATION_NAME);
        configurationsByType.put(CONFIGURATION_TYPE, configurations);

        when(configurationFileValidator.validateFile(eq(PROJECT_NAME), eq(NODE_NAME), eq(NODE_IDENTIFIER_VALUE), any(ArchiveArtifact.class)))
            .thenReturn(Collections.<String> emptyList());

        final boolean result = validateBulkImportFiles.execute(validationContext);

        assertTrue(result);
    }

    @Test
    public void whenValidatingProjectAndImportServiceValidationFailsThenValidationFailsAndErrorMessagesAreCorrectlyFormed() {

        final Map<String, List<String>> configurationsByType = new HashMap<>();
        final List<String> configurations = new ArrayList<>();
        configurations.add(INVALID_CONFIGURATION_NAME);
        configurationsByType.put(CONFIGURATION_TYPE, configurations);

        final List<String> importServiceValidationErrors = new ArrayList<>();
        importServiceValidationErrors.add("error1");
        importServiceValidationErrors.add("validationError1");

        when(configurationFileValidator.validateFile(eq(PROJECT_NAME), eq(NODE_NAME), eq(NODE_IDENTIFIER_VALUE), any(ArchiveArtifact.class)))
            .thenReturn(importServiceValidationErrors);

        final boolean result = validateBulkImportFiles.execute(validationContext);

        assertFalse(result);

        final List<String> validationErrors = validationContext.getValidationErrors();
        assertEquals(6, validationErrors.size());
    }
}
