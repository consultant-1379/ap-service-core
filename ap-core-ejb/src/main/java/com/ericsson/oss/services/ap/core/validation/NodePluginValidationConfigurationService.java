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
package com.ericsson.oss.services.ap.core.validation;

import static org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.workflow.ValidationConfigurationService;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.NodePluginRestClient;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginResponseHandler;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.factory.ValidationDataFactory;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationData;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResponse;

/**
 * Implementation that contains validations to be performed for the configuration files during the order integration.
 */
@EService
@Stateless
public class NodePluginValidationConfigurationService implements ValidationConfigurationService {

    @Inject
    private NodePluginRestClient nodePluginRestClient;

    @Inject
    private ValidationDataFactory validationDataFactory;

    @Inject
    private NodePluginResponseHandler responseHandler;

    @Inject
    private Logger logger;

    @Inject
    private MRExecutionRecorder recorder;

    @Override
    public String validateConfiguration(final String apNodeFdn, final String nodeType) {
        logger.debug("Validate configuration files for {}", apNodeFdn);
        final ValidationData validationData = validationDataFactory.createValidationData(apNodeFdn, nodeType);
        return processValidation(apNodeFdn, validationData);
    }

    @Override
    public String validateDeltaConfiguration(final String apNodeFdn, final String nodeType) {
        logger.debug("Validate delta configuration files for {}", apNodeFdn);
        final ValidationData validationData = validationDataFactory.createDeltaValidationData(apNodeFdn, nodeType);
        recorder.recordMRExecution(MRDefinition.AP_EXPANSION_NETCONF_VALIDATION);
        return processValidation(apNodeFdn, validationData);
    }

    private String processValidation(final String apNodeFdn, final ValidationData validationData) {
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(validationData, apNodeFdn);
        final int statusCode = validationResponse.getStatusCode();
        if (!validationData.isValidateDelta() && SC_INTERNAL_SERVER_ERROR == statusCode) {
            logger.info("Node Plugin may not be upgraded to support dynamic MoM loading. Re-sending request without upgradePackagePath.");
            validationData.setUpgradePackagePath(null);
            validationResponse = nodePluginRestClient.sendRequest(validationData, apNodeFdn);
        } else {
            recorder.recordMRExecution(MRDefinition.AP_DYNAMIC_MOM_LOADING);
        }
        return responseHandler.createMessage(validationResponse,
               validationData.getPreconfigurationFile() != null ? validationData.getPreconfigurationFile().getFileName() : null);
    }
}
