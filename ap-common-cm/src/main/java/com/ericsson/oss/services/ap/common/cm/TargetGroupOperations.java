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
package com.ericsson.oss.services.ap.common.cm;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.services.security.genericidentitymgmtserviceapi.exceptions.EntityNotFoundException;
import com.ericsson.oss.services.security.genericidentitymgmtserviceapi.exceptions.InternalLogicException;
import com.ericsson.oss.services.security.genericidentitymgmtserviceapi.exceptions.InternalUnexpectedException;
import com.ericsson.oss.services.security.genericidentitymgmtserviceapi.exceptions.InvalidArgumentException;
import com.ericsson.oss.services.security.genericidentitymgmtserviceapi.targetgroup.TargetGroupManagementInternalService;

/**
 * This class uses {@link TargetGroupManagementInternalService} to add targets to a target group
 */
public class TargetGroupOperations {

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_INTERVAL_IN_SECONDS = 2;

    @Inject
    private RetryManager retryManager;

    @Inject
    TransactionalExecutor transactionalExecutor;

    private TargetGroupManagementInternalService targetGroupManagementInternalService;
    /**
     * Assigns a list of targets to a Target Group
     * @param targetNames
     *         list of target names to be added to the target group
     * @param targetGroupName
     *         name of the target group
     */
    public void addTargetsToTargetGroup(final List<String> targetNames, final List<String> targetGroupName) {

        final Callable<Void> callable = () -> {
            getTargetGroupManagementInternalService().addTargetsToTargetGroups(targetNames, targetGroupName);
            return null;
        };
        final RetryPolicy policy = RetryPolicy.builder()
            .attempts(MAX_RETRIES)
            .waitInterval(RETRY_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
            .retryOn(EntityNotFoundException.class, InternalLogicException.class, InternalUnexpectedException.class, InvalidArgumentException.class)
            .build();

        retryManager.executeCommand(policy, (final RetryContext retryContext) -> {
            transactionalExecutor.execute(callable);
            return null;
        });
    }

    private TargetGroupManagementInternalService getTargetGroupManagementInternalService() {
        if (targetGroupManagementInternalService == null) {
            targetGroupManagementInternalService = new ServiceFinderBean().find(TargetGroupManagementInternalService.class);
        }
        return targetGroupManagementInternalService;
    }
}
