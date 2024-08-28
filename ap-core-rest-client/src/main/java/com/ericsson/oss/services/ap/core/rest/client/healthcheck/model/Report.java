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
 * A POJO representing the report received from the Health Check Service.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Report {

    private long mainReportId;
    private String reportName;
    private String status;
    private HealthStatusCounts healthStatusCounts;

    public Report() {
    }

    public Report(final long mainReportId, final String reportName, final String status, final HealthStatusCounts healthStatusCounts) {
        this.mainReportId = mainReportId;
        this.reportName = reportName;
        this.status = status;
        this.healthStatusCounts = healthStatusCounts;
    }

    public long getMainReportId() {
        return mainReportId;
    }

    public String getReportName() {
        return reportName;
    }

    public HealthStatusCounts getHealthStatusCounts() {
        return healthStatusCounts;
    }

    public String getStatus() {
        return status;
    }

}
