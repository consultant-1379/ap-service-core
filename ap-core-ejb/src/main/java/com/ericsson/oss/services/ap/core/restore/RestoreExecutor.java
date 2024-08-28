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
package com.ericsson.oss.services.ap.core.restore;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.restore.RestoreController;
import com.ericsson.oss.services.ap.api.restore.RestoredNodeStateResolver;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;

/**
 * Delegates the restore requests during an AP restore to each of the {@link RestoreController}.
 */
@Stateless
public class RestoreExecutor {

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private StateTransitionManagerLocal stateTransitionManagerLocal;

    @Inject
    private WorkflowActions workflowActions;

    @Inject
    private WorkflowRestoreCriteria workflowRestoreCriteria;

    private ServiceFinderBean serviceFinder;

    @PostConstruct
    public void init() {
        serviceFinder = new ServiceFinderBean();
    }

    /**
     * Determines what should happen to each suspended workflow, i.e. cancel or resume the workflow. Also sets the state of the restored node.
     *
     * @param suspendedWfInstanceIds
     *            list of suspended workflow IDs
     * @param isLastRestoreAttempt
     *            true if max restore duration has been reached
     * @return a <code>WorkflowRestoreResult</code> object for each suspend workflow, containing the result of the restore
     */
    public List<WorkflowRestoreResult> execute(final List<String> suspendedWfInstanceIds, final boolean isLastRestoreAttempt) {
        logger.info("Executing restore. Last restore attempt flag set to {}", isLastRestoreAttempt);
        final List<WorkflowRestoreResult> workflowRestoreResults = new ArrayList<>(suspendedWfInstanceIds.size());

        for (final String suspendedWfInstanceId : suspendedWfInstanceIds) {
            final MoData nodeMo = findNodeMoWithActiveWorkflowInstanceId(suspendedWfInstanceId);
            if (nodeMo == null) {
                logger.info("No AP node found with active workflow instance ID {}", suspendedWfInstanceId);
                continue;
            }

            if (workflowRestoreCriteria.isWorkflowCancellable(nodeMo.getFdn(), isLastRestoreAttempt)) {
                workflowRestoreResults.add(cancelWorkflow(suspendedWfInstanceId, nodeMo));
            } else if (workflowRestoreCriteria.isWorkflowResumable(nodeMo.getFdn(), isLastRestoreAttempt)) {
                workflowRestoreResults.add(resumeWorkflow(suspendedWfInstanceId, nodeMo));
            } else {
                logger.info("Restore for workflow {} with workflow instance ID {} is pending", nodeMo.getFdn(), suspendedWfInstanceId);
                workflowRestoreResults.add(new WorkflowRestoreResult(RestoreResult.PENDING, suspendedWfInstanceId, nodeMo.getFdn()));
            }
        }

        return workflowRestoreResults;
    }

    private WorkflowRestoreResult cancelWorkflow(final String suspendedWfInstanceId, final MoData nodeMo) {
        logger.info("Cancelling workflow for {} with workflow instance ID {}", nodeMo.getFdn(), suspendedWfInstanceId);
        workflowActions.cancelWorkflow(suspendedWfInstanceId);
        final String nodeType = nodeTypeMapper.getInternalEjbQualifier((String) nodeMo.getAttribute(NODE_TYPE.toString()));
        final RestoredNodeStateResolver restoredNodeStateResolver = serviceFinder.find(RestoredNodeStateResolver.class, nodeType.toLowerCase());
        final State restoredNodeState = restoredNodeStateResolver.resolveNodeState(nodeMo.getFdn());
        stateTransitionManagerLocal.setStateWithoutValidation(nodeMo.getFdn(), restoredNodeState);

        return new WorkflowRestoreResult(RestoreResult.CANCELLED, suspendedWfInstanceId, nodeMo.getFdn());
    }

    private WorkflowRestoreResult resumeWorkflow(final String suspendedWfInstanceId, final MoData nodeMo) {
        logger.info("Resuming workflow for {} with workflow instance ID {}", nodeMo.getFdn(), suspendedWfInstanceId);
        workflowActions.resumeWorkflow(suspendedWfInstanceId);
        stateTransitionManagerLocal.setStateWithoutValidation(nodeMo.getFdn(), State.ORDER_COMPLETED);

        return new WorkflowRestoreResult(RestoreResult.RESUMED, suspendedWfInstanceId, nodeMo.getFdn());
    }

    private MoData findNodeMoWithActiveWorkflowInstanceId(final String suspendedWfInstanceId) {
        MoData nodeMoData = null;
        try {
            final Iterator<ManagedObject> nodeMos = dpsQueries.findMosWithAttributeValue(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(),
                    suspendedWfInstanceId, AP.toString(), NODE.toString()).execute();
            if (nodeMos.hasNext()) {
                final ManagedObject nodeMo = nodeMos.next();
                nodeMoData = new MoData(nodeMo.getFdn(), nodeMo.getAllAttributes(), nodeMo.getType(),
                        new ModelData(nodeMo.getNamespace(), nodeMo.getVersion()));
            }
        } catch (final Exception e) {
            logger.warn("Could not find MO corresponding to workflow instance ID {}", suspendedWfInstanceId, e);
        }
        return nodeMoData;
    }
}
