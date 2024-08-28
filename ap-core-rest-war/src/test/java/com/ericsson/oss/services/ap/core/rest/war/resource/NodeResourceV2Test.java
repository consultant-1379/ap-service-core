/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.core.rest.builder.NodePropertiesBuilder;
import com.ericsson.oss.services.ap.core.rest.builder.NodeStatusDataBuilder;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.nodeproperty.NodeProperties;
import com.ericsson.oss.services.ap.core.rest.war.response.ApResponseBuilder;

@RunWith(MockitoJUnitRunner.class)
public class NodeResourceV2Test {

    @InjectMocks
    private NodeResourceV2 nodeResourcev2  = new NodeResourceV2();

    @Mock
    private AutoProvisioningService service ;

    @Mock
    private ArgumentResolver argumentResolver;

    @Mock
    private NodePropertiesBuilder nodePropertiesBuilder;

    @Mock
    private NodeProperties nodeProperties = new NodeProperties();

    @Mock
    private ApResponseBuilder apResponseBuilder;

    private static final String NODE_FDN = "Project=TestProject,Node=TestNode";
    private static final String PROJECT_NAME = "TestProject";
    private static final String NODE_NAME = "TestNode";
    private static final String IP_ADDRESS = "192.168.102.100";
    private static final String NODE_TYPE = "RadioNode";

   @Test
    public void getNodeStatus() throws Exception{
      List<StatusEntry> nodeStatusEntries = new ArrayList<>();
        nodeStatusEntries.add(new StatusEntry(StatusEntryNames.NODE_UP.toString(), StatusEntryProgress.WAITING.toString(), null, null));
        NodeStatus nodeStatus = new NodeStatus(NODE_NAME, PROJECT_NAME, nodeStatusEntries, State.ORDER_COMPLETED.toString());
        when(service.statusNode(NODE_FDN)).thenReturn(nodeStatus);
        Whitebox.setInternalState(nodeResourcev2, "nodeStatusDataBuilder", new NodeStatusDataBuilder());
        final Response response = mock(Response.class);
        when(apResponseBuilder.buildOk(nodeProperties)).thenReturn(response);
        nodeResourcev2.queryNode(PROJECT_NAME, NODE_NAME, "status");
    }

   @Test
    public void getNodeProperties() throws Exception{
      final Map<String, Object> attributes = new HashMap<>();
      attributes.put("projectName",PROJECT_NAME);
      attributes.put("NodeId", NODE_NAME);
      attributes.put("ipAddress", IP_ADDRESS);
      attributes.put("nodeType",NODE_TYPE);
      MoData nodeMoData = new MoData(NODE_FDN, attributes, NODE_TYPE, null);
      final Response response = mock(Response.class);
      List<Object>  nodeAttributes = new ArrayList<>();
      nodeAttributes.add(nodeMoData);
      Whitebox.setInternalState(nodeResourcev2, "nodePropertiesBuilder", nodePropertiesBuilder);
      Whitebox.setInternalState(nodeProperties, "attributes", nodeAttributes);
      when(nodePropertiesBuilder.buildNodeProperties(Arrays.asList(nodeMoData))).thenReturn(nodeProperties);
      when(apResponseBuilder.buildOk(nodeProperties)).thenReturn(response);
      nodeResourcev2.queryNode(PROJECT_NAME, NODE_NAME, "properties");
    }
}
