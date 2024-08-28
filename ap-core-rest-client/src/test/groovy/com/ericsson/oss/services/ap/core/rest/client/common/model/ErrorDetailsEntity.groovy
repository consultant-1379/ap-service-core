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
package com.ericsson.oss.services.ap.core.rest.client.common.model

class ErrorDetailsEntity {
    private String userMessage
    private Integer internalErrorCode
    private String developerMessage

    String getUserMessage() {
        return userMessage
    }

    void setUserMessage(final String message) {
        this.userMessage = message
    }

    Integer getInternalErrorCode() {
        return internalErrorCode
    }

    void setInternalErrorCode(final Integer internalErrorCode) {
        this.internalErrorCode = internalErrorCode
    }

    String getDeveloperMessage() {
        return developerMessage
    }

    void setDeveloperMessage(final String developerMessage) {
        this.developerMessage = developerMessage
    }
}
