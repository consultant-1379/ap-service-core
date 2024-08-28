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
package com.ericsson.oss.services.ap.core.instantaneouslicensing

import static com.ericsson.oss.services.ap.common.model.MoType.AI_OPTIONS
import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS
import static com.ericsson.oss.services.ap.common.model.Namespace.AP
import static org.mockserver.model.HttpResponse.response

import javax.inject.Inject

import org.apache.commons.lang.StringUtils
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
import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.workflow.messages.InstantaneousLicensingMessage
import com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.InstantaneousLicensingRestClient
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal

import spock.lang.Subject

/**
 * InstantaneousLicensingBeanSpec is a test class for {@link InstantaneousLicensingBean}
 */
class InstantaneousLicensingBeanSpec extends CdiSpecification {

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_NAME = "Node1"
    private static final String NODE_FDN = PROJECT_FDN + ",Node=" + NODE_NAME
    private static final String LICENSE_FDN = NODE_FDN + ",LicenseOptions=1"
    private static final String AUTOINTEGRATION_FDN = NODE_FDN + ",AutoIntegrationOptions=1"

    private static final String ATTRIBUTE_VALUE = NODE_NAME + "_attribute"
    private static final String FAILED = "INSTANTANEOUS_LICENSING_FAILED"
    private static final String RUNNING = "INSTANTANEOUS_LICENSING_RUNNING"
    private static final String COMPLETED = "INSTANTANEOUS_LICENSING_COMPLETED"

    private static final String REQUEST_ID = "IntegrationLicensekeyfiles_auto_administrator_011020205420"

    private static final String IL_CREATE_URL = "/oss/shm/rest/il/v1/integrationLkf"
    private static final String IL_GET_URL = "/oss/shm/rest/il/v1/integrationLkf/status/" + REQUEST_ID
    private static final String INTERNAL_URL = "INTERNAL_URL"
    private static final String USER_NAME = "user1"

    private static final String POST = "POST"
    private static final String GET = "GET"

    private static final String SUCCESSFUL_CREATE_RESPONSE =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"additionalInfo\": \"<reason>\" }"

    private static final String FAILURE_CREATE_RESPONSE =
    "{ \"requestId\": \"\"," +
    "  \"additionalInfo\": \"SHM license failure\" }"

    private static final String SUCCESSFUL_GET_RESPONSE_COMPLETED =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"fingerprint\": \"" + ATTRIBUTE_VALUE + "\"," +
    "  \"result\": \"SUCCESS\"," +
    "  \"state\": \"COMPLETED\"," +
    "  \"additionalInfo\": \"\" }"

    private static final String SUCCESSFUL_GET_RESPONSE_RUNNING =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"fingerprint\": \"" + ATTRIBUTE_VALUE + "\"," +
    "  \"result\": \"\"," +
    "  \"state\": \"RUNNING\"," +
    "  \"additionalInfo\": \"License request is running\" }"

    private static final String GET_RESPONSE_RESULT_FAILED =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"fingerprint\": \"" + ATTRIBUTE_VALUE + "\"," +
    "  \"result\": \"FAILED\"," +
    "  \"state\": \"COMPLETED\"," +
    "  \"additionalInfo\": \"Failure message\" }"

    private static final String GET_RESPONSE_REQUIST_ID_NOT_FOUND =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"fingerprint\": \"\"," +
    "  \"result\": \"\"," +
    "  \"state\": \"\"," +
    "  \"additionalInfo\": \"Job details not found\" }"

    private static final String GET_RESPONSE_STATE_CANCELLED =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"fingerprint\": \"" + ATTRIBUTE_VALUE + "\"," +
    "  \"result\": \"CANCELLED\"," +
    "  \"state\": \"COMPLETED\"," +
    "  \"additionalInfo\": \"Cancel action invoked\" }"

    private static final String FAILURE_GET_RESPONSE_UNKNOWN_STATE =
    "{ \"requestId\": \"" + REQUEST_ID + "\"," +
    "  \"fingerprint\": \"" + ATTRIBUTE_VALUE + "\"," +
    "  \"result\": \"\"," +
    "  \"state\": \"UNEXPECTED\"," +
    "  \"additionalInfo\": \"SHM unexpected response\" }"

    private static ClientAndServer mockServerClient

    @Subject
    @Inject
    private InstantaneousLicensingBean bean

    @Subject
    @Inject
    private InstantaneousLicensingRestClient restClient

    @Inject
    private InstantaneousLicensingBeanSpec executor

    @Inject
    private DpsOperations dpsOperations

    @Inject
    private Logger logger

    @MockedImplementation
    private WorkflowInstanceServiceLocal wfsInstanceService

    @MockedImplementation
    private EAccessControl accessControl

    private RuntimeConfigurableDps dps

    private final Map<String, Object> correlationMessageVariables = new HashMap<>()

    private ManagedObject projectMo
    private ManagedObject nodeMo
    private ManagedObject licenseMo

    def setupSpec() {
        mockServerClient = ClientAndServer.startClientAndServer(1080)
    }

    def cleanupSpec() {
        mockServerClient.stop(true)
    }

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        MoCreatorSpec.setDps(dps)
        projectMo =  MoCreatorSpec.createProjectMo(PROJECT_FDN)
        nodeMo =  MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
        licenseMo = addLicenseMo(nodeMo)
        addAutoIntegrationMo(nodeMo)

        bean.restClient = this.restClient
        bean.logger = this.logger
        executor.logger = this.logger

        System.setProperty(INTERNAL_URL, "http://localhost:1080")
        System.setProperty("web_host_default", "localhost")
        accessControl.getAuthUserSubject() >> new ESecuritySubject(USER_NAME)
        mockServerClient.reset()
    }

    def "If workflow message correlation fails, flow should react as expected"() {
        given: "Workflow correlation call should throw WorkflowMessageCorrelationException"
        wfsInstanceService.correlateMessage(RUNNING, _ as String, _ as Map<String, Object>) >>
        { throw new WorkflowMessageCorrelationException("Exception correlating workflow message") }

        and: "Mock server is set up to create license successfully"
        mockServerClient
                        .when(HttpRequest.request(IL_CREATE_URL)
                        .withMethod(POST)
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(SUCCESSFUL_CREATE_RESPONSE))

        when: "Call to request the license for the node with an empty requestId"
        bean.requestLicense(USER_NAME, NODE_FDN, StringUtils.EMPTY)

        then: "Exception is not propagated"
        notThrown(Exception)
        1 * logger.error("Error correlating the {} message for node {}: {}",
                        RUNNING, NODE_NAME, "Exception correlating workflow message", _ as Exception)
    }

    def "If license flow creates a license request successfully, then correct messages should be correlated" () {
        given: "Expected correlation variables set"
        correlationMessageVariables.put(InstantaneousLicensingMessage.getAdditionalInfoKey(), "License requested with job name " + REQUEST_ID)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getResultKey(), StringUtils.EMPTY)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getRequestId(), REQUEST_ID)

        and: "Mock server is set up to create license successfully"
        mockServerClient
                        .when(HttpRequest.request(IL_CREATE_URL)
                        .withMethod(POST)
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(SUCCESSFUL_CREATE_RESPONSE))

        when: "Call to request the license for the node with an empty requestId"
        bean.requestLicense(USER_NAME, NODE_FDN, StringUtils.EMPTY)

        then: "Correct correlation message is sent"
        1 * wfsInstanceService.correlateMessage(RUNNING, _, correlationMessageVariables)
        0 * wfsInstanceService.correlateMessage(COMPLETED, _, _)
        0 * wfsInstanceService.correlateMessage(FAILED, _, _)
    }

    def "If a Create REST request fails, failed messages should be as expected, no exceptions are thrown"() {
        given: "Expected correlation variables set including additional info message matching additional info in REST response from SHM mock"
        correlationMessageVariables.put(InstantaneousLicensingMessage.getAdditionalInfoKey(), "Error in HTTP response for create license job, response returned additional information: SHM license failure")
        correlationMessageVariables.put(InstantaneousLicensingMessage.getResultKey(), StringUtils.EMPTY)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getRequestId(), StringUtils.EMPTY)

        and: "Mock server is set up to create license with a failure"
        mockServerClient
                        .when(HttpRequest.request(IL_CREATE_URL)
                        .withMethod(POST)
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                        .withStatusCode(HttpStatusCode.BAD_REQUEST_400.code())
                        .withBody(FAILURE_CREATE_RESPONSE))

        when: "Call to request the license for the node with a requestId"
        bean.requestLicense(USER_NAME, NODE_FDN, StringUtils.EMPTY)

        then: "Correct correlation message is sent"
        1 * wfsInstanceService.correlateMessage(FAILED, _, correlationMessageVariables)
        0 * wfsInstanceService.correlateMessage(COMPLETED, _, _)
        0 * wfsInstanceService.correlateMessage(RUNNING, _, _)
    }

    def "If the GET poll is successfully for RUNNING and COMPLETED scenarios then workflow messages should correlate as expected"() {
        given: "Expected correlation variables set"
        correlationMessageVariables.put(InstantaneousLicensingMessage.getAdditionalInfoKey(), additionalInfo)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getResultKey(), result)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getRequestId(), requestId)

        and: "Mock server is set up to get a status on the license request"
        mockServerClient
                        .when(HttpRequest.request(IL_GET_URL)
                        .withMethod(GET)
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withBody(responseSent))

        when: "Call to request the license for the node with a requestId"
        bean.requestLicense(USER_NAME, NODE_FDN, REQUEST_ID)

        then: "Correct correlation message is sent"
        1 * wfsInstanceService.correlateMessage(expectedCorrelationMessageSent, _, correlationMessageVariables)
        0 * wfsInstanceService.correlateMessage(unexpectedCorrelationMessage, _, _)
        0 * wfsInstanceService.correlateMessage(FAILED, _, _)

        where: "Data for SHM GET response returning possible SHM messages and corresponding workflow variable values that should be sent"
        responseSent                      | expectedCorrelationMessageSent | unexpectedCorrelationMessage || additionalInfo               | result            | requestId
        SUCCESSFUL_GET_RESPONSE_COMPLETED | COMPLETED                      | RUNNING                      || StringUtils.EMPTY            | "SUCCESS"         | StringUtils.EMPTY
        GET_RESPONSE_RESULT_FAILED        | COMPLETED                      | RUNNING                      || "Failure message"            | "FAILED"          | StringUtils.EMPTY
        SUCCESSFUL_GET_RESPONSE_RUNNING   | RUNNING                        | COMPLETED                    || "License request is running" | StringUtils.EMPTY | REQUEST_ID
        GET_RESPONSE_STATE_CANCELLED      | COMPLETED                      | RUNNING                      || "Cancel action invoked"      | "CANCELLED"       | StringUtils.EMPTY
    }

    def "If a GET REST request fails, failed messages should be as expected, no exceptions are thrown"() {
        given: "Expected correlation variables set including additional info message matching additional info in REST response from SHM mock"
        correlationMessageVariables.put(InstantaneousLicensingMessage.getAdditionalInfoKey(), additionalInfo)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getResultKey(), StringUtils.EMPTY)
        correlationMessageVariables.put(InstantaneousLicensingMessage.getRequestId(), StringUtils.EMPTY)

        and: "Mock server is set up to get a status on the license request"
        mockServerClient
                        .when(HttpRequest.request(IL_GET_URL)
                        .withMethod(GET)
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                        .respond(response()
                        .withStatusCode(statusCode)
                        .withBody(responseBody))

        when: "Call to request the license for the node with a requestId"
        bean.requestLicense(USER_NAME, NODE_FDN, REQUEST_ID)

        then: "Correct correlation message is sent"
        1 * wfsInstanceService.correlateMessage(FAILED, _, correlationMessageVariables)
        0 * wfsInstanceService.correlateMessage(COMPLETED, _, _)
        0 * wfsInstanceService.correlateMessage(RUNNING, _, _)
        where: "Different error scenarios"
        responseBody                       | statusCode                            | additionalInfo
        GET_RESPONSE_RESULT_FAILED         | HttpStatusCode.BAD_REQUEST_400.code() | "Error in HTTP response for get license status job, response returned additional information: Failure message"
        GET_RESPONSE_REQUIST_ID_NOT_FOUND  | HttpStatusCode.OK_200.code()          | "Job details not found"
        FAILURE_GET_RESPONSE_UNKNOWN_STATE | HttpStatusCode.OK_200.code()          | "SHM unexpected response"
    }

    private ManagedObject addLicenseMo(final ManagedObject parentMo) {
        final Map<String,Object> attributes = new HashMap<>()
        attributes.put("fingerprint", ATTRIBUTE_VALUE)
        attributes.put("softwareLicenseTargetId", ATTRIBUTE_VALUE)
        attributes.put("hardwareType", ATTRIBUTE_VALUE)
        attributes.put("radioAccessTechnologies", new ArrayList<>())
        attributes.put("groupId", ATTRIBUTE_VALUE)
        return dps.addManagedObject()
                        .withFdn(LICENSE_FDN)
                        .type(LICENSE_OPTIONS.toString())
                        .namespace(AP.toString())
                        .addAttributes(attributes)
                        .parent(parentMo)
                        .build()
    }

    private ManagedObject addAutoIntegrationMo(final ManagedObject parentMo) {
        final Map<String,Object> attributes = new HashMap<>()
        attributes.put("upgradePackageName", ATTRIBUTE_VALUE)
        return dps.addManagedObject()
                        .withFdn(AUTOINTEGRATION_FDN)
                        .type(AI_OPTIONS.toString())
                        .namespace(AP.toString())
                        .addAttributes(attributes)
                        .parent(parentMo)
                        .build()
    }
}