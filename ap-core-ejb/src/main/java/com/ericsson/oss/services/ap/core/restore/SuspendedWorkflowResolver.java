/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.restore;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID;
import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryParameters.STATE;
import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.State.SUSPENDED;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.wfs.api.query.Query;
import com.ericsson.oss.services.wfs.api.query.QueryBuilderFactory;
import com.ericsson.oss.services.wfs.api.query.QueryType;
import com.ericsson.oss.services.wfs.api.query.Restriction;
import com.ericsson.oss.services.wfs.api.query.WorkflowObject;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;

/**
 * Finds any AP workflows that are suspended.
 */
@Stateless
public class SuspendedWorkflowResolver {

    private static final int MAX_RETRIES = 5;
    private static final String WORKFLOW_INSTANCE_ID_ATTRIBUTE = "workflowInstanceId";

    private int retryIntervalInSeconds = 60;
    private ServiceFinderBean serviceFinder;

    @Inject
    private RetryManager retryManager;

    @Inject
    private DpsQueries dpsQueries;

    @PostConstruct
    public void init() {
        serviceFinder = new ServiceFinderBean();
    }

    /**
     * Attempts to retrieve any suspended parent AP workflows.
     *
     * @return the suspended workflows
     */
    public List<String> getSuspendedWorkflows() {
        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(MAX_RETRIES)
                .waitInterval(retryIntervalInSeconds, TimeUnit.SECONDS)
                .retryOn(IllegalStateException.class)
                .build();

        return retryManager.executeCommand(policy, new RetriableCommand<List<String>>() {

            @Override
            public List<String> execute(final RetryContext arg0) {
                return getSuspendedApWorkflows();
            }
        });
    }

    private List<String> getSuspendedApWorkflows() {
        final List<Object[]> mosAttributes = dpsQueries
                .getMoAttributeValues(Namespace.AP.toString(), MoType.NODE.toString(), ACTIVE_WORKFLOW_INSTANCE_ID.toString()).execute();

        final Set<String> activeWfInstanceIds = new HashSet<>();
        for (final Object attributes : mosAttributes) {
            // moAttributes gets returned as a List<String> in this case, so toString() call on each item in mosAttributes returns String rather than @86xf2 etc.
            activeWfInstanceIds.add(attributes.toString());
        }

        final List<WorkflowObject> wfInstances = getSuspendWorkflowInstances();

        final List<String> suspendedWfInstanceIds = new ArrayList<>(wfInstances.size());
        for (final WorkflowObject wo : wfInstances) {
            if (activeWfInstanceIds.contains(wo.getAttribute(WORKFLOW_INSTANCE_ID_ATTRIBUTE))) {
                suspendedWfInstanceIds.add((String) wo.getAttribute(WORKFLOW_INSTANCE_ID_ATTRIBUTE));
            }
        }

        return suspendedWfInstanceIds;
    }

    private List<WorkflowObject> getSuspendWorkflowInstances() {
        final WorkflowQueryServiceLocal workflowQueryServiceLocal = serviceFinder.find(WorkflowQueryServiceLocal.class);
        final Query query = QueryBuilderFactory.getDefaultQueryBuilder().createTypeQuery(QueryType.WORKFLOW_INSTANCE_QUERY);

        final Restriction allRestrictions = query.getRestrictionBuilder().isEqual(STATE, SUSPENDED);
        query.setRestriction(allRestrictions);

        return workflowQueryServiceLocal.executeQuery(query);
    }

    /**
     * Set the interval in seconds between attempts to retrieve suspended workflows.
     *
     * @param retryInSeconds
     *                  the retry interval
     */
    public void setRetryInterval(final int retryInSeconds) {
        retryIntervalInSeconds = retryInSeconds;
    }
}
