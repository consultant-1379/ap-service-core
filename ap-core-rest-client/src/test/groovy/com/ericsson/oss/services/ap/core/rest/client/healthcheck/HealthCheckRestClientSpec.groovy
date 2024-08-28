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
package com.ericsson.oss.services.ap.core.rest.client.healthcheck

import static org.mockserver.model.HttpResponse.response

import javax.inject.Inject

import org.apache.http.entity.ContentType
import org.mockito.internal.util.reflection.Whitebox
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpStatusCode
import org.slf4j.Logger

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException
import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject
import com.ericsson.oss.services.ap.api.exception.HealthCheckRestServiceException
import com.ericsson.oss.services.ap.api.exception.HealthCheckProfileNotFoundException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.CreateReportResponse
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.ProfileDetails
import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Subject

/**
 * HealthCheckRestClientSpec is a test class for {@link HealthCheckRestClient}
 */
class HealthCheckRestClientSpec extends CdiSpecification {

    @Subject
    @Inject
    private HealthCheckRestClient healthCheckRestClient

    @Inject
    private DpsOperations dpsOperations

    @Inject
    private HealthCheckResponseHandler responseHandler

    @Inject
    private HealthCheckRestClientSpec executor

    @Inject
    private HealthCheckMoHelper healthCheckMoUpdater

    @MockedImplementation
    private Logger logger

    @MockedImplementation
    private EAccessControl accessControl

    private RuntimeConfigurableDps dps

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = PROJECT_FDN + ",Node=Node1"
    private static final String HEALTH_CHECK_FDN = NODE_FDN + ",HealthCheck=1"
    private static final String PROFILE_NAME = "AP_Profile1"
    private static final String USER_NAME = "user1"
    private static final String NHC_URL = "/nhcservice/v1/reports/"
    private static final String NHC_GET_PROFILE_DETAILS_URL = "/nhcprofileservice/v2/profiles/"
    private static final String PRE_HEALTHCHECK = "PRE_HEALTHCHECK"
    private static final String POST_HEALTHCHECK = "POST_HEALTHCHECK"
    public static final String REPORT_ID = "12345"
    public static final String REPORT_ID2 = "56789"
    public static final String REPORT_NAME = "AutoProvisioning_2020"

    private static final String INTERNAL_URL = "INTERNAL_URL"
    private static ClientAndServer mockServerClient
    private ManagedObject projectMo
    private ManagedObject apNodeMo

    final String createResponse = "[{" +
    "\"reportTemplateId\": 2039353," +
    "\"reportName\": \"AutoProvisioning_197001191111\"," +
    "\"statusCode\": 201," +
    "\"message\": \"Report Created\","+
    "\"links\": {" +
    "\"self\": {" +
    "\"href\": \"/nhcservice/v1/reports?name=Report_administrator_147258369\" "+
    "}"+
    "}"+
    "}]"

    final String viewByNameResponse = "{\"links\":{},\"reports\":"+
    "[{\"mainReportId\":" + REPORT_ID + ",\"reportName\":\"AutoProvisioning_2020\","+
    "\"status\":\"COMPLETED\",\"result\":\"SUCCESS\",\"progressPercentage\":100.0,"+
    "\"createdBy\":\"administrator\",\"totalNodes\":1,\"periodic\":false,"+
    "\"healthStatusCounts\":{\"unhealthyNodes\":0,\"healthyNodes\":1,\"warningNodes\":0,\"undeterminedNodes\":0},"+
    "\"links\":{\"self\":{\"href\":\"/nhcservice/v1/reports/" + REPORT_ID + "\"}}}],"+
    "\"totalReports\":1}"


    final String viewByNameResponse2 = "{\"links\":{},\"reports\":"+
    "[{\"mainReportId\":" + REPORT_ID2 + ",\"reportName\":\"AutoProvisioning_2020\","+
    "\"status\":\"COMPLETED\",\"result\":\"SUCCESS\",\"progressPercentage\":100.0,"+
    "\"createdBy\":\"administrator\",\"totalNodes\":1,\"periodic\":false,"+
    "\"healthStatusCounts\":{\"unhealthyNodes\":0,\"healthyNodes\":1,\"warningNodes\":0,\"undeterminedNodes\":0},"+
    "\"links\":{\"self\":{\"href\":\"/nhcservice/v1/reports/" + REPORT_ID2 + "\"}}}],"+
    "\"totalReports\":1}"

    final String viewByIdResponse = "{\"mainReportId\":" + REPORT_ID + ",\"reportName\":\"AutoProvisioning_2020\","+
    "\"status\":\"COMPLETED\",\"result\":\"SUCCESS\",\"progressPercentage\":100.0,"+
    "\"createdBy\":\"administrator\",\"totalNodes\":1,\"periodic\":false,"+
    "\"healthStatusCounts\":{\"unhealthyNodes\":0,\"healthyNodes\":1,\"warningNodes\":0,\"undeterminedNodes\":0},"+
    "\"links\":{\"self\":{\"href\":\"/nhcservice/v1/reports/" + REPORT_ID + "\"}}}"

    final String viewByIdResponseUnhealthy = "{\"mainReportId\":" + REPORT_ID2 + ",\"reportName\":\"AutoProvisioning_2020\","+
    "\"status\":\"COMPLETED\",\"result\":\"SUCCESS\",\"progressPercentage\":100.0,"+
    "\"createdBy\":\"administrator\",\"totalNodes\":1,\"periodic\":false,"+
    "\"healthStatusCounts\":{\"unhealthyNodes\":1,\"healthyNodes\":0,\"warningNodes\":0,\"undeterminedNodes\":0},"+
    "\"links\":{\"self\":{\"href\":\"/nhcservice/v1/reports/" + REPORT_ID2 + "\"}}}"

    final String getProfileDetailsResponseWhenProfileExists = "{\"name\":\"AP_Profile1\",\"softwareVersion\":\"CXP9024418/6_R2CXS2\"," +
            "\"creationTime\":1607379541626,\"createdBy\":\"administrator\",\"userLabel\":[],\"nodeType\":\"RadioNode\"}";

    def setupSpec() {
        mockServerClient = ClientAndServer.startClientAndServer(1080)
    }

    def cleanupSpec() {
        mockServerClient.stop()
    }

    void setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        MoCreatorSpec.setDps(dps)
        System.setProperty(INTERNAL_URL,"http://localhost:1080")
        System.setProperty("web_host_default", "localhost")
        projectMo = MoCreatorSpec.createProjectMo(PROJECT_FDN)
        apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        mockServerClient.reset()
        healthCheckRestClient.logger = logger
        responseHandler.logger = logger
        executor.logger = logger
        healthCheckMoUpdater.logger = logger
        accessControl.getAuthUserSubject() >> new ESecuritySubject(USER_NAME)
    }

    def "AP should successfully receive a response from NHC when creating report"() {

        given: "Successful response received from NHC on create"
            MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
            mockServerClient
                        .when(HttpRequest.request(NHC_URL)
                        .withMethod("POST")
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                            .withStatusCode(HttpStatusCode.CREATED_201.code())
                            .withBody(createResponse))

        when: "Create request is executed"
            healthCheckRestClient.createReport(NODE_FDN)

        then: "Message logged correctly"
            1 * logger.info("Successfully created health check report")
    }

    def "AP should throw application exception if error received from NHC when creating report"() {

        given: "Unsuccessful response received from NHC"
            final String reportName = "AutoProvisioning_2020"
            final ObjectMapper objectMapper = new ObjectMapper()
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
            final CreateReportResponse createResponse = new CreateReportResponse(reportName, http_status_code, message)
            final List<CreateReportResponse> responseList = Arrays.asList(createResponse)

            mockServerClient
                        .when(HttpRequest.request(NHC_URL)
                        .withMethod("POST")
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                            .withStatusCode(http_status_code)
                            .withBody(objectMapper.writeValueAsString(responseList)))

        when: "Create request is executed"
            healthCheckRestClient.createReport(NODE_FDN)

        then: "Correct exception is thrown"
            HealthCheckRestServiceException ex = thrown()
            ex.getMessage().equals(message)

        where: "Valid inputs"
            http_status_code                                | message
            HttpStatusCode.INTERNAL_SERVER_ERROR_500.code() | "Report Creation Failed"
            HttpStatusCode.BAD_REQUEST_400.code()           | "Profile AP_Profile1 not found"
            HttpStatusCode.CONFLICT_409.code()              | "Creation of report AP_Profile_1 failed, as there is another report created with the same name."
    }

    def "AP should successfully get status from Health Service"() {

        given: "Successful response received from NHC on view"
            final ManagedObject healthCheckMo = MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
            final String mainReportId = REPORT_ID
            mockServerClient
                        .when(HttpRequest.request(NHC_URL)
                        .withMethod("GET")
                        .withQueryStringParameter("name", REPORT_NAME)
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                            .withStatusCode(HttpStatusCode.OK_200.code())
                            .withBody(viewByNameResponse))

           mockServerClient
                        .when(HttpRequest.request(NHC_URL + mainReportId)
                        .withMethod("GET")
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                            .withStatusCode(HttpStatusCode.OK_200.code())
                            .withBody(viewByIdResponse))

        when: "View request is executed"
            def viewResponse = healthCheckRestClient.viewReport(NODE_FDN, REPORT_NAME, reportPhase)
            healthCheckMo = dps.build().getLiveBucket().findMoByFdn(healthCheckMo.getFdn())

        then: "MainReportId is correct"
            String.valueOf(viewResponse.getMainReportId()) == mainReportId

        and: "Health Check MO update is successful"
            ((List<String>) healthCheckMo.getAttribute(reportIdsAttributeName)).contains(REPORT_ID)

        where: "Health Check is running either pre or post expansion"
            reportPhase      | reportIdsAttributeName
            PRE_HEALTHCHECK  | "preReportIds"
            POST_HEALTHCHECK | "postReportIds"
    }

    def "AP should throw NodeNotFoundException if profile included in NodeInfo and HealthCheck MO is not found on create"() {

        when: "Create request is executed"
            healthCheckRestClient.createReport(NODE_FDN)

        then: "Correct exception is thrown"
            NodeNotFoundException ex = thrown()
            ex.getMessage().equals(String.format("HealthCheckMo with FDN [%s] could not be found.", HEALTH_CHECK_FDN))
    }

    def "When executing view using report name returns error HTTP status code then AP logs correct message"() {

        given:"Unsuccessful response received from NHC"
            final String reportName = "AutoProvisioning"
            mockServerClient
                        .when(HttpRequest.request(NHC_URL)
                        .withMethod("GET")
                        .withQueryStringParameter("name", reportName)
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                            .withStatusCode(http_status_code)
                            .withBody(nhc_error))

        when: "View request is executed"
            healthCheckRestClient.viewReport(NODE_FDN, reportName, PRE_HEALTHCHECK)

        then: "Correct exception is thrown"
            thrown(RetriableCommandException)

        where:
            http_status_code                      | exception_message                                      | nhc_error
            HttpStatusCode.BAD_REQUEST_400.code() | "Error retrieving status of report: AutoProvisioning"  | "Invalid reportId"
    }

    def "Health check report run multiple times and MO updated with multiple pre reportIds" () {
        given: "first report done and returned unhealthy result"
            final ManagedObject healthCheckMo = MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
            mockServerClient
                    .when(HttpRequest.request(NHC_URL)
                    .withMethod("GET")
                    .withQueryStringParameter("name", REPORT_NAME)
                    .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                    .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                    .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(viewByNameResponse2))

            mockServerClient
                    .when(HttpRequest.request(NHC_URL + REPORT_ID2)
                    .withMethod("GET")
                    .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                    .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                    .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(viewByIdResponseUnhealthy))

            healthCheckRestClient.viewReport(NODE_FDN, REPORT_NAME, PRE_HEALTHCHECK)

        and: "Second healthy report is setup"
            mockServerClient
                    .when(HttpRequest.request(NHC_URL)
                    .withMethod("GET")
                    .withQueryStringParameter("name", REPORT_NAME + "2")
                    .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                    .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                    .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(viewByNameResponse))

            mockServerClient
                    .when(HttpRequest.request(NHC_URL + REPORT_ID)
                    .withMethod("GET")
                    .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                    .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                    .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(viewByIdResponse))

        when: "View Report is called for second health check"
            healthCheckRestClient.viewReport(NODE_FDN, REPORT_NAME + "2", PRE_HEALTHCHECK)
            healthCheckMo = dps.build().getLiveBucket().findMoByFdn(healthCheckMo.getFdn())

        then: "reportIds has multiple unique entries for pre report ids"
            ((List<String>) healthCheckMo.getAttribute("preReportIds")).size() == 2
            ((List<String>) healthCheckMo.getAttribute("preReportIds")).contains(REPORT_ID)
            ((List<String>) healthCheckMo.getAttribute("preReportIds")).contains(REPORT_ID2)
    }

    def "AP should successfully get a response from NHC when deleting report"() {
        given: "Successful response received from NHC on delete"
            final String mainReportIds = "preReport1,preReport2"
            mockServerClient
                        .when(HttpRequest.request(NHC_URL + mainReportIds)
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody("2 report(s) deleted successfully"))

         when: "Delete Report request is executed"
            final Set<String> reportIds = new HashSet<String>();
            reportIds.add("preReport1")
            reportIds.add("preReport2")
            healthCheckRestClient.deleteReport(reportIds)

        then: "report is deleted"
            1 * logger.info('Delete NHC report(s): status: {} message: {} ', 200, '2 report(s) deleted successfully')
    }

    def "When AP received 200 and ProfileDetails as response for get profile details request from NHC, then ProfileDetails Object is returned"() {

        given: "200 response received from NHC on getting details of profile"
        final ObjectMapper objectMapper = new ObjectMapper()
        final ProfileDetails actualProfileDetails = objectMapper.readValue(getProfileDetailsResponseWhenProfileExists, ProfileDetails.class);
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        mockServerClient
                .when(HttpRequest.request(NHC_GET_PROFILE_DETAILS_URL + PROFILE_NAME)
                        .withMethod("GET")
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(getProfileDetailsResponseWhenProfileExists))

        when: "Get profile details executed"
        ProfileDetails returnedProfileDetails = healthCheckRestClient.getProfileDetails(PROFILE_NAME);

        then: "ProfileDetails object is returned"
        actualProfileDetails.properties == returnedProfileDetails.properties
    }

    def "When AP received 404 response for get profile details request from NHC, then HealthCheckProfileNotFoundException is thrown"() {

        given: "404 response received from NHC on getting details of profile"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        mockServerClient
                .when(HttpRequest.request(NHC_GET_PROFILE_DETAILS_URL + PROFILE_NAME)
                        .withMethod("GET")
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code()))

        when: "Get profile details executed"
        healthCheckRestClient.getProfileDetails(PROFILE_NAME);

        then: "HealthCheckProfileNotFoundException exception is thrown"
        thrown(HealthCheckProfileNotFoundException)
    }

    def "When AP received 500 response for get profile details request from NHC, then HealthCheckRestServiceException is thrown"() {

        given: "500 response received from NHC on getting details of profile"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        mockServerClient
                .when(HttpRequest.request(NHC_GET_PROFILE_DETAILS_URL + PROFILE_NAME)
                        .withMethod("GET")
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                        .withStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR_500.code()))

        when: "Get profile details executed"
        healthCheckRestClient.getProfileDetails(PROFILE_NAME);

        then: "HealthCheckRestServiceException exception is thrown"
        thrown(HealthCheckRestServiceException)
    }

    def "When AP received 403 response for get profile details request from NHC, then HealthCheckRestServiceException is thrown"() {

        given: "403 response received from NHC on getting details of profile"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        mockServerClient
                .when(HttpRequest.request(NHC_GET_PROFILE_DETAILS_URL + PROFILE_NAME)
                        .withMethod("GET")
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                        .withStatusCode(HttpStatusCode.FORBIDDEN_403.code()))

        when: "Get profile details executed"
        healthCheckRestClient.getProfileDetails(PROFILE_NAME);

        then: "HealthCheckRestServiceException exception is thrown"
        thrown(HealthCheckRestServiceException)
    }

    def "When AP received 200 response for get profile details request from NHC and then exception happens while mapping the response object then HealthCheckRestServiceException is thrown"() {

        given: "200 response received from NHC on getting details of profile"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        mockServerClient
                .when(HttpRequest.request(NHC_GET_PROFILE_DETAILS_URL + PROFILE_NAME)
                        .withMethod("GET")
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(""))

        when: "Get profile details executed"
        healthCheckRestClient.getProfileDetails(PROFILE_NAME);

        then: "HealthCheckRestServiceException exception is thrown"
        thrown(HealthCheckRestServiceException)
    }
}
