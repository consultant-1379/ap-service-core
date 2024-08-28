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
package com.ericsson.oss.services.ap.common.artifacts.generated;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.SmrsAccountOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Handles deletion of generated artifacts for a node or project.
 */
class GeneratedNodeArtifactsDeleter {

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Inject
    private SmrsAccountOperations smrsAccountOperations;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    private DataPersistenceService dps;

    @PostConstruct
    public void init() {
        dps = new ServiceFinderBean().find(DataPersistenceService.class);
    }

    /**
     * Deletes all artifacts of the supplied type for the specified AP node.
     *
     * @param apNodeFdn
     *            the node whose artifacts are to be deleted
     * @param artifactType
     *            the type of the artifact to delete
     * @see GeneratedArtifactHandler#deleteAllOfType(String, String)
     */
    public void deleteAllOfType(final String apNodeFdn, final String artifactType) {
        final Collection<ManagedObject> artifactMosOfType = nodeArtifactMoOperations.getNodeArtifactMosOfType(apNodeFdn, artifactType);
        for (final ManagedObject artifactMo : artifactMosOfType) {
            final String artifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.GEN_LOCATION.toString());
            if (StringUtils.isEmpty(artifactLocation)) {
                continue;
            }
            if (isArtifactStoredOnSmrs(artifactLocation)) {
                deleteSmrsAccountIfLastArtifact(artifactLocation, artifactMo);
            }
            artifactResourceOperations.deleteFile(artifactLocation);
            updateOrDeleteArtifactMo(artifactMo);
        }
    }

    /**
     * Deletes all generated artifacts for the specified AP node.
     *
     * @param nodeFdn
     *            the FDN ofthe AP node
     * @param updateNodeArtifactMo
     *            set to true if <code>NodeArtifact</code> MO should be updated to reflect change in generated file location
     * @see GeneratedArtifactHandler#deleteAllForNodeWithNoModelUpdate(String)
     * @see GeneratedArtifactHandler#deleteAllForNode(String)
     */
    public void deleteAllGeneratedArtifactsForNode(final String nodeFdn, final boolean updateNodeArtifactMo) {
        final ManagedObject nodeMo = dps.getLiveBucket().findMoByFdn(nodeFdn);
        final String projectName = nodeMo.getParent().getName();

        final String generatedPathForNode = new StringBuilder()
                .append(DirectoryConfiguration.getGeneratedDirectory())
                .append(File.separator)
                .append(projectName)
                .append(File.separator)
                .append(nodeMo.getName())
                .toString();

        if (artifactResourceOperations.directoryExistAndNotEmpty(generatedPathForNode)) {
            artifactResourceOperations.deleteDirectory(generatedPathForNode);
            final String nodeType = nodeMo.getAttribute(NodeAttribute.NODE_TYPE.toString());
            smrsAccountOperations.deleteSmrsAccount(nodeMo.getName(), nodeType);

            deleteBindFile(nodeMo);
        }

        if (updateNodeArtifactMo) {
            updateOrDeleteNodeArtifactMos(nodeMo);
        }
    }

    /**
     * Deletes all generated files for all nodes within the specified AP project.
     *
     * @param projectFdn
     *            the FDN of the AP project
     * @see GeneratedArtifactHandler#deleteAllForProjectWithNoModelUpdate(String)
     */
    public void deleteAllGeneratedArtifactsForProject(final String projectFdn) {
        final String projectName = FDN.get(projectFdn).getRdnValue();
        final String generatedProjectPath = DirectoryConfiguration.getGeneratedDirectory() + File.separator + projectName;

        if (artifactResourceOperations.directoryExists(generatedProjectPath)) {
            artifactResourceOperations.deleteDirectory(generatedProjectPath);
            final Iterator<ManagedObject> nodeMos = dpsQueries.findChildMosOfTypes(projectFdn, AP.toString(), MoType.NODE.toString()).execute();

            while (nodeMos.hasNext()) {
                final ManagedObject nodeMo = nodeMos.next();
                final String nodeType = nodeMo.getAttribute(NodeAttribute.NODE_TYPE.toString());
                smrsAccountOperations.deleteSmrsAccount(nodeMo.getName(), nodeType);
                deleteBindFile(nodeMo);
            }
        }
    }

    private void updateOrDeleteArtifactMo(final ManagedObject artifactMo) {
        final String rawLocation = artifactMo.getAttribute(NodeArtifactAttribute.RAW_LOCATION.toString());
        if (rawLocation == null) {
            nodeArtifactMoOperations.deleteNodeArtifactMo(artifactMo);
        } else {
            artifactMo.setAttribute(NodeArtifactAttribute.GEN_LOCATION.toString(), null);
        }
    }

    private void updateOrDeleteNodeArtifactMos(final ManagedObject nodeMo) {
        final ManagedObject nodeArtifactContainerMo = nodeMo.getChild(MoType.NODE_ARTIFACT_CONTAINER.toString() + "=1");
        for (final ManagedObject nodeArtifactMo : nodeArtifactContainerMo.getChildren()) {
            updateOrDeleteArtifactMo(nodeArtifactMo);
        }
    }

    private void deleteBindFile(final ManagedObject nodeMo) {
        final String nodeSerialNumber = nodeMo.getAttribute(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString());
        if (nodeSerialNumber == null) {
            return;
        }

        try {
            final String bindFilePath = String.format("%s%s%s.xml", DirectoryConfiguration.getBindDirectory(), File.separator, nodeSerialNumber);
            artifactResourceOperations.deleteFile(bindFilePath);
        } catch (final ApApplicationException e) {
            logger.warn("Failed to delete file {}.xml from bind directory", nodeSerialNumber, e);
        }
    }

    private static boolean isArtifactStoredOnSmrs(final String artifactLocation) {
        return artifactLocation.contains("smrs");
    }

    private void deleteSmrsAccountIfLastArtifact(final String artifactLocation, final ManagedObject artifactMo) {
        if (artifactResourceOperations.isSingleFileInDirectory(artifactLocation)) {
            final ManagedObject nodeMo = artifactMo.getParent().getParent();
            final String nodeType = nodeMo.getAttribute(NodeAttribute.NODE_TYPE.toString());
            smrsAccountOperations.deleteSmrsAccount(nodeMo.getName(), nodeType);
        }
    }
}
