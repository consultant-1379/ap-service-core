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

import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.DEFAULT_PORT;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.HOST;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.PROTOCOL;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.USERNAME_HEADER;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.model.ApplyAmosFAExecutionResult;
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAReportResult;
import com.google.gson.Gson;

/**
 * Flow Automation Rest Client which is used to sent to POST or GET request to Apply AMOS Script flow.
 */
public class ApplyAmosFARestClient {

    private static final String DEFAULT_URL = "%s://%s:%d%s";
    private static final String FA_SERV_HOSTNAME = "flowautomation-service";
    private static final String EXECUTE_PATH = "/flowautomation/v1/flows/%s/execute";
    private static final String REPORT_PATH = "/flowautomation/v1/executions/%s/report?flow-id=%s";
    private static final String APPLY_AMOS_FLOW_ID = "com.ericsson.oss.services.ap.flows.amos";
    private static final String FLOW_JSON_NAME = "flow-input.json";

    @Inject
    private Logger logger;

    @Inject
    private ApplyAmosFAResponseHandler responseHandler;

    /**
     * Send POST request to Flow Automation Service to execute Apply AMOS Script flow
     *
     * @param userName
     *            the name of user
     * @param executionName
     *            the name of execution flow
     * @param nodeName
     *            the name of node where the AMOS script to be applied
     * @param amosScriptName
     *            the name of the AMOS script to be applied
     * @param amosScriptContents
     *            the content of AMOS script to be applied
     * @param ignoreError
     *            if import error for AMOS script is ignored
     * @return the flow execution result
     *
     */
    public ApplyAmosFAExecutionResult execute(final String userName, final String executionName, final String nodeName, final String amosScriptName, final String amosScriptContents, final boolean ignoreError) {
        HttpResponse httpResponse = null;
        final ApplyAmosFAExecutionResult executionResult = new ApplyAmosFAExecutionResult();

        try {
            final HttpPost httpPost = new HttpPost(getFullUrl(getDefaultExecuteUrl()));
            final HttpEntity httpEntity = buildExecuteRequest(executionName, nodeName, amosScriptName, amosScriptContents, ignoreError).build();
            httpPost.setEntity(httpEntity);
            httpPost.setHeader(USERNAME_HEADER, userName);
            httpPost.setHeader(HOST, FA_SERV_HOSTNAME);

            final HttpClient client = HttpClients.createDefault();
            httpResponse = client.execute(httpPost);
            responseHandler.handleExecuteResponse(executionResult, httpResponse);
        } catch (final Exception e) {
            logger.error("Failed to start flow {}. ", executionName, e);
            executionResult.setErrorMessage("Failed to start flow. " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }

        return executionResult;
    }

    /**
     * Send GET request to Flow Automation Service to get the report of the Apply AMOS Script flow
     *
     * @param userName
     *            the name of user
     * @param executionName
     *            the name of execution flow
     * @return the flow report result
     *
     */
    public ApplyAmosFAReportResult report(final String userName, final String executionName) {
        final ApplyAmosFAReportResult reportResult = new ApplyAmosFAReportResult();
        HttpResponse httpResponse = null;

        try {
            final HttpGet httpGet = new HttpGet(getFullUrl(getDefaultReportUrl(executionName)));
            httpGet.setHeader(USERNAME_HEADER, userName);
            httpGet.setHeader(HOST, FA_SERV_HOSTNAME);

            final HttpClient client = HttpClients.createDefault();
            httpResponse = client.execute(httpGet);

            responseHandler.handleReportResponse(reportResult, httpResponse, executionName);
        } catch (final Exception e) {
            logger.error("Failed to retrieve report {}. ", executionName, e);
            reportResult.setErrorMessage("Failed to retrieve report. " + e.getMessage());
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }

        return reportResult;
    }

    private MultipartEntityBuilder buildExecuteRequest(final String executionName, final String nodeName, final String amosScriptName, final String amosScriptContents, final boolean ignoreError) throws Exception {
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setBoundary(String.valueOf(System.currentTimeMillis()));

        final FormBodyPart namePart = FormBodyPartBuilder.create().setName("name").setBody(new StringBody(executionName, ContentType.create(ContentType.TEXT_PLAIN.getMimeType()))).build();
        multipartEntityBuilder.addPart(namePart);

        final FormBodyPart fileNamePart = FormBodyPartBuilder.create().setName("file-name").setBody(new StringBody(FLOW_JSON_NAME, ContentType.create(ContentType.TEXT_PLAIN.getMimeType()))).build();
        multipartEntityBuilder.addPart(fileNamePart);

        final AmosExecuteRequestBody requestBody = new AmosExecuteRequestBody(nodeName, amosScriptName, amosScriptContents, ignoreError);
        requestBody.generateToken();
        multipartEntityBuilder.addBinaryBody(FLOW_JSON_NAME.split("\\.")[0], new Gson().toJson(requestBody).getBytes(),
                ContentType.TEXT_PLAIN, FLOW_JSON_NAME);
        return multipartEntityBuilder;
    }

    private String getFullUrl(final String defaultUrl) {
        return System.getProperty("INTERNAL_URL", defaultUrl);
    }

    private String getDefaultExecuteUrl() {
        final String path = String.format(EXECUTE_PATH, APPLY_AMOS_FLOW_ID);
        return String.format(DEFAULT_URL, PROTOCOL, FA_SERV_HOSTNAME, DEFAULT_PORT, path);
    }

    private String getDefaultReportUrl(final String executionName) {
        final String path = String.format(REPORT_PATH, executionName, APPLY_AMOS_FLOW_ID);
        return String.format(DEFAULT_URL, PROTOCOL, FA_SERV_HOSTNAME, DEFAULT_PORT, path);
    }
}
