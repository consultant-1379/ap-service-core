/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;

/**
 * Clean up artifacts in APs generated/raw directories once a week.
 * Only deletes artifacts if the respective {@link ManagedObject} of their project/node no longer exists.
 */
@Singleton
public class ArtifactCleaner {

    private static final String NODE_FDN_PATTERN = "Project=%1$s,Node=%2$s";

    private static final String PROJECT_FDN_PATTERN = "Project=%s";

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private GeneratedArtifactHandler generatedArtifactHandler;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    @Inject
    private Logger logger;

    @EServiceRef
    private ResourceService resourceService;

    @EServiceRef
    private APServiceClusterMember apServiceClusterMember;

    @Schedule(hour = "1", dayOfWeek = "*")
    public void deleteArtifacts() {
        try {
            if (!apServiceClusterMember.isMasterNode()) {
                return;
            }

            final String rawDirectory = DirectoryConfiguration.getRawDirectory();
            if (resourceService.exists(rawDirectory)) {
                deleteArtifactsIfMoDoesNotExist(rawDirectory);
            }

            final String bindDirectory = DirectoryConfiguration.getBindDirectory();
            if (resourceService.exists(bindDirectory)) {
                deleteBindArtifactsIfMoDoesNotExist(bindDirectory);
            }
        } catch (final Exception e) {
            logger.warn("Could not clean up artifacts from generated and raw directories", e);
        }
    }

    private void deleteArtifactsIfMoDoesNotExist(final String rawDirectory) {
        final Collection<String> projects = resourceService.listDirectories(rawDirectory);

        for (final String projectName : projects) {
            final String projectDirectory = getPath(rawDirectory, projectName);
            final Collection<String> nodes = resourceService.listDirectories(projectDirectory);

            for (final String nodeName : nodes) {
                if (!doesMoExist(nodeName, MoType.NODE)) {
                    deleteNodeArtifacts(nodeName, projectName);
                }
            }
            if (!doesMoExist(projectName, MoType.PROJECT)) {
                deleteProjectArtifacts(projectName);
            }
        }
    }

    private void deleteBindArtifactsIfMoDoesNotExist(final String bindDirectory) {
        final Collection<Resource> bindArtifacts = resourceService.listFiles(bindDirectory);

        for (final Resource bindArtifact : bindArtifacts) {
            final String bindArtifactPath = getPath(bindDirectory, bindArtifact.getName());
            if (!doesMoOfArtifactExist(bindArtifactPath)) {
                artifactResourceOperations.deleteFile(bindArtifactPath);
            }
        }
    }

    private void deleteNodeArtifacts(final String nodeName, final String projectName) {
        try {
            final String nodeFdn = String.format(NODE_FDN_PATTERN, projectName, nodeName);
            final String generatedNodeDirectory = DirectoryConfiguration.getGeneratedDirectory() + File.separator + projectName + File.separator + nodeName;
            artifactResourceOperations.deleteDirectory(generatedNodeDirectory);
            rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(nodeFdn);
            logger.debug("Successfully deleted artifacts of {} from generated and raw directories", nodeName);
        } catch (final Exception e) {
            logger.warn("Error deleting artifacts of {} from generated and raw directories", nodeName, e);
        }
    }

    private void deleteProjectArtifacts(final String projectName) {
        try {
            final String projectFdn = String.format(PROJECT_FDN_PATTERN, projectName);
            generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(projectFdn);
            rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(projectFdn);
            logger.debug("Successfully deleted artifacts of {} from generated and raw directories", projectName);
        } catch (final Exception e) {
            logger.warn("Error deleting artifacts of {} from generated and raw directories", projectName, e);
        }
    }

    private boolean doesMoExist(final String moName, final MoType moType) {
        final Iterator<ManagedObject> moIterator = dpsQueries.findMoByName(moName, moType.toString(), AP.toString()).execute();
        return moIterator.hasNext();
    }

    private boolean doesMoOfArtifactExist(final String artifactPath) {
        final Iterator<ManagedObject> moIterator = dpsQueries.findMosWithAttributeValue(NodeArtifactAttribute.GEN_LOCATION.toString(),
                artifactPath, AP.toString(), MoType.NODE_ARTIFACT.toString()).execute();
        return moIterator.hasNext();
    }

    private String getPath(final String directory, final String subDirectory) {
        return Paths.get(directory, subDirectory).toString();
    }
}
