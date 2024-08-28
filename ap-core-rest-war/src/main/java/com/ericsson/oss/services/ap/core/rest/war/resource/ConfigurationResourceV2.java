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

import java.io.File;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.ArtifactBaseType;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.ResolveFdn;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.annotations.InjectFdn;

@Path("/v2/projects/{projectId}/nodes/{nodeId}/")
@Stateless
@LocalBean
public class ConfigurationResourceV2 extends AbstractResource {

    @Inject
    private Logger logger;

    private static final String DAY_ZERO = "dayZero";
    private static final String SITE_INSTALL = "siteInstall";

    /**
     * REST endpoint to download Auto Provisioning ordered node artifacts.
     *
     * @param projectId
     *          The project ID to retrieve the project by.
     * @param nodeId
     *          The node ID to retrieve the related artifact(s) by.
     * @param nodeFdn
     *          The node fdn
     * @param configurationName
     *          The configuration Name to retrieve the related configuration by.
     * @return Response containing the node artifact data if there are no errors, otherwise return Response containing the service error.
     */
    @ResolveFdn(usecase = UseCaseName.DOWNLOAD_ARTIFACT)
    @GET
    @Path("{configurationName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response downloadConfigurationFile(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId, @PathParam("configurationName") final String configurationName, @InjectFdn final String nodeFdn) {
        logger.info("DownloadConfigurationFile request received with projectId : {}, nodeId : {}, configurationName : {}", projectId, nodeId, configurationName );
        try {
            if(configurationName.equalsIgnoreCase(DAY_ZERO)) {
                final String generatedFilePath = autoProvisioningService.downloadConfigurationFile(nodeFdn, nodeId);
                logger.info("Generated file path for the day0 configuration for the node: {} is : {}", nodeId, generatedFilePath);
                if(generatedFilePath != null && !generatedFilePath.isEmpty()){
                    final File file = new File(generatedFilePath);
                    return Response.ok(file, "text/plain").header("content-disposition", "attachment; filename=" + nodeId+"_day0.json").build();
                } else{
                    return Response.status(Response.Status.NOT_FOUND).entity("File doesnot exist or File Generation not completed").build();
                }
            } else if (configurationName.equalsIgnoreCase(SITE_INSTALL)) {
                final String downloadableArtifactId = autoProvisioningService.downloadNodeArtifact(nodeFdn, ArtifactBaseType.GENERATED);
                final String generatedProjectPath = DirectoryConfiguration.getDownloadDirectory() + File.separator + downloadableArtifactId;
                final File file = new File(generatedProjectPath);
                return Response.ok(file).header("Content-Disposition", "attachment;filename=" + downloadableArtifactId.split("_", 2)[1]).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Configuration Name").build();
            }
        } catch (final Exception exception) {
            logger.error("Exception occurred while retrieving configuration file data: {}", exception.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(exception.getMessage()).build();
        }
    }
}
