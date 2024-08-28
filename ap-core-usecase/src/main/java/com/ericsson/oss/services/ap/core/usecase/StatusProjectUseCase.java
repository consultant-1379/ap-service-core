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
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * View status for a single project.
 */
@UseCase(name = UseCaseName.STATUS_PROJECT)
public class StatusProjectUseCase {

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    /**
     * Gets the overall status of the project, detailing the state of each node in the project.
     *
     * @param projectFdn
     *            the FDN of the project in AP model
     * @return {@link ApNodeGroupStatus}
     * @throws ApServiceException
     *             if there is an error viewing the project status
     * @throws ApApplicationException
     *             if there is an error viewing the project status
     */
    public ApNodeGroupStatus execute(final String projectFdn) {
        try {
            final String projectName = FDN.get(projectFdn).getRdnValue();
            final List<NodeStatus> nodesStatuses = readStatusForAllNodesInProject(projectFdn);
            final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(projectName, nodesStatuses);
            logger.info("Project status for project {}: {}", projectFdn, projectStatus);
            return projectStatus;
        } catch (final ApServiceException e) {
            throw e;
        } catch (final Exception exception) {
            throw new ApApplicationException(String.format("Error reading status for project %s", projectFdn), exception);
        }
    }

    private List<NodeStatus> readStatusForAllNodesInProject(final String projectFdn) {
        final String projectName = FDN.get(projectFdn).getRdnValue();
        final Iterator<ManagedObject> nodeStatusMos = dpsQueries.findChildMosOfTypes(projectFdn, AP.toString(), NODE_STATUS.toString()).execute();

        final List<NodeStatus> nodesStatus = new ArrayList<>();
        while (nodeStatusMos.hasNext()) {
            final ManagedObject nodeStatusMo = nodeStatusMos.next();
            final String nodeName = nodeStatusMo.getParent().getName();
            final String state = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());

            nodesStatus.add(new NodeStatus(nodeName, projectName, Collections.<StatusEntry> emptyList(), state));
        }
        return nodesStatus;
    }
}
