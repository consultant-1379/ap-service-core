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

package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.*;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.NODE_IDENTIFIER;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.*;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps;
import com.ericsson.oss.services.ap.api.exception.ApNodeExistsException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;

/**
 * Unit tests for {@link NodeMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeMoCreatorTest {

    @InjectMocks
    private NodeMoCreator nodeMoCreator;

    @Mock
    private DdpTimer ddpTimer; // NOPMD

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private NodeInfo nodeInfo;

    private DataPersistenceService dps;

    @Mock
    private DpsOperations dpo;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Before
    public void setup() {
        final RuntimeConfigurableDps configurableDps = new RuntimeConfigurableDps();
        dps = configurableDps.build();
        configurableDps.addManagedObject().withFdn(PROJECT_FDN).build();
        configurableDps.addManagedObject().withFdn(PROJECT_FDN + ",Node=NodeAlreadyPresent").build();
        Whitebox.setInternalState(dpo, "dps", dps);
        Whitebox.setInternalState(nodeMoCreator, "dps", dpo);
        when(dpo.getDataPersistenceService()).thenReturn(dps);
        final Map<String, Object> nodeLocation = new HashMap<>();
        nodeLocation.put("latitude", "53.42911");
        nodeLocation.put("longitude", "-7.94398");
        final Map<String, Object> nodeAttributes = new HashMap<>();
        final Map<String, Object> nodeArtifactContainerAttributes = new HashMap<>();
        nodeAttributes.put("nodeType", VALID_NODE_TYPE);
        nodeAttributes.put("ipAddress", IP_ADDRESS);
        nodeAttributes.put(NodeAttribute.NODE_LOCATION.toString(), nodeLocation);
        nodeArtifactContainerAttributes.put("suspend", Boolean.FALSE);
        nodeArtifactContainerAttributes.put("strict", Boolean.TRUE);
        nodeInfo = new NodeInfo();
        nodeInfo.setName(NODE_NAME);
        nodeInfo.setNodeType(VALID_NODE_TYPE);
        nodeInfo.setNodeAttributes(nodeAttributes);
        nodeInfo.setConfigurationAttributes(nodeArtifactContainerAttributes);
        nodeInfo.setNodeLocation(nodeLocation);
        final Map<String, Object> healthCheckProfileAttributes = new HashMap<>();
        healthCheckProfileAttributes.put("healthCheckProfileName", "expansionProfile");
        nodeInfo.setHealthCheckAttributes(healthCheckProfileAttributes);
    }

    @Test
    public void whenCreateIsSuccessfulThenTheNodeManagedObjectIsReturned() {
        when(dpsQueries.findMoByNameInTransaction(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        final ManagedObject createdNodeMo = nodeMoCreator.create(PROJECT_FDN, nodeInfo);
        final Map<String, Object> nodeLocation = createdNodeMo.getAttribute(NodeAttribute.NODE_LOCATION.toString());
        assertEquals(NODE_FDN, createdNodeMo.getFdn());
        assertEquals(MoType.NODE.toString(), createdNodeMo.getType());
        assertEquals(VALID_NODE_TYPE, createdNodeMo.getAttribute("nodeType"));
        assertEquals(IP_ADDRESS, createdNodeMo.getAttribute("ipAddress"));
        assertEquals(LONGITUDE, nodeLocation.get("longitude").toString());
        assertEquals(LATITUDE, nodeLocation.get("latitude").toString());
    }

    @Test
    public void whenCreateIsSuccessfulThenTheNodeManagedObjectForRouterNodeIsReturned() {
        final Map<String, Object> nodeLocation1 = new HashMap<>();
        nodeLocation1.put("latitude", "53.42911");
        nodeLocation1.put("longitude", "-7.94398");
        final Map<String, Object> nodeAttributes = new HashMap<>();
        final Map<String, Object> nodeArtifactContainerAttributes = new HashMap<>();
        nodeAttributes.put("nodeType", VALID_NODE_TYPE_R6K_IN_OSS);
        nodeAttributes.put("ipAddress", IP_ADDRESS);
        nodeAttributes.put(NodeAttribute.NODE_LOCATION.toString(), nodeLocation1);
        nodeArtifactContainerAttributes.put("suspend", Boolean.FALSE);
        nodeArtifactContainerAttributes.put("strict", Boolean.TRUE);
        nodeInfo = new NodeInfo();
        nodeInfo.setName(NODE_NAME);
        nodeInfo.setNodeType(VALID_NODE_TYPE_R6K_IN_OSS);
        nodeInfo.setNodeAttributes(nodeAttributes);
        nodeInfo.setConfigurationAttributes(nodeArtifactContainerAttributes);
        nodeInfo.setNodeLocation(nodeLocation1);
        final Map<String, Object> healthCheckProfileAttributes = new HashMap<>();
        healthCheckProfileAttributes.put("healthCheckProfileName", "expansionProfile");
        nodeInfo.setHealthCheckAttributes(healthCheckProfileAttributes);

        when(dpsQueries.findMoByNameInTransaction(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        final ManagedObject createdNodeMo = nodeMoCreator.create(PROJECT_FDN, nodeInfo);
        final Map<String, Object> nodeLocation = createdNodeMo.getAttribute(NodeAttribute.NODE_LOCATION.toString());
        assertEquals(NODE_FDN, createdNodeMo.getFdn());
        assertEquals(MoType.NODE.toString(), createdNodeMo.getType());
        assertEquals(VALID_NODE_TYPE_R6K_IN_AP, createdNodeMo.getAttribute("nodeType"));
        assertEquals(IP_ADDRESS, createdNodeMo.getAttribute("ipAddress"));
        assertEquals(LONGITUDE, nodeLocation.get("longitude").toString());
        assertEquals(LATITUDE, nodeLocation.get("latitude").toString());
    }

    @Test
    public void whenNodeIsAlreadyPresentThenApNodeExistsExceptionIsThrown() {

        when(dpsQueries.findMoByNameInTransaction(anyString(), eq(NODE.toString()), eq(AP.toString())))
                .thenReturn(dpsQueryExecutor);
        final ManagedObject nodeMo = dps.getLiveBucket().findMoByFdn(NODE_FDN);
        final List<ManagedObject> existingNodeMos = new ArrayList<>();
        existingNodeMos.add(nodeMo);
        final Iterator<ManagedObject> itrMoList = existingNodeMos.iterator();

        when(dpsQueryExecutor.execute()).thenReturn(itrMoList);
        nodeInfo.setName("NodeAlreadyPresent");

        expectedException.expect(ApNodeExistsException.class);

        nodeMoCreator.create(PROJECT_FDN, nodeInfo);

    }


    @Test
    public void whenEoiCreateIsSuccessfulThenTheNodeManagedObjectIsReturned() {

        final Map<String, Object> nodeAttribute = new HashMap<>();
        nodeAttribute.put(ProjectRequestAttributes.NODE_NAME.toString(), NODE_NAME);
        nodeAttribute.put(NODE_TYPE.toString(), SHARED_CNF_NODE_TYPE);
        nodeAttribute.put(CNF_TYPE.toString(),"Shared-CNF");
        nodeAttribute.put(IPADDRESS.toString(),IP_ADDRESS);
        nodeAttribute.put(OSS_PREFIX.toString(),OSS_PREFIX_VALUE);
        nodeAttribute.put(NODE_IDENTIFIER.toString(),NODE_IDENTIFIER_VALUE);
        nodeAttribute.put(TIME_ZONE.toString(),"Europe/Dublin");

        when(dpsQueries.findMoByNameInTransaction(anyString(), eq(NODE.toString()), eq(AP.toString()))).thenReturn(dpsQueryExecutor);
        final ManagedObject createdNodeMo = nodeMoCreator.eoiCreate(PROJECT_FDN, nodeAttribute);

        assertEquals(NODE_FDN, createdNodeMo.getFdn());
        assertEquals(MoType.NODE.toString(), createdNodeMo.getType());
        assertEquals(SHARED_CNF_NODE_TYPE, createdNodeMo.getAttribute("nodeType"));
        assertEquals(IP_ADDRESS, createdNodeMo.getAttribute("ipAddress"));
        assertEquals(OSS_PREFIX_VALUE, createdNodeMo.getAttribute(OSS_PREFIX.toString()));
        assertEquals(NODE_IDENTIFIER_VALUE, createdNodeMo.getAttribute(NODE_IDENTIFIER.toString()));
    }

}
