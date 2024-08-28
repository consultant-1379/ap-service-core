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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ArtifactFileNotFoundException;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Creates the <code>NodeArtifact</code> MOs and writes the node artifacts to the file system using
 * {@link RawArtifactHandler#createForProject(String, List)}.
 */
public class NodeArtifactMosCreator {

    @Inject
    private NodeInfoReader nodeInfoReader;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    @Inject
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    private static final Logger logger = LoggerFactory.getLogger(NodeArtifactMosCreator.class);

    private static final String REPLACE_REGEX = "\\<\\?xml(.+?)\\?\\>";
    private static final String NETCONF_KEY_CHARACTOR = "<rpc";
    private static final String BULK_3GPP_KEY_CHARACTOR = "<bulkCmConfigDataFile";
    private static final String BASELINE_ARTIFACT_TAG = "baseline";

    /**
     * Creates all the node artifacts and the MOs in AP model.
     *
     * @param projectFdn
     *            the project FDN
     * @param projectArchive
     *            the project archive
     */
    public void createArtifactsAndMos(final String projectFdn, final Archive projectArchive) {
        final List<ArtifactDetails> rawArtifacts = getAllRawArtifacts(projectFdn, projectArchive);
        final String projectName = FDN.get(projectFdn).getRdnValue();
        rawArtifactHandler.createForProject(projectName, rawArtifacts);
    }

    /**
     * Deletes and Creates all the node artifacts and the MOs in AP model.
     *
     * @param projectFdn
     *            the project FDN
     * @param projectArchive
     *            the project archive
     */
    public void refreshArtifactsAndMos(final String projectFdn, final Archive projectArchive) {
        for (final String directory : projectArchive.getAllDirectoryNames()) {
            final NodeInfo nodeData = nodeInfoReader.read(projectArchive, directory);
            final String nodeFdn = projectFdn + "," + MoType.NODE.toString() + "=" + nodeData.getName();
            rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(nodeFdn);
        }

        final List<ArtifactDetails> rawArtifacts = getAllRawArtifacts(projectFdn, projectArchive);
        final String projectName = FDN.get(projectFdn).getRdnValue();
        rawArtifactHandler.createForProject(projectName, rawArtifacts);
    }

    /**
     * Update the suspend attribute in node artifacts container MO.
     *
     * @param nodeFdn
     *            the node FDN
     * @param nodeConfigurationAttributeMap
     *            the configuration attributes
     */
    public void refreshNodeArtifactContainerMo(final String nodeFdn, final Map<String, Object> nodeConfigurationAttributeMap) {
        final String nodeArtifactContainerFdn = nodeFdn + "," + MoType.NODE_ARTIFACT_CONTAINER.toString() + "=1";
        nodeArtifactMoOperations.refreshNodeArtifactContainerMo(nodeArtifactContainerFdn, nodeConfigurationAttributeMap);
    }

    private List<ArtifactDetails> getAllRawArtifacts(final String projectFdn, final Archive projectArchive) {
        final List<ArtifactDetails> rawArtifacts = new ArrayList<>();

        for (final String dirName : projectArchive.getAllDirectoryNames()) {
            final NodeInfo nodeInfo = nodeInfoReader.read(projectArchive, dirName);
            final String nodeName = nodeInfo.getName();
            final String nodeFdn = generateNodeFdn(projectFdn, nodeInfo);
            final Map<String, String> remoteNodeNames = nodeInfo.getRemoteNodeNames();
            final Map<String, String> ignoreErrors = nodeInfo.getIgnoreErrors();
            final List<ArtifactDetails> nodeRawArtifacts = generateNodeArtifactDetails(nodeFdn, nodeInfo.getArtifactDetailsInStrictSequence(), dirName, projectArchive, nodeName,
                remoteNodeNames, ignoreErrors);
            rawArtifacts.addAll(nodeRawArtifacts);
        }
        return rawArtifacts;
    }

    private static List<ArtifactDetails> generateNodeArtifactDetails(final String nodeFdn, final List<ArtifactDetails> artifactDetails,
        final String dirName, final Archive projectArchive,
        final String nodeName, final Map<String, String> remoteNodeNames, final Map<String, String> ignoreErrors) {
        final List<ArtifactDetails> nodeRawArtifacts = new ArrayList<>();

        for (final ArtifactDetails artifact: artifactDetails) {
            final String artifactFilename = artifact.getNameWithExtension();
            final String fileType = artifact.getType();
            final ArchiveArtifact archive = projectArchive.getArtifactOfNameInDir(dirName, artifactFilename);
            String configurationNodename = remoteNodeNames.get(artifactFilename);
            final boolean ignoreError = Boolean.parseBoolean(ignoreErrors.get(artifactFilename));
            if (configurationNodename == null) {
                configurationNodename = nodeName;
            }

            byte[] artifactContentBytes = null;
            if (archive != null) {
                artifactContentBytes = artifactFilename.endsWith("zip") ? archive.getContentsAsBytes()
                    : archive.getContentsAsString().getBytes(StandardCharsets.UTF_8);
            }

            final ArtifactFileFormat fileFormat = parseArtifactFile(fileType, artifactContentBytes,
                    artifactFilename, nodeFdn);
            final ArtifactBuilder artifactBuilder = new ArtifactDetails.ArtifactBuilder()
            .apNodeFdn(nodeFdn)
            .name(artifactFilename)
            .type(fileType)
            .configurationNodeName(configurationNodename)
            .fileFormat(fileFormat)
            .artifactContent(artifactContentBytes)
            .ignoreError(ignoreError);

            nodeRawArtifacts.add(artifactBuilder.build());
        }
        return nodeRawArtifacts;
    }

    private static ArtifactFileFormat parseArtifactFileFormat(final String type, final byte[] artifactContents) {
        if (BASELINE_ARTIFACT_TAG.equals(type)) {
            return ArtifactFileFormat.AMOS_SCRIPT;
        }

        final String artifactContentsInString = new String(artifactContents, StandardCharsets.UTF_8).replaceAll(REPLACE_REGEX, "").trim();
        if (artifactContentsInString.contains(BULK_3GPP_KEY_CHARACTOR)) {
            return ArtifactFileFormat.BULK_3GPP;
        }
        if (artifactContentsInString.contains(NETCONF_KEY_CHARACTOR)) {
            return ArtifactFileFormat.NETCONF;
        }
        return ArtifactFileFormat.UNKNOWN;
    }

    private static String generateNodeFdn(final String projectFdn, final NodeInfo nodeInfo) {
        final String nodeName = nodeInfo.getName();
        return String.format("%s,%s=%s", projectFdn, MoType.NODE.toString(), nodeName);
    }

    private static ArtifactFileFormat parseArtifactFile(final String type, final byte[] artifactContentBytes,
            final String artifactFilename, final String nodeFdn) {
        final ArtifactFileFormat fileFormat;

        try {
            fileFormat = parseArtifactFileFormat(type, artifactContentBytes);
        } catch (final Exception e) {
            logger.trace("Error when parsing artifact file format: {}", e.getMessage());
            throw new ArtifactFileNotFoundException(String.format("No node artifact file found with name: %s for %s", artifactFilename,
                nodeFdn));
        }

        return fileFormat;
    }
}
