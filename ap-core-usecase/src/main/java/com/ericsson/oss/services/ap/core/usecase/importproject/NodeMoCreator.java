/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.CNF_TYPE;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.IPADDRESS;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.NODE_IDENTIFIER;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.NODE_NAME;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.NODE_TYPE;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.OSS_PREFIX;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.TIME_ZONE;

import java.util.*;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApNodeExistsException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;

/**
 * Creates the AP <code>Node</code> MO.
 */
public class NodeMoCreator {

    private static final String FH_NODE_TYPE_IN_AP = "FRONTHAUL6000";
    private static final String FH_NODE_TYPE_IN_OSS = "FRONTHAUL-6000";
    private static final String R6K2_NODE_TYPE_IN_AP = "Router60002";
    private static final String R6K2_NODE_TYPE_IN_OSS = "Router6000-2";
    private static final String SHARED_CNF_NODE_TYPE_IN_AP = "SharedCNF";
    private static final String SHARED_CNF_NODE_TYPE_IN_OSS = "Shared-CNF";


    @Inject
    private DpsOperations dps;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private DpsQueries dpsQueries;

    /**
     * Creates an AP node MO under the given AP project, using the supplied data for its attributes. Also creates the
     * {@link MoType#NODE_ARTIFACT_CONTAINER} as a child of the node MO.
     *
     * @param projectFdn
     *            the FDN of the parent AP project
     * @param nodeInfo
     *            class describing the attribute values of the APnode MO
     * @return the AP node {@link ManagedObject}
     */
    public ManagedObject create(final String projectFdn, final NodeInfo nodeInfo) {
        if (isNodeExisting(nodeInfo.getName())) {
            throw new ApNodeExistsException(nodeInfo.getName());
        }
        final DataBucket liveBucket = dps.getDataPersistenceService().getLiveBucket();
        final ManagedObject projectMo = liveBucket.findMoByFdn(projectFdn);
        ddpTimer.start(CommandLogName.CREATE_NODE_MO.toString());
        final ManagedObject nodeMo = createNodeMo(liveBucket, projectMo, nodeInfo);
        ddpTimer.end(nodeMo.getFdn());

         createNodeArtifactContainerMo(liveBucket, nodeMo, nodeInfo);
        return nodeMo;
    }

 /**
     * Creates an AP node MO under the given AP project, using the supplied data for its attributes. Also creates the
     * {@link MoType#NODE_ARTIFACT_CONTAINER} as a child of the node MO.
     *
     * @param projectFdn
     *            the FDN of the parent AP project
     * @param nodeData
     *            class describing the attribute values of the APnode MO
     * @return the AP node {@link ManagedObject}
     */
    public ManagedObject eoiCreate(final String projectFdn, final Map<String,Object> nodeData) {
        NodeInfo nodeInfo = new NodeInfo();
        final Map<String, Object> nodeAttribute = nodeAttributesReader(nodeData);
        nodeInfo.setName((String) nodeAttribute.get(NODE_NAME.toString()));
        nodeAttribute.remove(NODE_NAME.toString());
        nodeInfo.setConfigurationAttributes(Collections.emptyMap());
        nodeInfo.setNodeAttributes(nodeAttribute);
        return create(projectFdn, nodeInfo);
    }

    private Map<String,Object> nodeAttributesReader(final Map<String,Object> networkElements)
    {
        final Map<String,Object> nodeAttribute = new HashMap<>();
        nodeAttribute.put(NODE_NAME.toString(), networkElements.get(NODE_NAME.toString()));
        nodeAttribute.put(NODE_TYPE.toString(), networkElements.get(NODE_TYPE.toString()));
        nodeAttribute.put(CNF_TYPE.toString(), networkElements.get(CNF_TYPE.toString()));
        nodeAttribute.put(IPADDRESS.toString(), networkElements.get(IPADDRESS.toString()));
        nodeAttribute.put(OSS_PREFIX.toString(), networkElements.get(OSS_PREFIX.toString()));
        nodeAttribute.put(NODE_IDENTIFIER.toString(), networkElements.get(NODE_IDENTIFIER.toString()));
        nodeAttribute.put(TIME_ZONE.toString(), networkElements.get(TIME_ZONE.toString()));
        return  nodeAttribute;
    }


    private boolean isNodeExisting(final String nodeName) {
        final Iterator<ManagedObject> nodeMo = dpsQueries.findMoByNameInTransaction(nodeName, MoType.NODE.toString(), Namespace.AP.toString()).execute();
        if(nodeMo != null) {
            return nodeMo.hasNext();
        }
        return false;
    }

    private static ManagedObject createNodeMo(final DataBucket liveBucket, final ManagedObject projectMo, final NodeInfo nodeInfo) {
        final Map<String, Object> nodeAttributes = nodeInfo.getNodeAttributes();
        final String nodeType = NodeAttribute.NODE_TYPE.toString();
        if(nodeAttributes.containsKey(nodeType) && FH_NODE_TYPE_IN_OSS.equals(nodeAttributes.get(nodeType).toString())) {
            nodeAttributes.put(nodeType, FH_NODE_TYPE_IN_AP);
        }
        if(nodeAttributes.containsKey(nodeType) && R6K2_NODE_TYPE_IN_OSS.equals(nodeAttributes.get(nodeType).toString())) {
            nodeAttributes.put(nodeType, R6K2_NODE_TYPE_IN_AP);
        }
        if(nodeAttributes.containsKey(nodeType) && SHARED_CNF_NODE_TYPE_IN_OSS.equals(nodeAttributes.get(nodeType).toString()))
        {
            nodeAttributes.put(nodeType, SHARED_CNF_NODE_TYPE_IN_AP);
        }
        nodeAttributes.put(NodeAttribute.WORKFLOW_INSTANCE_ID_LIST.toString(), new ArrayList<String>());

        return liveBucket.getManagedObjectBuilder()
            .type(MoType.NODE.toString())
            .name(nodeInfo.getName())
            .parent(projectMo)
            .addAttributes(nodeAttributes)
            .create();
    }


    private static void createNodeArtifactContainerMo(final DataBucket liveBucket, final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        liveBucket.getManagedObjectBuilder()
            .type(MoType.NODE_ARTIFACT_CONTAINER.toString())
            .parent(nodeMo)
            .addAttributes(nodeInfo.getConfigurationAttributes())
            .create();
    }
}
