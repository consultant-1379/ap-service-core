/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.delete;

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Utility class used to delete AP model related data
 */
public class ApModelDeleter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private HealthCheckReportDeleter deleteHealthCheckReport;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private GeneratedArtifactHandler generatedArtifactHandler;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    /**
     * Deletes the AP node and its artifact data. If the node is the last one in the project and has no associated profile with the project, delete the project as well.
     *
     * @param nodeFdn
     *              the node fdn
     */
    public void deleteApNodeData(final String nodeFdn) {
        final String projectFdn = FDN.get(nodeFdn).getParent();
        final int numberOfNodesInProject = getNumberOfNodesInProject(projectFdn);
        deleteHealthCheckReport.deleteHealthCheckReports(nodeFdn);

        // Must delete artifacts after workflow, or else cancelling security will regenerate SMRS directory
        if (!isLastNodeInProject(numberOfNodesInProject) || hasProfilesAssociatedWithProject(projectFdn)) {
            deleteRawAndGeneratedNodeArtifacts(nodeFdn);
            deleteNodeMo(nodeFdn);
        } else {
            logger.info("{} is last node and no profile is associated with project so project will also be deleted", nodeFdn);
            deleteRawAndGeneratedProjectArtifacts(projectFdn);
            deleteProjectMo(projectFdn);
        }
    }

    private int getNumberOfNodesInProject(final String projectFdn) {
        return dpsQueries.findChildMosOfTypes(projectFdn, Namespace.AP.toString(), MoType.NODE.toString()).executeCount().intValue();
    }

    private static boolean isLastNodeInProject(final int numberOfNodesInProject) {
        return numberOfNodesInProject == 1;
    }

    private boolean hasProfilesAssociatedWithProject(final String projectFdn) {
        return dpsQueries.findChildMosOfTypes(projectFdn, Namespace.AP.toString(), MoType.CONFIGURATION_PROFILE.toString()).executeCount().intValue() != 0;
    }

    private void deleteRawAndGeneratedNodeArtifacts(final String nodeFdn) {
        logger.debug("Deleting raw and generated file for node {}", nodeFdn);
        ddpTimer.start(CommandLogName.DELETE_RAW_AND_GENERATED_NODE_ARTIFACTS.toString());
        try {
            rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(nodeFdn);
        } catch (final Exception e) {
            ddpTimer.endWithError(nodeFdn);
            logger.warn("Error deleting raw artifacts", e);
        }

        try {
            generatedArtifactHandler.deleteAllForNodeWithNoModelUpdate(nodeFdn);
        } catch (final Exception e) {
            ddpTimer.endWithError(nodeFdn);
            logger.warn("Error deleting generated artifacts", e);
        }
        ddpTimer.end(nodeFdn);
    }

    private void deleteRawAndGeneratedProjectArtifacts(final String projectFdn) {
        ddpTimer.start(CommandLogName.DELETE_RAW_AND_GENERATED_PROJECT_ARTIFACTS.toString());
        try {
            rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(projectFdn);
        } catch (final Exception e) {
            ddpTimer.endWithError(projectFdn, getNumberOfNodesInProject(projectFdn));
            logger.warn("Failed to delete raw folder for {}", projectFdn, e);
        }
        try {
            generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(projectFdn);
        } catch (final Exception e) {
            ddpTimer.endWithError(projectFdn, getNumberOfNodesInProject(projectFdn));
            logger.warn("Failed to delete generated folder for {}", projectFdn, e);
        }
        ddpTimer.end(projectFdn, getNumberOfNodesInProject(projectFdn));
    }

    private void deleteNodeMo(final String nodeFdn) {
        logger.debug("Deleting node, nodeFdn={}", nodeFdn);
        ddpTimer.start(CommandLogName.DELETE_NODE_MO.toString());
        try {
            dpsOperations.deleteMo(nodeFdn);
            ddpTimer.end(nodeFdn);
        } catch (final Exception exception) {
            ddpTimer.endWithError(nodeFdn);
            throw new ApApplicationException(String.format("Error deleting node %s", nodeFdn), exception);
        }
    }

    private void deleteProjectMo(final String projectFdn) {
        ddpTimer.start(CommandLogName.DELETE_PROJECT_MO.toString());
        try {
            dpsOperations.deleteMo(projectFdn);
            ddpTimer.end(projectFdn, getNumberOfNodesInProject(projectFdn));
        } catch (final Exception e) {
            ddpTimer.endWithError(projectFdn, getNumberOfNodesInProject(projectFdn));
            throw new ApApplicationException(String.format("Error deleting project %s", projectFdn), e);
        }
    }
}
