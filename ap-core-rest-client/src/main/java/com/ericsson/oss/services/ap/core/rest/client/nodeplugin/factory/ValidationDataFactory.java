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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ConfigurationFile;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationData;
import com.ericsson.oss.services.ap.core.rest.client.shm.ShmRestClient;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.UpgradePackageProductDetails;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.ShmDetailsRetriever;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactContainerAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;

/**
 * A factory class that is responsible to create the validation data to be used in the ValidationConfigurationService
 */
public class ValidationDataFactory {

    private static final String UNDERSCORE_SEPARATOR = "_";
    private static final String SLASH_SEPARATOR = "/";
    private static final String SITE_EQUIPMENT = "SiteEquipment";
    private static final String SITE_BASIC = "SiteBasic";
    private static final String UPGRADE_PACKAGE = "upgradePackageName";
    private static final String UNLOCK_CELLS = "unlockCell";
    private static final String NODE_CONFIGURATION = "nodeConfiguration";
    private static final String OPTIONAL_FEATURE = "optionalFeature";
    private static final String MANAGED_ELEMENT_TAG = "ManagedElement";
    private static final String READ_NODE_CONFIG_FILE_FAIL_MSG = "Failed to get node configuration required for NETCONF validation.";
    private static final String NODE_CONFIG_FILE_NOT_EXIST_MSG = READ_NODE_CONFIG_FILE_FAIL_MSG + "Node configuration file %s does not exist.";

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    @Inject
    private ShmDetailsRetriever shmDetailsRetriever;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private ShmRestClient shmRestClient;

    @Inject
    private RetryManager retryManager;

    @Inject
    private SystemRecorder recorder; // NOPMD

    @Inject
    private Logger logger;

    private DataPersistenceService dps;

    @PostConstruct
    public void init() {
        dps = new ServiceFinderBean().find(DataPersistenceService.class);
    }

    /**
     * Creates the Validation object required for calling the Node Plugin service : Product Number, Revision and Configuration Files (siteBasic.xml,
     * siteEquipment.xml, etc.) if the provided data is correct.
     *
     * @param apNodeFdn
     *              the node fdn
     * @param nodeType
     *              the node type
     * @return an object containing the Product Number, Revision and Configuration Files
     */
    public ValidationData createValidationData(final String apNodeFdn, final String nodeType) {
        final String upgradePackageName = retrieveUpgradePackageData(apNodeFdn);
        recorder.recordEvent(String.format("Getting product details. Upgrade package name: %s and node type: %s", upgradePackageName,
            nodeType), EventLevel.DETAILED, apNodeFdn, "", "");
        return processData(apNodeFdn, nodeType, upgradePackageName, null);
    }

    /**
     * Creates the Validation object required for calling the Node Plugin service : Product Number, Revision, Configuration Files and
     * Preconfiguration File if the provided data is correct.
     *
     * @param apNodeFdn
     *              the node fdn
     * @param nodeType
     *              the node type
     * @return an object containing the Product Number, Revision, Preconfiguration File and Configuration Files
     */
    public ValidationData createDeltaValidationData(final String apNodeFdn, final String nodeType) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final String upgradePackageName = shmRestClient.getUpgradePackageName(nodeName, nodeType);
        recorder.recordEvent(String.format("Getting product details from SHM. Upgrade package name: %s and node type: %s", upgradePackageName,
                nodeType), EventLevel.DETAILED, apNodeFdn, "", "");
        final ConfigurationFile preconfigurationFile = getPreconfigurationFile(apNodeFdn);
        return processData(apNodeFdn, nodeType, upgradePackageName, preconfigurationFile);
    }

    /**
     * Returns the product number. Product number underscore separator is replaced with slash separator.
     *
     * @param productNumber
     *            with the underscore separator
     * @return productNumber with the slash separator
     */
    public String getProductNumber(final String productNumber) {
        if (productNumber != null && productNumber.contains(UNDERSCORE_SEPARATOR)) {
            return productNumber.replace(UNDERSCORE_SEPARATOR, SLASH_SEPARATOR);
        }
        return productNumber;
    }

    private String retrieveUpgradePackageData(final String apNodeFdn) {
        final ManagedObject autoIntegrationMo = getNodeMo(apNodeFdn).getChild(MoType.AI_OPTIONS.toString() + "=1");
        if (autoIntegrationMo == null) {
            throw new ApApplicationException("AutoIntegrationOptions child MO not found for " + apNodeFdn);
        }
        return autoIntegrationMo.getAttribute(UPGRADE_PACKAGE);
    }

    private ManagedObject getNodeMo(final String apNodeFdn) {
        final ManagedObject nodeMo = dps.getLiveBucket().findMoByFdn(apNodeFdn);
        if (nodeMo == null) {
            throw new ApApplicationException("No AP node found for FDN " + apNodeFdn);
        }
        return nodeMo;
    }

    private ValidationData processData(final String apNodeFdn, final String nodeType, final String upgradePackageName,
            final ConfigurationFile preconfigurationFile) {
        final ValidationData validationData = new ValidationData();
        final UpgradePackageProductDetails upgradePackage = shmDetailsRetriever.getUpgradePackageProductDetails(upgradePackageName, nodeType);
        validationData.setNodeType(nodeType);
        validationData.setProductNumber(getProductNumber(upgradePackage.getProductNumber()));
        validationData.setRevision(upgradePackage.getProductRevision());
        validationData.setUpgradePackagePath(shmDetailsRetriever.getUpgradePackageAbsolutePath(upgradePackageName));
        validationData.setPreconfigurationFile(preconfigurationFile);
        validationData.setConfigurationFiles(preconfigurationFile == null ? getConfigurationFiles(apNodeFdn) :
            getNetconfNodeConfigurationFiles(apNodeFdn));
        recorder.recordEvent(String.format("Product number: %s and revision: %s", validationData.getProductNumber(),
            validationData.getRevision()), EventLevel.DETAILED, apNodeFdn, "", "");
        return validationData;
    }

    private List<ConfigurationFile> getConfigurationFiles(final String apNodeFdn) {
        final List<ConfigurationFile> configurationFileList = new ArrayList<>();
        configurationFileList.add(this.getConfigurationFile(apNodeFdn, SITE_EQUIPMENT));
        configurationFileList.add(this.getConfigurationFile(apNodeFdn, SITE_BASIC));

        if (!isImportConfigurationInStrictSequence(apNodeFdn)) {
            final List<ConfigurationFile> netconfConfigurations = getNetconfNodeConfigurationFiles(apNodeFdn);
            if (!netconfConfigurations.isEmpty()) {
                configurationFileList.addAll(netconfConfigurations);
            }
        } else {
            logger.info("Node Plugin validation for NETCONF configuration files is skipped due to strict attribute set for {}. Only {} and {} are sent for validation.",
                    apNodeFdn, SITE_BASIC, SITE_EQUIPMENT);
        }
        return configurationFileList;
    }

    private ConfigurationFile getConfigurationFile(final String apNodeFdn, final String fileName) {
        final ArtifactDetails artifactDetails = rawArtifactHandler.readFirstOfType(apNodeFdn, fileName);
        final ConfigurationFile configurationFile = new ConfigurationFile();
        configurationFile.setFileName(artifactDetails.getNameWithExtension());
        recorder.recordEvent(String.format("Adding file to be validated: %s", artifactDetails.getNameWithExtension()),
            EventLevel.DETAILED, apNodeFdn, "", "");
        configurationFile.setFileContent(artifactDetails.getArtifactContent());
        return configurationFile;
    }

    private boolean isImportConfigurationInStrictSequence(final String apNodeFdn) {
        final ManagedObject artifactContainerMo = getNodeMo(apNodeFdn).getChild(MoType.NODE_ARTIFACT_CONTAINER.toString() + "=1");
        if (artifactContainerMo == null) {
            throw new ApApplicationException("NodeArtifactContainer child MO not found for " + apNodeFdn);
        }
        final Boolean strictAttribute = artifactContainerMo.getAttribute(NodeArtifactContainerAttribute.STRICT.toString());
        return (strictAttribute != null) && strictAttribute;
    }

    private ConfigurationFile getPreconfigurationFile(final String apNodeFdn) {
        try {
            final String filePath = GeneratedArtifactHandler.getNetconfPreconfigurationFileFullpathForNode(apNodeFdn);
            final String fileName = new File (filePath).getName();

            final String fileContent = getFileContent(filePath);
            logger.info("Size of node configuration file {} is: {}", fileName, fileContent != null ? fileContent.length() : 0);
            validatePreconfigFileContents(fileName, fileContent);

            final ConfigurationFile preconfigurationFile = new ConfigurationFile();
            preconfigurationFile.setFileName(fileName);
            recorder.recordEvent(String.format("Adding file to assist validation: %s", fileName),
                EventLevel.DETAILED, "", "", "");
            preconfigurationFile.setFileContent(fileContent);
            return preconfigurationFile;
        } catch (final Exception e) {
            logger.error("Error getting preconfig file for {}", apNodeFdn, e);
            throw new ApApplicationException(READ_NODE_CONFIG_FILE_FAIL_MSG, e);
        }
    }

    private String getFileContent(final String filePath) {
        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(15)
                .waitInterval(3, TimeUnit.SECONDS)
                .retryOn(ApApplicationException.class)
                .build();

        try {
            retryManager.executeCommand(policy, new RetriableCommand<Void>() {
                @Override
                public Void execute(final RetryContext retryContext) throws Exception {
                    if (!Resources.getFileSystemResource(filePath).exists()) {
                        logger.info("{} file not ready. Waiting and recheck {}", filePath, retryContext.getCurrentAttempt());
                        throw new ApApplicationException(String.format(NODE_CONFIG_FILE_NOT_EXIST_MSG, filePath));
                    }
                    return null;
                }
            });
        } catch (final RetriableCommandException e) {
            logger.error("RetriableCommand failed", e);
            throw new ApApplicationException(String.format(NODE_CONFIG_FILE_NOT_EXIST_MSG, filePath));
        }

        return artifactResourceOperations.readArtifactAsText(filePath);
    }

    private void validatePreconfigFileContents(final String fileName, final String fileContent) {
        final DocumentReader preconfigDocumentReader = new DocumentReader(fileContent);
        final String rootTag = preconfigDocumentReader.getRootTag();
        if (!MANAGED_ELEMENT_TAG.equals(rootTag)) {
            throw new ApApplicationException(String.format("Invalid Pre-configuration file %s, root element is %s", fileName, rootTag));
        }
    }

    private List<ConfigurationFile> getNetconfNodeConfigurationFiles(final String apNodeFdn) {

        final Collection<ArtifactDetails> configurationArtifacts = rawArtifactHandler.readAllOfType(apNodeFdn, NODE_CONFIGURATION);
        final Collection<ArtifactDetails> optionalFeatureArtifacts = rawArtifactHandler.readAllOfType(apNodeFdn, OPTIONAL_FEATURE);
        final Collection<ArtifactDetails> unlockCellArtifacts = rawArtifactHandler.readAllOfType(apNodeFdn, UNLOCK_CELLS);

        if (optionalFeatureArtifacts != null) {
            configurationArtifacts.addAll(optionalFeatureArtifacts);
        }

        if (unlockCellArtifacts != null) {
            configurationArtifacts.addAll(unlockCellArtifacts);
        }

        final List<ConfigurationFile> configurationFiles = new ArrayList<>();
        if (!configurationArtifacts.isEmpty()) {
            for (final ArtifactDetails artifactDetails : configurationArtifacts) {
                if (configurationFileIsNetconfType(artifactDetails)) {
                    final ConfigurationFile configurationFile = new ConfigurationFile();
                    configurationFile.setFileName(artifactDetails.getNameWithExtension());
                    recorder.recordEvent(String.format("Adding netconf file to be validated: %s", artifactDetails.getNameWithExtension()),
                        EventLevel.DETAILED, apNodeFdn, "", "");
                    configurationFile.setFileContent(artifactDetails.getArtifactContent());
                    configurationFiles.add(configurationFile);
                }
            }
        }
        return configurationFiles;
    }

    private boolean configurationFileIsNetconfType(final ArtifactDetails artifactDetails) {
        return artifactDetails.getArtifactContent().contains("<rpc");
    }
}
