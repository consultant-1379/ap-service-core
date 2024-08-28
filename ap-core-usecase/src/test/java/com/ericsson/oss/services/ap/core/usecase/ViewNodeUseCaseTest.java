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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;

/**
 * Unit tests for {@link ViewNodeUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ViewNodeUseCaseTest {

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private DpsQueries dpsQueries;

    @InjectMocks
    private DpsOperations dps;

    @InjectMocks
    private ViewNodeUseCase viewUseCase;

    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setUp() {
        final DataPersistenceService dpservice = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dps, "dps", dpservice);
        Whitebox.setInternalState(dpsQueries, "dps", dpservice);
        Whitebox.setInternalState(viewUseCase, "dpsQueries", dpsQueries);
        Whitebox.setInternalState(viewUseCase, "dps", dps);
    }

    @Test
    public void whenViewNodeWithNoArtifactsOrErrorReadingArtifactThenOnlyNodeMoDataReturned() {
        final NodeDescriptor nodeDescriptor = new NodeDescriptorBuilder(ERBS)
                .build();
        dpsGenerator.generate(nodeDescriptor);

        final List<MoData> viewNodeData = viewUseCase.execute(nodeDescriptor.getNodeFdn());
        assertEquals(1, viewNodeData.size());
    }

    @Test
    public void when_view_node_then_returned_node_mo_contains_all_attributes_from_node_managed_object() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .withNodeAttribute("attr1", "value1")
                .withNodeAttribute("attr2", "value2")
                .build();
        dpsGenerator.generate(nodeDescriptor);

        final MoData nodeMo = viewUseCase.execute(nodeDescriptor.getNodeFdn()).iterator().next();
        assertEquals("value1", nodeMo.getAttribute("attr1"));
        assertEquals("value2", nodeMo.getAttribute("attr2"));
    }

    @Test
    public void when_view_node_then_returned_node_contains_additional_attributes_for_project_and_node_name() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .build();
        dpsGenerator.generate(nodeDescriptor);

        final MoData nodeMo = viewUseCase.execute(nodeDescriptor.getNodeFdn()).iterator().next();

        assertEquals(nodeDescriptor.getProjectName(), nodeMo.getAttribute("projectName"));
        assertEquals(nodeDescriptor.getNodeName(), nodeMo.getAttribute("nodeName"));
    }

    @Test
    public void when_view_node_with_artifacts_then_the_node_is_first_in_the_returned_list() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .withArtifact("type1", "/SiteBasic.xml", null, null)
                .build();
        dpsGenerator.generate(nodeDescriptor);

        final List<MoData> moData = viewUseCase.execute(nodeDescriptor.getNodeFdn());
        final MoData firstMo = moData.iterator().next();

        assertEquals("Node", firstMo.getType());
    }

    @Test
    public void when_view_node_with_artifacts_then_only_artifact_mos_with_raw_location_set_are_returned() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .withArtifact("type1", "/SiteBasic.xml", null, null)
                .withArtifact("type2", null, null, null)
                .build();
        dpsGenerator.generate(nodeDescriptor);

        final List<MoData> moData = viewUseCase.execute(nodeDescriptor.getNodeFdn() + ",NodeArtifactContainer=1");

        assertEquals(2, moData.size());
        assertEquals(NODE_FDN + ",NodeArtifactContainer=1,NodeArtifact=1", moData.get(1).getFdn());
    }
}
