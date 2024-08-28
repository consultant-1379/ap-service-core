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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
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
import com.ericsson.oss.itpf.datalayer.dps.object.builder.ManagedObjectBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactFileFormat;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;

/**
 * Unit tests for {@link RawArtifactsCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RawArtifactsCreatorTest {

    private static final String NODE_ARTIFACT_CONTAINER_FDN = NODE_FDN + ",NodeArtifactContainer=1";

    private static final String RBS_SUMMARY_TYPE = "RbsSummary";
    private static final String RBS_SUMMARY_FILE_CONTENTS = "RbsSummary...";
    private static final String ARTIFACT_NAME = "Athlone-East-RbsSummary.xml";
    private static final String CONFIGURATION_NODE_NAME = "remote-node01";
    private static final Boolean IGNORE_ERROR = false;

    private static final String SITE_INTSALL_TYPE = "siteInstallation";
    private static final String ARTIFACT_NAME1 = "SiteInstallation.xml";
    private static final String SITE_INTSALL_FILE_CONTENT = "SiteInstallation...";

    private static final byte[] ENCRYPTED_CONTENTS = "encryptContents".getBytes(StandardCharsets.UTF_8);

    private static final String RAW_BASE_DIR = "/tmp/raw";
    private static final String RAW_PROJECT_DIRECTORY_PATH = RAW_BASE_DIR + File.separator + PROJECT_NAME;
    private static final String RAW_FILE_PATH = RAW_PROJECT_DIRECTORY_PATH + File.separator + NODE_NAME + File.separator + ARTIFACT_NAME;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ArtifactResourceOperations artifactResourceOperations;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObjectBuilder moBuilder;

    @Mock
    private ManagedObject parentMo;

    @Mock
    private ManagedObject artifactMo;

    @Spy
    private final CryptographyService cyptographyService = new CryptographyService() {

        @Override
        public byte[] decrypt(final byte[] bytes) {
            return bytes;
        }

        @Override
        public byte[] encrypt(final byte[] bytes) {
            return ENCRYPTED_CONTENTS.clone();
        }
    };

    @InjectMocks
    private final RawArtifactsCreator rawArtifactsCreator = new RawArtifactsCreator();

    private final List<ArtifactDetails> rawArtifacts = new ArrayList<>();

    private final ArtifactDetails artifactDetails = new ArtifactBuilder().apNodeFdn(NODE_FDN)
        .name(ARTIFACT_NAME)
        .type(RBS_SUMMARY_TYPE)
        .artifactContent(RBS_SUMMARY_FILE_CONTENTS)
        .configurationNodeName(CONFIGURATION_NODE_NAME)
        .fileFormat(ArtifactFileFormat.UNKNOWN)
        .ignoreError(IGNORE_ERROR)
        .build();

    private final ArtifactDetails artifactDetails4 = new ArtifactBuilder().apNodeFdn(NODE_FDN)
        .name(ARTIFACT_NAME1)
        .type(SITE_INTSALL_TYPE)
        .artifactContent(SITE_INTSALL_FILE_CONTENT)
        .configurationNodeName(CONFIGURATION_NODE_NAME)
        .fileFormat(ArtifactFileFormat.UNKNOWN)
        .ignoreError(IGNORE_ERROR)
        .build();

    @Before
    public void setUp() {
        rawArtifacts.add(artifactDetails);

        when(dpsService.getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_ARTIFACT_CONTAINER_FDN)).thenReturn(parentMo);
        when(liveBucket.getManagedObjectBuilder()).thenReturn(moBuilder);

        when(moBuilder.type(MoType.NODE_ARTIFACT.toString())).thenReturn(moBuilder);
        when(moBuilder.parent(parentMo)).thenReturn(moBuilder);
        when(moBuilder.addAttributes(anyMapOf(String.class, Object.class))).thenReturn(moBuilder);
        when(moBuilder.create()).thenReturn(artifactMo);
    }

    @Test
    public void whenCreateRawArtifactsTheFileIsWrittenToTheSharedFilesystem() {
        rawArtifactsCreator.createProjectArtifacts(PROJECT_NAME, rawArtifacts);
        verify(artifactResourceOperations, times(1)).writeArtifacts(anyMapOf(String.class, byte[].class));
    }

    @Test
    public void whenCreateRawArtifactTheNodeArtifactMoIsCreated() {
        final Map<String, Object> createParameters = new HashMap<>();
        createParameters.put(NodeArtifactAttribute.NAME.toString(), ARTIFACT_NAME);
        createParameters.put(NodeArtifactAttribute.TYPE.toString(), RBS_SUMMARY_TYPE);
        createParameters.put(NodeArtifactAttribute.GEN_LOCATION.toString(), null);
        createParameters.put(NodeArtifactAttribute.RAW_LOCATION.toString(), RAW_FILE_PATH);
        createParameters.put(NodeArtifactAttribute.EXPORTABLE.toString(), true);
        createParameters.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.NOT_STARTED.name());
        createParameters.put(NodeArtifactAttribute.FILE_FORMAT.toString(), ArtifactFileFormat.UNKNOWN);
        createParameters.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), CONFIGURATION_NODE_NAME);
        createParameters.put(NodeArtifactAttribute.IGNORE_ERROR.toString(), IGNORE_ERROR);

        rawArtifactsCreator.createProjectArtifacts(PROJECT_NAME, rawArtifacts);
        verify(moBuilder, times(1)).create();
    }

    @Test
    public void whenCreateMultipleArtifactsThenTheSameNumberOfMosAreCreated() {
        final ArtifactDetails artifactDetails2 = new ArtifactBuilder().apNodeFdn(NODE_FDN)
            .name(ARTIFACT_NAME)
            .type(RBS_SUMMARY_TYPE)
            .artifactContent(RBS_SUMMARY_FILE_CONTENTS)
            .configurationNodeName(CONFIGURATION_NODE_NAME)
            .fileFormat(ArtifactFileFormat.UNKNOWN)
            .ignoreError(IGNORE_ERROR)
            .build();
        final ArtifactDetails artifactDetails3 = new ArtifactBuilder().apNodeFdn(NODE_FDN)
            .name(ARTIFACT_NAME)
            .type(RBS_SUMMARY_TYPE)
            .artifactContent(RBS_SUMMARY_FILE_CONTENTS)
            .configurationNodeName(CONFIGURATION_NODE_NAME)
            .fileFormat(ArtifactFileFormat.UNKNOWN)
            .ignoreError(IGNORE_ERROR)
            .build();
        rawArtifacts.add(artifactDetails2);
        rawArtifacts.add(artifactDetails3);

        rawArtifactsCreator.createProjectArtifacts(PROJECT_NAME, rawArtifacts);
        verify(moBuilder, times(3)).create();
    }

    @Test
    public void testFilePermissionUsingPosix() throws UnsupportedOperationException {

        final List<ArtifactDetails> rawArtifacts1 = new ArrayList<>();

        rawArtifacts1.add(artifactDetails4);
        Set<PosixFilePermission> expectedPermissions = new HashSet<>();
        expectedPermissions.add(PosixFilePermission.OWNER_READ);
        expectedPermissions.add(PosixFilePermission.OWNER_WRITE);
        rawArtifactsCreator.createProjectArtifacts(PROJECT_NAME, rawArtifacts1);
        assertTrue(rawArtifacts1.get(0).getType().equalsIgnoreCase(SITE_INTSALL_TYPE));
    }

}
