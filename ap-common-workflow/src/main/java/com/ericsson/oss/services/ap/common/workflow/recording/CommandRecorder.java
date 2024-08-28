/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.recording;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.context.classic.ContextServiceBean;
import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;

/**
 * Record result of the order, order rollback and integrate usecases to the System Command log.
 */
public class CommandRecorder {

    private static final String INTEGRATE_CANCELLED_ADDITIONAL_INFO = "Integration cancelled by operator";
    private static final String MIGRATE_CANCELLED_ADDITIONAL_INFO = "Migration cancelled by operator";
    private static final String MIGRATE_COMPLETED_WITH_WARNING_ADDITIONAL_INFO = "Migration completed with warnings";
    private static final String INTEGRATE_COMPLETED_WITH_WARNING_ADDITIONAL_INFO = "Integration completed with warnings";
    private static final String VIEW_STATUS_ADDITIONAL_INFO = "Execute command 'ap status -n %s' to view details of failed tasks during the integration";
    private static final String VIEW_STATUS_ADDITIONAL_INFO_FOR_MIGRATION = "Execute command 'ap status -n %s' to view details of failed tasks during the migration";
    private static final String ORDER_SUCCESSFUL_ADDITIONAL_INFO = "Order Node completed for node %s EXECUTION_TIME=%d milliseconds , TOTAL_NODE(S)=%d";
    private static final String PRE_MIGRATION_SUCCESSFUL_ADDITIONAL_INFO = "Pre Migration of Node completed for node %s EXECUTION_TIME=%d milliseconds , TOTAL_NODE(S)=%d";
    private static final String ORDER_FAILED_ADDITIONAL_INFO = "Order rollback completed";
    private static final String PRE_MIGRATION_FAILED_ADDITIONAL_INFO = "Pre Migration failed";
    private static final String ORDER_ROLLBACK_FAILED_ADDITIONAL_INFO = "Order rollback unsuccessful";
    private static final String RECONFIGURATION_COMPLETED_ADDITIONAL_INFO = "Reconfiguration completed, EXECUTION_TIME=%d milliseconds, TOTAL_NODE(S)=%d";
    private static final String RECONFIGURATION_COMPLETED_WITH_WARNINGS_ADDITIONAL_INFO = "Reconfiguration completed with warnings";
    private static final String RECONFIGURATION_CANCELLED_ADDITIONAL_INFO = "Reconfiguration cancelled by operator";
    private static final String CANCELLED_ADDITIONAL_INFO = "Cancelled";

    private static final String ADDITIONAL_INFO_KEY = "ADDITIONAL_INFO";
    private static final String EXECUTION_TIME_MS_KEY = "EXECUTION_TIME_MS";
    private static final String MO_NAME_KEY = "MO_NAME";
    private static final String PHASE_KEY = "PHASE";
    private static final String TRIGGER_TYPE_KEY = "TRIGGER_TYPE";

    private enum SourceTypes {
        ECT,
        PCI,
        OTHER,
        UNKNOWN
    }

    private final SystemRecorder systemRecorder = new SystemRecorderBean();

    /**
     * Records successful integration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void integrationSuccessful(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);

        final long integrationExecutionTime = System.currentTimeMillis() - workflowVariables.getIntegrationStartTime();
        final String additionalInfo = String.format("Integration completed, EXECUTION_TIME=%d milliseconds, TOTAL_NODE(S)=%d",
                integrationExecutionTime, 1);
        systemRecorder.recordCommand(CommandLogName.INTEGRATE.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records migration completed with warnings
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void migrationCompletedWithWarnings(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        final String viewStatusAdditionalInfo = String.format(VIEW_STATUS_ADDITIONAL_INFO, workflowVariables.getNodeName());
        systemRecorder.recordCommand(CommandLogName.MIGRATION.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), MIGRATE_COMPLETED_WITH_WARNING_ADDITIONAL_INFO + " " + viewStatusAdditionalInfo);
    }

    /**
     * Records successful Migration.
     *
     * @param workflowVariables the workflow variables
     */
    public void migrationSuccessful(final AbstractWorkflowVariables workflowVariables) {
        final long migrationExecutionTime = System.currentTimeMillis() - workflowVariables.getOrderStartTime();
        setUserIdContext(workflowVariables);
        String additionalInfo = String.format("Migraton completed, EXECUTION_TIME=%d milliseconds, TOTAL_NODE(S)=%d", migrationExecutionTime, 1);
        this.systemRecorder.recordCommand(CommandLogName.MIGRATION.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(), workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records cancelled integration.
     *
     * @param workflowVariables
     *            the workflow variables
     */
    public void migrationCancelled(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        systemRecorder.recordCommand(CommandLogName.MIGRATION.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), MIGRATE_CANCELLED_ADDITIONAL_INFO);
    }

    /**
     * Records integration completed with warnings
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void integrationCompletedWithWarnings(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        final String viewStatusAdditionalInfo = String.format(VIEW_STATUS_ADDITIONAL_INFO, workflowVariables.getNodeName());
        systemRecorder.recordCommand(CommandLogName.INTEGRATE.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), INTEGRATE_COMPLETED_WITH_WARNING_ADDITIONAL_INFO + " " + viewStatusAdditionalInfo);
    }

    /**
     * Records failed integration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void integrationFailed(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        final String additionalInfo = String.format(VIEW_STATUS_ADDITIONAL_INFO, workflowVariables.getNodeName());
        systemRecorder.recordCommand(CommandLogName.INTEGRATE.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
        systemRecorder.recordError(CommandLogName.INTEGRATE.toString(), ErrorSeverity.ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records failed migration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void migrationFailed(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        final String additionalInfo = String.format(VIEW_STATUS_ADDITIONAL_INFO_FOR_MIGRATION, workflowVariables.getNodeName());
        systemRecorder.recordCommand(CommandLogName.MIGRATION.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), additionalInfo);
        systemRecorder.recordError(CommandLogName.MIGRATION.toString(), ErrorSeverity.ERROR, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records cancelled integration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void integrationCancelled(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        systemRecorder.recordCommand(CommandLogName.INTEGRATE.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), INTEGRATE_CANCELLED_ADDITIONAL_INFO);
    }

    /**
     * Records successful order.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void orderSuccessful(final AbstractWorkflowVariables workflowVariables) {
        final long useCaseExecutionTime = System.currentTimeMillis() - workflowVariables.getOrderStartTime();
        setUserIdContext(workflowVariables);
        final String additionalInfo = String.format(ORDER_SUCCESSFUL_ADDITIONAL_INFO, workflowVariables.getApNodeFdn(), useCaseExecutionTime, 1);
        systemRecorder.recordCommand(CommandLogName.ORDER_NODE.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records successful Pre Migration.
     *
     * @param workflowVariables the workflow variables
     */
    public void preMigrationSuccessful(final AbstractWorkflowVariables workflowVariables) {
        final long useCaseExecutionTime = System.currentTimeMillis() - workflowVariables.getOrderStartTime();
        setUserIdContext(workflowVariables);
        final String additionalInfo = String.format(PRE_MIGRATION_SUCCESSFUL_ADDITIONAL_INFO, workflowVariables.getApNodeFdn(), useCaseExecutionTime, 1);
        systemRecorder.recordCommand(CommandLogName.PRE_MIGRATION_NODE.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records failed order.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void orderFailed(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        systemRecorder.recordCommand(CommandLogName.ORDER_NODE.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), ORDER_FAILED_ADDITIONAL_INFO);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), ORDER_FAILED_ADDITIONAL_INFO);
    }

    /**
     * Records Pre Migration failed error.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void preMigrationFailed(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        systemRecorder.recordCommand(CommandLogName.PRE_MIGRATION_NODE.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), PRE_MIGRATION_FAILED_ADDITIONAL_INFO);
        systemRecorder.recordError(CommandLogName.PRE_MIGRATION_NODE.toString(), ErrorSeverity.ERROR, workflowVariables.getNodeName(),
            workflowVariables.getApNodeFdn(), PRE_MIGRATION_FAILED_ADDITIONAL_INFO);
    }

    /**
     * Records failed order rollback.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void orderRollbackFailed(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        systemRecorder.recordCommand(CommandLogName.ORDER_NODE.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), ORDER_ROLLBACK_FAILED_ADDITIONAL_INFO);
        systemRecorder.recordError(CommandLogName.ORDER_NODE.toString(), ErrorSeverity.ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), ORDER_ROLLBACK_FAILED_ADDITIONAL_INFO);
    }

    private static void setUserIdContext(final AbstractWorkflowVariables workflowVariables) {
        final ContextServiceBean contextService = new ContextServiceBean();
        final String userId = workflowVariables.getUserId();
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);
    }

    /**
     * Records successful reconfiguration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void reconfigurationCompleted(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);

        final long reconfigExecutionTime = System.currentTimeMillis() - workflowVariables.getOrderStartTime();
        final String additionalInfo = String.format(RECONFIGURATION_COMPLETED_ADDITIONAL_INFO, reconfigExecutionTime, 1);
        systemRecorder.recordCommand(CommandLogName.RECONFIGURATION.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records reconfiguration completed with warnings
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void reconfigurationCompletedWithWarnings(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        final String viewStatusAdditionalInfo = String.format(VIEW_STATUS_ADDITIONAL_INFO, workflowVariables.getNodeName());
        systemRecorder.recordCommand(CommandLogName.RECONFIGURATION.toString(), CommandPhase.FINISHED_WITH_SUCCESS, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), RECONFIGURATION_COMPLETED_WITH_WARNINGS_ADDITIONAL_INFO + " " + viewStatusAdditionalInfo);
    }

    /**
     * Records failed reconfiguration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void reconfigurationFailed(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        final String additionalInfo = String.format(VIEW_STATUS_ADDITIONAL_INFO, workflowVariables.getNodeName());
        systemRecorder.recordCommand(CommandLogName.RECONFIGURATION.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
        systemRecorder.recordError(CommandLogName.RECONFIGURATION.toString(), ErrorSeverity.ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), additionalInfo);
    }

    /**
     * Records cancelled reconfiguration.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void reconfigurationCancelled(final AbstractWorkflowVariables workflowVariables) {
        setUserIdContext(workflowVariables);
        systemRecorder.recordCommand(CommandLogName.RECONFIGURATION.toString(), CommandPhase.FINISHED_WITH_ERROR, workflowVariables.getNodeName(),
                workflowVariables.getApNodeFdn(), RECONFIGURATION_CANCELLED_ADDITIONAL_INFO);
    }

    /**
     * Records expansion attempt.
     *
     * @param apNodeFdn
     *          the AP node FDN
     */
    public void expansionStarted(final String apNodeFdn) {
        final Map<String, Object> eventData = new HashMap<>();

        eventData.put(PHASE_KEY, CommandPhase.STARTED);
        eventData.put(MO_NAME_KEY, FDN.get(apNodeFdn).getRdnValue());
        systemRecorder.recordEventData(CommandLogName.EXPANSION.toString(), eventData);
    }

    /**
     * Records expansion resumed with AVC or 'ap resume'.
     *
     * @param apNodeFdn
     *          the AP node FDN
     * @param triggerType
     *          the expansion trigger type
     */
    public void expansionResumed(final String apNodeFdn, final String triggerType) {
        final Map<String, Object> eventData = new HashMap<>();

        eventData.put(PHASE_KEY, CommandPhase.ONGOING);
        eventData.put(MO_NAME_KEY, FDN.get(apNodeFdn).getRdnValue());
        eventData.put(TRIGGER_TYPE_KEY, triggerType);
        systemRecorder.recordEventData(CommandLogName.EXPANSION.toString(), eventData);
    }

    /**
     * Records successful expansion.
     *
     * @param workflowVariables
     *          the workflow variables
     */
    public void expansionSuccessful(final AbstractWorkflowVariables workflowVariables) {
        final long expansionExecutionTime = System.currentTimeMillis() - workflowVariables.getOrderStartTime() - workflowVariables.getTotalSuspendTime();
        final Map<String, Object> eventData = new HashMap<>();

        eventData.put(PHASE_KEY, CommandPhase.FINISHED_WITH_SUCCESS);
        eventData.put(MO_NAME_KEY, workflowVariables.getNodeName());
        eventData.put(EXECUTION_TIME_MS_KEY, expansionExecutionTime);
        systemRecorder.recordEventData(CommandLogName.EXPANSION.toString(), eventData);
    }

    /**
     * Records failed expansion.
     *
     * @param apNodeFdn
     *          the AP node FDN
     * @param additionalInfo
     *          the additional information
     */
    public void expansionFailed(final String apNodeFdn, final String additionalInfo) {
        final Map<String, Object> eventData = new HashMap<>();

        eventData.put(PHASE_KEY, CommandPhase.FINISHED_WITH_ERROR);
        eventData.put(MO_NAME_KEY, FDN.get(apNodeFdn).getRdnValue());
        if (StringUtils.isNotBlank(additionalInfo)){
            eventData.put(ADDITIONAL_INFO_KEY, additionalInfo);
        }
        systemRecorder.recordEventData(CommandLogName.EXPANSION.toString(), eventData);
    }

    /**
     * Records cancelled expansion.
     *
     * @param apNodeFdn
     *          the AP node FDN
     */
    public void expansionCancelled(final String apNodeFdn) {
        final Map<String, Object> eventData = new HashMap<>();

        eventData.put(PHASE_KEY, CommandPhase.FINISHED_WITH_ERROR);
        eventData.put(MO_NAME_KEY, FDN.get(apNodeFdn).getRdnValue());
        eventData.put(ADDITIONAL_INFO_KEY, CANCELLED_ADDITIONAL_INFO);
        systemRecorder.recordEventData(CommandLogName.EXPANSION.toString(), eventData);
    }

    /**
     * Record started activity with configuration source
     *
     * @param activityType
     *          the activity type
     * @param source
     *          the configuration source
     * @param isZeroTouch
     *          is Zero Touch or not
     */
    public void activityStarted(final String activityType, final String source, final boolean isZeroTouch) {
        final Map<String, Object> eventData = new HashMap<>();

        eventData.put("ACTIVITY_TYPE", isZeroTouch ? activityType + "ZT" : activityType);

        Arrays.stream(SourceTypes.values()).forEach(type -> eventData.put(type.name(), 0));
        SourceTypes sourceType = SourceTypes.UNKNOWN;
        if (StringUtils.isNotBlank(source)) {
            sourceType = Arrays.stream(SourceTypes.values())
                    .filter(type -> type.name().equals(source.toUpperCase(Locale.ENGLISH).trim()))
                    .findFirst()
                    .orElse(SourceTypes.OTHER);
        }
        eventData.put(sourceType.name(), 1);

        systemRecorder.recordEventData(CommandLogName.NODE_PROVISIONING_TOOL.toString(), eventData);
    }
}
