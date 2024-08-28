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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.exception.ArtifactFileNotFoundException;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link NodeArtifactMosCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeArtifactMosCreatorTest {

    private static final String DIRECTORY = "node1";

    private static final String ARTIFACT_TYPE = "siteBasic";
    private static final String ARTIFACT_NAME = "siteBasic.xml";
    private static final String ARTIFACT_CONTENT = "Site Basic Content";
    private static final String ARTIFACT_TYPE2 = "siteInstallation";
    private static final String ARTIFACT_NAME2 = "SiteInstall.xml";
    private static final String ARTIFACT_CONTENT2 = "site installation content";
    private static final String ARTIFACT_NAME3 = "radio.xml";
    private static final String ARTIFACT_CONTENT3 = "radio node content";
    private static final String ARTIFACT_TYPE4 = "baseline";
    private static final String ARTIFACT_NAME4 = "postIntegrationScript.mos";
    private static final String ARTIFACT_CONTENT4 = "moshell script content";
    private static final String INVALID_ARTIFACT_NAME = "siteBasic";
    private static final String TRUE = "true";

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RawArtifactHandler rawArtifactHandler;

    @Mock
    private NodeInfoReader nodeInfoReader;

    @Mock
    private NodeInfo nodeInfo;

    @Mock
    private ArchiveArtifact artifactArchive;

    @Mock
    private ArchiveArtifact artifactArchive2;

    @Mock
    private ArchiveArtifact artifactArchive3;

    @Mock
    private ArchiveArtifact artifactArchive4;

    @Mock
    private Archive projectArchive;

    @InjectMocks
    private NodeArtifactMosCreator nodeArtifactMosCreator;

    @Mock
    private NodeArtifactMoOperations nodeArtifactMoOperations;

    private final List<String> directoryNames = new ArrayList<>();

    private final Map<String, String> ignoreErrors = new HashMap<>();
    private final List<String> artifactFilenames = new ArrayList<>();
    private final List<String> artifactFilenames2 = new ArrayList<>();
    private final List<String> artifactFilenames4 = new ArrayList<>();
    private final List<ArtifactDetails> artifacts = new ArrayList<>();

    @Before
    public void setUp() {
        when(projectArchive.getAllDirectoryNames()).thenReturn(directoryNames);
        when(nodeInfoReader.read(projectArchive, DIRECTORY)).thenReturn(nodeInfo);
        when(nodeInfo.getName()).thenReturn(NODE_NAME);

        when(projectArchive.getArtifactOfNameInDir(DIRECTORY, ARTIFACT_NAME)).thenReturn(artifactArchive);
        when(projectArchive.getArtifactOfNameInDir(DIRECTORY, ARTIFACT_NAME2)).thenReturn(artifactArchive2);
        when(projectArchive.getArtifactOfNameInDir(DIRECTORY, ARTIFACT_NAME3)).thenReturn(artifactArchive3);
        when(artifactArchive.getContentsAsString()).thenReturn(ARTIFACT_CONTENT);
        when(artifactArchive2.getContentsAsString()).thenReturn(ARTIFACT_CONTENT2);
        when(artifactArchive3.getContentsAsString()).thenReturn(ARTIFACT_CONTENT3);
    }

    @Test
    public void whenCreateEmptyNodeArtifactMoThenZeroNodeArtifactMosAreCreated() {
        when(nodeInfo.getNodeArtifacts()).thenReturn(new HashMap<String, List<String>>());
        nodeArtifactMosCreator.createArtifactsAndMos(PROJECT_FDN, projectArchive);
        verify(rawArtifactHandler).createForProject(PROJECT_NAME, new ArrayList<ArtifactDetails>());
    }

    @Test
    public void whenCreateOneNodeArtifactMoThenNodeArtifactMoIsCreated() {
        directoryNames.add(DIRECTORY);
        artifactFilenames.add(ARTIFACT_NAME);
        artifacts.add(new ArtifactDetails.ArtifactBuilder()
                .name(ARTIFACT_NAME)
                .type(ARTIFACT_TYPE)
                .build());
        when(nodeInfo.getArtifactDetailsInStrictSequence()).thenReturn(artifacts);

        nodeArtifactMosCreator.createArtifactsAndMos(PROJECT_FDN, projectArchive);

        verify(rawArtifactHandler).createForProject(anyString(), argThat(new IsListOfArtifactDetailsWithOneArtifact()));
    }

    @Test
    public void whenCreateBaselineArtifactMoAndArtifactExistInDirectoryThenNodeArtifactMoIsCreated() {
        when(projectArchive.getArtifactOfNameInDir(DIRECTORY, ARTIFACT_NAME4)).thenReturn(artifactArchive4);
        when(artifactArchive4.getContentsAsString()).thenReturn(ARTIFACT_CONTENT4);
        directoryNames.add(DIRECTORY);
        artifactFilenames4.add(ARTIFACT_NAME4);
        for (final String artifactFilename:artifactFilenames4){
            artifacts.add(new ArtifactDetails.ArtifactBuilder()
                    .name(artifactFilename)
                    .type(ARTIFACT_TYPE4)
                    .build());
        }
        ignoreErrors.put(ARTIFACT_NAME4, TRUE);
        when(nodeInfo.getArtifactDetailsInStrictSequence()).thenReturn(artifacts);
        when(nodeInfo.getIgnoreErrors()).thenReturn(ignoreErrors);

        nodeArtifactMosCreator.createArtifactsAndMos(PROJECT_FDN, projectArchive);

        verify(rawArtifactHandler).createForProject(anyString(), argThat(new IsListOfArtifactDetailsWithBaselineArtifact()));
    }

    @Test
    public void whenCreateBaselineArtifactMoAndArtifactNotExistInDirectoryThenNodeArtifactMoIsCreated() {
        when(projectArchive.getArtifactOfNameInDir(DIRECTORY, ARTIFACT_NAME4)).thenReturn(null);
        directoryNames.add(DIRECTORY);
        artifactFilenames4.add(ARTIFACT_NAME4);
        for (final String artifactFilename:artifactFilenames4){
            artifacts.add(new ArtifactDetails.ArtifactBuilder()
                    .name(artifactFilename)
                    .type(ARTIFACT_TYPE4)
                    .build());
        }
        when(nodeInfo.getArtifactDetailsInStrictSequence()).thenReturn(artifacts);

        nodeArtifactMosCreator.createArtifactsAndMos(PROJECT_FDN, projectArchive);

        verify(rawArtifactHandler).createForProject(anyString(), argThat(new IsListOfArtifactDetailsWithBaselineArtifact()));
    }

    @Test
    public void whenCreateTwoNodeArtifactsAndMosThenNodeArtifactMoIsCreated() {
        directoryNames.add(DIRECTORY);
        artifactFilenames.add(ARTIFACT_NAME);
        artifactFilenames2.add(ARTIFACT_NAME2);
        for (final String artifactFilename:artifactFilenames){
            artifacts.add(new ArtifactDetails.ArtifactBuilder()
                    .name(artifactFilename)
                    .type(ARTIFACT_TYPE)
                    .build());
        }
        for (final String artifactFilename:artifactFilenames2){
            artifacts.add(new ArtifactDetails.ArtifactBuilder()
                    .name(artifactFilename)
                    .type(ARTIFACT_TYPE2)
                    .build());
        }
        when(nodeInfo.getArtifactDetailsInStrictSequence()).thenReturn(artifacts);

        nodeArtifactMosCreator.createArtifactsAndMos(PROJECT_FDN, projectArchive);

        verify(rawArtifactHandler).createForProject(anyString(), argThat(new IsListOfArtifactDetailsWithTwoArtifacts()));
    }

    @Test
    public void whenCreateOneNodeArtifactMoAndArtifactFileIsNotFoundAnArtifactNotFoundExceptionIsThrown() {
        directoryNames.add(DIRECTORY);
        artifactFilenames.add(INVALID_ARTIFACT_NAME);
        for (final String artifactFilename:artifactFilenames){
            artifacts.add(new ArtifactDetails.ArtifactBuilder()
                    .name(artifactFilename)
                    .type(ARTIFACT_TYPE)
                    .build());
        }
        when(nodeInfo.getArtifactDetailsInStrictSequence()).thenReturn(artifacts);

        expectedException.expect(ArtifactFileNotFoundException.class);

        nodeArtifactMosCreator.createArtifactsAndMos(PROJECT_FDN, projectArchive);

        verify(rawArtifactHandler, times(0)).createForProject(anyString(), argThat(new IsListOfArtifactDetailsWithOneArtifact()));
    }

    private static class IsListOfArtifactDetailsWithOneArtifact extends ArgumentMatcher<List<ArtifactDetails>> {

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(final Object list) {
            final List<ArtifactDetails> artifacts = (List<ArtifactDetails>) list;
            return artifacts.size() == 1 && artifacts.get(0).getApNodeFdn().equals(NODE_FDN);
        }
    }

    private static class IsListOfArtifactDetailsWithTwoArtifacts extends ArgumentMatcher<List<ArtifactDetails>> {

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(final Object list) {
            final List<ArtifactDetails> artifacts = (List<ArtifactDetails>) list;
            return artifacts.size() == 2 && artifacts.get(0).getType().equals(ARTIFACT_TYPE) && artifacts.get(1).getType().equals(ARTIFACT_TYPE2);
        }
    }

    private static class IsListOfArtifactDetailsWithBaselineArtifact extends ArgumentMatcher<List<ArtifactDetails>> {

        @Override
        @SuppressWarnings("unchecked")
        public boolean matches(final Object list) {
            final List<ArtifactDetails> artifacts = (List<ArtifactDetails>) list;
            return artifacts.size() == 1 && artifacts.get(0).getType().equals(ARTIFACT_TYPE4);
        }
    }
}