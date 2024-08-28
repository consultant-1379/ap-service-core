/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.metadata.cli.cache.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewItemMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;
import com.ericsson.oss.services.ap.core.metadata.cli.model.Group;
import com.ericsson.oss.services.ap.core.metadata.cli.model.Table;

/**
 * Unit tests for {@link MetadataResourceUnmarshaller}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataResourceUnmarshallerTest {

    private static final String METADATA_FILE_PATH = "test/path";
    private static final String TEST_VALUE = "value";
    private static final String METADATA_VIEW = "<cliViewMetadata><view><viewitem><group>"
            + "<table><attribute><label>" + TEST_VALUE + "</label></attribute></table>"
            + "</group></viewitem></view></cliViewMetadata>";
    private static final String INVALID_METADATA_VIEW = "<malformedXmlFile>";

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private Resource resource;

    @Mock
    private ResourceService resourceService;

    @Mock
    private SystemRecorder recorder; // NOPMD

    @InjectMocks
    private MetadataResourceUnmarshaller metadataResourceUnmarshaller;

    @Before
    public void setUp() {
        final List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceService.listFiles(METADATA_FILE_PATH)).thenReturn(resources);
    }

    @Test
    public void whenObjectIsUnmarshalledFromXml_thenTheOutputCliViewShouldContainXmlValues() {
        final InputStream inputStream = new ByteArrayInputStream(METADATA_VIEW.getBytes(StandardCharsets.UTF_8));
        when(resource.getInputStream()).thenReturn(inputStream);

        final Collection<CliViews> actualDocumentCollection = metadataResourceUnmarshaller.loadMetadata(METADATA_FILE_PATH, CliViews.class);
        final String labelValue = getLabelFromCliMetadata(actualDocumentCollection);

        assertEquals("Label value was not unmarshalled from the XML input correctly", TEST_VALUE, labelValue);
    }

    @Test
    public void whenObjectIsUnmarshalledFromXml_andTheXmlIsMalformed_thenTheOutputCliViewShouldBeEmpty() {
        final InputStream inputStream = new ByteArrayInputStream(INVALID_METADATA_VIEW.getBytes(StandardCharsets.UTF_8));
        when(resource.getInputStream()).thenReturn(inputStream);

        final Collection<CliViews> actualDocumentCollection = metadataResourceUnmarshaller.loadMetadata(METADATA_FILE_PATH, CliViews.class);

        assertTrue("CliViews collection contains data, but malformed data should not have been successfully parsed and added",
                actualDocumentCollection.isEmpty());
    }

    private String getLabelFromCliMetadata(final Collection<CliViews> actualDocumentCollection) {
        final CliViews cliViewMetadata = actualDocumentCollection.iterator().next();
        final ViewMetadata view = cliViewMetadata.getViews().get(0);
        final ViewItemMetadata viewItem = view.getViewItems().get(0);
        final Group group = (Group) viewItem.getViewComponentsMetadata().get(0);
        final Table table = (Table) group.getViewComponentsMetadata().get(0);
        final AttributeMetadata attribute = table.getAttributes().get(0);
        return attribute.getLabel();
    }
}
