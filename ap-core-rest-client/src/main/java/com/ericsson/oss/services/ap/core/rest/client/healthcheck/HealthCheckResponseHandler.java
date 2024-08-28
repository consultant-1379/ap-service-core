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
package com.ericsson.oss.services.ap.core.rest.client.healthcheck;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.HealthCheckProfileNotFoundException;
import com.ericsson.oss.services.ap.api.exception.HealthCheckRestServiceException;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.CreateReportResponse;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.ProfileDetails;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.Report;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.ViewReportResponse;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class is responsible for handling responses from the Health Check Service.
 */
public class HealthCheckResponseHandler {

    private static final String COMPLETED = "COMPLETED";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles the create Response from the Health Check Service
     *
     * @param httpResponse
     *            the HTTPResponse received from NHC
     * @return the name of the generated report
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public String processCreateResponse(final HttpResponse httpResponse) throws JsonParseException, JsonMappingException, IOException {
        String response = "";
        String errorMessage = "";
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_CREATED:
                response = EntityUtils.toString(httpResponse.getEntity());
                final String reportName = getReportName(response);
                logger.info("Successfully created health check report");
                return reportName;
            case HttpStatus.SC_BAD_REQUEST:
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            case HttpStatus.SC_CONFLICT:
                response = EntityUtils.toString(httpResponse.getEntity());
                errorMessage = getErrorDetail(response);
                throw new HealthCheckRestServiceException(errorMessage);
            default:
                response = EntityUtils.toString(httpResponse.getEntity());
                throw new HealthCheckRestServiceException();
        }
    }

    /**
     * Handles the delete report Response from the Health Check Service
     *
     * @param httpResponse
     *           The HTTPResponse received from NHC
     * @throws JsonParseException
     *             This exception is raised if there is a serious issue that occurs during parsing of a Json string.
     * @throws JsonMappingException
     *             Checked exception used to signal fatal problems with mapping of content, distinct from low-level I/O problems
     * @throws IOException
     *             Signals that an I/O exception of some sort has occurred.
     */
    public void processDeleteReportResponse(final HttpResponse httpResponse) throws JsonParseException, JsonMappingException, IOException {
        final String response = EntityUtils.toString(httpResponse.getEntity());
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_OK:
                logger.info("Delete NHC report(s): status: {} message: {} ", statusCode, response);
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                logger.warn("Delete NHC report(s): status: {} message: {} ", statusCode, response);
                break;
            default:
                throw new ApApplicationException("Problem encountered while deleting NHC report(s). Status Code: " + statusCode);
        }
    }

    /**
     * Handles the view response when executed with the report name.
     *
     * @param httpResponse
     *            the HTTPResponse received from NHC
     * @return The mainReportId of the generated report
     * @throws IOException
     * @throws ParseException
     */
    public String processViewByNameResponse(final HttpResponse httpResponse) throws ParseException, IOException {
        String response;
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_OK:
                return extractMainReportId(httpResponse);
            case HttpStatus.SC_BAD_REQUEST:
                response = EntityUtils.toString(httpResponse.getEntity());
                logger.error(response);
                throw new ApApplicationException(response);
            case HttpStatus.SC_NOT_FOUND:
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                response = EntityUtils.toString(httpResponse.getEntity());
                logger.error(response);
                throw new HealthCheckRestServiceException(response);
            default:
                throw new ApApplicationException("Unable to process response when viewing report status using reportName");
        }
    }

    /**
     * Handles the view response when executed with the main report ID
     *
     * @param httpResponse
     *            the HTTPResponse received from NHC
     * @return the generated {@link Report}
     * @throws ParseException
     * @throws IOException
     */
    public Report handleViewByIdResponse(final HttpResponse httpResponse) throws ParseException, IOException {
        String responseString;
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_OK:
                responseString = EntityUtils.toString(httpResponse.getEntity());
                return getReportDetail(responseString);
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                responseString = EntityUtils.toString(httpResponse.getEntity());
                throw new HealthCheckRestServiceException(responseString);
            case HttpStatus.SC_NOT_FOUND:
            case HttpStatus.SC_BAD_REQUEST:
                responseString = EntityUtils.toString(httpResponse.getEntity());
                throw new ApApplicationException(responseString);
            default:
                logger.error("Unable to process response from NHC");
                throw new ApApplicationException("Unable to process NHC response when viewing report status using mainReportID");
        }
    }

    /**
     * Handles the response for get profile details
     *
     * @param httpResponse
     *            the HTTP Response received from NHC
     * @return the profile details {@link ProfileDetails}
     * @throws IOException
     * @throws HealthCheckProfileNotFoundException
     * @throws HealthCheckRestServiceException
     */
    public ProfileDetails handleGetProfileDetailsResponse(final HttpResponse httpResponse) throws IOException {
        String responseString;
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_OK:
                responseString = EntityUtils.toString(httpResponse.getEntity());
                return objectMapper.readValue(responseString, ProfileDetails.class);
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                responseString = EntityUtils.toString(httpResponse.getEntity());
                throw new HealthCheckRestServiceException(responseString);
            case HttpStatus.SC_NOT_FOUND:
                responseString = EntityUtils.toString(httpResponse.getEntity());
                throw new HealthCheckProfileNotFoundException(responseString);
            default:
                logger.error("Unable to process response from NHC");
                throw new HealthCheckRestServiceException("Unable to process NHC response while getting health check profile details");
        }
    }

    private String getReportName(final String responseString) throws JsonParseException, JsonMappingException, IOException {
        final List<CreateReportResponse> myObjects = Arrays.asList(objectMapper.readValue(responseString, CreateReportResponse[].class));
        return myObjects.get(0).getReportName();
    }

    private Report getReportDetail(final String responseString) throws JsonParseException, JsonMappingException, IOException {
        final Report report = objectMapper.readValue(responseString, Report.class);
        if (report != null && report.getStatus().equals(COMPLETED)) {
            return report;
        } else {
            logger.warn("Report is null or status is not completed");
            throw new HealthCheckRestServiceException();
        }
    }

    private String getErrorDetail(final String responseString) throws JsonParseException, JsonMappingException, IOException {
        final List<CreateReportResponse> myObjects = Arrays.asList(objectMapper.readValue(responseString, CreateReportResponse[].class));
        return myObjects.get(0).getMessage();
    }

    private String extractMainReportId(final HttpResponse httpResponse) throws IOException, JsonParseException, JsonMappingException {
        final String response = EntityUtils.toString(httpResponse.getEntity());
        final Report report = getReport(response);
        final long reportId = report.getMainReportId();
        if (String.valueOf(reportId) == null) {
            logger.warn("MainReportId not found in JSON response");
            throw new HealthCheckRestServiceException("MainReportId not found");
        } else {
            logger.info("Successfully retrieved mainReport when extracting ID");
            return String.valueOf(reportId);
        }
    }

    private Report getReport(final String responseString) {
        try {
            final ViewReportResponse report = objectMapper.readValue(responseString, ViewReportResponse.class);
            return report.getReports().get(0);
        } catch (final Exception e) {
            throw new HealthCheckRestServiceException("No reports found from viewById throwing health check exception");
        }
    }
}
