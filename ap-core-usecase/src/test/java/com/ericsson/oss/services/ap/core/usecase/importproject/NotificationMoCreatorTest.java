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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.model.NodeType.RadioNode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

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
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;

/**
 * Tests {@link NotificationMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NotificationMoCreatorTest {

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private HierarchicalPrimaryTypeSpecification primaryTypeSpecification;

    @InjectMocks
    private NotificationMoCreator notificationMoCreator;

    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setUp() {
        final DataPersistenceService dps = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(notificationMoCreator, "dps", dps);

        when(modelReader.getLatestPrimaryTypeSpecification("ap_ecim", "Notification"))
            .thenReturn(primaryTypeSpecification);
        when(primaryTypeSpecification.getModelInfo()).thenReturn(new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE,
            "ap_" + VALID_NODE_TYPE.toLowerCase(Locale.US), "Notification", "1.0.0"));
        when(nodeTypeMapper.getNamespace(VALID_NODE_TYPE)).thenReturn("ap_ecim");

    }

    @Test
    public void whenCreateIsSuccessfulThenNotificationManagedObjectIsReturned() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(RadioNode).build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);

        final Map<String, Object> nodeNotificationAttributes = new HashMap<>();
        nodeNotificationAttributes.put("email", "john.smith@ericsson.com");

        final NodeInfo nodeInfo = createNodeInfo(VALID_NODE_TYPE, nodeNotificationAttributes);
        final ManagedObject createdMo = notificationMoCreator.create(nodeMo, nodeInfo);

        assertEquals(dpsGenerator.getNotificationFdn(nodeMo.getFdn()), createdMo.getFdn());
        assertEquals("john.smith@ericsson.com", createdMo.getAttribute("email"));
    }

    @Test
    public void whenCreateNotificationMoAndThereAreNoNodeNotificationAttributesThenNoNotificationMoIsCreatedAndNullIsReturned() {
        final NodeDescriptor nodeDescriptor = createDefaultNode(RadioNode).build();
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor);
        final NodeInfo nodeInfo = createNodeInfo(VALID_NODE_TYPE, Collections.<String, Object> emptyMap());
        final ManagedObject createdMo = notificationMoCreator.create(nodeMo, nodeInfo);

        assertNull(createdMo);
    }

    private NodeInfo createNodeInfo(final String nodeType, final Map<String, Object> notificationAttributes) {
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeType(nodeType);
        nodeInfo.setNotifications(notificationAttributes);
        return nodeInfo;
    }
}