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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.api.download.DownloadArtifactService;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.api.exception.IllegalDownloadArtifactException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Downloads exportable raw and generated artifacts for a node.
 */
@UseCase(name = UseCaseName.DOWNLOAD_ARTIFACT)
public class DownloadArtifactUseCase {

    private ResourceService resourceService;

    @Inject
    private DpsOperations dps;

    @Inject
    private GeneratedArtifactHandler generatedArtifactHandler;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private RawArtifactHandler rawArtifactHandler;

    private ServiceFinderBean serviceFinder = new ServiceFinderBean();  //NOPMD

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Downloads all exportable raw or generated artifacts for the specified node. The generated file to be downloaded in stored in the download
     * staging directory from which it can be downloaded.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @param artifactBaseType
     *            state of artifacts, raw or generated
     * @return the ID which identifies the file in the download staging directory
     * @throws ApApplicationException
     *             if there is an error downloading the artifacts
     * @throws UnsupportedCommandException
     *             if download of an generated artifact file that is not supported is attempted
     */
    public String execute(final String nodeFdn, final ArtifactBaseType artifactBaseType) {
        verifyDownloadOrderedArtifactSupported(getNodeType(nodeFdn), artifactBaseType);
        verifyDownloadArtifactPermitted(nodeFdn);
        verifyDownloadDirAccess();

        try {
            final Collection<ArtifactDetails> nodeArtifacts = getArtifactsToBeDownloaded(nodeFdn, artifactBaseType);
            checkArtifactExistence(nodeArtifacts);
            final String uniqueFileId = generateUniqueFileId(nodeFdn, nodeArtifacts, artifactBaseType);
            saveArtifactsToDownloadDir(nodeArtifacts, uniqueFileId);

            return uniqueFileId;
        } catch (final ApApplicationException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new ApApplicationException(String.format("Error downloading %s artifact(s) for node %s", artifactBaseType.toString(), nodeFdn),
                    exception);
        }
    }

    private String getNodeType(final String nodeFdn) {
        final ManagedObject nodeMo = getNodeMo(nodeFdn);
        return nodeMo.getAttribute(NODE_TYPE.toString());
    }

    private ManagedObject getNodeMo(final String nodeFdn) {
        return dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
    }

    private void verifyDownloadOrderedArtifactSupported(final String nodeType, final ArtifactBaseType artifactBaseType) {
        if (!isRawArtifact(artifactBaseType) && !isOrderedArtifactSupported(nodeType)) {
            throw new UnsupportedCommandException("There are no generated artifacts for node type " + nodeType);
        }
    }

    private boolean isOrderedArtifactSupported(final String nodeType) {
        final String internalEJBQualifier = nodeTypeMapper.getInternalEjbQualifier(nodeType);
        final DownloadArtifactService downloadArtifactService = serviceFinder.find(DownloadArtifactService.class, internalEJBQualifier);
        return downloadArtifactService.isOrderedArtifactSupported();
    }

    private void verifyDownloadArtifactPermitted(final String nodeFdn) {
        final String state = getNodeState(nodeFdn);
        if (IntegrationPhase.SUCCESSFUL == IntegrationPhase.getIntegrationPhase(state)) {
            throw new IllegalDownloadArtifactException("Download artifact is not permitted");
        }
    }

    private String getNodeState(final String nodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final ManagedObject nodeStatusMo = nodeMo.getChild(MoType.NODE_STATUS.toString() + "=1");
        return nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

    private void verifyDownloadDirAccess() {
        if (!resourceService.supportsWriteOperations(DirectoryConfiguration.getDownloadDirectory())) {
            throw new ApApplicationException("Unable to access system resource " + DirectoryConfiguration.getDownloadDirectory());
        }
    }

    private Collection<ArtifactDetails> getArtifactsToBeDownloaded(final String nodeFdn, final ArtifactBaseType artifactBaseType) {
        if (isRawArtifact(artifactBaseType)) {
            return rawArtifactHandler.readAllForNode(nodeFdn);
        } else {
            return getExportableGeneratedArtifacts(generatedArtifactHandler.readAllForNode(nodeFdn));
        }
    }

    private static List<ArtifactDetails> getExportableGeneratedArtifacts(final Collection<ArtifactDetails> generatedArtifacts) {
        final List<ArtifactDetails> exportableGeneratedArtifacts = new ArrayList<>(generatedArtifacts.size());
        for (final ArtifactDetails artifactDetails : generatedArtifacts) {
            if (artifactDetails.isExportable()) {
                exportableGeneratedArtifacts.add(artifactDetails);
            }
        }

        return exportableGeneratedArtifacts;
    }

    private static void checkArtifactExistence(final Collection<ArtifactDetails> nodeArtifacts) {
        if (nodeArtifacts.isEmpty()) {
            throw new ArtifactNotFoundException("No artifact found to download");
        }
    }

    private static String generateUniqueFileId(final String nodeFdn, final Collection<ArtifactDetails> nodeArtifacts,
                                               final ArtifactBaseType artifactBaseType) {
        final long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
        return currentTimeInMillis + "_" + getFilePostfixWithExtention(nodeFdn, nodeArtifacts, artifactBaseType);
    }

    private static String getFilePostfixWithExtention(final String nodeFdn, final Collection<ArtifactDetails> nodeArtifacts,
                                                      final ArtifactBaseType artifactBaseType) {
        final String nodeName = FDN.get(nodeFdn).getRdnValue();

        if (nodeArtifacts.size() == 1) {
            return getFilePostfixForSingleFile(nodeArtifacts.iterator().next(), nodeName);
        }
        return getFilePostFixForMultipleFiles(artifactBaseType, nodeName);
    }

    private static String getFilePostfixForSingleFile(final ArtifactDetails artifactDetails, final String nodeName) {
        final String singleFileFormat = nodeName + "_%s_" + createTimeStamp() + ".%s";
        return String.format(singleFileFormat, artifactDetails.getName(), artifactDetails.getExtension());
    }

    private static String getFilePostFixForMultipleFiles(final ArtifactBaseType artifactBaseType, final String nodeName) {
        final String multipleFileFormat = nodeName + "_%s%s_" + createTimeStamp() + ".%s";
        return String.format(multipleFileFormat, artifactBaseType.toString().toLowerCase(Locale.US), "Artifacts", "zip");
    }

    private static String createTimeStamp() {
        final SimpleDateFormat timeStampFormat = new SimpleDateFormat("yyyyMMddHHmm");
        return timeStampFormat.format(new Date());
    }

    private void saveArtifactsToDownloadDir(final Collection<ArtifactDetails> nodeArtifacts, final String uniqueFileId) {
        if (uniqueFileId.endsWith(".zip")) {
            writeArtifactsToZipFile(nodeArtifacts, uniqueFileId);
        } else {
            writeArtifactToFile(nodeArtifacts.iterator().next(), uniqueFileId);
        }
    }

    private void writeArtifactsToZipFile(final Collection<ArtifactDetails> nodeArtifacts, final String uniqueFileId) {
        final Map<String, byte[]> zipFileContents = new HashMap<>();
        for (final ArtifactDetails artifactDetails : nodeArtifacts) {
            if (artifactDetails.isNotEmptyContent()) {
                zipFileContents.put(artifactDetails.getName() + "." + artifactDetails.getExtension(), artifactDetails.getArtifactContentAsBytes());
            }
        }

        final String fileUri = DirectoryConfiguration.getDownloadDirectory() + File.separator + uniqueFileId;
        resourceService.writeContentsToZip(fileUri, zipFileContents);
    }

    private void writeArtifactToFile(final ArtifactDetails artifactDetails, final String uniqueFileId) {
        if (artifactDetails.isNotEmptyContent()) {
            final String fileUri = DirectoryConfiguration.getDownloadDirectory() + File.separator + uniqueFileId;
            resourceService.write(fileUri, artifactDetails.getArtifactContentAsBytes(), false);
        }
    }

    private static boolean isRawArtifact(final ArtifactBaseType artifactBaseType) {
        return ArtifactBaseType.RAW == artifactBaseType;
    }
}
