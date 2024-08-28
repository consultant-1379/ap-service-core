/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.model.eoi;

public class EoiSecurityErrorResponse {

    private String httpStatus;
    private String message;
    private String causedBy;
    private String suggestedSolution;

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCausedBy() {
        return causedBy;
    }

    public void setCausedBy(String causedBy) {
        this.causedBy = causedBy;
    }

    public String getSuggestedSolution() {
        return suggestedSolution;
    }

    public void setSuggestedSolution(String suggestedSolution) {
        this.suggestedSolution = suggestedSolution;
    }

    @Override
    public String toString() {
        return "EoiSecurityErrorResponse{" +
                "httpStatus='" + httpStatus + '\'' +
                ", message='" + message + '\'' +
                ", causedBy='" + causedBy + '\'' +
                ", suggestedSolution='" + suggestedSolution + '\'' +
                '}';
    }

}
