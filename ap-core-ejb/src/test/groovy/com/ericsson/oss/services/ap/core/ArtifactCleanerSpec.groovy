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
package com.ericsson.oss.services.ap.core

import static com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute.GEN_LOCATION

import javax.inject.Inject

import org.slf4j.Logger

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.sdk.resources.Resource
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember
import com.ericsson.oss.services.ap.api.resource.ResourceService
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.wfs.api.WorkflowServiceException

/**
* ArtifactCleanerSpec is a test class for {@link ArtifactCleaner}
*/
public class ArtifactCleanerSpec extends AbstractNodeStatusSpec {

    private static final String PROJECT2_FDN = "Project=Project2"
    private static final String NODE2_FDN = PROJECT2_FDN + ",Node=Node2"

    @ObjectUnderTest
    private ArtifactCleaner artifactCleaner

    @MockedImplementation
    private ArtifactResourceOperations artifactResourceOperations

    @MockedImplementation
    private GeneratedArtifactHandler generatedArtifactHandler

    @MockedImplementation
    private APServiceClusterMember apServiceClusterMembership

    @Inject
    private Logger logger

    @MockedImplementation
    private RawArtifactHandler rawArtifactHandler

    @MockedImplementation
    private ResourceService resourceService

    @MockedImplementation
    private Resource bindArtifact

    def setup () {
        apServiceClusterMembership.isMasterNode() >> true
        resourceService.listDirectories(DirectoryConfiguration.getRawDirectory()) >> ["Project1", "Project2"]
        resourceService.listDirectories(DirectoryConfiguration.getRawDirectory().replace('/', File.separator) + File.separator + "Project1") >> ["Node1"]
        resourceService.listDirectories(DirectoryConfiguration.getRawDirectory().replace('/', File.separator) + File.separator + "Project2") >> ["Node2"]
        bindArtifact.getName() >> "bindArtifact1.xml" >> "bindArtifact2.xml"
        resourceService.listFiles(DirectoryConfiguration.getBindDirectory()) >> [bindArtifact, bindArtifact]
    }

    def "Artifact cleaner should delete project and node artifacts only if the MOs does not exist"() {
        given: "a project and node with MOs and artifacts exist"
            resourceService.exists(_ as String) >> true
            ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            final def artifactAttributes = [
                    (GEN_LOCATION.toString()): DirectoryConfiguration.getGeneratedDirectory() + File.separator + "file1" + File.separator + "test1.xml"
            ]
            final ManagedObject artifactMo = MoCreatorSpec.createNodeArtifactMo(NODE_FDN + ",NodeArtifactContainer=2,NodeArtifact=2", nodeMo, artifactAttributes)

        when: "the artifact cleaner is called"
            artifactCleaner.deleteArtifacts()

        then: "the project and node with MOs should not be deleted"
            0 * generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN)
            0 * rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN)
            0 * artifactResourceOperations.deleteDirectory(DirectoryConfiguration.getGeneratedDirectory() + File.separator + "Project1" + File.separator + "Node1")
            0 * rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_FDN)
            projectMo != null
            nodeMo != null
            artifactMo != null

        and: "the artifacts for the project and node without MOs should be deleted"
            1 * generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT2_FDN)
            1 * rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT2_FDN)
            1 * artifactResourceOperations.deleteDirectory(DirectoryConfiguration.getGeneratedDirectory() + File.separator + "Project2" + File.separator + "Node2")
            1 * rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE2_FDN)
    }

    def "Artifact cleaner should delete bind artifacts only if the node MO does not exist"() {
        given: "a node with an existing MO and bind artifact"
            resourceService.exists(_ as String) >> true
            ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            final def artifactAttributes = [
                    (GEN_LOCATION.toString()): DirectoryConfiguration.getBindDirectory().replace('/', File.separator) + File.separator + "bindArtifact1.xml"
            ]
            final ManagedObject artifactMo = MoCreatorSpec.createNodeArtifactMo(NODE_FDN + ",NodeArtifactContainer=2,NodeArtifact=2", nodeMo, artifactAttributes)

        when: "the artifact cleaner is called"
            artifactCleaner.deleteArtifacts()

        then: "the bind artifact for the node with an existing MO should not be deleted"
            0 * artifactResourceOperations.deleteFile(DirectoryConfiguration.getBindDirectory().replace('/', File.separator) + File.separator + "bindArtifact1.xml")
            projectMo != null
            nodeMo != null
            artifactMo != null

        and: "the bind artifact for the node without an existing MO should be deleted"
            1 * artifactResourceOperations.deleteFile(DirectoryConfiguration.getBindDirectory().replace('/', File.separator) + File.separator + "bindArtifact2.xml")
    }

    def "Artifact cleaner should not start when raw directory and bind directory does not exist"() {
        when: "the artifact cleaner is called without an existing raw directory and bind directory"
        artifactCleaner.deleteArtifacts()

        then: "the artifact cleaner should not start"
        0 * artifactCleaner.deleteArtifactsIfMoDoesNotExist(_ as String)
        0 * artifactCleaner.deleteBindArtifactsIfMoDoesNotExist(_ as String)
    }

    def "Catch and log exception when artifacts resource does not exist"() {
        given: "an error is thrown when checking if artifact exists"
        resourceService.exists(_ as String) >> { throw new WorkflowServiceException() }

        when: "the artifact cleaner is called"
        artifactCleaner.deleteArtifacts()

        then: "the exception is logged"
        1 * logger.warn('Could not clean up artifacts from generated and raw directories', _ as Object)
    }

    def "Catch and log an exception when error occurs while deleting a node directory"() {
        given: "an error is thrown when deleting a node directory"
        resourceService.exists(_ as String) >> true
        artifactResourceOperations.deleteDirectory(_ as String) >> { throw new WorkflowServiceException() }

        when: "the artifact cleaner is called"
        artifactCleaner.deleteArtifacts()

        then: "the exception is logged for the associated node"
        1 * logger.warn('Error deleting artifacts of {} from generated and raw directories', "Node1", _ as Object)
    }

    def "Catch and log an exception when error occurs while deleting a project directory"() {
        given: "an error is thrown when deleting a project directory"
        resourceService.exists(_ as String) >> true
        generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(_ as String) >> { throw new WorkflowServiceException() }

        when: "the artifact cleaner is called"
        artifactCleaner.deleteArtifacts()

        then: "the exception is logged for the associated project"
        1 * logger.warn('Error deleting artifacts of {} from generated and raw directories', "Project2", _ as Object)
    }
}
