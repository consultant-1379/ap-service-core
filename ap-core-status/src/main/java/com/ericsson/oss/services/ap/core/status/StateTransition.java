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
package com.ericsson.oss.services.ap.core.status;

/**
 * Defines a transition from one state to another, given a specific event.
 */
public class StateTransition {

    private final String from;
    private final String event;
    private final String to;

    public StateTransition(final String from, final String event, final String to) {
        this.from = from;
        this.to = to;
        this.event = event;
    }

    public String from() {
        return from;
    }

    public String to() {
        return to;
    }

    public String getEvent() {
        return event;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final StateTransition that = (StateTransition) o;

        if (from != null ? !from.equals(that.from) : that.from != null) {
            return false;
        }

        if (event != null ? !event.equals(that.event) : that.event != null) {
            return false;
        }
        return to != null ? to.equals(that.to) : that.to == null;
    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (event != null ? event.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }
}
