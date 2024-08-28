/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;

/**
 * Creates the <code>AutoIntegrationOptions</code> MO from the input {@link NodeInfo}.
 */
public class NodeStatusMoCreator {

    @Inject
    private DpsOperations dps;

    /**
     * Creates an {@link MoType#NODE_STATUS} MO for node in AP model.
     *
     * @param nodeMo   the AP node MO
     * @param nodeData the node data
     * @return the created NodeStatus MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeData) {
        final Map<String, Object> nodeStatusAttributes = new HashMap<>();
        nodeStatusAttributes.put(NodeStatusAttribute.STATUS_ENTRIES.toString(), new ArrayList<>());

        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket()
            .getManagedObjectBuilder()
            .type(MoType.NODE_STATUS.toString())
            .parent(nodeMo)
            .addAttributes(nodeStatusAttributes)
            .create();
        if (ActivityType.EXPANSION_ACTIVITY.equals(nodeData.getActivity())) {
            nodeStatusMo.setAttribute(NodeStatusAttribute.STATE.toString(), State.READY_FOR_EXPANSION.name());
        } else if (ActivityType.MIGRATION_ACTIVITY.equals(nodeData.getActivity())) {
            nodeStatusMo.setAttribute(NodeStatusAttribute.STATE.toString(), State.READY_FOR_PRE_MIGRATION.name());
        } else if (ActivityType.EOI_INTEGRATION_ACTIVITY.equals(nodeData.getActivity())){
            nodeStatusMo.setAttribute(NodeStatusAttribute.STATE.toString(), State.READY_FOR_EOI_INTEGRATION.name());
        }
        return nodeStatusMo;
    }

    public ManagedObject eoiCreate(final ManagedObject nodeMo) {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setActivity(ActivityType.EOI_INTEGRATION_ACTIVITY);
        return create(nodeMo, nodeInfo);

    }
}
