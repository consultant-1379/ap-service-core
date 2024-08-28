/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Unit tests for {@link DownloadFolderCleaner}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DownloadFolderCleanerTest {

    private static final String DOWNLOAD_FILE = "downloadFile";

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private APServiceClusterMember apServiceClusterMembership;

    @Mock
    private ResourceService resourceService;

    @Mock
    private Resource resource;

    @InjectMocks
    private DownloadFolderCleaner downloadCleaner;

    @Before
    public void setUp() {
        when(apServiceClusterMembership.isMasterNode()).thenReturn(true);
        when(resourceService.exists(DirectoryConfiguration.getDownloadDirectory())).thenReturn(true);

        final Collection<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceService.listFiles(DirectoryConfiguration.getDownloadDirectory())).thenReturn(resources);
        when(resource.getName()).thenReturn(DOWNLOAD_FILE);
    }

    @Test
    public void whenDownloadDirectoryDoesNotExist_thenNoActionTaken() {
        when(resourceService.exists(DirectoryConfiguration.getDownloadDirectory())).thenReturn(false);
        downloadCleaner.deleteExpiredFiles();
        verify(resourceService, never()).delete(Paths.get(DirectoryConfiguration.getDownloadDirectory(), resource.getName()).toString());
    }

    @Test
    public void when_not_on_master_node_then_no_action_taken() {
        when(apServiceClusterMembership.isMasterNode()).thenReturn(false);
        downloadCleaner.deleteExpiredFiles();
        verify(resourceService, never()).exists(DirectoryConfiguration.getDownloadDirectory());
    }

    @Test
    public void when_downloaded_file_less_than_one_hour_old_then_file_not_deleted() throws IOException {
        final Calendar lessThanOneHourAgo = Calendar.getInstance();
        lessThanOneHourAgo.add(Calendar.MINUTE, -30);
        when(resource.getLastModificationTimestamp()).thenReturn(lessThanOneHourAgo.getTimeInMillis());

        downloadCleaner.deleteExpiredFiles();

        verify(resourceService, never()).delete(Paths.get(DirectoryConfiguration.getDownloadDirectory(), resource.getName()).toString());
    }

    @Test
    public void when_downloaded_file_greater_than_one_hour_old_then_file_deleted() throws IOException {
        final Calendar greaterThanOneHourAgo = Calendar.getInstance();
        greaterThanOneHourAgo.add(Calendar.MINUTE, -90);
        when(resource.getLastModificationTimestamp()).thenReturn(greaterThanOneHourAgo.getTimeInMillis());

        downloadCleaner.deleteExpiredFiles();

        verify(resourceService).delete(Paths.get(DirectoryConfiguration.getDownloadDirectory(), resource.getName()).toString());
    }
}
