/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
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
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.core.cli.view.CliView;
import com.ericsson.oss.services.ap.core.metadata.cli.api.CliMetadataService;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;

/**
 * Unit tests for {@link ViewResponseDtoBuilder}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewResponseDtoBuilderTest {

    @Mock
    private CliMetadataService cliMetadataService;

    @Mock
    private CliView cliView;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    private ViewResponseDtoBuilder viewResponseDtoBuilder;

    private final ResponseDto allProjectsResponse = new ResponseDto(Collections.<AbstractDto> emptyList());
    private final ResponseDto nodeResponse = new ResponseDto(Collections.<AbstractDto> emptyList());
    private final ResponseDto projectWithNodesResponse = new ResponseDto(Collections.<AbstractDto> emptyList());
    private final ResponseDto projectOnlyResponse = new ResponseDto(Collections.<AbstractDto> emptyList());

    @Before
    public void setUp() {
        final ViewMetadata nodeViewMetadata = mock(ViewMetadata.class);
        when(cliMetadataService.getViewMetadata(VALID_NODE_TYPE.toLowerCase(), "node")).thenReturn(nodeViewMetadata);
        when(cliView.buildViewFromMetadata(eq(nodeViewMetadata), anyList())).thenReturn(nodeResponse);

        final ViewMetadata projectOnlyViewMetadata = mock(ViewMetadata.class);
        when(cliMetadataService.getViewMetadata("project")).thenReturn(projectOnlyViewMetadata);
        when(cliView.buildViewFromMetadata(eq(projectOnlyViewMetadata), anyList())).thenReturn(projectOnlyResponse);

        final ViewMetadata projectAndNodesViewMetadata = mock(ViewMetadata.class);
        when(cliMetadataService.getViewMetadata(VALID_NODE_TYPE.toLowerCase(), "project")).thenReturn(projectAndNodesViewMetadata);
        when(cliView.buildViewFromMetadata(eq(projectAndNodesViewMetadata), anyList())).thenReturn(projectWithNodesResponse);

        final ViewMetadata allProjectsViewMetadata = mock(ViewMetadata.class);
        when(cliMetadataService.getViewMetadata("projects")).thenReturn(allProjectsViewMetadata);
        when(cliView.buildViewFromMetadata(eq(allProjectsViewMetadata), anyList())).thenReturn(allProjectsResponse);

        when(nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE.toLowerCase());
    }

    @Test
    public void whenBuildViewForNode_thenNodeResponseIsReturned() {
        final List<MoData> viewMoDatas = new ArrayList<>();
        final Map<String, Object> nodeMoAttributes = new HashMap<>();
        nodeMoAttributes.put("nodeType", VALID_NODE_TYPE);
        viewMoDatas.add(new MoData(NODE_FDN, nodeMoAttributes, null, null));

        final ResponseDto result = viewResponseDtoBuilder.buildViewForNode(viewMoDatas);

        assertEquals(nodeResponse, result);
    }

    @Test
    public void whenBuildViewForProject_andProjectHasNoNodes_thenProjectOnlyResponseIsReturned() {
        final List<MoData> viewMoDatas = new ArrayList<>();
        viewMoDatas.add(new MoData(PROJECT_FDN, Collections.<String, Object> emptyMap(), null, null));

        final ResponseDto result = viewResponseDtoBuilder.buildViewForProject(viewMoDatas);
        assertEquals(projectOnlyResponse, result);
    }

    @Test
    public void whenBuildViewForProject_andProjectHasNodes_thenProjectWithNodeResponseIsReturned() {
        final List<MoData> viewMoDatas = new ArrayList<>();
        final Map<String, Object> nodeMoAttributes = new HashMap<>();
        nodeMoAttributes.put("nodeType", VALID_NODE_TYPE);
        viewMoDatas.add(new MoData(PROJECT_FDN, Collections.<String, Object> emptyMap(), null, null));
        viewMoDatas.add(new MoData(NODE_FDN, nodeMoAttributes, null, null));

        final ResponseDto result = viewResponseDtoBuilder.buildViewForProject(viewMoDatas);

        assertEquals(projectWithNodesResponse, result);
    }

    @Test
    public void whenBuildViewForAllProjects_thenAllProjectsResponseIsReturned() {
        final ResponseDto result = viewResponseDtoBuilder.buildViewForAllProjects(Collections.<MoData> emptyList());
        assertEquals(allProjectsResponse, result);
    }
}
