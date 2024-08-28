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
package com.ericsson.oss.services.ap.common.util.alarm;

import com.ericsson.oss.mediation.translator.model.Constants;
import com.ericsson.oss.mediation.translator.model.EventNotification;

/**
 * Builds the <code>EventNotification</code> object for an FM modelled event.
 */
public class AlarmDataBuilder {

    public static final String SEVERITY_CRITICAL = Constants.SEV_CRITICAL;
    public static final String SEVERITY_MAJOR = Constants.SEV_MAJOR;
    public static final String SEVERITY_MINOR = Constants.SEV_MINOR;
    public static final String SEVERITY_WARNING = Constants.SEV_WARNING;
    public static final String SEVERITY_INDETERMINATE = Constants.SEV_INDETERMINATE;

    private final EventNotification eventNotification;

    public AlarmDataBuilder() {
        eventNotification = new EventNotification();
        // FM demands that internal alarms should contain additional attribute of "fdn" set to "ENM".
        eventNotification.addAdditionalAttribute("fdn", "ManagementSystem=ENM");
    }

    /**
     * Sets the MO FDN that the alarm is being raised on.
     *
     * @param fdn
     *            FDN of MO that the alarm is being raised on
     * @return AlarmDataBuilder
     */
    public AlarmDataBuilder setManagedObjectInstance(final String fdn) {
        eventNotification.setManagedObjectInstance(fdn);
        return this;
    }

    /**
     * Sets the severity of the alarm. Can be:
     * <ul>
     * <li>SEVERITY_CRITICAL</li>
     * <li>SEVERITY_MAJOR</li>
     * <li>SEVERITY_MINOR</li>
     * <li>SEVERITY_WARNING</li>
     * <li>SEVERITY_INDETERMINATE</li>
     * </ul>
     *
     * @param perceivedSeverity
     *            severity of the alarm
     * @return AlarmDataBuilder
     */
    public AlarmDataBuilder setPerceivedSeverity(final String perceivedSeverity) {
        eventNotification.setPerceivedSeverity(perceivedSeverity);
        return this;
    }

    /**
     * Sets the problem that the alarm is being raised for.
     *
     * @param specificProblem
     *            problem the alarm is being raised for
     * @return AlarmDataBuilder
     */
    public AlarmDataBuilder setSpecificProblem(final String specificProblem) {
        eventNotification.setSpecificProblem(specificProblem);
        return this;
    }

    /**
     * Sets the probable cause of the problem.
     *
     * @param probableCause
     *            the probable cause of the problem
     * @return AlarmDataBuilder
     */
    public AlarmDataBuilder setProbableCause(final String probableCause) {
        eventNotification.setProbableCause(probableCause);
        return this;
    }

    /**
     * Sets the event type i.e. 'Node integration error'.
     *
     * @param eventType
     *            the event type
     * @return AlarmDataBuilder
     */
    public AlarmDataBuilder setEventType(final String eventType) {
        eventNotification.setEventType(eventType);
        return this;
    }

    /**
     * Sets a description for the alarm.
     *
     * @param description
     *            the description
     * @return AlarmDataBuilder
     */
    public AlarmDataBuilder setDescription(final String description) {
        eventNotification.addAdditionalAttribute("additionalText", description);
        return this;
    }

    /**
     * Builds the <code>EventNotification</code> object encapsulating all the data for the alarm.
     *
     * @return the <code>EventNotification</code> object
     */
    public EventNotification build() {
        return eventNotification;
    }

}
