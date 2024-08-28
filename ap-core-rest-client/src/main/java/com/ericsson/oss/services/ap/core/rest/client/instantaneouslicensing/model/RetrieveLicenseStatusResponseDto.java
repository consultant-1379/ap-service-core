/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for the response data sent from SHM from the request to get the status of a license
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrieveLicenseStatusResponseDto {

    private String requestId;
    private String fingerprint;
    private String result;
    private String state;
    private String additionalInfo;

    public RetrieveLicenseStatusResponseDto() {
    }

    public RetrieveLicenseStatusResponseDto(final String requestId, final String fingerprint, final String result, final String state, final String additionalInfo) {
        this.requestId = requestId;
        this.fingerprint = fingerprint;
        this.result = result;
        this.state = state;
        this.additionalInfo = additionalInfo;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getResult() {
        return result;
    }

    public String getState() {
        return state;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
