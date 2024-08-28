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
package com.ericsson.oss.services.ap.core.rest.model;

/**
 * Container object for the integration status of an AP node.
 */
public class StatusEntryData {

    private final String task;
    private final String progress;
    private final String timestamp;
    private final String additionalInfo;

    /**
     * Constructor for class. AP application code use this constructor.
     *
     * @param task           the name of the task
     * @param progress       possible progress state for a status entry
     * @param timestamp      time of the last update for the task
     * @param additionalInfo optional additional information for the task
     */
    public StatusEntryData(final String task, final String progress, final String timestamp, final String additionalInfo) {
        this.task = task;
        this.progress = progress;
        this.timestamp = timestamp;
        this.additionalInfo = additionalInfo;
    }

    /**
     * The name of the task.
     *
     * @return the taskName
     */
    public String getTask() {
        return task;
    }

    /**
     * The progress state.
     *
     * @return the taskProgress
     */
    public String getProgress() {
        return progress;
    }

    /**
     * The current timestamp.
     *
     * @return the timeStamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * The additional information.
     *
     * @return the additionalInfo
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
