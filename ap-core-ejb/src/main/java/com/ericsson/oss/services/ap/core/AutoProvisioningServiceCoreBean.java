/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.EOI_NETWORK_ELEMENTS;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.PROJECT_NAME;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceQualifier;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.annotation.Authorize;
import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.UseCaseRecorder.Scope;
import com.ericsson.oss.services.ap.core.rest.builder.NodeStatusDataBuilder;
import com.ericsson.oss.services.ap.core.rest.model.NodeStatusData;
import com.ericsson.oss.services.ap.core.usecase.BatchBindUseCase;
import com.ericsson.oss.services.ap.core.usecase.BindUseCase;
import com.ericsson.oss.services.ap.core.usecase.CancelUseCase;
import com.ericsson.oss.services.ap.core.usecase.CreateProfileUseCase;
import com.ericsson.oss.services.ap.core.usecase.CreateProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.DeleteNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.DeleteProfileUseCase;
import com.ericsson.oss.services.ap.core.usecase.DeleteProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.DownloadArtifactUseCase;
import com.ericsson.oss.services.ap.core.usecase.DownloadSchemaUseCase;
import com.ericsson.oss.services.ap.core.usecase.DumpSnapshotUseCase;
import com.ericsson.oss.services.ap.core.usecase.ExportCIQUsecase;
import com.ericsson.oss.services.ap.core.usecase.GetSnapshotUseCase;
import com.ericsson.oss.services.ap.core.usecase.ImportUseCase;
import com.ericsson.oss.services.ap.core.usecase.ModifyProfileUseCase;
import com.ericsson.oss.services.ap.core.usecase.OrderNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.ResumeUseCase;
import com.ericsson.oss.services.ap.core.usecase.SkipUseCase;
import com.ericsson.oss.services.ap.core.usecase.StatusAllProjectsUseCase;
import com.ericsson.oss.services.ap.core.usecase.StatusDeploymentUseCase;
import com.ericsson.oss.services.ap.core.usecase.StatusNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.StatusProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.UploadArtifactUseCase;
import com.ericsson.oss.services.ap.core.usecase.UseCaseFactory;
import com.ericsson.oss.services.ap.core.usecase.ViewAllProjectsUseCase;
import com.ericsson.oss.services.ap.core.usecase.ViewNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.ViewProfilesUseCase;
import com.ericsson.oss.services.ap.core.usecase.ViewProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.importproject.EoiProjectValidator;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectImporter;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectInfo;

/**
 * Provides implementation for the methods on the {@link AutoProvisioningService} interface.
 */
@Stateless
@Local
@EServiceQualifier("apcore")
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@SuppressWarnings("PMD")
public class AutoProvisioningServiceCoreBean implements AutoProvisioningService { // NOPMD - Too many fields

    private static final String RBAC_RESOURCE_AP = "ap";
    private static final String RBAC_ACTION_READ = "read";
    private static final String RBAC_ACTION_EXECUTE = "execute";
    private static final String RBAC_ACTION_PATCH = "patch";
    private static final boolean VALIDATION_REQUIRED = true;
    private static final String VALIDATION_SUCCESS_MESSAGE = "Day0 flow triggered successfully";

    @Inject
    private NodeStatusDataBuilder nodeStatusDataBuilder;

    @EServiceRef
    private StatusEntryManagerLocal statusEntryManager;

    @Inject
    private AsyncUseCaseExecutorBean asyncUseCaseExecutorBean;

    @Inject
    private ContextService contextService;

    @Inject
    private UseCaseFactory useCaseFactory;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private ProjectImporter projectImporter;

    @Inject
    private EoiProjectValidator eoiProjectValidator;

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_PATCH)
    public BatchBindResult batchBind(final String csvFilename, final byte[] csvFileContents) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.BIND, Scope.MULTIPLE_NODES, csvFilename);
        return UseCaseExecutor.execute(recorder, () -> {
            final BatchBindUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.BATCH_BIND);
            final BatchBindResult bindResult = usecase.execute(csvFilename, csvFileContents);

            if (bindResult.isSuccessful()) {
                final String batchBindCommandLogInfoFormat = String.format("%s%n%nSuccessful binds -> %s", UseCaseRecorder.PROJECTS_LOG,
                    bindResult.getSuccessfulBindDetails().toString());
                final String info = String.format(batchBindCommandLogInfoFormat, recorder.getExecutionTime(), bindResult.getTotalBinds());
                recorder.success(info);
            } else {
                final String info = String.format("Bind failed for %s/%s nodes%n%n%s%n%nSuccessful binds -> %s", bindResult.getFailedBinds(),
                    bindResult.getTotalBinds(), bindResult.getFailedBindMessages().toString(),
                    bindResult.getSuccessfulBindDetails().toString());
                recorder.error(info);
            }

            return bindResult;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void cancel(final String nodeFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.CANCEL, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final CancelUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.CANCEL);
            usecase.execute(nodeFdn);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void deleteNode(final String nodeFdn, final boolean ignoreNetworkElement) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DELETE_NODE, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final DeleteNodeUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.DELETE_NODE);
            usecase.execute(nodeFdn, ignoreNetworkElement);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void deleteProject(final String projectFdn, final boolean ignoreNetworkElement) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DELETE_PROJECT, Scope.MULTIPLE_NODES, FDN.get(projectFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final DeleteProjectUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.DELETE_PROJECT);
            final int noOfNodesInProject = usecase.execute(projectFdn, ignoreNetworkElement);
            recorder.success(noOfNodesInProject);
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public String downloadNodeArtifact(final String nodeFdn, final ArtifactBaseType artifactBaseType) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DOWNLOAD_ARTIFACT, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        return UseCaseExecutor.execute(recorder, () -> {
            final DownloadArtifactUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.DOWNLOAD_ARTIFACT);
            final String uniqueId = usecase.execute(nodeFdn, artifactBaseType);
            recorder.success(String.format("Downloaded %s artifact(s)", artifactBaseType));
            return uniqueId;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public String downloadSchemaAndSamples(final String nodeType) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DOWNLOAD_SCHEMA, Scope.SINGLE_NODE);
        return UseCaseExecutor.execute(recorder, () -> {
            final DownloadSchemaUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.DOWNLOAD_SCHEMA_SAMPLE);
            final String requestedFileId = usecase.execute(nodeType);

            recorder.setResource(requestedFileId);
            recorder.success(String.format("Downloaded sample and schema files for node type %s", nodeType));
            return requestedFileId;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void orderNode(final String nodeFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.ORDER_NODE, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final OrderNodeUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.ORDER_NODE);
            usecase.execute(nodeFdn);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public String orderProject(final String fileName, final byte[] projectContents, final boolean validationRequired) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.ORDER_PROJECT, Scope.MULTIPLE_NODES, fileName);
        return UseCaseExecutor.execute(recorder, () -> {
            final ImportUseCase importUsecase = useCaseFactory.getNamedUsecase(UseCaseName.IMPORT);
            final ProjectInfo projectInfo = importUsecase.execute(fileName, projectContents, validationRequired);
            final String projectFdn = MoType.PROJECT.toString() + "=" + projectInfo.getName();

            final String userId = contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY);
            asyncUseCaseExecutorBean.orderProject(projectFdn, userId, validationRequired, projectInfo);

            recorder.setResource(projectFdn);
            recorder.success(projectInfo.getNodeQuantity());

            return projectFdn;
        });
    }

    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public String eoiOrderProject(final Map<String, Object> eoiProjectRequest) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.EOI_ORDER_PROJECT, Scope.MULTIPLE_NODES);
        return UseCaseExecutor.execute(recorder, () -> {
            eoiProjectValidator.validateStandardProject(eoiProjectRequest);
            final String projectFdn = MoType.PROJECT.toString() + "=" + eoiProjectRequest.get(PROJECT_NAME.toString());
            projectImporter.createEoiNodes((List<Map<String, Object>>) eoiProjectRequest.get(EOI_NETWORK_ELEMENTS.toString()), projectFdn);
            final String userId = contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY);
            asyncUseCaseExecutorBean.eoiOrderProject(projectFdn, userId, (String) eoiProjectRequest.get("baseUrl"), (String) eoiProjectRequest.get("sessionId"));
            return VALIDATION_SUCCESS_MESSAGE;
        });
    }


    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void orderProject(final String projectFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.RESUME, Scope.SINGLE_NODE, FDN.get(projectFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final String projectName = FDN.get(projectFdn).getRdnValue();
            final String userId = contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY);
            asyncUseCaseExecutorBean.orderProject(projectFdn, userId, VALIDATION_REQUIRED);
            recorder.success(String.format("Order integration for %1$s is initiated. Execute the command 'ap status -p %1$s' for progress",
                projectName));
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void resume(final String nodeFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.RESUME, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final ResumeUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.RESUME);
            usecase.execute(nodeFdn);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public List<ApNodeGroupStatus> statusAllProjects() {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.STATUS, Scope.MULTIPLE_PROJECTS);
        return UseCaseExecutor.execute(recorder, () -> {
            final StatusAllProjectsUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.STATUS_ALL_PROJECTS);
            final List<ApNodeGroupStatus> statusOfAllProjects = usecase.execute();
            recorder.success(statusOfAllProjects.size());
            return statusOfAllProjects;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public NodeStatus statusNode(final String nodeFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.STATUS_NODE, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        return UseCaseExecutor.execute(recorder, () -> {
            final StatusNodeUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.STATUS_NODE);
            final NodeStatus nodeStatus = usecase.execute(nodeFdn);
            recorder.success();
            return nodeStatus;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public ApNodeGroupStatus statusDeployment(final String deployment) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.STATUS_DEPLOYMENT, Scope.MULTIPLE_NODES, deployment);
        return UseCaseExecutor.execute(recorder, () -> {
            final StatusDeploymentUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.STATUS_DEPLOYMENT);
            final ApNodeGroupStatus deploymentStatus = usecase.execute(deployment);
            recorder.success(deploymentStatus.getNumberOfNodes());
            return deploymentStatus;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public ApNodeGroupStatus statusProject(final String projectFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.STATUS_PROJECT, Scope.MULTIPLE_NODES, FDN.get(projectFdn));
        return UseCaseExecutor.execute(recorder, () -> {
            final StatusProjectUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.STATUS_PROJECT);
            final ApNodeGroupStatus projectStatus = usecase.execute(projectFdn);
            recorder.success(projectStatus.getNumberOfNodes());
            return projectStatus;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void uploadArtifact(final String nodeFdn, final String fileName, final byte[] fileByteData) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.UPLOAD_ARTIFACT, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
            final UploadArtifactUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.UPLOAD_ARTIFACT);
            usecase.execute(nodeFdn, fileName, fileByteData);
            final String message = String.format("Uploaded %s", fileName);
            statusEntryManager.taskCompleted(nodeFdn, StatusEntryNames.UPLOAD_CONFIGURATION.toString(), message);
            recorder.success(message);
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public List<MoData> viewAllProjects() {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.VIEW, Scope.MULTIPLE_PROJECTS);
        return UseCaseExecutor.execute(recorder, () -> {
            final ViewAllProjectsUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.VIEW_ALL_PROJECTS);
            final List<MoData> viewOfAllProjects = usecase.execute();
            recorder.success(viewOfAllProjects.size());
            return viewOfAllProjects;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public List<MoData> viewNode(final String nodeFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.VIEW_NODE, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        return UseCaseExecutor.execute(recorder, () -> {
            final ViewNodeUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.VIEW_NODE);
            final List<MoData> nodeView = usecase.execute(nodeFdn);
            recorder.success();
            return nodeView;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public List<MoData> viewProject(final String projectFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.VIEW_PROJECT, Scope.MULTIPLE_NODES, FDN.get(projectFdn));
        return UseCaseExecutor.execute(recorder, () -> {
            final ViewProjectUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.VIEW_PROJECT);
            final List<MoData> projectView = usecase.execute(projectFdn);
            final int numberOfNodes = projectView.size() - 1; // First entry in list is the Project MO's data
            recorder.success(numberOfNodes);
            return projectView;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public MoData createProject(final String name, final String creator, final String description) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.CREATE_PROJECT, Scope.SINGLE_PROJECT);
        return UseCaseExecutor.execute(recorder, () -> {
            final CreateProjectUseCase createProjectUseCase = useCaseFactory.getNamedUsecase(
                UseCaseName.CREATE_PROJECT);
            return createProjectUseCase.execute(name, creator, description);
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public MoData createProfile(final MoData profile) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.CREATE_PROFILE, Scope.SINGLE_PROJECT);
        return UseCaseExecutor.execute(recorder, () -> {
            final CreateProfileUseCase createProfileUseCase = useCaseFactory.getNamedUsecase(
                UseCaseName.CREATE_PROFILE);
            return createProfileUseCase.execute(profile);
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public List<MoData> viewProfiles(final String projectFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.VIEW_PROFILES, Scope.SINGLE_PROJECT);
        return UseCaseExecutor.execute(recorder, () -> {
            final ViewProfilesUseCase viewProfilesUseCase = useCaseFactory.getNamedUsecase(
                UseCaseName.VIEW_PROFILES);
            return viewProfilesUseCase.execute(projectFdn, null);
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public List<MoData> viewProfilesByProfileType(String projectFdn, String dataType) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.VIEW_PROFILES, Scope.SINGLE_PROJECT);
        return UseCaseExecutor.execute(recorder, () -> {
            final ViewProfilesUseCase viewProfilesUseCase = useCaseFactory.getNamedUsecase(
                UseCaseName.VIEW_PROFILES);
            return viewProfilesUseCase.execute(projectFdn, dataType);
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public MoData modifyProfile(final MoData profile, final String projectFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.MODIFY_PROFILE, Scope.SINGLE_PROJECT);
        return UseCaseExecutor.execute(recorder, () -> {
            final ModifyProfileUseCase modifyProfileUseCase = useCaseFactory.getNamedUsecase(UseCaseName.MODIFY_PROFILE);
            return modifyProfileUseCase.execute(profile);
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public String exportProfileCIQ(final String projectFdn, final String profileId) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.EXPORT_CIQ, Scope.SINGLE_PROJECT, profileId);
        return UseCaseExecutor.execute(recorder, () -> {
            final ExportCIQUsecase exportCIQUsecase = useCaseFactory.getNamedUsecase(UseCaseName.EXPORT_CIQ);
            return exportCIQUsecase.execute(projectFdn, profileId);
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void deleteProfile(final String projectId, final String profileId) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DELETE_PROFILE, Scope.SINGLE_PROJECT, profileId);
        UseCaseExecutor.execute(recorder, () -> {
            final DeleteProfileUseCase deleteProfileUsecase = useCaseFactory.getNamedUsecase(UseCaseName.DELETE_PROFILE);
            deleteProfileUsecase.execute(projectId, profileId);
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_PATCH)
    public void bind(final String nodeNameOrFdn, final String hardwareSerialNumber) {
        final boolean isNodeName = !nodeNameOrFdn.contains("=");
        final String nodeName = isNodeName ? nodeNameOrFdn : FDN.get(nodeNameOrFdn).getRdnValue();

        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.BIND, Scope.SINGLE_NODE, nodeName);
        UseCaseExecutor.execute(recorder, () -> {
            final String nodeFdn = !isNodeName ? nodeNameOrFdn : findFdnForNodeName(nodeName);
            final BindUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.BIND);
            usecase.execute(nodeFdn, hardwareSerialNumber);
            recorder.setResource(nodeFdn);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void skip(final String nodeFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.SKIP, Scope.SINGLE_NODE, FDN.get(nodeFdn));
        UseCaseExecutor.execute(recorder, () -> {
            final SkipUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.SKIP);
            usecase.execute(nodeFdn);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public void dumpSnapshot(final String projectId, final String profileId, final String nodeId, final String profileFdn) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DUMP_SNAPSHOT, Scope.SINGLE_PROJECT, profileFdn);
        UseCaseExecutor.execute(recorder, () -> {
            final DumpSnapshotUseCase dumpSnapshotUsecase = useCaseFactory.getNamedUsecase(UseCaseName.DUMP_SNAPSHOT);
            dumpSnapshotUsecase.execute(projectId, profileId, nodeId, profileFdn);
            recorder.success();
            return null;
        });
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_READ)
    public String getSnapshot(final String projectId, final String profileFdn, final String nodeId) {
        final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.GET_SNAPSHOT, Scope.SINGLE_PROJECT, profileFdn);
        return UseCaseExecutor.execute(recorder, () -> {
            final GetSnapshotUseCase getSnapshotUsecase = useCaseFactory.getNamedUsecase(UseCaseName.GET_SNAPSHOT);
            final String snapshotContent = getSnapshotUsecase.execute(projectId, profileFdn, nodeId);
            recorder.success();
            return snapshotContent;
        });
    }

    private String findFdnForNodeName(final String nodeName) {
        final Iterator<ManagedObject> nodeMos = dpsQueries.findMoByName(nodeName, MoType.NODE.toString(), AP.toString()).execute();
        if (!nodeMos.hasNext()) {
            throw new NodeNotFoundException(nodeName);
        }
        return nodeMos.next().getFdn();
    }

    private ManagedObject getNodeArtifactMoByName(final String artifactName) {
        final Iterator<ManagedObject> artifactMos = dpsQueries.findMosWithAttributeValue(NodeArtifactAttribute.NAME.toString(), artifactName, Namespace.AP.toString(), MoType.NODE_ARTIFACT.toString()).execute();
        return artifactMos.hasNext() ? artifactMos.next() : null;
    }

    @Override
    @Authorize(resource = RBAC_RESOURCE_AP, action = RBAC_ACTION_EXECUTE)
    public String downloadConfigurationFile(final String nodeFdn, final String nodeId) {
        final NodeStatus nodeStatus = statusNode(nodeFdn);
        final NodeStatusData nodeStatusData = nodeStatusDataBuilder.buildNodeStatusData(nodeStatus);
        if(nodeStatusData.getState().equals(State.EOI_INTEGRATION_COMPLETED.getDisplayName())) {
            final ManagedObject artifactMo = getNodeArtifactMoByName(nodeId+"_day0");
            if(artifactMo != null){
                final String importProgress = artifactMo.getAttribute(NodeArtifactAttribute.IMPORT_PROGRESS.toString());
                if(ArtifactImportProgress.COMPLETED.toString().equals(importProgress)) {
                    return artifactMo.getAttribute(NodeArtifactAttribute.GEN_LOCATION.toString());
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
