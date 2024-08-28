/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApNodeExistsException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.common.artifacts.UpgradePackageProductDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.ShmDetailsRetriever;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.ap.core.usecase.DeleteNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.DeleteProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.hardwarereplace.HardwareReplaceUtil;
import com.ericsson.oss.services.ap.core.usecase.migration.MigrationUtil;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData;

/**
 * Import validated project archive to create project MO, node MOs and artifact MOs.
 */
public class ProjectImporter {

    private static final String AUTOMATIC_LICENSE_REQUEST = "automaticLicenseRequest";
    private static final String IMPORT_FAILED_FOR_ZIP = "Import failed for ZIP file %s";
    private static final String GREENFIELD_NODE_INFO_XSD = "NodeInfo.xsd";
    private static final String EXPANSION_NODE_INFO_XSD = "ExpansionNodeInfo.xsd";
    private static final String HARDWARE_REPLACE_NODE_INFO_XSD = "HardwareReplaceNodeInfo.xsd";
    private static final String MIGRATION_NODE_INFO_XSD = "MigrationNodeInfo.xsd";
    private static final String UPGRADE_PACKAGE_NAME_ATTRIBUTE = "upgradePackageName";
    private static final String ADD_TO_ENM = "add_to_enm";
    private static final String ORDER_FAILED_FOR_EOI = "Eoi Integration failed for %";

    private static final Map<String, ActivityType> activityTypes = new HashMap<>();

    static {
        activityTypes.put(GREENFIELD_NODE_INFO_XSD, ActivityType.GREENFIELD_ACTIVITY);
        activityTypes.put(EXPANSION_NODE_INFO_XSD, ActivityType.EXPANSION_ACTIVITY);
        activityTypes.put(HARDWARE_REPLACE_NODE_INFO_XSD, ActivityType.HARDWARE_REPLACE_ACTIVITY);
        activityTypes.put(MIGRATION_NODE_INFO_XSD, ActivityType.MIGRATION_ACTIVITY);
        activityTypes.put(ADD_TO_ENM, ActivityType.EOI_INTEGRATION_ACTIVITY);
    }

    @Inject
    @UseCase(name = UseCaseName.DELETE_PROJECT)
    private DeleteProjectUseCase deleteProjectUseCase;

    @Inject
    @UseCase(name = UseCaseName.DELETE_NODE)
    private DeleteNodeUseCase deleteNodeUseCase;

    @Inject
    private Logger logger;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private ModelCreator nodeModelCreator;

    @Inject
    private ProjectInfoReader projectInfoReader;

    @Inject
    private NodeInfoReader nodeInfoReader;

    @Inject
    private AutomaticLicenseRequestReader automaticLicenseRequestReader;

    @Inject
    private ProjectMoCreator projectMoCreator;

    @Inject
    private NodeArtifactMosCreator nodeArtifactMosCreator;

    @Inject
    private NodeSchemaProcessor nodeSchemaProcessor;

    @Inject
    private ModelReader modelReader;

    @Inject
    private MRExecutionRecorder recorder;

    @Inject
    private ShmDetailsRetriever shmDetailsRetriever;

    @Inject
    private DpsOperations dps;

    @Inject
    private HardwareReplaceUtil hardwareReplaceUtil;

    @Inject
    private MigrationUtil migrationUtil;

    private boolean projectExisting;

    private List<String> greenfieldNodes;

    /**
     * Import a project archive to the AP model.
     *
     * @param projectFileName
     *            the name of the project archive
     * @param projectArchive
     *            the project archive
     * @return the project element data
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public ProjectInfo importProject(final String projectFileName, final Archive projectArchive) {
        final ProjectInfo projectInfo = projectInfoReader.read(projectArchive);
        projectExisting = isProjectExisting(projectInfo.getName());

        if (!projectExisting) {
            createProject(projectInfo);
        }

        createNodes(projectFileName, projectInfo, projectArchive);
        createArtifacts(projectFileName, projectInfo, projectArchive);

        return projectInfo;
    }

    private void createProject(final ProjectInfo projectInfo) {
        ddpTimer.start(CommandLogName.CREATE_PROJECT_MO.toString());
        final String projectFdn = projectMoCreator.create(projectInfo);
        ddpTimer.end(projectFdn);
        logger.info("Creation of AP Project MO for {}", projectInfo.getName());
    }

    private void createNodes(final String projectFileName, final ProjectInfo projectInfo, final Archive archive) {
        final String projectFdn = getProjectFdnByName(projectInfo.getName());
        greenfieldNodes = new ArrayList<>();
        try {
            for (final String directory : archive.getAllDirectoryNames()) {
                final NodeInfo nodeData = nodeInfoReader.read(archive, directory);
                handleLicenseRequestData(archive, directory, nodeData);

                final ActivityType activityType = getActivityType(archive, directory);
                if (isRemoveApDataUsecase(activityType)) {
                    checkApNodeExists(projectFdn, nodeData.getName());
                }
                if (isNotEmpty(nodeData.getDhcpAttributes())) {
                    recorder.recordMRExecution(MRDefinition.AP_DHCP);
                }
                nodeData.setActivity(activityType);
                projectInfo.addNodeInfo(nodeData);

                switch (activityType) {
                    case GREENFIELD_ACTIVITY:
                        setNodeIdentifierUsingUpgradePackageProductDetails(nodeData);
                        final String nodeFdn = nodeModelCreator.create(projectFdn, nodeData);
                        greenfieldNodes.add(nodeFdn);
                        break;
                    case HARDWARE_REPLACE_ACTIVITY:
                        hardwareReplaceUtil.create(projectFdn, nodeData);
                        break;
                    case EXPANSION_ACTIVITY:
                        nodeModelCreator.create(projectFdn, nodeData);
                        break;
                    case MIGRATION_ACTIVITY:
                        migrationUtil.create(nodeData);
                        nodeModelCreator.create(projectFdn, nodeData);
                        break;
                }
            }
        } catch (final ApServiceException e) {
            // need to rollback any created artifacts since each file is created in a new tx
            // Resources JCA component only supports creation of single file in a tx
            // only rollback greenfield nodes
            if (!projectExisting || !greenfieldNodes.isEmpty()) {
                rollbackImport(archive);
            }
            throw e;
        } catch (final Exception e) {
            if (!projectExisting || !greenfieldNodes.isEmpty()) {
                rollbackImport(archive);
            }
            throw new ApApplicationException(String.format(IMPORT_FAILED_FOR_ZIP, projectFileName), e);
        }
    }


 public void createEoiNodes(final List<Map<String, Object>> eoiNetworkElements, final String projectFdn) {
        final List<String> eoiIntegrationNodes = new ArrayList<>();
         try {
             for (final Map<String, Object> eoiNetworkElement : eoiNetworkElements) {
                 final String nodeFdn = nodeModelCreator.eoiCreate(projectFdn, eoiNetworkElement);
                 eoiIntegrationNodes.add(nodeFdn);
             }
         } catch (final ApServiceException e) {
             logger.info("ApServiceException {}", e.getMessage());
             throw e;
         } catch (final Exception e) {
             logger.info("Exception {}", e.getMessage());
             throw new ApApplicationException(String.format(ORDER_FAILED_FOR_EOI, projectFdn), e);
         }
     }

    private void handleLicenseRequestData(Archive archive, String directory, NodeInfo nodeData) {
        final AutomaticLicenseRequestData licenseRequestData = handleAutomaticLicenseAttributes(archive, directory, nodeData);
        if (licenseRequestData != null) {
            nodeData.setAutomaticLicenseReqAttributes(licenseRequestData);
            recorder.recordMRExecution(MRDefinition.AP_INSTANTANEOUS_LICENSE);
        }
    }

    private AutomaticLicenseRequestData handleAutomaticLicenseAttributes(final Archive archive, final String directory, final NodeInfo nodeData) {
        AutomaticLicenseRequestData automaticLicenseRequestData = null;
        if (nodeData.getLicenseAttributes().containsKey(AUTOMATIC_LICENSE_REQUEST)) {
            final String automaticLicenseRequestValue = nodeData.getLicenseAttributes().get(AUTOMATIC_LICENSE_REQUEST).toString();
            automaticLicenseRequestData = automaticLicenseRequestReader.read(archive, directory, automaticLicenseRequestValue);
        }
        return automaticLicenseRequestData;
    }

    private boolean isRemoveApDataUsecase(final ActivityType activityType) {
        return ActivityType.EXPANSION_ACTIVITY.equals(activityType) || ActivityType.HARDWARE_REPLACE_ACTIVITY.equals(activityType) ||
                     ActivityType.MIGRATION_ACTIVITY.equals(activityType);
    }

    private void createArtifacts(final String projectFileName, final ProjectInfo projectInfo, final Archive projectArchive) {
        final String projectFdn = getProjectFdnByName(projectInfo.getName());
        ddpTimer.start(CommandLogName.CREATE_AND_WRITE_PROJECT_ARTIFACTS.toString());

        try {
            nodeArtifactMosCreator.createArtifactsAndMos(projectFdn, projectArchive);
            ddpTimer.end(projectFdn, projectInfo.getNodeQuantity());
            logger.info("Creation of AP MOs of artifacts successful for project {} and file {}", projectInfo.getName(), projectFileName);
        } catch (final ApServiceException e) {
            if (!projectExisting || !greenfieldNodes.isEmpty()) {
                rollbackImport(projectArchive);
            }
            throw e;
        }
    }

    private ActivityType getActivityType(final Archive archive, final String nodeDirectory) {
        final String attributeValue = nodeSchemaProcessor.getNoNamespaceSchemaLocation(archive, nodeDirectory);
        if (attributeValue == null) {
            return activityTypes.get(GREENFIELD_NODE_INFO_XSD);
        }
        switch (attributeValue) {
            case EXPANSION_NODE_INFO_XSD:
            case HARDWARE_REPLACE_NODE_INFO_XSD:
            case MIGRATION_NODE_INFO_XSD:
                return activityTypes.get(attributeValue);
            default:
                return activityTypes.get(GREENFIELD_NODE_INFO_XSD);
        }
    }

    private boolean isProjectExisting(final String projectName) {
        final String projectFdn = getProjectFdnByName(projectName);
        final ManagedObject projectMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(projectFdn);

        return projectMo != null;
    }

    private void rollbackImport(final Archive archive) {
        final String projectFdn = MoType.PROJECT.toString() + "=" + projectInfoReader.read(archive).getName();
        logger.info("Rolling back creation of AP MOs for project {}", projectFdn);

        try {
            if (!projectExisting) {
                deleteProjectUseCase.execute(projectFdn, true);
            } else {
                for (final String nodeFdn : greenfieldNodes) {
                    deleteNodeUseCase.execute(nodeFdn, false);
                }
            }
        } catch (final Exception e) {
            logger.error("Error while rolling back AP Create MOs for the project {}", projectFdn, e);
        }
    }

    private String getProjectFdnByName(final String projectName) {
        return MoType.PROJECT.toString() + "=" + projectName;
    }

    private void setNodeIdentifierUsingUpgradePackageProductDetails(final NodeInfo nodeData) {
        if (isBlank(nodeData.getNodeIdentifier())) {
            final String upgradePackageName = nodeData.getIntegrationAttributes().get(UPGRADE_PACKAGE_NAME_ATTRIBUTE).toString();
            final UpgradePackageProductDetails upgradePackageProductDetails = shmDetailsRetriever.getUpgradePackageProductDetails(upgradePackageName,
                nodeData.getNodeType());
            final String ossModelIdentity = modelReader.getOssModelIdentity(nodeData.getNodeType(),
                upgradePackageProductDetails.getProductNumber(), upgradePackageProductDetails.getProductRevision());
            nodeData.setNodeIdentifier(ossModelIdentity);
            nodeData.getNodeAttributes().put(NodeAttribute.NODE_IDENTIFIER.toString(), ossModelIdentity);
        }
    }

    private void checkApNodeExists(final String projectFdn, final String nodeName) {
        final String nodeFdn = new StringBuilder().append(projectFdn).append(",Node=").append(nodeName).toString();
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        if (nodeMo != null) {
            throw new ApNodeExistsException(FDN.get(nodeFdn).getRdnValue());
        }
    }
}
