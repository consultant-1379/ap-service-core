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

import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Handles create, deletion and reading of generated artifacts located in the configured AP generated directory on the SFS.
 */
public class GeneratedArtifactHandler {

    @Inject
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Inject
    private GeneratedArtifactCreator generatedArtifactCreator;

    @Inject
    private GeneratedNodeArtifactsDeleter generatedNodeArtifactsDeleter;

    @Inject
    private GeneratedNodeArtifactsUpdater generatedNodeArtifactsUpdater;

    /**
     * Read all generated artifacts for the given node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return Collection of {@link ArtifactDetails} or empty collection if no artifacts exists
     */
    public Collection<ArtifactDetails> readAllForNode(final String apNodeFdn) {
        final Collection<ArtifactDetails> artifactDetails = new ArrayList<>();
        final Collection<ManagedObject> artifactMos = nodeArtifactMoOperations.getNodeArtifactMos(apNodeFdn);

        for (final ManagedObject artifactMo : artifactMos) {
            final String artifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.GEN_LOCATION.toString());
            if (!StringUtils.isEmpty(artifactLocation)) {
                final ArtifactDetails artifact = nodeArtifactMoOperations.createGeneratedArtifactDetails(apNodeFdn, artifactLocation, artifactMo);
                artifactDetails.add(artifact);
            }
        }
        return artifactDetails;
    }

    /**
     * Read node artifact information for all generated artifacts of the specified type.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the artifact type requested to be read
     * @return Collection of {@link ArtifactDetails} (no item will be a null object) or empty collection if no artifact of the type exists
     */
    public Collection<ArtifactDetails> readAllOfType(final String apNodeFdn, final String artifactType) {
        final Collection<ArtifactDetails> artifactDetailsOfType = new ArrayList<>();
        final Collection<ManagedObject> artifactMosOfType = nodeArtifactMoOperations.getNodeArtifactMosOfType(apNodeFdn, artifactType);

        for (final ManagedObject artifactMo : artifactMosOfType) {
            final String artifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.GEN_LOCATION.toString());
            if (StringUtils.isNotEmpty(artifactLocation)) {
                final ArtifactDetails artifact = nodeArtifactMoOperations.createGeneratedArtifactDetails(apNodeFdn, artifactLocation, artifactMo);
                artifactDetailsOfType.add(artifact);
            }
        }
        return artifactDetailsOfType;
    }

    /**
     * Read node artifact information for all generated artifacts of the specified type, then retrieves the first one.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the artifact type requested to be read
     * @return {@link ArtifactDetails} for the first artifact of the specified type
     * @throws ArtifactNotFoundException
     *             thrown if no generated artifact of the specified type exists
     */
    public ArtifactDetails readFirstOfType(final String apNodeFdn, final String artifactType) {
        final Collection<ArtifactDetails> allArtifactDetails = readAllOfType(apNodeFdn, artifactType);

        if (allArtifactDetails.isEmpty()) {
            throw new ArtifactNotFoundException(String.format("%s artifact not found for node %s", artifactType, apNodeFdn));
        }

        return allArtifactDetails.iterator().next();
    }

    /**
     * Delete all generated node artifacts of the specified type from the system.
     * <p>
     * On successful execution all artifacts of the given type will be deleted from the system. If it is the last artifact in the directory then the
     * parent node directory will also be deleted.
     * <p>
     * In case of an artifact on smrs, then the smrs account will be deleted if it is the last remaining artifact.
     * <p>
     * If there is no associated raw artifact then the <code>NodeArtifact</code> mo will be deleted, otherwise the generatedLocation attribute for
     * each <code>NodeArtifact</code> mo will be set to null.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the artifact type requested to be deleted.
     */
    public void deleteAllOfType(final String apNodeFdn, final String artifactType) {
        generatedNodeArtifactsDeleter.deleteAllOfType(apNodeFdn, artifactType);
    }

    /**
     * Deletes all generated artifacts for the specified node and updates the <code>NodeArtifact</code> managed objects to reflect the change in the
     * generate file location.
     * <p>
     * All artifacts which are located in the configured generated directories on the SFS and in the SMRS AI node directories will be deleted.
     * <p>
     * The SMRS node account will be deleted.
     * <p>
     * If there is no associated raw artifact then the <code>NodeArtifact</code> mo will be deleted, otherwise the generatedLocation attribute for
     * each <code>NodeArtifact</code> mo will be set to null.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     */
    public void deleteAllForNode(final String nodeFdn) {
        generatedNodeArtifactsDeleter.deleteAllGeneratedArtifactsForNode(nodeFdn, true);
    }

    /**
     * Deletes all generated artifacts for the specified node. The <code>NodeArtifact</code> managed objects are not updated to reflect the change in
     * generated file location. As such it is expected that this method would be called in conjunction with deleting a node from the system.
     * <p>
     * All artifacts which are located in the configured generated directories on the SFS and in the SMRS AI node directories will be deleted.
     * <p>
     * The SMRS node account will be deleted.
     * <p>
     * The generatedLocation attribute for each <code>NodeArtifact</code> mo will be unchanged.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     */
    public void deleteAllForNodeWithNoModelUpdate(final String nodeFdn) {
        generatedNodeArtifactsDeleter.deleteAllGeneratedArtifactsForNode(nodeFdn, false);
    }

    /**
     * Deletes all generated artifacts for the specified project. The <code>NodeArtifact</code> managed objects are not updated to reflect the change
     * in generated file location. As such call this method in conjunction with deleting a project from the system.
     * <p>
     * For each node in the project all artifacts which are located in the configured generated directories on the SFS and in the SMRS AI node
     * directories will be deleted.
     * <p>
     * For each node the SMRS node account will be deleted.
     * <p>
     * The generatedLocation attribute for each <code>NodeArtifact</code> mo will be unchanged.
     *
     * @param projectFdn
     *            the FDN of the AP project
     */
    public void deleteAllForProjectWithNoModelUpdate(final String projectFdn) {
        generatedNodeArtifactsDeleter.deleteAllGeneratedArtifactsForProject(projectFdn);
    }

    /**
     * Update generated node artifact of the specified type.
     * <p>
     * On successful execution the artifact will be updated. The <code>generatedLocation</code> attribute will be updated on the existing
     * <code>NodeArtifact</code> MO of same artifactType.
     *
     * @param artifact
     *            the artifact to be created
     */
    public void updateArtifact(final ArtifactDetails artifact) {
        final String apNodeFdn = artifact.getApNodeFdn();
        final String previousFileLocation = retrieveGeneratedLocation(apNodeFdn, artifact.getType());
        generatedNodeArtifactsUpdater.updateArtifactInGeneratedDirectory(previousFileLocation, artifact);

    }

    /**
     * Create generated node artifact of the specified type.
     * <p>
     * On successful execution the artifact will be created. If the location attribute is set for <code>Artifact</code> then the file will be created
     * in the specified directory, otherwise the file will be created in the configured generated directory. If a <code>NodeArtifact</code> managed
     * object of same type exists then the generated artifact name then <code>generatedLocation</code> will be updated on this mo. If no matching
     * <code>NodeArtifact</code> mo found then a new mo will be created with <code>generatedLocation</code> set.
     *
     * @param artifact
     *            the artifact to be created
     */
    public void create(final ArtifactDetails artifact) {
        generatedArtifactCreator.createArtifactInGeneratedDir(artifact);
    }


    public void eoiCreate(final ArtifactDetails artifact) {
        generatedArtifactCreator.createEoiArtifactInArtifactDir(artifact);
    }


    /**
     * Create generated node artifact of the specified type on smrs.
     * <p>
     * On successful execution the artifact will be created in the SMRS AI directory for the node. If a <code>NodeArtifact</code> managed object of
     * same type exists with raw artifact name matching the generated artifact name then <code>generatedLocation</code> will be updated on this mo. If
     * no matching <code>NodeArtifact</code> mo found then a new mo will be created with <code>generatedLocation</code> set.
     *
     * @param artifact
     *            the artifact to be created
     * @param smrsNodeType
     *            the node type used for resolution of the node specific SMRS account
     */
    public void createOnSmrs(final ArtifactDetails artifact, final String smrsNodeType) {
        generatedArtifactCreator.createArtifactInSmrsDir(artifact, smrsNodeType);
    }

    /**
     * Copies raw artifact of the specified type to the SMRS node directory.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactName
     *            the name of the artifact file
     * @param artifactType
     *            the type of artifact
     */
    public void copyRawToSmrs(final String apNodeFdn, final String artifactName, final String artifactType) {
        generatedArtifactCreator.copyRawArtifactToSmrsDir(apNodeFdn, artifactName, artifactType);
    }

    /**
     * Get the full path of the Netconf preconfiguration file for Node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     */
    public static String getNetconfPreconfigurationFileFullpathForNode(final String apNodeFdn) {
        final String projectFdn = FDN.get(apNodeFdn).getParent();
        final String projectName = FDN.get(projectFdn).getRdnValue();
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();

        return String.format("%s/%s/%s/preconfiguration_%s.xml", DirectoryConfiguration.getGeneratedDirectory(),
                projectName, nodeName, nodeName);
    }

    private String retrieveGeneratedLocation(final String apNodeFdn, final String artifactType) {
        final Collection<ArtifactDetails> nodeArtifactsOfType = readAllOfType(apNodeFdn, artifactType);
        if (!nodeArtifactsOfType.isEmpty()) {
            final ArtifactDetails nodeArtifact = nodeArtifactsOfType.iterator().next();
            return nodeArtifact.getLocation();
        }
        return null;
    }
}
