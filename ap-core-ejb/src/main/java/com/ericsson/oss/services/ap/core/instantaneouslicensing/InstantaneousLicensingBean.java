/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.instantaneouslicensing;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.exception.InstantaneousLicensingRestServiceException;
import com.ericsson.oss.services.ap.api.workflow.InstantaneousLicensingService;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.InstantaneousLicensingMessage;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.InstantaneousLicensingRestClient;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.RetrieveLicenseStatusResponseDto;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseFileManagerService;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
/**
 * {@inheritDoc}
 */
@Stateless
@Asynchronous
@EService
public class InstantaneousLicensingBean implements InstantaneousLicensingService {

    @Inject
    private InstantaneousLicensingRestClient restClient;

    @Inject
    private Logger logger;

    @Inject
    private ContextService contextService;

    @EServiceRef
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @EServiceRef
    private LicenseFileManagerService licenseFileManagerService;

    @Override
    public void requestLicense(final String userId, final String apNodeFdn, final String requestId) {
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();

        try {
            if (StringUtils.isBlank(requestId)) {
                final String createdRequestId = restClient.createLicenseRequest(apNodeFdn).getRequestId();
                logger.info("Create license request is successful. Job name for license request is {}", createdRequestId);
                resumeWorkflow(nodeName, InstantaneousLicensingMessage.getRunningMessage(), StringUtils.EMPTY, "License requested with job name " + createdRequestId, createdRequestId);
            } else {
                getLicenseRequestStatus(nodeName, requestId);
            }
        } catch (final InstantaneousLicensingRestServiceException e) {
            logger.error("Failure requesting Instantaneous Licensing for node {}. Correlating INSTANTANEOUS_LICENSING_FAILED due to exception: {}", nodeName, e.getMessage());
            resumeWorkflow(nodeName, InstantaneousLicensingMessage.getFailedMessage(), StringUtils.EMPTY, e.getMessage(), StringUtils.EMPTY);
        } catch (final Exception e) {
            logger.error("Failure requesting Instantaneous Licensing for node {} with unexpected exception. Correlating INSTANTANEOUS_LICENSING_FAILED due to exception: {}", nodeName, e.getMessage());
            resumeWorkflow(nodeName, InstantaneousLicensingMessage.getFailedMessage(), StringUtils.EMPTY, "Error during Retrieve License: " + e.getMessage(), StringUtils.EMPTY);
        }
    }

    /**
     * Request license status with a given requestId
     * Status will be handled by reading the state param
     * For COMPLETED state scenario, additional information should return as an empty string, otherwise the additional info from the response is used
     *
     * @param nodeName
     *              name of the node
     * @param requestId
     *              id for the license request
     */
    private void getLicenseRequestStatus(final String nodeName, final String requestId) {
        final RetrieveLicenseStatusResponseDto response = restClient.getLicenseRequestStatus(requestId);
        switch (response.getState()) {
            case "RUNNING":
                resumeWorkflow(nodeName, InstantaneousLicensingMessage.getRunningMessage(), StringUtils.EMPTY, response.getAdditionalInfo(), requestId);
                break;
            case "COMPLETED":
                final String additionalInfo = response.getResult().equals("SUCCESS") ? StringUtils.EMPTY : response.getAdditionalInfo();
                resumeWorkflow(nodeName, InstantaneousLicensingMessage.getCompletedMessage(), response.getResult(), additionalInfo, StringUtils.EMPTY);
                break;
            default:
                resumeWorkflow(nodeName, InstantaneousLicensingMessage.getFailedMessage(), StringUtils.EMPTY, response.getAdditionalInfo(), StringUtils.EMPTY);
        }
    }

    private void resumeWorkflow(final String nodeName, final String workflowMessage, final String result, final String additionalInformation, final String requestId) {
        try {
            final Map<String, Object> correlationVariables = new HashMap<>();
            correlationVariables.put(InstantaneousLicensingMessage.getResultKey(), result);
            correlationVariables.put(InstantaneousLicensingMessage.getAdditionalInfoKey(), additionalInformation);
            correlationVariables.put(InstantaneousLicensingMessage.getRequestId(), requestId);
            correlateMessage(nodeName, workflowMessage, correlationVariables);
        } catch (final WorkflowMessageCorrelationException e) {
            logger.error("Error correlating the {} message for node {}: {}", workflowMessage, nodeName, e.getMessage(), e);
        }
    }

    private void correlateMessage(final String nodeName, final String workflowMessage, final Map<String, Object> correlationVariables)
            throws WorkflowMessageCorrelationException {
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromNodeName(nodeName);
        wfsInstanceService.correlateMessage(workflowMessage, businessKey, correlationVariables);
    }
}

