/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status;

import java.io.Serializable;

/**
 * Container object for the integration status of an AP node.
 */
public class StatusEntry implements Serializable {

    private static final long serialVersionUID = 195432561437162923L;

    private String taskName;
    private String taskProgress;
    private String timeStamp;
    private String additionalInfo;

    /**
     * Default constructor (required for use by JSON parser). AP application code should use the other argument based constructor for this class.
     */
    public StatusEntry() {

    }

    /**
     * Constructor for class. AP application code use this constructor.
     *
     * @param taskName
     *            the name of the task
     * @param taskProgress
     *            possible progress state for a status entry
     * @param timeStamp
     *            time of the last update for the task
     * @param additionalInfo
     *            optional additional information for the task
     */
    public StatusEntry(final String taskName, final String taskProgress, final String timeStamp, final String additionalInfo) {
        this.taskName = taskName;
        this.taskProgress = taskProgress;
        this.timeStamp = timeStamp;
        this.additionalInfo = additionalInfo;
    }

    /**
     * The name of the task.
     *
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * The progress state.
     *
     * @return the taskProgress
     */
    public String getTaskProgress() {
        return taskProgress;
    }

    /**
     * The current timestamp.
     *
     * @return the timeStamp
     */
    public String getTimeStamp() {
        return timeStamp;
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
