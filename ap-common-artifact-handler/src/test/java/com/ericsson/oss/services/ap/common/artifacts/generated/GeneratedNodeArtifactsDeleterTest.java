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
package com.ericsson.oss.services.ap.common.artifacts.generated;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.SmrsAccountOperations;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Unit tests for {@link GeneratedNodeArtifactsDeleter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneratedNodeArtifactsDeleterTest {

    private static final String ARTIFACT_TYPE = "Type1";
    private static final String HARDWARE_SERIAL_NUMBER_VALUE_2 = HARDWARE_SERIAL_NUMBER_VALUE + "2.xml";

    private static final String GENERATED_PROJECT_DIRECTORY = DirectoryConfiguration.getGeneratedDirectory() + File.separator + PROJECT_NAME;
    private static final String GENERATED_NODE_DIRECTORY = GENERATED_PROJECT_DIRECTORY + File.separator + NODE_NAME;

    private final Collection<ManagedObject> artifactMos = new ArrayList<>();

    @Mock
    private ArtifactResourceOperations artifactResourceOperations;

    @Mock
    private SmrsAccountOperations smrsAccountOperations;

    @Mock
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> queryExecutor;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private Logger logger;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject projectMo;

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private ManagedObject artifactMo;

    @Mock
    private ResourceService resourceService;

    @Mock
    private Resource resource;

    @InjectMocks
    private GeneratedNodeArtifactsDeleter artifactDeleter;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        when(nodeMo.getParent()).thenReturn(projectMo);
        when(nodeMo.getName()).thenReturn(NODE_NAME);
        when(nodeMo.getAttribute("nodeType")).thenReturn(VALID_NODE_TYPE);
        when(nodeMo.getAttribute("hardwareSerialNumber")).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        when(projectMo.getName()).thenReturn(PROJECT_NAME);
    }

    @Test
    public void whenDeleteArtifactsForTypeAndTwoArtifactOfTypesExistsThenBothFilesAreDeleted() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1").thenReturn("/artifact2");

        artifactDeleter.deleteAllOfType(NODE_FDN, ARTIFACT_TYPE);
        verify(artifactResourceOperations).deleteFile("/artifact1");
        verify(artifactResourceOperations).deleteFile("/artifact2");
    }

    @Test
    public void whenDeleteArtifactsForTypeAndTwoNodesExistThenBothNodeArtifactMosAreDeleted() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1").thenReturn("/artifact2");

        artifactDeleter.deleteAllOfType(NODE_FDN, ARTIFACT_TYPE);
        verify(nodeArtifactMoOperations, times(2)).deleteNodeArtifactMo(artifactMo);
    }

    @Test
    public void whenDeleteArtifactsForTypeAndRawLocationExistsThenGeneratedLocationForArtifactMoIsUpdated() {
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/file1");

        artifactDeleter.deleteAllOfType(NODE_FDN, ARTIFACT_TYPE);
        verify(artifactMo, times(1)).setAttribute("generatedLocation", null);
    }

    @Test
    public void whenDeleteArtifactsForTypeAndNoArtifactsOfTypeExistsThenDoNothing() {
        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(Collections.<ManagedObject> emptyList());

        artifactDeleter.deleteAllOfType(NODE_FDN, ARTIFACT_TYPE);
        verifyZeroInteractions(artifactResourceOperations);
        verify(nodeArtifactMoOperations, never()).deleteNodeArtifactMo(artifactMo);
    }

    @Test
    public void whenDeleteArtifactsForTypeFromSmrsDirAndLastArtifactInDirThenSmrsAccountIsDeleted() {
        final ManagedObject artifactContainerMo = Mockito.mock(ManagedObject.class);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/home/smrs/artifact1.xml");
        when(artifactResourceOperations.isSingleFileInDirectory("/home/smrs/artifact1.xml")).thenReturn(true);
        when(artifactMo.getParent()).thenReturn(artifactContainerMo);
        when(artifactContainerMo.getParent()).thenReturn(nodeMo);
        when(nodeMo.getAttribute("nodeType")).thenReturn(VALID_NODE_TYPE);
        when(nodeMo.getName()).thenReturn(NODE_NAME);

        artifactDeleter.deleteAllOfType(NODE_FDN, ARTIFACT_TYPE);
        verify(smrsAccountOperations).deleteSmrsAccount(NODE_NAME, VALID_NODE_TYPE);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeThenTheGeneratedNodeDirectoryIsDeleted() {
        when(artifactResourceOperations.directoryExistAndNotEmpty(GENERATED_NODE_DIRECTORY)).thenReturn(true);
        artifactDeleter.deleteAllGeneratedArtifactsForNode(NODE_FDN, false);
        verify(artifactResourceOperations).deleteDirectory(GENERATED_NODE_DIRECTORY);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeThenTheSmrsAccountIsDeleted() {
        when(artifactResourceOperations.directoryExistAndNotEmpty(GENERATED_NODE_DIRECTORY)).thenReturn(true);
        artifactDeleter.deleteAllGeneratedArtifactsForNode(NODE_FDN, false);
        verify(smrsAccountOperations).deleteSmrsAccount(NODE_NAME, VALID_NODE_TYPE);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeThenBindFileIsDeleted() {
        when(artifactResourceOperations.directoryExistAndNotEmpty(GENERATED_NODE_DIRECTORY)).thenReturn(true);
        artifactDeleter.deleteAllGeneratedArtifactsForNode(NODE_FDN, false);
        verify(artifactResourceOperations)
            .deleteFile(DirectoryConfiguration.getBindDirectory() + File.separator + HARDWARE_SERIAL_NUMBER_VALUE + ".xml");
    }

    @Test
    public void whenDeleteAllArtifactsForNodeAndUpdateArtifactMoFlagIsTrueThenGeneratedLocationIsNullForAllNodeArtifactMos() {
        final ManagedObject nodeArtifactContainerMo = Mockito.mock(ManagedObject.class);
        final ManagedObject nodeArtifactMo = Mockito.mock(ManagedObject.class);
        final Collection<ManagedObject> nodeArtifactMos = new ArrayList<>();
        nodeArtifactMos.add(nodeArtifactMo);
        nodeArtifactMos.add(nodeArtifactMo);
        nodeArtifactMos.add(nodeArtifactMo);

        when(nodeMo.getChild("NodeArtifactContainer=1")).thenReturn(nodeArtifactContainerMo);
        when(nodeArtifactContainerMo.getChildren()).thenReturn(nodeArtifactMos);
        when(nodeArtifactMo.getAttribute("rawLocation")).thenReturn("/file1");
        when(nodeArtifactMo.getAttribute("generatedLocation")).thenReturn("/file");

        artifactDeleter.deleteAllGeneratedArtifactsForNode(NODE_FDN, true);
        verify(nodeArtifactMo, times(3)).setAttribute("generatedLocation", null);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeWithModelUpdateAndAssociatedRawArtifactsThenGeneratedLocationIsNullForAllNodeArtifactMo() {
        final ManagedObject nodeArtifactContainerMo = Mockito.mock(ManagedObject.class);
        final ManagedObject nodeArtifactMo = Mockito.mock(ManagedObject.class);
        final Collection<ManagedObject> nodeArtifactMos = new ArrayList<>();
        nodeArtifactMos.add(nodeArtifactMo);
        nodeArtifactMos.add(nodeArtifactMo);
        nodeArtifactMos.add(nodeArtifactMo);

        when(nodeMo.getChild("NodeArtifactContainer=1")).thenReturn(nodeArtifactContainerMo);
        when(nodeArtifactContainerMo.getChildren()).thenReturn(nodeArtifactMos);
        when(nodeArtifactMo.getAttribute("generatedLocation")).thenReturn("/file1");

        artifactDeleter.deleteAllGeneratedArtifactsForNode(NODE_FDN, true);
        verify(nodeArtifactMoOperations, times(3)).deleteNodeArtifactMo(nodeArtifactMo);
    }

    @Test
    public void whenDeleteAllArtifactsForProjectWithModelUpdateAndNoAssociatedRawArtifactsThenAllNodeArtifactMosAreDeleted() {
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, "ap", "Node")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(new ArrayList<ManagedObject>().iterator());
        when(artifactResourceOperations.directoryExists(GENERATED_PROJECT_DIRECTORY)).thenReturn(true);

        artifactDeleter.deleteAllGeneratedArtifactsForProject(PROJECT_FDN);

        verify(artifactResourceOperations).deleteDirectory(GENERATED_PROJECT_DIRECTORY);
    }

    @Test
    public void whenDeleteAllArtifactsForProjectThenSmrsAccountIsDeletedForAllNodesInProject() {
        final List<ManagedObject> nodeMos = new ArrayList<>();
        nodeMos.add(nodeMo);
        nodeMos.add(nodeMo);

        when(nodeMo.getName()).thenReturn(NODE_NAME).thenReturn("node2");
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, "ap", "Node")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(nodeMos.iterator());
        when(artifactResourceOperations.directoryExists(GENERATED_PROJECT_DIRECTORY)).thenReturn(true);

        artifactDeleter.deleteAllGeneratedArtifactsForProject(PROJECT_FDN);

        verify(smrsAccountOperations).deleteSmrsAccount(NODE_NAME, VALID_NODE_TYPE);
        verify(smrsAccountOperations).deleteSmrsAccount("node2", VALID_NODE_TYPE);
    }

    @Test
    public void whenDeleteAllArtifactsForProjectThenBindFileIsDeletedForAllNodesInProject() {
        final List<ManagedObject> nodeMos = new ArrayList<>();
        nodeMos.add(nodeMo);
        nodeMos.add(nodeMo);

        when(nodeMo.getName()).thenReturn(NODE_NAME).thenReturn("node2");
        when(nodeMo.getAttribute("hardwareSerialNumber")).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE_2);
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, "ap", "Node")).thenReturn(queryExecutor);
        when(queryExecutor.execute()).thenReturn(nodeMos.iterator());
        when(artifactResourceOperations.directoryExists(GENERATED_PROJECT_DIRECTORY)).thenReturn(true);

        artifactDeleter.deleteAllGeneratedArtifactsForProject(PROJECT_FDN);

        verify(artifactResourceOperations)
            .deleteFile(DirectoryConfiguration.getBindDirectory() + File.separator + HARDWARE_SERIAL_NUMBER_VALUE + ".xml");
        verify(artifactResourceOperations)
            .deleteFile(DirectoryConfiguration.getBindDirectory() + File.separator + HARDWARE_SERIAL_NUMBER_VALUE_2 + ".xml");
    }

    @Test
    public void whenIfProjectDirectoryDoesNotExistsOrEmptyThenDeleteSmrsAccountNotCalled() {
        when(artifactResourceOperations.directoryExists(GENERATED_PROJECT_DIRECTORY)).thenReturn(false);
        artifactDeleter.deleteAllGeneratedArtifactsForProject(PROJECT_FDN);
        verify(smrsAccountOperations, times(0)).deleteSmrsAccount(anyString(), anyString());
    }

    @Test
    public void whenIfNodeDirectoryDoesNotExistOrIsEmptyThenDeleteSmrsAccountNotCalled() {
        when(artifactResourceOperations.directoryExistAndNotEmpty(GENERATED_NODE_DIRECTORY)).thenReturn(false);
        artifactDeleter.deleteAllGeneratedArtifactsForNode(NODE_FDN, false);
        verify(smrsAccountOperations, times(0)).deleteSmrsAccount(anyString(), anyString());
    }
}
