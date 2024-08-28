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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ArtifactNotFoundException;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;

/**
 * Unit tests for {@link GeneratedArtifactHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneratedArtifactHandlerTest {

    @Mock
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    @Mock
    private ManagedObject artifactMo;

    @InjectMocks
    public GeneratedArtifactHandler generatedHandler;

    private final ArtifactDetails artifact1 = new ArtifactDetails.ArtifactBuilder().apNodeFdn(NODE_FDN).type("type1").build();
    private final ArtifactDetails artifact2 = new ArtifactDetails.ArtifactBuilder().apNodeFdn(NODE_FDN).type("type2").build();

    private final Collection<ManagedObject> artifactMos = new ArrayList<>();

    @Test
    public void whenReadRawArtifactsForNodeThenReturnAllArtifacts() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMos(NODE_FDN)).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = generatedHandler.readAllForNode(NODE_FDN);
        assertEquals(2, result.size());
    }

    @Test
    public void whenReadRawArtifactsForNodeWithNoArtifactsThenReturnEmptyList() {
        when(nodeArtifactMoOperations.getNodeArtifactMos(NODE_FDN)).thenReturn(Collections.<ManagedObject> emptyList());
        final Collection<ArtifactDetails> result = generatedHandler.readAllForNode(NODE_FDN);
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenReadArtifactsForTypeAndOneArtifactOfTypeExistsThenReturnArtifact() {
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);

        final Collection<ArtifactDetails> result = generatedHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertEquals(1, result.size());
    }

    @Test
    public void whenReadArtifactsForTypeAndTwoArtifactOfTypeExistsThenReturnArtifacts() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = generatedHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertEquals(2, result.size());
    }

    @Test
    public void whenReadArtifactsForTypeAndNoArtifactOfTypeExistsThenReturnEmptyList() {
        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(Collections.<ManagedObject> emptyList());
        final Collection<ArtifactDetails> result = generatedHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenReadFirstArtifactForTypeAndOneArtifactOfTypeExistsThenReturnArtifact() {
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);

        final ArtifactDetails result = generatedHandler.readFirstOfType(NODE_FDN, artifact1.getType());
        assertEquals(artifact1, result);
    }

    @Test
    public void whenReadFirstArtifactForTypeAndTwoArtifactsOfTypeExistThenReturnFirstArtifact() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("/artifact1").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact1", artifactMo)).thenReturn(artifact1);
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact1);

        final ArtifactDetails result = generatedHandler.readFirstOfType(NODE_FDN, artifact1.getType());
        assertEquals(artifact1, result);
    }

    @Test(expected = ArtifactNotFoundException.class)
    public void whenReadFirstArtifactForTypeAndNoArtifactOfTypeExistsThenArtifactNotFoundExceptionIsThrown() {
        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(Collections.<ManagedObject> emptyList());
        generatedHandler.readFirstOfType(NODE_FDN, artifact1.getType());
    }

    @Test
    public void whenReadArtifactsForTypeAndRawLocationIsNotSetThenReturnEmptyList() {
        artifactMos.add(artifactMo);
        artifactMos.add(artifactMo);

        when(nodeArtifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, artifact1.getType())).thenReturn(artifactMos);
        when(artifactMo.getAttribute("generatedLocation")).thenReturn("").thenReturn("/artifact2");
        when(nodeArtifactMoOperations.createGeneratedArtifactDetails(NODE_FDN, "/artifact2", artifactMo)).thenReturn(artifact2);

        final Collection<ArtifactDetails> result = generatedHandler.readAllOfType(NODE_FDN, artifact1.getType());
        assertEquals(1, result.size());
    }

    @Test
    public void getNodeConfigurationFileFullpathForNodeThenReturnCorrectFullPath() {
        final String fullpath = GeneratedArtifactHandler.getNetconfPreconfigurationFileFullpathForNode(NODE_FDN);
        assertEquals(fullpath, "/ericsson/autoprovisioning/artifacts/generated/Project1/Node1/preconfiguration_Node1.xml");
    }
}
