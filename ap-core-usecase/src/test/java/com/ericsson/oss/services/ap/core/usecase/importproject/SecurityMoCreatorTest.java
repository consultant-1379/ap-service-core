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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.SHARED_CNF_NODE_TYPE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultEoiNode;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static com.ericsson.oss.services.ap.model.NodeType.SharedCNF;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;

/**
 * Tests {@link SecurityMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityMoCreatorTest {

    @Mock
    private ModelReader modelReader;

    @Mock
    private DpsOperations dps;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private HierarchicalPrimaryTypeSpecification primaryTypeSpecification;

    @InjectMocks
    private SecurityMoCreator securityMoCreator;

    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setUp() {
        final DataPersistenceService dpservice = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dps, "dps", dpservice);
        Whitebox.setInternalState(securityMoCreator, "dps", dps);
        when(dps.getDataPersistenceService()).thenReturn(dpservice);

        when(modelReader.getLatestPrimaryTypeSpecification("ap_erbs", "Security")).thenReturn(primaryTypeSpecification);
        when(primaryTypeSpecification.getModelInfo())
                .thenReturn(new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, "ap_" + VALID_NODE_TYPE.toLowerCase(Locale.US), "Security", "1.0.0"));
        when(nodeTypeMapper.getNamespace(VALID_NODE_TYPE)).thenReturn("ap_" + VALID_NODE_TYPE.toLowerCase(Locale.US));
    }

    @Test
    public void whenCreateIsSuccessfulThenSecurityMoIsReturned() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final Map<String, Object> nodeSecurityOptions = new HashMap<>();
        nodeSecurityOptions.put("minimumSecurityLevel", "1");
        nodeSecurityOptions.put("optimumSecurityLevel", "2");
        nodeSecurityOptions.put("enrollmentMode", "SCEP");
        final NodeInfo nodeInfo = createNodeInfo(VALID_NODE_TYPE, nodeSecurityOptions);
        final ManagedObject createdMo = securityMoCreator.create(nodeMo, nodeInfo);

        final String securityFdn = dpsGenerator.getSecurityFdn(nodeMo.getFdn());
        assertEquals(securityFdn, createdMo.getFdn());
        assertEquals("1", createdMo.getAttribute("minimumSecurityLevel"));
        assertEquals("2", createdMo.getAttribute("optimumSecurityLevel"));
        assertEquals("SCEP", createdMo.getAttribute("enrollmentMode"));
    }

    @Test
    public void whenCreateSecurityMoAndThereAreNoNodeSecurityAttributesThenNoSecurityMoIsCreatedAndNullIsReturned() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final NodeInfo nodeInfo = createNodeInfo(VALID_NODE_TYPE, Collections.<String, Object> emptyMap());
        final ManagedObject createdMo = securityMoCreator.create(nodeMo, nodeInfo);

        assertNull(createdMo);
    }

    private NodeInfo createNodeInfo(final String nodeType, final Map<String, Object> securityAttributes) {
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeType(nodeType);
        nodeInfo.setSecurityAttributes(securityAttributes);
        return nodeInfo;
    }


    @Test
    public void whenCreateIsSuccessfulThenEoiSecurityMoIsReturned() {
        when(modelReader.getLatestPrimaryTypeSpecification("ap_eoi", "Security")).thenReturn(primaryTypeSpecification);
        when(primaryTypeSpecification.getModelInfo())
            .thenReturn(new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, "ap_" +"eoi".toLowerCase(Locale.US), "Security", "1.0.0"));
        when(nodeTypeMapper.getNamespace(SHARED_CNF_NODE_TYPE)).thenReturn("ap_" + "eoi".toLowerCase(Locale.US));

        final NodeDescriptor nodeDescriptor = createDefaultEoiNode(SharedCNF)
            .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("nodeType",SHARED_CNF_NODE_TYPE);
        final ManagedObject createdMo = securityMoCreator.eoiCreate(nodeMo, nodeData);

        final String securityFdn = dpsGenerator.getSecurityFdn(nodeMo.getFdn());
        assertEquals(securityFdn, createdMo.getFdn());
    }

}
