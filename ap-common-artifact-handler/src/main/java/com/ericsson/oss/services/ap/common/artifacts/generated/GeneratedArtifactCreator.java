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

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.itpf.smrs.SmrsAccount;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.SmrsAccountOperations;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Handles create of generated artifacts for a single artifact type.
 */
class GeneratedArtifactCreator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DpsOperations dps;

    @Inject
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private SmrsAccountOperations smrsAccountOperations;

    @Inject
    private CryptographyService cyptographyService;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    public void createArtifactInGeneratedDir(final ArtifactDetails artifact) {
        final String apNodeFdn = artifact.getApNodeFdn();
        String generatedLocation = artifact.getLocation();

        if (generatedLocation == null) {
            final String generatedNodeDir = getGeneratedDirForNode(apNodeFdn);
            generatedLocation = generatedNodeDir + File.separator + artifact.getNameWithExtension();
        }

        createArtifact(artifact, generatedLocation);
    }

    public void createEoiArtifactInArtifactDir(final ArtifactDetails artifact) {
        final String apNodeFdn = artifact.getApNodeFdn();
        final String generatedNodeDir = getEoiDirForNode(apNodeFdn);
        final String location = generatedNodeDir + File.separator + artifact.getNameWithExtension();
        createEoiArtifact(artifact, location);
    }

    public void createEoiArtifact(final ArtifactDetails artifact, final String location) {
        logger.debug("Creating generated file {}", location);
        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), artifact.getType());
        createParameters.put(NodeArtifactAttribute.NAME.toString(), artifact.getName());
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), location);
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), artifact.isExportable());
        createParameters.put(NodeArtifactAttribute.ENCRYPTED.toString(), artifact.isEncrypted());
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), artifact.getConfigurationNodeName());
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.IN_PROGRESS.name());
        createParameters.put(NodeArtifactAttribute.FILE_FORMAT.toString(), artifact.getFileFormat().name());
        nodeArtifactMoOperations.createNodeArtifactMo(artifact.getApNodeFdn(), createParameters);

        final byte[] fileContents = getFileContents(artifact);
        artifactResourceOperations.writeArtifact(location, fileContents);
    }



    public void createArtifactInSmrsDir(final ArtifactDetails artifact, final String smrsNodeType) {
        final String smrsNodeDir = getSmrsDirForNode(artifact.getApNodeFdn(), smrsNodeType);
        final String generatedLocation = smrsNodeDir + artifact.getNameWithExtension();
        createArtifact(artifact, generatedLocation);
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
    public void copyRawArtifactToSmrsDir(final String apNodeFdn, final String artifactName, final String artifactType) {
        final String rawArtifactContents = readRawArtifactContentsOfType(apNodeFdn, artifactType);
        final ArtifactDetails generatedSiteInstallArtifact = new ArtifactDetails.ArtifactBuilder()
                .apNodeFdn(apNodeFdn)
                .exportable(false)
                .artifactContent(rawArtifactContents)
                .type(artifactType)
                .name(artifactName)
                .build();

        createArtifactInSmrsDir(generatedSiteInstallArtifact, getNodeType(apNodeFdn));
    }

    private String readRawArtifactContentsOfType(final String apNodeFdn, final String artifactType) {
        final ArtifactDetails artifactDetails = rawArtifactHandler.readFirstOfType(apNodeFdn, artifactType);
        return artifactDetails.getArtifactContent();
    }

    private String getNodeType(final String apNodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(apNodeFdn);
        return nodeMo.getAttribute(NODE_TYPE.toString()).toString();
    }

    private void createArtifact(final ArtifactDetails artifact, final String generatedLocation) {
        logger.debug("Creating generated file {}", generatedLocation);

        final ManagedObject existingNodeArtifact = findRawArtifactOfSameType(artifact.getApNodeFdn(), artifact.getType());
        if (existingNodeArtifact == null) {
            createGeneratedArtifactMo(artifact, generatedLocation);
        } else {
            existingNodeArtifact.setAttribute(NodeArtifactAttribute.EXPORTABLE.toString(), artifact.isExportable());
            existingNodeArtifact.setAttribute(NodeArtifactAttribute.ENCRYPTED.toString(), artifact.isEncrypted());
            existingNodeArtifact.setAttribute(NodeArtifactAttribute.GEN_LOCATION.toString(), generatedLocation);
        }

        final byte[] fileContents = getFileContents(artifact);
        artifactResourceOperations.writeArtifact(generatedLocation, fileContents);
    }

    private byte[] getFileContents(final ArtifactDetails artifact) {
        final byte[] fileContents = artifact.getArtifactContentAsBytes();
        if (artifact.isEncrypted()) {
            return cyptographyService.encrypt(fileContents);
        }
        return fileContents;
    }

    private void createGeneratedArtifactMo(final ArtifactDetails artifact, final String generatedLocation) {
        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), artifact.getType());
        createParameters.put(NodeArtifactAttribute.NAME.toString(), artifact.getName());
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), generatedLocation);
        createParameters.put(NodeArtifactAttribute.RAW_LOCATION.toString(), null);
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), artifact.isExportable());
        createParameters.put(NodeArtifactAttribute.ENCRYPTED.toString(), artifact.isEncrypted());
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.NOT_STARTED.name());
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), artifact.getConfigurationNodeName());
        createParameters.put(NodeArtifactAttribute.FILE_FORMAT.toString(), artifact.getFileFormat().name());

        nodeArtifactMoOperations.createNodeArtifactMo(artifact.getApNodeFdn(), createParameters);
    }

    private static String getGeneratedDirForNode(final String apNodeFdn) {
        final String projectFdn = FDN.get(apNodeFdn).getParent();
        final String projectName = FDN.get(projectFdn).getRdnValue();
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();

        return new StringBuilder()
                .append(DirectoryConfiguration.getGeneratedDirectory())
                .append(File.separator)
                .append(projectName)
                .append(File.separator)
                .append(nodeName)
                .toString();
    }

    private static String getEoiDirForNode(final String apNodeFdn) {
        final String projectFdn = FDN.get(apNodeFdn).getParent();
        final String projectName = FDN.get(projectFdn).getRdnValue();
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();

        return new StringBuilder()
            .append(DirectoryConfiguration.getArtifactsDirectory())
            .append(File.separator)
            .append(projectName)
            .append(File.separator)
            .append(nodeName)
            .toString();
    }


    private String getSmrsDirForNode(final String apNodeFdn, final String nodeType) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final SmrsAccount smrsAccount = smrsAccountOperations.getSmrsAccount(nodeName, nodeType);
        return smrsAccount.getHomeDirectory();
    }

    private ManagedObject findRawArtifactOfSameType(final String apNodeFdn, final String artifactType) {
        final Collection<ManagedObject> nodeArtifactMos = nodeArtifactMoOperations.getNodeArtifactMosOfType(apNodeFdn, artifactType);
        return nodeArtifactMos.size() == 1 ? nodeArtifactMos.iterator().next() : null;
    }
}
