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
 * DTO for the response data sent from SHM from the request to create a license
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateLicenseResponseDto {

    private String requestId;
    private String additionalInfo;

    public CreateLicenseResponseDto() {
    }

    public CreateLicenseResponseDto(final String requestId, final String additionalInfo) {
        this.requestId = requestId;
        this.additionalInfo = additionalInfo;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
