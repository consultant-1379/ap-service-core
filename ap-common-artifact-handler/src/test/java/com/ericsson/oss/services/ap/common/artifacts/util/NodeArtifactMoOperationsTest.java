/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.artifacts.util;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.itpf.security.cryptography.CryptographyServiceDecryptionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.ManagedObjectBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.query.ObjectField;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;

/**
 * Unit tests for {@link NodeArtifactMoOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeArtifactMoOperationsTest {

    private static final byte[] DECRYPTED_CONTENTS = "decryptedContents".getBytes();
    private static final byte[] UNENCRYPTED_CONTENTS = "contents".getBytes();

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> queryExecutor;

    @Mock
    private ManagedObject artifactMo;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObjectBuilder moBuilder;

    @Mock
    private ArtifactResourceOperations resourceOperations;

    @Spy
    private final CryptographyService cryptographyService = new CryptographyService() { // NOPMD

        @Override
        public byte[] decrypt(final byte[] bytes) {
            return DECRYPTED_CONTENTS;
        }

        @Override
        public byte[] encrypt(final byte[] bytes) {
            return bytes;
        }
    };

    @InjectMocks
    public NodeArtifactMoOperations artifactMoOperations;

    @Test
    public void whenGetNodeArtifactMosAndArtifactsExistsThenReturnAllArtifacts() {
        final List<ManagedObject> artifactMos = new ArrayList<>();
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(dpsQueries.findChildMosOfTypes(NODE_FDN, ObjectField.CREATED_TIME, "NodeArtifact")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(artifactMos.iterator());

        final Collection<ManagedObject> resultArtifactMos = artifactMoOperations.getNodeArtifactMos(NODE_FDN);
        assertEquals(2, resultArtifactMos.size());
    }

    @Test
    public void whenGetNodeArtifactMosAndNoArtifactsExistThenReturnEmptyList() {
        when(dpsQueries.findChildMosOfTypes(NODE_FDN, ObjectField.CREATED_TIME, "NodeArtifact")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(new ArrayList<ManagedObject>().iterator());

        final Collection<ManagedObject> artifactMos = artifactMoOperations.getNodeArtifactMos(NODE_FDN);
        assertTrue(artifactMos.isEmpty());
    }

    @Test
    public void whenGetNodeArtifactMosForTypeAndArtifactsOfTypeExistThenReturnAllArtifacts() {
        final List<ManagedObject> artifactMos = new ArrayList<>();
        final ManagedObject artifactMo2 = Mockito.mock(ManagedObject.class);
        final ManagedObject artifactMo3 = Mockito.mock(ManagedObject.class);
        final ManagedObject artifactMo4 = Mockito.mock(ManagedObject.class);
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo2);
        artifactMos.add(artifactMo3);
        artifactMos.add(artifactMo4);

        when(dpsQueries.findChildMosOfTypes(NODE_FDN, ObjectField.CREATED_TIME, "NodeArtifact")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(artifactMos.iterator());
        when(artifactMo.getFdn()).thenReturn("DummyFdn=5");
        when(artifactMo2.getFdn()).thenReturn("DummyFdn=1");
        when(artifactMo3.getFdn()).thenReturn("DummyFdn=3");
        when(artifactMo4.getFdn()).thenReturn("DummyFdn=32");
        when(artifactMo.getAttribute("type")).thenReturn("type1");
        when(artifactMo2.getAttribute("type")).thenReturn("type2");
        when(artifactMo3.getAttribute("type")).thenReturn("type1");
        when(artifactMo4.getAttribute("type")).thenReturn("type1");

        final List<ManagedObject> resultArtifactMos = (List<ManagedObject>) artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, "type1");
        assertEquals(3, resultArtifactMos.size());
        assertEquals("DummyFdn=3", resultArtifactMos.get(0).getFdn());
        assertEquals("DummyFdn=5", resultArtifactMos.get(1).getFdn());
        assertEquals("DummyFdn=32", resultArtifactMos.get(2).getFdn());
    }

    @Test
    public void whenGetNodeArtifactMosForTypeAndNoArtifactsOfTypeExistThenReturnEmptyList() {
        final List<ManagedObject> artifactMos = new ArrayList<>();
        when(dpsQueries.findChildMosOfTypes(NODE_FDN, ObjectField.CREATED_TIME, "NodeArtifact")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(artifactMos.iterator());
        when(artifactMo.getAttribute("type")).thenReturn("type1").thenReturn("type2").thenReturn("type1");

        final Collection<ManagedObject> resultArtifactMos = artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, "type3");
        assertTrue(resultArtifactMos.isEmpty());
    }

    @Test
    public void whenCreateNodeArtifactMoThenMoIsSuccessfullyCreated() {
        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.NAME.toString(), "artifact1.xml");
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), "type1");
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), "/tmp/raw/artifact1.xml");
        createParameters.put(NodeArtifactAttribute.RAW_LOCATION.toString(), null);
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), true);
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.NOT_STARTED.name());
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), "node1");
        createParameters.put(NodeArtifactAttribute.IGNORE_ERROR.toString(), true);

        final ManagedObject nodeArtifactContainerMo = Mockito.mock(ManagedObject.class);

        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.getManagedObjectBuilder()).thenReturn(moBuilder);
        when(liveBucket.findMoByFdn(NODE_FDN + ",NodeArtifactContainer=1")).thenReturn(nodeArtifactContainerMo);
        when(moBuilder.type(MoType.NODE_ARTIFACT.toString())).thenReturn(moBuilder);
        when(moBuilder.parent(nodeArtifactContainerMo)).thenReturn(moBuilder);
        when(moBuilder.addAttributes(createParameters)).thenReturn(moBuilder);
        when(moBuilder.create()).thenReturn(artifactMo);

        artifactMoOperations.createNodeArtifactMo(NODE_FDN, createParameters);
        verify(moBuilder).create();
    }

    @Test
    public void whenDeleteNodeArtifactMoThenMoIsSuccessfullyDeleted() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        artifactMoOperations.deleteNodeArtifactMo(artifactMo);
        verify(liveBucket).deletePo(artifactMo);
    }

    @Test
    public void whenCreateRawArtifactDetailsThenArtifactReturnedWithExpectedAttributes() {
        final String artifactLocation = "/tmp/raw/artifact1.xml";
        final byte[] artifactContents = UNENCRYPTED_CONTENTS;

        when(resourceOperations.readArtifact(artifactLocation)).thenReturn(artifactContents);
        when(artifactMo.getAttribute("type")).thenReturn("type1");
        when(artifactMo.getAttribute("exportable")).thenReturn(true);
        when(artifactMo.getAttribute("encrypted")).thenReturn(false);
        when(artifactMo.getAttribute("importProgress")).thenReturn("NOT_STARTED");
        when(artifactMo.getAttribute("configurationNodeName")).thenReturn("node1");
        when(artifactMo.getAttribute("ignoreError")).thenReturn(true);

        final ArtifactDetails artifact = artifactMoOperations.createRawArtifactDetails(NODE_FDN, artifactLocation, artifactMo);
        assertEquals(NODE_FDN, artifact.getApNodeFdn());
        assertEquals(artifactLocation, artifact.getLocation());
        assertEquals("artifact1", artifact.getName());
        assertEquals("type1", artifact.getType());
        assertTrue(artifact.isExportable());
        assertEquals("node1", artifact.getConfigurationNodeName());
        assertEquals(true, artifact.isIgnoreError());
        assertArrayEquals(UNENCRYPTED_CONTENTS, artifact.getArtifactContentAsBytes());
    }

    @Test
    public void whenCreateRawArtifactDetailsWithEncryptedSetThenEncryptedIgnoredAndUnencryptedContentsReturned() {
        final String artifactLocation = "/tmp/raw/artifact1.xml";
        final byte[] artifactContents = UNENCRYPTED_CONTENTS;
        when(resourceOperations.readArtifact(artifactLocation)).thenReturn(artifactContents);
        when(artifactMo.getAttribute("type")).thenReturn("type1");
        when(artifactMo.getAttribute("exportable")).thenReturn(true);
        when(artifactMo.getAttribute("encrypted")).thenReturn(true);
        when(artifactMo.getAttribute("importProgress")).thenReturn("NOT_STARTED");
        when(artifactMo.getAttribute("ignoreError")).thenReturn(true);
        final ArtifactDetails artifact = artifactMoOperations.createRawArtifactDetails(NODE_FDN, artifactLocation, artifactMo);
        assertArrayEquals(UNENCRYPTED_CONTENTS, artifact.getArtifactContentAsBytes());
    }

    @Test
    public void whenCreateRawArtifactDetailsWithEncryptedSetThenEncryptedIgnoredAndUnencryptedContentsReturnedforSiteInstallFile() {
        final String artifactLocation = "/tmp/raw/artifact1.xml";
        final byte[] artifactContents = DECRYPTED_CONTENTS;
        when(resourceOperations.readArtifact(artifactLocation)).thenReturn(artifactContents);
        when(artifactMo.getAttribute("type")).thenReturn("siteInstallation");
        when(artifactMo.getAttribute("exportable")).thenReturn(true);
        when(artifactMo.getAttribute("encrypted")).thenReturn(true);
        when(artifactMo.getAttribute("importProgress")).thenReturn("NOT_STARTED");
        when(artifactMo.getAttribute("ignoreError")).thenReturn(true);
        final ArtifactDetails artifact = artifactMoOperations.createRawArtifactDetails(NODE_FDN, artifactLocation, artifactMo);
        assertArrayEquals(DECRYPTED_CONTENTS, artifact.getArtifactContentAsBytes());
    }

    @Test
    public void whenCreateRawArtifactDetailsWithEncryptedSetThenEncryptedIgnoredAndUnencryptedContentsReturnedforSiteInstallFileForException() {
        final String artifactLocation = "/tmp/raw/artifact2.xml";
        final byte[] artifactContents = UNENCRYPTED_CONTENTS;
        when(resourceOperations.readArtifact(artifactLocation)).thenReturn(artifactContents);
        when(cryptographyService.decrypt(artifactContents)).thenThrow(new CryptographyServiceDecryptionException("",null));
        when(artifactMo.getAttribute("type")).thenReturn("siteInstallation");
        when(artifactMo.getAttribute("exportable")).thenReturn(true);
        when(artifactMo.getAttribute("encrypted")).thenReturn(true);
        when(artifactMo.getAttribute("importProgress")).thenReturn("NOT_STARTED");
        when(artifactMo.getAttribute("ignoreError")).thenReturn(true);
        final ArtifactDetails artifact = artifactMoOperations.createRawArtifactDetails(NODE_FDN, artifactLocation, artifactMo);
        assertArrayEquals(UNENCRYPTED_CONTENTS, artifact.getArtifactContentAsBytes());
    }

    @Test
    public void whenCreateGeneratedArtifactDetailsWithEncryptedSetThenArtifactReturnedWithDecryptedContents() {
        final String artifactLocation = "/tmp/raw/artifact1.xml";
        final byte[] artifactContents = UNENCRYPTED_CONTENTS;

        when(resourceOperations.readArtifact(artifactLocation)).thenReturn(artifactContents);
        when(artifactMo.getAttribute("type")).thenReturn("type1");
        when(artifactMo.getAttribute("exportable")).thenReturn(true);
        when(artifactMo.getAttribute("encrypted")).thenReturn(true);
        when(artifactMo.getAttribute("importProgress")).thenReturn("NOT_STARTED");
        when(artifactMo.getAttribute("ignoreError")).thenReturn(true);

        final ArtifactDetails artifact = artifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, artifactLocation, artifactMo);
        assertArrayEquals(DECRYPTED_CONTENTS, artifact.getArtifactContentAsBytes());
    }
}
