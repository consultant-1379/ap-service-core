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
package com.ericsson.oss.services.ap.common.artifacts.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.ejb.EJBTransactionRolledbackException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;

/**
 * Unit tests for {@link ArtifactResourceOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactResourceOperationsTest {

    private static final byte[] FILE_CONTENT = "fileContents".getBytes();
    private static final String TEMP_FILE = "/tmp/file1";
    private static final String TEMP_RAW = "/tmp/raw/file1";
    private static final String TEMP_GENERATED = "/tmp/generated/File1.xml";

    @Mock
    private ResourceService resourceService;

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private ArtifactResourceOperations artifactResourceOperations;

    @Test
    public void whenDeleteFileAndFileExistsThenFileIsDeleted() {
        final Collection<Resource> resources = new ArrayList<>();
        resources.add(Mockito.mock(Resource.class));
        resources.add(Mockito.mock(Resource.class));

        when(resourceService.isDirectoryExists("/tmp")).thenReturn(true);
        when(resourceService.listFiles(TEMP_FILE)).thenReturn(resources);

        artifactResourceOperations.deleteFile(TEMP_FILE);
        verify(resourceService).delete(TEMP_FILE);
    }

    @Test
    public void whenDeleteFileThenDirectoryIsDeletedWhenLastFile() {
        final File file = new File(TEMP_RAW);
        when(resourceService.isDirectoryExists(file.getParent())).thenReturn(true);

        artifactResourceOperations.deleteFile(file.getPath());
        verify(resourceService).delete(file.getPath());
        verify(resourceService).deleteDirectoryIfEmpty(file.getParent());
    }

    @Test
    public void whenCheckForSingleFileInDirAndSingleFileExistsThenReturnTrue() {
        final File file = new File(TEMP_RAW);

        final Collection<Resource> resources = new ArrayList<>();
        resources.add(Mockito.mock(Resource.class));

        when(resourceService.isDirectoryExists(file.getParent())).thenReturn(true);
        when(resourceService.listFiles(file.getParent())).thenReturn(resources);

        assertTrue(artifactResourceOperations.isSingleFileInDirectory(file.getPath()));
    }

    @Test
    public void whenCheckForSingleFileInDirAndNoFileExistsThenReturnFalse() {
        final File file = new File(TEMP_RAW);

        when(resourceService.isDirectoryExists(file.getParent())).thenReturn(true);
        when(resourceService.listFiles(file.getParent())).thenReturn(Collections.<Resource> emptyList());

        assertFalse(artifactResourceOperations.isSingleFileInDirectory(file.getPath()));
    }

    @Test
    public void whenCheckForSingleFileInDirAndMultipleFilesExistsThenReturnFalse() {
        final File file = new File(TEMP_RAW);

        final Collection<Resource> resources = new ArrayList<>();
        resources.add(Mockito.mock(Resource.class));
        resources.add(Mockito.mock(Resource.class));

        when(resourceService.isDirectoryExists(file.getParent())).thenReturn(true);
        when(resourceService.listFiles(file.getParent())).thenReturn(resources);

        assertFalse(artifactResourceOperations.isSingleFileInDirectory(file.getPath()));
    }

    @Test
    public void whenCheckForSingleFileInDirAndDirDoesNotExistThenReturnFalse() {
        final File file = new File(TEMP_RAW);
        when(resourceService.isDirectoryExists(file.getParent())).thenReturn(false);
        assertFalse(artifactResourceOperations.isSingleFileInDirectory(file.getPath()));
    }

    @Test
    public void whenWriteArtifactIsRequestedThenFirstAttemptSucceeds() {
        when(resourceService.write(TEMP_GENERATED, FILE_CONTENT, false)).thenReturn(1);
        artifactResourceOperations.writeArtifact(TEMP_GENERATED, FILE_CONTENT);
        verify(resourceService, times(1)).write(TEMP_GENERATED, FILE_CONTENT, false);
    }

    @Test
    public void whenStaleFileErrorOccursThenWriteArtifactIsRetriedAndThenSucceeds() {
        final EJBTransactionRolledbackException rollbackException = new EJBTransactionRolledbackException();
        rollbackException.initCause(new Exception("ARJUNA016053: Could not commit transaction"));
        when(resourceService.write(TEMP_GENERATED, FILE_CONTENT, false)).thenThrow(rollbackException).thenReturn(1);

        artifactResourceOperations.writeArtifact(TEMP_GENERATED, FILE_CONTENT);
        verify(resourceService, times(2)).write(TEMP_GENERATED, FILE_CONTENT, false);

    }

    @Test
    public void whenStaleFileErrorOccursThenWriteArtifactsIsRetriedAndThenSucceeds() {
        final Map<String, byte[]> artifacts = Collections.<String, byte[]> emptyMap();
        final EJBTransactionRolledbackException rollbackException = new EJBTransactionRolledbackException();
        rollbackException.initCause(new Exception("ARJUNA016053: Could not commit transaction"));

        doThrow(rollbackException).doNothing().when(resourceService).writeFiles(artifacts, false);

        artifactResourceOperations.writeArtifacts(Collections.<String, byte[]> emptyMap());
        verify(resourceService, times(2)).writeFiles(artifacts, false);
    }

    //Disabled this test method as it takes 45 seconds to run    @Test(expected = APServiceException.class)
    public void whenStaleFileErrorOccursThenWriteArtifactsIsRetriedMaxTimes() {
        final Map<String, byte[]> artifacts = Collections.<String, byte[]> emptyMap();
        final EJBTransactionRolledbackException rollbackException = new EJBTransactionRolledbackException();
        rollbackException.initCause(new Exception("ARJUNA016053: Could not commit transaction"));

        doThrow(rollbackException).when(resourceService).writeFiles(artifacts, false);

        artifactResourceOperations.writeArtifacts(Collections.<String, byte[]> emptyMap());
    }

    @Test
    public void whenCheckForNonEmptyDirectoryAndDirectoryDoesNotExistThenReturnFalse() {
        when(resourceService.isDirectoryExists("/tmp")).thenReturn(false);
        assertFalse(artifactResourceOperations.directoryExistAndNotEmpty(TEMP_FILE));
    }

    @Test
    public void whenCheckForNonEmptyDirectoryAndDirectoryIsEmptyThenReturnFalse() {
        when(resourceService.isDirectoryExists("/tmp")).thenReturn(true);
        when(resourceService.listDirectories(TEMP_FILE)).thenReturn(Collections.<String> emptyList());
        when(resourceService.listFiles(TEMP_FILE)).thenReturn(Collections.<Resource> emptyList());

        assertFalse(artifactResourceOperations.directoryExistAndNotEmpty(TEMP_FILE));
    }

    @Test
    public void whenCheckForNonEmptyDirectoryAndDirectoryContainsDirectoriesThenReturnTrue() {
        final Collection<String> listDirectories = new ArrayList<>();
        listDirectories.add("dir1");

        when(resourceService.isDirectoryExists("/tmp/file1")).thenReturn(true);
        when(resourceService.listDirectories(TEMP_FILE)).thenReturn(listDirectories);
        when(resourceService.listFiles(TEMP_FILE)).thenReturn(Collections.<Resource> emptyList());

        assertTrue(artifactResourceOperations.directoryExistAndNotEmpty(TEMP_FILE));
    }

    @Test
    public void whenCheckForNonEmptyDirectoryAndDirectoryContainsFilesThenReturnTrue() {
        final Collection<Resource> listFiles = new ArrayList<>();
        listFiles.add(Mockito.mock(Resource.class));

        when(resourceService.isDirectoryExists("/tmp/file1")).thenReturn(true);
        when(resourceService.listDirectories(TEMP_FILE)).thenReturn(Collections.<String> emptyList());
        when(resourceService.listFiles(TEMP_FILE)).thenReturn(listFiles);

        assertTrue(artifactResourceOperations.directoryExistAndNotEmpty(TEMP_FILE));

    }

    @Test
    public void whenCheckForNonEmptyDirectoryContainsFilesAndDirectoriesThenReturnTrue() {
        final Collection<String> listDirectories = new ArrayList<>();
        listDirectories.add("dir1");
        final Collection<Resource> listFiles = new ArrayList<>();
        listFiles.add(Mockito.mock(Resource.class));

        when(resourceService.isDirectoryExists("/tmp/file1")).thenReturn(true);
        when(resourceService.listDirectories(TEMP_FILE)).thenReturn(listDirectories);
        when(resourceService.listFiles(TEMP_FILE)).thenReturn(listFiles);

        assertTrue(artifactResourceOperations.directoryExistAndNotEmpty(TEMP_FILE));
    }
}
