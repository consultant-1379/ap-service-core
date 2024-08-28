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
 * POJO for health status counts of the node
 */
public class HealthStatusCounts {

    private Integer undeterminedNodes;
    private Integer healthyNodes;
    private Integer unhealthyNodes;
    private Integer warningNodes;

    public HealthStatusCounts() {

    }

    public HealthStatusCounts(final Integer undeterminedNodes, final Integer healthyNodes, final Integer unhealthyNodes, final Integer warningNodes) {
        this.undeterminedNodes = undeterminedNodes;
        this.healthyNodes = healthyNodes;
        this.unhealthyNodes = unhealthyNodes;
        this.warningNodes = warningNodes;
    }

    public Integer getUndeterminedNodes() {
        return undeterminedNodes;
    }

    public void setUndeterminedNodes(final Integer undeterminedNodes) {
        this.undeterminedNodes = undeterminedNodes;
    }

    public Integer getHealthyNodes() {
        return healthyNodes;
    }

    public void setHealthyNodes(final Integer healthyNodes) {
        this.healthyNodes = healthyNodes;
    }

    public Integer getUnhealthyNodes() {
        return unhealthyNodes;
    }

    public void setUnhealthyNodes(final Integer unhealthyNodes) {
        this.unhealthyNodes = unhealthyNodes;
    }

    public Integer getWarningNodes() {
        return warningNodes;
    }

    public void setWarningNodes(final Integer warningNodes) {
        this.warningNodes = warningNodes;
    }

}
