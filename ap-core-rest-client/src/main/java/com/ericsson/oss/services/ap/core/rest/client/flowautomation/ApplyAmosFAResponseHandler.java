/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.flowautomation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.model.ApplyAmosFAExecutionResult;
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAReportResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the response received from Flow Automation service and formats it.
 */
public class ApplyAmosFAResponseHandler {

    private static final String ADDITIONAL_INFORMATION = "additionalInformation";
    private static final String BODY = "body";
    private static final String COMPLETE_REPORT_STATUS = "COMPLETED";
    private static final String CONTEXTUAL_LINK = "contextualLink";
    private static final String HEADER = "header";
    private static final String NAME = "name";
    private static final String REPORT_SUMMARY = "reportSummary";
    private static final String RESULT = "result";
    private static final String STATUS = "status";
    private static final String SUCCESS_REPORT_RESULT = "SUCCESS";

    @Inject
    private Logger logger;

    /**
     * Handles the HTTP response for getting flow automation execution report.
     *
     * @param reportResult
     *            the report result
     * @param httpResponse
     *            the http response
     * @param executionName
     *            the name of execution
     * @throws IOException
     * @throws ParseException
     */
    public void handleReportResponse(final ApplyAmosFAReportResult reportResult, final HttpResponse httpResponse, final String executionName) throws ParseException, IOException {
        if (!hasJsonType(httpResponse)) {
            reportResult.setErrorMessage(httpResponse.getStatusLine().toString());
            return;
        }
        final HttpEntity entity = httpResponse.getEntity();
        String responseString = null;

        if (entity != null) {
            responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }

        final int httpResponseCode = httpResponse.getStatusLine().getStatusCode();

        if (httpResponseCode == HttpStatus.SC_OK) {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode rootNode = mapper.readTree(responseString);
            final JsonNode headerPart = rootNode.get(HEADER);
            final JsonNode bodyPart = rootNode.get(BODY);
            final JsonNode reportSummaryPart = bodyPart.get(REPORT_SUMMARY);

            if (headerPart.get(STATUS).asText().equals(COMPLETE_REPORT_STATUS)) {
                reportResult.setComplete(true);

                final String contextLink = reportSummaryPart.get(CONTEXTUAL_LINK).asText();
                reportResult.setContextLink(contextLink);
                logger.info("Get contextual link from flow execution {}: {}", executionName, contextLink);

                final String additionalInfo = reportSummaryPart.get(ADDITIONAL_INFORMATION).asText();
                if (StringUtils.isNotEmpty(additionalInfo)) {
                    logger.warn("Get additional information from flow execution {}: {}", executionName, additionalInfo);
                }

                if (reportSummaryPart.get(RESULT).asText().equals(SUCCESS_REPORT_RESULT)) {
                    reportResult.setSuccess(true);
                } else {
                    reportResult.setErrorMessage(additionalInfo);
                }
            }
        } else {
            logger.warn("Receive Response for GET Request:\n{}\n{}", httpResponse.getStatusLine(), responseString);

            if ((httpResponseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) || (httpResponseCode == HttpStatus.SC_UNAUTHORIZED)) {
                reportResult.setComplete(true);
                reportResult.setErrorMessage("Failed to retrieve report. " + httpResponse.getStatusLine().toString());
            }
        }
    }

    /**
     * Handles the HTTP response for sending POST request to Flow Automation Service to execute Apply AMOS Script flow
     *
     * @param executionResult
     *            the execution response result
     * @param httpResponse
     *            the http response
     * @throws IOException
     */
    public void handleExecuteResponse(final ApplyAmosFAExecutionResult executionResult, final HttpResponse httpResponse) throws IOException {
        final int httpResponseCode = httpResponse.getStatusLine().getStatusCode();
        final HttpEntity entity = httpResponse.getEntity();
        String responseString = null;

        if (entity != null) {
            responseString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }

        if (httpResponseCode == HttpStatus.SC_OK) {
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode rootNode = mapper.readTree(responseString);
            executionResult.setFlowExecutionName(rootNode.get(NAME).asText());
            executionResult.setSuccess(true);
        } else {
            logger.warn("Receive Response for POST Request:\n{}\n{}", httpResponse.getStatusLine(), responseString);
            executionResult.setErrorMessage("Failed to start flow. " + httpResponse.getStatusLine().toString());
        }
    }

    private boolean hasJsonType(final HttpResponse httpResponse) {
        return getContentType(httpResponse).equals(MediaType.APPLICATION_JSON);
    }

    private String getContentType(final HttpResponse httpResponse) {
        final HttpEntity entity = httpResponse.getEntity();
        final ContentType contentType = ContentType.getOrDefault(entity);
        return contentType.getMimeType();
    }
}
