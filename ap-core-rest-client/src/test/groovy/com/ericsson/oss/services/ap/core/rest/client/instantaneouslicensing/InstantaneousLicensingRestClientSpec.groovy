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

import static com.ericsson.oss.services.ap.common.model.MoType.AI_OPTIONS
import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS
import static com.ericsson.oss.services.ap.common.model.Namespace.AP
import static org.mockserver.model.HttpResponse.response

import org.apache.http.entity.ContentType
import org.mockito.internal.util.reflection.Whitebox
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpStatusCode
import spock.lang.Subject

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject
import com.ericsson.oss.services.ap.api.exception.InstantaneousLicensingRestServiceException
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants

/**
 * InstantaneousLicensingRestClientSpec is a test class for {@link InstantaneousLicensingRestClient}
 * Positive scenarios are tested in the InstantaneousLicensingBeanSpec
 */
class InstantaneousLicensingRestClientSpec extends CdiSpecification {

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_NAME = "Node1"
    private static final String NODE_FDN = PROJECT_FDN + ",Node=" + NODE_NAME
    private static final String LICENSE_FDN = NODE_FDN + ",LicenseOptions=1"
    private static final String AUTOINTEGRATION_FDN = NODE_FDN + ",AutoIntegrationOptions=1"

    private static final String ATTRIBUTE_VALUE = NODE_NAME + "_attribute"
    private static final String REQUEST_ID = "IntegrationLicensekeyfiles_auto_administrator_011020205420"

    private static final String IL_CREATE_URL = "/oss/shm/rest/il/v1/integrationLkf"
    private static final String IL_GET_URL = "/oss/shm/rest/il/v1/integrationLkf/status/" + REQUEST_ID
    private static final String INTERNAL_URL = "INTERNAL_URL"
    private static final String USER_NAME = "user1"

    private static final String POST = "POST"
    private static final String GET = "GET"

    private static final String SUCCESSFUL_CREATE_RESPONSE =
            "{ \"requestId\": \"" + REQUEST_ID + "\"," +
            "  \"additionalInfo\": \"Message\"}"

    private static final String SUCCESSFUL_GET_RESPONSE_RUNNING =
            "{ \"requestId\": \"" + REQUEST_ID + "\"," +
            "  \"fingerprint\": \"" + ATTRIBUTE_VALUE + "\"," +
            "  \"result\": \"\"," +
            "  \"state\": \"RUNNING\"," +
            "  \"additionalInfo\": \"License request is running\" }"

    private static ClientAndServer mockServerClient

    @Subject
    @Inject
    private InstantaneousLicensingRestClient restClient

    @Inject
    private DpsOperations dpsOperations

    @MockedImplementation
    private EAccessControl accessControl

    private RuntimeConfigurableDps dps
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

        System.setProperty(INTERNAL_URL, "http://localhost:1080")
        System.setProperty("web_host_default", "localhost")
        accessControl.getAuthUserSubject() >> new ESecuritySubject(USER_NAME)
        mockServerClient.reset()
    }

    def "A POST request with error status code has exceptions handled correctly" () {
        given: "Mock server is set up to create license with bad status code"
            mockServerClient
                .when(HttpRequest.request(IL_CREATE_URL)
                .withMethod(POST)
                .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                    .withStatusCode(HttpStatusCode.BAD_REQUEST_400.code())
                    .withBody(SUCCESSFUL_CREATE_RESPONSE))

        when: "Call to request the license for the node"
            restClient.createLicenseRequest(NODE_FDN)

        then: "Exception is handled correctly"
            Exception e = thrown(InstantaneousLicensingRestServiceException.class)
            e.getMessage().startsWith("Error in HTTP response for create license job")
    }

    def "A GET request with error status code has exceptions handled correctly"() {
        given: "Mock server is set up to get a status on the license request with error status code"
            mockServerClient
                .when(HttpRequest.request(IL_GET_URL)
                .withMethod(GET)
                .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                .withHeader(HttpConstants.USERNAME_HEADER, USER_NAME))
                .respond(response()
                    .withStatusCode(HttpStatusCode.BAD_REQUEST_400.code())
                    .withBody(SUCCESSFUL_GET_RESPONSE_RUNNING))

        when: "Call to get the license status for a request id"
            restClient.getLicenseRequestStatus(REQUEST_ID)

        then: "Exception is handled correctly"
            Exception e = thrown(InstantaneousLicensingRestServiceException.class)
            e.getMessage().startsWith("Error in HTTP response for get license status job")
    }

    private ManagedObject addLicenseMo(final ManagedObject parentMo) {
        final Map<String,Object> attributes = new HashMap<>();
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
        final Map<String,Object> attributes = new HashMap<>();
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