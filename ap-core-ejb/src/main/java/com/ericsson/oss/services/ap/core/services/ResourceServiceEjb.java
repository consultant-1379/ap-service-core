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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ejb.Local;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.ResourcesException;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;

/**
 * Provides Auto Provisioning management of resources using Service Framework {@link Resource}. Each {@link Resource} call is executed in a new
 * transaction.
 * <p>
 * Required because Resources JCA component does not support creation of multiple resources in a single transaction so creation and deletion of each
 * resource must be executed in a new transaction.
 */
@Stateless
@Local
@EService
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class ResourceServiceEjb implements ResourceService {

    @javax.annotation.Resource
    private SessionContext context;

    @Inject
    private Logger logger;

    @Inject
    private ResourceFactory resourceFactory;

    @Override
    public int write(final String resourceName, final byte[] content, final boolean append) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.write(content, append);
        } catch (final ResourcesException exception) {
            final String message = "Unable to write to " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public void writeFiles(final Map<String, byte[]> fileWithContents, final boolean append) {
        if (fileWithContents.isEmpty()) {
            return;
        }

        try {
            final String firstFileName = fileWithContents.entrySet().iterator().next().getKey();
            final Resource resource = resourceFactory.getFileSystemResource(firstFileName);
            for (final Entry<String, byte[]> entry : fileWithContents.entrySet()) {
                resource.setURI(entry.getKey());
                resource.write(entry.getValue(), append);
            }
        } catch (final ResourcesException exception) {
            final String message = "Unable to write files: " + fileWithContents.toString();
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public void createDirectory(final String directoryPath) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(directoryPath);
            resource.createDirectory();
        } catch (final ResourcesException exception) {
            final String message = "Unable to create directory " + directoryPath;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public boolean delete(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.delete();
        } catch (final ResourcesException exception) {
            final String message = "Unable to delete " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public void deleteDirectory(final String dirPath) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(dirPath);
            resource.deleteDirectory();
        } catch (final ResourcesException exception) {
            final String message = "Unable to delete directory " + dirPath;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public boolean deleteDirectoryIfEmpty(final String dirPath) {
        final Resource resource = resourceFactory.getFileSystemResource(dirPath);
        if (resource.listFiles().isEmpty()) {
            resource.deleteDirectory();
            return true;
        }
        return false;
    }

    @Override
    public boolean exists(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.exists();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get verify if resource exists : " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public boolean isDirectoryExists(final String dirPath) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(dirPath);
            return resource.isDirectoryExists();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get verify if directory exists : " + dirPath;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public String getAsText(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.getAsText();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get text from " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public OutputStream getOutputStream(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.getOutputStream();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get outputstream for " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public InputStream getInputStream(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.getInputStream();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get inputstream for " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public long getLastModificationTimestamp(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.getLastModificationTimestamp();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get last modification timestamp for " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public boolean supportsWriteOperations(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.supportsWriteOperations();
        } catch (final ResourcesException exception) {
            final String message = "Unable to verify if write is supported for " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public void writeContentsToZip(final String fileUri, final Map<String, byte[]> zipFileContents) {
        checkIfResourceServiceIsAvailable(fileUri);

        try (final OutputStream outputStream = getResourceOutputStream(fileUri);
                final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            writeZipFile(zipFileContents, zipOutputStream);
        } catch (final IOException exception) {
            final String message = "I/O Error writing zip contents to " + fileUri;
            logger.error(message, exception);
            throw new ApApplicationException(message, exception);
        }
    }

    private void checkIfResourceServiceIsAvailable(final String fileUri) {
        if (!context.getBusinessObject(ResourceService.class).exists(fileUri)) {
            context.getBusinessObject(ResourceService.class).write(fileUri, null, false);
        }

        if (!context.getBusinessObject(ResourceService.class).supportsWriteOperations(fileUri)) {
            throw new ApApplicationException("Unable to access system resource " + fileUri);
        }
    }

    private OutputStream getResourceOutputStream(final String fileUri) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(fileUri);
            return resource.getOutputStream();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get outputstream for " + fileUri;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    private static void writeZipFile(final Map<String, byte[]> zipFileContents, final ZipOutputStream zipOutputStream) throws IOException {
        for (final Map.Entry<String, byte[]> entry : zipFileContents.entrySet()) {
            final ZipEntry zipEntry = new ZipEntry(entry.getKey());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.write(entry.getValue());
        }
    }

    @Override
    public byte[] getBytes(final String resourceName) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(resourceName);
            return resource.getBytes();
        } catch (final ResourcesException exception) {
            final String message = "Unable to get byte from " + resourceName;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public Collection<Resource> listFiles(final String directory) {
        try {
            final Resource resource = resourceFactory.getFileSystemResource(directory);
            return resource.listFiles();
        } catch (final ResourcesException exception) {
            final String message = "Unable to list resources in " + directory;
            logger.error(message, exception);
            throw new ApApplicationException(message);
        }
    }

    @Override
    public Collection<String> listDirectories(final String directory) {
        final Path dirPath = Paths.get(directory);
        if (!Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException(String.format("Resource %s is not a directory", directory));
        }

        final List<String> dirNames = new ArrayList<>();

        try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath, new DirectoriesFilter())) {
            for (final Path path : directoryStream) {
                final Path fileName = path.getFileName();
                if (fileName != null) {
                    dirNames.add(fileName.toString());
                }
            }
        } catch (final IOException exception) {
            logger.error("Error reading resource {}", directory, exception);
            throw new ApApplicationException(exception.getMessage());
        }
        return dirNames;
    }

    private static class DirectoriesFilter implements Filter<Path> {
        @Override
        public boolean accept(final Path entry) throws IOException {
            return Files.isDirectory(entry);
        }
    }
}
