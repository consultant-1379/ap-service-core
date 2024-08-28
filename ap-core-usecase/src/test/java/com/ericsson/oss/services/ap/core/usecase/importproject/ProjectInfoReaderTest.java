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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.common.model.access.ModeledAttributeFilter;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader;
import com.ericsson.oss.services.ap.core.usecase.utilities.ZipUtil;

/**
 * Unit tests for {@link ProjectInfoReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProjectInfoReaderTest {

    private static final String INVALID_PROJECT_XML = "<?xml version='1.0' encoding='UTF-8'?> "
            + "<projectInfo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
            + "xsi:noNamespaceSchemaLocation='erbsProjectInfo.xsd'>"
            + "<name>"
            + PROJECT_NAME
            + "</name>"
            + "<description>proj description</description> "
            + "<creator>APCreator</creator>";

    private static final String VALID_PROJECT_XML = INVALID_PROJECT_XML + "</projectInfo>";

    @Mock
    private ModeledAttributeFilter modeledAttrFilter;

    @InjectMocks
    private ProjectInfoReader projectInfoReader;

    private Map<String, String> rootChildElements;
    private Map<String, Object> projectAttributes;

    @Before
    public void setUp() {
        projectAttributes = new HashMap<>();
        projectAttributes.put("creator", "APCreator");
        projectAttributes.put("description", "proj description");

        rootChildElements = new HashMap<>();
        rootChildElements.put("name", PROJECT_NAME);
        rootChildElements.put("creator", "APCreator");
        rootChildElements.put("description", "proj description");
    }

    @Test
    public void whenReadProjectInfoTheReturnedProjectAttributesMatchTheValuesInTheXmlFile() throws IOException {
        final Archive projectArchiveReader = createZipArchive(VALID_PROJECT_XML);

        when(modeledAttrFilter.apply("ap", "Project", rootChildElements)).thenReturn(projectAttributes);

        final ProjectInfo projectInfo = projectInfoReader.read(projectArchiveReader);
        final Map<String, Object> actualProjectAttributes = projectInfo.getProjectAttributes();

        assertEquals(PROJECT_NAME, projectInfo.getName());
        assertEquals(projectAttributes, actualProjectAttributes);
        assertEquals(2, projectInfo.getNodeQuantity());
    }

    @Test(expected = ValidationException.class)
    public void whenProjectInfoXmlIsInvalidThenExceptionIsThrown() throws IOException {
        final Archive archiveReader = createZipArchive(INVALID_PROJECT_XML);
        projectInfoReader.read(archiveReader);
    }

    private Archive createZipArchive(final String xml) throws IOException {
        final Map<String, String> projectArchive = new HashMap<>();
        projectArchive.put("projectInfo.xml", xml);
        projectArchive.put("/node1/nodeInfo.xml", "");
        projectArchive.put("/node2/nodeInfo.xml", "");
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive);
        return ArchiveReader.read(zipFile);
    }
}