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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO representing the response from NHC from view interface
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViewReportResponse {

    private List<Report> reports;

    public ViewReportResponse() {
    }

    public ViewReportResponse(final List<Report> reports) {
        this.reports = reports;
    }

    public List<Report> getReports() {
        return reports;
    }

}
