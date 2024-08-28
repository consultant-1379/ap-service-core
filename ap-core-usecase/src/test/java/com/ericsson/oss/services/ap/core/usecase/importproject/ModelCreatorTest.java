/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;

/**
 * Unit tests for {@link ModelCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModelCreatorTest { //NOPMD - Too many fields

    @Mock
    private DdpTimer ddpTimer; // NOPMD

    @Mock
    private NodeMoCreator nodeMoCreator;

    @Mock
    private SecurityMoCreator securityModelCreator;

    @Mock
    private SupervisionOptionsMoCreator supervisionMoCreator;

    @Mock
    private NotificationMoCreator notificationMoCreator;

    @Mock
    private NodeUserCredentialsMoCreator nodeUserCredentialsMoCreator;

    @Mock
    private AutoIntegrationOptionsMoCreator autoIntegrationCreator;

    @Mock
    private LicenseOptionsMoCreator licenseOptionsMoCreator;

    @Mock
    private NodeStatusMoCreator nodeStatusMoCreator;

    @Mock
    private NodeArtifactMosCreator nodeArtifactMosCreator;

    @Mock
    private NodeDhcpMoCreator nodeDhcpMoCreator;

    @Mock
    private ControllingNodesMoCreator controllingNodesMoCreator;

    @Mock
    private HealthCheckMoCreator healthCheckMoCreator;

    @Mock
    private ManagedObject nodeMo;

    @InjectMocks
    private ModelCreator modelCreator;

    private final NodeInfo nodeInfo = new NodeInfo();
    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setup() {
        when(nodeMoCreator.create(PROJECT_FDN, nodeInfo)).thenReturn(nodeMo);
        when(nodeMo.getFdn()).thenReturn("DummyFdn");

        nodeInfo.setName(NODE_NAME);
        nodeInfo.setNodeArtifacts(new HashMap<String, List<String>>());
        nodeInfo.setNodeAttributes(new HashMap<String, Object>());
        nodeInfo.setConfigurationAttributes(new HashMap<String, Object>());

    }

    @Test
    public void whenModelCreatedTestAllNodeAndChildMosCreated() {
        modelCreator.create(PROJECT_FDN, nodeInfo);

        verify(nodeMoCreator).create(eq(PROJECT_FDN), eq(nodeInfo));
        verify(securityModelCreator).create(nodeMo, nodeInfo);
        verify(notificationMoCreator).create(nodeMo, nodeInfo);
        verify(supervisionMoCreator).create(nodeMo, nodeInfo);
        verify(autoIntegrationCreator).create(nodeMo, nodeInfo);
        verify(licenseOptionsMoCreator).create(nodeMo, nodeInfo);
        verify(nodeUserCredentialsMoCreator).create(nodeMo, nodeInfo);
        verify(nodeStatusMoCreator).create(nodeMo, nodeInfo);
        verify(nodeDhcpMoCreator).create(nodeMo, nodeInfo);
        verify(controllingNodesMoCreator).create(nodeMo, nodeInfo);
    }


    @Test
    public void whenEoiModelCreatedTestAllNodeAndChildMosCreated() {
        final Map<String,Object> nodeData = new HashMap<>();
        nodeData.put("nodeName", "Node1");
        nodeData.put("nodeType", SHARED_CNF_NODE_TYPE);
        nodeData.put("cnfType", "Shared-CNF");
        nodeData.put("ipAddress", "1.2.3.4");
        nodeData.put("ossPrefix", "subNetwork=Autoprovisioning");
        nodeData.put("nodeIdentifier","23.Q1-R68145");

        when(nodeMoCreator.eoiCreate(PROJECT_FDN, nodeData)).thenReturn(nodeMo);
        when(nodeMo.getFdn()).thenReturn("DummyFdn");

        modelCreator.eoiCreate(PROJECT_FDN, nodeData);

        verify(nodeMoCreator).eoiCreate(eq(PROJECT_FDN), eq(nodeData));
        verify(securityModelCreator).eoiCreate(nodeMo, nodeData);
        verify(supervisionMoCreator).eoiCreate(nodeMo, nodeData);
        verify(nodeUserCredentialsMoCreator).eoiCreate(nodeMo, nodeData);
        verify(nodeStatusMoCreator).eoiCreate(nodeMo);
    }

    @Test(expected = DpsPersistenceException.class)
    public void whenModelCreatedTestExceptionHandling() {
        doThrow(DpsPersistenceException.class).when(nodeMoCreator).create(PROJECT_FDN, nodeInfo);
        modelCreator.create(PROJECT_FDN, nodeInfo);
    }
}
