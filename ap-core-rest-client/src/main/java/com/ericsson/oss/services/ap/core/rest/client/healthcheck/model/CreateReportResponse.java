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
package com.ericsson.oss.services.ap.core.rest.client.healthcheck.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO representing the response from NHC when creating a report
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateReportResponse {

    private String reportName;
    private int statusCode;
    private String message;

    public CreateReportResponse() {
    }

    public CreateReportResponse(final String reportName, final int statusCode, final String message) {
        this.reportName = reportName;
        this.statusCode = statusCode;
        this.message = message;
    }

    public String getReportName() {
        return reportName;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}