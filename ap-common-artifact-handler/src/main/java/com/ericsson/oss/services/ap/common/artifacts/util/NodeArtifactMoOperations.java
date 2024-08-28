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
package com.ericsson.oss.services.ap.common.artifacts.util;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.security.cryptography.CryptographyServiceDecryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.ManagedObjectBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.util.collections.IteratorUtils;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Commons set of operations for creating/deleting/reading <code>NodeArtifact</code> managed object.
 */
public class NodeArtifactMoOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private CryptographyService cyptographyService;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    private DataPersistenceService dps;

    @PostConstruct
    public void init() {
        dps = new ServiceFinderBean().find(DataPersistenceService.class);
    }

    /**
     * Gets all <code>NodeArtifacts</code> MOs for the node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return MOs or empty list if no artifacts exists
     */
    public Collection<ManagedObject> getNodeArtifactMos(final String apNodeFdn) {
        final Iterator<ManagedObject> artifactMos = dpsQueries
                .findChildMosOfTypes(apNodeFdn, ObjectField.CREATED_TIME, MoType.NODE_ARTIFACT.toString()).execute();
        return IteratorUtils.convertIteratorToList(artifactMos);
    }

    /**
     * Gets all <code>NodeArtifacts</code> MOs of the specified type for the node.
     * Collection returned will be ordered based on NodeArtifactId
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactType
     *            the type of artifact to retrieve
     * @return MOs or empty list if no artifacts exists
     */
    public Collection<ManagedObject> getNodeArtifactMosOfType(final String apNodeFdn, final String artifactType) {
        return getNodeArtifactMos(apNodeFdn).stream()
                .filter(artifactMo -> equalsArtifactType(artifactMo, artifactType))
                .sorted(Comparator.comparingInt(this::getRdnValue))
                .collect(Collectors.toList());
    }

    private boolean equalsArtifactType(final ManagedObject artifactMo, final String artifactType) {
        final String artifactMoNodeType = artifactMo.getAttribute(NodeArtifactAttribute.TYPE.toString());
        return artifactMoNodeType.equalsIgnoreCase(artifactType);
    }

    private int getRdnValue(final ManagedObject artifactMo) {
        return new Integer(FDN.get(artifactMo.getFdn()).getRdnValue());
    }

    /**
     * Deletes the <code>NodeArtifact</code> MO.
     *
     * @param nodeArtifactMo
     *            the MO to delete
     */
    public void deleteNodeArtifactMo(final ManagedObject nodeArtifactMo) {
        final String nodeArtifactFdn = nodeArtifactMo.getFdn();
        dps.getLiveBucket().deletePo(nodeArtifactMo);
        logger.debug("Successfully deleted MO {}", nodeArtifactFdn);
    }

    /**
     * Creates <code>NodeArtifact</code> MO with supplied attributes.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param createAttributes
     *            the attributes with which to create the NodeArtifact MO
     */
    public void createNodeArtifactMo(final String apNodeFdn, final Map<String, Object> createAttributes) {
        final String nodeArtifactContainerFdn = apNodeFdn + "," + MoType.NODE_ARTIFACT_CONTAINER.toString() + "=1";
        final DataBucket liveBucket = dps.getLiveBucket();
        final ManagedObject parentMo = liveBucket.findMoByFdn(nodeArtifactContainerFdn);
        final ManagedObjectBuilder moBuilder = liveBucket.getManagedObjectBuilder()
                .type(MoType.NODE_ARTIFACT.toString())
                .parent(parentMo)
                .addAttributes(createAttributes);
        final ManagedObject mo = moBuilder.create();

        logger.debug("Successfully created MO {}", mo.getFdn());
    }

    /**
     * Creates raw <code>ArtifactDetails</code> containing details of the node raw artifact on the filesystem.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactLocation
     *            the location of the raw artifact on the filesystem
     * @param artifactMo
     *            the NodeArtifact MO
     * @return <code>ArtifactDetails</code> containing the details of the NodeArtifact MO
     */
    public ArtifactDetails createRawArtifactDetails(final String apNodeFdn, final String artifactLocation, final ManagedObject artifactMo) {
        byte[] artifactContents = artifactResourceOperations.readArtifact(artifactLocation);
        if(artifactMo.getAttribute("type").toString().equalsIgnoreCase("siteInstallation"))
        {
            try {
                artifactContents = cyptographyService.decrypt(artifactContents);
            }
            catch (CryptographyServiceDecryptionException e)
            {
                logger.warn("Error occurred while decrypting the content due to backward compatibility {}",e.getMessage());
            }
        }
        return createArtifactDetails(apNodeFdn, artifactLocation, artifactMo, artifactContents);
    }

    /**
     * Creates generated <code>ArtifactDetails</code> containing details of the node generated artifact on the filesystem. If the generated artifact
     * was encrypted on the filesystem, it will be decrypted in the <code>ArtifactDetails</code> object.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param artifactLocation
     *            the location of the generated artifact on the filesystem
     * @param artifactMo
     *            the NodeArtifact MO
     * @return <code>ArtifactDetails</code> containing the details of the NodeArtifact MO
     */
    public ArtifactDetails createGeneratedArtifactDetails(final String apNodeFdn, final String artifactLocation, final ManagedObject artifactMo) {
        final boolean encrypted = artifactMo.getAttribute(NodeArtifactAttribute.ENCRYPTED.toString());
        byte[] artifactContents = artifactResourceOperations.readArtifact(artifactLocation);

        if (encrypted) {
            artifactContents = cyptographyService.decrypt(artifactContents);
        }
        return createArtifactDetails(apNodeFdn, artifactLocation, artifactMo, artifactContents);
    }

    /**
     * Update the suspend attribute in node artifacts container MO.
     *
     * @param nodeArtifactContainerFdn
     *            the node Artifact Container FDN
     * @param nodeConfigurationAttributeMap
     *            the configuration attributes
     */
    public void refreshNodeArtifactContainerMo(final String nodeArtifactContainerFdn, final Map<String, Object> nodeConfigurationAttributeMap) {
        final DataBucket liveBucket = dps.getLiveBucket();
        final ManagedObject parentMo = liveBucket.findMoByFdn(nodeArtifactContainerFdn);
        parentMo.setAttributes(nodeConfigurationAttributeMap);
    }

    private static ArtifactDetails createArtifactDetails(final String apNodeFdn, final String artifactLocation, final ManagedObject artifactMo,
                                                         final byte[] artifactContents) {
        final String artifactName = new File(artifactLocation).getName();
        final String artifactType = artifactMo.getAttribute(NodeArtifactAttribute.TYPE.toString());
        final boolean exportable = artifactMo.getAttribute(NodeArtifactAttribute.EXPORTABLE.toString());
        final ArtifactImportProgress importProgress = ArtifactImportProgress
                .getImportProgress(artifactMo.getAttribute(NodeArtifactAttribute.IMPORT_PROGRESS.toString()));
        final List<String> importErrorMsg = artifactMo.getAttribute(NodeArtifactAttribute.IMPORT_ERR_MSG.toString());
        final String configurationNodeName = artifactMo.getAttribute(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString());
        final Boolean ignoreErrorValue = artifactMo.getAttribute(NodeArtifactAttribute.IGNORE_ERROR.toString());
        final boolean ignoreError = (ignoreErrorValue != null) && ignoreErrorValue;
        final ArtifactFileFormat fileFormat = ArtifactFileFormat.getFileFormat(artifactMo.getAttribute(NodeArtifactAttribute.FILE_FORMAT.toString()));

        return new ArtifactBuilder()
                .apNodeFdn(apNodeFdn)
                .name(artifactName)
                .type(artifactType)
                .location(artifactLocation)
                .exportable(exportable)
                .encrypted(false)
                .artifactContent(artifactContents)
                .importProgress(importProgress)
                .importErrorMsg(importErrorMsg)
                .configurationNodeName(configurationNodeName)
                .fileFormat(fileFormat)
                .ignoreError(ignoreError)
                .build();
    }

    /**
     * Get the file format of a Node Artifact
     *
     * @param rawLocation
     *            the raw location of the artifact file
     * @return the {@link ArtifactFileFormat} of the file
     *
     */
    public ArtifactFileFormat getArtifactFileFormat(final String rawLocation) {
        final ManagedObject artifactMo = getNodeArtifactMoByRawLocation(rawLocation);
        if (artifactMo == null) {
            logger.error("Could not find NodeArtifact MO with raw location {}", rawLocation);
            return ArtifactFileFormat.UNKNOWN;
        }
        return ArtifactFileFormat.getFileFormat(artifactMo.getAttribute(NodeArtifactAttribute.FILE_FORMAT.toString()));
    }

    /**
     * Get the Node Artifact MO by raw location
     *
     * @param rawLocation
     *            the raw location of the artifact file
     * @return the {@link ManagedObject} of node artifact
     *
     */
    public ManagedObject getNodeArtifactMoByRawLocation(final String rawLocation) {
        final Iterator<ManagedObject> artifactMos = dpsQueries.findMosWithAttributeValue(NodeArtifactAttribute.RAW_LOCATION.toString(), rawLocation,
                Namespace.AP.toString(), MoType.NODE_ARTIFACT.toString()).execute();
        return artifactMos.hasNext() ? artifactMos.next() : null;
    }
}
