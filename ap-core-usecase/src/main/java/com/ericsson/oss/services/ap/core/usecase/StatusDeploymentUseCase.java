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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * View status for a single deployment.
 */
@UseCase(name = UseCaseName.STATUS_DEPLOYMENT)
public class StatusDeploymentUseCase {

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    /**
     * Gets the overall status of the deployment, detailing the state of each node in the project.
     *
     * @param deployment
     *            the name of the deployment
     * @return {@link ApNodeGroupStatus}
     * @throws ApServiceException
     *             if there is an error viewing the deployment status
     * @throws ApApplicationException
     *             if there is an error viewing the deployment status
     */
    public ApNodeGroupStatus execute(final String deployment) {
        try {
            final List<NodeStatus> nodesStatuses = readStatusForAllNodesInDeployment(deployment);
            final ApNodeGroupStatus deploymentStatus = ApNodeGroupStatus.getDeploymentApNodeGroupStatus(deployment, nodesStatuses);
            logger.info("Deployment status for deployment {}: {}", deployment, deploymentStatus);
            return deploymentStatus;
        } catch (final ApServiceException e) {
            throw e;
        } catch (final Exception exception) {
            throw new ApApplicationException(String.format("Error reading status for deployment %s", deployment), exception);
        }
    }

    private List<NodeStatus> readStatusForAllNodesInDeployment(final String deployment) {
        final Iterator<ManagedObject> nodeMos = dpsQueries
                .findMosWithAttributeValue(NodeAttribute.DEPLOYMENT.toString(), deployment, AP.toString(), NODE.toString())
                .execute();

        if (!nodeMos.hasNext()) {
            return Collections.emptyList();
        }

        final List<NodeStatus> nodesStatus = new ArrayList<>();
        while (nodeMos.hasNext()) {
            final ManagedObject nodeMo = nodeMos.next();
            final ManagedObject nodeStatusMo = nodeMo.getChild(NODE_STATUS.toString() + "=1");
            final String nodeName = nodeStatusMo.getParent().getName();
            final String state = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());

            nodesStatus.add(new NodeStatus(nodeName, deployment, Collections.<StatusEntry>emptyList(), state));
        }
        return nodesStatus;
    }
}