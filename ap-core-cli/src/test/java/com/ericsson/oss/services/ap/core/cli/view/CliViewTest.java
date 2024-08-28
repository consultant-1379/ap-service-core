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
package com.ericsson.oss.services.ap.core.cli.view;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.core.cli.view.navigator.MetadataModelNavigator;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;

/**
 * Unit tests for {@link CliView}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CliViewTest {

    @Mock
    private MetadataModelNavigator metadataModelNavigator;

    @Mock
    private ViewMetadata viewMetadata;

    @InjectMocks
    private CliView cliView;

    @Test
    public void whenBuildingView_thenValidResponseIsReturned() {
        final List<Object> dataSource = new ArrayList<>();
        dataSource.add(new MoData(null, null, null, null));
        final List<AbstractDto> views = new ArrayList<>();
        views.add(new LineDto());
        when(metadataModelNavigator.constructView(viewMetadata, dataSource)).thenReturn(views);

        final ResponseDto actualResponseDto = cliView.buildViewFromMetadata(viewMetadata, dataSource);
        assertFalse(actualResponseDto.getElements().isEmpty());
    }

    @Test
    public void whenBuildingView_andInputIsEmptyDataSource_thenEmptyViewIsReturned() {
        final ResponseDto actualResponseDto = cliView.buildViewFromMetadata(null, Collections.<Object> emptyList());
        assertTrue(actualResponseDto.getElements().isEmpty());
    }
}
