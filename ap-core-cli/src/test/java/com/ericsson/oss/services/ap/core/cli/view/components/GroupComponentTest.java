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
package com.ericsson.oss.services.ap.core.cli.view.components;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.view.components.LineComponentTest.TestData;
import com.ericsson.oss.services.ap.core.metadata.cli.api.GroupMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;

/**
 * Unit tests for {@link GroupComponent}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupComponentTest {

    @Spy
    private ResponseDtoBuilder responseBuilder; // NOPMD

    @Mock
    private GroupMetadata groupMetadata;

    @InjectMocks
    private GroupComponent groupComponent;

    @Test
    public void group_created_with_heading() {
        final String testHeading = "Test Heading";
        when(groupMetadata.getHeading()).thenReturn(testHeading);

        final List<TestData> dataList = new ArrayList<>();
        groupComponent.setComponentMetadata(groupMetadata);
        final Collection<AbstractDto> result = groupComponent.getAbstractDtos(dataList, null);

        final List<AbstractDto> actualViewDtos = new ArrayList<>(result);
        final LineDto actualLineDto = (LineDto) actualViewDtos.get(0);
        final String actualValue = actualLineDto.getValue();

        assertTrue("Group heading not expected value", actualValue.contains(testHeading));
    }

    @Test
    public void group_created_with_no_heading() {
        when(groupMetadata.getHeading()).thenReturn(null);
        groupComponent.setComponentMetadata(groupMetadata);
        final Collection<AbstractDto> result = groupComponent.getAbstractDtos(Collections.<Object> emptyList(), null);
        final List<AbstractDto> actualViewDtos = new ArrayList<>(result);

        assertTrue("Group heading not expected value", actualViewDtos.isEmpty());
    }

    @Test
    public void whenGetChildMetadata_thenValidResponseIsReturned() {
        when(groupMetadata.getViewComponentsMetadata()).thenReturn(Collections.<Metadata> emptyList());
        final Collection<Metadata> result = groupComponent.getChildMetadata();
        assertTrue(result.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void whenGetChildMetadata_andGroupMetadataIsNotSet_thenIllegalStateExceptionIsThrown() {
        groupComponent.setComponentMetadata(null);
        groupComponent.getChildMetadata();
    }

    @Test(expected = IllegalStateException.class)
    public void whenGetAbstractDtos_andGroupMetadataIsNotSet_thenIllegalStateExceptionIsThrown() {
        groupComponent.setComponentMetadata(null);
        groupComponent.getAbstractDtos(Collections.<Object> emptyList(), null);
    }
}
