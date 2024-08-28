/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.log;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderNonCDIImpl;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Class that starts a timer for a given use case, and then logs to DDP at the end of execution.
 *
 * DDP must be notified of changes if:
 *      1. updates are made to what is recorded when timer ends,
 *      2. a new task is being logged by calling start and end methods with new log name
 */
public class DdpLogTimer {

    private SystemRecorder recorder = new SystemRecorderNonCDIImpl(); // NOPMD

    private String logName;
    private long startTime;

    /**
     * Starts the timer for the given use case.
     *
     * @param logName
     *            the name of the use case to log for DDP
     */
    public void start(final String logName) {
        this.logName = logName;
        startTime = System.currentTimeMillis();
    }

    /**
     * Sets the start time retrospectively for a given log name.
     *
     * @param logName
     * @param startTime
     *              the time in ms that the use case started
     */
    public void setStartTime(final String logName, final long startTime) {
        this.logName = logName;
        this.startTime = startTime;
    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}. Will be logged as finishing successfully.
     * Includes number of nodes in the log entry. Should only be used for project specific usecases where number of nodes is applicable
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     * @param numberOfNodes
     *            the number of AP nodes this use case is being executed over
     */
    public void end(final String moFdn, final int numberOfNodes) {
        end(moFdn, numberOfNodes, CommandPhase.FINISHED_WITH_SUCCESS);

    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}. Will be logged as finishing with error.
     * Includes number of nodes in the log entry. Should only be used for project specific usecases where number of nodes is applicable
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     * @param numberOfNodes
     *            the number of AP nodes this use case is being executed over
     */
    public void endWithError(final String moFdn, final int numberOfNodes) {
        end(moFdn, numberOfNodes, CommandPhase.FINISHED_WITH_ERROR);
    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}.
     * Includes number of nodes in the log entry. Should only be used for project specific usecases where number of nodes is applicable
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     * @param numberOfNodes
     *            the number of AP nodes this use case is being executed over
     * @param phase
     *            whether the usecase was completed successfully or with an error
     */
    public void end(final String moFdn, final int numberOfNodes, final CommandPhase phase) {
        //updates to data logged here must be coordinated with DDP

        final long executionTimeInMilliseconds = (System.currentTimeMillis() - startTime);
        final String moName = FDN.get(moFdn).getRdnValue();

        final Map<String, Object> eventData = new HashMap<>();
        eventData.put("PHASE", phase);
        eventData.put("MO_NAME", moName);
        eventData.put("NUMBER_OF_NODES", numberOfNodes);
        eventData.put("EXECUTION_TIME_MS", executionTimeInMilliseconds);
        recorder.recordEventData(logName, eventData);
    }


    /**
     * Ends the timer, and logs using the {@link SystemRecorder}. Will be logged as finishing successfully.
     * <p>
     * Should be used for node specific usecases where number of nodes is not needed.
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     */
    public void end(final String moFdn) {
        end(moFdn, CommandPhase.FINISHED_WITH_SUCCESS);
    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}. Will be logged as finishing with error.
     * <p>
     * Should be used for node specific usecases where number of nodes is not needed.
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     */
    public void endWithError(final String moFdn) {
        end(moFdn, CommandPhase.FINISHED_WITH_ERROR);
    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}.
     * <p>
     * Should be used for node specific usecases where number of nodes is not needed.
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     * @param phase
     *            whether the usecase was completed successfully or with an error
     */
    public void end(final String moFdn, final CommandPhase phase) {
        //updates to data logged here must be coordinated with DDP

        final long executionTimeInMilliseconds = (System.currentTimeMillis() - startTime);
        final String moName = FDN.get(moFdn).getRdnValue();

        final Map<String, Object> eventData = new HashMap<>();
        eventData.put("PHASE", phase);
        eventData.put("MO_NAME", moName);
        eventData.put("EXECUTION_TIME_MS", executionTimeInMilliseconds);
        recorder.recordEventData(logName, eventData);
    }

}
