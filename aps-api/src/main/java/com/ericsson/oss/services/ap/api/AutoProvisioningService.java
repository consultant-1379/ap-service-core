/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.api.bind.BatchBindResult;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.exception.PartialProjectDeletionException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedNodeTypeException;
import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.NodeStatus;

/**
 * Shared service interface. Interface to AutoProvisioning services.
 */
public interface AutoProvisioningService {

    /**
     * Bulk binds a group of nodes. The bind is performed best-effort. An attempt will be made to bind all nodes defined in the CSV file.
     * <p>
     * The CSV file will be parsed according to format RFC4180. Empty lines will be ignored. Lines beginning with '#' will be treated as comments.
     * <p>
     * All bind failures will be reported in the <code>BatchBindResult</code>. A bind may fail due to the following reasons:
     * <ul>
     * <li>no value specified for either the node name or hardware serial number in the CSV file</li>
     * <li>the specified node does not exist</li>
     * <li>the node is not in a valid state</li>
     * <li>the specified hardware serial number is already bound</li>
     * </ul>
     *
     * @param csvFilename
     *            the name of the CSV file
     * @param csvFileContents
     *            CSV file contents containing node names and associated serial numbers to be bound
     * @return {@link BatchBindResult}
     * @throws ApServiceException
     *             if there is a general error executing the bind
     * @see AutoProvisioningService#bind(String, String)
     */
    BatchBindResult batchBind(final String csvFilename, final byte[] csvFileContents);

    /**
     * Bind the hardware to OSS node.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @param serialNumber
     *            node serial number
     * @throws ApServiceException
     *             if there is a general error executing the bind
     * @throws HwIdAlreadyBoundException
     *             if specified serialNumber is already bound to another node
     */
    void bind(final String nodeFdn, final String serialNumber);

    /**
     * Cancel integration for a node.
     * <p>
     * If the integration of a node has failed, it may be in a state waiting to be cancelled.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @throws ApServiceException
     *             if cancel fails for some reason
     * @throws InvalidNodeStateException
     *             if cancel is called when node integration is not waiting for a cancel command
     */
    void cancel(final String nodeFdn);

    /**
     * Delete the Auto Provisioning <code>Node</code> MO and all children with the specified FDN. If ignore the deletion of
     * <code>NetworkElement</code> is set to true, then it will not be deleted.
     *
     * @param nodeFdn
     *            the FDN of the AP node to be deleted
     * @param ignoreNetworkElement
     *            ignore the deletion of NetworkElement
     */
    void deleteNode(final String nodeFdn, final boolean ignoreNetworkElement);

    /**
     * Delete the Auto Provisioning <code>Project</code> MO and all children with the specified FDN. If ignore the deletion of
     * <code>NetworkElement</code> is set to true, then it will not be deleted for any AP node.
     *
     * @param projectFdn
     *            the FDN of the AP project to be deleted
     * @param ignoreNetworkElement
     *            ignore the deletion of NetworkElement
     * @throws ApServiceException
     *             for general exception that may come back (such as CRUD or DPS exceptions etc)
     * @throws PartialProjectDeletionException
     *             if delete fails for one or more nodes
     */
    void deleteProject(final String projectFdn, final boolean ignoreNetworkElement);

    /**
     * Copies the requested file or files into the download area and returns a unique file ID. When multiple files are to be downloaded a single ZIP
     * file will be created. The fileId is used by a subsequent REST command to actually download the file.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @param artifactBaseType
     *            the {@link ArtifactBaseType} of an artifact
     * @return a unique ID that can be used to find the file in the download staging area
     * @throws ApServiceException
     *             if there is an error retrieving node artifact
     */
    String downloadNodeArtifact(final String nodeFdn, final ArtifactBaseType artifactBaseType);

    /**
     * Downloads the artifact schema and sample data files for all identifiers of the specified node type. Downloads as a ZIP file to the download
     * staging area.
     *
     * @param nodeType
     *            the type of the node
     * @return a unique ID that can be used to find the file in the download staging area
     * @throws ApServiceException
     *             if unexpected error occurs during schema or sample retrieval
     * @throws UnsupportedNodeTypeException
     *             if nodeType is a not valid node type
     */
    String downloadSchemaAndSamples(final String nodeType);

    /**
     * Downloads a CIQ file which has been placed on the file system after being retrieved from a Node Plugin.
     *
     * @param projectFdn
     *            FDN of the project
     * @param profileId
     *            the id of the profile which has the CIQ to be exported
     * @return a unique ID that can be used to find the file in the download staging area
     * @throws ApServiceException
     *             if there is an error retrieving CIQ
     */
    String exportProfileCIQ(final String projectFdn, final String profileId);

    /**
     * Orders the integration for a single node.
     *
     * @param nodeFdn
     *            the FDN of the node
     * @throws ApServiceException
     *             if order fails for some reason
     * @throws InvalidNodeStateException
     *             if order cannot be performed in the current node state
     */
    void orderNode(final String nodeFdn);

    /**
     * Orders the integration for a project defined in a ZIP archive. This method returns once order has been initiated for each of the nodes. It does
     * not wait for order to complete. No error will be returned in the event of failure to order any or all of the nodes. The individual node status
     * must be checked.
     * <p>
     * If the project archive is invalid and validation is performed then {@code <code>ValidationException</code>} will be thrown and no attempt will
     * be made to order any of the nodes in the project.
     *
     * @param fileName
     *            the file name
     * @param projectContents
     *            the project archive
     * @param validationRequired
     *            is validation required?
     * @return the FDN of the ordered project
     * @throws ApServiceException
     *             if there is an unexpected error ordering the project
     * @throws ValidationException
     *             if the project ZIP contains invalid data
     */
    String orderProject(final String fileName, final byte[] projectContents, final boolean validationRequired);

    /**
     * Order the integration for a EOI based project
     */

    String eoiOrderProject(final Map<String,Object> eoiProjectRequest);


    /**
     * Order the integration for an existing project. This method returns once order has been initiated for each of the nodes. It does not wait for
     * order to complete. Any node which is not in a valid state to be ordered will be ignored. No error will be returned in the event of failure to
     * order any or all of the nodes. The individual node status must be checked.
     *
     * @param projectFdn
     *            the FDN of the AP project
     * @throws ApServiceException
     *             if there is an unexpected error ordering the project
     */
    void orderProject(final String projectFdn);

    /**
     * Resume integration for a node.
     * <p>
     * If the integration of a node has failed, it may be in a state waiting for resume.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @throws ApServiceException
     *             if resume fails for some reason.
     * @throws InvalidNodeStateException
     *             if resume is called when node integration is not waiting for a resume command
     */
    void resume(final String nodeFdn);

    /**
     * Get a high level view of the status of all the projects in the system.
     *
     * @return {@link List} of {@link ApNodeGroupStatus}
     * @throws ApServiceException
     *             if there is an error reading the project status
     */
    List<ApNodeGroupStatus> statusAllProjects();

    /**
     * Get integration status entries for a specified AP node.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @return {@link NodeStatus}
     * @throws ApServiceException
     *             if there is an error reading the node status
     * @throws NodeNotFoundException
     *             if the specified node does not exist
     */
    NodeStatus statusNode(final String nodeFdn);

    /**
     * Gets the integration status of the specified deployment.
     *
     * @param deploymentName
     *            the deployment name
     * @return {@link ApNodeGroupStatus}
     * @throws ApServiceException
     *             if there is an error reading the deployment status
     */
    ApNodeGroupStatus statusDeployment(final String deploymentName);

    /**
     * Gets the integration status of the specified project.
     *
     * @param projectFdn
     *            the FDN of the AP project
     * @return {@link ApNodeGroupStatus}
     * @throws ApServiceException
     *             if there is an error reading the project data
     * @throws ProjectNotFoundException
     *             if the specified project does not exist
     */
    ApNodeGroupStatus statusProject(final String projectFdn);

    /**
     * Upload a configuration artifact file for a given node.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @param fileName
     *            the fileName
     * @param fileByteData
     *            the data of the artifact file
     */
    void uploadArtifact(String nodeFdn, String fileName, byte[] fileByteData);

    /**
     * Get all view data for all AP projects. Each element in the list relates to a specific project.
     *
     * @return a {@link List} of {@link MoData} containing the result of the view request
     */
    List<MoData> viewAllProjects();

    /**
     * Get view node data for a specified AP Node with the given FDN.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @return a {@link List} of {@link MoData} containing the result of the view request
     */
    List<MoData> viewNode(final String nodeFdn);

    /**
     * Get project view data for a specified AP Project with the given FDN. Each element in the list relates to an individual node or project. The
     * project will be first in the list and the child nodes will be next.
     *
     * @param projectFdn
     *            the FDN of the AP project
     * @return a {@link List} of {@link MoData} containing the result of the view request
     */
    List<MoData> viewProject(final String projectFdn);

    /**
     * Creates a Project MO
     *
     * @param name
     *            supplied project name
     * @param creator
     *            username of person who created project
     * @param description
     *            brief description of the project
     * @return {@link MoData} containing created project MO
     */
    MoData createProject(final String name, final String creator, final String description);

    /**
     * Creates a Profile MO and stores profile specific files
     *
     * @param profile
     *            {@link MoData} containing profile data used to create Profile
     * @return {@link MoData} containing created profile MO
     */
    MoData createProfile(final MoData profile);

    /**
     * Retrieves Profile MOs associated with a project
     *
     * @param projectFdn
     *            FDN of the project
     * @return List of {@link MoData} containing profile MOs for the project
     */
    List<MoData> viewProfiles(final String projectFdn);

    List<MoData> viewProfilesByProfileType(final String projectFdn,  final String dataType);

    /**
     * Updates a ConfigurationProfile MO and profile files stored.
     *
     * @param profile
     *            {@link MoData} containing profile data to be updated
     * @param projectFdn
     *            FDN of the project
     * @return {@link MoData} containing Updated ConfigurationProfile MO
     */
    MoData modifyProfile(final MoData profile, final String projectFdn);

    /**
     * Deletes a ConfigurationProfile MO and stored profile specific files
     *
     * @param projectId
     *            the Id of the AP project
     * @param profileId
     *            the id of the profile to be deleted
     */
    void deleteProfile(final String projectId, final String profileId);

    /**
     * Skip importing the failed artifact.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @throws ApServiceException
     *             if skip fails for some reason
     * @throws IllegalSkipOperationException
     *             if skip is called when node integration is not waiting for a skip command
     * @throws UnsupportedCommandException
     *             if skip is called when node doesn't support a skip command
     */
    void skip(final String nodeFdn);

    /**
     * Trigger to dump Netconf configuration snapshot from node.
     *
     * @param projectId
     *            the name of the project
     * @param profileId
     *            the id of the profile which has the GET filter information to be fetched
     * @param nodeId
     *            the name of the AP node
     * @param profileFdn
     *            the profileFdn of the AP node
     */
    void dumpSnapshot(final String projectId, final String profileId, final String nodeId, final String profileFdn);

    /**
     * Get Netconf configuration snapshot.
     *
     * @param projectId
     *            the name of the project
     * @param profileFdn
     *            the fdn of the profile which has the GET filter information to be fetched
     * @param nodeId
     *            the name of the AP node
     * @return node configuration snapshot content
     */
    String getSnapshot(final String projectId, final String profileFdn, final String nodeId);

    /**
     * Displays the requested file based on the configuration name.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @throws ApServiceException
     *             if there is an error retrieving node artifact
     */
    String downloadConfigurationFile(final String nodeFdn, final String nodeId);
}
