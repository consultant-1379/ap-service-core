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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
 * Unit tests for {@link AutoIntegrationOptionsMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoIntegrationOptionsMoCreatorTest {

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    private AutoIntegrationOptionsMoCreator autoIntegrationOptionsMoCreator;

    @Mock
    private DpsOperations dpsOperations;

    private final ModelData apErbsModelData = new ModelData("ap_erbs", "1.0.0");

    private NodeInfo nodeInfo;

    private Map<String, Object> nodeAiOptions;

    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setup() {

        final DataPersistenceService dpservice = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dpsOperations, "dps", dpservice);
        Whitebox.setInternalState(autoIntegrationOptionsMoCreator, "dpsOperations", dpsOperations);

        when(dpsOperations.getDataPersistenceService()).thenReturn(dpservice);
        when(nodeTypeMapper.getNamespace("ERBS")).thenReturn("ap_erbs");
        when(modelReader.getLatestPrimaryTypeModel("ap_erbs", "AutoIntegrationOptions")).thenReturn(apErbsModelData);

        nodeAiOptions = new HashMap<>();
        nodeAiOptions.put("unlockCells", "true");

        nodeInfo = new NodeInfo();
        nodeInfo.setNodeType("ERBS");
        nodeInfo.setIntegrationAttributes(nodeAiOptions);
    }

    @Test
    public void when_create_is_successful_then_autointegation_managed_object_is_returned() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);

        final ManagedObject createdMo = autoIntegrationOptionsMoCreator.create(nodeMo, nodeInfo);
        assertEquals(dpsGenerator.getAIOptionsFdn(nodeMo.getFdn()), createdMo.getFdn());
    }
}
