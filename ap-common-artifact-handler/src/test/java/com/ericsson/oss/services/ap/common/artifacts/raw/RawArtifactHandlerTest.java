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
package com.ericsson.oss.services.ap.common.artifacts.raw;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Unit tests for {@link RawArtifactHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RawArtifactHandlerTest {

    private static final String RAW_DIRECTORY = DirectoryConfiguration.getRawDirectory() + File.separator;

    @Mock
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Mock
    private ArtifactResourceOperations artifactResourceOperations;

    @Mock
    private RawArtifactsCreator rawArtifactsCreator;

    @Mock
    private ManagedObject artifactMo;

    private final ArtifactDetails artifact1 = new ArtifactDetails.ArtifactBuilder().apNodeFdn(NODE_FDN).type("type1").build();
    private final ArtifactDetails artifact2 = new ArtifactDetails.ArtifactBuilder().apNodeFdn(NODE_FDN).type("type2").build();
    private final Collection<ManagedObject> artifactMos = new ArrayList<>();

    @InjectMocks
    public RawArtifactHandler rawHandler;

    @Test
    public void whenReadRawArtifactsForNodeThenReturnAllArtifacts() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMos(NODE_FDN)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = rawHandler.readAllForNode(NODE_FDN);
        assertEquals(2, result.size());
    }

    @Test
    public void whenReadRawArtifactsForNodeWithNoArtifactsThenReturnEmptyList() {
        when(nodeArtifactMoOperations.getNodeArtifactMos(NODE_FDN)).thenReturn(Collections.<ManagedObject> emptyList());
        final Collection<ArtifactDetails> result = rawHandler.readAllForNode(NODE_FDN);
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenReadArtifactsForTypeAndOneArtifactOfTypeExistsThenReturnArtifact() {
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = rawHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertEquals(1, result.size());
    }

    @Test
    public void whenReadArtifactsForTypeAndTwoArtifactOfTypeExistsThenReturnArtifacts() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = rawHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertEquals(2, result.size());
    }

    @Test
    public void whenReadArtifactsForTypeAndNoArtifactOfTypeExistsThenReturnEmptyList() {
        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(Collections.<ManagedObject> emptyList());

        final Collection<ArtifactDetails> result = rawHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenReadArtifactsForTypeAndRawLocationIsNotSetThenReturnEmptyList() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = rawHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertEquals(1, result.size());
    }

    @Test
    public void whenReadFirstArtifactForTypeAndOneArtifactOfTypeExistsThenReturnArtifact() {
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final ArtifactDetails result = rawHandler.readFirstOfType(NODE_FDN, artifact1.getType());
        assertEquals(artifact1, result);
    }

    @Test
    public void whenReadFirstArtifactForTypeAndTwoArtifactsOfTypeExistThenReturnFirstArtifact() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createRawArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final ArtifactDetails result = rawHandler.readFirstOfType(NODE_FDN, artifact1.getType());
        assertEquals(artifact1, result);
    }

    @Test(expected = ArtifactNotFoundException.class)
    public void whenReadFirstArtifactForTypeAndNoArtifactOfTypeExistsThenArtifactNotFoundExceptionIsThrown() {
        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(Collections.<ManagedObject> emptyList());
        rawHandler.readFirstOfType(NODE_FDN, artifact1.getType());
    }

    @Test
    public void whenDeleteArtifactsForTypeAndTwoArtifactOfTypesExistsThenBothFilesAreDeleted() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, "type1")).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");

        rawHandler.deleteAllOfType(NODE_FDN, "type1");
        verify(artifactResourceOperations).deleteFile("/artifact1");
        verify(artifactResourceOperations).deleteFile("/artifact2");
    }

    @Test
    public void whenDeleteArtifactsForTypeAndTwoArtifactOfTypesExistsThenBothArtifactMosAreDeleted() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, "type1")).thenReturn(artifactMos);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");

        rawHandler.deleteAllOfType(NODE_FDN, "type1");
        verify(nodeArtifactMoOperations, times(2)).deleteNodeArtifactMo(artifactMo);
    }

    @Test
    public void whenDeleteArtifactsForTypeAndNoArtifactsOfTypeExistsThenDoNothing() {
        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, "type1")).thenReturn(Collections.<ManagedObject> emptyList());

        rawHandler.deleteAllOfType(NODE_FDN, "type1");
        verifyZeroInteractions(artifactResourceOperations);
        verify(nodeArtifactMoOperations, never()).deleteNodeArtifactMo(artifactMo);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeThenTheRawNodeDirectoryIsDeleted() {
        rawHandler.deleteAllForNodeWithNoModelUpdate(NODE_FDN);
        final String rawNodeDir = RAW_DIRECTORY + PROJECT_NAME + File.separator + NODE_NAME;
        verify(artifactResourceOperations).deleteDirectory(rawNodeDir);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeWithModelUpdatesAndAssociatedGeneratedArtifactsThenRawLocationIsSetToNull() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMos(NODE_FDN)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1");
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");

        rawHandler.deleteAllForNode(NODE_FDN);
        verify(artifactMo, times(2)).setAttribute("rawLocation", null);
    }

    @Test
    public void whenDeleteAllArtifactsForNodeWithModelUpdatesAndNoAssociatedGeneratedArtifactsThenNodeArtifactMosAreDeleted() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMos(NODE_FDN)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn(null);
        when(artifactMo.getAttribute("rawLocation")).thenReturn("/artifact1").thenReturn("/artifact2");

        rawHandler.deleteAllForNode(NODE_FDN);
        verify(nodeArtifactMoOperations, times(2)).deleteNodeArtifactMo(artifactMo);
    }

    @Test
    public void whenDeleteAllArtifactsForProjectThenTheRawProjectDirectoryIsDeleted() {
        rawHandler.deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        final String rawProjectDir = RAW_DIRECTORY + PROJECT_NAME;
        verify(artifactResourceOperations).deleteDirectory(rawProjectDir);
    }

    @Test
    public void whenCreateArtifactInRawDirThenTheFileIsCreated() {
        final List<ArtifactDetails> rawArtifacts = new ArrayList<>();
        rawArtifacts.add(artifact1);
        rawHandler.createForProject(PROJECT_NAME, rawArtifacts);
        verify(rawArtifactsCreator).createProjectArtifacts(PROJECT_NAME, rawArtifacts);
    }
}
