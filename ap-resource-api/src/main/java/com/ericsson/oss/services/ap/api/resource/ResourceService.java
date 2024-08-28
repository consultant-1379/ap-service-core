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
package com.ericsson.oss.services.ap.api.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import com.ericsson.oss.itpf.sdk.resources.Resource;

/**
 * Resource Service that will manage CRUD for resources via sdk-resources-api. This interface is to start a new transaction for each of the below
 * calls.
 */
public interface ResourceService {

    /**
     * Creates a directory.
     *
     * @param directoryPath
     *            the filePath of the directory to create
     */
    void createDirectory(final String directoryPath);

    /**
     * Deletes a resource from the file system.
     *
     * @param resourceName
     *            the name of the resource to delete
     * @return true if the resource was deleted successfully
     */
    boolean delete(final String resourceName);

    /**
     * Deletes a directory from the file system.
     *
     * @param directoryPath
     *            the filePath of the directory to delete
     */
    void deleteDirectory(final String directoryPath);

    /**
     * Delete a directory from the file system only if it does not contain any files.
     *
     * @param directoryPath
     *            the filePath of the directory to delete
     * @return true if the directory is deleted successfully.
     */
    boolean deleteDirectoryIfEmpty(final String directoryPath);

    /**
     * Checks if the supplied resource exists on the file system.
     *
     * @param resourceName
     *            the name of the resource to check
     * @return true if the resource exists
     */
    boolean exists(final String resourceName);

    /**
     * Retrieves the resource as a {@link String}.
     *
     * @param resourceName
     *            the name of the resource to retrieve
     * @return the resource as a {@link String}
     */
    String getAsText(final String resourceName);

    /**
     * Retrieves the resource as an array of bytes.
     *
     * @param resourceName
     *            the name of the resource to retrieve
     * @return the resource as an array of bytes
     */
    byte[] getBytes(final String resourceName);

    /**
     * Retrieves the resource as an {@link InputStream}.
     *
     * @param resourceName
     *            the name of the resource to retrieve
     * @return the resource as an {@link InputStream}
     */
    InputStream getInputStream(final String resourceName);

    /**
     * Gets the timestamp of the last modification of the resource.
     *
     * @param resourceName
     *            the name of the resource to retrieve
     * @return long
     * @see Resource#getLastModificationTimestamp()
     */
    long getLastModificationTimestamp(final String resourceName);

    /**
     * Retrieves the resource as an {@link OutputStream}.
     *
     * @param resourceName
     *            the name of the resource to retrieve
     * @return the resource as an {@link OutputStream}
     */
    OutputStream getOutputStream(String resourceName);

    /**
     * Checks if the supplied directory exists on the file system.
     *
     * @param directoryPath
     *            the filePath of the directory to check
     * @return true if the directory exists
     */
    boolean isDirectoryExists(final String directoryPath);

    /**
     * Lists all {@link Resource}s in the specified directory.
     *
     * @param directoryPath
     *            the filePath of the directory to check
     * @return a {@link Collection} of all {@link Resource}s in the directory
     */
    Collection<Resource> listFiles(final String directoryPath);

    /**
     * Lists all sub-directories in the specified directory.
     *
     * @param directoryPath
     *            the filePath of the directory to check
     * @return a {@link Collection} of all sub-directories
     */
    Collection<String> listDirectories(final String directoryPath);

    /**
     * Checks if the supplied resource supports 'write' operations.
     *
     * @param resourceName
     *            the name of the resource to check
     * @return true if the resource supports 'write' operations
     */
    boolean supportsWriteOperations(final String resourceName);

    /**
     * Writes the supplied resource with the supplied content.
     *
     * @param resourceFilePath
     *            the filePath of the resource to write
     * @param content
     *            the contents of the resource
     * @param append
     *            whether to append an existing resource
     * @return int the response code of the operation
     * @see Resource#write(byte[], boolean)
     */
    int write(final String resourceFilePath, final byte[] content, final boolean append);

    /**
     * Write a suite of files into to a zip file. If the directory and/or file do not exist it will be created.
     *
     * @param fileUri
     *            name of the zip file to write to
     * @param zipFileContents
     *            the map representing the files to write to the zip
     */
    void writeContentsToZip(final String fileUri, final Map<String, byte[]> zipFileContents);

    /**
     * Support write multiple files in one transaction.
     *
     * @param fileContents
     *            the map representing the file name and file contents
     * @param append
     *            whether to append an existing resource
     */
    void writeFiles(final Map<String, byte[]> fileContents, final boolean append);
}
