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
package com.ericsson.oss.services.ap.core.metadata.cli.search;

import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;

/**
 * Unit tests for {@link NodeTypeAndViewIdSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeTypeAndViewIdSearchTest {

    @Mock
    private CliViews nodeCliMetadataNodeX, nodeCliMetadataNodeY;

    @Mock
    private ViewMetadata viewMetadataX, expectedViewMetadata;

    @Test
    public void when_filtering_using_node_type_correct_metadata_returned() {

        final List<CliViews> metadataForAllNodeTypes = new ArrayList<>();
        metadataForAllNodeTypes.add(nodeCliMetadataNodeX);
        metadataForAllNodeTypes.add(nodeCliMetadataNodeY);

        final String expectedNodeType = "type_expected";
        final String notExpectedNodeType = "type_X";

        final String expectedViewId = "view_expected";
        final String notExpectedViewId = "view_X";

        final List<ViewMetadata> viewMetadata = new ArrayList<>();
        viewMetadata.add(viewMetadataX);
        viewMetadata.add(expectedViewMetadata);

        given(nodeCliMetadataNodeX.getNamespace()).willReturn(notExpectedNodeType);
        given(nodeCliMetadataNodeY.getNamespace()).willReturn(expectedNodeType);

        given(nodeCliMetadataNodeY.getViews()).willReturn(viewMetadata);
        given(nodeCliMetadataNodeX.getViews()).willReturn(viewMetadata);

        given(viewMetadataX.getId()).willReturn(notExpectedViewId);
        given(expectedViewMetadata.getId()).willReturn(expectedViewId);

        final NodeTypeAndViewIdSearch metadataFilter = new NodeTypeAndViewIdSearch(expectedNodeType, expectedViewId);
        final ViewMetadata actualViewMetadata = metadataFilter.execute(metadataForAllNodeTypes);

        assertSame("Cant find expected metadata for given node type and view id", actualViewMetadata, expectedViewMetadata);
    }
}
