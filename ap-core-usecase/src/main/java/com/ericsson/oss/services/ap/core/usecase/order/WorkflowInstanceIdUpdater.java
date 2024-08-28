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
package com.ericsson.oss.services.ap.core.usecase.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;

/**
 * Update an AP node MO with the current active workflow instance ID.
 */
public class WorkflowInstanceIdUpdater {

    @Inject
    private DpsOperations dps;

    /**
     * Update {@link NodeAttribute#WORKFLOW_INSTANCE_ID_LIST} and {@link NodeAttribute#ACTIVE_WORKFLOW_INSTANCE_ID}, using {@link Transactional}
     * annotations to ensure MO update is done in separate transaction.
     *
     * @param workflowInstanceId
     *            the current workflow instance ID
     * @param nodeFdn
     *            the FDN of the AP node to update
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void update(final String workflowInstanceId, final String nodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final List<String> workflowInstanceIds = getWorkflowInstanceIds(nodeMo);
        workflowInstanceIds.add(workflowInstanceId);

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(NodeAttribute.WORKFLOW_INSTANCE_ID_LIST.toString(), workflowInstanceIds);
        attributes.put(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(), workflowInstanceId);
        nodeMo.setAttributes(attributes);
    }

    private static List<String> getWorkflowInstanceIds(final ManagedObject nodeMo) {
        final List<String> immutableWorkflowInstanceIds = nodeMo.getAttribute(NodeAttribute.WORKFLOW_INSTANCE_ID_LIST.toString());
        final List<String> mutableWorkflowInstanceIds = new ArrayList<>(immutableWorkflowInstanceIds.size());

        for (final String workflowInstanceId : immutableWorkflowInstanceIds) {
            mutableWorkflowInstanceIds.add(workflowInstanceId);
        }

        return mutableWorkflowInstanceIds;
    }
}
