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
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

import com.ericsson.oss.services.ap.api.exception.InstantaneousLicensingRestServiceException;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.CreateLicenseResponseDto;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.RetrieveLicenseStatusResponseDto;

/**
 * Handles REST responses for Instantaneous Licensing
 */
public class InstantaneousLicensingResponseHandler {

    private static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles responses from the create license request. For 200 OK responses the response object is
     * returned, otherwise a {@link InstantaneousLicensingRestServiceException} is thrown
     *
     * @param httpResponse
     *              the response received for the license request
     * @return {@link CreateLicenseResponseDto}
     *              the response read from SHM
     */
    public CreateLicenseResponseDto handleCreateRequestResponse(final HttpResponse httpResponse) {
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        try {
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    return objectMapper.readValue(EntityUtils.toString(httpResponse.getEntity()), CreateLicenseResponseDto.class);
                case HttpStatus.SC_BAD_REQUEST: case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    final CreateLicenseResponseDto createLicenseResponseDto = objectMapper.readValue(EntityUtils.toString(httpResponse.getEntity()), CreateLicenseResponseDto.class);
                    throw new InstantaneousLicensingRestServiceException(
                            String.format("Error in HTTP response for create license job, response returned additional information: %s",
                                    createLicenseResponseDto.getAdditionalInfo()));
                case HttpStatus.SC_NOT_FOUND:
                    throw new InstantaneousLicensingRestServiceException(String.format("Request to SHM failed, response code: %s", statusCode));
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    throw new InstantaneousLicensingRestServiceException("Request to SHM failed, No server is available to handle this request");
                default:
                    throw new InstantaneousLicensingRestServiceException(String.format("Unexpected response code %s for create Instantaneous Licensing", statusCode));
            }
        } catch (final IOException e) {
            throw new InstantaneousLicensingRestServiceException(String.format("Failed to create license, status code %s: %s", statusCode, e.getMessage()), e);
        }
    }

    /**
     * Handles responses from the Get License Status Request response. For 200 OK responses the response object is
     * returned, otherwise a {@link InstantaneousLicensingRestServiceException} is thrown
     *
     * @param httpResponse
     *              the response received for the license request
     * @return {@link RetrieveLicenseStatusResponseDto}
     *              the response object from SHM containing information about the license request status
     */
    public RetrieveLicenseStatusResponseDto handleGetRequestResponse(final HttpResponse httpResponse) {
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        try {
            switch (statusCode) {
                case HttpStatus.SC_OK:
                    return objectMapper.readValue(EntityUtils.toString(httpResponse.getEntity()), RetrieveLicenseStatusResponseDto.class);
                case HttpStatus.SC_BAD_REQUEST: case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    final RetrieveLicenseStatusResponseDto licenseStatusResponse = objectMapper.readValue(EntityUtils.toString(httpResponse.getEntity()), RetrieveLicenseStatusResponseDto.class);
                    throw new InstantaneousLicensingRestServiceException(
                            String.format("Error in HTTP response for get license status job, response returned additional information: %s",
                                    licenseStatusResponse.getAdditionalInfo()));
                case HttpStatus.SC_NOT_FOUND:
                    throw new InstantaneousLicensingRestServiceException(String.format("Request to SHM failed, response code: %s", statusCode));
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    throw new InstantaneousLicensingRestServiceException("Request to SHM failed, No server is available to handle this request");
                default:
                    throw new InstantaneousLicensingRestServiceException(String.format("Unexpected response code %s for get request status in Instantaneous Licensing", statusCode));
            }
        } catch (final IOException e) {
            throw new InstantaneousLicensingRestServiceException(String.format("Failed to get license request status, status code %s: %s", statusCode, e.getMessage()), e);
        }
    }
}