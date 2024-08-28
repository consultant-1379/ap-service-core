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
package com.ericsson.oss.services.ap.core.usecase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.PartialProjectDeletionException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.core.usecase.delete.DeleteNodeWorkflowHelper;
import com.ericsson.oss.services.ap.core.usecase.delete.DeleteSkipStateOptions;
import com.ericsson.oss.services.ap.core.usecase.delete.HealthCheckReportDeleter;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowOperations;
import com.ericsson.oss.services.ap.ejb.api.CoreExecutorLocal;

/**
 * Usecase to delete a single AP project's MOs and artifacts.
 */
@UseCase(name = UseCaseName.DELETE_PROJECT)
public class DeleteProjectUseCase extends DeleteSkipStateOptions {

    @Inject
    private CoreExecutorLocal coreExecutorLocal;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private GeneratedArtifactHandler generatedArtifactHandler;

    @Inject
    private Logger logger;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    @Inject
    private SystemRecorder recorder;

    @Inject
    private WorkflowOperations workflowOperations;

    @Inject
    private DeleteNodeWorkflowHelper workflowHelper;

    @Inject
    private HealthCheckReportDeleter deleteHealthCheckReport;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    private static final String BASE_PROJECT_DIRECTORY_LOCATION = DirectoryConfiguration.getProfileDirectory();
    private static final String PROFILE_BASE_PATH = BASE_PROJECT_DIRECTORY_LOCATION + "/%s/profiles";

    /**
     * Deletes all nodes in the project along with all associated raw and generated files. If ignoreNetworkElement is true, then it will not delete
     * the NetworkElement for any node, otherwise, it will.
     *
     * @param projectFdn
     *            the FDN of the project in AP model
     * @param ignoreNetworkElement
     *            ignore the deletion of NetworkElement
     * @return the number of nodes in project
     * @throws ApApplicationException
     *             if there is an error deleting all nodes in the project
     * @throws PartialProjectDeletionException
     *             if there is an error deleting some of the nodes in the project
     */
    public int execute(final String projectFdn, final boolean ignoreNetworkElement) {
        final ManagedObject managedObject = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(projectFdn);
        if (managedObject == null) {
            throw new ProjectNotFoundException(String.format("Project with FDN [%s] could not be found.", projectFdn));
        }

        return executeDeleteForProject(projectFdn, ignoreNetworkElement);
    }

    private int executeDeleteForProject(final String projectFdn, final boolean ignoreNetworkElement) {
        final DeleteProjectProgress deleteProjectProgress = new DeleteProjectProgress();

        executeDeleteWorkflows(projectFdn, deleteProjectProgress, ignoreNetworkElement);
        final int numberOfNodesInProject = deleteProjectProgress.successfulNodeNames.size();

        deleteHealthCheckReports(projectFdn);
        deleteProjectArtifacts(projectFdn, numberOfNodesInProject);

        if (deleteProjectProgress.isFailed()) {
            throw new ApApplicationException("Delete failed for all nodes");
        } else if (deleteProjectProgress.isPartial()) {
            deleteNodeMos(projectFdn, deleteProjectProgress);
            throw new PartialProjectDeletionException(deleteProjectProgress.successfulNodeNames, deleteProjectProgress.failedNodeNames);
        }

        deleteProjectMo(projectFdn, numberOfNodesInProject);
        return deleteProjectProgress.successfulNodeNames.size();
    }

    private void deleteHealthCheckReports(final String projectFdn) {
        final Iterator<ManagedObject> nodeMos = dpsQueries
            .findChildMosOfTypes(projectFdn, Namespace.AP.toString(), MoType.NODE.toString()).execute();

        while (nodeMos.hasNext()) {
            final String nodeFdn = nodeMos.next().getFdn();
            deleteHealthCheckReport.deleteHealthCheckReports(nodeFdn);
        }
    }

    private void deleteProjectArtifacts(final String projectFdn, final int numberOfNodesInProject) {
        ddpTimer.start(CommandLogName.DELETE_RAW_AND_GENERATED_PROJECT_ARTIFACTS.toString());
        deleteRawArtifactsForProject(projectFdn);
        deleteGeneratedArtifactsForProject(projectFdn);
        deleteProfileArtifactsForProject(projectFdn);
        ddpTimer.end(projectFdn, numberOfNodesInProject);
    }

    private void deleteRawArtifactsForProject(final String projectFdn) {
        try {
            rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(projectFdn);
        } catch (final Exception e) {
            // delete of raw folder is best effort. So catch any Exception, log it, and continue.
            logger.warn("Failed to delete raw folder for {}", projectFdn, e);
        }
    }

    private void deleteGeneratedArtifactsForProject(final String projectFdn) {
        try {
            generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(projectFdn);
        } catch (final Exception e) {
            // delete of generated folders is best effort. So catch any Exception, log it, and continue.
            logger.warn("Failed to delete generated folder for {}", projectFdn, e);
        }
    }

    private void deleteProfileArtifactsForProject(final String projectFdn) {
        try {
            final String projectName = FDN.get(projectFdn).getRdnValue();
            final String profileFilesPath = String.format(PROFILE_BASE_PATH, projectName);

            if (artifactResourceOperations.directoryExistAndNotEmpty(profileFilesPath)) {
                artifactResourceOperations.deleteDirectory(profileFilesPath);
            }
        } catch (final Exception e) {
            logger.warn("Failed to delete profiles folder for {}", projectFdn, e);
        }
    }

    private DeleteProjectProgress executeDeleteWorkflows(final String projectFdn, final DeleteProjectProgress deleteProjectProgress,
        final boolean ignoreNetworkElement) {
        final Iterator<ManagedObject> nodeStatusMos = dpsQueries
            .findChildMosOfTypes(projectFdn, Namespace.AP.toString(), MoType.NODE_STATUS.toString()).execute();

        final Map<String, Future<Boolean>> nodeFutures = new HashMap<>();

        while (nodeStatusMos.hasNext()) {
            executeDeleteWorkflowForEachNode(deleteProjectProgress, ignoreNetworkElement, nodeStatusMos, nodeFutures);
        }

        updateDeleteProgressForEachNodeFuture(projectFdn, deleteProjectProgress, nodeFutures);
        return deleteProjectProgress;
    }

    private void executeDeleteWorkflowForEachNode(final DeleteProjectProgress deleteProjectProgress, final boolean ignoreNetworkElement,
        final Iterator<ManagedObject> nodeStatusMos, final Map<String, Future<Boolean>> nodeFutures) {
        final ManagedObject nodeStatusMo = nodeStatusMos.next();

        final String nodeFdn = FDN.get(nodeStatusMo.getFdn()).getParent();
        final String nodeName = FDN.get(nodeFdn).getRdnValue();
        final String nodeState = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());

        stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.DELETE_STARTED);
        final boolean isCmNodeHeartbeatSupervisionActive = workflowHelper.getCmNodeHeartbeatSupervisionStatus(nodeFdn);
        final boolean shouldNetworkElementDeletionBeIgnored = isCmNodeHeartbeatSupervisionActive || ignoreNetworkElement
            || isHardwareReplaceNode(nodeFdn);

        if (isExecuteDeleteWorkflow(nodeState)) {
            final Future<Boolean> deleteWorkflowFuture = getDeleteWorkflowFuture(shouldNetworkElementDeletionBeIgnored, nodeFdn);
            nodeFutures.put(nodeName, deleteWorkflowFuture);
        } else {
            deleteProjectProgress.deleteWorkflowComplete(nodeName, true);
        }
    }

    private Future<Boolean> getDeleteWorkflowFuture(final boolean shouldNetworkElementDeletionBeIgnored, final String nodeFdn) {
        return coreExecutorLocal.execute(() -> {
            final String dhcpClientId = workflowHelper.getWorkflowVariable(nodeFdn, AbstractWorkflowVariables.DHCP_CLIENT_ID_TO_REMOVE_KEY);
            workflowOperations.cancelIntegrationWorkflowIfActive(nodeFdn);
            return workflowOperations.executeDeleteWorkflow(nodeFdn, shouldNetworkElementDeletionBeIgnored, dhcpClientId);
        });
    }

    private boolean isHardwareReplaceNode(final String nodeFdn) {
        final ManagedObject nodeMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final Boolean isHardwareReplaceNode = nodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString());
        return isHardwareReplaceNode == null ? Boolean.FALSE : isHardwareReplaceNode;
    }

    private void updateDeleteProgressForEachNodeFuture(final String projectFdn, final DeleteProjectProgress deleteProjectProgress,
        final Map<String, Future<Boolean>> nodeFutures) {
        for (final Entry<String, Future<Boolean>> entry : nodeFutures.entrySet()) {
            final String nodeName = entry.getKey();
            try {
                final boolean deleteWorkflowSuccess = entry.getValue().get();
                deleteProjectProgress.deleteWorkflowComplete(nodeName, deleteWorkflowSuccess);
            } catch (final Exception e) {
                logger.error("Error executing delete workflow for node {}", nodeName, e);
                deleteProjectProgress.deleteWorkflowComplete(nodeName, false);
                final String nodeFdn = String.format("%s,Node=%s", projectFdn, nodeName);
                recorder.recordError(CommandLogName.DELETE_PROJECT.toString(), ErrorSeverity.ERROR, nodeName, nodeFdn,
                    ExceptionUtils.getRootCauseMessage(e));
                stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.DELETE_FAILED);
            }
        }
    }

    private static boolean isExecuteDeleteWorkflow(final String nodeState) {
        return !SKIP_DELETE_WORKFLOW_STATES.contains(nodeState);
    }

    private void deleteNodeMos(final String projectFdn, final DeleteProjectProgress deleteProjectProgress) {
        for (final String nodeName : deleteProjectProgress.successfulNodeNames) {
            final String nodeFdn = String.format("%s,Node=%s", projectFdn, nodeName);
            try {
                dpsOperations.deleteMo(nodeFdn);
            } catch (final Exception e) {
                deleteProjectProgress.deleteNodeMoFailed(nodeName);
                logger.error("Error deleting node {}", nodeFdn, e);
                stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.DELETE_FAILED);
            }
        }
    }

    private void deleteProjectMo(final String projectFdn, final int numberOfNodesInProject) {
        ddpTimer.start(CommandLogName.DELETE_PROJECT_MO.toString());
        try {
            dpsOperations.deleteMo(projectFdn);
            ddpTimer.end(projectFdn, numberOfNodesInProject);
        } catch (final Exception e) {
            ddpTimer.endWithError(projectFdn, numberOfNodesInProject);
            logger.error("Error deleting project MO {}", projectFdn, e);
            throw new ApApplicationException(e);
        }
    }

    private static class DeleteProjectProgress {
        private final List<String> successfulNodeNames = new ArrayList<>();
        private final List<String> failedNodeNames = new ArrayList<>();

        public boolean isFailed() {
            return successfulNodeNames.isEmpty() && !failedNodeNames.isEmpty();
        }

        public boolean isPartial() {
            return !failedNodeNames.isEmpty() && !successfulNodeNames.isEmpty();
        }

        public void deleteWorkflowComplete(final String nodeName, final boolean success) {
            if (success) {
                successfulNodeNames.add(nodeName);
            } else {
                failedNodeNames.add(nodeName);
            }
        }

        public void deleteNodeMoFailed(final String nodeName) {
            successfulNodeNames.remove(nodeName);
            failedNodeNames.add(nodeName);
        }
    }
}
