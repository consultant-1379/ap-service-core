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

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.MibRootBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.modeling.common.info.ModelVersionInfo;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Unit tests for {@link NodeUserCredentialsMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeUserCredentialsMoCreatorTest {

    private static final String PASSWORD = "password";
    private static final String USERNAME = "userName";

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private MibRootBuilder mibRootBuilder;

    @Mock
    private ModelReader modelReader;

    @Spy
    private final CryptographyService cyptographyService = new CryptographyService() { // NOPMD
        @Override
        public byte[] decrypt(final byte[] bytes) {
            return PASSWORD.getBytes();
        }

        @Override
        public byte[] encrypt(final byte[] bytes) {
            return PASSWORD.getBytes();
        }
    };

    @InjectMocks
    private NodeUserCredentialsMoCreator nodeUserCredentialsCreator;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.getMibRootBuilder()).thenReturn(mibRootBuilder);
        when(mibRootBuilder.parent(any(ManagedObject.class))).thenReturn(mibRootBuilder);
        when(mibRootBuilder.namespace(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.version(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.type(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.name(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.addAttributes(anyMapOf(String.class, Object.class))).thenReturn(mibRootBuilder);
    }

    @Test
    public void whenCreateNodeUserMoAndNodeInfoHasNoUserCredentialThenNullIsReturned() {
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeUserCredentialAttributes(Collections.<String, Object> emptyMap());
        final ManagedObject result = nodeUserCredentialsCreator.create(nodeMo, nodeInfo);
        assertNull(result);
    }

    @Test
    public void whenCreateNodeUserMoThenNodeUserCredentialMoIsCreatedAndPasswordIsEncrypted() {
        final HierarchicalPrimaryTypeSpecification nodeUserCredentialsModel = mock(HierarchicalPrimaryTypeSpecification.class);
        when(modelReader.getLatestPrimaryTypeSpecification("ap", "NodeUserCredentials")).thenReturn(nodeUserCredentialsModel);
        final ModelInfo nodeUserCredentialsModelInfo = mock(ModelInfo.class);
        when(nodeUserCredentialsModel.getModelInfo()).thenReturn(nodeUserCredentialsModelInfo);
        final ModelVersionInfo nodeUserCredentialsVersionInfo = mock(ModelVersionInfo.class);
        when(nodeUserCredentialsModelInfo.getVersion()).thenReturn(nodeUserCredentialsVersionInfo);
        when(nodeUserCredentialsVersionInfo.toString()).thenReturn("1.0.0");

        final NodeInfo nodeInfo = new NodeInfo();
        final Map<String, Object> nodeUserAttributes = new HashMap<>();
        nodeUserAttributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), USERNAME);
        nodeUserAttributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), PASSWORD);
        nodeInfo.setNodeUserCredentialAttributes(nodeUserAttributes);

        nodeUserCredentialsCreator.create(nodeMo, nodeInfo);

        verify(cyptographyService, times(1)).encrypt(any(byte[].class));
    }

    @Test
    public void whenCreateEoiNodeUserMoThenNodeUserCredentialMoIsCreatedAndPasswordIsEncrypted() {
        final HierarchicalPrimaryTypeSpecification nodeUserCredentialsModel = mock(HierarchicalPrimaryTypeSpecification.class);
        when(modelReader.getLatestPrimaryTypeSpecification("ap", "NodeUserCredentials")).thenReturn(nodeUserCredentialsModel);
        final ModelInfo nodeUserCredentialsModelInfo = mock(ModelInfo.class);
        when(nodeUserCredentialsModel.getModelInfo()).thenReturn(nodeUserCredentialsModelInfo);
        final ModelVersionInfo nodeUserCredentialsVersionInfo = mock(ModelVersionInfo.class);
        when(nodeUserCredentialsModelInfo.getVersion()).thenReturn(nodeUserCredentialsVersionInfo);
        when(nodeUserCredentialsVersionInfo.toString()).thenReturn("1.0.0");

        final Map<String, Object> nodeUserAttributes = new HashMap<>();
        nodeUserAttributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), USERNAME);
        nodeUserAttributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), PASSWORD);

        nodeUserCredentialsCreator.eoiCreate(nodeMo, nodeUserAttributes);

        verify(cyptographyService, times(1)).encrypt(any(byte[].class));
    }

}
