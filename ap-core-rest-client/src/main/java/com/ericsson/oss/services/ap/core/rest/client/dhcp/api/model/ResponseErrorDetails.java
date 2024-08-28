/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.dhcp.api.model;

/**
 * JSON Mapping class for error details parsed from the REST response status. The data in this POJO is defined by the Configuration Template Service
 * API.
 */
public class ResponseErrorDetails{

    private String message;
    private String correctiveAction;
    private String additionalInformation;
    private int errorCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public void setCorrectiveAction(final String correctiveAction) {
        this.correctiveAction = correctiveAction;
    }

    public void setAdditionalInformation(final String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }
}
