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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ZipContentGenerator;

/**
 * Unit tests for {@link ValidateNodeArtifactsExist}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeArtifactsExistTest {

    private static final String NODE_INFO_XML = "nodeInfo.xml";
    private static final String SITE_BASIC_XML = "SiteBasic.xml";
    private static final String SITE_INSTALLATION_XML = "SiteInstallation.xml";
    private static final String SITE_EQUIPMENT_XML = "SiteEquipment.xml";
    private static final String TN_DATA_XML = "TN_Data.xml";
    private static final String RN_DATA_XML = "RN_Data.xml";
    private static final String BASELINE_MOS = "PostIntegrationScript.mos";
    private static final String PROJECT_INFO_XML = "projectInfo.xml";
    private static final String DIAGRAM_FILE = "diagram.svg";
    private static final String VALID_PROJECT_FILE_NAME = "test.zip";
    private static final String ULSTER_NODE_FOLDER_NAME = "ulster";

    private static final String FILES_NOT_LISTED_PRESENT_MESSAGE = "Invalid files present in project file. Contains files not listed in nodeInfo.xml";
    private static final String FILE_CONTENT_MISSING_MESSAGE = "Artifact %s referenced in nodeInfo.xml is not found";

    private final List<String> requiredFileNames = new ArrayList<>();
    private final List<String> mosFileNames = new ArrayList<>();

    @Mock
    private NodeInfo nodeInfo;

    @InjectMocks
    @Spy
    private ValidateNodeArtifactsExist validator;

    private ValidationContext context;

    @Before
    public void setUp() {
        final String group = "validate_project_content";
        final String dummyProjectInfoContent = "dummycontent";
        final String dummyNodeInfoContent = "dummycontent";
        final String dummyArtifactContent = "dummycontent";

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECT_INFO_XML, dummyProjectInfoContent);
        zcg.createFileInZip(ULSTER_NODE_FOLDER_NAME, NODE_INFO_XML, dummyNodeInfoContent);
        zcg.createFileInZip(ULSTER_NODE_FOLDER_NAME, SITE_BASIC_XML, dummyArtifactContent);
        zcg.createFileInZip(ULSTER_NODE_FOLDER_NAME, SITE_INSTALLATION_XML, dummyArtifactContent);
        zcg.createFileInZip(ULSTER_NODE_FOLDER_NAME, SITE_EQUIPMENT_XML, dummyArtifactContent);
        zcg.createFileInZip(ULSTER_NODE_FOLDER_NAME, TN_DATA_XML, dummyArtifactContent);
        zcg.createFileInZip(ULSTER_NODE_FOLDER_NAME, DIAGRAM_FILE, dummyArtifactContent);

        final Map<String, Object> target = zcg.getZipData(VALID_PROJECT_FILE_NAME);
        context = new ValidationContext(group, target);

        requiredFileNames.add(SITE_BASIC_XML);
        requiredFileNames.add(SITE_INSTALLATION_XML);
        requiredFileNames.add(SITE_EQUIPMENT_XML);

        doReturn(nodeInfo).when(validator).getNodeInfo(any(ValidationContext.class), anyString());
    }

    @Test
    public void whenArtifactsInNodeInfoMatchArtifactsInDirThenValidationSucceeds() {
        requiredFileNames.add(TN_DATA_XML);
        final Map<String, List<String>> artifacts = new HashMap<>();
        artifacts.put("artifacts", requiredFileNames);
        when(nodeInfo.getNodeArtifacts()).thenReturn(artifacts);

        assertTrue(validator.execute(context));
    }

    @Test
    public void whenArtifactsCaseDiffersThenValidationSucceeds() {
        requiredFileNames.clear();
        requiredFileNames.add(TN_DATA_XML.toLowerCase());
        requiredFileNames.add(SITE_BASIC_XML.toUpperCase());
        requiredFileNames.add(SITE_INSTALLATION_XML);
        requiredFileNames.add(SITE_EQUIPMENT_XML.toLowerCase());

        final Map<String, List<String>> artifacts = new HashMap<>();
        artifacts.put("artifacts", requiredFileNames);
        when(nodeInfo.getNodeArtifacts()).thenReturn(artifacts);

        assertTrue(validator.execute(context));
    }

    @Test
    public void whenArtifactsInDirNotInNodeInfoNoPresentThenValidationFails() {
        final Map<String, List<String>> artifacts = new HashMap<>();
        artifacts.put("artifacts", requiredFileNames);
        when(nodeInfo.getNodeArtifacts()).thenReturn(artifacts);

        final String expectedMessage = String.format(FILES_NOT_LISTED_PRESENT_MESSAGE);

        validator.execute(context);

        assertEquals(String.format("%s - %s", ULSTER_NODE_FOLDER_NAME, expectedMessage), context.getValidationErrors().get(0));
    }

    @Test
    public void whenNonBaselineArtifactsInNodeInfoNotPresentThenValidationFails() {
        requiredFileNames.add(TN_DATA_XML);
        requiredFileNames.add(RN_DATA_XML);

        final Map<String, List<String>> artifacts = new HashMap<>();
        artifacts.put("artifacts", requiredFileNames);

        when(nodeInfo.getNodeArtifacts()).thenReturn(artifacts);
        final String expectedMessage = String.format(FILE_CONTENT_MISSING_MESSAGE, RN_DATA_XML);

        validator.execute(context);

        assertEquals(String.format("%s - %s", ULSTER_NODE_FOLDER_NAME, expectedMessage), context.getValidationErrors().get(0));
    }

    @Test
    public void whenBaselineArtifactsInNodeInfoNotPresentThenValidationSucceeds() {
        requiredFileNames.add(TN_DATA_XML);
        mosFileNames.add(BASELINE_MOS);

        final Map<String, List<String>> artifacts = new HashMap<>();
        artifacts.put("artifacts", requiredFileNames);
        artifacts.put("baseline", mosFileNames);

        when(nodeInfo.getNodeArtifacts()).thenReturn(artifacts);

        assertTrue(validator.execute(context));
    }
}
