/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.test.statements;

import static com.ericsson.oss.services.ap.core.test.util.TestUtils.toMapList;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.Parameter.param;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.http.entity.ContentType;
import org.mockserver.integration.ClientAndServer;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.arquillian.util.Resources;
import com.ericsson.oss.services.ap.core.test.steps.EnvironmentTestSteps;
import com.ericsson.oss.services.ap.core.test.steps.StubbedServiceStepsForDeleteNode;

import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.en.Given;

public class EnvironmentStatements extends ServiceCoreTestStatements {

    private static final String ACCEPT_HEADER = "accept";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String GET_PARAM_ATTACH = "attach";
    private static final String GET_PARAM_ATTACH_CONTENT = "content";
    private static final String CONFIGURATION_TEMPLATES_V1_PATH = Paths.get("/configuration-templates/v1/templates").toString();
    private static final String USER_ROLE_KEY = "X-Tor-UserID";
    private static final String USER_ROLE = "administrator";
    private static final String HOST = "Host";
    private static final String CONTENT_LENGTH = "content-length";
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final String USER_AGENT = "User-Agent";
    private static final String CONNECTION = "Connection";
    private static final String INTERNAL_URL = "INTERNAL_URL";
    private static final int CTS_PORT = 1080;

    @Inject
    protected EnvironmentTestSteps environmentTestSteps;

    @Inject
    private StubbedServiceStepsForDeleteNode stubbedServiceForDeleteNode;

    private static final List<ManagedObject> projects = new ArrayList<>();
    private ClientAndServer clientAndServer;

    private boolean isClientAndServerInitialized = false;

    @Override
    public void clear() {
        projects.clear();
    }

    @Given("^the system has the following project\\(s\\):$")
    public void configure_system_with_projects_and_nodes(final DataTable dataTable) {
        for (final Map<String, String> rowMap : toMapList(dataTable)) {
            projects.add(environmentTestSteps
                .create_project(
                    rowMap.get("ProjectName"),
                    rowMap.get("NodeType"),
                    Integer.parseInt(rowMap.get("NodeCount")),
                    rowMap.get("NodeNames").split(","))
                .getParent());
        }
    }

    @Given("^the system has (?:one|another) project with (\\d+) node\\(s\\) of type (.+)$")
    public void configure_system_with_projects_and_nodes(final int nodeCount, final String nodeType) {
        projects.add(environmentTestSteps.create_project(nodeType, nodeCount).getParent());
    }

    @Given("^the system has (?:one|another) project without artifacts and with (\\d+) node\\(s\\) of type (.+)$")
    public void configure_system_with_projects_and_nodes_without_artifacts(final int nodeCount, final String nodeType) {
        projects.add(environmentTestSteps.create_project_without_artifacts(nodeType, nodeCount).getParent());
    }

    @Given("^the system has the following project name (.+), node name (.+) and profile name (.+)$")
    public void configure_system_with_projects_and_nodes(final String projectName, final String nodeName, final String profileName) {
        final ManagedObject projectMo = environmentTestSteps
            .create_project(projectName, "RadioNode", 1, nodeName)
            .getParent();
        projects.add(projectMo);
        environmentTestSteps.createProfileMo(projectMo, profileName);
    }

    @Given("^the system has the following project name (.+), node type (.+), node name (.+) and file format (.+)$")
    public void configureSystemWithProjectsAndNodes(final String projectName, final String nodeType, final String nodeName, final String fileFormat) {
        projects.add(environmentTestSteps.createProject(projectName, nodeType, nodeName, fileFormat).getParent());
    }

    @Given("^the system has an smrs account that will fail to delete$")
    public void create_smrs_that_will_not_delete() {
        stubbedServiceForDeleteNode.create_smrs_service_stub_that_fails_to_delete_smrs_account();
    }

    @Given("^the client makes call with template name (.+), response returned has status code (\\d+) and content '(.+)'$")
    public void start_client_and_server_with_expectation(final String templateName, final int statusCode, final String templateContent)
        throws Throwable {
        System.setProperty(INTERNAL_URL, "http://localhost:1080/");
        createExpectationMockServerClient(templateName, statusCode, templateContent);
    }

    @Given("^the system has a project '(.+)' with (no|one) profile '(.+)'$")
    public void project_and_profile(final String projectName, final String condition, final String profileName) {
        final ManagedObject projectMo = environmentTestSteps.createProjectMo(projectName);
        if (condition.equals("one")) {
            environmentTestSteps.createProfileMo(projectMo, profileName);
        }
    }

    @Given("^the system already has a node '(.+)'$")
    public void create_node_without_project(final String nodeName) {
        environmentTestSteps.createRadioNodeMO(nodeName);
    }

    public List<ManagedObject> get_precreated_projects() {
        return Collections.unmodifiableList(projects);
    }

    public ManagedObject get_precreated_project(final int projectIndex) {
        return projects.get(projectIndex - 1);
    }

    public ManagedObject get_precreated_node(final int projectIndex, final int nodeIndex) {
        final ManagedObject parent = get_precreated_project(projectIndex);
        final ManagedObject[] children = parent.getChildren().toArray(new ManagedObject[parent.getChildren().size()]);
        return children[nodeIndex - 1];
    }

    /**
     * Finds a 1-N indexed node index for a given nodeName.
     * To be used with "get_precreated_node" when performing order sensitive operations
     * based on the node name.
     *
     * @param projectIndex int
     * @param nodeName the node name to be found
     * @return 1-N index if found, -1 otherwise
     */
    public int find_index_for_node_name(final int projectIndex, final String nodeName) {
        final ManagedObject parent = get_precreated_project(projectIndex);
        final ManagedObject[] children = parent.getChildren().toArray(new ManagedObject[parent.getChildren().size()]);
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().equals(nodeName)) {
                return i + 1;
            }
        }
        return -1;
    }

    @After
    public void cleanClient() {
        if (clientAndServer != null) {
            clientAndServer.stop();
        }
    }

    private void createExpectationMockServerClient(final String templateName, final int statusCode, final String mockCTSResponse) {
        final String ctsResponse = getFileContent(mockCTSResponse);

        if (!isClientAndServerInitialized) {
            clientAndServer = new ClientAndServer(CTS_PORT);
            isClientAndServerInitialized = true;
        }

        clientAndServer.when(
            request().withPath(CONFIGURATION_TEMPLATES_V1_PATH + "/name=" + templateName)
                .withMethod("GET")
                .withQueryStringParameter(param(GET_PARAM_ATTACH, GET_PARAM_ATTACH_CONTENT))
                .withHeader(header(HOST, ".*"))
                .withHeader(header(CONTENT_LENGTH, ".*"))
                .withHeader(header(ACCEPT_HEADER, ContentType.MULTIPART_FORM_DATA.toString()))
                .withHeader(header(ACCEPT_ENCODING, ".*"))
                .withHeader(header(USER_ROLE_KEY, USER_ROLE))
                .withHeader(header(USER_AGENT, ".*"))
                .withHeader(header(CONNECTION, ".*"))
                .withKeepAlive(true)
                .withSecure(false))
            .respond(
                response().withStatusCode(statusCode).withHeaders(header(CONTENT_TYPE_HEADER, ContentType.APPLICATION_OCTET_STREAM.toString()))
                    .withBody(ctsResponse));
    }

    public String getFileContent(final String filePath) {
        return new String(Resources.getResourceAsBytes(filePath), StandardCharsets.UTF_8);
    }
}
