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
package com.ericsson.oss.services.ap.core.rest.model;

/**
 * Defines the overall status summary of a project.
 */
public class ProjectStatusSummary {

    private String projectId;

    private IntegrationPhaseSummary integrationPhaseSummary;

    private int numberOfNodes;

    public ProjectStatusSummary(final String projectId, final IntegrationPhaseSummary integrationPhaseSummary, final int numberOfNodes) {
        this.projectId = projectId;
        this.integrationPhaseSummary = integrationPhaseSummary;
        this.numberOfNodes = numberOfNodes;
    }

    public String getProjectId() {
        return projectId;
    }

    public IntegrationPhaseSummary getIntegrationPhaseSummary() {
        return integrationPhaseSummary;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }
}
