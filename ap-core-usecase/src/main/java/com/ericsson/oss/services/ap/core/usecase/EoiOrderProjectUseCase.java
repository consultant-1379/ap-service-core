package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;


@UseCase(name = UseCaseName.EOI_ORDER_PROJECT) //can we create in UseCaseName.java. if yes what will be the usecase.
public class EoiOrderProjectUseCase {

    @Inject
    @UseCase(name = UseCaseName.EOI_ORDER_NODE)
    private EoiOrderNodeUseCase eoiOrderNodeUseCase;

    @Inject
    private ContextService contextService;

    @Inject
    private Logger logger;

    @Inject
    private SystemRecorder recorder;

    @Inject
    private DpsQueries dpsQueries;

    /**
     * Start integration workflow for all the nodes in EOI based project
     *
     * @param projectFdn
     * @return
     */

    public void execute(final String projectFdn, final String baseUrl, final String sessionId) {
        try {
            final List<FDN> nodeFdnList = getNodeFdnFromProjectFdn(projectFdn);
            orderAllNodes(projectFdn, nodeFdnList, baseUrl, sessionId);
        } catch (final Exception e) {
            logger.error("Error reading nodes in project {}", projectFdn, e);
            recorder.recordError(CommandLogName.EOI_ORDER_PROJECT.toString(), ErrorSeverity.ERROR, FDN.get(projectFdn).getRdnValue(), projectFdn,
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


    private void orderAllNodes(final String projectFdn, final List<FDN> nodeFdnList, final String baseUrl, final String sessionId) {
        final String userId = getUserIdContext();
        int successfulOrders = 0;
        int failedOrders = 0;
        int invalidOrders = 0;

        for (final FDN nodeFdn : nodeFdnList) {
            final String nodeName = nodeFdn.getRdnValue();
            try {
                eoiOrderNodeUseCase.execute(nodeFdn.toString(), baseUrl, sessionId);
                setUserIdContext(userId);
                successfulOrders++;
            } catch (final InvalidNodeStateException e) {
                invalidOrders++;
                logger.error("Node {} is not in valid state to be ordered: [{}]", nodeFdn, e.getMessage(), e);
                recorder.recordError(CommandLogName.EOI_ORDER_PROJECT.toString(), ErrorSeverity.ERROR, nodeName, nodeFdn.toString(),
                    e.getMessage());
            } catch (final Exception e) {
                failedOrders++;
                logger.error("Error ordering node {}", nodeFdn, e);
                recorder.recordError(CommandLogName.EOI_ORDER_PROJECT.toString(), ErrorSeverity.ERROR, nodeName, nodeFdn.toString(),
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
