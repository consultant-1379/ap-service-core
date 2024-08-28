/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.common.model.CmSyncStatus.SYNCHRONIZED;
import static com.ericsson.oss.services.ap.common.model.MoType.CM_FUNCTION;
import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel.INSTANCE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.model.CmFunctionAttribute;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;

/**
 * Rule to validate the remote node availability for nodename attribute and the remote node status
 */
@Groups(value = {@Group(name = ValidationRuleGroups.ORDER, priority = 13, abortOnFail = true),
                 @Group(name = ValidationRuleGroups.EXPANSION, priority = 10, abortOnFail = true)})
@Rule(name = "ValidateRemoteNodeAvailability")
public class ValidateRemoteNodeAvailability extends AbstractValidateRule {

    private static final String VALIDATION_FAIL_NODE_MUST_BE_SYNCHRONIZED_IN_ENM = "validation.remote.node.not.synchronized.in.enm.failure";
    private static final String VALIDATION_FAIL_NODE_MUST_EXIST_IN_ENM = "validation.remote.node.does.not.exist.failure";
    private static final String VALIDATION_FILE_FORMAT_NOT_SUPPORTED_ERROR = "validation.remote.file.format.not.supported";
    private static final String VALIDATION_NODENAME_NOT_SET_ERROR = "validation.remote.node.name.not.set";
    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String NETCONF_KEY_CHARACTOR = "<rpc";
    private static final String REPLACE_REGEX = "\\<\\?xml(.+?)\\?\\>";
    private static final String NETCONF_CAPABILITY = "APPLY_NETCONF_POST_SYNC";
    private static final String IS_SUPPORTED = "isSupported";

    @Inject
    private NodeInfoReader nodeInfoReader;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Archive archive = getArchive(context);
        for (final String directoryName : directoryNames) {
            final NodeInfo nodeInfo = nodeInfoReader.read(archive, directoryName);
            final Map<String, String> remoteNodeNames = nodeInfo.getRemoteNodeNames();
            final Map<String, Map<String, ArtifactFileFormat>> validatingNodes = new HashMap<>();
            validateRemoteNodeConfigArtifact(context, directoryName, archive, remoteNodeNames, validatingNodes);
            validateRemoteNodeAvailable(context, directoryName, validatingNodes);
        }

        return isValidatedWithoutError(context);
    }

    private void validateRemoteNodeConfigArtifact(final ValidationContext context, final String directoryName, final Archive archive,
                                                  final Map<String, String> remoteNodeNames,
                                                  final Map<String, Map<String, ArtifactFileFormat>> validatingNodes) {
        remoteNodeNames.forEach((fileName, nodeName) -> {
            final ArchiveArtifact remoteNodeConfigArtifact = archive.getArtifactOfNameInDir(directoryName, fileName);
            final ArtifactFileFormat currentArtifactFormat = getFileFormat(remoteNodeConfigArtifact);
            if (StringUtils.isNotBlank(nodeName)) {
                Map<String, ArtifactFileFormat> fileFormats = validatingNodes.get(nodeName);
                if (fileFormats == null) {
                    fileFormats = new HashMap<>();
                    validatingNodes.put(nodeName, fileFormats);
                }
                fileFormats.put(fileName, currentArtifactFormat);
            } else if (currentArtifactFormat.equals(ArtifactFileFormat.NETCONF)) {
                recordNodeValidationError(context, VALIDATION_NODENAME_NOT_SET_ERROR, directoryName, fileName);
            }
        });
    }

    private ArtifactFileFormat getFileFormat(final ArchiveArtifact archiveArtifact) {
        final String fileContent = archiveArtifact.getContentsAsString();
        if (StringUtils.isNotBlank(fileContent) && fileContent.replaceAll(REPLACE_REGEX, "").trim().contains(NETCONF_KEY_CHARACTOR)) {
            return ArtifactFileFormat.NETCONF;
        }
        return ArtifactFileFormat.BULK_3GPP;
    }

    private void validateRemoteNodeAvailable(final ValidationContext context, final String directoryName,
                                             final Map<String, Map<String, ArtifactFileFormat>> validatingNodes) {
        validatingNodes.forEach((nodename, fileFormats) -> {
            try {
                final ManagedObject networkElementMo = findMo(nodename, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString());
                if (networkElementMo == null) {
                    final String message = apMessages.format(VALIDATION_FAIL_NODE_MUST_EXIST_IN_ENM, nodename);
                    addNodeValidationFailure(context, message, directoryName);
                } else {
                    final String nodeType = networkElementMo.getAttribute(NetworkElementAttribute.NE_TYPE.toString());
                    if (fileFormats.containsValue(ArtifactFileFormat.NETCONF)
                            && !INSTANCE.getAttributeAsBoolean(nodeType, NETCONF_CAPABILITY, IS_SUPPORTED)) {
                        recordFileFormatError(context, directoryName, fileFormats, nodeType, nodename, ArtifactFileFormat.NETCONF);
                    } else {
                        final ManagedObject cmFunctionMo = networkElementMo.getChild(String.format("%s=1", CM_FUNCTION.toString()));
                        if (cmFunctionMo == null
                                || !cmFunctionMo.getAttribute(CmFunctionAttribute.SYNC_STATUS.toString()).equals(SYNCHRONIZED.toString())) {
                            final String message = apMessages.format(VALIDATION_FAIL_NODE_MUST_BE_SYNCHRONIZED_IN_ENM, nodename);
                            addNodeValidationFailure(context, message, directoryName);
                        }
                    }
                }
            } catch (final Exception e) {
                logger.error("Unexpected error while validating that nodeName is synchronized in ENM for node {}", nodename, e);
                throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
            }
        });
    }

    private void recordFileFormatError(final ValidationContext context, final String directoryName, final Map<String, ArtifactFileFormat> fileFormats,
                                       final String nodeType, final String nodename, final ArtifactFileFormat unsupportedFileFormat) {
        fileFormats.forEach((filename, format) -> {
            if (format.equals(unsupportedFileFormat)) {
                final String message = apMessages.format(VALIDATION_FILE_FORMAT_NOT_SUPPORTED_ERROR, filename, format, nodename, nodeType);
                addNodeValidationFailure(context, message, directoryName);
            }
        });
    }

}
