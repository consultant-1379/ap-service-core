/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderNonCDIImpl;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Class used to log the execute of a usecase in {@link AutoProvisioningServiceCoreBean}, for success and failure scenarios.
 */
class UseCaseRecorder {

    /**
     * ENUM defining the scope of the usecase. Used to determine if DDP logging is at project or node scope.
     */
    enum Scope {
        MULTIPLE_PROJECTS,
        MULTIPLE_NODES,
        SINGLE_PROJECT,
        SINGLE_NODE;
    }

    static final String NODES_LOG = "EXECUTION_TIME=%s milliseconds, TOTAL_NODE(S)=%d";
    static final String PROJECTS_LOG = "EXECUTION_TIME=%s milliseconds, TOTAL_PROJECT(S)=%d";

    private final long startTime = System.nanoTime();
    private final String commandName;
    private final Scope scope;

    private String source = "";
    private String resource = "";

    private final SystemRecorder recorder = new SystemRecorderNonCDIImpl();

    UseCaseRecorder(final CommandLogName commandName, final Scope scope, final String source) {
        this.commandName = commandName.toString();
        this.scope = scope;
        this.source = source;
    }

    UseCaseRecorder(final CommandLogName commandName, final Scope scope, final FDN moFdn) {
        this.commandName = commandName.toString();
        this.scope = scope;
        source = moFdn.getRdnValue();
        resource = moFdn.toString();
    }

    UseCaseRecorder(final CommandLogName commandName, final Scope scope) {
        this.commandName = commandName.toString();
        this.scope = scope;
    }

    void setResource(final String resource) {
        this.resource = resource;
    }

    /**
     * Logs that the command finished with {@link CommandPhase#FINISHED_WITH_SUCCESS} to {@link SystemRecorder}.
     * <p>
     * DDP logging assumes a single node/project was executed on.
     */
    void success() {
        final long executionTime = getExecutionTime();
        final String message = getErrorMessage(executionTime, 1);
        recorder.recordCommand(commandName, CommandPhase.FINISHED_WITH_SUCCESS, source, resource, message);
    }

    /**
     * Logs that the command finished with {@link CommandPhase#FINISHED_WITH_SUCCESS} to {@link SystemRecorder}.
     * <p>
     * DDP logging will log the input number of MOs (project or node).
     *
     * @param numberOfMos the number of projects/nodes the usecases is executing upon
     */
    void success(final int numberOfMos) {
        final long executionTime = getExecutionTime();
        final String message = getErrorMessage(executionTime, numberOfMos);
        recorder.recordCommand(commandName, CommandPhase.FINISHED_WITH_SUCCESS, source, resource, message);
    }

    /**
     * Logs that the command finished with {@link CommandPhase#FINISHED_WITH_SUCCESS} to {@link SystemRecorder}.
     * <p>
     * Will not use the default DDP logging, and will instead log the input message.
     *
     * @param message the message to be logged
     */
    void success(final String message) {
        recorder.recordCommand(commandName, CommandPhase.FINISHED_WITH_SUCCESS, source, resource, message);
    }

    /**
     * Records the error using {@link SystemRecorder}. Logs twice:
     * <ul>
     * <li>{@link SystemRecorder#recordCommand} with {@link CommandPhase#FINISHED_WITH_ERROR}, using the input {@link Exception#getMessage()} as the
     * error message</li>
     * <li>{@link SystemRecorder#recordError} with {@link ErrorSeverity#ERROR}, with the root cause of the input {@link Exception} using
     * {@link ExceptionUtils#getRootCause} as the error message</li>
     * </ul>
     *
     * @param e the {@link Exception} thrown during the usecase
     */
    void error(final Exception e) {
        recorder.recordCommand(commandName, CommandPhase.FINISHED_WITH_ERROR, source, resource, e.getMessage());
        recorder.recordError(commandName, ErrorSeverity.ERROR, source, resource, ExceptionUtils.getRootCauseMessage(e));
    }

    /**
     * Records the error using {@link SystemRecorder}. Logs twice:
     * <ul>
     * <li>{@link SystemRecorder#recordCommand} with {@link CommandPhase#FINISHED_WITH_ERROR}, using the input error message</li>
     * <li>{@link SystemRecorder#recordError} with {@link ErrorSeverity#ERROR}, using the input error message</li>
     * </ul>
     *
     * @param errorMessage the error message for the usecase
     */
    void error(final String errorMessage) {
        recorder.recordCommand(commandName, CommandPhase.FINISHED_WITH_ERROR, source, resource, errorMessage);
        recorder.recordError(commandName, ErrorSeverity.ERROR, source, resource, errorMessage);
    }

    /**
     * Retrieve the usecase execution time in milliseconds.
     *
     * @return the usecase execution time
     */
    long getExecutionTime() {
        return (System.nanoTime() - startTime) / 1_000_000;
    }

    private String getErrorMessage(final long executionTime, final int numberOfMos) {
        if (Scope.MULTIPLE_PROJECTS == scope || Scope.SINGLE_PROJECT == scope) {
            return String.format(PROJECTS_LOG, executionTime, numberOfMos);
        }

        return String.format(NODES_LOG, executionTime, numberOfMos);
    }
}
