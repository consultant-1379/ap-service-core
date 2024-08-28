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
package com.ericsson.oss.services.ap.api.workflow;

/**
 * Interface which interacts with NHC to create health check reports.
 */
public interface HealthCheckService {

    /**
     * This method is responsible for creating a health check report in NHC
     *
     * @param userId
     *            the userId from ContextService to be passed in execution
     * @param apNodeFdn
     *            The FDN of the AP Node
     * @param healthCheckPhase
     *            The phase the health check is being run in. As this is currently only supported for expansion,
     *            supported phases are PRE (pre expansion notification) or POST (post expansion notification)
     */
    void createReport(final String userId, final String apNodeFdn, final String healthCheckPhase);

}
