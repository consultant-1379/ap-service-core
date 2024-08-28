/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.configuration.validation.SnapshotConfigurationValidator;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.core.rest.builder.ProfileDataBuilder;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.model.NodeSnapshot;
import com.ericsson.oss.services.ap.core.rest.model.profile.Profile;
import com.ericsson.oss.services.ap.core.rest.model.profile.ProfileData;
import com.ericsson.oss.services.ap.core.rest.model.request.builder.ProfileMoDataBuilder;
import com.ericsson.oss.services.ap.core.rest.model.request.profile.ProfileRequest;
import com.ericsson.oss.services.ap.core.rest.model.request.node.NodeRequest;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.ResolveFdn;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.Validate;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.annotations.InjectFdn;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;


@Path("v1/projects/{projectId}/profiles")
@Stateless
public class ProfileResource extends AbstractResource {

    @Inject
    private ProfileDataBuilder profileDataBuilder;

    @Inject
    private ProfileMoDataBuilder profileMoDataBuilder;

    @Inject
    private SnapshotConfigurationValidator snapshotValidator;

    @Inject
    private Logger logger;

    @Inject
    private MRExecutionRecorder recorder;

    private static final String SNAPSHOT_NAME = "NodeConfigurationSnapshot.xml";

    /**
     * Retrieve all AP Profiles under a given Parent Project MO.
     *
     * @param projectId
     *            The project ID required to retrieve any Profile(s) for that Project.
     * @param projectFdn
     *            The project fdn
     * @return Response containing list of one or more {@link Profile} Objects if they exists and there are no errors, otherwise, return Response
     *         containing the service error. It is possible that no profiles exist for given project in which case a empty list will be returned.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ResolveFdn(usecase = UseCaseName.VIEW_PROFILES)
    public Response viewProfiles(@PathParam("projectId") final String projectId, @InjectFdn final String projectFdn) {
        final List<MoData> moData = autoProvisioningService.viewProfiles(projectFdn);
        final ProfileData profileData = profileDataBuilder.buildProfileData(moData);
        return responseBuilder.buildOk(profileData);
    }

    /**
     * Retrieve all AP Profiles under a given Parent Project MO based on data type.
     *
     * @param projectId
     *            The project ID required to retrieve any Profile(s) for that Project.
     * @param dataType
     *           The data type of the profile.
     * @param projectFdn
     *            The project fdn
     * @return Response containing list of one or more {@link Profile} Objects if they exists and there are no errors, otherwise, return Response
     *         containing the service error. It is possible that no profiles exist for given project in which case a empty list will be returned.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ResolveFdn(usecase = UseCaseName.VIEW_PROFILES)
    @Path("/dataType/{dataType}")
    public Response viewProfilesByProfileType(@PathParam("projectId") final String projectId, @InjectFdn final String projectFdn,
                                              @PathParam("dataType") final String dataType) {
        final List<MoData> moData = autoProvisioningService.viewProfilesByProfileType(projectFdn, dataType);
        final ProfileData profileData = profileDataBuilder.buildProfileData(moData);
        return responseBuilder.buildOk(profileData);
    }

    /**
     * Create Profile MO and related files
     *
     * @param projectId
     *            name of project
     * @param profileRequest
     *            POJO representing POST json body
     * @return Response containing a {@link Profile} or error if exception occurs
     */
    @Validate
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProfile(@PathParam("projectId") final String projectId, final ProfileRequest profileRequest) {
        final String profileFdn = buildApProfileFdn(projectId, profileRequest.getName());
        final MoData profileMoData = autoProvisioningService.createProfile(profileMoDataBuilder.buildMoData(profileFdn, profileRequest));
        return responseBuilder.buildCreated(profileDataBuilder.buildProfile(profileMoData));
    }

    /**
     * Modify Profile MO and related files
     *
     * @param projectId
     *            name of project
     * @param profileRequest
     *            POJO representing POST json body
     * @return Response containing a {@link Profile} or error if exception occurs
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response modifyProfile(@PathParam("projectId") final String projectId, final ProfileRequest profileRequest) {
        final String profileFdn = buildApProfileFdn(projectId, profileRequest.getName());
        final MoData profileMoData = autoProvisioningService
            .modifyProfile(profileMoDataBuilder.buildMoData(profileFdn, profileRequest), buildApProjectFdn(projectId));
        return responseBuilder.buildOk(profileDataBuilder.buildProfile(profileMoData));
    }

    /**
     * Deletes a ConfigurationProfile MO and stored profile specific files
     *
     * @param projectId
     *            the ID of the AP project
     * @param profileId
     *            the ID of the profile to be deleted
     * @return Response not containing content or error if exception occurs
     */
    @DELETE
    @Path("{profileId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteProfile(@PathParam("projectId") final String projectId,
                                  @PathParam("profileId") final String profileId) {
        autoProvisioningService.deleteProfile(projectId, profileId);
        return Response.noContent().build();
    }

    /**
     * REST endpoint to export CIQ file generated by the Node Plugin.
     *
     * @param projectId
     *            The name of the AP Project
     * @param profileId
     *            The profile ID to retrieve the CIQ by.
     * @param projectFdn
     *            The project fdn
     * @return Response containing the CIQ if there are no errors, otherwise return Response containing the service error.
     */
    @ResolveFdn(usecase = UseCaseName.EXPORT_CIQ)
    @GET
    @Path("{profileId}/ciq")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response exportCIQ(@PathParam("projectId") final String projectId, @PathParam("profileId") final String profileId, @InjectFdn final String projectFdn) {
        final String downloadableArtifactId = autoProvisioningService.exportProfileCIQ(projectFdn, profileId);
        final String generatedProjectPath = DirectoryConfiguration.getDownloadDirectory() + File.separator + downloadableArtifactId;
        final File file = new File(generatedProjectPath);
        final ResponseBuilder response = Response.ok(file);

        response.header("Content-Disposition", "attachment;filename=" + downloadableArtifactId);
        if (downloadableArtifactId.endsWith(".xlsx")) {
            response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        return response.build();
    }

    /**
     * REST endpoint to trigger node configuration dump with filter generated by the Node Plugin.
     *
     * @param projectId
     *            The name of the AP Project
     * @param profileId
     *            The profile ID to retrieve the netconf snapshot.
     * @param nodeConfigurationRequest
     *            POJO representing POST json body
     * @return {@link Response}
     */
    @POST
    @Path("{profileId}/nodeDumpSnapshot")
    @Produces({MediaType.APPLICATION_JSON})
    public Response triggerNodeConfigurationDump(@PathParam("projectId") final String projectId,
                @PathParam("profileId") final String profileId, @NotNull final NodeRequest nodeConfigurationRequest) {
        final String profileFdn = buildApProfileFdn(projectId, profileId);
        logger.info("Dumping node snapshot request received for {}", profileFdn);

        try {
            recorder.recordMRExecution(MRDefinition.AP_ENHANCED_EXPANSION);
            nodeConfigurationRequest.getNodeIds().forEach(nodeId -> {
                if (snapshotValidator.readyTriggerSnapshotContent(profileFdn)) {
                    autoProvisioningService.dumpSnapshot(projectId, profileId, nodeId, profileFdn);
                }
            });
        } catch (final Exception exception) {
            logger.error("Fail to trigger snapshot dumping ", exception);
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.DUMP_SNAPSHOT.toString(), exception);
            return generateResponse(errorResponse);
        }
        return Response.status(Response.Status.OK).build();
}

    /**
     * REST endpoint to get node configuration snapshot dumped.
     *
     * @param projectId
     *            The name of the AP Project
     * @param profileId
     *            The profile ID to retrieve the netconf snapshot.
     * @param nodeId
     *            The name of the node
     * @return {@link Response}
     */
    @GET
    @Path("{profileId}/node/{nodeId}/nodeDumpSnapshot")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
    public Response getNodeConfigurationSnapshot(@PathParam("projectId") final String projectId,
                @PathParam("profileId") final String profileId, @PathParam("nodeId") final String nodeId) {
        final String profileFdn = buildApProfileFdn(projectId, profileId);

        try {
            logger.debug("Retrieving snapshot request received for {} {}", profileFdn, nodeId);
            String nodeConfigContent = null;

            if (snapshotValidator.readyGetSnapshotContent(profileFdn)) {
                nodeConfigContent = autoProvisioningService.getSnapshot(projectId, profileFdn, nodeId);
            }
            return Response.status(StringUtils.isNotBlank(nodeConfigContent) ? Response.Status.OK : Response.Status.ACCEPTED)
                           .entity(StringUtils.isNotBlank(nodeConfigContent) ? (new NodeSnapshot(SNAPSHOT_NAME, nodeConfigContent)) : null)
                           .build();
        } catch (final Exception exception) {
            logger.error("Fail to get snapshot content ", exception);
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.GET_SNAPSHOT.toString(), exception);
            return generateResponse(errorResponse);
        }
    }
}
