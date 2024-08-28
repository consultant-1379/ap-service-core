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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultEoiNode;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static com.ericsson.oss.services.ap.model.NodeType.SharedCNF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;

/**
 * Tests {@link SupervisionMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SupervisionOptionsMoCreatorTest {

    private static final String NODE_TYPE = "ERBS";
    private static final String SHAREDCNF_NODE_TYPE = "SharedCNF";
    private static final String SUPERVISION_OPTIONS = "SupervisionOptions";
    private static final String AP_NAMESPACE = "ap_erbs";
    private static final String EOI_AP_NAMESPACE = "ap_eoi";
    private static final String VERSION = "1.0.0";

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private ModelData supervisionModelData;

    @InjectMocks
    private SupervisionOptionsMoCreator supervisionOptionsMoCreator;

    @Mock
    private DpsOperations dps;

    private final Map<String, Object> nodeSupervisionOptions = new HashMap<>();
    private final Map<String, Object> eoiNodeSupervisionOptions = new HashMap<>();

    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();
    private static final String FM = "fm";
    private static final String PM = "pm";
    private static final String CM = "cm";
    private static final String INVENTORY = "inventory";
    private static final String DISABLED = "disabled";
    private static final String ENABLED = "enabled";

    private NodeInfo nodeInfo;

    @Before
    public void setup() {
        final DataPersistenceService dpservice = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dps, "dps", dpservice);
        Whitebox.setInternalState(supervisionOptionsMoCreator, "dps", dps);
        when(dps.getDataPersistenceService()).thenReturn(dpservice);
        nodeSupervisionOptions.put(FM, ENABLED);
        nodeSupervisionOptions.put(PM, DISABLED);
        nodeSupervisionOptions.put(INVENTORY, ENABLED);

        eoiNodeSupervisionOptions.put(FM, ENABLED);
        eoiNodeSupervisionOptions.put(CM,ENABLED);
        eoiNodeSupervisionOptions.put(PM,DISABLED);

        nodeInfo = new NodeInfo();
        nodeInfo.setNodeType(NODE_TYPE);
        nodeInfo.setSupervisionAttributes(nodeSupervisionOptions);

        when(nodeTypeMapper.getNamespace(NODE_TYPE)).thenReturn(AP_NAMESPACE);
        when(modelReader.getLatestPrimaryTypeModel(AP_NAMESPACE, SUPERVISION_OPTIONS)).thenReturn(supervisionModelData);

        when(supervisionModelData.getVersion()).thenReturn(VERSION);
        when(supervisionModelData.getNameSpace()).thenReturn(AP_NAMESPACE);

    }

    @Test
    public void testSupervisioneMoCreated() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
            .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final ManagedObject createdMo = supervisionOptionsMoCreator.create(nodeMo, nodeInfo);

        assertEquals(ENABLED, createdMo.getAttribute(FM));
        assertEquals(DISABLED, createdMo.getAttribute(PM));
        assertEquals(ENABLED, createdMo.getAttribute(INVENTORY));
    }
    @Test
    public void testSupervisioneMoForEoiCreated() {

        when(nodeTypeMapper.getNamespace(SHAREDCNF_NODE_TYPE)).thenReturn(EOI_AP_NAMESPACE);
        when(modelReader.getLatestPrimaryTypeModel(EOI_AP_NAMESPACE, SUPERVISION_OPTIONS)).thenReturn(supervisionModelData);

        when(supervisionModelData.getVersion()).thenReturn(VERSION);
        when(supervisionModelData.getNameSpace()).thenReturn(EOI_AP_NAMESPACE);

        nodeSupervisionOptions.put(SUPERVISION_OPTIONS,eoiNodeSupervisionOptions);
        nodeSupervisionOptions.put("nodeType",SHAREDCNF_NODE_TYPE);
        final NodeDescriptor nodeDescriptor = createDefaultEoiNode(SharedCNF)
            .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final ManagedObject createdMo = supervisionOptionsMoCreator.eoiCreate(nodeMo, nodeSupervisionOptions);

        assertEquals(ENABLED, createdMo.getAttribute(FM));
        assertEquals(DISABLED, createdMo.getAttribute(PM));
        assertEquals(ENABLED, createdMo.getAttribute(CM));
    }

    @Test
    public void whenCreateEioSupervisionMoAndThereAreNoSupervisionAttributeThenNoSupervisionMoIsCreatedAndNullIsReturned() {        final NodeDescriptor nodeDescriptor = createDefaultEoiNode(SharedCNF)
        .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final ManagedObject createdMo = supervisionOptionsMoCreator.eoiCreate(nodeMo, Collections.<String, Object> emptyMap());
        assertNull(createdMo);
    }


}
