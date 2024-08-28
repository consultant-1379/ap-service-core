/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.scriptengine.spi.dtos.file.FileSystemLocatedFileDto;

/**
 * Unit tests for {@link AutoProvisioningFileDownloadExecutor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoProvisioningFileDownloadExecutorTest {

    private static final String DOWNLOADED_FILE_NAME = "SiteInstall.xml";
    private static final String GENERATED_FILE_NAME = "dd6b7d3f-a477-4b0d-be2e-27b8c42f5d67_" + DOWNLOADED_FILE_NAME;
    private static final String FULL_DOWNLOAD_PATH = DirectoryConfiguration.getDownloadDirectory() + File.separator + GENERATED_FILE_NAME;

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private AutoProvisioningFileDownloadExecutor fileDownloader;

    @Test
    public void testDownloadFileExists() {
        when(resourceService.exists(FULL_DOWNLOAD_PATH)).thenReturn(true);

        final FileSystemLocatedFileDto response = (FileSystemLocatedFileDto) fileDownloader.execute(GENERATED_FILE_NAME);
        assertEquals(response.getFileName(), DOWNLOADED_FILE_NAME);
    }

    @Test(expected = IllegalStateException.class)
    public void testDownloadFileDirectoryNotExist() {
        when(resourceService.exists(FULL_DOWNLOAD_PATH)).thenReturn(false);
        fileDownloader.execute(GENERATED_FILE_NAME);
    }
}