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
package com.ericsson.oss.services.ap.core.healthcheck;

import java.io.Serializable;

/**
 * Holds Health Check data to be passed to Timer Service for polling the status of an
 * ongoing Asynchronous Health Check
 */
public class HealthCheckData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String nodeFdn;
    private String healthCheckPhase;

    /**
     * Constructor to build data needed for the Health Check
     *
     * @param userId the ID of the user who ran the usecase
     * @param nodeFdn the FDN of the node
     * @param healthCheckPhase the phase the Health Check is run in
     */
    public HealthCheckData(final String userId, final String nodeFdn, final String healthCheckPhase) {
        this.userId = userId;
        this.nodeFdn = nodeFdn;
        this.healthCheckPhase = healthCheckPhase;
    }

    /**
     * Gets the User Id
     *
     * @return the User Id
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the Node FDN
     *
     * @return the Node FDN
     */
    public String getNodeFdn() {
        return nodeFdn;
    }

    /**
     * Gets the Health Check phase
     *
     * @return the Health Check phase
     */
    public String getHealthCheckPhase() {
        return healthCheckPhase;
    }
}
