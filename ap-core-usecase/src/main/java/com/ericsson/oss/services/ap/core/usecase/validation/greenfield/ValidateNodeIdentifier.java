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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_IDENTIFIER;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.artifacts.UpgradePackageProductDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.ShmDetailsRetriever;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Rule to validate that all nodes in an AP project have valid node identifiers.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 8, abortOnFail = true)
@Rule(name = "ValidateNodeIdentifier")
public class ValidateNodeIdentifier extends ZipBasedValidation {

    private static final String UPGRADE_PACKAGE_NAME_ATTRIBUTE = "upgradePackageName";
    private static final String VALIDATION_NODEIDENTIFIER_NOT_DETERMINED_ERROR = "validation.project.zip.file.node.nodeidentifier.not.determined";
    private static final String VALIDATION_NODEIDENTIFIER_NOT_SUPPORTED_ERROR = "validation.project.zip.file.node.nodeidentifier.not.supported";

    @Inject
    private ModelReader modelReader;

    @Inject
    private ShmDetailsRetriever shmDetailsRetriever;

    private String updatedNodeIdentifier;


    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean allNodeIdetifiersValid = true;
        for (final String directoryName : directoryNames) {
            final DocumentReader nodeInfoDoc = readNodeInfo(context, directoryName);
            allNodeIdetifiersValid &= isSupportedNodeIdentifier(context, nodeInfoDoc, directoryName);
        }

        return allNodeIdetifiersValid;
    }

    private DocumentReader readNodeInfo(final ValidationContext context, final String directoryName) {
        final ArchiveArtifact nodeInfo = getArtifactOfNameInDir(context, directoryName, NODEINFO.toString());
        return new DocumentReader(nodeInfo.getContentsAsString());
    }

    private boolean isSupportedNodeIdentifier(final ValidationContext context, final DocumentReader nodeInfoDoc, final String dirName) {
        final String nodeType = nodeInfoDoc.getElementValue(NODE_TYPE.toString());
        final String nodeIdentifier = nodeInfoDoc.getElementValue(NODE_IDENTIFIER.toString());

        if (!isNodeIdentifierValueDefined(context, nodeIdentifier, dirName)) {
            return false;
        }

        if (!isNodeIdentifierSupported(nodeType, nodeIdentifier)) {
            recordNodeValidationError(context, VALIDATION_NODEIDENTIFIER_NOT_SUPPORTED_ERROR, dirName, nodeIdentifier);
            return false;
        }

        return true;
    }

    private boolean isNodeIdentifierValueDefined(final ValidationContext context, final String nodeIdentifier, final String dirName) {
        if (isBlank(nodeIdentifier)) {
            final NodeInfo nodeInfo = getNodeInfo(context, dirName);
            final String upgradePackageName = nodeInfo.getIntegrationAttributes().get(UPGRADE_PACKAGE_NAME_ATTRIBUTE).toString();
            final UpgradePackageProductDetails upgradePackageProductDetails = shmDetailsRetriever.getUpgradePackageProductDetails(upgradePackageName, nodeInfo.getNodeType());
            logger.info("SHM returned Product Number: {} and Product Revision: {} for Node: {} with Upgrade Package Name: {} given in NodeInfo.xml",
                upgradePackageProductDetails.getProductNumber(), upgradePackageProductDetails.getProductRevision(), nodeInfo.getName(), upgradePackageName);
            final String ossModelIdentity = modelReader.getOssModelIdentity(nodeInfo.getNodeType(), upgradePackageProductDetails.getProductNumber(), upgradePackageProductDetails.getProductRevision());
            logger.info("Model Service returned Oss Model Identity: {} for Product Number: {} and Product Revision: {} for Node {}",
                ossModelIdentity, upgradePackageProductDetails.getProductNumber(), upgradePackageProductDetails.getProductRevision(), nodeInfo.getName());

            if (isBlank(ossModelIdentity)) {
                recordNodeValidationError(context, VALIDATION_NODEIDENTIFIER_NOT_DETERMINED_ERROR, dirName, nodeInfo.getName());
                return false;
            } else {
                updatedNodeIdentifier = ossModelIdentity;
                return true;
            }
        }
        return true;
    }

    private boolean isNodeIdentifierSupported(final String nodeType, final String nodeIdentifier) {
        if (isBlank(nodeIdentifier)) {
            return modelReader.checkOssModelIdentityExists(nodeType, updatedNodeIdentifier);
        } else {
            return modelReader.checkOssModelIdentityExists(nodeType, nodeIdentifier);
        }
    }
}
