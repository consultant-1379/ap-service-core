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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.HealthCheckAttribute;

/**
 * Class to help carry out read and update actions on the HealthCheck MO.
 */
public class HealthCheckMoHelper {

    @Inject
    private DpsOperations dpsOperations;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Retrieves the name of the health check profile stored on the HealthCheck MO
     *
     * @param apNodeFdn the FDN of the node
     * @return
     */
    protected String getHealthCheckProfileName(final String apNodeFdn) {
        final String healthCheckMoFdn = new StringBuilder().append(apNodeFdn).append(",HealthCheck=1").toString();
        final ManagedObject healthCheckMo = getHealthCheckMo(healthCheckMoFdn);
        if (healthCheckMo == null) {
            throw new NodeNotFoundException(String.format("HealthCheckMo with FDN [%s] could not be found.", healthCheckMoFdn));
        }
        return healthCheckMo.getAttribute(HealthCheckAttribute.HEALTH_CHECK_PROFILE_NAME.getAttributeName());
    }

    /**
     * Updates the HealthCheck MO to store a new report ID.
     * Either the preReportIds or the postReportIds list is updated, depending on the health check phase.
     *
     * @param reportId the health check report ID
     * @param apNodeFdn the FDN of the node
     * @param healthCheckPhase the phase the healthcheck is running in, eg PRE_HEALTHCHECK or POST_HEALTHCHECK
     */
    protected void updateReportIdOnHealthCheckMO(final String reportId, final String apNodeFdn, final String healthCheckPhase) {
        final String healthCheckMoFdn = new StringBuilder().append(apNodeFdn).append(",HealthCheck=1").toString();
        final ManagedObject healthCheckMO = getHealthCheckMo(healthCheckMoFdn);

        final String reportIdsAttributeName = getReportIdsAttributeName(healthCheckPhase);
        final List<String> reportIds = buildUpdatedReportIdsList(reportId, healthCheckMO, reportIdsAttributeName);

        try {
            dpsOperations.updateMo(healthCheckMoFdn, reportIdsAttributeName, reportIds);
        } catch (final Exception e) {
            logger.error(String.format("Error saving health check report ID {} to AP Node MO with FDN {}"), reportId, apNodeFdn);
        }
    }

    private List<String> buildUpdatedReportIdsList(final String reportId, final ManagedObject healthCheckMO, final String reportIdsAttributeName) {
        List<String> reportIds = healthCheckMO.getAttribute(reportIdsAttributeName);
        if (reportIds == null) {
            reportIds = new ArrayList<>();
        }

        reportIds.add(reportId);
        return reportIds;
    }

    private String getReportIdsAttributeName(final String healthCheckPhase) {
        return healthCheckPhase == null || healthCheckPhase.equals("PRE_HEALTHCHECK") ? "preReportIds" : "postReportIds";
    }

    private ManagedObject getHealthCheckMo(final String healthCheckMoFdn){
        return dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(healthCheckMoFdn);
    }
}