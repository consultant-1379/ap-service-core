/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Creates or update the {@code ControllingNodes} MO from the input {@link NodeInfo}.
 */
public class ControllingNodesMoCreator {

    @Inject
    private ModelReader modelReader;

    private DataPersistenceService dps;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @PostConstruct
    public void init() {
        dps = new ServiceFinderBean().find(DataPersistenceService.class);
    }

    /**
     * Creates an {@link MoType#CONTROLLING_NODES} MO for greenfield node in AP model.
     *
     * @param nodeMo
     *            the node MO
     * @param nodeInfo
     *            the node information from nodeInfo.xml
     * @return the created Controlling Nodes MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        if (nodeInfo.getControllingNodesAttributes().isEmpty()) {
            return null;
        }
        return createControllingNodesMo(nodeMo, nodeInfo);
    }

    private ManagedObject createControllingNodesMo(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String apNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());
        final ModelData controllingNodesModelData = modelReader.getLatestPrimaryTypeModel(apNamespace, MoType.CONTROLLING_NODES.toString());
        return dps.getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(apNamespace)
            .version(controllingNodesModelData.getVersion())
            .type(MoType.CONTROLLING_NODES.toString())
            .name("1")
            .addAttributes(nodeInfo.getControllingNodesAttributes())
            .create();
    }
}
