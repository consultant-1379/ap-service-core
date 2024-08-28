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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;

/**
 * Common set of resources operations for handling raw and generated artifacts.
 */
public class ArtifactResourceOperations {

    private static final int MAX_RETRIES = 15;
    private static final int RETRY_INTERVAL_IN_SECONDS = 3;
    private static final String REPLACE_REGEX = "\\<\\?xml(.+?)\\?\\>";
    private static final String NETCONF_KEY_CHARACTOR = "<rpc";
    private static final String BULK_3GPP_KEY_CHARACTOR = "<bulkCmConfigDataFile";
    private static final String BASELINE_ARTIFACT_TAG = "baseline";

    @Inject
    private Logger logger;

    private ResourceService resourceService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Reads the artifact at the specfied file location.
     *
     * @param artifactLocation
     * @return the file contents
     */
    public byte[] readArtifact(final String artifactLocation) {
        return resourceService.getBytes(artifactLocation);
    }

    /**
     * Reads the artifact at the specfied file location.
     *
     * @param artifactLocation
     * @return the file contents in string value
     */
    public String readArtifactAsText(final String artifactLocation) {
        if (resourceService.exists(artifactLocation)){
            try(InputStreamReader reader = new InputStreamReader(resourceService.getInputStream(artifactLocation), StandardCharsets.UTF_8)) {
                return new BufferedReader(reader)
                      .lines()
                      .collect(Collectors.joining(System.lineSeparator()));
            } catch (final Exception e){
                logger.error("Failed to read file {}", artifactLocation, e);
            }
        } else {
            logger.warn("File {} does not exist.", artifactLocation);
        }
        return "";
    }

    /**
     * Reads the artifact file format at the specfied file location.
     *
     * @param type artifact type
     * @param artifactLocation artifact file location
     * @return the file format
     */
    public ArtifactFileFormat readArtifactFileFormat(final String type, final String artifactLocation) {
        if (BASELINE_ARTIFACT_TAG.equals(type)) {
            return ArtifactFileFormat.AMOS_SCRIPT;
        }
        final byte[] artifactContents = resourceService.getBytes(artifactLocation);
        return readArtifactFileFormat("", artifactContents);
    }

    /**
     * Reads the artifact file format.
     *
     * @param type artifact type
     * @param artifactContents artifact file contents
     * @return the file format
     */
    public ArtifactFileFormat readArtifactFileFormat(final String type, final byte[] artifactContents) {
        if (BASELINE_ARTIFACT_TAG.equals(type)) {
            return ArtifactFileFormat.AMOS_SCRIPT;
        }

        final String artifactContentsInString = new String(artifactContents).replaceAll(REPLACE_REGEX, "").trim();
        if (artifactContentsInString.contains(BULK_3GPP_KEY_CHARACTOR)) {
            return ArtifactFileFormat.BULK_3GPP;
        }
        if (artifactContentsInString.contains(NETCONF_KEY_CHARACTOR)) {
            return ArtifactFileFormat.NETCONF;
        }
        return ArtifactFileFormat.UNKNOWN;
    }

    /**
     * Writes the aritfact contents to the filesystem.
     * <p>
     * A Transient error due to 'Stale File Handle' is possible in certain use case scenarios because AP is a clustered application using a shared
     * file system (SFS). If a write fails due to this issue then it will be retried again up to a maximum of {@link #MAX_RETRIES} times.
     *
     * @param artifactFilePath
     * @param artifactContents
     */
    public void writeArtifact(final String artifactFilePath, final byte[] artifactContents) {
        final RetriableCommand<Void> retriableCommand = (final RetryContext retryContext) -> {
            resourceService.write(artifactFilePath, artifactContents, false);
            return null;
        };
        executeRetriableCommand(retriableCommand);
    }

    /**
     * Writes the artifacts to the filesystem.
     * <p>
     * Retries a maximum of {@link #MAX_RETRIES} times in case of write failure. This caters for cases such as 'Stale File Handle' when writing to
     * SFS.
     *
     * @param fileWithContents
     *            Map where key is the artifact location and value is the artifact contents
     */
    public void writeArtifacts(final Map<String, byte[]> fileWithContents) {
        final RetriableCommand<Void> retriableCommand = (final RetryContext retryContext) -> {
            resourceService.writeFiles(fileWithContents, false);
            return null;
        };
        executeRetriableCommand(retriableCommand);
    }

    private static void executeRetriableCommand(final RetriableCommand<?> retriableCommand) {
        final RetryManager retryManager = new RetryManagerBean();
        final RetryPolicy policy = RetryPolicy.builder()
            .attempts(MAX_RETRIES)
            .waitInterval(RETRY_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
            .retryOn(EJBTransactionRolledbackException.class)
            .build();

        try {
            retryManager.executeCommand(policy, retriableCommand);
        } catch (final RetriableCommandException e) {
            throw new ApServiceException(e.getMessage(), e);
        }
    }

    /**
     * Deletes the specified directory. Does nothing if the directory does not exist.
     *
     * @param dirPath
     *            the directory to delete
     */
    public void deleteDirectory(final String dirPath) {
        try {
            resourceService.deleteDirectory(dirPath);
        } catch (final Exception e) {
            // Deletion of folder is best effort. So catch any Exception, log it, and continue.
            logger.warn("Failed to delete folder {}", dirPath, e);
        }
    }

    /**
     * Deletes the specified files. If it is the last file in the directory then the directory is also deleted.
     *
     * @param filePath
     */
    public void deleteFile(final String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }

        logger.trace("Deleting file {}", filePath);

        resourceService.delete(filePath);
        deleteDirectoryIfEmpty(filePath);
    }

    private void deleteDirectoryIfEmpty(final String filePath) {
        final String parentDir = new File(filePath).getParent();

        try {
            if (resourceService.isDirectoryExists(parentDir)) {
                resourceService.deleteDirectoryIfEmpty(parentDir);
            }
        } catch (final Exception e) {
            logger.error("Could not delete the directory {}", filePath, e);
        }
    }

    /**
     * @return true if single file exists in the directory.
     */
    public boolean isSingleFileInDirectory(final String artifactLocation) {
        if (artifactLocation == null) {
            return false;
        }
        final String parentDir = new File(artifactLocation).getParent();
        return resourceService.isDirectoryExists(parentDir) && (resourceService.listFiles(parentDir).size() == 1);
    }

    /**
     * @return true if directory exists, otherwise false
     */
    public boolean directoryExists(final String dirPath) {
        return resourceService.isDirectoryExists(dirPath);

    }

    /**
     * @return true if directory exists and containing files or directories, otherwise false
     */
    public boolean directoryExistAndNotEmpty(final String dirPath) {
        return resourceService.isDirectoryExists(dirPath) && isNonEmptyDirectory(dirPath);

    }

    private boolean isNonEmptyDirectory(final String dirPath) {
        return !resourceService.listDirectories(dirPath).isEmpty() || !resourceService.listFiles(dirPath).isEmpty();
    }
}
