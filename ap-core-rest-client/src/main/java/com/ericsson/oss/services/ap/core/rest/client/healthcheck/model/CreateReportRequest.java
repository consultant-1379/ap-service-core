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
import java.util.Objects;

/**
 * POJO for the JSON request sent to NHC when creating a health check report.
 */
public class CreateReportRequest {

    private String reportName;
    private List<String> profileNames;
    private List<String> nodeNames;
    private String executionMode;

    public CreateReportRequest() {
    }

    public CreateReportRequest(final String reportName, final List<String> profileNames, final List<String> nodeNames, final String executionMode) {
        this.reportName = reportName;
        this.profileNames = profileNames;
        this.nodeNames = nodeNames;
        this.executionMode = executionMode;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(final String reportName) {
        this.reportName = reportName;
    }

    public List<String> getProfileNames() {
        return profileNames;
    }

    public void setProfileNames(final List<String> profileNames) {
        this.profileNames = profileNames;
    }

    public List<String> getNodeNames() {
        return nodeNames;
    }

    public void setNodeNames(final List<String> nodeNames) {
        this.nodeNames = nodeNames;
    }

    public String getExecutionMode() {
        return executionMode;
    }

    public void setExecutionMode(final String executionMode) {
        this.executionMode = executionMode;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CreateReportRequest that = (CreateReportRequest) o;
        return Objects.equals(reportName, that.reportName) &&
            Objects.equals(profileNames, that.profileNames) &&
            Objects.equals(nodeNames, that.nodeNames) &&
            Objects.equals(executionMode, that.executionMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportName, profileNames, nodeNames, executionMode);
    }

    @Override
    public String toString() {
        return "CreateReportRequest{" +
            "reportName='" + reportName + '\'' +
            ", profileNames='" + profileNames + '\'' +
            ", nodeNames='" + nodeNames + '\'' +
            ", executionMode='" + executionMode + '\'' +
            '}';
    }
}
