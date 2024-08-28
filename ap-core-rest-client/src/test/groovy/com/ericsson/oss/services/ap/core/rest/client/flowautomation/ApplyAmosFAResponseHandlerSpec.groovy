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

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.ApplyAmosFAReportResult

class ApplyAmosFAResponseHandlerSpec extends CdiSpecification {

    public final ApplyAmosFAReportResult reportResult = new ApplyAmosFAReportResult();

    public final String SUCCESS_RESPONSE_BODY =
        """
    {
    \t"header" : {
    \t\t"reportTime" : "2020-06-12T10:25:51+0100",
    \t\t"flowId" : "com.ericsson.oss.services.ap.flows.amos",
    \t\t"flowVersion" : "1.0.6",
    \t\t"flowName" : "Apply AMOS Flow",
    \t\t"flowExecutionName" : "AAS-LTE01dg2ERBS00002-test.mos1591953921444",
    \t\t"startedBy" : "administrator",
    \t\t"startTime" : "2020-06-12T10:25:21+0100",
    \t\t"endTime" : "2020-06-12T10:25:34+0100",
    \t\t"status" : "COMPLETED"
    \t},
    \t"body" : {
    \t\t"reportSummary" : {
    \t\t\t"nodeName" : "LTE01dg2ERBS00002",
    \t\t\t"amosScriptName" : "test.mos",
    \t\t\t"result" : "SUCCESS",
    \t\t\t"additionalInformation" : "",
    \t\t\t"contextualLink" : "https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255"
    \t\t}
    \t}
    }
    """

    public final String FAIL_RESPONSE_BODY =
    "{\n" +
    "\t\"header\" : {\n" +
    "\t\t\"reportTime\" : \"2020-06-12T10:25:51+0100\",\n" +
    "\t\t\"flowId\" : \"com.ericsson.oss.services.ap.flows.amos\",\n" +
    "\t\t\"flowVersion\" : \"1.0.6\",\n" +
    "\t\t\"flowName\" : \"Apply AMOS Flow\",\n" +
    "\t\t\"flowExecutionName\" : \"AAS-LTE01dg2ERBS00002-test.mos1591953921444\",\n" +
    "\t\t\"startedBy\" : \"administrator\",\n" +
    "\t\t\"startTime\" : \"2020-06-12T10:25:21+0100\",\n" +
    "\t\t\"endTime\" : \"2020-06-12T10:25:34+0100\",\n" +
    "\t\t\"status\" : \"COMPLETED\"\n" +
    "\t},\n" +
    "\t\"body\" : {\n" +
    "\t\t\"reportSummary\" : {\n" +
    "\t\t\t\"nodeName\" : \"LTE01dg2ERBS00002\",\n" +
    "\t\t\t\"amosScriptName\" : \"test.mos\",\n" +
    "\t\t\t\"result\" : \"FAIL\",\n" +
    "\t\t\t\"additionalInformation\" : \"Cannot connect to MO service.\",\n" +
    "\t\t\t\"contextualLink\" : \"https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255\"\n" +
    "\t\t}\n" +
    "\t}\n" +
    "}"

    @ObjectUnderTest
    ApplyAmosFAResponseHandler faResponseHandler;

    @MockedImplementation
    HttpResponse httpResponse

    @MockedImplementation
    StatusLine statusLine

    def "When HTTP response with FAIL then result is built correctly and no exception is thrown"() {
        given: "HTTP Response with fail execution report"
            HttpEntity httpEntity = new StringEntity(FAIL_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> 200

        when: "Handling HTTP Response"
            faResponseHandler.handleReportResponse(reportResult, httpResponse, "executionname")

        then: "The result is completed and failed"
            reportResult.isComplete() == true
            reportResult.isSuccess() == false
            reportResult.getErrorMessage() == "Cannot connect to MO service."
            reportResult.getContextLink() == "https://enmapache.athtem.eei.ericsson.se/#advancedmoscripting?sbbprogress&context=a3752ba4-ac8e-11ea-9bbd-525400f83255"
    }

    def "When HTTP response with SUCCESS then result is built correctly and no exception is thrown"() {
        given: "HTTP Response with success execution report"
            HttpEntity httpEntity = new StringEntity(SUCCESS_RESPONSE_BODY, ContentType.APPLICATION_JSON)
            httpResponse.getStatusLine() >> statusLine
            httpResponse.getEntity() >> httpEntity
            statusLine.getStatusCode() >> 200

        when: "Handling HTTP Response"
            faResponseHandler.handleReportResponse(reportResult, httpResponse, "executionname")

        then: "The result is completed and success"
            reportResult.isComplete() == true
            reportResult.isSuccess() == true
    }
}
