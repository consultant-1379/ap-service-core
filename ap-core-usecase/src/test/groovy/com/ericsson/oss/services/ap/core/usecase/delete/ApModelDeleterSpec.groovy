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
package com.ericsson.oss.services.ap.core.usecase.delete

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN

import java.util.concurrent.Callable

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestExecutor
import com.ericsson.oss.services.ap.core.usecase.testutils.SimpleTestMosFactory
import com.ericsson.oss.services.ap.core.usecase.testutils.TestPersistenceService

class ApModelDeleterSpec extends CdiSpecification {

    private static final String NODE_1_FDN = PROJECT_FDN + ",Node=" + "TEST_NODE_1"
    private static final String NODE_2_FDN = PROJECT_FDN + ",Node=" + "TEST_NODE_2"

    @ObjectUnderTest
    private ApModelDeleter apModelDeleter

    @MockedImplementation
    private GeneratedArtifactHandler generatedArtifactHandler

    @MockedImplementation
    private RawArtifactHandler rawArtifactHandler

    @MockedImplementation
    private HealthCheckRestExecutor healthCheckExecutor;

    @MockedImplementation
    private TransactionalExecutor executor

    @Inject
    TestPersistenceService testPersistenceService

    @Inject
    SimpleTestMosFactory simpleTestMosFactory

    ManagedObject projectManagedObject = null

    void setup() {
        executor.execute(_) >> {
            args -> ((Callable) args[0]).call()
        }

        testPersistenceService.setupPersistence(cdiInjectorRule.getService(RuntimeConfigurableDps), executor)
        projectManagedObject = simpleTestMosFactory.newProject()
    }

    def "when deleteApNodeData is called for a node within a project node should be deleted"() {
        given: "A Project with multiple nodes"
        simpleTestMosFactory.newTestNodeWithName(NODE_1_FDN, projectManagedObject, Collections.emptyMap())
        simpleTestMosFactory.newTestNodeWithName(NODE_2_FDN, projectManagedObject, Collections.emptyMap())

        when: "deleteApNodeData is called"
        apModelDeleter.deleteApNodeData(NODE_1_FDN)

        then: "node will be deleted"
        1 * rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_1_FDN)
        1 * generatedArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_1_FDN)

        testPersistenceService.findByFdn(NODE_1_FDN) == null
        testPersistenceService.findByFdn(PROJECT_FDN) != null
    }

    def "when deleteApNodeData is called for the last node of a project then the project should be deleted"() {
        given: "A Project with only one node and no profiles"
        simpleTestMosFactory.newTestNodeWithName(NODE_1_FDN, projectManagedObject, Collections.emptyMap())

        when: "deleteApNodeData is called"
        apModelDeleter.deleteApNodeData(NODE_1_FDN)

        then: "project will be deleted"
        1 * rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN)
        1 * generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN)

        testPersistenceService.findByFdn(NODE_1_FDN) == null
        testPersistenceService.findByFdn(PROJECT_FDN) == null
    }

    def "when deleteApNodeData is called for the last node of a project with a profile then the project should NOT be deleted"() {
        given: "A Project with only one node and one profile"
        simpleTestMosFactory.newTestNodeWithName(NODE_1_FDN, projectManagedObject, Collections.emptyMap())
        simpleTestMosFactory.newEmptyProfile(projectManagedObject)

        when: "deleteApNodeData is called"
        apModelDeleter.deleteApNodeData(NODE_1_FDN)

        then: "node will be deleted but not the project"
        1 * rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_1_FDN)
        1 * generatedArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_1_FDN)

        testPersistenceService.findByFdn(NODE_1_FDN) == null
        testPersistenceService.findByFdn(PROJECT_FDN) != null
    }

    def "when deleteApNodeData is called for the last node of a project with a profile and artifacts could not be deleted node should be deleted"() {
        given: "A Project with only one node and one profile"
        simpleTestMosFactory.newTestNodeWithName(NODE_1_FDN, projectManagedObject, Collections.emptyMap())
        simpleTestMosFactory.newEmptyProfile(projectManagedObject)

        generatedArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_1_FDN) >> {
            throw new IOException()
        }

        rawArtifactHandler.deleteAllForNodeWithNoModelUpdate(NODE_1_FDN) >> {
            throw new IOException()
        }

        when: "deleteApNodeData is called"
        apModelDeleter.deleteApNodeData(NODE_1_FDN)

        then: "node will be deleted but not the project"
        testPersistenceService.findByFdn(NODE_1_FDN) == null
        testPersistenceService.findByFdn(PROJECT_FDN) != null
    }

    def "when deleteApNodeData is called for the last node of a project without a profile and artifacts could not be deleted project should be deleted"() {
        given: "A Project with only one node and one profile"
        simpleTestMosFactory.newTestNodeWithName(NODE_1_FDN, projectManagedObject, Collections.emptyMap())

        rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN) >> {
            throw new IOException()
        }

        generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN) >> {
            throw new IOException()
        }

        when: "deleteApNodeData is called"
        apModelDeleter.deleteApNodeData(NODE_FDN)

        then: "project will be deleted"
        testPersistenceService.findByFdn(NODE_1_FDN) == null
        testPersistenceService.findByFdn(PROJECT_FDN) == null
    }
}

