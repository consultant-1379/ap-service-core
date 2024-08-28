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
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing

import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.CreateLicenseResponseDto
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.mockserver.model.HttpStatusCode
import spock.lang.Subject

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.InstantaneousLicensingRestServiceException
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.RetrieveLicenseStatusResponseDto

class InstantaneousLicensingResponseHandlerSpec extends CdiSpecification {

    private final String SUCCESS_CREATE_LICENSE_RESPONSE_BODY =
            "{\"requestId\": \"IntegrationLicensekeyfiles_auto_administrator_011020205420\"," +
            "  \"additionalInfo\": \"<reason>\"}"

    private final String FAILED_CREATE_LICENSE_RESPONSE_BODY =
            "{\"requestId\": \"IntegrationLicensekeyfiles_auto_administrator_011020205420\"," +
            " \"additionalInfo\": \"Failed with error making request\"}"

    private final String INVALID_RESPONSE_BODY =
            "{ \"unexpectedValue\": \"Ile2etestnode1_integration\""

    private final String SUCCESS_GET_LICENSE_STATUS_RESPONSE_BODY =
            "{\"requestId\": \"IntegrationLicensekeyfiles_auto_administrator_011020205420\"," +
            "  \"fingerprint\": \"Ile2etestnode1_integration\"," +
            "  \"result\": \"SUCCESS\"," +
            "  \"state\": \"RUNNING\"," +
            "  \"additionalInfo\": \"Request is running\"}"

    private final String FAILED_GET_LICENSE_STATUS_RESPONSE_BODY =
            "{\"requestId\": \"IntegrationLicensekeyfiles_auto_administrator_011020205420\"," +
            "  \"fingerprint\": \"Ile2etestnode1_integration\"," +
            "  \"result\": \"FAILED\"," +
            "  \"state\": \"\"," +
            "  \"additionalInfo\": \"Failed with error making request\"}"

    @Subject
    @Inject
    private InstantaneousLicensingResponseHandler responseHandler

    @MockedImplementation
    HttpResponse httpResponse

    @MockedImplementation
    StatusLine statusLine

    def "When HTTP response for create is as expected with 200 response then requestid is returned"() {
        given: "HTTP Response with success response content"
            HttpEntity httpEntity = new StringEntity(SUCCESS_CREATE_LICENSE_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> 200

        when: "Handling HTTP Response"
            CreateLicenseResponseDto result = responseHandler.handleCreateRequestResponse(httpResponse)

        then: "The request id is returned"
            result.getRequestId() ==  "IntegrationLicensekeyfiles_auto_administrator_011020205420"
    }

    def "When HTTP response for create is failed with error response then exception is thrown"() {
        given: "HTTP Response with failure response content"
            HttpEntity httpEntity = new StringEntity(FAILED_CREATE_LICENSE_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> statusCode

        when: "Handling HTTP Response"
            responseHandler.handleCreateRequestResponse(httpResponse)

        then: "Exception is handled correctly"
            Exception e = thrown(InstantaneousLicensingRestServiceException.class)
            e.getMessage().equals(exceptionMessage)

        where: "HTTP data"
            statusCode                                      | exceptionMessage
            HttpStatusCode.BAD_REQUEST_400.code()           | "Error in HTTP response for create license job, response returned additional information: Failed with error making request"
            HttpStatusCode.INTERNAL_SERVER_ERROR_500.code() | "Error in HTTP response for create license job, response returned additional information: Failed with error making request"
            HttpStatusCode.NOT_FOUND_404.code()             | "Request to SHM failed, response code: 404"
            HttpStatusCode.SERVICE_UNAVAILABLE_503.code()   | "Request to SHM failed, No server is available to handle this request"
            HttpStatusCode.BAD_GATEWAY_502.code()           | "Unexpected response code 502 for create Instantaneous Licensing"

    }

    def "When HTTP response for create returns unexpected values then exception is thrown"() {
        given: "HTTP Response with invalid response content"
            HttpEntity httpEntity = new StringEntity(INVALID_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> 200

        when: "Handling HTTP Response"
            responseHandler.handleCreateRequestResponse(httpResponse)

        then: "Exception is handled correctly"
            Exception e = thrown(InstantaneousLicensingRestServiceException.class)
            e.getMessage().startsWith("Failed to create license, status code 200:")
    }

    def "When HTTP response for get is as expected with 200 response then correct object is returned"() {
        given: "HTTP Response with success response content"
            HttpEntity httpEntity = new StringEntity(SUCCESS_GET_LICENSE_STATUS_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> 200

        when: "Handling HTTP Response"
            RetrieveLicenseStatusResponseDto result = responseHandler.handleGetRequestResponse(httpResponse)

        then: "Object returned is as expected"
            result.getRequestId() == "IntegrationLicensekeyfiles_auto_administrator_011020205420"
            result.getFingerprint() == "Ile2etestnode1_integration"
            result.getResult() == "SUCCESS"
            result.getState() == "RUNNING"
            result.getAdditionalInfo() == "Request is running"
    }

    def "When HTTP response for get status is failed with error response then exception is thrown"() {
        given: "HTTP Response with failure response content"
            HttpEntity httpEntity = new StringEntity(FAILED_GET_LICENSE_STATUS_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> statusCode

        when: "Handling HTTP Response"
            responseHandler.handleGetRequestResponse(httpResponse)

        then: "Exception is handled correctly"
            Exception e = thrown(InstantaneousLicensingRestServiceException.class)
            e.getMessage().equals(exceptionMessage)

        where: "HTTP data"
            statusCode                                      | exceptionMessage
            HttpStatusCode.BAD_REQUEST_400.code()           | "Error in HTTP response for get license status job, response returned additional information: Failed with error making request"
            HttpStatusCode.INTERNAL_SERVER_ERROR_500.code() | "Error in HTTP response for get license status job, response returned additional information: Failed with error making request"
            HttpStatusCode.NOT_FOUND_404.code()             | "Request to SHM failed, response code: 404"
            HttpStatusCode.SERVICE_UNAVAILABLE_503.code()   | "Request to SHM failed, No server is available to handle this request"
            HttpStatusCode.BAD_GATEWAY_502.code()           | "Unexpected response code 502 for get request status in Instantaneous Licensing"
    }

    def "When HTTP response for get returns unexpected values then exception is thrown"() {
        given: "HTTP Response with invalid response content"
            HttpEntity httpEntity = new StringEntity(INVALID_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> 200

        when: "Handling HTTP Response"
            responseHandler.handleGetRequestResponse(httpResponse)

        then: "Exception is handled correctly"
            Exception e = thrown(InstantaneousLicensingRestServiceException.class)
            e.getMessage().startsWith("Failed to get license request status, status code 200:")
    }
}
