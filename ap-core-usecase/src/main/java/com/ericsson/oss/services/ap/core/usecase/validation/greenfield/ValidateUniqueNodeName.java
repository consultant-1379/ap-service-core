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

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NAME;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Validate node name specified per node is unique in the project.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 10)
@Rule(name = "ValidateUniqueNodeName")
public class ValidateUniqueNodeName extends ZipBasedValidation {
    private static final String NODE_INFO_SCHEMA_TYPE = "NodeInfo";
    private static final String VALIDATION_NODEINFO_FORMAT_ERROR = "validation.artifact.format.failure";
    private static final String INVALID_NODEINFO_FORMAT_ERROR = "validation.nodeinfo.missing";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Collection<String> nodeNames = new HashSet<>();
        final Archive projectArchive = getArchive(context);
        final List<String> allDirecoryNames = projectArchive.getAllDirectoryNames();

        for (final String dirName : allDirecoryNames) {
            if(projectArchive.getArtifactOfNameInDir(dirName, NODEINFO.artifactName()) != null){
                validateUniqueNodeName(context, nodeNames, projectArchive, dirName);
            }else{
                logger.warn("Error validating nodeInfo.xml file name");
                recordNodeValidationError(context, INVALID_NODEINFO_FORMAT_ERROR, dirName, NODEINFO.toString());
            }
        }

        return context.getValidationErrors().isEmpty();
    }

    private void validateUniqueNodeName(final ValidationContext context, final Collection<String> nodeNames, final Archive projectArchive, final String dirName) {
        try {
            final ArchiveArtifact nodeArtifact = projectArchive.getArtifactOfNameInDir(dirName, NODEINFO.artifactName());
            final DocumentReader nodeInfoDocumentReader = new DocumentReader(nodeArtifact.getContentsAsString());
            final String nodeName = nodeInfoDocumentReader.getElementValue(NAME.toString());
            processName(context, nodeNames, dirName, nodeName);
        } catch (final XmlException e) {
            logger.warn("Error validating {} schema: {}", NODE_INFO_SCHEMA_TYPE, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_NODEINFO_FORMAT_ERROR, dirName, NODEINFO.toString());
        }
    }

    private void processName(final ValidationContext context, final Collection<String> nodeNames, final String dirName, final String nodeName) {
        if (nodeName == null) {
            recordNodeValidationError(context, "validation.xml.parse.error", dirName, NAME.toString());
        } else if (!nodeNames.add(nodeName)) {
            recordNodeValidationError(context, "validation.duplicate.node.name", dirName, nodeName);
        } else {
            nodeNames.add(nodeName);
        }
    }
}
