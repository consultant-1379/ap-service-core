/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import javax.ejb.TimerService;
import javax.inject.Inject;

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
 * Unit tests for {@link EoiFilesCleanUpEjb}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EoiFilesCleanUpEjbTest {

    private static final String JSON_FILE = "node1_day0.json";

    @Mock
    private TimerService timerService;

    @Mock
    private Logger logger;

    @Mock
    private APServiceClusterMember apServiceClusterMembership;

    @Mock
    private ResourceService resourceService;

    @Mock
    private Resource resource;

    @InjectMocks
    private EoiFilesCleanUpEjb eoiFilesCleanUpEjb;

    @Before
    public void setUp() throws URISyntaxException {
        eoiFilesCleanUpEjb.init();
        when(apServiceClusterMembership.isMasterNode()).thenReturn(true);
        when(resourceService.exists(DirectoryConfiguration.getArtifactsDirectory())).thenReturn(true);

        final Collection<String> projectDirectories = new ArrayList<>();
        projectDirectories.add("project1");
        final Collection<String> nodeDirectories = new ArrayList<>();
        nodeDirectories.add("node1");
        final Collection<Resource> resources = new ArrayList<>();
        resources.add(resource);
        when(resourceService.listDirectories(DirectoryConfiguration.getArtifactsDirectory())).thenReturn(projectDirectories);
        when(resourceService.listDirectories(DirectoryConfiguration.getProjectDirectory("project1"))).thenReturn(nodeDirectories);
        when(resourceService.listFiles(DirectoryConfiguration.getNodeDirectory("project1", "node1"))).thenReturn(resources);
        when(resource.getName()).thenReturn(JSON_FILE);
    }

    @Test
    public void whenGeneratedDirectoryDoesNotExistThenNoActionIsTaken() {
        when(resourceService.exists(DirectoryConfiguration.getArtifactsDirectory())).thenReturn(false);
        eoiFilesCleanUpEjb.start();
        verify(resourceService, never()).delete(Paths.get(DirectoryConfiguration.getArtifactsDirectory(), resource.getName()).toString());
    }

    @Test
    public void whenNotOnMasterNodeThenNoActionIsTaken() {
        when(apServiceClusterMembership.isMasterNode()).thenReturn(false);
        eoiFilesCleanUpEjb.start();
        verify(resourceService, never()).exists(DirectoryConfiguration.getArtifactsDirectory());
    }

    @Test
    public void whenGeneratedFileIsLessThanOneDayAgoThenFileIsNotDeleted() throws IOException {
        final Calendar lessThanOneDayAgo = Calendar.getInstance();
        lessThanOneDayAgo.add(Calendar.MINUTE, -20);
        when(resource.getLastModificationTimestamp()).thenReturn(lessThanOneDayAgo.getTimeInMillis());
        eoiFilesCleanUpEjb.start();
        verify(resourceService, never()).delete(Paths.get(DirectoryConfiguration.getArtifactsDirectory(), resource.getName()).toString());
    }

    @Test
    public void whenGeneratedFileIsGreaterThanOneDayAgoThenFileIsDeletedSuccessfully() throws IOException {
        final Calendar greaterThanOneDayAgo = Calendar.getInstance();
        greaterThanOneDayAgo.add(Calendar.HOUR, -26);
        when(resource.getLastModificationTimestamp()).thenReturn(greaterThanOneDayAgo.getTimeInMillis());
        eoiFilesCleanUpEjb.start();
        verify(resourceService, times(1)).delete(Paths.get(DirectoryConfiguration.getNodeDirectory("project1","node1"), resource.getName()).toString());
    }
}
