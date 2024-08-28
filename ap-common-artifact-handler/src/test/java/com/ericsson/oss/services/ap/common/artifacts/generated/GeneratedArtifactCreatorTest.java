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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.itpf.smrs.SmrsAccount;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.NodeArtifactMoOperations;
import com.ericsson.oss.services.ap.common.artifacts.util.SmrsAccountOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;

/**
 * Unit tests for {@link GeneratedArtifactCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class GeneratedArtifactCreatorTest {

    private static final String ARTIFACT_TYPE = "Type1";

    private static final byte[] ENCRYPTED_CONTENTS = "encryptContents".getBytes();
    private static final String ARTIFACT_CONTENTS = "contents";
    private static final String GENERATED_DIRECTORY = DirectoryConfiguration.getGeneratedDirectory() + File.separator;

    private static final String NODE_NAME = "Node1";
    private static final String PROJECT_NAME = "Project1";

    private static final String EOI_GENERATED_DIRECTORY = DirectoryConfiguration.getArtifactsDirectory() + File.separator;

    private static final SmrsAccount SMRS_ACCOUNT = new SmrsAccount("user1", "/home/smrs/");

    @Mock
    private ArtifactResourceOperations artifactResourceOperations;

    @Mock
    private NodeArtifactMoOperations artifactMoOperations;

    @Mock
    private ManagedObject artifactMo;

    @Mock
    private SmrsAccountOperations smrsAccountOperations;

    @InjectMocks
    private GeneratedArtifactCreator generatedArtifactCreator;

    @Spy
    private final CryptographyService cyptographyService = new CryptographyService() {

        @Override
        public byte[] decrypt(final byte[] bytes) {
            return bytes;
        }

        @Override
        public byte[] encrypt(final byte[] bytes) {
            return ENCRYPTED_CONTENTS;
        }
    };

    private static final ArtifactDetails SMRS_ARTIFACT = new ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .name("smrsArtifact.xml")
            .type(ARTIFACT_TYPE)
            .artifactContent(ARTIFACT_CONTENTS)
            .build();

    private static final ArtifactDetails GENERATED_ARTIFACT_WITH_NO_LOCATION = new ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .name("generatedArtifact.xml")
            .type(ARTIFACT_TYPE)
            .artifactContent(ARTIFACT_CONTENTS)
            .exportable(true)
            .encrypted(true)
            .build();

    private static final ArtifactDetails GENERATED_ARTIFACT_WITH_LOCATION = new ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
            .name("generatedArtifact.xml")
            .type(ARTIFACT_TYPE)
            .artifactContent(ARTIFACT_CONTENTS)
            .location("/tmp/file1.xml")
            .fileFormat(ArtifactFileFormat.UNKNOWN)
            .ignoreError(false)
            .build();

    private static final ArtifactDetails Eoi_JSON_WITH_LOCATION = new ArtifactBuilder()
            .apNodeFdn(NODE_FDN)
                .artifactContent(ARTIFACT_CONTENTS)
                .type("EoiDay0Configuration")
                .exportable(false)
                .configurationNodeName(NODE_NAME)
                .encrypted(false)
                .fileFormat(ArtifactFileFormat.JSON)
                .name(NODE_NAME+"_day0.json")
                .location("/tmp/file1.json")
                .build();


    private final Collection<ManagedObject> artifactMos = new ArrayList<>();

    @Test
    public void whenCreateGeneratedArtifactWithNoLocationSetThenFileIsWrittenToGeneratedDirAndArtifactMoGeneratedLocationIsUpdated() {
        artifactMos.add(artifactMo);

        final String expectedGeneratedDir = GENERATED_DIRECTORY + PROJECT_NAME + File.separator + NODE_NAME + File.separator
                + GENERATED_ARTIFACT_WITH_NO_LOCATION.getNameWithExtension();

        when(artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);

        generatedArtifactCreator.createArtifactInGeneratedDir(GENERATED_ARTIFACT_WITH_NO_LOCATION);
        verify(artifactResourceOperations).writeArtifact(eq(expectedGeneratedDir), any(byte[].class));
        verify(artifactMo).setAttribute("exportable", true);
    }

    @Test
    public void whenCreateGeneratedArtifactWithEncryptedSetThenEncryptedFileIsWrittenToGeneratedDir() {
        artifactMos.add(artifactMo);

        final String expectedGeneratedDir = GENERATED_DIRECTORY + PROJECT_NAME + File.separator + NODE_NAME + File.separator
                + GENERATED_ARTIFACT_WITH_NO_LOCATION.getNameWithExtension();

        when(artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);

        generatedArtifactCreator.createArtifactInGeneratedDir(GENERATED_ARTIFACT_WITH_NO_LOCATION);

        verify(artifactResourceOperations).writeArtifact(expectedGeneratedDir, ENCRYPTED_CONTENTS);
    }

    @Test
    public void whenCreateGeneratedArtifactWithLocationSetThenArtifactIsWrittenToSpecifiedLocationAndArtifactMoGeneratedLocationIsUpdated() {
        artifactMos.add(artifactMo);

        final String expectedGeneratedDir = GENERATED_ARTIFACT_WITH_LOCATION.getLocation();

        when(artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);

        generatedArtifactCreator.createArtifactInGeneratedDir(GENERATED_ARTIFACT_WITH_LOCATION);
        verify(artifactResourceOperations).writeArtifact(eq(expectedGeneratedDir), any(byte[].class));
        verify(artifactMo).setAttribute("generatedLocation", expectedGeneratedDir);
    }


    @Test
    public void whenCreateEoiArtifactInArtifactDirMethodIsCalled() {

        final String expectedGeneratedDir = EOI_GENERATED_DIRECTORY + PROJECT_NAME + File.separator + NODE_NAME + File.separator
            + Eoi_JSON_WITH_LOCATION.getNameWithExtension();

        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), Eoi_JSON_WITH_LOCATION.getType());
        createParameters.put(NodeArtifactAttribute.NAME.toString(), Eoi_JSON_WITH_LOCATION.getName());
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), expectedGeneratedDir);
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), Eoi_JSON_WITH_LOCATION.isExportable());
        createParameters.put(NodeArtifactAttribute.ENCRYPTED.toString(), Eoi_JSON_WITH_LOCATION.isEncrypted());
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), Eoi_JSON_WITH_LOCATION.getConfigurationNodeName());
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.IN_PROGRESS.name());
        createParameters.put(NodeArtifactAttribute.FILE_FORMAT.toString(), Eoi_JSON_WITH_LOCATION.getFileFormat().name());

        generatedArtifactCreator.createEoiArtifactInArtifactDir(Eoi_JSON_WITH_LOCATION);
        verify(artifactResourceOperations).writeArtifact(eq(expectedGeneratedDir), any(byte[].class));
        verify(artifactMoOperations).createNodeArtifactMo(Eoi_JSON_WITH_LOCATION.getApNodeFdn(), createParameters);

    }

    @Test
    public void whenCreateGeneratedArtifactAndNoAssociatedRawFileThenArtifactMoIsCreated() {
        final String expectedGeneratedDir = GENERATED_DIRECTORY + PROJECT_NAME + File.separator + NODE_NAME + File.separator
                + GENERATED_ARTIFACT_WITH_NO_LOCATION.getNameWithExtension();

        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), GENERATED_ARTIFACT_WITH_NO_LOCATION.getType());
        createParameters.put(NodeArtifactAttribute.NAME.toString(), GENERATED_ARTIFACT_WITH_NO_LOCATION.getName());
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), expectedGeneratedDir);
        createParameters.put(NodeArtifactAttribute.ENCRYPTED.toString(), GENERATED_ARTIFACT_WITH_NO_LOCATION.isEncrypted());
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), GENERATED_ARTIFACT_WITH_NO_LOCATION.isExportable());
        createParameters.put(NodeArtifactAttribute.RAW_LOCATION.toString(), null);
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.NOT_STARTED.name());
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), null);
        createParameters.put(NodeArtifactAttribute.FILE_FORMAT.toString(), ArtifactFileFormat.UNKNOWN.name());

        when(artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(Collections.<ManagedObject> emptyList());

        generatedArtifactCreator.createArtifactInGeneratedDir(GENERATED_ARTIFACT_WITH_NO_LOCATION);
        verify(artifactResourceOperations).writeArtifact(eq(expectedGeneratedDir), any(byte[].class));
        verify(artifactMoOperations).createNodeArtifactMo(NODE_FDN, createParameters);
    }

    @Test
    public void whenCreateSmrsArtifactThenFileIsWrittenToSmrsDirAndArtifactMoGeneratedLocationIsUpdated() {
        final String expectedSmrsDir = SMRS_ACCOUNT.getHomeDirectory() + SMRS_ARTIFACT.getNameWithExtension();
        artifactMos.add(artifactMo);

        when(artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(artifactMos);
        when(smrsAccountOperations.getSmrsAccount(NODE_NAME, VALID_NODE_TYPE)).thenReturn(SMRS_ACCOUNT);

        generatedArtifactCreator.createArtifactInSmrsDir(SMRS_ARTIFACT, VALID_NODE_TYPE);
        verify(artifactResourceOperations).writeArtifact(eq(expectedSmrsDir), any(byte[].class));
        verify(artifactMo).setAttribute("generatedLocation", expectedSmrsDir);
    }

    @Test
    public void whenCreateSmrsArtifactAndNoAssociatedRawFileThenArtifactMoIsCreated() {
        final String expectedSmrsDir = SMRS_ACCOUNT.getHomeDirectory() + SMRS_ARTIFACT.getNameWithExtension();

        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), SMRS_ARTIFACT.getType());
        createParameters.put(NodeArtifactAttribute.NAME.toString(), SMRS_ARTIFACT.getName());
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), expectedSmrsDir);
        createParameters.put(NodeArtifactAttribute.RAW_LOCATION.toString(), null);
        createParameters.put(NodeArtifactAttribute.ENCRYPTED.toString(), false);
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), SMRS_ARTIFACT.isExportable());
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.NOT_STARTED.name());
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), null);
        createParameters.put(NodeArtifactAttribute.FILE_FORMAT.toString(), ArtifactFileFormat.UNKNOWN.name());

        when(artifactMoOperations.getNodeArtifactMosOfType(NODE_FDN, ARTIFACT_TYPE)).thenReturn(Collections.<ManagedObject> emptyList());
        when(smrsAccountOperations.getSmrsAccount(NODE_NAME, VALID_NODE_TYPE)).thenReturn(SMRS_ACCOUNT);

        generatedArtifactCreator.createArtifactInSmrsDir(SMRS_ARTIFACT, VALID_NODE_TYPE);
        verify(artifactMoOperations).createNodeArtifactMo(NODE_FDN, createParameters);
    }
}
