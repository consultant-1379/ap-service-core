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
package com.ericsson.oss.services.ap.core.rest.war.resource;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.api.NotificationService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.workflow.messages.GetNodeConfigurationMessage;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.reader.FileDataReader;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.ResolveFdn;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.annotations.InjectFdn;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("/v1/projects/{projectId}/nodes/{nodeId}/configurations")
@Stateless
@LocalBean
public class ConfigurationResource extends AbstractResource {

    @Inject
    private Logger logger;

    @EServiceRef
    protected NotificationService notificationService;

    @Inject
    private FileDataReader fileDataReader;

    @Inject
    private ArgumentResolver argumentResolver;

    /**
     * REST endpoint to download Auto Provisioning ordered node artifacts.
     *
     * @param projectId
     *          The project ID to retrieve the project by.
     * @param nodeId
     *          The node ID to retrieve the related artifact(s) by.
     * @param nodeFdn
     *          The node fdn
     * @return Response containing the node artifact data if there are no errors, otherwise return Response containing the service error.
     */
    @ResolveFdn(usecase = UseCaseName.DOWNLOAD_ARTIFACT)
    @GET
    @Path("siteinstall")
    @Produces({MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_JSON})
    public Response downloadSiteInstall(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId, @InjectFdn final String nodeFdn) {
        try {
            final String downloadableArtifactId = autoProvisioningService.downloadNodeArtifact(nodeFdn, ArtifactBaseType.GENERATED);
            final String generatedProjectPath = DirectoryConfiguration.getDownloadDirectory() + File.separator + downloadableArtifactId;
            final File file = new File(generatedProjectPath);
            final ResponseBuilder response = Response.ok(file);
            response.header("Content-Disposition", "attachment;filename=" + downloadableArtifactId.split("_", 2)[1]);
            return response.build();
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.DOWNLOAD_ARTIFACT.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * REST endpoint to upload a configuration file to Auto Provisioning.
     * @param input
     *          The upload file as a MultipartFormDataInput.
     * @param projectId
     *          The project ID to retrieve the project by.
     * @param nodeId
     *          The node ID to retrieve the related artifact(s) by.
     * @return no content Response if there are no errors, otherwise return Response containing the service error.
     */
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadConfiguration(final MultipartFormDataInput input, @PathParam("projectId") final String projectId,
                                        @PathParam("nodeId") final String nodeId) {
        try {
            final Map<String, byte[]> fileDetails = fileDataReader.extractFileDetails(input);
            final Map.Entry<String, byte[]> entry = fileDetails.entrySet().iterator().next();
            final String nodeFdn = buildApNodeFdn(projectId, nodeId);
            argumentResolver.resolveFdn(nodeFdn, UseCaseName.UPLOAD_ARTIFACT.toString());
            autoProvisioningService.uploadArtifact(nodeFdn, entry.getKey(), entry.getValue());
            return Response.noContent().build();
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.UPLOAD_ARTIFACT.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * REST endpoint to put a preConfiguration file to Auto Provisioning.
     *
     * @param input
     *            The json format contains status and file path.
     * @param nodeId
     *            The node ID to retrieve the related artifact(s) by.
     * @return no content Response if there are no errors, otherwise return Response containing the service error.
     */
    @POST
    @Path("preConfiguration")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response putConfiguration(final GetNodeConfigurationMessage input, @PathParam("nodeId") final String nodeId) {
        try {
            notificationService.sendNotification(nodeId, GetNodeConfigurationMessage.getMessageKey(),
                    input.convertToWorkflowVariables());
            return Response.status(Response.Status.OK).build();
        } catch (final Exception exception) {
            logger.warn("Error sending workflow notification for node {}: {}", nodeId, exception.getMessage(), exception);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}