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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact;

/**
 * Helper class to identify the node schema of a reconfiguration node.
 */
public class NodeSchemaProcessor {
    private static final String NAMESPACE_LOCATION = "xsi:noNamespaceSchemaLocation";
    private static final String RECONFIGURE_NODE_INFO_XSD = "ReconfigureNodeInfo.xsd";

    @Inject
    private Logger logger;

    /**
     * Checks the nodeInfo of first node in a project.zip
     * @param archive
     *            <code>Archive</code> containing the project contents.
     * @return true
     *            if nodeInfo contains ReconfigureNodeInfo.xsd schema.
     */
    public boolean isNodeReconfiguration(final Archive archive) {
        try {
            final String nodeInfoContent = getContentAsString(archive, ProjectArtifact.NODEINFO.toString());
            if (nodeInfoContent == null) {
                return false;
            }
            final DocumentReader nodeInfoDoc = new DocumentReader(nodeInfoContent);
            final String attributeValue = nodeInfoDoc.getValueOfSpecifiedAttributeInRootElement(NAMESPACE_LOCATION);

            return RECONFIGURE_NODE_INFO_XSD.equals(attributeValue);
        } catch (final Exception e) {
            logger.warn("Ignore this exception while identifying reconfiguration node", e);
            return false;
        }
    }

    private static String getContentAsString(final Archive archive, final String artifactName) {
        final List<String> allDirectoryNames = archive.getAllDirectoryNames();
        if (allDirectoryNames.isEmpty()) {
            return null;
        } else {
            return archive.getArtifactOfNameInDir(allDirectoryNames.get(0), artifactName).getContentsAsString();
        }
    }

    /**
     * Checks the noNamespaceSchemaLocation value of nodeInfo.xml
     * @param archive
     *            <code>Archive</code> containing the project contents.
     * @param directory
     *            The directory of the .zip project
     * @return attribute value
     *            if nodeInfo contains ReconfigureNodeInfo.xsd schema.
     */
    public String getNoNamespaceSchemaLocation(final Archive archive, final String directory){
        final ArchiveArtifact archiveArtifact = archive.getArtifactOfNameInDir(directory, ProjectArtifact.NODEINFO.toString());
        return getNameSpaceAttribute(archiveArtifact);
    }

    /**
     * Checks if the nodeInfo.xml file exists
     * @param archive Name of the node Info file
     * @param directory Directory in which the node info file must be
     * @return true if the file exists in that directory otherwise will return false.
     */
    public boolean existsNodeInfoFile(final Archive archive, final String directory) {
        return archive.getArtifactOfNameInDir(directory, ProjectArtifact.NODEINFO.toString()) != null;
    }

    private String getNameSpaceAttribute (final ArchiveArtifact archiveArtifact){
        final String nodeInfoContent = archiveArtifact.getContentsAsString();
        final DocumentReader nodeInfoFile = new DocumentReader(nodeInfoContent);
        return nodeInfoFile.getValueOfSpecifiedAttributeInRootElement(NAMESPACE_LOCATION);
    }
}
