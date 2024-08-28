/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.resource;

import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.core.rest.builder.ProjectDataBuilder;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.model.NodeSummary;
import com.ericsson.oss.services.ap.core.rest.model.Project;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiProjectRequest;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiNetworkElement;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiNetworkElement.Supervision;
import com.ericsson.oss.services.ap.core.rest.model.request.builder.ProjectRequestDataBuilder;
import com.ericsson.oss.services.ap.core.rest.war.response.ApResponseBuilder;
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ExceptionMapperFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectResourceV2Test {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @InjectMocks
    private final ProjectResourceV2 projectResourceV2 = new ProjectResourceV2();

    @Mock
    private UriInfo uriInfo;

    @Mock
    private ProjectDataBuilder projectDataBuilder;

    @Mock
    private ProjectRequestDataBuilder projectRequestDataBuilder;

    @Mock
    private AutoProvisioningService service;

    @Mock
    private ApResponseBuilder responseBuilder;

    @Mock
    private Project project;

    @Mock
    private ErrorResponse errorResponse;

    @Mock
    private Response response;

    @Mock
    private ExceptionMapperFactory exceptionMapperFactory;

    @Mock
    private ArgumentResolver argumentResolver;

    @Mock
    private DpsOperations dpsOperations;

    @Mock
    private StatusEntry statusEntry;

    @Mock
    private NodeStatus nodeStatus;

    @Mock
    private ApNodeGroupStatus apNodeGroupStatus;

    @Mock
    private ApResponseBuilder apResponseBuilder;

    private static final String DESCRIPTION = "some description";
    private static final String CREATOR = "john";
    private static final String CREATION_DATE = "2018-08-17 14:45:34";
    private static final String PROJECT_NAME = "Project1";

    @Test
    public void whenCreateFileArtifactSuccessful() throws IOException, URISyntaxException {
        EoiProjectRequest eoiProjectRequest = new EoiProjectRequest();
        EoiNetworkElement networkElement = new EoiNetworkElement();
        Supervision supervision = networkElement.new Supervision();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("projectName",PROJECT_NAME);
        attributes.put("description", DESCRIPTION);
        attributes.put("creationDate", CREATION_DATE);
        attributes.put("creator",CREATOR);

        MoData moData =  new MoData("Project=" + "PROJECT_NAME",
            attributes, null, null);

        Cookie cookie = new Cookie("iPlanetDirectoryPro","xyz");

        eoiProjectRequest.setName("Project");
        eoiProjectRequest.setCreator("creator");
        eoiProjectRequest.setDescription("description");
        eoiProjectRequest.setNetworkUsecaseType("usecasetype");
        networkElement.setCnfType("cnftype");
        networkElement.setIpAddress("1.1.1.1");
        networkElement.setModelVersion("0.0.0.0");
        networkElement.setNodeName("vcu-cp");
        networkElement.setOssPrefix("ossprefix");
        networkElement.setNeType("cnf");
        networkElement.setSubjectAltName("subAltName");
        networkElement.setUserName("userName");
        networkElement.setPassword("password");
        networkElement.setTimezone("Europe/Dublin");
        supervision.setCm(true);
        supervision.setFm(true);
        supervision.setPm(true);
        networkElement.setSupervision(supervision);

        List<EoiNetworkElement> netList =new ArrayList<>();
        netList.add(networkElement);
        eoiProjectRequest.setNetworkElements(netList);
        when(uriInfo.getBaseUri()).thenReturn(new URI("https"));
        when(service.createProject(eoiProjectRequest.getName(), eoiProjectRequest.getCreator(), eoiProjectRequest.getDescription())).thenReturn(moData);
        projectResourceV2.createProject( cookie,eoiProjectRequest);
    }

    @Test
    public void whenCreateProjectWithoutNetworkElementIsSuccessful() throws IOException, URISyntaxException {
        EoiProjectRequest eoiProjectRequest = new EoiProjectRequest();

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("projectName",PROJECT_NAME);
        attributes.put("description", DESCRIPTION);
        attributes.put("creationDate", CREATION_DATE);
        attributes.put("creator",CREATOR);

        MoData moData =  new MoData("Project=" + "PROJECT_NAME",
            attributes, null, null);

        Cookie cookie = new Cookie("iPlanetDirectoryPro","xyz");

        eoiProjectRequest.setName("Project");
        eoiProjectRequest.setCreator("creator");
        eoiProjectRequest.setDescription("description");
        eoiProjectRequest.setNetworkUsecaseType("usecasetype");
        when(uriInfo.getBaseUri()).thenReturn(new URI("https"));
        when(service.createProject(eoiProjectRequest.getName(), eoiProjectRequest.getCreator(), eoiProjectRequest.getDescription())).thenReturn(moData);
        projectResourceV2.createProject( cookie, eoiProjectRequest);
    }

    @Test
    public void getProjectStatus() throws Exception{
        List<StatusEntry> statusEntries = new ArrayList<>();
        List<NodeStatus> nodeStatuses = new ArrayList<>();
        final List<NodeSummary> nodeSummaryList = new ArrayList<>();
        final Response result = mock(Response.class);
        nodeSummaryList.add(new NodeSummary("Project01", "COMPLETED", "READY_FOR_ORDER"));
        nodeStatuses.add(new NodeStatus("Node01", "Project01", statusEntries, "READY_FOR_ORDER"));        
        Whitebox.setInternalState(projectResourceV2, "projectDataBuilder", projectDataBuilder);
        when(apNodeGroupStatus.getNodesStatus()).thenReturn(nodeStatuses);
        when(service.statusProject(PROJECT_NAME)).thenReturn(apNodeGroupStatus);
        when(apResponseBuilder.buildOk(projectDataBuilder.buildProjectStatus(nodeStatuses, PROJECT_NAME))).thenReturn(result);
        try{
            projectResourceV2.queryProject(PROJECT_NAME, "status");
        }catch(final Exception e){
            logger.warn("Error while performing action to getProjectStatus for the project {}: ", PROJECT_NAME,  e);
        }
    }

    @Test
    public void getProjectProperties() throws IOException{
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("projectName",PROJECT_NAME);
        attributes.put("description", DESCRIPTION);
        attributes.put("creationDate", CREATION_DATE);
        attributes.put("creator",CREATOR);
        MoData moData =  new MoData("Project=" + "PROJECT_NAME", attributes, null, null);
        when(service.viewProject(PROJECT_NAME)).thenReturn(Arrays.asList(moData));
        projectResourceV2.queryProject(PROJECT_NAME, "properties");
    }
}
