/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.hardwarereplace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeDhcpMoCreator;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.SecurityMoCreator;

/**
 * Creates the Hardware Replace Node MO.
 */
public class HardwareReplaceModelCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private NodeDhcpMoCreator nodeDhcpMoCreator;

    @Inject
    private SecurityMoCreator securityMoCreator;

    /**
     * Creates an AP node MO under the given AP project, using the supplied data for its attributes.
     *
     * @param nodeInfo
     *            class describing the attribute values of the AP node MO
     * @param projectFdn
     *            the FDN of the parent AP project
     * @return the AP node {@link ManagedObject}
     */
    public ManagedObject create(final NodeInfo nodeInfo, final String projectFdn) {
        final DataBucket liveBucket = dps.getDataPersistenceService().getLiveBucket();
        final ManagedObject projectMo = liveBucket.findMoByFdn(projectFdn);
        final ManagedObject nodeMo = createNodeMo(liveBucket, projectMo, nodeInfo);
        createNodeArtifactContainerMo(liveBucket, nodeMo);
        createApStatusMo(nodeMo);
        nodeDhcpMoCreator.create(nodeMo, nodeInfo);
        securityMoCreator.createSecurityMo(nodeMo, nodeInfo);
        return nodeMo;
    }

    private static ManagedObject createNodeMo(final DataBucket liveBucket, final ManagedObject projectMo, final NodeInfo nodeInfo) {
        final Map<String, Object> nodeAttributes = nodeInfo.getNodeAttributes();
        nodeAttributes.put(NodeAttribute.WORKFLOW_INSTANCE_ID_LIST.toString(), new ArrayList<String>());

        return liveBucket.getManagedObjectBuilder()
            .type(MoType.NODE.toString())
            .name(nodeInfo.getName())
            .parent(projectMo)
            .addAttributes(nodeAttributes)
            .create();
    }

    private static void createNodeArtifactContainerMo(final DataBucket liveBucket, final ManagedObject nodeMo) {
        liveBucket.getManagedObjectBuilder()
            .type(MoType.NODE_ARTIFACT_CONTAINER.toString())
            .parent(nodeMo)
            .addAttributes(new HashMap<String, Object>())
            .create();
    }

    private void createApStatusMo(final ManagedObject apNodeMo) {
        final Map<String, Object> nodeStatusAttributes = new HashMap<>();
        nodeStatusAttributes.put(NodeStatusAttribute.STATUS_ENTRIES.toString(), new ArrayList<>());
        nodeStatusAttributes.put(NodeStatusAttribute.STATE.toString(), State.READY_FOR_HARDWARE_REPLACE.name());
        dps.getDataPersistenceService().getLiveBucket()
            .getManagedObjectBuilder()
            .type(MoType.NODE_STATUS.toString())
            .parent(apNodeMo)
            .addAttributes(nodeStatusAttributes)
            .name("1")
            .create();
    }
}
