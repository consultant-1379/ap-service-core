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

/**
 * ENUM of possible values for node health check status
 */
public enum HealthCheckStatus {

    HEALTHY("healthy"),
    UNDETERMINED("undetermined"),
    UNHEALTHY("unhealthy"),
    WARNING("warning");

    private String status;

    private HealthCheckStatus(final String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }

}
