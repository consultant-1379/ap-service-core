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
package com.ericsson.oss.services.ap.api.model;

import java.io.Serializable;

/**
 * The report result for Apply AMOS Script flow
 */
public class ApplyAmosFAReportResult implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean complete;
    private boolean success;
    private String errorMessage;
    private String contextLink;

    /**
     * @return the complete
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * @param complete
     *            the complete to set
     */
    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    /**
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success
     *            the success to set
     */
    public void setSuccess(final boolean success) {
        this.success = success;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage
     *            the errorMessage to set
     */
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the contextLink
     */
    public String getContextLink() {
        return contextLink;
    }

    /**
     * @param contextLink
     *            the contextLink to set
     */
    public void setContextLink(final String contextLink) {
        this.contextLink = contextLink;
    }

}
