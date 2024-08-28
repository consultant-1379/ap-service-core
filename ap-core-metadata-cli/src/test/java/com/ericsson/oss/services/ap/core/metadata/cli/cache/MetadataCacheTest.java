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
package com.ericsson.oss.services.ap.core.metadata.cli.cache;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.core.metadata.cli.cache.xml.MetadataResourceUnmarshaller;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;
import com.ericsson.oss.services.ap.core.metadata.cli.upgrade.ResourceChangeListener;

/**
 * Unit tests for {@link MetadataCache}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataCacheTest {

    private final CliViews cacheEntryOne = new CliViews();
    private final CliViews cacheEntryTwo = new CliViews();

    @Mock
    private MetadataResourceUnmarshaller metadataResourceBuilder;

    @Mock
    private ResourceChangeListener resourceChangeListener;

    @InjectMocks
    private MetadataCache metadataCache;

    @Before
    public void initializeCacheWithData() {
        final Collection<CliViews> cacheEntries = new ArrayList<>();
        cacheEntries.add(cacheEntryOne);
        cacheEntries.add(cacheEntryTwo);

        given(metadataResourceBuilder.loadMetadata(DirectoryConfiguration.getCliMetaDataDirectory(), CliViews.class)).willReturn(cacheEntries);
        given(resourceChangeListener.registerListner()).willReturn(true);

        metadataCache.initialize();
    }

    @Test
    public void cache_contains_expected_contents_after_initialization() {
        final Collection<CliViews> models = metadataCache.getAllMetadata();
        assertThat("Cache does not contain expected content after initialization", new ArrayList<>(models),
                hasItems(cacheEntryOne, cacheEntryTwo));
    }

    @Test
    public void cache_contains_expected_contents_after_reload() {
        metadataCache.reloadCache();
        final Collection<CliViews> models = metadataCache.getAllMetadata();
        assertThat("Cache does not contain expected content after reload", new ArrayList<>(models), hasItems(cacheEntryOne, cacheEntryTwo));
    }
}
