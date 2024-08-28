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
package com.ericsson.oss.services.ap.common.util.log;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderNonCDIImpl;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * Class that starts a timer for a given use case, and then logs to DDP at the end of execution.
 */
public class DdpTimer {

    private static final String DDP_LOG_MESSAGE_FORMAT = "EXECUTION_TIME=%s milliseconds, TOTAL_NODE(S)=%d";

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
     * <p>
     * Logs the use case for a single node only.
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     */
    public void end(final String moFdn) {
        end(moFdn, 1);
    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}. Will be logged as finishing with error.
     * <p>
     * Logs the use case for a single node only.
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     */
    public void endWithError(final String moFdn) {
        endWithError(moFdn, 1);
    }

    /**
     * Ends the timer, and logs using the {@link SystemRecorder}. Will be logged as finishing successfully.
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
     *
     * @param moFdn
     *            the FDN of the AP project or AP node
     * @param numberOfNodes
     *            the number of AP nodes this use case is being executed over
     * @param phase
     *            whether the usecase was completed successfully or with an error
     */
    public void end(final String moFdn, final int numberOfNodes, final CommandPhase phase) {
        final String moName = FDN.get(moFdn).getRdnValue();
        final long executionTimeInMilliseconds = (System.currentTimeMillis() - startTime);

        recorder.recordCommand(logName, phase, moName, moFdn, String.format(DDP_LOG_MESSAGE_FORMAT, executionTimeInMilliseconds, numberOfNodes));
    }

}
