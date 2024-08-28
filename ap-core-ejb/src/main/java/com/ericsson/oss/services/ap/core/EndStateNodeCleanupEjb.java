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
package com.ericsson.oss.services.ap.core;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.time.LocalDate;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.DeleteNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.UseCaseFactory;

/**
 * Startup bean that handles nodes in completed states that could be deleted.
 * 7 days after reaching a successful end state this bean will initiate a delete node on any eligible nodes.
 * Check for eligible nodes is done nightly.
 */
@EService
@Startup
@Singleton
public class EndStateNodeCleanupEjb {

    private static final String AUTOPROVISIONING_APPLICATION_SYSTEM = "autoprovisioning_application_system";
    private static final String END_STATE_DATE = "endStateDate";
    private static final String STATE = "state";

    private static Set<String> validStates =
        new HashSet<>(Arrays.asList(State.INTEGRATION_COMPLETED.toString(), State.INTEGRATION_COMPLETED_WITH_WARNING.toString(),
            State.INTEGRATION_CANCELLED.toString(), State.ORDER_CANCELLED.toString(), State.HARDWARE_REPLACE_COMPLETED.toString(),
            State.EXPANSION_COMPLETED.toString(), State.EXPANSION_CANCELLED.toString(), State.MIGRATION_COMPLETED.toString(), State.MIGRATION_COMPLETED_WITH_WARNING.toString(),
            State.MIGRATION_CANCELLED.toString(),State.PRE_MIGRATION_CANCELLED.toString(), State.MIGRATION_FAILED.toString(), State.PRE_MIGRATION_FAILED.toString()));

    private boolean mrExecuted = false;

    @Resource
    private TimerService timerService;

    @Inject
    private Logger logger;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private UseCaseFactory useCaseFactory;

    @Inject
    private MRExecutionRecorder mrExecutionRecorder;

    @Inject
    private ContextService contextService;

    @EServiceRef
    private APServiceClusterMember apServiceClusterMember;

    @PostConstruct
    public void init() {
        timerService.createCalendarTimer(scheduleTimer());
    }

    /**
     * Method executed on the Timer Service schedule.
     * Nodes are checked to see if they're eligible for deletion. The delete usecase is kicked off if conditions are met.
     */
    @Timeout
    public void start() {
        if (!apServiceClusterMember.isMasterNode()) {
            logger.debug("Ignoring cleanup nodes call, not master node");
            return;
        }
        executeDelete();
        logMrExecution();
    }

    private ScheduleExpression scheduleTimer(){
        final ScheduleExpression scheduleExpression = new ScheduleExpression();
        scheduleExpression.hour("4");
        scheduleExpression.minute("15");
        scheduleExpression.second("0");
        return scheduleExpression;
    }

    private void executeDelete() {
        logger.info("Executing housekeeping. AP is checking if any nodes are eligible for deletion");
        final Iterator<ManagedObject> validNodeStatusMos = dpsQueries.findMosWithAttribute(END_STATE_DATE, Namespace.AP.toString(), MoType.NODE_STATUS.toString()).execute();
        validNodeStatusMos.forEachRemaining(nodeStatusMo -> {
            if(validStates.contains(nodeStatusMo.getAttribute(STATE)) && checkEndStateDate(nodeStatusMo.getAttribute(END_STATE_DATE))){
                executeDeleteUsecase(nodeStatusMo.getParent());
            }
        });
    }

    private boolean checkEndStateDate(final String endStateDate) {
        if (StringUtils.isBlank(endStateDate)) {
            return false;
        }
        final LocalDate formattedEndStateDate = LocalDate.parse(endStateDate);
        final LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        return sevenDaysAgo.isEqual(formattedEndStateDate) || sevenDaysAgo.isAfter(formattedEndStateDate);
    }

    private void executeDeleteUsecase(final ManagedObject nodeMo) {
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, AUTOPROVISIONING_APPLICATION_SYSTEM);
        try {
            final UseCaseRecorder recorder = new UseCaseRecorder(CommandLogName.DELETE_NODE, UseCaseRecorder.Scope.SINGLE_NODE, FDN.get(nodeMo.getFdn()));
            UseCaseExecutor.execute(recorder, (Callable<Void>) () -> {
                final DeleteNodeUseCase usecase = useCaseFactory.getNamedUsecase(UseCaseName.DELETE_NODE);
                usecase.execute(nodeMo.getFdn(), true);
                recorder.success();
                return null;
            });
            mrExecuted = true;
        } catch (final Exception e) {
            logger.error(String.format("Error while executing delete for node %s by housekeeping", nodeMo.getFdn()), e);
        }
    }

    private void logMrExecution(){
        if (mrExecuted){
            mrExecutionRecorder.recordMRExecution(MRDefinition.AP_AUTOMATIC_HOUSEKEEPING);
        }
        mrExecuted = false;
    }
}
