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
package com.ericsson.oss.services.ap.core.rest.war.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import com.ericsson.oss.itpf.sdk.instrument.annotation.Profiled;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.file.File;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.core.rest.builder.ProjectDataBuilder;
import com.ericsson.oss.services.ap.core.rest.builder.ProjectZipBuilder;
import com.ericsson.oss.services.ap.core.rest.constant.ApQueryParameter;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.model.Project;
import com.ericsson.oss.services.ap.core.rest.model.SuccessResponse;
import com.ericsson.oss.services.ap.core.rest.model.request.DeleteProjectRequest;
import com.ericsson.oss.services.ap.core.rest.model.request.ProjectRequest;
import com.ericsson.oss.services.ap.core.rest.model.request.order.configurations.OrderNodeConfigurationsRequest;
import com.ericsson.oss.services.ap.core.rest.reader.FileDataReader;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.ResolveFdn;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.Validate;
import com.ericsson.oss.services.ap.core.rest.war.response.DeleteEndpointResponse;

/**
 * Entry-point for Project related resources.
 */
@Path("/v1/projects")
@Stateless
@LocalBean
public class ProjectResource extends AbstractResource {

    @Inject
    private ProjectDataBuilder projectDataBuilder;

    @Inject
    private FileDataReader fileDataReader;

    @Inject
    private ProjectZipBuilder projectZipBuilder;

    @Inject
    private MRExecutionRecorder recorder;

    @Inject
    private ArgumentResolver argumentResolver;

    /**
     * <p>
     * Retrieve information for all AP projects. A query parameter provided in the URL will determine which information is returned. If no query
     * parameter is provided in URL then the default behavior will be to return status information of the projects.
     * </p>
     *
     * @param filter
     *            Optional {@link QueryParam} to distinguish between the status or properties request.
     * @return Response with status 200 OK with projects information based on the filter.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryAllProjects(@QueryParam("filter") @DefaultValue("status") final String filter) {
        final String usecase = ApQueryParameter.getEnumParameterName(filter).getParameterName();
        if (usecase.equals(ApQueryParameter.STATUS.getParameterName())) {
            final List<ApNodeGroupStatus> statusAllProjectsData = autoProvisioningService.statusAllProjects();
            return responseBuilder.buildOk(projectDataBuilder.buildStatusAllProjects(statusAllProjectsData));
        } else {
            final List<MoData> moData = autoProvisioningService.viewAllProjects();
            return responseBuilder.buildOk(projectDataBuilder.buildProjectData(moData));
        }
    }

    /**
     * <p>
     * Retrieve information for a specified project. A query parameter provided in the URL will determine which information is returned. If no query
     * parameter is provided in URL then the default behavior will be to return status information of the project.
     * </p>
     *
     * @param projectId
     *            The project ID to retrieve the Project by.
     * @param filter
     *            Optional {@link QueryParam} to distinguish between a status or properties request.
     * @return Response with status 200 OK with project information based on the filter.
     */
    @GET
    @Path("{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryProject(@PathParam("projectId") final String projectId, @QueryParam("filter") @DefaultValue("status") final String filter) {
        final String usecase = ApQueryParameter.getEnumParameterName(filter).getParameterName();
        final String projectFdn = buildApProjectFdn(projectId);
        argumentResolver.resolveFdn(projectFdn, usecase);
        if (usecase.equals(ApQueryParameter.STATUS.getParameterName())) {
            final List<NodeStatus> projectStatusList = autoProvisioningService.statusProject(projectFdn).getNodesStatus();
            return responseBuilder.buildOk(projectDataBuilder.buildProjectStatus(projectStatusList, projectId));
        } else {
            final List<MoData> projectData = autoProvisioningService.viewProject(projectFdn);
            return responseBuilder.buildOk(projectDataBuilder.buildProjectProperties(projectData));
        }
    }

    @Validate
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProject(final ProjectRequest projectRequest) {
        try {
            final MoData project = autoProvisioningService.createProject(projectRequest.getName(), projectRequest.getCreator(),
                projectRequest.getDescription());
            final Project projectData = projectDataBuilder.buildProject(project);
            return responseBuilder.buildCreated(projectData);
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.CREATE_PROJECT.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * Import and order a given project zip
     *
     * @param input
     *            FormData object containing project zip supplied by the user
     * @param validate
     *            Optional {@link QueryParam}. No validation will be carried out on the Project if query param is false. Default value is set to true
     *            if query param is not provided.
     * @return Response status 201 Created with project FDN
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Profiled
    public Response importProjectZip(final MultipartFormDataInput input, @QueryParam("validate") @DefaultValue("true") final boolean validate) {
        try {
            final Map<String, byte[]> files = fileDataReader.extractFileDetails(input);
            final Map.Entry<String, byte[]> projectZipDetailsEntry = files.entrySet().iterator().next();
            final String projectFdn = autoProvisioningService.orderProject(projectZipDetailsEntry.getKey(), projectZipDetailsEntry.getValue(),
                validate);
            final String projectId = projectFdn.split("=")[1];
            return responseBuilder.buildCreated(new SuccessResponse(projectId));
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.ORDER_PROJECT.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * Orders the nodes generated by the node-plugin
     *
     * @param projectId
     *            the project ID to which the profile belongs
     * @param input
     *            POJO representing POST json body
     * @param validate
     *            Optional {@link QueryParam}. No validation will be carried out on the Project if query param is false. Default value is set to true
     *            if query param is not provided.
     * @return Response status 201 Created with project FDN
     */
    @POST
    @Path("{projectId}/actions/order")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ResolveFdn(usecase = UseCaseName.ORDER_PROJECT)
    public Response order(@PathParam("projectId") final String projectId, final OrderNodeConfigurationsRequest input,
        @QueryParam("validate") @DefaultValue("true") final boolean validate) {
        try {
            recorder.recordMRExecution(MRDefinition.AP_INTEGRATED_PROVISIONING);
            final Map<String, List<File>> nodeList = fileDataReader.retrieveNodeList(input);
            final byte[] projectZip = projectZipBuilder.createProjectZipFile(projectId, nodeList);
            final Map<String, byte[]> filesMap = new HashMap<>();
            filesMap.put("project.zip", projectZip);
            final Map.Entry<String, byte[]> projectZipDetailsEntry = filesMap.entrySet().iterator().next();
            autoProvisioningService.orderProject(projectZipDetailsEntry.getKey(), projectZipDetailsEntry.getValue(), validate);
            return responseBuilder.buildCreated(new SuccessResponse(projectId));
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.ORDER_PROJECT.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * Delete one or more projects by project ID.
     *
     * @param deleteProjectRequest
     *            The payload body containing the projects to delete.
     * @return {@link Response} with no content if no error(s) occur, otherwise return Response containing the service errors.
     */
    @Validate
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProjects(@NotNull final DeleteProjectRequest deleteProjectRequest) {
        final List<DeleteEndpointResponse> errorResponses = new ArrayList<>();
        final boolean ignoreNetworkElement = deleteProjectRequest.isIgnoreNetworkElement();

        deleteProjectRequest.getProjectIds().forEach(projectId -> {
            try {
                autoProvisioningService.deleteProject(buildApProjectFdn(projectId), ignoreNetworkElement);
            } catch (final Exception exception) {
                errorResponses.add(new DeleteEndpointResponse(projectId,
                    responseBuilder.buildServiceError(UseCaseName.DELETE_PROJECT.toString(), exception).getErrorTitle()));
            }
        });

        return Response
            .status(errorResponses.isEmpty() ? Response.Status.NO_CONTENT.getStatusCode() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .entity(errorResponses.isEmpty() ? null : errorResponses)
            .build();
    }

    /**
     * Delete project by project ID.
     *
     * @param projectId
     *            The project ID to delete.
     * @return {@link Response} with no content if no error(s) occur, otherwise return Response containing the service errors.
     */
    @Validate
    @DELETE
    @Path("{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProject(@PathParam("projectId") final String projectId) {
        try {
            autoProvisioningService.deleteProject(buildApProjectFdn(projectId), false);
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.DELETE_PROJECT.toString(), exception);
            return generateResponse(errorResponse);
        }
        return Response.noContent().build();
    }
}