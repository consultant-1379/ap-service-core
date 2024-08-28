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

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
/**
 * Deletes json files in the Generated directory once a day as per the Timer Service schedule.
 * Only json files that are older than 24 hours are deleted.
 */
@EService
@Startup
@Singleton
public class EoiFilesCleanUpEjb {

    @Inject
    private TimerService timerService;

    @Inject
    private Logger logger;

    @EServiceRef
    private APServiceClusterMember apServiceClusterMember;

    @EServiceRef
    private ResourceService resourceService;

    private List<String> folders = Arrays.asList("generated", "raw", "download", "temp", "preconfig", "schemadata");

    @PostConstruct
    public void init() {
        timerService.createCalendarTimer(scheduleTimer());
    }

    /**
     * Start method is executed on the Timer Service schedule.
     * Json files in the mentioned path are checked to see if they're eligible for deletion.
     * The delete usecase is kicked off if the conditions are met.
     */
    @Timeout
    public void start() {
        try{
            if (!apServiceClusterMember.isMasterNode()) {
                logger.debug("Ignoring cleanup nodes call, not master node");
                return;
            }
            final String artifactsDirectoryPath = DirectoryConfiguration.getArtifactsDirectory();
            if (resourceService.exists(artifactsDirectoryPath)) {
                deleteFilesFromEoiFileDirectory(artifactsDirectoryPath);
            }
        } catch (final Exception e) {
            logger.error("Error cleaning Day0 Projects directory : {}", e.getMessage());
        }
    }

    public ScheduleExpression scheduleTimer(){
        final ScheduleExpression scheduleExpression = new ScheduleExpression();
        scheduleExpression.hour("3");
        scheduleExpression.minute("45");
        scheduleExpression.second("0");
        return scheduleExpression;
    }

    private void deleteFilesFromEoiFileDirectory(final String artifactsDirectoryPath) {
        logger.info("Cleaning up json files in Day0 Projects directory {}", artifactsDirectoryPath);
        final Collection<String> projectDirectories = resourceService.listDirectories(artifactsDirectoryPath);
        if(projectDirectories != null && !projectDirectories.isEmpty()){
            for(final String projectDirectory : projectDirectories) {
                if(!folders.contains(projectDirectory)){
                    getNodeDirectories(projectDirectory);
                }
            }
        }
    }

    private void getNodeDirectories(final String projectDirectory){
        logger.info("Retreiving node directories in the project directory : {}", projectDirectory);
        final Collection<String> nodeDirectories = resourceService.listDirectories(DirectoryConfiguration.getProjectDirectory(projectDirectory));
        if(nodeDirectories != null && !nodeDirectories.isEmpty()){
            for(final String nodeDirectory : nodeDirectories) {
                deleteJsonFiles(DirectoryConfiguration.getNodeDirectory(projectDirectory, nodeDirectory));
            }
        }
    }

    private void deleteJsonFiles(final String nodeDirPath){
        final Collection<Resource> files = resourceService.listFiles(nodeDirPath);
        if(files != null && !files.isEmpty()){
            for (final Resource generatedFile : files) {
                if(generatedFile.getName().contains("_day0.json")){
                    deleteFileOlderThanTwentyFourHours(generatedFile, nodeDirPath);
                }
            }
        }
    }

    private void deleteFileOlderThanTwentyFourHours(final Resource file, final String nodeDirPath) {
        if (isFileOlderThanTwentyFourHours(file)) {
            try {
                resourceService.delete(getResourceUri(file.getName(), nodeDirPath));
                logger.info("Successfully deleted file {} from Day0 Projects directory {}", file.getName(), nodeDirPath);
            } catch (final Exception e) {
                logger.error("Error deleting file {} from Day0 Projects directory: {}", file.getName(), e.getMessage());
            }
        }
    }

    private static boolean isFileOlderThanTwentyFourHours(final Resource file) {
        return isFileOlderThanDate(file, getOneDayAgo());
    }

    private static boolean isFileOlderThanDate(final Resource file, final Date date) {
        return file.getLastModificationTimestamp() < date.getTime();
    }

    private static Date getOneDayAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -24);
        return cal.getTime();
    }

    private String getResourceUri(final String resourceName, final String nodeDirPath) {
        return Paths.get(nodeDirPath, resourceName).toString();
    }
}
