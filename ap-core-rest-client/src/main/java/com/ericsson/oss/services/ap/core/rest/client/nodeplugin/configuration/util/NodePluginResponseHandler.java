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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.HttpTimeoutException;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationDetails;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResponse;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResults;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the response received from Node Plugin and formats it.
 */
public class NodePluginResponseHandler {

    private static final String EMPTY_STRING = "";

    private static final String SPACE_STRING = " ";

    private static final String NEW_LINE_CHARACTER = "\n";

    private static final String SUCCESS = "SUCCESS";

    private static final String WARNING = "WARNING";

    private static final String FAILED = "FAILED";

    private static final String ERROR = "ERROR";

    private static final String FILE_DELIMITER_STRING = " *** ";

    private static final String SEMICOLON_STRING = ";";

    private static final String NODE_PLUGIN_VALIDATION_ADDITIONAL_INFO = "File: %s, Validation Result: %s, Errors : ";

    private static final String NODE_PLUGIN_VALIDATION_FAILURE_MESSAGE = "Unable to perform additional validation on NETCONF files";

    private static final String NODE_PLUGIN_VALIDATION_TIMEOUT_MESSAGE = "Failed to validate NETCONF files. Timeout. See troubleshooting guide for more information.";

    private static final String NODE_PLUGIN_VALIDATION_UNKNOWN_STATUS = "Failure to validate the configuration files (NETCONF). "
        + "Please check the logs for additional information. Validation status received : %s";

    private static final String NODE_PLUGIN_VALIDATION_RESPONSE_STATUS = "Node Plugin Validation Response Status: {} ";

    private static final String RECORDER_NODE_PLUGIN_RESPONSE_BODY = "Node Plugin Validation Response Body: %s";

    private static final String RECORDER_NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE = "Node Plugin Validation Response Status: %s, Validation Result: %s";

    private static final String NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE = "Node Plugin Validation Response Status: {}, Validation Result: {} ";

    private static final String DEFAULT_NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE = "Node Plugin Validation Response: {}";

    private static final String NODE_PLUGIN_PING_FAILURE_MESSAGE = "Unable to get capability information from node plugin";

    private static final String NODE_PLUGIN_PING_RESPONSE_STATUS = "Node Plugin Ping Response Status: {} ";

    private static ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(NodePluginResponseHandler.class);

    @Inject
    private SystemRecorder recorder; // NOPMD

    /**
     * Preparing the message for the Additional info from Node Plugin response.
     *
     * @param response
     *              the response to put in the message
     *
     * @return the response from Node Plugin formatted
     *
     */
    public String createMessage(final ValidationResponse response, final String ignoreWarningFile) {
        final String status = response.getStatus();
        final String warningAndErrorMessages;

        switch (status) {
            case SUCCESS :
                return EMPTY_STRING;
            case WARNING :
            case FAILED:
                warningAndErrorMessages = collectErrors(response, status, ignoreWarningFile);
                break;
            case ERROR:
                throw new ApApplicationException(response.getMessage());
            default:
                throw new ApApplicationException(String.format(NODE_PLUGIN_VALIDATION_UNKNOWN_STATUS, status));
        }
        return warningAndErrorMessages;
    }

    /**
     * Handles the HTTP response retrieved from rest call to Node Plugin.
     *
     * @param httpResponse
     *             the http response
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return the ValidationResponse
     *
     */
    public ValidationResponse handleResponse(final HttpResponse httpResponse, final String apNodeFdn) throws IOException {
        ValidationResponse validationResponse = new ValidationResponse();
        if (httpResponse != null) {
            logger.debug(NODE_PLUGIN_VALIDATION_RESPONSE_STATUS, httpResponse.getStatusLine());

            int httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            final String responseString = EntityUtils.toString(httpResponse.getEntity());
            recorder.recordEvent(String.format(RECORDER_NODE_PLUGIN_RESPONSE_BODY, responseString), EventLevel.DETAILED, apNodeFdn, "", "");
            if (HttpStatus.SC_OK == httpResponseCode) {
                validationResponse = objectMapper.readValue(responseString, ValidationResponse.class);
                validationResultLogLevel(httpResponse, validationResponse);
            } else if (HttpStatus.SC_NOT_FOUND == httpResponseCode && hasJsonType(httpResponse)) {
                validationResponseConvert(validationResponse, responseString);
            } else if (HttpStatus.SC_INTERNAL_SERVER_ERROR == httpResponseCode) {
                validationResponse.setStatus(FAILED);
                logger.error(NODE_PLUGIN_VALIDATION_RESPONSE_STATUS, httpResponse.getStatusLine());
            } else if (HttpStatus.SC_GATEWAY_TIMEOUT == httpResponseCode) {
                validationResponse.setStatus(FAILED);
                logger.error(NODE_PLUGIN_VALIDATION_RESPONSE_STATUS, httpResponse.getStatusLine());
                throw new HttpTimeoutException(NODE_PLUGIN_VALIDATION_TIMEOUT_MESSAGE);
            } else {
                validationResponse.setStatus(FAILED);
                logger.error(NODE_PLUGIN_VALIDATION_RESPONSE_STATUS, httpResponse.getStatusLine());
                throw new ApApplicationException(NODE_PLUGIN_VALIDATION_FAILURE_MESSAGE);
            }
            validationResponse.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        }
        return validationResponse;
    }

    /**
     * Handles the HTTP response retrieved from ping request to Node Plugin.
     *
     * @param httpResponse
     *             the http response
     * @return a list of NodePluginCapability
     *
     */
    public List<NodePluginCapability> handlePingResponse(final HttpResponse httpResponse) {

        if (httpResponse != null) {
            logger.debug(NODE_PLUGIN_PING_RESPONSE_STATUS, httpResponse.getStatusLine());
            try {
                final int httpResponseCode = httpResponse.getStatusLine().getStatusCode();

                if (HttpStatus.SC_OK == httpResponseCode) {
                    final JsonParser parser = new JsonParser();
                    final Gson gson = new Gson();
                    final List<NodePluginCapability> capabilityList = new ArrayList<>();
                    final JsonArray jsonArray = parser.parse(EntityUtils.toString(httpResponse.getEntity())).getAsJsonArray();
                    for (final JsonElement capability : jsonArray) {
                        final NodePluginCapability capabilityBean = gson.fromJson(capability, NodePluginCapability.class);
                        capabilityList.add(capabilityBean);
                    }
                    return capabilityList;
                } else {
                    logger.error(NODE_PLUGIN_PING_RESPONSE_STATUS, httpResponse.getStatusLine());
                    throw new ApApplicationException(NODE_PLUGIN_PING_FAILURE_MESSAGE);
                }
            } catch (final Exception e) {
                logger.error("Exception: {}", e.getMessage(), e);
                throw new ApApplicationException(NODE_PLUGIN_PING_FAILURE_MESSAGE, e);
            }
        }
        return Collections.emptyList();
    }

    private String collectErrors(final ValidationResponse response, final String status, final String ignoreWarningFile) {

        final List<ValidationResults> validationResultsList = response.getValidationResults();
        final StringBuilder warningAndErrorMessages = new StringBuilder();

        if (CollectionUtils.isEmpty(validationResultsList)) {
            throw new ApApplicationException(NODE_PLUGIN_VALIDATION_FAILURE_MESSAGE);
        }
        String fileDelimiter = EMPTY_STRING;
        for (ValidationResults validationResults : validationResultsList) {

            final String result = validationResults.getResult();
            final String configurationName = validationResults.getConfigurationName();
            if (isRecordRequired(result, configurationName, ignoreWarningFile)) {
                warningAndErrorMessages.append(fileDelimiter).append(String.format(NODE_PLUGIN_VALIDATION_ADDITIONAL_INFO, configurationName, result)).append(NEW_LINE_CHARACTER);
                if (CollectionUtils.isEmpty(validationResults.getValidationDetails())) {
                    throw new ApApplicationException(NODE_PLUGIN_VALIDATION_FAILURE_MESSAGE);
                }
                fileDelimiter = FILE_DELIMITER_STRING + SPACE_STRING;
                String errorDelimiter = EMPTY_STRING;
                int validationDetailsCount = validationResults.getValidationDetails().size();

                if (validationDetailsCount > 1){
                    errorDelimiter =  SEMICOLON_STRING;
                }

                for (ValidationDetails details : validationResults.getValidationDetails()) {
                    warningAndErrorMessages.append(details.getValidationMessage().trim()).append(errorDelimiter).append(SPACE_STRING).append(NEW_LINE_CHARACTER);
                    errorDelimiter = formatErrorDelimiter(validationDetailsCount);
                    validationDetailsCount --;
                }
            }

        }
        if (FAILED.equals(status)){
            throw new ApApplicationException(warningAndErrorMessages.toString());
        }

        return warningAndErrorMessages.toString();
    }

    private boolean isRecordRequired(final String result, final String configurationName, final String ignoreWarningFile) {
        return WARNING.equals(result) && !configurationName.equals(ignoreWarningFile) || FAILED.equals(result);
    }

    private String formatErrorDelimiter(int validationDetailsCount) {

        if (validationDetailsCount > 2) {
            return SEMICOLON_STRING + SPACE_STRING;
        }
        return EMPTY_STRING;
    }

    private void validationResultLogLevel(final HttpResponse httpResponse, final ValidationResponse validationResponse){

        final String response = validationResponse.getStatus();
        switch (response) {
            case SUCCESS:
                recorder.recordEvent(String.format(RECORDER_NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE, httpResponse.getStatusLine(), validationResponse.getStatus()), EventLevel.DETAILED, EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
                break;
            case WARNING:
                logger.warn(NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE, httpResponse.getStatusLine(), validationResponse.getStatus());
                break;
            case FAILED:
                logger.error(NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE, httpResponse.getStatusLine(), validationResponse.getStatus());
                break;
            default:
                logger.error(DEFAULT_NODE_PLUGIN_VALIDATION_RESPONSE_MESSAGE, response);
        }
    }

    private boolean hasJsonType(final HttpResponse httpResponse){
        return getContentType(httpResponse).equals(MediaType.APPLICATION_JSON);
    }

    private String getContentType(final HttpResponse httpResponse) {
        HttpEntity entity = httpResponse.getEntity();
        ContentType contentType = ContentType.getOrDefault(entity);
        return contentType.getMimeType();
    }

    private void validationResponseConvert(final ValidationResponse  validationResponse,
        final String responseString) throws IOException {
        ErrorResponse validationErrorResponse = objectMapper.readValue(responseString, ErrorResponse.class);
        validationResponse.setStatusCode(validationErrorResponse.getDetails().getHttpResponseCode());
        validationResponse.setMessage(validationErrorResponse.getDetails().getDescription());
        validationResponse.setStatus(ERROR);
    }
}
