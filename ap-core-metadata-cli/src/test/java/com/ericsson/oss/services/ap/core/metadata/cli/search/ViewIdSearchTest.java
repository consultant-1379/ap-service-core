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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;

/**
 * Unit tests for {@link ViewIdSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewIdSearchTest {

    @Mock
    private CliViews expectedNodeCliMetadata;

    @Mock
    private ViewMetadata viewMetadataX, expectedViewMetadata;

    @Test
    public void whenFilteringUsingViewId_thenCorrectMetadataIsReturned() {
        final Collection<CliViews> metadataForAllNodeTypes = new ArrayList<>();
        metadataForAllNodeTypes.add(expectedNodeCliMetadata);

        final String expectedViewId = "view_expected";
        final String notExpectedViewId = "view_X";

        final List<ViewMetadata> viewMetadata = new ArrayList<>();
        viewMetadata.add(viewMetadataX);
        viewMetadata.add(expectedViewMetadata);

        given(expectedNodeCliMetadata.getViews()).willReturn(viewMetadata);
        given(viewMetadataX.getId()).willReturn(notExpectedViewId);
        given(expectedViewMetadata.getId()).willReturn(expectedViewId);

        final ViewIdSearch findViewMatchingViewId = new ViewIdSearch(expectedViewId);
        final ViewMetadata actualViewMetadata = findViewMatchingViewId.execute(metadataForAllNodeTypes);

        assertSame("Cant find expected metadata for given view ID", actualViewMetadata, expectedViewMetadata);
    }

    @Test
    public void whenFilteringUsingViewId_AndNoMetadataExists_thenNullIsReturned() {
        final String expectedViewId = "view_expected";
        final String notExpectedViewId = "view_X";

        final List<ViewMetadata> viewMetadata = new ArrayList<>();
        viewMetadata.add(viewMetadataX);
        viewMetadata.add(expectedViewMetadata);

        given(expectedNodeCliMetadata.getViews()).willReturn(viewMetadata);
        given(viewMetadataX.getId()).willReturn(notExpectedViewId);
        given(expectedViewMetadata.getId()).willReturn(expectedViewId);

        final ViewIdSearch findViewMatchingViewId = new ViewIdSearch(expectedViewId);
        final ViewMetadata actualViewMetadata = findViewMatchingViewId.execute(Collections.<CliViews> emptyList());

        assertNull(actualViewMetadata);
    }
}
