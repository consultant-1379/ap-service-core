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

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.core.metadata.cli.cache.xml.MetadataResourceUnmarshaller;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;
import com.ericsson.oss.services.ap.core.metadata.cli.upgrade.ResourceChangeCallback;
import com.ericsson.oss.services.ap.core.metadata.cli.upgrade.ResourceChangeListener;

/**
 * Implements a cache used to store all the CLI metadata. There will be one <code>NodeCliMetadata</code> object per node type supported.
 * <p>
 * The cache contents is initialized at managed bean creation.
 */
@ApplicationScoped
public class MetadataCache {

    /**
     * One <code>NodeCliMetadata</code> object per node type supported. Each <code>NodeCliMetadata</code> can contain one or more views.
     */
    private CopyOnWriteArrayList<CliViews> cache;

    @Inject
    private MetadataResourceUnmarshaller metadataResourceUnmarshaller;

    @Inject
    private ResourceChangeListener resourceChangeListener;

    /**
     * Returns unfiltered contents of the cache.
     *
     * @return list containing metadata for each node type supported
     */
    public Collection<CliViews> getAllMetadata() {
        return Collections.unmodifiableList(cache);
    }

    /**
     * Reloads the cache with all the current metadata.
     */
    public void reloadCache() {
        final String cliMetadataDirectory = DirectoryConfiguration.getCliMetaDataDirectory();
        final Collection<CliViews> metadata = metadataResourceUnmarshaller.loadMetadata(cliMetadataDirectory, CliViews.class);
        switchToUpdatedCache(metadata);
    }

    private void switchToUpdatedCache(final Collection<CliViews> metadata) {
        final CopyOnWriteArrayList<CliViews> reloadedCache = new CopyOnWriteArrayList<>(metadata);
        cache = reloadedCache;
    }

    @PostConstruct
    public void initialize() {
        initializeDataUpgrade();
        initializeCacheData();
    }

    private void initializeCacheData() {
        cache = new CopyOnWriteArrayList<>();
        final String cliMetadataDirectory = DirectoryConfiguration.getCliMetaDataDirectory();

        final Collection<CliViews> metadataModels = metadataResourceUnmarshaller.loadMetadata(cliMetadataDirectory, CliViews.class);
        cache.addAll(metadataModels);
    }

    private void initializeDataUpgrade() {
        resourceChangeListener.setCallbackListner(new ResourceChangeCallback() {

            @Override
            public void resourceChanged() {
                reloadCache();
            }
        });

        resourceChangeListener.registerListner();
    }
}
