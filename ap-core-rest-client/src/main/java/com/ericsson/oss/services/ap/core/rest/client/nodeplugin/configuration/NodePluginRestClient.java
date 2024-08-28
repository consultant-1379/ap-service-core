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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration;

import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.HttpTimeoutException;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationData;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResponse;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginResponseHandler;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginCapability;
import com.ericsson.oss.services.ap.core.rest.client.RestUrls;

/**
 * REST client used to interact with Node Plugin REST interface.
 */
public class NodePluginRestClient {

    @Inject
    private NodePluginResponseHandler responseHandler;

    private final HttpClient client;

    private final Logger logger = LoggerFactory.getLogger(NodePluginRestClient.class);

    public NodePluginRestClient() {
        client = HttpClients.createDefault();
    }

    @Inject
    private NodePluginRequestBuilder nodePluginRequestBuilder;

    /**
     * Sends the POST multi-part request towards the Node Plugin REST interface.
     *
     * @param validationData
     *            the data to be validated
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return a response with the results of the validation
     */
    public ValidationResponse sendRequest(final ValidationData validationData, final String apNodeFdn) {
        HttpResponse httpResponse = null;
        ValidationResponse validationResponse = null;
        try {
            final HttpEntity httpEntity = nodePluginRequestBuilder.buildNodePluginRequest(validationData).build();
            final String uri = (validationData.isValidateDelta() ? RestUrls.NODE_PLUGIN_DELTA_VALIDATION_SERVICE.getFullUrl() :
                RestUrls.NODE_PLUGIN_VALIDATION_SERVICE.getFullUrl()).replace("{nodeType}", validationData.getNodeType());

            final HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(httpEntity);
            httpPost.setHeader("host", RestUrls.NODE_PLUGIN_VALIDATION_SERVICE.getHost());
            httpResponse = client.execute(httpPost);
            validationResponse = responseHandler.handleResponse(httpResponse, apNodeFdn);
        } catch (final HttpTimeoutException e) {
            logger.error("Exception occurred during validation call : {}", e.getMessage());
            throw new ApApplicationException(e.getMessage(), e);
        } catch (final Exception e) {
            logger.error("Exception occurred during validation call : {}", e.getMessage());
            throw new ApApplicationException("Failed to validate NETCONF files. "
                    + "Please check the logs for additional information.", e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
        return validationResponse;
    }

    /**
     * Get capabilities via the Node Plugin REST Ping interface.
     *
     * @param nodeType
     *            the data to be used for the ping request
     * @return a list of NodePluginCapability
     */
    public List<NodePluginCapability> getCapabilities(final String nodeType) {
        HttpResponse httpResponse = null;
        try {
            final String uri = RestUrls.NODE_PLUGIN_CAPABILITY_SERVICE.getFullUrl().replace("{nodeType}", nodeType);
            final HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("host", RestUrls.NODE_PLUGIN_CAPABILITY_SERVICE.getHost());
            httpResponse = client.execute(httpGet);
            return responseHandler.handlePingResponse(httpResponse);
        } catch (final Exception e) {
            logger.error("Failed to get capability information for {} : {} ",nodeType, e.getMessage());
            return Collections.emptyList();
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }
}
