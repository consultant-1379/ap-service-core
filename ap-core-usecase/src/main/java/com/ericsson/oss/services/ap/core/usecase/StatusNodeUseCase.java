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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * View integration status for a node.
 */
@UseCase(name = UseCaseName.STATUS_NODE)
public class StatusNodeUseCase {

    @Inject
    private DpsOperations dps;

    private StatusEntryManagerLocal statusEntryManager;

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        statusEntryManager = new ServiceFinderBean().find(StatusEntryManagerLocal.class);
    }

    /**
     * Gets the integration status for a node detailing the progress in the integration workflow.
     *
     * @param nodeFdn
     *               the FDN of the node in AP model
     * @return <code>NodeStatus</code>
     *               node status information for an AP node
     */
    public NodeStatus execute(final String nodeFdn) {
        logger.info("Viewing status for {}", nodeFdn);
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        return getNodeStatus(nodeMo);
    }

    private NodeStatus getNodeStatus(final ManagedObject nodeMo) {
        final FDN nodeFdn = FDN.get(nodeMo.getFdn());
        final String nodeName = nodeFdn.getRdnValue();
        final String projectName = FDN.get(nodeFdn.getParent()).getRdnValue();
        final ManagedObject nodeStatusMo = nodeMo.getChild(MoType.NODE_STATUS.toString() + "=1");
        final List<StatusEntry> statusEntries = statusEntryManager.getAllStatusEntries(nodeFdn.toString());

        final String state = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());

        return new NodeStatus(nodeName, projectName, statusEntries, state);
    }
}
