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
package com.ericsson.oss.services.ap.core.rest.client.flowautomation

import org.apache.http.entity.ContentType
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAExecutionResult
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAReportResult

class ApplyAmosFARestClientSpec extends CdiSpecification {

    private static final int PORT = 8010
    private static final String POST_URL = "/flowautomation/v1/flows/com.ericsson.oss.services.ap.flows.amos/execute"
    private static final String GET_URL = "/flowautomation/v1/executions/AAS-LTE01dg2ERBS00002-scriptname-1591953921444/report"

    public static final String EXECUTE_SUEECSS_RESP_BODY = "{\"name\":\"AAS-LTE01dg2ERBS00002-scriptname-1591953921444\"}"

    public static final String REPORT_SUCCESS_RESPONSE_BODY =
        """
    {
    \t"header" : {
    \t\t"reportTime" : "2020-06-12T10:25:51+0100",
    \t\t"flowId" : "com.ericsson.oss.services.ap.flows.amos",
    \t\t"flowVersion" : "1.0.6",
    \t\t"flowName" : "Apply AMOS Flow",
    \t\t"flowExecutionName" : "AAS-LTE01dg2ERBS00002-scriptname-1591953921444",
    \t\t"startedBy" : "administrator",
    \t\t"startTime" : "2020-06-12T10:25:21+0100",
    \t\t"endTime" : "2020-06-12T10:25:34+0100",
    \t\t"status" : "COMPLETED"
    \t},
    \t"body" : {
    \t\t"reportSummary" : {
    \t\t\t"nodeName" : "LTE01dg2ERBS00002",
    \t\t\t"amosScriptName" : "scriptname.mos",
    \t\t\t"result" : "SUCCESS",
    \t\t\t"additionalInformation" : "",
    \t\t\t"contextualLink" : "https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255"
    \t\t}
    \t}
    }
    """

    public static final String REPORT_FAIL_RESPONSE_BODY =
    """
    {
    \t"header" : {
    \t\t"reportTime" : "2020-06-12T10:25:51+0100",
    \t\t"flowId" : "com.ericsson.oss.services.ap.flows.amos",
    \t\t"flowVersion" : "1.0.6",
    \t\t"flowName" : "Apply AMOS Flow",
    \t\t"flowExecutionName" : "AAS-LTE01dg2ERBS00002-scriptname-1591953921444",
    \t\t"startedBy" : "administrator",
    \t\t"startTime" : "2020-06-12T10:25:21+0100",
    \t\t"endTime" : "2020-06-12T10:25:34+0100",
    \t\t"status" : "COMPLETED"
    \t},
    \t"body" : {
    \t\t"reportSummary" : {
    \t\t\t"nodeName" : "LTE01dg2ERBS00002",
    \t\t\t"amosScriptName" : "scriptname.mos",
    \t\t\t"result" : "FAIL",
    \t\t\t"additionalInformation" : "fail to get report",
    \t\t\t"contextualLink" : "https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255"
    \t\t}
    \t}
    }
    """

    private static ClientAndServer mockEndpoint

    @ObjectUnderTest
    ApplyAmosFARestClient faRestClient

    def setupSpec() {
        mockEndpoint = ClientAndServer.startClientAndServer(PORT)
    }

    def setup() {
        mockEndpoint.reset()
    }

    def cleanupSpec() {
        mockEndpoint.stop()
    }

    def "Send POST request to execute flow and success response received" () {
        given: "POST request is mocked to respond with status OK and no error"
            System.setProperty("INTERNAL_URL","http://localhost:" + PORT + POST_URL)

            mockEndpoint.when(
                    HttpRequest.request(POST_URL)
                    .withMethod("POST")
                    .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
                    .withHeader("X-Tor-UserID", "ap_user")
                    ).respond(
                    HttpResponse.response()
                    .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                    .withBody(EXECUTE_SUEECSS_RESP_BODY)
                    .withStatusCode(200)
                    )

        when: "The http request has been sent and the response has been processed"
            ApplyAmosFAExecutionResult result = faRestClient.execute("ap_user", "AAS-LTE01dg2ERBS00002-scriptname-1591953921444", "LTE01dg2ERBS00002", "scriptname.mos", null, false)

        then: "Mocked status 200 response is received and handled correctly"
            result.isSuccess() == true
    }

    def "Send POST request to execute flow and failed response received" () {
        given: "POST request is mocked to respond with 500 server error"
            System.setProperty("INTERNAL_URL","http://localhost:" + PORT + POST_URL)

            mockEndpoint.when(
                    HttpRequest.request(POST_URL)
                    .withMethod("POST")
                    .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
                    .withHeader("X-Tor-UserID", "ap_user")
                    ).respond(
                    HttpResponse.response()
                    .withStatusCode(500)
                    )

        when: "The http request has been sent and the response has been processed"
            ApplyAmosFAExecutionResult result = faRestClient.execute("ap_user", "AAS-LTE01dg2ERBS00002-scriptname-1591953921444", "LTE01dg2ERBS00002", "scriptname.mos", null, false)

        then: "Mocked status 500 server error response is received and handled correctly"
            result.isSuccess() == false
    }

    def "Send GET request to get report and success response received" () {
        given: "GET request is mocked to respond with status OK and no error"
            System.setProperty("INTERNAL_URL","http://localhost:" + PORT + GET_URL)

            mockEndpoint.when(
                    HttpRequest.request(GET_URL)
                    .withMethod("GET")
                    .withHeader("X-Tor-UserID", "ap_user")
                    ).respond(
                    HttpResponse.response()
                    .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                    .withBody(REPORT_SUCCESS_RESPONSE_BODY)
                    .withStatusCode(200)
                    )

        when: "The http request has been sent and the response has been processed"
            ApplyAmosFAReportResult result = faRestClient.report("ap_user", "AAS-LTE01dg2ERBS00002-scriptname-1591953921444")

        then: "Mocked status 200 response is received and handled correctly"
            result.isComplete() == true
            result.isSuccess() == true
            result.getContextLink() == "https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255"
    }

    def "Send GET request to get report and fail response received" () {
        given: "GET request is mocked to respond with status is complete and result is fail"
            System.setProperty("INTERNAL_URL","http://localhost:" + PORT + GET_URL)

            mockEndpoint.when(
                    HttpRequest.request(GET_URL)
                    .withMethod("GET")
                    .withHeader("X-Tor-UserID", "ap_user")
                    ).respond(
                    HttpResponse.response()
                    .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                    .withBody(REPORT_FAIL_RESPONSE_BODY)
                    .withStatusCode(200)
                    )

        when: "The http request has been sent and the response has been processed"
            ApplyAmosFAReportResult result = faRestClient.report("ap_user", "AAS-LTE01dg2ERBS00002-scriptname-1591953921444")

        then: "Mocked status 200 response is received and FAIL result is handled correctly"
            result.isComplete() == true
            result.isSuccess() == false
            result.getContextLink() == "https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255"
    }
}
