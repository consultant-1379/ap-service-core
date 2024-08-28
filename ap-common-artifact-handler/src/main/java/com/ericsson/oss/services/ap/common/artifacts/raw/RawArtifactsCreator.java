/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;

/**
 * Handles creation of all raw artifacts and associated <code>NodeArtifact</code> MOs for a single AP project.
 */
class RawArtifactsCreator {

    private static final String ZIP_EXTENSION = "zip";

    @Inject
    private ArtifactResourceOperations resourceOperations;

    @Inject
    private Logger logger;

    private DataPersistenceService dps;

    @Inject
    private CryptographyService cryptographyService;

    @PostConstruct
    public void init() {
        final ServiceFinderBean serviceFinder = new ServiceFinderBean();
        dps = serviceFinder.find(DataPersistenceService.class);
    }

    /**
     * Create raw artifacts for the specified project. Stores the artifacts in the configured raw directory on the SFS. Creates a
     * <code>NodeArtifact</code> MO for any artifact with the <i>rawLocation</i> attribute set.
     *
     * @param projectName
     *            the project name
     * @param rawArtifacts
     *            all raw artifacts for each node in the project
     */
    public void createProjectArtifacts(final String projectName, final List<ArtifactDetails> rawArtifacts) {
        logger.debug("Creating raw artifacts for project {}", projectName);
        final Map<String, ManagedObject> managedObjectByFdn = new HashMap<>();
        final Map<String, byte[]> fileWithContents = new HashMap<>();
        final DataBucket liveBucket = dps.getLiveBucket();
        String siteInstallPath = "";

        for (final ArtifactDetails rawArtifact : rawArtifacts) {
            final String apNodeFdn = rawArtifact.getApNodeFdn();
            final String nodeName = FDN.get(apNodeFdn).getRdnValue();
            final String artifactName = rawArtifact.getName();
            final String artifactExtension = rawArtifact.getExtension();
            final String artifactNameWithExtension = artifactName + "." + artifactExtension;
            final String artifactType = rawArtifact.getType();
            final String artifactContent = rawArtifact.getArtifactContent();
            final String rawArtifactPath = getRawArtifactPath(projectName, nodeName, artifactNameWithExtension);
            final String parentFdnOfArtifact = apNodeFdn + "," + MoType.NODE_ARTIFACT_CONTAINER.toString() + "=1";
            final String configurationNodeName = rawArtifact.getConfigurationNodeName();
            final ArtifactFileFormat fileFormat = rawArtifact.getFileFormat();
            final boolean ignoreError = rawArtifact.isIgnoreError();

            final Map<String, Object> nodeArtifactAttributes = createNodeArtifactAttributes(artifactName, artifactType, rawArtifactPath,
                    configurationNodeName, fileFormat, ignoreError);
            createArtifactMo(parentFdnOfArtifact, nodeArtifactAttributes, managedObjectByFdn, liveBucket);

            if (artifactContent == null) {
                continue;
            }

            if (!artifactExtension.equals(ZIP_EXTENSION)) {
                if(artifactType.equalsIgnoreCase("siteInstallation")) {
                    siteInstallPath = rawArtifactPath;
                    byte[] afterEncryption = cryptographyService.encrypt(artifactContent.getBytes(StandardCharsets.UTF_8));
                    fileWithContents.put(rawArtifactPath, afterEncryption);
                }
                else {
                    fileWithContents.put(rawArtifactPath, artifactContent.getBytes());
                }
            } else {
                fileWithContents.put(rawArtifactPath, rawArtifact.getArtifactContentAsBytes());
            }
        }

        resourceOperations.writeArtifacts(fileWithContents);

        if(!siteInstallPath.trim().isEmpty()){
            try {
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_READ);
                permissions.add(PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(Paths.get(siteInstallPath), permissions);
            }
            catch (IOException | UnsupportedOperationException e) {
                logger.error(" Exception {} while setting file permission for siteInstallation file : {} ", e.getMessage(), siteInstallPath);
            }
        }
        else {
            logger.warn("SiteInstall file is not found for project {}", projectName);
        }
    }

    private static Map<String, Object> createNodeArtifactAttributes(final String artifactName, final String artifactType,
                                                                    final String rawArtifactPath, final String configurationNodeName,
                                                                    final ArtifactFileFormat fileFormat, final boolean ignoreError) {
        final Map<String, Object> nodeArtifactAttributes = new HashMap<>();
        nodeArtifactAttributes.put(NodeArtifactAttribute.NAME.toString(), artifactName);
        nodeArtifactAttributes.put(NodeArtifactAttribute.TYPE.toString(), artifactType);
        nodeArtifactAttributes.put(NodeArtifactAttribute.GEN_LOCATION.toString(), null);
        nodeArtifactAttributes.put(NodeArtifactAttribute.RAW_LOCATION.toString(), rawArtifactPath);
        nodeArtifactAttributes.put(NodeArtifactAttribute.EXPORTABLE.toString(), Boolean.TRUE);
        nodeArtifactAttributes.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.NOT_STARTED.name());
        nodeArtifactAttributes.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), configurationNodeName);
        nodeArtifactAttributes.put(NodeArtifactAttribute.FILE_FORMAT.toString(), fileFormat.name());
        nodeArtifactAttributes.put(NodeArtifactAttribute.IGNORE_ERROR.toString(), ignoreError);

        return nodeArtifactAttributes;
    }

    private void createArtifactMo(final String parentFdn, final Map<String, Object> createAttributes,
                                  final Map<String, ManagedObject> managedObjectByFdn, final DataBucket liveBucket) {
        final ManagedObject parentMo = getArtifactParentMo(parentFdn, managedObjectByFdn, liveBucket);
        final ManagedObject mo = liveBucket.getManagedObjectBuilder().type(MoType.NODE_ARTIFACT.toString()).parent(parentMo)
                .addAttributes(createAttributes).create();
        logger.debug("Successfully created NodeArtifact MO {}", mo.getFdn());
    }

    private static ManagedObject getArtifactParentMo(final String parentFdn, final Map<String, ManagedObject> managedObjectByFdn,
                                                     final DataBucket liveBucket) {
        if (!managedObjectByFdn.containsKey(parentFdn)) {
            final ManagedObject parentMo = liveBucket.findMoByFdn(parentFdn);
            managedObjectByFdn.put(parentFdn, parentMo);
        }
        return managedObjectByFdn.get(parentFdn);
    }

    private static String getRawArtifactPath(final String projectName, final String nodeName, final String artifactName) {
        return new StringBuilder(DirectoryConfiguration.getRawDirectory())
                .append(File.separator)
                .append(projectName)
                .append(File.separator)
                .append(nodeName)
                .append(File.separator)
                .append(artifactName)
                .toString();
    }
}
