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
package com.ericsson.oss.services.ap.common.artifacts.raw;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Handles create, deletion and reading of raw artifacts located in the configured AP Raw directory on the SFS.
 */
public class RawArtifactHandler {

    @Inject
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private RawArtifactsCreator rawArtifactsCreator;

    /**
     * Read all raw artifacts for the given node.
     *
     * @param apNodeFdn
     *            the fdn of the autoprovisioning node
     * @return Collection of {@link ArtifactDetails} or empty collection if no artifacts exists
     */
    public Collection<ArtifactDetails> readAllForNode(final String apNodeFdn) {
        final Collection<ArtifactDetails> artifactDetails = new ArrayList<>();

        final Collection<ManagedObject> artifactMos = nodeArtifactMoOperations.getNodeArtifactMos(apNodeFdn);

        for (final ManagedObject artifactMo : artifactMos) {
            final String artifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.RAW_LOCATION.toString());
            if (!StringUtils.isEmpty(artifactLocation)) {
                final ArtifactDetails artifact = nodeArtifactMoOperations.createRawArtifactDetails(apNodeFdn, artifactLocation, artifactMo);
                artifactDetails.add(artifact);
            }
        }
        return artifactDetails;
    }

    /**
     * Read artifact information for all raw artifacts of the specified type.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the artifact type requested to be read
     * @return Collection of {@link ArtifactDetails} or empty collection if no artifact of the type exists
     */
    public Collection<ArtifactDetails> readAllOfType(final String apNodeFdn, final String artifactType) {
        final Collection<ArtifactDetails> artifactDetailsOfType = new ArrayList<>();

        final Collection<ManagedObject> artifactMosOfType = nodeArtifactMoOperations.getNodeArtifactMosOfType(apNodeFdn, artifactType);

        for (final ManagedObject artifactMo : artifactMosOfType) {
            final String artifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.RAW_LOCATION.toString());
            if (!StringUtils.isEmpty(artifactLocation)) {
                final ArtifactDetails artifact = nodeArtifactMoOperations.createRawArtifactDetails(apNodeFdn, artifactLocation, artifactMo);
                artifactDetailsOfType.add(artifact);
            }
        }
        return artifactDetailsOfType;
    }

    /**
     * Read node artifact information for all raw artifacts of the specified type, then retrieves the first one.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the artifact type requested to be read
     * @return {@link ArtifactDetails} for the first artifact of the specified type
     * @throws ArtifactNotFoundException
     *             thrown if no raw artifact of the specified type exists
     */
    public ArtifactDetails readFirstOfType(final String apNodeFdn, final String artifactType) {
        final Collection<ArtifactDetails> allArtifactDetails = readAllOfType(apNodeFdn, artifactType);

        if (allArtifactDetails.isEmpty()) {
            throw new ArtifactNotFoundException(String.format("%s artifact not found for node %s", artifactType, apNodeFdn));
        }

        return allArtifactDetails.iterator().next();
    }

    /**
     * Delete all raw artifacts of the specified type from the system.
     * <p>
     * On successful execution all artifacts of the given type will be deleted from the system. The associated <code>NodeArtifact</code> managed
     * objects will also be deleted.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the artifact type requested to be deleted
     */
    public void deleteAllOfType(final String apNodeFdn, final String artifactType) {
        final Collection<ManagedObject> artifactMosOfType = nodeArtifactMoOperations.getNodeArtifactMosOfType(apNodeFdn, artifactType);
        for (final ManagedObject artifactMo : artifactMosOfType) {
            final String artifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.RAW_LOCATION.toString());
            artifactResourceOperations.deleteFile(artifactLocation);
            nodeArtifactMoOperations.deleteNodeArtifactMo(artifactMo);
        }
    }

    /**
     * Deletes all raw artifacts for the specified node, along with the associated <code>NodeArtifact</code> managed objects.
     * <p>
     * All artifacts which are located in the configured raw directories on the SFS will be deleted.
     * <p>
     * The associated <code>NodeArtifact</code> mos will be deleted if there is no associated generated artifact. If case there is an associated
     * generated artifact then the rawLocation will be set to null.
     *
     * @param apNodeFdn
     *            the fdn of the AP node
     */
    public void deleteAllForNode(final String apNodeFdn) {
        deleteAllForNodeWithNoModelUpdate(apNodeFdn);

        final Collection<ManagedObject> artifactMos = nodeArtifactMoOperations.getNodeArtifactMos(apNodeFdn);

        for (final ManagedObject artifactMo : artifactMos) {
            final String generatedArtifactLocation = artifactMo.getAttribute(NodeArtifactAttribute.GEN_LOCATION.toString());
            if (generatedArtifactLocation == null) {
                nodeArtifactMoOperations.deleteNodeArtifactMo(artifactMo);
            } else {
                artifactMo.setAttribute(NodeArtifactAttribute.RAW_LOCATION.toString(), null);
            }
        }
    }

    /**
     * Deletes all raw artifacts for the specified node. The <code>NodeArtifact</code> managed objects are not updated to reflect the change in raw
     * file location. As such it is expected that this is method is called in conjunction with deleting a node from the system.
     * <p>
     * All artifacts which are located in the configured raw directories on the SFS will be deleted.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     */
    public void deleteAllForNodeWithNoModelUpdate(final String apNodeFdn) {
        final FDN nodeFdn = FDN.get(apNodeFdn);
        final String projectFdn = nodeFdn.getParent();
        final String projectName = FDN.get(projectFdn).getRdnValue();

        final String rawPathForNode = DirectoryConfiguration.getRawDirectory() + File.separator + projectName + File.separator
                + nodeFdn.getRdnValue();
        artifactResourceOperations.deleteDirectory(rawPathForNode);
    }

    /**
     * Deletes all raw artifacts for the specified project. The <code>NodeArtifact</code> managed objects are not updated to reflect the change in raw
     * file location. As such call it is expected this method is called in conjunction with deleting a project from the system.
     * <p>
     * For each node in the project all artifacts which are located in the configured raw directory on the SFS will be deleted.
     *
     * @param projectFdn
     *            the FDN of the AP project
     */
    public void deleteAllForProjectWithNoModelUpdate(final String projectFdn) {
        final String projectName = FDN.get(projectFdn).getRdnValue();
        final String rawProjectPath = DirectoryConfiguration.getRawDirectory() + File.separator + projectName;
        artifactResourceOperations.deleteDirectory(rawProjectPath);
    }

    /**
     * Create raw artifacts for the specified project.
     * <p>
     * On successful execution each of the given artifacts will be created in the configured raw directory on the SFS. A <code>NodeArtifact</code>
     * managed object will be created for each artifact, with the <code>rawLocation</code> set to the location of the created artifact. The
     * <code>NodeArtifact</code> mos are created with auto-generated rdn in the order they occur in the rawArtifacts list.
     *
     * @param projectName
     *            the name of project
     * @param rawArtifacts
     *            all artifact details of one project
     */
    public void createForProject(final String projectName, final List<ArtifactDetails> rawArtifacts) {
        rawArtifactsCreator.createProjectArtifacts(projectName, rawArtifacts);
    }
}
