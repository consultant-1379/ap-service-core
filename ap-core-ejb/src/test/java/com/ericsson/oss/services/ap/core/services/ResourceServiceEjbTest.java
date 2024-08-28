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
package com.ericsson.oss.services.ap.core.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.ejb.SessionContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.oss.itpf.sdk.resources.ResourcesException;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;

/**
 * Unit tests for {@link ResourceServiceEjb}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceServiceEjbTest {

    private static final String TEMP_RESOURCE = "tmp";
    private static final String TEMP_DIRECTORY = "/tmp/xxx";
    private static final byte[] FILE_CONTENTS = "fileContent".getBytes();
    private static final String TEMP_DOWNLOAD_FILE = System.getProperty("java.io.tmpdir") + "DummyUri.zip";

    private final Map<String, byte[]> zipFileContents = new HashMap<>();

    @Mock
    private ZipOutputStream mockZipOutputStream;

    @Mock
    private SessionContext sessionContext;

    @Mock
    private Resource resource;

    @Mock
    private ResourceFactory resourceFactory;

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    @Spy
    private ResourceServiceEjb resourceService;

    @Before
    public void setUp() {
        zipFileContents.put("file1.xml", FILE_CONTENTS);
        when(sessionContext.getBusinessObject(ResourceService.class)).thenReturn(resourceService);
        when(resourceFactory.getFileSystemResource(anyString())).thenReturn(resource);
    }

    @Test(expected = ApApplicationException.class)
    public void whenWriteContents_andErrorOccurs_thenApApplicationExceptionIsThrown() {
        doThrow(ResourcesException.class).when(resource).write(any(byte[].class), anyBoolean());
        resourceService.write(TEMP_DOWNLOAD_FILE, FILE_CONTENTS, false);
    }

    @Test
    public void whenWriteFiles_andNoFilesArePassedIn_thenNothingHappens() {
        resourceService.writeFiles(Collections.<String, byte[]> emptyMap(), true);
        verify(resourceFactory, never()).getFileSystemResource(anyString());
    }

    @Test
    public void whenWriteFiles_andHasWithThreeFiles_thenThreeFilesAreWritten() {
        final Map<String, byte[]> filesToWrite = new HashMap<>();
        filesToWrite.put("file1", FILE_CONTENTS);
        filesToWrite.put("file2", FILE_CONTENTS);
        filesToWrite.put("file3", FILE_CONTENTS);

        resourceService.writeFiles(filesToWrite, true);
        verify(resource, times(3)).write(FILE_CONTENTS, true);
    }

    @Test(expected = ApApplicationException.class)
    public void whenWriteFiles_andErrorOccurs_thenApApplicationExceptionIsThrown() {
        doThrow(ResourcesException.class).when(resource).write(any(byte[].class), anyBoolean());
        final Map<String, byte[]> filesToWrite = new HashMap<>();
        filesToWrite.put("file1", FILE_CONTENTS);
        filesToWrite.put("file2", FILE_CONTENTS);
        filesToWrite.put("file3", FILE_CONTENTS);

        resourceService.writeFiles(filesToWrite, true);
    }

    @Test
    public void testWriteContentsToZipSuccessful() {
        when(resourceService.supportsWriteOperations(TEMP_DOWNLOAD_FILE)).thenReturn(true);
        when(resourceService.getOutputStream(TEMP_DOWNLOAD_FILE)).thenReturn(mockZipOutputStream);
        when(resource.write(any(byte[].class), anyBoolean())).thenReturn(0);
        when(resource.getOutputStream()).thenReturn(mockZipOutputStream);

        resourceService.writeContentsToZip(TEMP_DOWNLOAD_FILE, zipFileContents);
    }

    @Test(expected = ApApplicationException.class)
    public void testWriteContentsToZipGetOutputStreamException() {
        when(resourceService.supportsWriteOperations(TEMP_DOWNLOAD_FILE)).thenReturn(true);
        when(resourceService.getOutputStream(TEMP_DOWNLOAD_FILE)).thenReturn(mockZipOutputStream);
        when(resource.write(any(byte[].class), anyBoolean())).thenReturn(0);
        doThrow(ResourcesException.class).when(resource).getOutputStream();

        resourceService.writeContentsToZip(TEMP_DOWNLOAD_FILE, zipFileContents);
    }

    @Test(expected = ApApplicationException.class)
    public void testWriteContentsToZipSystemResourceUnavailable() {
        final Resource res = Resources.getFileSystemResource(TEMP_DOWNLOAD_FILE);
        if (res.exists()) {
            res.delete();
        }
        when(resourceService.exists(TEMP_DOWNLOAD_FILE)).thenReturn(false);
        doThrow(ApApplicationException.class).when(resourceService).write(eq(TEMP_DOWNLOAD_FILE), any(byte[].class), anyBoolean());

        resourceService.writeContentsToZip(TEMP_DOWNLOAD_FILE, zipFileContents);
    }

    @Test(expected = ApApplicationException.class)
    public void testWriteContentsToZipWithUnsupportedWrite() {
        final Resource res = Resources.getFileSystemResource(TEMP_DOWNLOAD_FILE);
        if (res.exists()) {
            res.delete();
        }
        when(resourceService.supportsWriteOperations(TEMP_DOWNLOAD_FILE)).thenReturn(false);

        resourceService.writeContentsToZip(TEMP_DOWNLOAD_FILE, zipFileContents);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenListDirectories_andDirectoryDoesNotExist_thenIllegalArgumentExceptionIsPropagated() {
        resourceService.listDirectories(TEMP_DIRECTORY);
    }

    @Test
    public void whenListDirectories_andDirectoryExists_thenDirectoryNamesAreReturned() {
        final Collection<String> result = resourceService.listDirectories("/");
        assertFalse(result.isEmpty());
    }

    @Test
    public void testListResourcesInDirectory() {
        final Collection<Resource> expectedResourceCollection = new ArrayList<>();
        expectedResourceCollection.add(resource);
        when(resource.listFiles()).thenReturn(expectedResourceCollection);

        final Collection<Resource> actualResourceCollection = resourceService.listFiles(anyString());

        assertTrue(actualResourceCollection.contains(resource));
    }

    @Test
    public void testDeleteDirectoryIfEmptyWithContents() {
        final Collection<Resource> expectedResourceCollection = new ArrayList<>();
        expectedResourceCollection.add(resource);
        when(resource.listFiles()).thenReturn(expectedResourceCollection);

        assertFalse(resourceService.deleteDirectoryIfEmpty(TEMP_DIRECTORY));

        verify(resource, never()).delete();
    }

    @Test
    public void testDeleteDirectoryIfEmptyWithNoContents() {
        final Collection<Resource> expectedResourceCollection = new ArrayList<>();
        when(resource.listFiles()).thenReturn(expectedResourceCollection);

        assertTrue(resourceService.deleteDirectoryIfEmpty(TEMP_DIRECTORY));

        verify(resource).deleteDirectory();
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenCreateDirectoryFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).createDirectory();
        resourceService.createDirectory(TEMP_DIRECTORY);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenDeleteFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).delete();
        resourceService.delete(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenDeleteDirectoryFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).deleteDirectory();
        resourceService.deleteDirectory(TEMP_DIRECTORY);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenCheckingResourceExistsFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).exists();
        resourceService.exists(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenCheckingDirectoryExistsFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).isDirectoryExists();
        resourceService.isDirectoryExists(TEMP_DIRECTORY);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenConvertingResourceToTextFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).getAsText();
        resourceService.getAsText(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenGetOutputStreamFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).getOutputStream();
        resourceService.getOutputStream(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenGetInputStreamFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).getInputStream();
        resourceService.getInputStream(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenGetLastModTimestampFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).getLastModificationTimestamp();
        resourceService.getLastModificationTimestamp(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenSupportsWriteOperationsFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).supportsWriteOperations();
        resourceService.supportsWriteOperations(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenGetBytesFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).getBytes();
        resourceService.getBytes(TEMP_RESOURCE);
    }

    @Test(expected = ApApplicationException.class)
    public void testWhenListFilesFails_ExceptionIsWrappedAsAPException() {
        doThrow(ResourcesException.class).when(resource).listFiles();
        resourceService.listFiles(TEMP_RESOURCE);
    }
}
