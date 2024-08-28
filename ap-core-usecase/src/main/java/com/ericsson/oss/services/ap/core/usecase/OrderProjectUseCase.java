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

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectInfo;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;


/**
 * Starts integration workflow for all nodes in a project.
 */
@UseCase(name = UseCaseName.ORDER_PROJECT)
public class OrderProjectUseCase {

    @Inject
    private ContextService contextService;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    @UseCase(name = UseCaseName.ORDER_NODE)
    private OrderNodeUseCase orderNodeUseCase;

    @Inject
    private Logger logger;

    @Inject
    private SystemRecorder recorder;

    /**
     * Start integration workflow for all nodes in the project.
     *
     * @param projectFdn
     *            the FDN of the AP project
     * @param validationRequired
     *            is validation required
     */
    public void execute(final String projectFdn, final boolean validationRequired) {
        this.execute(projectFdn, validationRequired, null);
    }

    /**
     * Start integration workflow for all nodes in the project.
     *
     * @param projectFdn
     *            the FDN of the AP project
     * @param validationRequired
     *            is validation required
     * @param projectInfo
     *            project info parsed from project.zip
     */
    public void execute(final String projectFdn, final boolean validationRequired, final ProjectInfo projectInfo) {
        try {
            final List<FDN> nodeFdnList = (projectInfo == null) ? getNodeFdnFromProjectFdn(projectFdn) : getNodeFdnFromProjectInfo(projectFdn, projectInfo);
            orderAllNodes(projectFdn, nodeFdnList, validationRequired, projectInfo);
        } catch (final Exception e) {
            logger.error("Error reading nodes in project {}", projectFdn, e);
            recorder.recordError(CommandLogName.ORDER_PROJECT.toString(), ErrorSeverity.ERROR, FDN.get(projectFdn).getRdnValue(), projectFdn,
                    e.getMessage());
        }
    }

    private List<FDN> getNodeFdnFromProjectFdn(final String projectFdn) {
        final List<FDN> nodeFdnList = new ArrayList<>();
        final Iterator<ManagedObject> apNodesMos = dpsQueries.findChildMosOfTypes(projectFdn, AP.toString(), MoType.NODE.toString()).execute();
        while (apNodesMos.hasNext()) {
            final ManagedObject apNodeMo = apNodesMos.next();
            final FDN nodeFdn = FDN.get(apNodeMo.getFdn());
            nodeFdnList.add(nodeFdn);
        }
        return nodeFdnList;
    }

    private List<FDN> getNodeFdnFromProjectInfo(final String projectFdn, final ProjectInfo projectInfo) {
        final List<FDN> nodeFdnList = new ArrayList<>();
        final Map<String, NodeInfo> nodeInfos = projectInfo.getNodeInfos();
        for (String key : nodeInfos.keySet()) {
            final String nodeName = key;
            final FDN nodeFdn = FDN.get(projectFdn + ",Node=" + nodeName);
            nodeFdnList.add(nodeFdn);
        }
        return nodeFdnList;
    }

    private void orderAllNodes(final String projectFdn, final List<FDN> nodeFdnList, final boolean validationRequired, final ProjectInfo projectInfo) {
        final String userId = getUserIdContext();
        int successfulOrders = 0;
        int failedOrders = 0;
        int invalidOrders = 0;

        for (final FDN nodeFdn : nodeFdnList) {
            final String nodeName = nodeFdn.getRdnValue();
            final NodeInfo nodeInfo = projectInfo != null ? projectInfo.getNodeInfoFromName(nodeName) : new NodeInfo();

            try {
                orderNodeUseCase.execute(nodeFdn.toString(), validationRequired, nodeInfo);
                setUserIdContext(userId);
                successfulOrders++;
            } catch (final InvalidNodeStateException e) {
                invalidOrders++;
                logger.error("Node {} is not in valid state to be ordered: [{}]", nodeFdn, e.getMessage(), e);
                recorder.recordError(CommandLogName.ORDER_PROJECT.toString(), ErrorSeverity.ERROR, nodeName, nodeFdn.toString(),
                        e.getMessage());
            } catch (final Exception e) {
                failedOrders++;
                logger.error("Error ordering node {}", nodeFdn, e);
                recorder.recordError(CommandLogName.ORDER_PROJECT.toString(), ErrorSeverity.ERROR, nodeName, nodeFdn.toString(),
                        e.getMessage());
            }
        }

        final int numOrderedNodes = successfulOrders + failedOrders + invalidOrders;
        logger.info("Order project completed for {} [Nodes: {}, Successful orders: {}, Failed orders: {}, Invalid orders: {}]", projectFdn,
                numOrderedNodes, successfulOrders, failedOrders, invalidOrders);
    }

    private String getUserIdContext() {
        return contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY);
    }

    // Sets the user ID in the ContextService. The user ID is cleaned in the pre-destroy method of the EJB Call
    // to the workflow instance service in OrderNode and will be lost after this unless set explicitly.
    private void setUserIdContext(final String userId) {
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);
    }
}
