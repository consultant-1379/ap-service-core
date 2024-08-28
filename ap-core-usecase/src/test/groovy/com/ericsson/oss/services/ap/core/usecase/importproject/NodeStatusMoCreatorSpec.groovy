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
package com.ericsson.oss.services.ap.core.usecase.importproject

import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_EXPANSION
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultEoiNode
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode
import static com.ericsson.oss.services.ap.model.NodeType.RadioNode
import static com.ericsson.oss.services.ap.model.NodeType.SharedCNF
import static org.junit.Assert.assertNotNull

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox;

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator

class NodeStatusMoCreatorSpec extends CdiSpecification {

    @ObjectUnderTest
    private NodeStatusMoCreator nodeStatusMoCreator

    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator()

    @Inject
    private DpsOperations dps

    NodeDescriptor nodeDescriptor

    def setup() {
        Whitebox.setInternalState(dps, "dps", dpsGenerator.getStubbedDps())
        Whitebox.setInternalState(nodeStatusMoCreator, "dps", dps)
        nodeDescriptor = createDefaultNode(RadioNode)
                .build();
    }

    def "when node status Mo created successfully THEN node status Mo should exist in system" () {
        given: "a new created node Mo"
            final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor)
            final NodeInfo nodeInfo = new NodeInfo();

        when: "create node status Mo for the node Mo successfully"
            final ManagedObject createdMo = nodeStatusMoCreator.create(nodeMo, nodeInfo)

        then: "the node status Mo should exist in the system"
            assertNotNull(dps.getDataPersistenceService().getLiveBucket().findMoByFdn(createdMo.getFdn()))
    }

    def "when node status Mo created successfully for shared-CNF THEN node status Mo should exist in system" () {
        given: "a new created node Mo"

        nodeDescriptor = createDefaultEoiNode(SharedCNF).build()
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor)
        when: "create node status Mo for the node Mo successfully"
        final ManagedObject createdMo = nodeStatusMoCreator.eoiCreate(nodeMo)

        then: "the node status Mo should exist in the system"
        assertNotNull(dps.getDataPersistenceService().getLiveBucket().findMoByFdn(createdMo.getFdn()))
    }

}
