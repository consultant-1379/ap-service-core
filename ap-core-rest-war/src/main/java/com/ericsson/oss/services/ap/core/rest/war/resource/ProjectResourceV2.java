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

import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;


import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


import com.ericsson.oss.services.ap.core.rest.model.EoiSuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.rest.builder.ProjectDataBuilder;
import com.ericsson.oss.services.ap.core.rest.constant.ApQueryParameter;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.model.Project;
import com.ericsson.oss.services.ap.core.rest.model.request.EoiProjectRequest;
import com.ericsson.oss.services.ap.core.rest.model.request.builder.ProjectRequestDataBuilder;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.Validate;

@Path("/v2/projects/")
@Stateless
@LocalBean
public class ProjectResourceV2 extends AbstractResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectResourceV2.class);

    @Inject
    private ProjectDataBuilder projectDataBuilder;

    @Inject
    private ProjectRequestDataBuilder projectRequestDataBuilder;

    @Context
    UriInfo uriInfo;

    @Inject
    private ArgumentResolver argumentResolver;

    @Validate
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProject(@CookieParam("iPlanetDirectoryPro") final Cookie cookie, final EoiProjectRequest eoiProjectRequest) {

        try {
            LOGGER.info("Request parameter for EOI node:- {}", eoiProjectRequest);
            final MoData project = autoProvisioningService.createProject(eoiProjectRequest.getName(), eoiProjectRequest.getCreator(), eoiProjectRequest.getDescription());
            final Project projectData = projectDataBuilder.buildProject(project);


            if (eoiProjectRequest.getNetworkElements() != null && !eoiProjectRequest.getNetworkElements().isEmpty()) {
                final Map<String, Object> eoiObjects = projectRequestDataBuilder.buildProjectRequestData(eoiProjectRequest);
                final String baseUrl = uriInfo.getBaseUri().toString().replace("/auto-provisioning/", "");
                final String sessionId = cookie.getValue();
                eoiObjects.put("baseUrl", baseUrl);
                eoiObjects.put("sessionId", sessionId);
                final String eoiIntegrationStatus = autoProvisioningService.eoiOrderProject(eoiObjects);
                final String responseMessage = eoiIntegrationStatus + " for project " + eoiProjectRequest.getName();
                return responseBuilder.buildCreated(new EoiSuccessResponse(responseMessage));
            } else {
                return responseBuilder.buildCreated(projectData);

            }

        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.CREATE_PROJECT.toString(), exception);
            return generateResponse(errorResponse);
        }

    }

    @GET
    @Path("{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryProject(@PathParam("projectId") final String projectId, @QueryParam("filter") @DefaultValue("status") final String filter) {
        LOGGER.info("Request parameter for EOI node with project Id {}", projectId);
        final String usecase = ApQueryParameter.getEnumParameterName(filter).getParameterName();
        final String projectFdn = buildApProjectFdn(projectId);
        LOGGER.info("Request usecase for EOI node {}", usecase);
        argumentResolver.resolveFdn(projectFdn, usecase);
        if (usecase.equals(ApQueryParameter.STATUS.getParameterName())) {
            final List<NodeStatus> projectStatusList = autoProvisioningService.statusProject(projectFdn).getNodesStatus();
            return responseBuilder.buildOk(projectDataBuilder.buildProjectStatus(projectStatusList, projectId));
        } else {
            final List<MoData> projectData = autoProvisioningService.viewProject(projectFdn);
            return responseBuilder.buildOk(projectDataBuilder.buildProjectProperties(projectData));
        }
    }
}

