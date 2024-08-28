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
package com.ericsson.oss.services.ap.core.usecase.delete;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.HealthCheckAttribute;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestClient;

/**
 * Class to delete the NHC report
 */
public class HealthCheckReportDeleter {

    @Inject
    private HealthCheckRestClient healthCheckRestClient;

    @Inject
    private DpsOperations dpsOperations;

    /**
     * Deletes any health check reports generated for the given node.
     * @param nodeFdn
     *              the node fdn
     */
    public void deleteHealthCheckReports(final String nodeFdn){
        final String healthCheckFdn = String.format("%s,HealthCheck=1", nodeFdn);
        final ManagedObject nodeMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(healthCheckFdn);
        if(nodeMo != null){
            final Set<String> reportIds = processHealthCheckReportIds(nodeMo);
            healthCheckRestClient.deleteReport(reportIds);
        }
    }

    private Set<String> processHealthCheckReportIds(final ManagedObject nodeMo){
        final List <String> preReportIds = nodeMo.getAttribute(HealthCheckAttribute.PRE_REPORT_IDS.toString());
        final List <String> postReportIds = nodeMo.getAttribute(HealthCheckAttribute.POST_REPORT_IDS.toString());
        final Set<String> allReportIds = new HashSet<>();
        allReportIds.addAll(preReportIds);
        allReportIds.addAll(postReportIds);
        return allReportIds;
    }
}