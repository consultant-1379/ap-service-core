/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Deletes files in the AP download directory once a day. Only deletes files that are older than 1 hour.
 */
@Singleton
public class DownloadFolderCleaner {

    @Inject
    private Logger logger;

    @EServiceRef
    private ResourceService resourceService;

    @EServiceRef
    private APServiceClusterMember apServiceClusterMember;

    @Schedule(hour = "1", dayOfWeek = "*")
    public void deleteExpiredFiles() {
        try {
            if (!apServiceClusterMember.isMasterNode()) {
                return;
            }

            final String downloadDirectory = DirectoryConfiguration.getDownloadDirectory();
            if (resourceService.exists(downloadDirectory)) {
                deleteFilesFromDownloadDirectory(downloadDirectory);
            }
        } catch (final Exception e) {
            logger.warn("Error cleaning AP download directory", e);
        }
    }

    private void deleteFilesFromDownloadDirectory(final String downloadDirectory) {
        logger.info("Cleaning up files in AP download directory {}", downloadDirectory);
        final Collection<Resource> files = resourceService.listFiles(downloadDirectory);

        for (final Resource downloadedFile : files) {
            deleteFileOlderThanOneHour(downloadedFile, downloadDirectory);
        }
    }

    private void deleteFileOlderThanOneHour(final Resource file, final String downloadDirectory) {
        if (isFileOlderThanOneHour(file)) {
            try {
                resourceService.delete(getResourceUri(file.getName(), downloadDirectory));
                logger.debug("Successfully deleted file {} from download directory", file.getName());
            } catch (final Exception e) {
                logger.warn("Error deleting file {} from download directory: {}", file.getName(), e.getMessage(), e);
            }
        }
    }

    private static boolean isFileOlderThanOneHour(final Resource file) {
        return isFileOlderThanDate(file, getOneHourAgo());
    }

    private static boolean isFileOlderThanDate(final Resource file, final Date date) {
        return file.getLastModificationTimestamp() < date.getTime();
    }

    private static Date getOneHourAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -1);
        return cal.getTime();
    }

    private static String getResourceUri(final String resourceName, final String downloadDirectory) {
        return Paths.get(downloadDirectory, resourceName).toString();
    }
}