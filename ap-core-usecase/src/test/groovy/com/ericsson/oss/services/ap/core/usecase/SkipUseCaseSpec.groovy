/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataBucket
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.exception.IllegalSkipOperationException
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.core.usecase.workflow.ApWorkflowServiceResolver
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowCleanUpOperations
import com.ericsson.oss.services.ap.common.workflow.messages.SkipMessage
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException

/**
 * Unit tests for {@link SkipUseCase}.
 */
class SkipUseCaseSpec extends CdiSpecification {

    @ObjectUnderTest
    private SkipUseCase skipUseCase

    @MockedImplementation
    private ManagedObject apNodeMo;

    @MockedImplementation
    private ApWorkflowServiceResolver apWorkflowServiceResolver;

    @MockedImplementation
    private AutoProvisioningWorkflowService apWorkflowService;

    @MockedImplementation
    private DataBucket liveBucket;

    @MockedImplementation
    private DpsOperations dps;

    @MockedImplementation
    private DataPersistenceService dpsService;

    @MockedImplementation
    private NodeTypeMapper nodeTypeMapper;

    @MockedImplementation
    protected WorkflowInstanceServiceLocal wfsInstanceService;

    @MockedImplementation
    protected WorkflowCleanUpOperations workflowCleanUpOperations;

    @MockedImplementation
    protected StateTransitionManagerLocal stateTransitionManager

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(WorkflowInstanceServiceLocal.class, null) >> wfsInstanceService
        serviceFinderSPI.find(StateTransitionManagerLocal.class, null) >> stateTransitionManager
        dps.getDataPersistenceService() >> dpsService
        dps.getDataPersistenceService().getLiveBucket() >> liveBucket
        liveBucket.findMoByFdn(NODE_FDN) >> apNodeMo
        apNodeMo.getAttribute("nodeType") >> VALID_NODE_TYPE
        nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE) >> VALID_NODE_TYPE.toLowerCase()
        apWorkflowServiceResolver.getApWorkflowService(VALID_NODE_TYPE.toLowerCase()) >> apWorkflowService
        skipUseCase.init()
    }

    def "when current workflow supports skip command for the node and skip usecase is executed, the skip correlate message is sent"() {

        given: "current workflow supports skip command for the node"
            apWorkflowService.isSupported(_) >> true

        when: "when skip usecase is executed"
            skipUseCase.execute(NODE_FDN)

        then: "the skip correlate message is sent"
            1 * wfsInstanceService.correlateMessage(SkipMessage.getMessageKey(), _)
    }

    def "when skip usecase is executed and WorkflowMessageCorrelationException is thrown when sending Skip correlate message, skip is failed and IllegalSkipOperationException is thrown"() {

        given: "WorkflowMessageCorrelationException is thrown when sending Skip correlate message"
            apWorkflowService.isSupported(_) >> true
            wfsInstanceService.correlateMessage(SkipMessage.getMessageKey(), _) >> {throw new WorkflowMessageCorrelationException()}

        when: "when skip usecase is executed"
            skipUseCase.execute(NODE_FDN)

        then: "skip is failed and IllegalSkipOperationException is thrown"
            thrown(IllegalSkipOperationException)
    }

    def "when skip usecase is executed and Exception is thrown when sending Skip correlate message, skip is failed and ApApplicationException is thrown"() {

        given: "Exception is thrown when sending Skip correlate message "
            apWorkflowService.isSupported(_) >> true
            wfsInstanceService.correlateMessage(SkipMessage.getMessageKey(), _) >> {throw new InvalidNodeStateException()}

        when: "when skip usecase is executed"
            skipUseCase.execute(NODE_FDN)

        then: "skip is failed and ApApplicationException is thrown"
            thrown(ApApplicationException)
    }

    def "when current workflow supports skip command for the node and skip usecase is executed, skip is failed and ApApplicationException is thrown"() {

        given: "current workflow doesn't supports skip command for the node"
            apWorkflowService.isSupported(SkipMessage.getMessageKey()) >> false

        when: "when skip usecase is executed"
            skipUseCase.execute(NODE_FDN)

        then: "skip is failed and unsupportedCommandException is thrown"
            thrown(UnsupportedCommandException)
    }
}
