/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.test.statements;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;

import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.core.test.model.ProjectData;
import com.ericsson.oss.services.ap.core.test.steps.OrderTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceSteps;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Header;
import com.jayway.restassured.response.Response;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class RestStatements extends ServiceCoreTestStatements {

    private static final String FILE_PREFIX_STANDARD = "import/standard/";
    private static final Set<String> VNF_NODE_TYPES = new HashSet<>(Arrays.asList("vPP", "vSD"));

    private Response response;

    private String fileName;

    @Inject
    private StubbedServiceSteps stubbedService;

    @Inject
    protected ZipStatements zipStatements;

    @Inject
    protected OrderTestSteps orderSteps;

    private final JsonParser parser = new JsonParser();

    @Given("^the rest assured properties are set$")
    public void configure_system_with_restassured_properties() throws UnknownHostException {
        final InetAddress inetAddress = InetAddress.getLocalHost();
        RestAssured.baseURI = "http://" + inetAddress.getHostAddress();
        RestAssured.port = 8080;
    }

    @Given("^the user has an invalid file named '(.+)'$")
    public void set_batch_filename(final String fileName) {
        this.fileName = fileName;
    }

    @Given("the rest workflow services are set")
    public void rest_workflow_services_are_set() {
        createServiceStubs();
        stubbedService.create_ap_workflow_service_stub();
    }

    @When("a user of type '(.+)' requests a rest call with the uri '(.+)'$")
    public void user_requests_rest_call_with_uri(final String userType, final String uri) {
        createServiceStubs();
        setResponse(given().header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType)).when().get(uri));
    }

    public void user_requests_put_rest_call_with_file_to_uri(final String userType, final File targetFile, final String uri)
        throws IOException {
        createServiceStubs();

        setResponse(
            given()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .multiPart(targetFile)
                .body(targetFile)
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .when()
                .put(uri));
    }

    @When("a user of type '(.+)' requests a rest call with Accept header '(.+)' with the uri '(.+)'$")
    public void a_user_requests_rest_call_with_uri(final String userType, final String header, final String uri) {
        createServiceStubs();
        setResponse(
            given().header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType)).header(new Header("Accept", header)).when().get(uri));
    }

    @When("a user of type '(.+)' requests a post rest call with the uri '(.+)'$")
    public void user_makes_post_request(final String userType, final String uri) throws IOException {
        createServiceStubs();

        setResponse(
            given()
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .header(new Header("Accept", "application/json"))
                .when()
                .post(uri));
    }

    @When("a user of type '(.+)' requests a get rest call with the uri '(.+)'$")
    public void user_makes_get_request(final String userType, final String uri) throws IOException {
        createServiceStubs();

        setResponse(
            given()
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .header(new Header("Accept", "application/json"))
                .when()
                .get(uri));
    }

    @When("a user of type '(.+)' requests a post rest call with file (\\d+) with uri '(.+)'$")
    public void user_makes_post_request_with_file(final String userType, final int projectIndex, final String uri) throws IOException {
        final ProjectData projectData = zipStatements.get_project_data(projectIndex);
        final String zipFileName = FILE_PREFIX_STANDARD + projectData.getFileName();
        final byte[] projectContents = orderSteps.generate_dynamic_zip_file_for_project(projectData.getProject());
        final File targetFile = new File(zipFileName);
        FileUtils.writeByteArrayToFile(targetFile, projectContents);

        setResponse(given()
            .contentType(MediaType.MULTIPART_FORM_DATA).header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
            .multiPart(targetFile)
            .body(targetFile)
            .when()
            .post(uri));
    }

    @When("a user of type '(.+)' requests a post rest call with zip with the uri '(.+)'$")
    public void user_makes_post_request_with_zip(final String userType, final String uri) throws IOException {
        createServiceStubs();
        final String zipFileName = FILE_PREFIX_STANDARD + fileName;
        final InputStream initialStream = Resources.getResourceAsStream(zipFileName);
        final File targetFile = new File(zipFileName);

        FileUtils.copyInputStreamToFile(initialStream, targetFile);
        setResponse(
            given()
                .contentType(MediaType.MULTIPART_FORM_DATA).header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .multiPart(targetFile)
                .body(targetFile)
                .when()
                .post(uri));
    }

    @When("a user of type '(.+)' makes a post rest call with the uri '(.+)' with body '(.+)' using node type '(.+)'$")
    public void user_makes_post_request_with_body(final String userType, final String uri, final String jsonString, final String nodeType)
        throws WorkflowMessageCorrelationException {
        stubbedService.createApWorkflowServiceStub(!VNF_NODE_TYPES.contains(nodeType), "BIND");

        final JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        setResponse(
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .body(jsonObject.toString())
                .when()
                .post(uri));
    }

    @When("a user of type '(.+)' makes a put rest call with the uri '(.+)' with body '(.+)' using node type '(.+)'$")
    public void user_makes_put_request_with_body(final String userType, final String uri, final String jsonString, final String nodeType)
        throws WorkflowMessageCorrelationException {
        stubbedService.createApWorkflowServiceStub(!VNF_NODE_TYPES.contains(nodeType), "BIND");

        final JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        setResponse(
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .body(jsonObject.toString())
                .when()
                .put(uri));
    }

    @When("a user of type '(.+)' makes a post rest call with the uri '(.+)' with json body from file '(.+)'$")
    public void user_makes_post_request_with_json_body_from_file(final String userType, final String uri, final String filePath) {
        final String jsonString = new String(Resources.getResourceAsBytes(filePath), StandardCharsets.UTF_8);
        setResponse(
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .body(jsonString)
                .when()
                .post(uri));
    }

    @When("a user of type '(.+)' makes a put rest call with the uri '(.+)' with json body from file '(.+)'$")
    public void user_makes_put_request_with_json_body_from_file(final String userType, final String uri, final String filePath) {
        final String jsonString = new String(Resources.getResourceAsBytes(filePath), StandardCharsets.UTF_8);
        setResponse(
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .body(jsonString)
                .when()
                .put(uri));
    }

    @When("a user of type '(.+)' makes a delete rest call with the uri '(.+)'(?: and the request body (.*))?$")
    public void user_makes_delete_request(final String userType, final String uri, final String requestBody) {
        setResponse(
            given()
                .contentType(MediaType.APPLICATION_JSON)
                .header(new Header(ContextConstants.HTTP_HEADER_USERNAME_KEY, userType))
                .body((requestBody != null) ? requestBody : "")
                .when()
                .delete(uri));
    }

    @Then("the status code is (\\d+)")
    public void validate_response(final int statusCode) {
        response.then().statusCode(statusCode);
    }

    @Then("response body contains '(.+)'$")
    public void verifyResponseBodyContent(final String jsonString) {
        final String responseBody = response.body().asString();
        assertTrue(responseBody.contains(jsonString));
    }

    @Then("response object attribute '(.+)' is equal to '(.+)'$")
    public void verifyResponseObjectAttribute(final String jsonAttributePath, final String jsonString) {
        final Object jsonAttributeValue = response.body().jsonPath().get(jsonAttributePath);
        assertTrue(jsonAttributeValue.equals(jsonString));
    }

    @Then("response body is equal to (.*)$")
    public void verifyResponseBodyIsEqualTo(final String expectedResponseBody) {
        final String actualResponseBody = response.body().asString();
        assertEquals(expectedResponseBody, actualResponseBody);
    }

    @Then("response header '(.+)' contains '(.+)'$")
    public void verifyResponseHeader(final String headerType, final String fileType) {
        final String responseHeader = response.getHeader(headerType);
        assertTrue(responseHeader.contains(fileType));
    }

    public void setResponse(final Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    private void createServiceStubs() {
        stubbedService.create_workflow_instance_service_stub();
        stubbedService.create_workflow_query_service_stub();
    }
}
