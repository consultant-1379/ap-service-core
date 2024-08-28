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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.api.NodePluginCapabilityVersion.V1;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.RAW_LOCATION;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.TYPE;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_IDENTIFIER;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;
import static com.ericsson.oss.services.ap.common.model.NodeStatusAttribute.STATE;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.NodePluginCapabilityType;
import com.ericsson.oss.services.ap.api.UploadArtifactService;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.api.exception.IllegalUploadNodeStateException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedArtifactTypeException;
import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.workflow.NodePluginCapabilityValidationService;
import com.ericsson.oss.services.ap.api.workflow.ValidationConfigurationService;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactContainerAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.common.validation.configuration.ConfigurationFileValidator;
import com.ericsson.oss.services.ap.common.workflow.recording.ErrorRecorder;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Upload a configuration artifact file for a given AP node.
 */
@UseCase(name = UseCaseName.UPLOAD_ARTIFACT)
public class UploadArtifactUseCase {

    private static final String VALIDATION_NODE_ARTIFACT_SCHEMA_ERROR = "validation.project.zip.file.schema.node.failure";
    private static final String VALIDATION_SCHEMA_ACCESS = "validation.artifact.schema.access";
    private static final String NETCONF_NODE_PLUGIN_VALIDATION_CAPABILITY = "NETCONF_NODE_PLUGIN_VALIDATION";
    private static final String IS_SUPPORTED_CAPABILITY_ATTRIBUTE = "isSupported";
    private static final String SITE_BASIC_TYPE = "siteBasic";
    private static final String SITE_EQUIPMENT_TYPE = "siteEquipment";

    private final DdpTimer ddpTimer = new DdpTimer();

    private final ApMessages apMessages = new ApMessages();

    private ResourceService resourceService;

    private ValidationConfigurationService validationConfigurationService;

    private NodePluginCapabilityValidationService nodePluginCapabilityValidationService;

    @Inject
    private DpsOperations dps;

    @Inject
    private ArtifactResourceOperations resourceOperations;

    @Inject
    private ConfigurationFileValidator configurationFileValidator;

    @Inject
    private Logger logger;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private NodeArtifactMoOperations artifactOperations;

    @Inject
    private SchemaService schemaService;

    @Inject
    private XmlValidator xmlValidator;

    @Inject
    private ErrorRecorder errorRecorder;

    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); //NOPMD

    @PostConstruct
    public void init() {
        resourceService = serviceFinder.find(ResourceService.class);
        validationConfigurationService = serviceFinder.find(ValidationConfigurationService.class);
        nodePluginCapabilityValidationService = serviceFinder.find(NodePluginCapabilityValidationService.class);
    }

    /**
     * Uploads an artifact for a node.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @param fileName
     *            the name of the artifact file
     * @param fileContent
     *            the artifact file contents
     */
    public void execute(final String nodeFdn, final String fileName, final byte[] fileContent) {
        ddpTimer.start(CommandLogName.UPLOAD_ARTIFACT.toString());
        try {
            logger.info("Attempting to upload file: [{}]", fileName);
            uploadArtifact(nodeFdn, fileName, fileContent);
            logger.info("Successful execution of upload artifact for node {}", nodeFdn);
        } catch (final ApApplicationException e) {
            errorRecorder.uploadArtifactFailed(nodeFdn, fileName, e);
            ddpTimer.end(nodeFdn);
            throw e;
        } catch (final Exception e) {
            errorRecorder.uploadArtifactFailed(nodeFdn, fileName, e);
            ddpTimer.end(nodeFdn);
            throw new ApApplicationException(String.format("Upload failed for node: %s, file %s%n%s", nodeFdn, fileName, e.getMessage()), e);
        }
    }

    private void uploadArtifact(final String nodeFdn, final String fileName, final byte[] fileContent) {
        final ManagedObject artifactMo = getNodeArtifact(nodeFdn, fileName);
        final ManagedObject nodeMo = getNodeMo(nodeFdn);

        final String nodeType = nodeMo.getAttribute(NODE_TYPE.toString());
        final String internalEJBQualifier = nodeTypeMapper.getInternalEjbQualifier(nodeType);

        final UploadArtifactService uploadResolver = serviceFinder.find(UploadArtifactService.class, internalEJBQualifier);

        final String artifactType = artifactMo.getAttribute(TYPE.toString());
        final ArtifactFileFormat artifactFileFormat = resourceOperations.readArtifactFileFormat(artifactType, fileContent);

        validateArtifactType(artifactType, uploadResolver);
        validateNodeState(nodeMo, artifactType, uploadResolver);
        validateArtifactAgainstSchema(nodeMo, artifactType, fileName, fileContent);
        validateConfigurationFormatEquivalence(artifactMo, fileName, artifactFileFormat);
        validateConfigurationArtifact(nodeFdn, fileName, fileContent, artifactFileFormat);
        if (!artifactFileFormat.equals(ArtifactFileFormat.NETCONF)) {
            replaceFileContent(artifactMo, fileContent);
        }

        try {
            uploadResolver.createGeneratedArtifact(artifactType, nodeFdn);
        } catch (final UnsupportedOperationException e) {
            logger.error("Create generated artifact operation not supported for artifacts of type: {}", artifactType, e);
        } catch (final Exception e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                logger.trace("Create generated artifact operation not supported for artifacts of type: {}", artifactType);
            } else {
                throw e;
            }
        }
    }

    private void validateNodeState(final ManagedObject nodeMo, final String artifactType, final UploadArtifactService uploadResolver) {
        final String nodeStateFdn = nodeMo.getFdn() + ",NodeStatus=1";
        final ManagedObject nodeStateMo = getNodeMo(nodeStateFdn);
        final String nodeState = nodeStateMo.getAttribute(STATE.toString());

        if (!uploadResolver.getValidStatesForUpload(artifactType).contains(nodeState)) {
            final String errorMessage = String.format("Node is not in correct state for upload of artifact type : %s", artifactType);
            throw new IllegalUploadNodeStateException(errorMessage, nodeState, new ArrayList<>(uploadResolver.getValidStatesForUpload(artifactType)));
        }
    }

    private void validateConfigurationArtifact(final String nodeFdn, final String fileName, final byte[] fileContent,
                                               final ArtifactFileFormat artifactFileFormat) {
        final ManagedObject artifactMo = getNodeArtifact(nodeFdn, fileName);

        switch (artifactFileFormat) {
            case NETCONF :
                validateNetconfArtifact(nodeFdn, artifactMo, fileName, fileContent);
                break;
            case BULK_3GPP :
                final ManagedObject nodeMo = getNodeMo(nodeFdn);
                validateBulkCMArtifact(nodeMo, fileName, new String(fileContent, StandardCharsets.UTF_8));
                break;
            case AMOS_SCRIPT:
                validateAMOSArtifact(artifactMo, fileName);
                break;
            default :
                break;
        }
    }

    private void validateNetconfArtifact(final String nodeFdn, final ManagedObject artifactMo,
                                         final String fileName, final byte[] fileContent) {
        final String artifactType = artifactMo.getAttribute(TYPE.toString());
        final String artifactFilePath = artifactMo.getAttribute(RAW_LOCATION.toString());
        final byte[] originalFileContent = resourceService.getBytes(artifactFilePath);
        final ManagedObject nodeMo = getNodeMo(nodeFdn);
        final String nodeType = nodeMo.getAttribute(NODE_TYPE.toString());
        final boolean isImportConfigurationInStrictSequence = isImportConfigurationInStrictSequence(nodeMo);
        final String response;

        resourceOperations.writeArtifact(artifactFilePath, fileContent);
        if (!NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(nodeType, NETCONF_NODE_PLUGIN_VALIDATION_CAPABILITY, IS_SUPPORTED_CAPABILITY_ATTRIBUTE)
                || "remoteNodeConfiguration".equals(artifactType) || "preMigrationConfiguration".equals(artifactType)
                || (isImportConfigurationInStrictSequence && !isSiteArtifact(artifactType))) {
            return;
        }

        try {
            if (hasFullConfigurations(nodeFdn)) {
                if (!isNodePluginCapabilitySupported(nodeType, NodePluginCapabilityType.VALIDATE)) {
                    return;
                }
                response = validationConfigurationService.validateConfiguration(nodeFdn, nodeType);
            } else {
                final String preConfigFilePath = GeneratedArtifactHandler.getNetconfPreconfigurationFileFullpathForNode(nodeFdn);
                if (!resourceService.exists(preConfigFilePath)) {
                    return;
                }
                response = validationConfigurationService.validateDeltaConfiguration(nodeFdn, nodeType);
            }
            if (StringUtils.isNotBlank(response)) {
                logger.warn("Warn validating Netconf file {}: {}", fileName, response);
            }
        } catch (final Exception e) {
            resourceOperations.writeArtifact(artifactFilePath, originalFileContent);
            logger.error("Error validating Netconf file {}", fileName, e);
            final String validationErrorMessage = String.format("Error validating Netconf file %s: %s", fileName, e.getMessage());
            throw new ValidationException(Collections.<String> emptyList(), validationErrorMessage);
        }
    }

    private void validateBulkCMArtifact(final ManagedObject nodeMo, final String fileName, final String fileContent) {
        final String nodeIdentifier = nodeMo.getAttribute(NodeAttribute.NODE_IDENTIFIER.toString());
        final String nodeName = nodeMo.getName();
        final ManagedObject projectMo = nodeMo.getParent();
        final String projectName = projectMo.getName();
        final ArchiveArtifact configurationFile = new ArchiveArtifact(fileName, fileContent);

        final List<String> validationErrors = configurationFileValidator.validateFile(projectName, nodeName, nodeIdentifier, configurationFile);

        if (!validationErrors.isEmpty()) {
            logger.error("Error validating file {} for node {}", fileName, nodeName);
            throw new ValidationException(Collections.<String> emptyList(), buildErrorMessage(validationErrors, nodeName));
        }
    }

    private void validateAMOSArtifact(final ManagedObject artifactMo, final String fileName) {
        final String artifactFilePath = artifactMo.getAttribute(RAW_LOCATION.toString());
        if (!resourceService.exists(artifactFilePath)) {
            final String validationErrorMessage = String
                    .format("Upload not supported, file %s is in shared folder and can only be updated from terminal", fileName);
            logger.error(validationErrorMessage);
            throw new ValidationException(Collections.<String> emptyList(), validationErrorMessage);
        }
    }

    private static String buildErrorMessage(final List<String> validationErrors, final String nodeName) {
        final StringBuilder errorMessage = new StringBuilder();
        errorMessage.append("Error validating file for node ").append(nodeName);

        for (final String validationError : validationErrors) {
            errorMessage.append(' ').append(validationError);
        }

        return errorMessage.toString();
    }

    private void validateArtifactAgainstSchema(final ManagedObject nodeMo, final String artifactType, final String fileName, final byte[] fileContent) {
        final SchemaData schema = readArtifactSchema(nodeMo, artifactType);
        if (schema != null) {
            try {
                xmlValidator.validateAgainstSchema(new String(fileContent, StandardCharsets.UTF_8), schema.getData());
            } catch (final SchemaValidationException e) {
                logger.error("Error validating {} XSD schema: {}", fileName, e.getMessage(), e);
                final String validationErrorMessage = apMessages.format(VALIDATION_NODE_ARTIFACT_SCHEMA_ERROR, fileName,
                        e.getValidationError());
                throw new ValidationException(Collections.<String> emptyList(), validationErrorMessage);
            } catch (final SchemaAccessException e) {
                logger.error("Error accessing {} XSD schema: {}", fileName, e.getMessage(), e);
                final String validationErrorMessage = apMessages.format(VALIDATION_SCHEMA_ACCESS, fileName);
                throw new ValidationException(Collections.<String> emptyList(), validationErrorMessage);
            }
        }
    }

    private SchemaData readArtifactSchema(final ManagedObject nodeMo, final String artifactType) {
        final String nodeIdentifier = nodeMo.getAttribute(NODE_IDENTIFIER.toString());
        final String nodeType = nodeMo.getAttribute(NODE_TYPE.toString());
        try {
            return schemaService.readSchema(nodeType, nodeIdentifier, artifactType);
        } catch (final ArtifactDataNotFoundException e) {
            logger.debug("Error reading schema -> artifactType={}, nodeType{}, nodeIdentifier={}", artifactType, nodeType, nodeIdentifier, e);
        }
        return null;
    }

    private static void validateArtifactType(final String artifactType, final UploadArtifactService uploadResolver) {
        final Set<String> supportedTypes = uploadResolver.getSupportedUploadTypes();
        if (!supportedTypes.contains(artifactType)) {
            throw new UnsupportedArtifactTypeException("Upload not supported for file type", artifactType);
        }
    }

    private void validateConfigurationFormatEquivalence(final ManagedObject artifactMo, final String fileName, final ArtifactFileFormat artifactFileFormat) {
        final String artifactFilePath = artifactMo.getAttribute(RAW_LOCATION.toString());
        final ArtifactFileFormat originalArtifactFileFormat = artifactOperations.getArtifactFileFormat(artifactFilePath);
        if (!artifactFileFormat.equals(originalArtifactFileFormat)) {
            logger.error("Error validating file {}, file format {} is not the same as the original format {}", fileName, artifactFileFormat, originalArtifactFileFormat);
            final String validationErrorMessage = String.format("Error validating file %s, file format %s is not the same as the original format %s",
                    fileName, artifactFileFormat.toString(), originalArtifactFileFormat.toString());
            throw new ValidationException(Collections.<String> emptyList(), validationErrorMessage);
        }
    }

    private void replaceFileContent(final ManagedObject artifactMo, final byte[] fileContent) {
        final String artifactFilePath = artifactMo.getAttribute(RAW_LOCATION.toString());
        resourceOperations.writeArtifact(artifactFilePath, fileContent);
    }

    private ManagedObject getNodeArtifact(final String nodeFdn, final String fileName) {
        final Collection<ManagedObject> nodeArtifactMos = artifactOperations.getNodeArtifactMos(nodeFdn);

        for (final ManagedObject nodeArtifactMo : nodeArtifactMos) {
            final String rawLocation = nodeArtifactMo.getAttribute(RAW_LOCATION.toString());
            if ((rawLocation != null) && fileNameMatches(rawLocation, fileName)) {
                return nodeArtifactMo;
            }
        }

        throw new ArtifactNotFoundException(String.format("No node artifact found with fileName:%s for %s", fileName, nodeFdn));
    }

    private static boolean fileNameMatches(final String rawLocation, final String fileName) {
        final String actualName = rawLocation.substring(rawLocation.lastIndexOf('/') + 1);
        return actualName.equalsIgnoreCase(fileName);
    }

    private ManagedObject getNodeMo(final String nodeFdn) {
        return dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
    }

    private boolean hasFullConfigurations(final String nodeFdn) {
        final Collection<ManagedObject> nodeArtifactMos = artifactOperations.getNodeArtifactMosOfType(nodeFdn, "siteBasic");
        return !nodeArtifactMos.isEmpty();
    }

    private boolean isNodePluginCapabilitySupported(final String nodeType, final NodePluginCapabilityType capability) {
        return nodePluginCapabilityValidationService.validateCapability(nodeType, V1, capability);
    }

    private boolean isImportConfigurationInStrictSequence(final ManagedObject nodeMo) {
        final ManagedObject nodeArtifactContainerMo = nodeMo.getChild(MoType.NODE_ARTIFACT_CONTAINER.toString() + "=1");
        final Boolean strict = nodeArtifactContainerMo.getAttribute(NodeArtifactContainerAttribute.STRICT.toString());
        return (strict != null) && strict;
    }

    private boolean isSiteArtifact(final String artifactType) {
        return SITE_BASIC_TYPE.equalsIgnoreCase(artifactType) || SITE_EQUIPMENT_TYPE.equalsIgnoreCase(artifactType);
    }
}
