/*
 ------------------------------------------------------------------------------
  *******************************************************************************
  * COPYRIGHT Ericsson 2017
  *
  * The copyright to the computer program(s) herein is the property of
  * Ericsson Inc. The programs may be used and/or copied only with written
  * permission from Ericsson Inc. or in accordance with the terms and
  * conditions stipulated in the agreement/contract under which the
  * program(s) have been supplied.
  *******************************************************************************
  *----------------------------------------------------------------------------
 */
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ZipContentGenerator;

/**
 * Unit tests for {@link ValidateTimeZoneSupported}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateTimeZoneSupportedTest {

    private static final String ZIPFILE_NAME = "test.zip";
    private static final String NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String NODEINFO_CONTENT = "";
    private static final String NODE_FOLDER_1 = "Folder1";
    private static final String NODE_FOLDER_2 = "Folder2";
    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";
    private static final String VALIDATION_TIMEZONE_NOT_EXIST_ERROR_MESSAGE_FORMAT = "Unsupported timeZone [%s] in nodeInfo.xml";
    private static final String TIMEZONE_INVALID_ATTRIBUTE = "INVALID";
    private static final String TIMEZONE_DATA_TYPE = "TimeZone";
    private static final String NAMESPACE_OSS_NE_DEF = "OSS_NE_DEF";
    private static final List<String> SUPPORTED_TIMEZONES = Arrays.asList("ACT", "AET", "Europe/Dublin", "Zulu");
    private static final List<String> NO_SUPPORTED_TIMEZONES = Arrays.asList("");

    private ValidationContext context;

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeInfoReader nodeInfoReader;

    @Mock
    private NodeInfo nodeInfo;

    @Mock
    private EnumDataTypeSpecification enumSpecification;

    @InjectMocks
    private ValidateTimeZoneSupported validateTimeZoneSupported;

    @Before
    public void setUp() {
        when(modelReader.getLatestEnumDataTypeSpecification(SchemaConstants.OSS_EDT, NAMESPACE_OSS_NE_DEF, TIMEZONE_DATA_TYPE))
                .thenReturn(enumSpecification);
        when(nodeInfoReader.read(any(Archive.class), anyString())).thenReturn(nodeInfo);
    }

    @Test
    public void testRuleReturnsTrueWhenTimezonesAreSupportedForAllNodesInProject() {
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT));
        when(nodeInfo.getTimeZone()).thenReturn("Europe/Dublin");
        when(enumSpecification.getMemberNames()).thenReturn(SUPPORTED_TIMEZONES);

        final boolean result = validateTimeZoneSupported.execute(context);

        assertTrue(result);
    }

    @Test
    public void testRuleFailsWhenTimezonesAreInvalidForNodesInProject() {
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT));
        when(nodeInfo.getTimeZone()).thenReturn(TIMEZONE_INVALID_ATTRIBUTE);
        when(enumSpecification.getMemberNames()).thenReturn(SUPPORTED_TIMEZONES);

        validateTimeZoneSupported.execute(context);

        assertEquals(2, context.getValidationErrors().size());
        final String validationErrorMessage = String.format(VALIDATION_TIMEZONE_NOT_EXIST_ERROR_MESSAGE_FORMAT, TIMEZONE_INVALID_ATTRIBUTE);
        assertEquals(String.format("%s - %s", NODE_FOLDER_1, validationErrorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("%s - %s", NODE_FOLDER_2, validationErrorMessage), context.getValidationErrors().get(1));
    }

    @Test
    public void testRulePassesWhenTimezonesNotSetForNodesInProject() {
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT));
        when(enumSpecification.getMemberNames()).thenReturn(SUPPORTED_TIMEZONES);
        when(nodeInfo.getTimeZone()).thenReturn(null);

        final boolean result = validateTimeZoneSupported.execute(context);

        assertTrue(result);
    }

    @Test
    public void testRuleFailsWhenNoTimezonesSupportedForNodesInProject() {
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT));
        when(nodeInfo.getTimeZone()).thenReturn(TIMEZONE_INVALID_ATTRIBUTE);
        when(enumSpecification.getMemberNames()).thenReturn(NO_SUPPORTED_TIMEZONES);

        validateTimeZoneSupported.execute(context);

        assertEquals(2, context.getValidationErrors().size());
        final String validationErrorMessage = String.format(VALIDATION_TIMEZONE_NOT_EXIST_ERROR_MESSAGE_FORMAT, TIMEZONE_INVALID_ATTRIBUTE);
        assertEquals(String.format("%s - %s", NODE_FOLDER_1, validationErrorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("%s - %s", NODE_FOLDER_2, validationErrorMessage), context.getValidationErrors().get(1));
    }

    private Map<String, Object> createTarget(final String nodeInfoTemplate) {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(NODE_FOLDER_1, NODEINFO_FILENAME, nodeInfoTemplate);
        zcg.createFileInZip(NODE_FOLDER_2, NODEINFO_FILENAME, nodeInfoTemplate);
        return zcg.getZipData(ZIPFILE_NAME);
    }
}
